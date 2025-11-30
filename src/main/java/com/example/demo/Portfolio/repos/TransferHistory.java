package com.example.demo.Portfolio.repos;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Portfolio.Portfolio;
import com.example.demo.Portfolio.PortfolioRepo;
import com.example.demo.Portfolio.Transfer;
import com.example.demo.Portfolio.TransferRepo;
import com.example.demo.Portfolio.response.TransferResponse;

import jakarta.servlet.http.HttpSession;

@RestController
public class TransferHistory {
    
    @Autowired
    private PortfolioRepo portfolioRepo;
    
    @Autowired
    private TransferRepo transferRepo;
    
    @GetMapping("/transferhistory")
    public List<TransferResponse> getTransferHistory(@RequestParam Long portfolioId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return List.of();
        }
        
        // verify portfolio ownership
        Optional<Portfolio> portfolioOpt = portfolioRepo.findPortfolioByUserIdAndPortfolioId(userId, portfolioId);
        
        if (!portfolioOpt.isPresent()) {
            return List.of();
        }
        
        // fetch transfer history
        List<Transfer> transfers = transferRepo.findTransfersByPortfolioId(portfolioId);
        
        // map to response types with normalized transfer types
        return transfers.stream().map(t -> {
                TransferResponse response = new TransferResponse();
                response.setTransferId(t.getTransferId());
                response.setAmount(t.getAmount());
                response.setDate(t.getDate());
                response.setTransType(normalizeType(t.getTransType()));
                response.setFromAcc((t.getFromAcc() != null ? t.getFromAcc() : "").trim());
                response.setToAcc((t.getToAcc() != null ? t.getToAcc() : "").trim());
                return response;
            })
            .collect(Collectors.toList());
    }
    // normalizes any transaction type string into a consistent format.
    private String normalizeType(String type) {
        if (type == null) {
            return "";
        }
        String normalized = type.trim().toLowerCase();
        return switch (normalized) {
            case "deposit" -> "Deposit";
            case "withdraw", "withdrawal" -> "Withdraw";
            case "buy stock", "buy" -> "Buy Stock";
            case "sell stock", "sell" -> "Sell Stock";
            default -> normalized.isEmpty() ? "" : normalized.substring(0, 1).toUpperCase() + normalized.substring(1); // return the cleaned value with the first letter capitalized or just return empty string by defalt.
        };
    }
}

