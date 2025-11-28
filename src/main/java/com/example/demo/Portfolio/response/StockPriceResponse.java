package com.example.demo.Portfolio.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockPriceResponse {
    private String symbol;
    private Double price;
    private LocalDate priceDate;
    private boolean found;
}

