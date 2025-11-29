package com.example.demo.Stocklists;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Portfolio.StockHolding;

import java.util.List;

public interface StockHoldingRepository extends JpaRepository<StockHolding, Long> {
    List<StockHolding> findByStockListId(Long stockListId);
    void deleteByStockListId(Long stockListId);
}