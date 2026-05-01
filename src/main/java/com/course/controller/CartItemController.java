package com.course.controller;


import com.course.domain.CartItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.course.service.CartItemService;

import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/cartItem")
@RequiredArgsConstructor
@Slf4j
public class CartItemController {

    private final CartItemService cartItemService;

    @GetMapping
    public ResponseEntity<?> getCartItemList(Authentication authentication){
        //todo로그인한 사용자의 장바구니 목록 가져오기
        String email = authentication.getName();
        log.info("getCartItemList에서 로그인한 사용자의 이메일:"+email);
        List<CartItem> list = null;
        try {
            list = cartItemService.getCartItemList(email);
        } catch (Exception e) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("getCartItemList실패");
        }
        return ResponseEntity.ok(list);
    }

    //@DeleteMapping("/{lectureNo}")



}
