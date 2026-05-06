package com.course.controller;


import com.course.dto.cart.CartItemResponse;
import com.course.enums.CartResult;
import com.course.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.course.service.CartItemService;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/cartItem")
@RequiredArgsConstructor
@Slf4j
public class CartItemController {

    private final CartItemService cartItemService;
    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<?> getCartItemList(Authentication authentication){
        //todo로그인한 사용자의 장바구니 목록 가져오기
        String email = authentication.getName();
        log.info("getCartItemList에서 로그인한 사용자의 이메일:"+email);
        List<CartItemResponse> list = null;
        try {
            list = cartItemService.getCartItemList(email);
        } catch (Exception e) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("getCartItemList실패");
        }
        return ResponseEntity.ok(list);
    }

    @PostMapping("/{lectureNo}")
    public ResponseEntity<?> addToCart(Authentication authentication, @PathVariable long lectureNo){
        String email = authentication.getName();
        log.info("addToCart진입, email:"+email);
        try {
            CartResult result = cartItemService.addToCart(email,lectureNo);
            if(result == CartResult.ALREADY_REGISTERED){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이미 장바구니에 있는 강의입니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("addToCart 에러, catch문");
        }
        return ResponseEntity.ok("장바구니에 아이템 추가 성공");
    }

    @DeleteMapping("/{lectureNo}")
    public ResponseEntity<?> deleteCartItem(Authentication authentication, @PathVariable long lectureNo){
        String email = authentication.getName();
        log.info("deleteItem진입, email:"+email);
        try {
            cartItemService.deleteCartItem(email,lectureNo);
        } catch (Exception e) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("deleteCartItem실패");
        }
        return ResponseEntity.ok("장바구니의 아이템 삭제 성공");
    }



}
