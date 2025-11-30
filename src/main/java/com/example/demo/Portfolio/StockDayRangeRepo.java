package com.example.demo.Portfolio;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    //insert new stock data
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO public.NewStocks (timestamp, symbol, open, high, low, close, volume) VALUES (?1, ?2, COALESCE(?3, 6), COALESCE(?4, 6), COALESCE(?5, 6), COALESCE(?6, 6), COALESCE(?7, 6))", nativeQuery = true)
    void updateCurrentPrice(LocalDate timestamp, String symbol, Double open, Double high, Double low, Double close,Long volume);

    // combines historical and latest stock data for a symbol across two tables
    @Query(value = "SELECT * FROM (SELECT timestamp, symbol, close, open, high, low, volume FROM stocks WHERE symbol = :symbol AND timestamp BETWEEN :start AND :end UNION ALL SELECT timestamp, symbol, close, open, high, low, volume FROM NewStocks WHERE symbol = :symbol AND timestamp BETWEEN :start AND :end) AS combined ORDER BY timestamp ASC", nativeQuery = true)
    List<StockDayRange> findCombinedPrices(@Param("symbol") String symbol, @Param("start") LocalDate start, @Param("end") LocalDate end);

    //get the most recent timestamp in both stocks or newstocks
    @Query(value = "SELECT GREATEST(COALESCE((SELECT MAX(timestamp) FROM stocks WHERE symbol = :symbol), '1900-01-01'),COALESCE((SELECT MAX(timestamp) FROM NewStocks WHERE symbol = :symbol), '1900-01-01'))", nativeQuery = true)
    LocalDate findLatestCombinedDate(@Param("symbol") String symbol);
}