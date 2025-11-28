package com.example.demo.Portfolio;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

@RestController
public class PortfolioDetails {
    
    @Autowired
    private PortfolioRepo portfolioRepo;
    
    
    @GetMapping("/portfoliodetails")
    public String getPortfolioDetails(@RequestParam Long portfolioId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "Not logged in";
        }
        
        Optional<Portfolio> portfolioOpt = portfolioRepo.findPortfolioByUserIdAndPortfolioId(userId, portfolioId);
        
        if (!portfolioOpt.isPresent()) {
            return "Portfolio not found or access denied";
        }
        
        Portfolio portfolio = portfolioOpt.get();
        String result = "Portfolio: " + portfolio.getPortfolioName() + "\n" + "Cash Balance: $" + portfolio.getBalance() + "\n\n";
        
        return result;
    }
    
}

