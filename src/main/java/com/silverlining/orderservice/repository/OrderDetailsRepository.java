package com.silverlining.orderservice.repository;

import com.silverlining.orderservice.models.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailsRepository extends JpaRepository<OrderDetail,Integer> {
}
