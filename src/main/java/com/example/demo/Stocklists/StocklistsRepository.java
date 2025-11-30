package com.example.demo.Stocklists;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StocklistsRepository extends JpaRepository<Stocklists, Long> {

    // find all stock lists belonging to a specific user
    @Query(value = "SELECT * FROM stock_lists WHERE user_id = :userId", nativeQuery = true)
    List<Stocklists> findByUserId(@Param("userId") Long userId);

    // find all stock lists with visibility = 'Public'
    @Query(value = "SELECT * FROM stock_lists WHERE LOWER(visibility) = LOWER(:visibility)", nativeQuery = true)
    List<Stocklists> findByVisibilityIgnoreCase(@Param("visibility") String visibility);

    // find all stock lists with visibility = 'Public' not owned by a specific user
    @Query(value = "SELECT * FROM stock_lists WHERE LOWER(visibility) = LOWER(:visibility) AND user_id != :userId", nativeQuery = true)
    List<Stocklists> findByVisibilityIgnoreCaseAndUserIdNot(@Param("visibility") String visibility, @Param("userId") Long userId);
}
