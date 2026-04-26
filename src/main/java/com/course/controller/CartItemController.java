package com.course.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.course.service.CartItemService;

import lombok.RequiredArgsConstructor;

import java.util.Collections;

@RestController
@RequestMapping("/api/cartItem")
@RequiredArgsConstructor
@Slf4j
public class CartItemController {

    private final CartItemService cartItemService;

    @GetMapping
    public ResponseEntity<?> getCartItemList(Authentication authentication){
        //todo로그인한 사용자의 장바구니 목록 가져오기
        String name = authentication.getName();
        log.info("로그인한 사용자의 getName"+name);

        return ResponseEntity.ok(Collections.EMPTY_LIST);
    }

}
