package com.example.demo.Portfolio.repos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Portfolio.StockDayRange;
import com.example.demo.Portfolio.StockDayRangeRepo;
import com.example.demo.Portfolio.response.PricePoint;

import jakarta.servlet.http.HttpSession;

@RestController
public class FuturePrices {

    @Autowired
    private StockDayRangeRepo stockDayRangeRepo;

    @GetMapping("/futureprices")
    public List<PricePoint> getFuturePrices(@RequestParam String symbol, @RequestParam(required = false, defaultValue = "30") Integer days, @RequestParam(required = false) String startDate, HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return List.of();      // usually shouldn't happen for price data but why nottt
        }

        if (symbol == null || symbol.isBlank()) {
            return List.of();      // symbol is required
        }
        String normalizedSymbol = symbol.trim().toUpperCase();
        // this whole thing is probably not needed now that I added a dropdown menu of options in frontend (todo)
        if (days == null || days <= 0) {
            days = 30;      // probably dont need this since we set default value, but just in case
        } else if (days > 365) {
            days = 365;     // cap max days to 1 year
        }

        LocalDate latest = stockDayRangeRepo.findLatestDate(normalizedSymbol);
        if (latest == null) {
            return List.of();  // no data for this symbol
        }

        LocalDate start = startDate != null ? LocalDate.parse(startDate) : latest.plusDays(1);
        LocalDate historicalStart = latest.minusYears(1);
        LocalDate historicalEnd = latest;

        // get the historical data for the past year to base predictions on
        List<StockDayRange> history = stockDayRangeRepo.findBySymbolAndDateRange(normalizedSymbol, historicalStart, historicalEnd);
        if (history.isEmpty()) {
            history = stockDayRangeRepo.findBySymbol(normalizedSymbol);
        }

        if (history.isEmpty()) {
            return List.of();   // still no data, (probably not the best way to go)
        }

        history.sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));
        List<Double> closes = new ArrayList<>();
        for (StockDayRange day : history) {
            Double close = day.getClose() != null ? day.getClose() : day.getOpen();
            if (close != null) {
                closes.add(close); // still no data
            }
        }

        if (closes.isEmpty()) {
            return List.of();
        }

        List<PricePoint> predictions = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            Double price = closes.get(i % closes.size());               // simple repeating pattern based on historical closes, could be improved with real prediction algorithms but we js using previous years
            predictions.add(new PricePoint(start.plusDays(i), price));  // assign date and predicted price
        }

        return predictions;
    }
}