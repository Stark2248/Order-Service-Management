package com.silverlining.orderservice.service;

import com.silverlining.orderservice.dto.Cart;
import com.silverlining.orderservice.dto.OrderDetailDto;
import com.silverlining.orderservice.dto.ProductDto;
import com.silverlining.orderservice.dto.UserDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface OrderDetailsService {

    public Optional<UserDto> getUser(String userId);

    public String placeOrder(List<Cart> cart,String userId, String location);

    public List<ProductDto> fetchProducts();

    public Optional<List<ProductDto>> fetchProductByLocation(String location);

    public List<OrderDetailDto> fetchOrderDetailsByOrderId(String orderId);

}
