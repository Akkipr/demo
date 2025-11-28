package com.example.demo.Portfolio;

public interface CovarianceMatrixEntry {
    String getSymbol1();
    String getSymbol2();
    Double getCovariance();
    Double getCorrelation();
}

