package com.example.kursach.repository.model;

import lombok.Data;

@Data
public class BuyProduct {
    private Long buyId;
    private Long productId;
    private int count;
}
