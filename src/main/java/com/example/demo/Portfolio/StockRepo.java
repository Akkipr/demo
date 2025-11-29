package com.example.demo.Portfolio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface StockRepo extends JpaRepository<Stock, String> {

    @Query(value = " SELECT symbol, close AS current_price FROM public.NewStocks WHERE symbol = ?1 ORDER BY timestamp DESC LIMIT 1", nativeQuery = true)
    Optional<Stock> findBySymbol(String symbol);

    @Query(value = "SELECT close AS currentPrice, timestamp FROM public.NewStocks WHERE symbol = ?1 ORDER BY timestamp DESC LIMIT 1", nativeQuery = true)
    Optional<StockPriceInfo> getLatestPriceFromNewStocks(String symbol);


    // get the latest close price from stocks table (the historical data table)
    @Query(value = "SELECT symbol, close AS currentPrice, timestamp FROM public.stocks WHERE symbol = ?1 ORDER BY timestamp DESC LIMIT 1", nativeQuery = true)
    Optional<StockPriceInfo> getLatestPrice(String symbol);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO public.NewStocks (timestamp, symbol, open, high, low, close, volume) VALUES (CURRENT_DATE, ?1, ?2, ?2, ?2, ?2, 0)", nativeQuery = true)
    void updateCurrentPrice(String symbol, Double price);
}