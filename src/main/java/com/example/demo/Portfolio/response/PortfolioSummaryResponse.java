package com.example.demo.Portfolio.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioSummaryResponse {
    private Long portfolioId;
    private String portfolioName;
    private Double balance;
}

