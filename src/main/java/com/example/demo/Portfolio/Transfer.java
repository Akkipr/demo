package com.example.demo.Portfolio;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "transfers")
public class Transfer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transferId;
    private Long portfolioId;
    private Double amount;
    private LocalDate date;
    private String transType;
    private String fromAcc;
    private String toAcc;
}

