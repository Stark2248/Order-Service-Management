package com.silverlining.orderservice.utils;

import com.silverlining.orderservice.dto.OrderDto;
import com.silverlining.orderservice.dto.ProductDto;
import com.silverlining.orderservice.dto.WarehouseDto;
import com.silverlining.orderservice.models.Order;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import java.util.List;

public class OrderUtilities {

    public static OrderDto getOrderDto(Order order){
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return mapper.map(order,OrderDto.class);
    }

    public static WarehouseDto getWarehouseDtoFromList(List<WarehouseDto> dtoList, String serialId){
        for(WarehouseDto dto : dtoList){
            if(dto.getSerialId().equals(serialId)){
                return dto;
            }
        }
        return null;
    }

}
