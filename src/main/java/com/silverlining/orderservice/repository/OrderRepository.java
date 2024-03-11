package com.silverlining.orderservice.repository;

import com.silverlining.orderservice.models.Order;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends CrudRepository<Order,String> {
}
