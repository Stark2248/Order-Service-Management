package com.silverlining.orderservice.httpmodels;

public class OrderRequestModel {
    String serialId;
    int quantity;

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

    @Override
    public String toString() {
        return "OrderRequestModel{" +
                "serialId='" + serialId + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
