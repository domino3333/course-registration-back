package com.course.service.impl;

import com.course.domain.CartItem;
import com.course.domain.Member;
import com.course.dto.cart.CartItemResponse;
import com.course.mapper.CartItemMapper;
import com.course.mapper.MemberMapper;
import com.course.service.CartItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartItemServiceImpl implements CartItemService {

    private final CartItemMapper cartItemMapper;
    private final MemberMapper memberMapper;

    @Override
    public List<CartItemResponse> getCartItemList(String email) throws Exception {
        return cartItemMapper.getCartItemList(email);
    }

    @Override
    public void addToCart(String email, long lectureNo) throws Exception {
        cartItemMapper.addToCart(email,lectureNo);
    }

    @Override
    public void deleteCartItem(String email, long lectureNo) throws Exception {
        log.info("deleteCartItem impl:"+email+ " " +lectureNo);
        cartItemMapper.deleteCartItem(email,lectureNo);
    }

}
