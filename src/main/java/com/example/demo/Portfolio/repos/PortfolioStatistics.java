package com.example.demo.Portfolio.repos;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
    public Map<String, Object> getPortfolioStatisticsJson(
            @RequestParam Long portfolioId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String timeRange,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
        }

        Optional<Portfolio> portfolioOpt = portfolioRepo.findPortfolioByUserIdAndPortfolioId(userId, portfolioId);
        if (!portfolioOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Portfolio not found or access denied");
        }

        DateRange range = resolvePortfolioRange(portfolioId, startDate, endDate, timeRange);
        if (range == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No stock data available for this portfolio");
        }

        // correlation matrix
        List<PortfolioCovarianceCache> matrix = statisticsCacheService.getPortfolioCovariance(portfolioId, range.start(), range.end());

        // convert to JSON-friendly map
        Map<String, Map<String, Double>> correlationMatrix = new HashMap<>();
        for (PortfolioCovarianceCache entry : matrix) {
            correlationMatrix
                .computeIfAbsent(entry.getSymbol1(), k -> new HashMap<>())
                .put(entry.getSymbol2(), entry.getCorrelation());
        }

        for (String stockA : correlationMatrix.keySet()) {
            for (String stockB : correlationMatrix.keySet()) {

                double valueAB =
                    correlationMatrix.getOrDefault(stockA, Map.of())
                                    .getOrDefault(stockB, Double.NaN);

                double valueBA =
                    correlationMatrix.getOrDefault(stockB, Map.of())
                                    .getOrDefault(stockA, Double.NaN);

                // If AB exists but BA doesn't, fill BA
                if (!Double.isNaN(valueAB) && Double.isNaN(valueBA)) {
                    correlationMatrix
                        .computeIfAbsent(stockB, k -> new HashMap<>())
                        .put(stockA, valueAB);
                }

                // If BA exists but AB doesn't, fill AB
                if (!Double.isNaN(valueBA) && Double.isNaN(valueAB)) {
                    correlationMatrix
                        .computeIfAbsent(stockA, k -> new HashMap<>())
                        .put(stockB, valueBA);
                }
            }
        }

        // Always enforce diagonal = 1.0
        for (String stock : correlationMatrix.keySet()) {
            correlationMatrix.get(stock).put(stock, 1.0);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("portfolioName", portfolioOpt.get().getPortfolioName());
        result.put("timePeriod", Map.of("start", range.start().toString(), "end", range.end().toString()));
        result.put("correlationMatrix", correlationMatrix);

        return result; // Spring will auto-convert this Map to JSON
    }


    
    @GetMapping("/stockstatistics")
    public Map<String, Object> getStockStatistics(
            @RequestParam String symbol,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpSession session) {
        
        Map<String, Object> result = new HashMap<>();

        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            result.put("error", "You are not logged in!");
            return result;
        }
        
        DateRange range = resolveSymbolRange(symbol, startDate, endDate);
        if (range == null) {
            result.put("error", "No data available for symbol " + symbol + " in the specified time period.");
            return result;
        }
        
        StockStatisticsCache stats = statisticsCacheService.getOrComputeStockStats(symbol, range.start(), range.end());
        
        if (stats == null) {
            result.put("error", "No data available for symbol " + symbol + " in the specified time period.");
            return result;
        }
        
        result.put("timeInterval", Map.of(
            "start", range.start().toString(),
            "end", range.end().toString()
        ));

        result.put("meanReturn", stats.getMeanReturn());
        result.put("standardDeviation", stats.getStdDev());
        result.put("cov", stats.getCoefficientOfVariation());
        result.put("beta", stats.getBeta());
        
        return result;
    }

    private DateRange resolveSymbolRange(String symbol, String startDate, String endDate) {
        LocalDate latest = stockDayRangeRepo.findLatestCombinedDate(symbol.trim().toUpperCase());
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

    private DateRange resolvePortfolioRange(Long portfolioId, String startDate, String endDate, String timeRange) {

        List<HoldingDetails> holdings = stockHoldingRepo.getHoldingDetailsByPortfolio(portfolioId);
        if (holdings.isEmpty()) {
            return null;
        }

        LocalDate latest = null;
        for (HoldingDetails holding : holdings) {
            LocalDate symbolLatest = stockDayRangeRepo.findLatestCombinedDate(holding.getSymbol());
            if (symbolLatest != null && (latest == null || symbolLatest.isAfter(latest))) {
                latest = symbolLatest;
            }
        }

        if (latest == null) return null;

        LocalDate end = endDate != null ? LocalDate.parse(endDate) : latest;
        if (end.isAfter(latest)) {
            end = latest;
        }

        LocalDate start;

        if (startDate != null) {
            start = LocalDate.parse(startDate);

        } else if (timeRange != null) {
            start = calculateStartDate(end, timeRange);   // ← PARSE HERE

        } else {
            start = end.minusYears(1);
        }

        if (start.isAfter(end)) {
            start = end.minusYears(1);
        }

        return new DateRange(start, end);
    }

    private LocalDate calculateStartDate(LocalDate end, String timeRange) {
        timeRange = timeRange.toLowerCase().trim();

        if (timeRange.endsWith("y")) {
            int years = Integer.parseInt(timeRange.replace("y", ""));
            return end.minusYears(years);
        }
        if (timeRange.endsWith("m")) {
            int months = Integer.parseInt(timeRange.replace("m", ""));
            return end.minusMonths(months);
        }
        if (timeRange.endsWith("w")) {
            int weeks = Integer.parseInt(timeRange.replace("w", ""));
            return end.minusWeeks(weeks);
        }

        // Default fallback
        return end.minusYears(1);
    }


    private record DateRange(LocalDate start, LocalDate end) {}
}

