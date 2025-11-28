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
@Table(name = "portfolios")
public class Portfolio {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    // adding the necessary columns since I named them differently in the tbale
    @Column(name = "portfolio_id")
    private Long portfolioId;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "portfolio_name")
    private String portfolioName;
    
    private Double balance;
}

