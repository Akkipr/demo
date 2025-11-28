package com.example.demo.Portfolio.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Portfolio.CovarianceMatrixEntry;
import com.example.demo.Portfolio.HoldingDetails;
import com.example.demo.Portfolio.PortfolioCovarianceCache;
import com.example.demo.Portfolio.PortfolioCovarianceCacheRepo;
import com.example.demo.Portfolio.PortfolioStatisticsRepo;
import com.example.demo.Portfolio.StockHoldingRepo;
import com.example.demo.Portfolio.StockStatistics;
import com.example.demo.Portfolio.StockStatisticsCache;
import com.example.demo.Portfolio.StockStatisticsCacheRepo;

@Service
public class StatisticsCacheService {

    @Autowired
    private StockStatisticsCacheRepo stockStatisticsCacheRepo;

    @Autowired
    private PortfolioCovarianceCacheRepo portfolioCovarianceCacheRepo;

    @Autowired
    private PortfolioStatisticsRepo portfolioStatisticsRepo;

    @Autowired
    private StockHoldingRepo stockHoldingRepo;

    public StockStatisticsCache getOrComputeStockStats(String symbol, LocalDate start, LocalDate end) {
        String normalizedSymbol = symbol.trim().toUpperCase();
        Optional<StockStatisticsCache> cached = stockStatisticsCacheRepo.findBySymbolAndStartDateAndEndDate(normalizedSymbol, start, end);
        if (cached.isPresent()) {
            return cached.get();
        }

        List<StockStatistics> computedStats = portfolioStatisticsRepo.getCoefficientOfVariation(normalizedSymbol, start, end);
        if (computedStats.isEmpty()) {
            return null;
        }

        StockStatistics stat = computedStats.get(0);
        Double beta = portfolioStatisticsRepo.getBeta(normalizedSymbol, start, end);
        stockStatisticsCacheRepo.upsert(normalizedSymbol, start, end,
            stat.getMeanReturn(), stat.getStdDev(), stat.getCoefficientOfVariation(), beta);

        return stockStatisticsCacheRepo.findBySymbolAndStartDateAndEndDate(normalizedSymbol, start, end).orElse(null);
    }

    public List<PortfolioCovarianceCache> getOrComputeCovarianceMatrix(Long portfolioId, LocalDate start, LocalDate end, List<String> symbols) {
        List<PortfolioCovarianceCache> cachedEntries =
            portfolioCovarianceCacheRepo.findByPortfolioIdAndStartDateAndEndDate(portfolioId, start, end);

        int symbolCount = symbols.size();              
        int expectedPairs = symbolCount * (symbolCount + 1) / 2;

        if (!symbols.isEmpty() && cachedEntries.size() == expectedPairs) {
            return cachedEntries;  // cache is complete
        }

        // compute covariance matrix
        List<CovarianceMatrixEntry> computedMatrix = portfolioStatisticsRepo.getCovarianceMatrix(portfolioId, start, end);

        portfolioCovarianceCacheRepo.deleteByPortfolioIdAndDateRange(portfolioId, start, end);

        for (CovarianceMatrixEntry entry : computedMatrix) {
            // upsert each entry, since we may have partial cache
            portfolioCovarianceCacheRepo.upsert(portfolioId, entry.getSymbol1(), entry.getSymbol2(), start, end, entry.getCovariance(), entry.getCorrelation());
        }

        return portfolioCovarianceCacheRepo.findByPortfolioIdAndStartDateAndEndDate(portfolioId, start, end);
    }

    @Transactional
    public void invalidateStockStatistics(String symbol) {
        String normalizedSymbol = symbol.trim().toUpperCase();
        stockStatisticsCacheRepo.deleteBySymbol(normalizedSymbol);
        List<Long> portfolioIds = stockHoldingRepo.findPortfolioIdsBySymbol(normalizedSymbol);
        for (Long pid : portfolioIds) {
            portfolioCovarianceCacheRepo.deleteByPortfolioId(pid);
        }
    }

    @Transactional
    public void invalidatePortfolioStatistics(Long portfolioId) {
        portfolioCovarianceCacheRepo.deleteByPortfolioId(portfolioId);
    }

    public Map<String, StockStatisticsCache> getPortfolioStockStats(Long portfolioId, LocalDate start, LocalDate end) {
        List<HoldingDetails> holdings = stockHoldingRepo.getHoldingDetailsByPortfolio(portfolioId);
        Set<String> symbols = new HashSet<>();
        for (HoldingDetails h : holdings) {
            symbols.add(h.getSymbol());
        }

        Map<String, StockStatisticsCache> statsMap = new java.util.TreeMap<>();
        for (String symbol : symbols) {
            StockStatisticsCache cache = getOrComputeStockStats(symbol, start, end);
            if (cache != null) {
                statsMap.put(symbol, cache);
            }
        }

        return statsMap;
    }

    public List<PortfolioCovarianceCache> getPortfolioCovariance(Long portfolioId, LocalDate start, LocalDate end) {
        List<HoldingDetails> holdings = stockHoldingRepo.getHoldingDetailsByPortfolio(portfolioId);
        List<String> symbols = new ArrayList<>();
        for (HoldingDetails h : holdings) {
            symbols.add(h.getSymbol());
        }
        return getOrComputeCovarianceMatrix(portfolioId, start, end, symbols);
    }
}

