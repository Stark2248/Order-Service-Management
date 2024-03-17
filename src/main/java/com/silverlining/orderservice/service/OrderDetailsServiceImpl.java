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
import java.util.*;
import java.util.stream.Collectors;

import static com.silverlining.orderservice.constants.OrderContants.*;

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
    public Optional<UserDto> getUser(String userId) {
        String uri = "http://USER-WS/user-management/users/"+userId;

        ResponseEntity<UserDto> responseUser = restTemplate.getForEntity(uri,UserDto.class);
        if(responseUser.getStatusCode().equals(HttpStatus.NOT_FOUND)){
            return Optional.empty();
        }

        return Optional.of(responseUser.getBody());
    }

    @Transactional
    @Override
    public String placeOrder(List<Cart> cartItems, String userId, String location) {

        if (getUser(userId).isEmpty()) {
            return "No User Found";
        }

        Optional<List<WarehouseDto>> optionalWarehouseDtoLists = getWarehouseDtos(location);
        if (optionalWarehouseDtoLists.isEmpty()) return "no items are available at the moment";
        List<WarehouseDto> warehouseDtoList = optionalWarehouseDtoLists.get();



        Optional<List<ProductDto>> optionalProductDtos = getItemProductDto(cartItems);
        if (optionalProductDtos.isEmpty()) return "Invalid Product Ids. please check again";

        String valid = validate(cartItems,warehouseDtoList);
        if(!valid.equals(VALID)){
            return valid;
        }

        List<ProductDto> productDtos = optionalProductDtos.get();

        LOGGER.info("productdto size: {{}}",productDtos.size());

        double totalPrice = getTotalPrice(cartItems, productDtos);
        LOGGER.info("Total Price: {{}}",totalPrice);

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        Order order = saveOrder(userId, location, totalPrice);
        LOGGER.info("Orderid : {{}}",order.getOrderId());

        try {
            updateWarehouseAndSaveOrderDetails(cartItems, location, order, productDtos, mapper, warehouseDtoList);
        } catch (Exception e) {
            LOGGER.error(e);
            LOGGER.info("Error Order Status");
            order.setOrderStatus(OrderStatus.ERROR.name());
            orderService.saveOrder(order);
        }
        return order.getOrderStatus();
    }

    private static OrderDetailDto getOrderDetailDto(Order order, List<ProductDto> productDtos, Cart item) {
        OrderDetailDto orderDetailDto = new OrderDetailDto();
        String serialId = item.getSerialId();
        orderDetailDto.setSerialId(serialId);

        orderDetailDto.setOrder(order);
        orderDetailDto.setQuantity(item.getQuantity());
        double price = 0;
        for (ProductDto productDto : productDtos) {
            if (productDto.getSerialId().equals(item.getSerialId())) {
                price = item.getQuantity() * productDto.getPrice();
            }
        }

        orderDetailDto.setPrice(price);
        return orderDetailDto;
    }

    private void updateWarehouseAndSaveOrderDetails(List<Cart> cartItems, String location, Order order, List<ProductDto> productDtos, ModelMapper mapper, List<WarehouseDto> warehouseDtoList) {
        try {
            for (Cart item : cartItems) {
                OrderDetailDto orderDetailDto = getOrderDetailDto(order, productDtos, item);
                OrderDetail orderDetail = mapper.map(orderDetailDto, OrderDetail.class);

                Optional<WarehouseDto> optionalWarehouseDto = OrderUtilities.getWarehouseDtoFromList(warehouseDtoList, item.getSerialId());
                optionalWarehouseDto.ifPresent(warehouseDto -> updateWarehouseQty(location, item, warehouseDto, orderDetail));

                detailsRepository.save(orderDetail);
            }
        } catch (Exception e) {
            LOGGER.error("Exception in updateWarehouseAndSaveOrderDetails: ",e);
            throw new RuntimeException(e);

        }
    }

    private void updateWarehouseQty(String location, Cart item, WarehouseDto warehouseDto, OrderDetail orderDetail) {

        orderDetail.setLocation(warehouseDto.getLocation());
        int newQty = warehouseDto.getQuantity() - item.getQuantity();
        warehouseDto.setQuantity(newQty);
        HttpHeaders httpHeader2 = new HttpHeaders();
        httpHeader2.setContentType(MediaType.APPLICATION_JSON);


        HttpEntity<WarehouseDto> requestEntity = new HttpEntity<>(warehouseDto,httpHeader2);

        String uri = WAREHOUSE_URL + location +"/"+ item.getSerialId();

        restTemplate.put(uri,requestEntity);
    }

    private Order saveOrder(String userId, String location, double totalPrice) {
        OrderDto orderDto = new OrderDto();
        orderDto.setUserId(userId);
        orderDto.setDate(LocalDateTime.now());
        orderDto.setOrderStatus(OrderStatus.PLACED.name());
        orderDto.setTotalPrice(totalPrice);
        orderDto.setLocation(location);
        return orderService.createOrder(orderDto);

    }

    private Optional<List<ProductDto>> getItemProductDto(List<Cart> cartItems) {

        List<String> serialIds = cartItems.stream().map(Cart::getSerialId).toList();

        return getProductDtosBySerialIds(serialIds);
    }

    private Optional<List<ProductDto>> getProductDtosBySerialIds(List<String> serialIds) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<String>> entity = new HttpEntity<>(serialIds, httpHeaders);
        ParameterizedTypeReference<List<ProductDto>> responseType = new ParameterizedTypeReference<List<ProductDto>>() {
        };
        ResponseEntity<List<ProductDto>> responseItemList = restTemplate.exchange(PRODUCTS_ALL_URL, HttpMethod.POST, entity, responseType);
        if (responseItemList.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
            return Optional.empty();
        }
        return Optional.ofNullable(responseItemList.getBody());
    }

    private Optional<List<WarehouseDto>> getWarehouseDtos(String location) {
        ParameterizedTypeReference<List<WarehouseDto>> responseType2 = new ParameterizedTypeReference<List<WarehouseDto>>() {};
        String uri ="http://product-ws/warehouse/locations/"+ location;
        ResponseEntity<List<WarehouseDto>> warehouseDtoResponse = restTemplate.exchange(uri,HttpMethod.GET,null,responseType2);

        if (warehouseDtoResponse.getStatusCode().equals(HttpStatus.NO_CONTENT)){
            return Optional.empty();
        }
        return Optional.ofNullable(warehouseDtoResponse.getBody());
    }

    private static String validate(List<Cart> items, List<WarehouseDto> warehouseDtoList) {
        List<String> serialIds = items.stream().map(Cart::getSerialId).toList();

        Map<String, Integer> serialQtyMap = items.stream().collect(Collectors.toMap(Cart::getSerialId, Cart::getQuantity));

        for (String serial : serialIds) {
            Optional<WarehouseDto> dtoOptional = OrderUtilities.getWarehouseDtoFromList(warehouseDtoList, serial);
            if (dtoOptional.isEmpty()) {
                return "productId:" + serial + " not found in the warehouse";
            }

            WarehouseDto warehouseDto = dtoOptional.get();
            if (serialQtyMap.get(serial) > warehouseDto.getQuantity()) {
                return warehouseDto.getName() + " Only " + warehouseDto.getQuantity() + " much available in stock at the moment. Please try getting less or wait until new stocks arrives. Thank you";
            }
            if (warehouseDto.getQuantity() == 0) {
                return warehouseDto.getName() + " is out of stock. try from different location.";
            }
        }
        return VALID;
    }

    private double getTotalPrice(List<Cart> cart, List<ProductDto> productDtos) {
        Map<String, Integer> serialQtyMap = cart.stream().collect(Collectors.toMap(Cart::getSerialId, Cart::getQuantity));
        LOGGER.info("Serial qty map: {{}}", serialQtyMap);
        double totalprice = 0;
        int qty;
        for (ProductDto productDto : productDtos) {
            String serialId = productDto.getSerialId();
            if (serialQtyMap.containsKey(serialId)) {
                LOGGER.info("Serial Id Present in Map: {{}}", serialId);
                qty = serialQtyMap.get(serialId);
                totalprice = totalprice + (qty * productDto.getPrice());
                LOGGER.info("Total price of {{}} : {{}}",serialId,totalprice);
            }

        }
        LOGGER.info("Final Total Price: {{}}",totalprice);
        return totalprice;
    }

    @Override
    public List<ProductDto> fetchProducts() {
        ParameterizedTypeReference<List<ProductDto>> responseType = new ParameterizedTypeReference<List<ProductDto>>() {};
        ResponseEntity<List<ProductDto>> responseLists = restTemplate.exchange(PRODUCTS_ALL_URL,HttpMethod.GET, null, responseType);
        if(responseLists.getStatusCode().equals(HttpStatus.NO_CONTENT))
            return Collections.emptyList();
        return responseLists.getBody();
    }

    @Override
    public Optional<List<ProductDto>> fetchProductByLocation(String location) {
        Optional<List<WarehouseDto>> optionalWarehouseDtos = getWarehouseDtos(location);
        return optionalWarehouseDtos.map(dtoList -> dtoList.stream()
                                                           .map(WarehouseDto::getSerialId)
                                                           .toList())
                                     .map(serialIds -> getProductDtosBySerialIds(serialIds)
                                                .orElse(Collections.emptyList()));

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
