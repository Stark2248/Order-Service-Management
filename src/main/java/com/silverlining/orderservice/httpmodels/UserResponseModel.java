package com.silverlining.orderservice.httpmodels;

import com.silverlining.orderservice.dto.OrderDto;

import java.util.List;

public class UserResponseModel {

    String userId;
    String firstName;
    String lastName;
    List<OrderDto> orderList;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<OrderDto> getOrderList() {
        return orderList;
    }

    public void setOrderList(List<OrderDto> orderList) {
        this.orderList = orderList;
    }
}
