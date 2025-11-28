package com.example.demo.Portfolio.repos;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Portfolio.HoldingDetails;
import com.example.demo.Portfolio.Portfolio;
import com.example.demo.Portfolio.PortfolioCovarianceCache;
import com.example.demo.Portfolio.PortfolioRepo;
import com.example.demo.Portfolio.StockDayRangeRepo;
import com.example.demo.Portfolio.StockHoldingRepo;
import com.example.demo.Portfolio.StockStatisticsCache;
import com.example.demo.Portfolio.service.StatisticsCacheService;

import jakarta.servlet.http.HttpSession;

@RestController
public class PortfolioStatistics {
    
    @Autowired
    private PortfolioRepo portfolioRepo;
    
    @Autowired
    private StatisticsCacheService statisticsCacheService;

    @Autowired
    private StockDayRangeRepo stockDayRangeRepo;

    @Autowired
    private StockHoldingRepo stockHoldingRepo;
    
    @GetMapping("/portfoliostatistics")
    public String getPortfolioStatistics(@RequestParam Long portfolioId, @RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate, HttpSession session) {
        
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "Not logged in";
        }
        
        // verify portfolio ownership
        Optional<Portfolio> portfolioOpt = portfolioRepo.findPortfolioByUserIdAndPortfolioId(userId, portfolioId);
        
        if (!portfolioOpt.isPresent()) {
            return "Portfolio not found or access denied";
        }
        
        DateRange range = resolvePortfolioRange(portfolioId, startDate, endDate); // get the range based on holdings
        if (range == null) {
            return "No stock data available for this portfolio.";
        }
        
        StringBuilder header1 = new StringBuilder();
        // this is the header
        header1.append("Portfolio Statistics for: ").append(portfolioOpt.get().getPortfolioName()).append("\n");
        header1.append("Time Period: ").append(range.start()).append(" to ").append(range.end()).append("\n\n");
        
        Map<String, StockStatisticsCache> stockStats = statisticsCacheService.getPortfolioStockStats(portfolioId, range.start(), range.end());
        
        if (stockStats.isEmpty()) {
            return header1.append("No stock data available for this time period.\n").toString();
        }
        
        header1.append("Stock Statistics:\n");
        header1.append(String.format("%-10s %-15s %-15s %-20s %-15s\n", 
            "Symbol", "Mean Return", "Std Dev", "Coefficient of Var", "Beta"));
        header1.append("------------------------------------------------------------------------\n");
        
        for (Map.Entry<String, StockStatisticsCache> entry : stockStats.entrySet()) {
            StockStatisticsCache stat = entry.getValue();
            Double meanReturn = stat.getMeanReturn();
            Double stdDev = stat.getStdDev();
            Double cov = stat.getCoefficientOfVariation();
            Double beta = stat.getBeta();
            header1.append(String.format("%-10s %-15.6f %-15.6f %-20.6f %-15.6f\n",
                entry.getKey(),
                meanReturn != null ? meanReturn : 0.0,
                stdDev != null ? stdDev : 0.0,
                cov != null ? cov : 0.0,
                beta != null ? beta : 0.0));
        }
        
        header1.append("\n");
        
        List<PortfolioCovarianceCache> matrix = statisticsCacheService.getPortfolioCovariance(portfolioId, range.start(), range.end());
        
        if (!matrix.isEmpty()) {
            header1.append("Covariance/Correlation Matrix:\n");
            header1.append(String.format("%-10s %-10s %-20s %-20s\n", 
                "Stock 1", "Stock 2", "Covariance", "Correlation"));
            header1.append("------------------------------------------------------------\n");
            
            for (PortfolioCovarianceCache entry : matrix) {
                Double covariance = entry.getCovariance();
                Double correlation = entry.getCorrelation();
                header1.append(String.format("%-10s %-10s %-20.6f %-20.6f\n",
                    entry.getSymbol1(),
                    entry.getSymbol2(),
                    covariance != null ? covariance : 0.0,
                    correlation != null ? correlation : 0.0));
            }
        }
        
        return header1.toString();
    }
    
    @GetMapping("/stockstatistics")
    public String getStockStatistics(
            @RequestParam String symbol,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpSession session) {
        
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "Not logged in";
        }
        
        DateRange range = resolveSymbolRange(symbol, startDate, endDate);
        if (range == null) {
            return "No data available for symbol " + symbol + " in the specified time period.";
        }
        
        StockStatisticsCache stats = statisticsCacheService.getOrComputeStockStats(symbol, range.start(), range.end());
        
        if (stats == null) {
            return "No data available for symbol " + symbol + " in the specified time period.";
        }
        
        Double meanReturn = stats.getMeanReturn();
        Double stdDev = stats.getStdDev();
        Double cov = stats.getCoefficientOfVariation();
        Double beta = stats.getBeta();
        StringBuilder result = new StringBuilder();
        result.append("Statistics for ").append(symbol).append("\n");
        result.append("Time Period: ").append(range.start()).append(" to ").append(range.end()).append("\n\n");
        result.append("Mean Daily Return: ").append(String.format("%.6f", 
            meanReturn != null ? meanReturn : 0.0)).append("\n");
        result.append("Standard Deviation: ").append(String.format("%.6f", 
            stdDev != null ? stdDev : 0.0)).append("\n");
        result.append("Coefficient of Variation: ").append(String.format("%.6f", 
            cov != null ? cov : 0.0)).append("\n");
        result.append("Beta (vs Market): ").append(String.format("%.6f", 
            beta != null ? beta : 0.0)).append("\n");
        
        return result.toString();
    }

    private DateRange resolveSymbolRange(String symbol, String startDate, String endDate) {
        LocalDate latest = stockDayRangeRepo.findLatestDate(symbol.trim().toUpperCase());
        if (latest == null) {
            return null;
        }

        LocalDate end = endDate != null ? LocalDate.parse(endDate) : latest;
        if (end.isAfter(latest)) {
            end = latest;
        }

        LocalDate start = startDate != null ? LocalDate.parse(startDate) : end.minusYears(1);
        if (start.isAfter(end)) {
            start = end.minusYears(1);
        }

        return new DateRange(start, end);
    }

    private DateRange resolvePortfolioRange(Long portfolioId, String startDate, String endDate) {
        List<HoldingDetails> holdings = stockHoldingRepo.getHoldingDetailsByPortfolio(portfolioId);
        if (holdings.isEmpty()) {
            return null;
        }

        LocalDate latest = null;
        for (HoldingDetails holding : holdings) {
            LocalDate symbolLatest = stockDayRangeRepo.findLatestDate(holding.getSymbol());
            if (symbolLatest != null && (latest == null || symbolLatest.isAfter(latest))) {
                latest = symbolLatest;
            }
        }

        if (latest == null) {
            return null;
        }

        LocalDate end = endDate != null ? LocalDate.parse(endDate) : latest;
        if (end.isAfter(latest)) {
            end = latest;
        }

        LocalDate start = startDate != null ? LocalDate.parse(startDate) : end.minusYears(1);
        if (start.isAfter(end)) {
            start = end.minusYears(1);
        }

        return new DateRange(start, end);
    }

    private record DateRange(LocalDate start, LocalDate end) {}
}

