package com.example.demo.Stocklists;

import jakarta.persistence.*;

@Entity
@Table(name = "stock_list_reviews")
public class StockListReview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String text;
    
    @Column(name = "stock_list_id", nullable = false)
    private Long stockListId;
    
    // Constructors
    public StockListReview() {}
    
    public StockListReview(String email, String text, Long stockListId) {
        this.email = email;
        this.text = text;
        this.stockListId = stockListId;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public Long getStockListId() { return stockListId; }
    public void setStockListId(Long stockListId) { this.stockListId = stockListId; }
}