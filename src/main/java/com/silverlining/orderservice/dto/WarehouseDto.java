package com.silverlining.orderservice.dto;

public class WarehouseDto {
    String serialId;
    String name;
    int quantity;

    String location;

    public WarehouseDto(){}

    public WarehouseDto(String serialId, String name, int quantity, String location) {
        this.serialId = serialId;
        this.name = name;
        this.quantity = quantity;
        this.location = location;
    }



    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSerialId() {
        return serialId;
    }

    public void setSerialId(String serialId) {
        this.serialId = serialId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
