package com.example.demo.Portfolio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface PortfolioRepo extends JpaRepository<Portfolio, Long> {
    
    // get all portfolios for a user
    @Query(value = "SELECT * FROM public.portfolios WHERE user_id = ?1", nativeQuery = true)
    List<Portfolio> findPortfoliosByUserId(Long userId);
    
    // get a specific portfolio by ID for a user
    @Query(value = "SELECT * FROM public.portfolios WHERE user_id = ?1 AND portfolio_id = ?2", nativeQuery = true)
    Optional<Portfolio> findPortfolioByUserIdAndPortfolioId(Long userId, Long portfolioId);
    
    // create a new portfolio
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO public.portfolios (user_id, portfolio_name, balance) VALUES (?1, ?2, ?3)", nativeQuery = true)
    void insertPortfolio(Long userId, String portfolioName, Double balance);

    @Query(value = "SELECT portfolio_id FROM public.portfolios WHERE user_id = ?1 AND portfolio_name = ?2 ORDER BY portfolio_id DESC LIMIT 1", nativeQuery = true)
    Long getPortfolioId(Long userId, String portfolioName);
    
    // update portfolio balance
    @Modifying
    @Transactional
    @Query(value = "UPDATE public.portfolios SET balance = ?2 WHERE portfolio_id = ?1", nativeQuery = true)
    void updateBalance(Long portfolioId, Double balance);
    
    // delete portfolio
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM public.portfolios WHERE portfolio_id = ?1", nativeQuery = true)
    void deletePortfolio(Long portfolioId);
}

