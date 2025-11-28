package com.example.demo.Portfolio.repos;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Portfolio.StockPriceInfo;
import com.example.demo.Portfolio.StockRepo;
import com.example.demo.Portfolio.response.StockPriceResponse;

import jakarta.servlet.http.HttpSession;

@RestController
public class StockInfoController {
    
    @Autowired
    private StockRepo stockRepo;
    
    @GetMapping("/stockprice")
    public StockPriceResponse getStockPrice(@RequestParam String symbol, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return new StockPriceResponse(symbol, 0.0, null, false);
        }
        
        String normalizedSymbol = symbol.trim().toUpperCase();
        
        Optional<StockPriceInfo> priceOpt = stockRepo.getLatestPrice(normalizedSymbol);
        
        if (priceOpt.isEmpty()) {
            return new StockPriceResponse(normalizedSymbol, 0.0, null, false);
        }
        
        StockPriceInfo info = priceOpt.get();
        LocalDate priceDate = info.getTimestamp() != null ? info.getTimestamp().toLocalDate() : null;
        return new StockPriceResponse(normalizedSymbol, info.getCurrentPrice(), priceDate, true);
    }
}

