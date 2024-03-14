package com.silverlining.orderservice.service;

import com.silverlining.orderservice.constants.OrderStatus;
import com.silverlining.orderservice.dto.Cart;
import com.silverlining.orderservice.dto.OrderDetailDto;
import com.silverlining.orderservice.dto.OrderDto;
import com.silverlining.orderservice.dto.ProductDto;
import com.silverlining.orderservice.models.Order;
import com.silverlining.orderservice.models.OrderDetail;
import com.silverlining.orderservice.repository.OrderDetailsRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
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


    @Transactional
    @Override
    public String placeOrder(List<Cart> cartItems, String userId, String location) {

        String url = "http://USER-WS/users/"+userId;


        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        OrderDto orderDto = new OrderDto();
        orderDto.setUserId(userId);
        orderDto.setDate(LocalDateTime.now());
        orderDto.setOrderStatus(OrderStatus.PLACED.name());
        double totalPrice = getTotalPrice(cartItems,location); // check for -1
        orderDto.setTotalPrice(totalPrice);
        orderDto.setLocation(location);
        //orderService.save(orderDto);
        LOGGER.info("Orderid : {{}}",orderDto.getOrderId());
        for(Cart item : cartItems){
            OrderDetailDto orderDetailDto = new OrderDetailDto();
            String serialId=item.getSerialId();
            String uri = "http://PRODUCT-WS/products/"+serialId;
            ResponseEntity<ProductDto> responseEntityProduct = restTemplate.getForEntity(uri,ProductDto.class);
            uri = "http://PRODUCT-WS/warehouse/stock/"+location+"/"+serialId;
            ProductDto productDto = responseEntityProduct.getBody();
            if(productDto!=null) {
                ResponseEntity<Integer> responseEntityQty = restTemplate.getForEntity(uri, Integer.class);
                if (responseEntityQty.getBody() != null) {
                    int warehouseQuantity = responseEntityQty.getBody();
                    if (warehouseQuantity == -1) {
                        return productDto.getName() + " Product out of stock or not available in the given location.";
                    }else if(warehouseQuantity < item.getQuantity()){
                        return productDto.getName() + " Only " + warehouseQuantity + " much available in stock at the moment. Please try getting less or wait until new stocks arrives. Thank you.";
                    }else{
                        orderDetailDto.setSerialId(serialId);
                        orderDetailDto.setPrice(productDto.getPrice()*item.getQuantity());
                        orderDetailDto.setOrder(mapper.map(orderDto, Order.class));
                        orderDetailDto.setQuantity(item.getQuantity());
                        OrderDetail orderDetail =mapper.map(orderDetailDto,OrderDetail.class);
                        detailsRepository.save(orderDetail);
                        return OrderStatus.PLACED.name();
                    }
                }
            }
        }
        return "unsuccesful";
    }

    private double getTotalPrice(List<Cart> cart, String location){
        double totalPrice = 0;

        List<String> serialIds = cart.stream().map(item -> item.getSerialId()).collect(Collectors.toList());
        Map<String, Integer> serialQtyMap = cart.stream().collect(Collectors.toMap(k -> k.getSerialId(), v -> v.getQuantity()));
        for(Cart item: cart){
            String serialId=item.getSerialId();
            String uri = "http://PRODUCT-WS/products/"+serialId;
            ResponseEntity<ProductDto> responseEntityProduct = restTemplate.getForEntity(uri,ProductDto.class);
            uri = "http://PRODUCT-WS/warehouse/stock/"+location+"/"+serialId;
            ProductDto productDto = responseEntityProduct.getBody();
            if(productDto!=null) {
                ResponseEntity<Integer> responseEntityQty = restTemplate.getForEntity(uri, Integer.class);
                if (responseEntityQty.getBody() != null) {
                    int warehouseQuantity = responseEntityQty.getBody();
                    if (warehouseQuantity == -1) {
                        return -1;
                    }else if(warehouseQuantity < item.getQuantity()){
                        return -1;
                    }else{
                        totalPrice+=(productDto.getPrice()*item.getQuantity());
                    }
                }
            }
        }
        return totalPrice;
    }

    @Override
    public List<ProductDto> fetchProducts() {
        return null;
    }

    @Override
    public List<ProductDto> fetchProductByLocation(String location) {
        return null;
    }

    @Override
    public List<OrderDetailDto> fetchOrderDetailsByOrderId(String orderId) {
        return null;
    }
}
