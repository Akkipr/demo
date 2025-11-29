package com.example.demo.Stocklists;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockListReviewRepository extends JpaRepository<StockListReview, Long> {
    List<StockListReview> findByStockListId(Long stockListId);
}