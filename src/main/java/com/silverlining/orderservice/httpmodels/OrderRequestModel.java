package com.silverlining.orderservice.httpmodels;

public class OrderRequestModel {
    String serialId;
    int qty;

    public String getSerialId() {
        return serialId;
    }

    public void setSerialId(String serialId) {
        this.serialId = serialId;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }
}
