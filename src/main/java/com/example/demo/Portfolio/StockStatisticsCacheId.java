package com.example.demo.Portfolio;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class StockStatisticsCacheId implements Serializable {
    private String symbol;
    private LocalDate startDate;
    private LocalDate endDate;

    public StockStatisticsCacheId() {} // default constructor

    public StockStatisticsCacheId(String symbol, LocalDate startDate, LocalDate endDate) {
        this.symbol = symbol;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false; // check for null and class type
        StockStatisticsCacheId that = (StockStatisticsCacheId) o; // cast to the correct class
        return Objects.equals(symbol, that.symbol) &&
               Objects.equals(startDate, that.startDate) &&
               Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, startDate, endDate);
    }
}

