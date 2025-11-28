package com.example.demo.Portfolio;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PortfolioStatisticsRepo extends JpaRepository<Portfolio, Long> {
    
    @Query(value = "WITH stock_returns AS (" +
                   "  SELECT symbol, " +
                   "         close, " +
                   "         LAG(close) OVER (PARTITION BY symbol ORDER BY timestamp) AS prev_close, " +
                   "         timestamp " +
                   "  FROM public.stocks " +
                   "  WHERE symbol = ?1 AND timestamp >= ?2 AND timestamp <= ?3 " +
                   "  ORDER BY timestamp " +
                   "), " +
                   "returns AS (" +
                   "  SELECT symbol, " +
                   "         CASE WHEN prev_close > 0 THEN (close - prev_close) / prev_close ELSE 0 END AS daily_return " +
                   "  FROM stock_returns " +
                   "  WHERE prev_close IS NOT NULL " +
                   ") " +
                   "SELECT " +
                   "  symbol, " +
                   "  STDDEV_POP(daily_return) AS std_dev, " +
                   "  AVG(daily_return) AS mean_return, " +
                   "  CASE WHEN AVG(daily_return) != 0 THEN ABS(STDDEV_POP(daily_return) / AVG(daily_return)) ELSE 0 END AS coefficient_of_variation " +
                   "FROM returns " +
                   "GROUP BY symbol", nativeQuery = true)
    List<StockStatistics> getCoefficientOfVariation(String symbol, LocalDate startDate, LocalDate endDate);
    
    @Query(value = "WITH market_returns AS (" +
                   "  SELECT timestamp, " +
                   "         AVG(close) AS market_close, " +
                   "         LAG(AVG(close)) OVER (ORDER BY timestamp) AS prev_market_close " +
                   "  FROM public.stocks " +
                   "  WHERE timestamp >= ?2 AND timestamp <= ?3 " +
                   "  GROUP BY timestamp " +
                   "), " +
                   "stock_returns AS (" +
                   "  SELECT s.timestamp, " +
                   "         s.close AS stock_close, " +
                   "         LAG(s.close) OVER (ORDER BY s.timestamp) AS prev_stock_close " +
                   "  FROM public.stocks s " +
                   "  WHERE s.symbol = ?1 AND s.timestamp >= ?2 AND s.timestamp <= ?3 " +
                   "), " +
                   "combined_returns AS (" +
                   "  SELECT sr.timestamp, " +
                   "         CASE WHEN sr.prev_stock_close > 0 THEN (sr.stock_close - sr.prev_stock_close) / sr.prev_stock_close ELSE 0 END AS stock_return, " +
                   "         CASE WHEN mr.prev_market_close > 0 THEN (mr.market_close - mr.prev_market_close) / mr.prev_market_close ELSE 0 END AS market_return " +
                   "  FROM stock_returns sr " +
                   "  JOIN market_returns mr ON sr.timestamp = mr.timestamp " +
                   "  WHERE sr.prev_stock_close IS NOT NULL AND mr.prev_market_close IS NOT NULL " +
                   ") " +
                   "SELECT " +
                   "  COALESCE(COVAR_POP(stock_return, market_return) / NULLIF(VAR_POP(market_return), 0), 0) AS beta " +
                   "FROM combined_returns", nativeQuery = true)
    Double getBeta(String symbol, LocalDate startDate, LocalDate endDate);
    
    @Query(value = "WITH portfolio_stocks AS (" +
                   "  SELECT DISTINCT symbol " +
                   "  FROM public.stock_holdings " +
                   "  WHERE portfolio_id = ?1 " +
                   "), " +
                   "stock_returns AS (" +
                   "  SELECT s.symbol, " +
                   "         s.timestamp, " +
                   "         s.close, " +
                   "         LAG(s.close) OVER (PARTITION BY s.symbol ORDER BY s.timestamp) AS prev_close " +
                   "  FROM public.stocks s " +
                   "  INNER JOIN portfolio_stocks ps ON s.symbol = ps.symbol " +
                   "  WHERE s.timestamp >= ?2 AND s.timestamp <= ?3 " +
                   "), " +
                   "returns AS (" +
                   "  SELECT symbol, " +
                   "         timestamp, " +
                   "         CASE WHEN prev_close > 0 THEN (close - prev_close) / prev_close ELSE 0 END AS daily_return " +
                   "  FROM stock_returns " +
                   "  WHERE prev_close IS NOT NULL " +
                   ") " +
                   "SELECT " +
                   "  r1.symbol AS symbol1, " +
                   "  r2.symbol AS symbol2, " +
                   "  COVAR_POP(r1.daily_return, r2.daily_return) AS covariance, " +
                   "  CASE " +
                   "    WHEN STDDEV_POP(r1.daily_return) > 0 AND STDDEV_POP(r2.daily_return) > 0 " +
                   "    THEN COVAR_POP(r1.daily_return, r2.daily_return) / (STDDEV_POP(r1.daily_return) * STDDEV_POP(r2.daily_return)) " +
                   "    ELSE 0 " +
                   "  END AS correlation " +
                   "FROM returns r1 " +
                   "JOIN returns r2 ON r1.timestamp = r2.timestamp " +
                   "WHERE r1.symbol <= r2.symbol " +
                   "GROUP BY r1.symbol, r2.symbol " +
                   "ORDER BY r1.symbol, r2.symbol", nativeQuery = true)
    List<CovarianceMatrixEntry> getCovarianceMatrix(Long portfolioId, LocalDate startDate, LocalDate endDate);
    
    // Get statistics for all stocks in a portfolio
    @Query(value = "WITH portfolio_stocks AS (" +
                   "  SELECT DISTINCT symbol " +
                   "  FROM public.stock_holdings " +
                   "  WHERE portfolio_id = ?1 " +
                   "), " +
                   "stock_returns AS (" +
                   "  SELECT s.symbol, " +
                   "         s.close, " +
                   "         LAG(s.close) OVER (PARTITION BY s.symbol ORDER BY s.timestamp) AS prev_close, " +
                   "         s.timestamp " +
                   "  FROM public.stocks s " +
                   "  INNER JOIN portfolio_stocks ps ON s.symbol = ps.symbol " +
                   "  WHERE s.timestamp >= ?2 AND s.timestamp <= ?3 " +
                   "  ORDER BY s.symbol, s.timestamp " +
                   "), " +
                   "returns AS (" +
                   "  SELECT symbol, " +
                   "         CASE WHEN prev_close > 0 THEN (close - prev_close) / prev_close ELSE 0 END AS daily_return " +
                   "  FROM stock_returns " +
                   "  WHERE prev_close IS NOT NULL " +
                   ") " +
                   "SELECT " +
                   "  symbol, " +
                   "  STDDEV_POP(daily_return) AS std_dev, " +
                   "  AVG(daily_return) AS mean_return, " +
                   "  CASE WHEN AVG(daily_return) != 0 THEN ABS(STDDEV_POP(daily_return) / AVG(daily_return)) ELSE 0 END AS coefficient_of_variation " +
                   "FROM returns " +
                   "GROUP BY symbol", nativeQuery = true)
    List<StockStatistics> getPortfolioStockStatistics(Long portfolioId, LocalDate startDate, LocalDate endDate);
}

