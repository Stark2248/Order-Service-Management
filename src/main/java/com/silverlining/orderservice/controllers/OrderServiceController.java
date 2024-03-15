package com.silverlining.orderservice.controllers;

import com.silverlining.orderservice.constants.OrderStatus;
import com.silverlining.orderservice.dto.Cart;
import com.silverlining.orderservice.dto.OrderDto;
import com.silverlining.orderservice.dto.ProductDto;
import com.silverlining.orderservice.dto.UserDto;
import com.silverlining.orderservice.httpmodels.OrderRequestModel;
import com.silverlining.orderservice.httpmodels.ProductResponseModel;
import com.silverlining.orderservice.httpmodels.UserResponseModel;
import com.silverlining.orderservice.service.OrderDetailsService;
import com.silverlining.orderservice.service.OrderService;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
public class OrderServiceController {

    OrderDetailsService orderDetailsService;
    OrderService orderService;

    @Autowired
    public OrderServiceController(OrderDetailsService orderDetailsService, OrderService orderService){
        this.orderDetailsService = orderDetailsService;
        this.orderService = orderService;
    }

    @GetMapping("{userId}")
    public ResponseEntity<?> getUserDetails(@PathVariable(name = "userId") String userId){
        UserDto userDto = orderDetailsService.getUser(userId);
        if(userDto == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No user present with user id "+userId);
        }
        UserResponseModel responseModel = new UserResponseModel();
        responseModel.setUserId(userDto.getId());
        responseModel.setFirstName(userDto.getFirstName());
        responseModel.setLastName(userDto.getLastName());
        List<OrderDto> orderDtoList = orderService.fetchUserOrders(userId);
        if(orderDtoList.isEmpty()){
            responseModel.setOrderList(Collections.emptyList());
        }else{
            responseModel.setOrderList(orderDtoList);
        }
        return ResponseEntity.status(HttpStatus.FOUND).body(responseModel);
    }

    @GetMapping("{userId}/browse/{location}")
    public ResponseEntity<List<ProductResponseModel>> getProductByLoaction(@PathVariable(name = "userId") String userId, @PathVariable(name = "location") String location){
        UserDto userDto = orderDetailsService.getUser(userId);
        if(userDto == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        List<ProductDto> productDtos = orderDetailsService.fetchProductByLocation(location);
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        List<ProductResponseModel> responseModelList = new ArrayList<>();
        responseModelList = productDtos.stream().map(productDto -> mapper.map(productDto,ProductResponseModel.class)).toList();

        return ResponseEntity.status(HttpStatus.FOUND).body(responseModelList);
    }

    @PostMapping("{userId}/order/{location}")
    public ResponseEntity<?> orderItems(@PathVariable("userId") String userId, @PathVariable(name = "location") String location, @RequestBody List<OrderRequestModel> itemrequests){
        List<Cart> cartItems  = new ArrayList<>();
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        for(OrderRequestModel requestModel: itemrequests){
            cartItems.add(mapper.map(requestModel,Cart.class));
        }
        String validation = orderDetailsService.placeOrder(cartItems,userId,location);
        if(validation.equals(OrderStatus.PLACED.name())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validation);
        }else{
            return ResponseEntity.status(HttpStatus.CREATED).body(validation);
        }

    }

}
