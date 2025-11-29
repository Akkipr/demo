package com.example.demo.Portfolio.repos;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Portfolio.StockDayRange;
import com.example.demo.Portfolio.StockDayRangeRepo;
import com.example.demo.Portfolio.service.StatisticsCacheService;

import jakarta.servlet.http.HttpSession;

@RestController
public class AddStockData {
    
    @Autowired
    private StockDayRangeRepo stockDayRangeRepo;
    
    @Autowired
    private StatisticsCacheService statisticsCacheService;
    
    @PostMapping("/addstockdata")
    public String addStockData(@RequestParam String symbol, @RequestParam String date,@RequestParam(required = false) Double open, @RequestParam(required = false) Double high, @RequestParam(required = false) Double low, @RequestParam(required = false) Double close, @RequestParam(required = false) Long volume, HttpSession session) {
        
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "Not logged in";
        }
        
        try {
            LocalDate stockDate = LocalDate.parse(date);
            String normalizedSymbol = symbol.trim().toUpperCase();
            
            // check if data for this date already exists, prevent duplicates
            List<StockDayRange> existing = stockDayRangeRepo.findBySymbolAndDateRange(normalizedSymbol, stockDate, stockDate);
            
            if (!existing.isEmpty()) {
                return "Stock data already exists for " + symbol + " on " + date + ". Use update endpoint to modify.";
            }
            
            // update current price in stocks_current table since this is the latest info we got !!
            stockDayRangeRepo.updateCurrentPrice(stockDate, normalizedSymbol,open,high,low,close,volume);
            statisticsCacheService.invalidateStockStatistics(normalizedSymbol);
            
            return "Stock data added successfully for " + normalizedSymbol + " on " + date;
            
        } catch (Exception e) {
            return "Error adding stock data";
        }
    }
    
}