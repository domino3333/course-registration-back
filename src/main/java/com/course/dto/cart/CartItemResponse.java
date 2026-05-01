package com.course.dto.cart;


import lombok.Data;

@Data
public class CartItemResponse {

    private Long lectureNo;
    private String title;
    private String professor;
    private Long capacity;
    private Long currentEnrollment;
    private Long credit;

}
