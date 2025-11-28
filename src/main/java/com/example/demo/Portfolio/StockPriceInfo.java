package com.example.demo.Portfolio;

public interface StockPriceInfo {
    String getSymbol();
    Double getCurrentPrice();
    java.sql.Date getTimestamp();
}

