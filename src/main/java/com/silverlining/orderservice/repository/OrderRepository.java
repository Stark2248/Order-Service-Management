package com.silverlining.orderservice.repository;

import com.silverlining.orderservice.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order,String> {

    //@Query("Select o.* from Order o where o.userId = :userId")
    List<Order> findByUserId(String userId);
}
