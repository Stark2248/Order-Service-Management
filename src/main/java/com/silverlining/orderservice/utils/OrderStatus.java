package com.silverlining.orderservice.utils;

import java.util.Objects;

public enum OrderStatus {

    DELIVERED("DLVR"),
    CANCELLED("CNCL"),
    PLACED("PLCD"),
    SHIPPED("SHPD");




    private final String abbreviation;
    OrderStatus(String abbreviation) {
        this.abbreviation = abbreviation;
    }
    public String getAbbreviation() {
        return abbreviation;
    }

    public static OrderStatus findByName(String status){
        OrderStatus result = null;
        for(OrderStatus orderStatus : values()){
            if(Objects.equals(status,orderStatus.name()) || Objects.equals(status,orderStatus.getAbbreviation())){
                result =orderStatus;
            }
        }
        return result;
    }

}
