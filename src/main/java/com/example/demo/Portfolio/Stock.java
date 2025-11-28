package com.example.demo.Portfolio;

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
    private Double currentPrice;
}

