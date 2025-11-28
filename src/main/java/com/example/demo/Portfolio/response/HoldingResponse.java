package com.example.demo.Portfolio.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HoldingResponse {
    private String symbol;
    private Integer shareCount;
    private Double lastClose;
    private Double marketValue;
}

