package com.course.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cartItem")
public class CartItemController {

    private final CartItemService cartItemService;

    @GetMapping
    public ResponseEntity<?> getCartItem(){



        return ResponseEntity.ok("f");
    }

}
