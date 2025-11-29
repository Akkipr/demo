package com.example.demo.Stocklists;

import jakarta.persistence.*;

@Entity
@Table(name = "stock_lists")
public class Stocklists {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_list_id")
    private Long stockListId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String visibility = "Private";
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    // Constructors
    public Stocklists() {}
    
    public Stocklists(String name, String visibility, Long userId) {
        this.name = name;
        this.visibility = visibility;
        this.userId = userId;
    }
    
    // Getters and Setters
    public Long getStockListId() { return stockListId; }
    public void setStockListId(Long stockListId) { this.stockListId = stockListId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}