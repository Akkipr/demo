package com.example.demo.Portfolio;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

@RestController
public class ManagePortfolios {
    
    @Autowired
    private PortfolioRepo portfolioRepo;
    
    @GetMapping("/portfolios")
    public String getPortfolios(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "Not logged in";
        }
        
        List<Portfolio> portfolios = portfolioRepo.findPortfoliosByUserId(userId);
        
        if (portfolios.isEmpty()) {
            return "No portfolios found";
        }
        
        String result = "";     
        for (Portfolio p : portfolios) {
            result += "Portfolio ID: " + p.getPortfolioId() + ", Name: " + p.getPortfolioName() + ", Balance: $" + p.getBalance()+ "\n";
        }
        
        return result;
    }
    
    @PostMapping("/createportfolio")
    public String createPortfolio(@RequestParam String portfolioName, @RequestParam Double initialBalance, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "Not logged in";
        }
        
        if (portfolioName == null || portfolioName.isEmpty()) {
            return "Portfolio name is required";
        }
        
        if (initialBalance == null) {
            initialBalance = 0.0;
        }
        
        Long portfolioId = portfolioRepo.createPortfolio(userId, portfolioName, initialBalance);
        
        return "Portfolio created successfully with ID: " + portfolioId;
    }
    
    @PostMapping("/removeportfolio")
    public String removePortfolio(@RequestParam Long portfolioId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "Not logged in";
        }
        
        // verify portfolio belongs to user
        if (!portfolioRepo.findPortfolioByUserIdAndPortfolioId(userId, portfolioId).isPresent()) {
            return "Portfolio not found or access denied";
        }
        
        portfolioRepo.deletePortfolio(portfolioId);
        return "Portfolio removed successfully";
    }
}

