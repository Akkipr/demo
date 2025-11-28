package com.example.demo.Portfolio.repos;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Portfolio.Portfolio;
import com.example.demo.Portfolio.PortfolioRepo;
import com.example.demo.Portfolio.StockHolding;
import com.example.demo.Portfolio.StockHoldingRepo;
import com.example.demo.Portfolio.StockPriceInfo;
import com.example.demo.Portfolio.StockRepo;
import com.example.demo.Portfolio.TransferRepo;
import com.example.demo.Portfolio.service.StatisticsCacheService;

import jakarta.servlet.http.HttpSession;

@RestController
public class ManageStocks {
    
    @Autowired
    private PortfolioRepo portfolioRepo;
    
    @Autowired
    private StockHoldingRepo stockHoldingRepo;
    
    @Autowired
    private StockRepo stockRepo;
    
    @Autowired
    private TransferRepo transferRepo;
    
    @Autowired
    private StatisticsCacheService statisticsCacheService;
    
    @PostMapping("/buystock")
    public String buyStock(@RequestParam Long portfolioId, @RequestParam String symbol, @RequestParam Integer shares, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "Not logged in";
        }
        
        if (shares <= 0) {
            return "Number of shares must be greater than 0"; // validate share count
        }
        
        String normalizedSymbol = symbol.trim().toUpperCase();

        Optional<Portfolio> portfolioOpt = portfolioRepo.findPortfolioByUserIdAndPortfolioId(userId, portfolioId);
        
        if (!portfolioOpt.isPresent()) {
            return "Portfolio not found or access denied";
        }
        
        Portfolio portfolio = portfolioOpt.get();
        
        // get current stock price
        Optional<StockPriceInfo> priceOpt = stockRepo.getLatestPrice(normalizedSymbol);
        
        if (!priceOpt.isPresent()) {
            return "Stock symbol not found or no price data available";
        }
        
        Double currentPrice = priceOpt.get().getCurrentPrice();
        Double totalCost = currentPrice * shares;
        
        if (portfolio.getBalance() < totalCost) {
            // insufficient funds and format to 2 decimal places
            return "Insufficient funds. Required: $" + String.format("%.2f", totalCost) + 
                   ", Available: $" + String.format("%.2f", portfolio.getBalance());
        }
        
        // check if holding already exists
        Optional<StockHolding> existingHolding = stockHoldingRepo.findHoldingByPortfolioAndSymbol(portfolioId, normalizedSymbol);
        
        // instead of createStockHolding with RETURNING:
        if (existingHolding.isPresent()) {
            // update existing holding
            StockHolding holding = existingHolding.get();
            Integer newShareCount = holding.getShareCount() + shares;
            stockHoldingRepo.updateShareCount(holding.getHoldingId(), newShareCount);
        } else {
            // insert first
            stockHoldingRepo.insertStockHolding(portfolioId, normalizedSymbol, shares);
        }
        
        // update portfolio balance
        Double newBalance = portfolio.getBalance() - totalCost;
        portfolioRepo.updateBalance(portfolioId, newBalance);
        
        // create transfer record
        transferRepo.createTransfer(portfolioId, totalCost, LocalDate.now(),
            "Buy Stock", "Portfolio " + portfolioId, normalizedSymbol + " (" + shares + " shares)");
        
        // update current price in stocks_current table
        stockRepo.updateCurrentPrice(normalizedSymbol, currentPrice);
        
        statisticsCacheService.invalidatePortfolioStatistics(portfolioId);
        
        // return success message, again, format to 2 decimal places
        return "Stock purchase successful. Bought " + shares + " shares of " + normalizedSymbol + 
               " at $" + String.format("%.2f", currentPrice) + " per share. " +
               "Total cost: $" + String.format("%.2f", totalCost) + 
               ". New balance: $" + String.format("%.2f", newBalance);
    }
    
    @PostMapping("/sellstock")
    public String sellStock(@RequestParam Long portfolioId, @RequestParam String symbol, @RequestParam Integer shares, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "Not logged in";
        }
        
        if (shares <= 0) {
            return "Number of shares must be greater than 0";
        }
        
        String normalizedSymbol = symbol.trim().toUpperCase();

        Optional<Portfolio> portfolioOpt = portfolioRepo.findPortfolioByUserIdAndPortfolioId(userId, portfolioId);
        
        if (!portfolioOpt.isPresent()) {
            return "Portfolio not found or access denied";
        }
        
        Portfolio portfolio = portfolioOpt.get();
        
        // Check if holding exists
        Optional<StockHolding> holdingOpt = stockHoldingRepo.findHoldingByPortfolioAndSymbol(portfolioId, normalizedSymbol);
        
        if (!holdingOpt.isPresent()) {
            return "You don't own any shares of " + normalizedSymbol;
        }
        
        StockHolding holding = holdingOpt.get();
        
        if (holding.getShareCount() < shares) {
            return "Insufficient shares. You own " + holding.getShareCount() + " shares of " + normalizedSymbol;
        }
        
        // get current stock price
        Optional<StockPriceInfo> priceOpt = stockRepo.getLatestPrice(normalizedSymbol);
        
        if (!priceOpt.isPresent()) {
            return "Stock price not available";
        }
        
        Double currentPrice = priceOpt.get().getCurrentPrice();
        Double totalProceeds = currentPrice * shares;
        
        // update holding
        Integer newShareCount = holding.getShareCount() - shares;
        
        if (newShareCount == 0) {
            // delete holding if no shares left
            stockHoldingRepo.deleteHolding(holding.getHoldingId());
        } else {
            stockHoldingRepo.updateShareCount(holding.getHoldingId(), newShareCount);
        }
        
        // update portfolio balance
        Double newBalance = portfolio.getBalance() + totalProceeds;
        portfolioRepo.updateBalance(portfolioId, newBalance);
        
        // create transfer record
        transferRepo.createTransfer(portfolioId, totalProceeds, LocalDate.now(),
            "Sell Stock", normalizedSymbol + " (" + shares + " shares)", "Portfolio " + portfolioId);
        
        // update current price in stocks_current table
        stockRepo.updateCurrentPrice(normalizedSymbol, currentPrice);
        
        statisticsCacheService.invalidatePortfolioStatistics(portfolioId);
        
        // return success message, again, formatted to 2 decimal places
        return "Stock sale successful. Sold " + shares + " shares of " + normalizedSymbol + 
               " at $" + String.format("%.2f", currentPrice) + " per share. " +
               "Total proceeds: $" + String.format("%.2f", totalProceeds) + 
               ". New balance: $" + String.format("%.2f", newBalance);
    }
}

