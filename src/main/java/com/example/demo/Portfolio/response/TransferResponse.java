package com.example.demo.Portfolio.response;

import java.time.LocalDate;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TransferResponse {
    private Long transferId;
    private Double amount;
    private LocalDate date;
    private String transType;
    private String fromAcc;
    private String toAcc;
}

