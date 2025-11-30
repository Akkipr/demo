package com.example.demo.Portfolio.repos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Portfolio.HoldingDetails;
import com.example.demo.Portfolio.Portfolio;
import com.example.demo.Portfolio.PortfolioRepo;
import com.example.demo.Portfolio.StockHoldingRepo;
import com.example.demo.Portfolio.StockPriceInfo;
import com.example.demo.Portfolio.StockRepo;
import com.example.demo.Portfolio.response.HoldingResponse;
import com.example.demo.Portfolio.response.PortfolioOverviewResponse;

import jakarta.servlet.http.HttpSession;

@RestController
public class PortfolioDetails {
    
    @Autowired
    private PortfolioRepo portfolioRepo;
    
    @Autowired
    private StockHoldingRepo stockHoldingRepo;
    
    @Autowired
    private StockRepo stockRepo;
    
    @GetMapping("/portfoliooverview")
    public PortfolioOverviewResponse getPortfolioDetails(@RequestParam Long portfolioId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return null;
        }
        
        // verify portfolio ownership
        Optional<Portfolio> portfolioOpt = portfolioRepo.findPortfolioByUserIdAndPortfolioId(userId, portfolioId);
        
        if (!portfolioOpt.isPresent()) {
            return null;
        }
        
        // fetch holdings and calculate total values
        Portfolio portfolio = portfolioOpt.get();
        List<HoldingDetails> holdings = stockHoldingRepo.getHoldingDetailsByPortfolio(portfolioId);
        List<HoldingResponse> holdingResponses = new ArrayList<>();

        // calculate total market value
        double totalMarketValue = 0.0;
        
        for (HoldingDetails h : holdings) {
            // get current price for each holding
            Optional<StockPriceInfo> priceOpt = stockRepo.getLatestPrice(h.getSymbol());

            
            double currentPrice;
            if (priceOpt.isPresent() && priceOpt.get().getCurrentPrice() != null) {
                currentPrice = priceOpt.get().getCurrentPrice(); // use current price
            } else {
                currentPrice = 0.0;
            }            
            
            // calculate market value
            double marketValue = currentPrice * h.getShareCount();
            // accumulate total market value
            totalMarketValue += marketValue;
            holdingResponses.add(new HoldingResponse(h.getSymbol(), h.getShareCount(), currentPrice, marketValue));
        }
        
        double totalValue = totalMarketValue + portfolio.getBalance();
        
        return new PortfolioOverviewResponse(portfolio.getPortfolioId(), portfolio.getPortfolioName(), portfolio.getBalance(), totalMarketValue, totalValue, holdingResponses);
    }
    // subject to erase
    @GetMapping("/portfolioholdings")
    public List<HoldingResponse> getHoldings(@RequestParam Long portfolioId, HttpSession session) {

        PortfolioOverviewResponse overview = getPortfolioDetails(portfolioId, session); 
        if (overview == null) {
            return List.of();
        }
        return overview.getHoldings();
    }
}

