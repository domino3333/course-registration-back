package com.course.service;

import com.course.domain.CartItem;
import java.util.List;

public interface CartService {
    void createCart(String email) throws Exception;
}
