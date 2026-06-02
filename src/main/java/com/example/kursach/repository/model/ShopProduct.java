package com.example.kursach.repository.model;

import lombok.Data;

@Data
public class ShopProduct {
    private Long shopId;
    private Long productId;
    private int count;
}