package com.example.demo.Portfolio.repos;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Portfolio.Portfolio;
import com.example.demo.Portfolio.PortfolioRepo;
import com.example.demo.Portfolio.response.PortfolioSummaryResponse;

import jakarta.servlet.http.HttpSession;

@RestController
public class ManagePortfolios {
    
    @Autowired
    private PortfolioRepo portfolioRepo;
    
    @GetMapping("/portfolios")
    public List<PortfolioSummaryResponse> getPortfolios(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return List.of();
        }
        
        List<Portfolio> portfolios = portfolioRepo.findPortfoliosByUserId(userId); // get all portfolios for user
        
        // map to response objects, return list of summaries. The frontend can request details as needed
        return portfolios.stream().map(p -> new PortfolioSummaryResponse(p.getPortfolioId(), p.getPortfolioName(), p.getBalance())).collect(Collectors.toList());
    }
    
    @PostMapping("/createportfolio")
    public String createPortfolio(@RequestParam String portfolioName, @RequestParam(defaultValue = "0.0") Double initialBalance, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "Not logged in";
        }
        
        if (portfolioName == null || portfolioName.isEmpty()) {
            return "Portfolio name is required";
        }
        
        if (initialBalance == null) {
            initialBalance = 0.0; // default to 0 if not provided, already handled by @RequestParam
        }
        
        // insert first
        portfolioRepo.insertPortfolio(userId, portfolioName, initialBalance);

        // then fetch the ID
        Long portfolioId = portfolioRepo.getPortfolioId(userId, portfolioName);
        
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

