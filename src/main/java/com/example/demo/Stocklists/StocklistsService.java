package com.example.demo.Stocklists;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Portfolio.StockHolding;

import java.util.List;

@Service
public class StocklistsService {
    
    @Autowired
    private StocklistsRepository stocklistsRepository;
    
    @Autowired
    private StockHoldingRepository stockHoldingRepository;
    
    @Autowired
    private StockListReviewRepository reviewRepository;
    
    public List<Stocklists> getStockListsByUserId(Long userId) {
        return stocklistsRepository.findByUserId(userId);
    }
    
    public Stocklists createStockList(String name, String visibility, Long userId) {
        Stocklists stockList = new Stocklists(name, visibility, userId);
        return stocklistsRepository.save(stockList);
    }
    
    @Transactional
    public void deleteStockList(Long id, Long userId) {
        Stocklists stockList = stocklistsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock list not found"));
        if (!stockList.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        stockHoldingRepository.deleteByStockListId(id);
        stocklistsRepository.deleteById(id);
    }
    
    public void updateVisibility(Long id, String visibility, Long userId) {
        Stocklists stockList = stocklistsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock list not found"));
        if (!stockList.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        stockList.setVisibility(visibility);
        stocklistsRepository.save(stockList);
    }
    
    public List<StockHolding> getHoldings(Long stockListId) {
        return stockHoldingRepository.findByStockListId(stockListId);
    }
    
    @Transactional
    public void updateHoldings(Long stockListId, List<StockHolding> holdings, Long userId) {
        // Delete existing holdings
        stockHoldingRepository.deleteByStockListId(stockListId);
        
        // Save new holdings
        for (StockHolding holding : holdings) {
            StockHolding newHolding = new StockHolding(
                    holding.getSymbol(),
                    holding.getShares(),
                    stockListId
            );
            stockHoldingRepository.save(newHolding);
        }
    }
    
    public List<StockListReview> getReviews(Long stockListId) {
        return reviewRepository.findByStockListId(stockListId);
    }
    
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }
}