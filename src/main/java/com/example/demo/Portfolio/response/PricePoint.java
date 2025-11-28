package com.example.demo.Portfolio.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PricePoint {
    private LocalDate date;
    private Double price;
}

