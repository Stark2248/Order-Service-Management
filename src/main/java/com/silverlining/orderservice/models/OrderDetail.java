package com.silverlining.orderservice.models;

import jakarta.persistence.*;

@Entity
@Table(name = "orderdetail")
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    String id;

    @OneToMany
    @JoinColumn(name = "orderId", referencedColumnName = "orderId")
    Order order;

    @Column(name = "serialId", nullable = false)
    String serialId;

    @Column(name = "quantity", nullable = false)
    int quantity;

    @Column(name = "price", nullable = false)
    double price;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getSerialId() {
        return serialId;
    }

    public void setSerialId(String serialId) {
        this.serialId = serialId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
