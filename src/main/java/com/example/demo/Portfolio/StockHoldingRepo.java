package com.example.demo.Portfolio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface StockHoldingRepo extends JpaRepository<StockHolding, Long> {
    
    // get all stock holdings for a portfolio
    @Query(value = "SELECT * FROM public.stock_holdings WHERE portfolio_id = ?1", nativeQuery = true)
    List<StockHolding> findHoldingsByPortfolioId(Long portfolioId);
    
    // get a specific holding with symbol
    @Query(value = "SELECT * FROM public.stock_holdings WHERE portfolio_id = ?1 AND symbol = ?2", nativeQuery = true)
    Optional<StockHolding> findHoldingByPortfolioAndSymbol(Long portfolioId, String symbol);
    
    // create a new stock holding
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO public.stock_holdings (portfolio_id, symbol, share_count) VALUES (?1, ?2, ?3) RETURNING holding_id", nativeQuery = true)
    Long createStockHolding(Long portfolioId, String symbol, Integer shareCount);
    
    // update share count
    @Modifying
    @Transactional
    @Query(value = "UPDATE public.stock_holdings SET share_count = ?2 WHERE holding_id = ?1", nativeQuery = true)
    void updateShareCount(Long holdingId, Integer shareCount);
    
    // delete holding
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM public.stock_holdings WHERE holding_id = ?1", nativeQuery = true)
    void deleteHolding(Long holdingId);
    
    // Get holding details with symbol for a portfolio (for display)
    @Query(value = "SELECT holding_id AS holdingId, share_count AS shareCount, symbol FROM public.stock_holdings WHERE portfolio_id = ?1", nativeQuery = true)
    List<HoldingDetails> getHoldingDetailsByPortfolio(Long portfolioId);

    // Get portfolio IDs that own a specific symbol
    @Query(value = "SELECT DISTINCT portfolio_id FROM public.stock_holdings WHERE symbol = ?1", nativeQuery = true)
    List<Long> findPortfolioIdsBySymbol(String symbol);
}

