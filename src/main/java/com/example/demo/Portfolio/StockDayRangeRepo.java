package com.example.demo.Portfolio;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface StockDayRangeRepo extends JpaRepository<StockDayRange, StockDayRangeId> {
    
    // get historical data for a stock symbol
    @Query(value = "SELECT * FROM public.stocks WHERE symbol = ?1 ORDER BY timestamp DESC", nativeQuery = true)
    List<StockDayRange> findBySymbol(String symbol);
    
    // get hitorical data for a stock within a date range
    @Query(value = "SELECT * FROM public.stocks WHERE symbol = ?1 AND timestamp >= ?2 AND timestamp <= ?3 ORDER BY timestamp DESC", nativeQuery = true)
    List<StockDayRange> findBySymbolAndDateRange(String symbol, LocalDate startDate, LocalDate endDate);
    
    // get the latest close price for a symbol
    @Query(value = "SELECT close FROM public.stocks WHERE symbol = ?1 ORDER BY timestamp DESC LIMIT 1", nativeQuery = true)
    Double getLatestClosePrice(String symbol);
    
    // insert new stock data
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO public.stocks (symbol, timestamp, open, high, low, close, volume) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7)", nativeQuery = true)
    void insertStockData(String symbol, LocalDate timestamp, Double open, Double high, Double low, Double close, Long volume);
    
    // update stock data
    @Modifying
    @Transactional
    @Query(value = "UPDATE public.stocks SET open = COALESCE(?3, open), high = COALESCE(?4, high), low = COALESCE(?5, low), close = COALESCE(?6, close), volume = COALESCE(?7, volume) WHERE symbol = ?1 AND timestamp = ?2", nativeQuery = true)
    void updateStockData(String symbol, LocalDate timestamp, Double open, Double high, Double low, Double close, Long volume);
    
    // update current price in stocks_current table
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO public.stocks_current (symbol, current_price) VALUES (?1, ?2) ON CONFLICT (symbol) DO UPDATE SET current_price = ?2", nativeQuery = true)
    void updateCurrentPrice(String symbol, Double price);

    @Query(value = "SELECT MAX(timestamp) FROM public.stocks WHERE symbol = ?1", nativeQuery = true)
    LocalDate findLatestDate(String symbol);
}

