package com.example.demo.Portfolio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface TransferRepo extends JpaRepository<Transfer, Long> {
    
    // get all transfers for a portfolio
    @Query(value = "SELECT * FROM public.transfers WHERE portfolio_id = ?1 ORDER BY date DESC", nativeQuery = true)
    List<Transfer> findTransfersByPortfolioId(Long portfolioId);
    
    // create a new transfer
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO public.transfers (portfolio_id, amount, date, trans_type, from_acc, to_acc) VALUES (?1, ?2, ?3, ?4, ?5, ?6)", nativeQuery = true)
    void createTransfer(Long portfolioId, Double amount, java.time.LocalDate date, String transType, String fromAcc, String toAcc);
}

