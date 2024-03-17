package com.silverlining.orderservice.service;

import com.silverlining.orderservice.dto.OrderDto;
import com.silverlining.orderservice.constants.OrderStatus;
import com.silverlining.orderservice.models.Order;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public interface OrderService {

    public Order saveOrder(Order order);

    public Order createOrder(OrderDto order);

    public OrderDto fetch(String orderId);

    public List<OrderDto>  fetchAll();

    public List<OrderDto> fetchUserOrders(String userId);

    public LocalDateTime fetchDate(String orderId);

    public OrderStatus fetchStatus(String orderId);

    public void updateStatus(String orderId, OrderStatus status);

    public void updateTotalPrice(String orderId, double totalPrice);



}
