package com.silverlining.orderservice.service;

import com.silverlining.orderservice.constants.OrderStatus;
import com.silverlining.orderservice.dto.OrderDto;
import com.silverlining.orderservice.models.Order;
import com.silverlining.orderservice.repository.OrderRepository;
import com.silverlining.orderservice.utils.OrderUtilities;
import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class OrderServiceImpl implements OrderService{

    private OrderRepository orderRepository;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository){
        this.orderRepository=orderRepository;
    }

    @Override
    public Order createOrder(OrderDto order) {
        Order od = new Order();
        if(StringUtils.isEmpty(order.getOrderId())){
            String id = UUID.randomUUID().toString();
            order.setOrderId(id);
            od.setOrderId(id);
        }

        od.setUserId(order.getUserId());
        od.setTotalPrice(order.getTotalPrice());
        od.setDate(order.getDate());
        od.setLocation(order.getLocation());
        od.setOrderStatus(order.getOrderStatus());

        return orderRepository.saveAndFlush(od);



    }
    @Override
    public Order saveOrder(Order order) {
        return orderRepository.saveAndFlush(order);

    }



    @Override
    public OrderDto fetch(String orderId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        return optionalOrder.map(OrderUtilities::getOrderDto).orElse(null);
    }

    @Override
    public List<OrderDto> fetchAll() {
        List<OrderDto> dtoList = new ArrayList<>();
        orderRepository.findAll().forEach(order -> {
            OrderDto dto = OrderUtilities.getOrderDto(order);
            dtoList.add(dto);
        });
        return dtoList;
    }

    @Override
    public List<OrderDto> fetchUserOrders(String userId) {
        List<OrderDto> dtoList =  orderRepository.findByUserId(userId).stream().map(OrderUtilities::getOrderDto).toList();
        if(!dtoList.isEmpty())
            return dtoList;
        return Collections.emptyList();
    }

    @Override
    public LocalDateTime fetchDate(String orderId) {
        return fetch(orderId).getDate();
    }

    @Override
    public OrderStatus fetchStatus(String orderId) {
        return OrderStatus.findByName(fetch(orderId).getOrderStatus());
    }

    @Override
    public void updateStatus(String orderId, OrderStatus status) {
        OrderDto dto = fetch(orderId);
        if(dto == null){
            return;
        }
        dto.setOrderStatus(status.name());
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        Order order = mapper.map(dto,Order.class);
        orderRepository.save(order);

    }

    @Override
    public void updateTotalPrice(String orderId, double totalPrice) {
        OrderDto dto = fetch(orderId);
        if(dto == null){
            return;
        }
        dto.setTotalPrice(totalPrice);
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        Order order = mapper.map(dto,Order.class);
        orderRepository.save(order);
    }
}
