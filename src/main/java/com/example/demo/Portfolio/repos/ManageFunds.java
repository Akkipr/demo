package com.example.demo.Portfolio.repos;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Portfolio.Portfolio;
import com.example.demo.Portfolio.PortfolioRepo;
import com.example.demo.Portfolio.TransferRepo;

import jakarta.servlet.http.HttpSession;

@RestController
public class ManageFunds {
    
    @Autowired
    private PortfolioRepo portfolioRepo;
    
    @Autowired
    private TransferRepo transferRepo;
    
    @PostMapping("/depositcash")
    public String depositCash(@RequestParam Long portfolioId, @RequestParam Double amount, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "Not logged in";
        }
        
        if (amount <= 0) {
            return "Amount must be greater than 0"; // validate amount
        }
        
        // verify portfolio ownership
        Optional<Portfolio> portfolioOpt = portfolioRepo.findPortfolioByUserIdAndPortfolioId(userId, portfolioId);
        
        if (!portfolioOpt.isPresent()) {
            return "Portfolio not found or access denied";
        }
        
        Portfolio portfolio = portfolioOpt.get();
        Double newBalance = portfolio.getBalance() + amount;
        portfolioRepo.updateBalance(portfolioId, newBalance); // update balance
        
        // create transfer record
        transferRepo.createTransfer(portfolioId, amount, LocalDate.now(), "Deposit", "External", "Portfolio " + portfolioId);
        
        return "Deposit successful. New balance: $" + String.format("%.2f", newBalance);
    }
    
    @PostMapping("/withdrawcash")
    public String withdrawCash(@RequestParam Long portfolioId, @RequestParam Double amount, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "Not logged in";
        }
        
        if (amount <= 0) {
            return "Amount must be greater than 0";
        }
        
        Optional<Portfolio> portfolioOpt = portfolioRepo.findPortfolioByUserIdAndPortfolioId(userId, portfolioId); // verify ownership
        
        if (!portfolioOpt.isPresent()) {
            return "Portfolio not found or access denied"; // not found or not owned by user
        }
        
        Portfolio portfolio = portfolioOpt.get(); // get portfolio details
        
        if (portfolio.getBalance() < amount) {
            return "Insufficient funds. Current balance: $" + String.format("%.2f", portfolio.getBalance()); // check sufficient balance, keep it simple and to the nearest 2 cents
        }
        
        Double newBalance = portfolio.getBalance() - amount;    // calculate new balance
        portfolioRepo.updateBalance(portfolioId, newBalance);   // update balance
        
        // create transfer record
        transferRepo.createTransfer(portfolioId, amount, LocalDate.now(), "Withdraw", "Portfolio " + portfolioId, "External");
        
        return "Withdrawal successful. New balance: $" + String.format("%.2f", newBalance);
    }
}

