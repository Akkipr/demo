package com.example.demo.Portfolio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StockRepo extends JpaRepository<Stock, String> {

    @Query(value = " SELECT symbol, close AS current_price FROM public.NewStocks WHERE symbol = ?1 ORDER BY timestamp DESC LIMIT 1", nativeQuery = true)
    Optional<Stock> findBySymbol(String symbol);

    // retrieves the latest close price + timestamp for a symbol from `NewStocks`. This is needed to get the latest price to compare, if it exists in the table
    @Query(value = "SELECT close AS currentPrice, timestamp FROM public.NewStocks WHERE symbol = ?1 ORDER BY timestamp DESC LIMIT 1", nativeQuery = true)
    Optional<StockPriceInfo> getLatestPriceFromNewStocks(String symbol);


    // get the latest close price from stocks table (the historical data table)
    @Query(value = "SELECT symbol, close AS currentPrice, timestamp FROM public.stocks WHERE symbol = ?1 ORDER BY timestamp DESC LIMIT 1", nativeQuery = true)
    Optional<StockPriceInfo> getLatestPrice(String symbol);

}