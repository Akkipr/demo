package com.example.demo.Stocklists;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StockListReviewRepository extends JpaRepository<StockListReview, Long> {
    List<StockListReview> findByStockListId(Long stockListId);
}