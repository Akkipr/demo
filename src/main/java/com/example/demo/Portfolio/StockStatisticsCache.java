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
@IdClass(StockStatisticsCacheId.class)
@Table(name = "stock_statistics_cache")
public class StockStatisticsCache {

    @Id
    private String symbol;

    @Id
    @Column(name = "start_date")
    private LocalDate startDate;

    @Id
    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "mean_return")
    private Double meanReturn;

    @Column(name = "std_dev")
    private Double stdDev;

    @Column(name = "coefficient_of_variation")
    private Double coefficientOfVariation;

    private Double beta;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}

