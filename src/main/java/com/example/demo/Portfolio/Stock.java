package com.example.demo.Portfolio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "stocks_current")
public class Stock {
    
    @Id
    private String symbol;
    
    @Column(name = "current_price")
    private Double currentPrice;
}

