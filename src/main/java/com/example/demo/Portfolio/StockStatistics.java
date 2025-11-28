package com.example.demo.Portfolio;

public interface StockStatistics {
    String getSymbol();
    Double getStdDev();
    Double getMeanReturn();
    Double getCoefficientOfVariation();
}

