package com.example.demo.Stocklists;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StocklistsRepository extends JpaRepository<Stocklists, Long> {
    List<Stocklists> findByUserId(Long userId);
}