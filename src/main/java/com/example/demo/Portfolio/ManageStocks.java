package com.example.demo.Portfolio;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

@RestController
public class ManageStocks {
    
    @Autowired
    private PortfolioRepo portfolioRepo;
    
    @Autowired
    private StockRepo stockRepo;
    
    @Autowired
    private TransferRepo transferRepo;
    
    @PostMapping("/buystock")
    public String buyStock(@RequestParam Long portfolioId, @RequestParam String symbol, @RequestParam Integer shares, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "Not logged in";
        }
        
        if (shares <= 0) {
            return "Number of shares must be greater than 0";
        }
        
        Optional<Portfolio> portfolioOpt = portfolioRepo.findPortfolioByUserIdAndPortfolioId(userId, portfolioId);
        
        if (!portfolioOpt.isPresent()) {
            return "Portfolio not found or access denied";
        }
        
        Portfolio portfolio = portfolioOpt.get();
        
        // get current stock price
        Optional<StockPriceInfo> priceOpt = stockRepo.getLatestPrice(symbol);
        
        if (!priceOpt.isPresent()) {
            return "Stock symbol not found or no price data available";
        }
        
        Double currentPrice = priceOpt.get().getCurrentPrice();
        Double totalCost = currentPrice * shares;
        
        if (portfolio.getBalance() < totalCost) {
            return "Insufficient funds. Required: $" + String.format("%.2f", totalCost) + 
                   ", Available: $" + String.format("%.2f", portfolio.getBalance());
        }

        // update portfolio balance
        Double newBalance = portfolio.getBalance() - totalCost;
        portfolioRepo.updateBalance(portfolioId, newBalance);
        
        // create transfer record
        transferRepo.createTransfer(portfolioId, totalCost, LocalDate.now(), 
            "Buy Stock", "Portfolio " + portfolioId, symbol + " (" + shares + " shares)");
        
        // update current price in stocks_current table
        stockRepo.updateCurrentPrice(symbol, currentPrice);
        
        return "Stock purchase successful. Bought " + shares + " shares of " + symbol + " at $" + currentPrice + " per share. " + "Total cost: $" + totalCost + ". New balance: $" + newBalance;
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
        
        Optional<Portfolio> portfolioOpt = portfolioRepo.findPortfolioByUserIdAndPortfolioId(userId, portfolioId);
        
        if (!portfolioOpt.isPresent()) {
            return "Portfolio not found or access denied";
        }
        
        Portfolio portfolio = portfolioOpt.get();
        
        // get current stock price
        Optional<StockPriceInfo> priceOpt = stockRepo.getLatestPrice(symbol);
        
        if (!priceOpt.isPresent()) {
            return "Stock price not available";
        }
        
        Double currentPrice = priceOpt.get().getCurrentPrice();
        Double totalProceeds = currentPrice * shares;
        
        // update portfolio balance
        Double newBalance = portfolio.getBalance() + totalProceeds;
        portfolioRepo.updateBalance(portfolioId, newBalance);
        
        // create transfer record
        transferRepo.createTransfer(portfolioId, totalProceeds, LocalDate.now(), "Sell Stock", symbol + " (" + shares + " shares)", "Portfolio " + portfolioId);
        
        // update current price in stocks_current table (this part is currently useless)
        stockRepo.updateCurrentPrice(symbol, currentPrice); 
        
        return "Stock sale successful. Sold " + shares + " shares of " + symbol + " at $" + currentPrice + " per share. " + "Total proceeds: $" + totalProceeds + ". New balance: $" + newBalance;
    }
}

