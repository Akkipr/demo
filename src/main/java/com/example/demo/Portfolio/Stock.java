package com.example.demo.Portfolio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "NewStocks")
public class Stock {

    @Id
    private String symbol; 

    @Column(name = "timestamp")
    private java.time.LocalDate timestamp;

    @Column(name = "open")
    private Double open;

    @Column(name = "high")
    private Double high;

    @Column(name = "low")
    private Double low;

    @Column(name = "close")
    private Double close;  // current/latest price can just be "close"

    @Column(name = "volume")
    private Integer volume;
}