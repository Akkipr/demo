package com.example.demo.Stocklists;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockListReviewRepository extends JpaRepository<StockListReview, Long> {

    // get all reviews for a specific stock list
    @Query(value = "SELECT * FROM stock_list_reviews WHERE stock_list_id = :stockListId", nativeQuery = true)
    List<StockListReview> findByStockListId(@Param("stockListId") Long stockListId);
}
