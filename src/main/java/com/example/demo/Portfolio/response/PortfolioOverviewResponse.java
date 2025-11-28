package com.example.demo.Portfolio.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioOverviewResponse {
    private Long portfolioId;
    private String portfolioName;
    private Double cashBalance;
    private Double totalMarketValue;
    private Double totalValue;
    private List<HoldingResponse> holdings;
}

