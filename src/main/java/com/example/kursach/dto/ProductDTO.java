package com.example.kursach.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private Long categoryId;
    private String categoryName;
    private Long discountId;
    private Double discountPercent;
    private Double price;
    private String unit;
    private int availableCount;
}
