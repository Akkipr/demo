package com.example.demo.Portfolio.repos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Portfolio.PortfolioRepo;
import com.example.demo.Portfolio.StockDayRange;
import com.example.demo.Portfolio.StockDayRangeRepo;
import com.example.demo.Portfolio.response.PricePoint;

import jakarta.servlet.http.HttpSession;

@RestController
public class HistoricalPrices {
    
    @Autowired
    private StockDayRangeRepo stockDayRangeRepo;
    
    @Autowired
    private PortfolioRepo portfolioRepo;
    
    @GetMapping("/historicalprices")
    public List<PricePoint> getHistoricalPrices(@RequestParam(required = false) Long portfolioId, @RequestParam(required = false) String symbol, @RequestParam(required = false) String interval, @RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate, HttpSession session) {
        
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return List.of();
        }
        
        if (portfolioId != null && portfolioRepo.findPortfolioByUserIdAndPortfolioId(userId, portfolioId).isEmpty()) {
            return List.of();
        }
        
        if (symbol == null) {
            return List.of();
        }
        
        LocalDate latest = stockDayRangeRepo.findLatestCombinedDate(symbol);
        if (latest == null) {
            return List.of();
        }
        
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : latest;
        if (end.isAfter(latest)) {
            end = latest;
        }
        
        LocalDate start;
        if (startDate != null) {
            start = LocalDate.parse(startDate);
        } else if (interval != null) {
            start = switch (interval.toLowerCase()) {
                case "week" -> end.minusWeeks(1);
                case "month" -> end.minusMonths(1);
                case "quarter" -> end.minusMonths(3);
                case "year" -> end.minusYears(1);
                case "fiveyears", "5years" -> end.minusYears(5);
                default -> end.minusYears(1);
            };
        } else {
            start = end.minusYears(1);
        }
        
        if (start.isAfter(end)) {
            start = end.minusYears(1);
        }

        
        List<StockDayRange> combinedPrices = stockDayRangeRepo.findCombinedPrices(symbol, start, end);
        
        List<PricePoint> result = new ArrayList<>();
        for (StockDayRange price : combinedPrices) {
            Double close = price.getClose() != null ? price.getClose() : price.getOpen();
            if (close == null) close = 0.0;
            result.add(new PricePoint(price.getTimestamp(), close));
        }
        return result;
    }
}