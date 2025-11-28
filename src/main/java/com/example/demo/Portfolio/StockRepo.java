package com.example.demo.Portfolio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface StockRepo extends JpaRepository<Stock, String> {
    
    // get current price of a stock (from stocks_current table)
    @Query(value = "SELECT symbol, current_price FROM public.stocks_current WHERE symbol = ?1", nativeQuery = true)
    Optional<Stock> findBySymbol(String symbol);
    
    // get the latest close price from stocks table (the historical data table)
    @Query(value = "SELECT symbol, close AS currentPrice FROM public.stocks WHERE symbol = ?1 ORDER BY timestamp DESC LIMIT 1", nativeQuery = true)
    Optional<StockPriceInfo> getLatestPrice(String symbol);
    
    // update or insert current price in stocks_current table
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO public.stocks_current (symbol, current_price) VALUES (?1, ?2) ON CONFLICT (symbol) DO UPDATE SET current_price = ?2", nativeQuery = true)
    void updateCurrentPrice(String symbol, Double price);
}

