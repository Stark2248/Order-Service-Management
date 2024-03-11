package com.silverlining.orderservice.service;

import com.silverlining.orderservice.dto.OrderDto;
import com.silverlining.orderservice.models.Order;
import com.silverlining.orderservice.utils.OrderStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public interface OrderService {

    public void save(OrderDto order);

    public OrderDto fetch(String orderId);

    public List<OrderDto>  fetchAll();

    public List<OrderDto> fetchUserOrders(String userId);

    public LocalDateTime fetchDate(String orderId);

    public OrderStatus fetchStatus(String orderId);



}
