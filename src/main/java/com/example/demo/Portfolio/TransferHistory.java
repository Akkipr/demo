package com.example.demo.Portfolio;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

@RestController
public class TransferHistory {
    
    @Autowired
    private PortfolioRepo portfolioRepo;
    
    @Autowired
    private TransferRepo transferRepo;
    
    @GetMapping("/transferhistory")
    public String getTransferHistory(@RequestParam Long portfolioId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "Not logged in";
        }
        Optional<Portfolio> portfolioOpt = portfolioRepo.findPortfolioByUserIdAndPortfolioId(userId, portfolioId);
        if (!portfolioOpt.isPresent()) {
            return "Portfolio not found or access denied";
        }
        List<Transfer> transfers = transferRepo.findTransfersByPortfolioId(portfolioId);
        if (transfers.isEmpty()) {
            return "No transfer history found";
        }
        String result = "Transfer History for Portfolio: " + portfolioOpt.get().getPortfolioName() + "\n\n";

        for (Transfer t : transfers) {
            result += "[" + t.getTransType() + "] - " + t.getDate();
            if (t.getFromAcc() != null && !t.getFromAcc().isEmpty()) {
                result += " - From: " + t.getFromAcc();
            }
            if (t.getToAcc() != null && !t.getToAcc().isEmpty()) {
                result += " - To: " + t.getToAcc();
            }
            
            result += "\n";
        }

        return result;
    }
}

