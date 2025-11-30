package com.example.demo.Stocklists;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Portfolio.StockHolding;

public interface StockHoldingRepository extends JpaRepository<StockHolding, Long> {

    // get all holdings for a specific stock list
    @Query(value = "SELECT * FROM stock_holdings WHERE stock_list_id = :stockListId", nativeQuery = true)
    List<StockHolding> findByStockListId(@Param("stockListId") Long stockListId);

    // felete all holdings for a specific stock list
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM stock_holdings WHERE stock_list_id = :stockListId", nativeQuery = true)
    void deleteByStockListId(@Param("stockListId") Long stockListId);
}
