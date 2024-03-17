package com.silverlining.orderservice.repository;

import com.silverlining.orderservice.models.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailsRepository extends JpaRepository<OrderDetail,Integer> {

    @Query("Select o from OrderDetail o  where o.order.orderId = :orderId")
    List<OrderDetail> findByOrderId(@Param("orderId") String orderId);

}
