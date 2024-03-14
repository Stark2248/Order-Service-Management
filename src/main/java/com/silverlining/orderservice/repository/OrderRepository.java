package com.silverlining.orderservice.repository;

import com.silverlining.orderservice.dto.OrderDto;
import com.silverlining.orderservice.models.Order;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends CrudRepository<Order,String> {
    List<Order> findByUserId(String userId);
}
