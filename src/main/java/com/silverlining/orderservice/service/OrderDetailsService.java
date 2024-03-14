package com.silverlining.orderservice.service;

import com.silverlining.orderservice.dto.Cart;
import com.silverlining.orderservice.dto.OrderDetailDto;
import com.silverlining.orderservice.dto.ProductDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface OrderDetailsService {

    public String placeOrder(List<Cart> cart,String userId, String location);

    public List<ProductDto> fetchProducts();

    public List<ProductDto> fetchProductByLocation(String location);

    public List<OrderDetailDto> fetchOrderDetailsByOrderId(String orderId);

}
