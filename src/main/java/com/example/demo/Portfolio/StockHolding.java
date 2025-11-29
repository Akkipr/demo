package com.example.demo.Portfolio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "stock_holdings")
public class StockHolding {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holding_id")
    private Long holdingId;
    
    // @Column(nullable = false)
    private String symbol;

    @Column(name = "share_count")
    private Integer shareCount;
    
    @Column(name = "portfolio_id")
    private Long portfolioId;
    
    @Column(name = "stock_list_id")
    private Long stockListId;
    
    public StockHolding() {}

    public StockHolding(String symbol, Integer shares, Long stockListId) {
        this.symbol = symbol;
        this.shareCount = shares;
        this.stockListId = stockListId;
    }
    
    // Getters and Setters
    public Long getHoldingId() { return holdingId; }
    public void setHoldingId(Long holdingId) { this.holdingId = holdingId; }
    
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public Integer getShares() { return shareCount; }
    public void setShares(Integer shareCount) { this.shareCount = shareCount; }
    
    public Long getStockListId() { return stockListId; }
    public void setStockListId(Long stockListId) { this.stockListId = stockListId; }
}

