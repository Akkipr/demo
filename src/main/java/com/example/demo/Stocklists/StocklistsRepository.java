package com.example.demo.Stocklists;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StocklistsRepository extends JpaRepository<Stocklists, Long> {
    List<Stocklists> findByUserId(Long userId);
    List<Stocklists> findByVisibilityIgnoreCase(String visibility);
    List<Stocklists> findByVisibilityIgnoreCaseAndUserIdNot(String visibility, Long userId);
}