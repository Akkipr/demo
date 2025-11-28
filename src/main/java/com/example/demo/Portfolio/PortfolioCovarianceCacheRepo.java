package com.example.demo.Portfolio;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface PortfolioCovarianceCacheRepo extends JpaRepository<PortfolioCovarianceCache, PortfolioCovarianceCacheId> {

    List<PortfolioCovarianceCache> findByPortfolioIdAndStartDateAndEndDate(Long portfolioId, LocalDate startDate, LocalDate endDate);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO portfolio_covariance_cache (portfolio_id, symbol1, symbol2, start_date, end_date, covariance, correlation, last_updated) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, NOW()) ON CONFLICT (portfolio_id, symbol1, symbol2, start_date, end_date) DO UPDATE SET covariance = EXCLUDED.covariance, correlation = EXCLUDED.correlation, last_updated = NOW()", nativeQuery = true)
    void upsert(Long portfolioId, String symbol1, String symbol2, LocalDate startDate, LocalDate endDate, Double covariance, Double correlation);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM portfolio_covariance_cache WHERE portfolio_id = ?1", nativeQuery = true)
    void deleteByPortfolioId(Long portfolioId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM portfolio_covariance_cache WHERE portfolio_id = ?1 AND start_date = ?2 AND end_date = ?3", nativeQuery = true)
    void deleteByPortfolioIdAndDateRange(Long portfolioId, LocalDate startDate, LocalDate endDate);
}

