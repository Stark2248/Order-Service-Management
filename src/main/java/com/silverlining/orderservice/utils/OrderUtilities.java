package com.silverlining.orderservice.utils;

import com.silverlining.orderservice.dto.OrderDto;
import com.silverlining.orderservice.dto.WarehouseDto;
import com.silverlining.orderservice.models.Order;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import java.util.List;
import java.util.Optional;

public class OrderUtilities {

    public static OrderDto getOrderDto(Order order){
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return mapper.map(order,OrderDto.class);
    }

    public static Optional<WarehouseDto> getWarehouseDtoFromList(List<WarehouseDto> dtoList, String serialId){
        return dtoList.stream().filter(dto -> dto.getSerialId().equalsIgnoreCase(serialId)).findFirst();
    }

}
