package com.example.demo.Portfolio;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface StockStatisticsCacheRepo extends JpaRepository<StockStatisticsCache, StockStatisticsCacheId> {

    Optional<StockStatisticsCache> findBySymbolAndStartDateAndEndDate(String symbol, LocalDate startDate, LocalDate endDate);

    @Modifying
    @Transactional
    // upsert stock statistics cache, insert if not exists, update if exists
    @Query(value = "INSERT INTO stock_statistics_cache (symbol, start_date, end_date, mean_return, std_dev, coefficient_of_variation, beta, last_updated) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, NOW()) ON CONFLICT (symbol, start_date, end_date) DO UPDATE SET mean_return = EXCLUDED.mean_return, std_dev = EXCLUDED.std_dev, coefficient_of_variation = EXCLUDED.coefficient_of_variation, beta = EXCLUDED.beta, last_updated = NOW()", nativeQuery = true)
    void upsert(String symbol, LocalDate startDate, LocalDate endDate, Double meanReturn, Double stdDev, Double cov, Double beta);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM stock_statistics_cache WHERE symbol = ?1", nativeQuery = true)
    void deleteBySymbol(String symbol);
}

