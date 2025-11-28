package com.example.demo.Portfolio;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

@RestController
public class HistoricalPrices {
    
    
    @Autowired
    private PortfolioRepo portfolioRepo;
    
    @GetMapping("/historicalprices")
    public String getHistoricalPrices(@RequestParam Long portfolioId, @RequestParam String symbol, @RequestParam String interval, @RequestParam String startDate, @RequestParam String endDate, HttpSession session) {
        
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "Not logged in";
        }
        
        // verify acess
        if (portfolioId != null) {
            if (!portfolioRepo.findPortfolioByUserIdAndPortfolioId(userId, portfolioId).isPresent()) {
                return "Portfolio not found or access denied";
            }
        }
        
        // if symbol is not provided but portfolioId is, get first stock from portfolio
        if (symbol == null || portfolioId != null) {
            // this would require a query to get symbols from portfolio
            return "Symbol parameter is required";
        }
        
        // calculate date range based on interval or use provided dates
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        LocalDate start;
        
        if (startDate != null) {
            start = LocalDate.parse(startDate);
        } else if (interval != null) {
            // doing allll this for the interval at which they want the historical data
            if (interval.equalsIgnoreCase("week")) {
                start = end.minusWeeks(1);
            } else if (interval.equalsIgnoreCase("month")) {
                start = end.minusMonths(1);
            } else if (interval.equalsIgnoreCase("quarter")) {
                start = end.minusMonths(3);
            } else if (interval.equalsIgnoreCase("year")) {
                start = end.minusYears(1);
            } else if (interval.equalsIgnoreCase("fiveyears") || interval.equalsIgnoreCase("5years")) {
                start = end.minusYears(5);
            } else {
                start = end.minusYears(1);
            }
        } else {
            start = end.minusYears(1); // Default to 1 year
        }
        
        String header = "Historical Prices for " + symbol + "\n" + "Time Period: " + start + " to " + end + "\n\n" + "Date         Open       High       Low        Close      Volume\n" + "------------------------------------------------------------------------\n";
        
        return header;
    }
}

