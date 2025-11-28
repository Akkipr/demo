package com.example.demo.Portfolio;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

@RestController
public class PortfolioStatistics {
    
    @Autowired
    private PortfolioRepo portfolioRepo;
    
    
    @GetMapping("/portfoliostatistics")
    public String getPortfolioStatistics(@RequestParam Long portfolioId, @RequestParam String startDate, @RequestParam String endDate, HttpSession session) {
        
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "Not logged in";
        }
        
        Optional<Portfolio> portfolioOpt = portfolioRepo.findPortfolioByUserIdAndPortfolioId(userId, portfolioId);
        
        if (!portfolioOpt.isPresent()) {
            return "Portfolio not found or access denied";
        }

        String result = "Portfolio Statistics for: " + portfolioOpt.get().getPortfolioName() + "\n" + "Time Period: " + startDate + " to " + endDate + "\n\n";
        
        result += "\n";
        
        return result;
    }
    
    @GetMapping("/stockstatistics")
    public String getStockStatistics(@RequestParam String symbol, @RequestParam String startDate, @RequestParam String endDate, HttpSession session) {
        
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "Not logged in";
        }
        String result = "Statistics for " + symbol + "\n" + "Time Period: " + startDate + " to " + endDate + "\n\n";
        return result;
    }
}

