package com.example.demo.Portfolio;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PortfolioStatisticsRepo extends JpaRepository<Portfolio, Long> {
    
    // compute statistics (mean return, std dev, coefficient of variation) for a single stock over a given date range.
    @Query(value = """
            WITH merged AS (
                SELECT timestamp, symbol, close
                FROM public.stocks
                WHERE symbol = ?1 AND timestamp >= ?2 AND timestamp <= ?3
                UNION ALL
                SELECT timestamp, symbol, close
                FROM public.newstocks
                WHERE symbol = ?1 AND timestamp >= ?2 AND timestamp <= ?3
            ),
            returns AS (
                SELECT symbol, timestamp, close,
                    LAG(close) OVER (PARTITION BY symbol ORDER BY timestamp) AS prev_close
                FROM merged
            ),
            stats AS (
                SELECT symbol,
                    CASE WHEN prev_close > 0 THEN (close - prev_close) / prev_close ELSE 0 END AS daily_return
                FROM returns
                WHERE prev_close IS NOT NULL
            )
            SELECT symbol,
                STDDEV_POP(daily_return) AS std_dev,
                AVG(daily_return) AS mean_return,
                CASE WHEN AVG(daily_return) != 0
                        THEN ABS(STDDEV_POP(daily_return) / AVG(daily_return))
                        ELSE 0 END AS coefficient_of_variation
            FROM stats
            GROUP BY symbol
            """,
            nativeQuery = true)
    List<StockStatistics> getCoefficientOfVariation(String symbol, LocalDate startDate, LocalDate endDate);


    // compute Beta of a stock relative to the market.
    @Query(value = """
            WITH market_series AS (
                SELECT timestamp, AVG(close) AS market_close, LAG(AVG(close)) OVER (ORDER BY timestamp) AS prev_market_close
                FROM public.stocks
                WHERE timestamp >= ?2 AND timestamp <= ?3
                GROUP BY timestamp
            ),
            stock_series AS (
                SELECT timestamp, close, LAG(close) OVER (ORDER BY timestamp) AS prev_close
                FROM public.stocks
                WHERE symbol = ?1 AND timestamp >= ?2 AND timestamp <= ?3
            ),
            combined AS (
                SELECT
                    ms.timestamp,
                    CASE 
                        WHEN ss.prev_close IS NOT NULL AND ss.prev_close > 0
                            THEN (ss.close - ss.prev_close) / ss.prev_close
                        ELSE NULL
                    END AS stock_return,
                    CASE 
                        WHEN ms.prev_market_close IS NOT NULL AND ms.prev_market_close > 0
                            THEN (ms.market_close - ms.prev_market_close) / ms.prev_market_close
                        ELSE NULL
                    END AS market_return
                FROM stock_series ss
                JOIN market_series ms ON ms.timestamp = ss.timestamp
            )
            SELECT 
                COALESCE(COVAR_POP(stock_return, market_return) / NULLIF(VAR_POP(market_return), 0), 0) AS beta
            FROM combined
            WHERE stock_return IS NOT NULL AND market_return IS NOT NULL
            """, nativeQuery = true)
    Double getBeta(String symbol, LocalDate start, LocalDate end);

    // Compute covariance and correlation between every pair ofstocks inside a given portfolio.
    @Query(value = """
            WITH portfolio_stocks AS (SELECT DISTINCT symbol FROM public.stock_holdings WHERE portfolio_id = ?1),
            price_series AS (
                SELECT s.symbol, s.timestamp, s.close, LAG(s.close) OVER (PARTITION BY s.symbol ORDER BY s.timestamp) AS prev_close
                FROM public.stocks s
                JOIN portfolio_stocks ps ON ps.symbol = s.symbol
                WHERE s.timestamp >= ?2
                AND s.timestamp <= ?3
            ),
            returns AS (
                SELECT symbol, timestamp,
                    CASE
                        WHEN prev_close IS NOT NULL AND prev_close > 0
                            THEN (close - prev_close) / prev_close
                        ELSE NULL
                    END AS daily_return
                FROM price_series
            ),
            valid_returns AS (
                SELECT * 
                FROM returns 
                WHERE daily_return IS NOT NULL
            )
            SELECT 
                r1.symbol AS symbol1,
                r2.symbol AS symbol2,
                COVAR_POP(r1.daily_return, r2.daily_return) AS covariance,
                CASE
                    WHEN STDDEV_POP(r1.daily_return) > 0 
                    AND STDDEV_POP(r2.daily_return) > 0
                        THEN COVAR_POP(r1.daily_return, r2.daily_return)
                            / (STDDEV_POP(r1.daily_return) * STDDEV_POP(r2.daily_return))
                    ELSE 0
                END AS correlation
            FROM valid_returns r1
            JOIN valid_returns r2 
                ON r1.timestamp = r2.timestamp
            WHERE r1.symbol <= r2.symbol
            GROUP BY r1.symbol, r2.symbol
            ORDER BY r1.symbol, r2.symbol
            """, nativeQuery = true)
    List<CovarianceMatrixEntry> getCovarianceMatrix(Long portfolioId, LocalDate start, LocalDate end);

    
    // get statistics for all stocks in a portfolio
    @Query(value = """
            WITH portfolio_stocks AS (
                SELECT DISTINCT symbol
                FROM public.stock_holdings
                WHERE portfolio_id = ?1
            ),
            stock_returns AS (
                SELECT s.symbol,
                    s.close,
                    LAG(s.close) OVER (PARTITION BY s.symbol ORDER BY s.timestamp) AS prev_close,
                    s.timestamp
                FROM public.stocks s
                INNER JOIN portfolio_stocks ps ON s.symbol = ps.symbol
                WHERE s.timestamp >= ?2 AND s.timestamp <= ?3
                ORDER BY s.symbol, s.timestamp
            ),
            returns AS (
                SELECT symbol,
                    CASE WHEN prev_close > 0 THEN (close - prev_close) / prev_close ELSE 0 END AS daily_return
                FROM stock_returns
                WHERE prev_close IS NOT NULL
            )
            SELECT
                symbol,
                STDDEV_POP(daily_return) AS std_dev,
                AVG(daily_return) AS mean_return,
                CASE WHEN AVG(daily_return) != 0
                    THEN ABS(STDDEV_POP(daily_return) / AVG(daily_return))
                    ELSE 0 END AS coefficient_of_variation
            FROM returns
            GROUP BY symbol
            """, nativeQuery = true)
    List<StockStatistics> getPortfolioStockStatistics(Long portfolioId, LocalDate startDate, LocalDate endDate);
}
