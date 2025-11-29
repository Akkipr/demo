package com.example.demo.Stocklists;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Portfolio.StockHolding;

import jakarta.servlet.http.HttpSession;

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

    @GetMapping("/other")
    public ResponseEntity<List<Stocklists>> getOtherStockLists(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(stocklistsService.getPublicStockListsNotOwnedByUser(userId));
    }

    // ...existing code...
    // POST /stocklists/{id}/reviews - Add a review (params instead of body)
    @PostMapping("/{id}/reviews")
    public ResponseEntity<StockListReview> addReview(
            @PathVariable Long id,
            @RequestParam String text,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).build();

        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String email = session.getAttribute("email").toString();
        
        StockListReview created = stocklistsService.addReview(id, text, email);
        return ResponseEntity.status(201).body(created);
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