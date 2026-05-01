package com.course.service.impl;

import com.course.domain.CartItem;
import com.course.mapper.CartItemMapper;
import com.course.service.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {

    private final CartItemMapper cartItemMapper;

    @Override
    public List<CartItem> getCartItemList(String email) throws Exception {
        

        return cartItemMapper.getCartItemList(email);
    }

}
