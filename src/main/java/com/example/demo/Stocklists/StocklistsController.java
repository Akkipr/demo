package com.example.demo.Stocklists;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.Portfolio.StockHolding;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stocklists")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class StocklistsController {
    
    @Autowired
    private StocklistsService stocklistsService;
    
    // GET /stocklists/my - Get current user's stock lists
    @GetMapping("/my")
    public ResponseEntity<List<Stocklists>> getMyStockLists(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(stocklistsService.getStockListsByUserId(userId));
    }
    
    // POST /stocklists - Create a new stock list
    @PostMapping
    public ResponseEntity<Stocklists> createStockList(
            @RequestBody Map<String, String> request,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        String name = request.get("name");
        String visibility = request.getOrDefault("visibility", "Private");
        Stocklists newList = stocklistsService.createStockList(name, visibility, userId);
        return ResponseEntity.ok(newList);
    }
    
    // DELETE /stocklists/{id} - Delete a stock list
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStockList(
            @PathVariable Long id,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        stocklistsService.deleteStockList(id, userId);
        return ResponseEntity.ok().build();
    }
    
    // PUT /stocklists/{id}/visibility - Update visibility
    @PutMapping("/{id}/visibility")
    public ResponseEntity<Void> updateVisibility(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        String visibility = request.get("visibility");
        stocklistsService.updateVisibility(id, visibility, userId);
        return ResponseEntity.ok().build();
    }
    
    // GET /stocklists/{id}/holdings - Get holdings for a stock list
    @GetMapping("/{id}/holdings")
    public ResponseEntity<List<StockHolding>> getHoldings(
            @PathVariable Long id,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(stocklistsService.getHoldings(id));
    }
    
    // PUT /stocklists/{id}/holdings - Update all holdings for a stock list
    @PutMapping("/{id}/holdings")
    public ResponseEntity<Void> updateHoldings(
            @PathVariable Long id,
            @RequestBody List<StockHolding> holdings,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        stocklistsService.updateHoldings(id, holdings, userId);
        return ResponseEntity.ok().build();
    }
    
    // GET /stocklists/{id}/reviews - Get reviews for a stock list
    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<StockListReview>> getReviews(@PathVariable Long id) {
        return ResponseEntity.ok(stocklistsService.getReviews(id));
    }
    
    // DELETE /stocklists/{id}/reviews/{reviewId} - Delete a review
    @DeleteMapping("/{id}/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long id,
            @PathVariable Long reviewId,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        stocklistsService.deleteReview(reviewId);
        return ResponseEntity.ok().build();
    }
}