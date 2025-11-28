package com.example.demo.Portfolio;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@IdClass(PortfolioCovarianceCacheId.class)
@Table(name = "portfolio_covariance_cache")
public class PortfolioCovarianceCache {

    @Id
    // adding the necessary columns since I named them differently in the tbale
    @Column(name = "portfolio_id")
    private Long portfolioId;

    @Id
    private String symbol1;

    @Id
    private String symbol2;

    @Id
    @Column(name = "start_date")
    private LocalDate startDate;

    @Id
    @Column(name = "end_date")
    private LocalDate endDate;

    private Double covariance;
    private Double correlation;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}

