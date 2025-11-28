package com.example.demo.Portfolio;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class StockDayRangeId implements Serializable {
    private String symbol;
    private LocalDate timestamp;
    
    public StockDayRangeId() {} // default constructor
    
    public StockDayRangeId(String symbol, LocalDate timestamp) {
        this.symbol = symbol;
        this.timestamp = timestamp;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        // Check for null and class type
        if (o == null || getClass() != o.getClass()) return false;
        StockDayRangeId that = (StockDayRangeId) o;
        return Objects.equals(symbol, that.symbol) && Objects.equals(timestamp, that.timestamp);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(symbol, timestamp);
    }
}

