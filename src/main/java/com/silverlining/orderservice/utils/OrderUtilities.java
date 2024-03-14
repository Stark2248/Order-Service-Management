package com.silverlining.orderservice.utils;

import com.silverlining.orderservice.dto.OrderDto;
import com.silverlining.orderservice.models.Order;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

public class OrderUtilities {

    public static OrderDto getOrderDto(Order order){
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return mapper.map(order,OrderDto.class);
    }

}
