package com.silverlining.orderservice.service;

import com.silverlining.orderservice.constants.OrderStatus;
import com.silverlining.orderservice.dto.*;
import com.silverlining.orderservice.models.Order;
import com.silverlining.orderservice.models.OrderDetail;
import com.silverlining.orderservice.repository.OrderDetailsRepository;
import com.silverlining.orderservice.utils.OrderUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OrderDetailsServiceImpl implements OrderDetailsService{

    private static final Logger LOGGER = LogManager.getLogger(OrderDetailsServiceImpl.class);

    OrderService orderService;

    RestTemplate restTemplate;

    OrderDetailsRepository detailsRepository;

    @Autowired
    public OrderDetailsServiceImpl(OrderService orderService, OrderDetailsRepository detailsRepository, RestTemplate restTemplate){
        this.orderService = orderService;
        this.detailsRepository =detailsRepository;
        this.restTemplate=restTemplate;
    }


    @Override
    public UserDto getUser(String userId) {
        String uri = "http://USER-WS/user-management/users/"+userId;

        ResponseEntity<UserDto> responseUser = restTemplate.getForEntity(uri,UserDto.class);
        if(responseUser.getStatusCode().equals(HttpStatus.NOT_FOUND)){
            return null;
        }

        return responseUser.getBody();
    }

    @Transactional
    @Override
    public String placeOrder(List<Cart> cartItems, String userId, String location) {

        String uri = "http://USER-WS/user-management/users/"+userId;

        ResponseEntity<UserDto> responseUser = restTemplate.getForEntity(uri,UserDto.class);
        if(responseUser.getStatusCode().equals(HttpStatus.NOT_FOUND)){
            return "User not found";
        }
        ParameterizedTypeReference<List<WarehouseDto>> responseType2 = new ParameterizedTypeReference<List<WarehouseDto>>() {};
        uri ="http://product-ws/warehouse/locations/"+location;
        ResponseEntity<List<WarehouseDto>> warehouseDtoResponse = restTemplate.exchange(uri,HttpMethod.GET,null,responseType2);

        if (warehouseDtoResponse.getStatusCode().equals(HttpStatus.NO_CONTENT)){
            return "no items are available at the moment";
        }
        List<WarehouseDto> warehouseDtoLists = warehouseDtoResponse.getBody();

        List<String> serialIds = cartItems.stream().map(item -> item.getSerialId()).collect(Collectors.toList());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<String>> entity = new HttpEntity<>(serialIds,httpHeaders);
        ParameterizedTypeReference<List<ProductDto>> responseType = new ParameterizedTypeReference<List<ProductDto>>() {};

        uri="http://product-ws/products/all";
        ResponseEntity<List<ProductDto>> responseLists = restTemplate.exchange(uri,HttpMethod.POST, entity, responseType);
        if(responseLists.getBody().isEmpty())
            return "Invalid Product Ids. please check again";

        String valid = validate(cartItems,warehouseDtoLists);
        if(!valid.equals("Valid")){
            return valid;
        }

        List<ProductDto> productDtos = responseLists.getBody();
        double totalPrice = getTotalPrice(cartItems,productDtos);


        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        OrderDto orderDto = new OrderDto();
        orderDto.setUserId(userId);
        orderDto.setDate(LocalDateTime.now());
        orderDto.setOrderStatus(OrderStatus.PLACED.name());
        orderDto.setTotalPrice(totalPrice);
        orderDto.setLocation(location);
        orderService.save(orderDto);
        Order order = mapper.map(orderDto,Order.class);
        LOGGER.info("Orderid : {{}}",orderDto.getOrderId());
        for(Cart item : cartItems){
            OrderDetailDto orderDetailDto = new OrderDetailDto();
            String serialId=item.getSerialId();

            orderDetailDto.setOrder(order);
            orderDetailDto.setQuantity(item.getQuantity());
            double price = 0;
            for(ProductDto productDto: productDtos){
                if(productDto.getSerialId().equals(item.getSerialId())){
                    price = item.getQuantity()*productDto.getPrice();
                }
            }

            orderDetailDto.setPrice(price);
            OrderDetail orderDetail = mapper.map(orderDetailDto,OrderDetail.class);

            WarehouseDto warehouseDto = OrderUtilities.getWarehouseDtoFromList(warehouseDtoLists,item.getSerialId());

            int newQty = warehouseDto.getQuantity() - item.getQuantity();

            HttpHeaders httpHeader2 = new HttpHeaders();
            httpHeader2.setContentType(MediaType.APPLICATION_JSON);

            warehouseDto.setQuantity(newQty);

            HttpEntity<WarehouseDto> requestEntity = new HttpEntity<>(warehouseDto,httpHeader2);

            uri = "http://product-ws/warehouse/"+location+"/"+item.getSerialId();

            restTemplate.put(uri,requestEntity);

            detailsRepository.save(orderDetail);
        }
        return OrderStatus.PLACED.name();
    }

    private static String validate(List<Cart> items, List<WarehouseDto> warehouseDtoList){
        List<String> serialIds = items.stream().map(item -> item.getSerialId()).collect(Collectors.toList());

        Map<String, Integer> serialQtyMap = items.stream().collect(Collectors.toMap(k -> k.getSerialId(), v -> v.getQuantity()));

        for(String serial : serialIds){
            WarehouseDto dto = OrderUtilities.getWarehouseDtoFromList(warehouseDtoList,serial);
            if(dto == null){
                return "productId:"+serial+" not found in the warehouse";
            }
            if(serialQtyMap.get(serial)>dto.getQuantity()){
                return dto.getName()+" Only "+dto.getQuantity()+" much available in stock at the moment. Please try getting less or wait until new stocks arrives. Thank you";
            }
            if(dto.getQuantity() == 0){
                return dto.getName()+" is out of stock. try from different location.";
            }
        }
        return "Valid";
    }

    private double getTotalPrice(List<Cart> cart, List<ProductDto> productDtos){
        double totalPrice = 0;

        //List<String> serialIds = cart.stream().map(item -> item.getSerialId()).collect(Collectors.toList());
        Map<String, Integer> serialQtyMap = cart.stream().collect(Collectors.toMap(k -> k.getSerialId(), v -> v.getQuantity()));
        double totalprice = 0;
        int qty;
        for(ProductDto productDto: productDtos){
            if(serialQtyMap.containsKey(productDto.getSerialId())){
                qty = serialQtyMap.get(productDto.getSerialId());
                totalprice = totalprice + (qty * productDto.getPrice());
            }
        }
        return totalPrice;
    }

    @Override
    public List<ProductDto> fetchProducts() {
        ParameterizedTypeReference<List<ProductDto>> responseType = new ParameterizedTypeReference<List<ProductDto>>() {};
        String uri="http://product-ws/products/all";
        ResponseEntity<List<ProductDto>> responseLists = restTemplate.exchange(uri,HttpMethod.GET, null, responseType);
        if(responseLists.getStatusCode().equals(HttpStatus.NO_CONTENT))
            return Collections.emptyList();
        return responseLists.getBody();
    }

    @Override
    public List<ProductDto> fetchProductByLocation(String location) {
        ParameterizedTypeReference<List<WarehouseDto>> responseType2 = new ParameterizedTypeReference<List<WarehouseDto>>() {};
        String uri ="http://product-ws/warehouse/locations/"+location;
        ResponseEntity<List<WarehouseDto>> warehouseDtoResponse = restTemplate.exchange(uri,HttpMethod.GET,null,responseType2);
        ParameterizedTypeReference<List<ProductDto>> responseType = new ParameterizedTypeReference<List<ProductDto>>() {};
        uri = "http://product-ws/products/all";
        ResponseEntity<List<ProductDto>> responseLists = restTemplate.exchange(uri,HttpMethod.GET, null, responseType);
        if(responseLists.getStatusCode().equals(HttpStatus.NO_CONTENT))
            return Collections.emptyList();

        List<ProductDto> productDtoList = new ArrayList<>();
        for(ProductDto productDto : responseLists.getBody()){
            WarehouseDto warehouseDto = OrderUtilities.getWarehouseDtoFromList(warehouseDtoResponse.getBody(),productDto.getSerialId());
            if(warehouseDto != null){
                productDtoList.add(productDto);
            }
        }

        if(!productDtoList.isEmpty()){
            return productDtoList;
        }
        return Collections.emptyList();
    }

    @Override
    public List<OrderDetailDto> fetchOrderDetailsByOrderId(String orderId) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        List<OrderDetail> orderDetails = detailsRepository.findByOrderId(orderId);
        if(!orderDetails.isEmpty()){
            List<OrderDetailDto> dtoList = new ArrayList<>();
            for(OrderDetail orderDetail : orderDetails){
                dtoList.add(mapper.map(orderDetail,OrderDetailDto.class));
            }
            return dtoList;
        }
        return Collections.emptyList();
    }
}
