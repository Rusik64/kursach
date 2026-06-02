package com.example.kursach.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BuyProductDTO {
    private Long productId;
    private String productName;
    private Double price;
    private Integer count;
    private Double totalPrice;
    private Double discountPercent; // процент скидки (если есть)
    private Double discountAmount; // сумма скидки
    private Double finalPrice;
}