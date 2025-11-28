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
    
    @Column(name = "portfolio_id")
    private Long portfolioId;
    
    private String symbol;
    
    @Column(name = "share_count")
    private Integer shareCount;
}

