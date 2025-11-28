package com.example.demo.Portfolio;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class PortfolioCovarianceCacheId implements Serializable {
    private Long portfolioId;
    private String symbol1;
    private String symbol2;
    private LocalDate startDate;
    private LocalDate endDate;

    public PortfolioCovarianceCacheId() {} // default constructor

    public PortfolioCovarianceCacheId(Long portfolioId, String symbol1, String symbol2, LocalDate startDate, LocalDate endDate) {
        this.portfolioId = portfolioId;
        this.symbol1 = symbol1;
        this.symbol2 = symbol2;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PortfolioCovarianceCacheId that = (PortfolioCovarianceCacheId) o;
        return Objects.equals(portfolioId, that.portfolioId) && Objects.equals(symbol1, that.symbol1) && Objects.equals(symbol2, that.symbol2) && Objects.equals(startDate, that.startDate) && Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(portfolioId, symbol1, symbol2, startDate, endDate);
    }
}

