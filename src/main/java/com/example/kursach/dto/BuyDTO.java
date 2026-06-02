package com.example.kursach.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BuyDTO {
    private Long id;
    private Long shopId;
    private Double sum;
    private LocalDateTime createdAt;
    private List<BuyProductDTO> products;
}
