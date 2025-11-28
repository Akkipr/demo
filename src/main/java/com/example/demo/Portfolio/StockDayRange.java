package com.example.demo.Portfolio;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "stocks")
@IdClass(StockDayRangeId.class)
public class StockDayRange {
    
    @Id
    @Column(name = "symbol")
    private String symbol;
    
    @Id
    @Column(name = "timestamp")
    private LocalDate timestamp;
    
    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Long volume;
}

