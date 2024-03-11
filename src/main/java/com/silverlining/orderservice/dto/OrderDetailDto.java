package com.silverlining.orderservice.dto;

import com.silverlining.orderservice.models.Order;
import jakarta.persistence.*;

public class OrderDetailDto {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int id;

    @OneToMany
    @JoinColumn(name = "orderId", referencedColumnName = "orderId")
    Order order;

    @Column(name = "serialId", nullable = false)
    String serialId;

    @Column(name = "quantity", nullable = false)
    int quantity;

    @Column(name = "price", nullable = false)
    double price;
}
