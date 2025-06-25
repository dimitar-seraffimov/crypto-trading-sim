package com.example.crypto_trading_sim.service;

import com.example.crypto_trading_sim.repository.HoldingsRepository;
import com.example.crypto_trading_sim.repository.TransactionRepository;
import com.example.crypto_trading_sim.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimpleTradingService {
    
    private final KrakenApiService krakenApiService;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final HoldingsRepository holdingsRepository;
    
    private static final BigDecimal MIN_QUANTITY = new BigDecimal("0.00000001");
    
    public static class PortfolioHolding {
        private String symbol;
        private BigDecimal quantity;
        private BigDecimal avgPrice;
        private BigDecimal totalInvested;
        private LocalDateTime lastUpdated;
        
        public PortfolioHolding(String symbol, BigDecimal quantity, BigDecimal avgPrice, BigDecimal totalInvested) {
            this.symbol = symbol;
            this.quantity = quantity;
            this.avgPrice = avgPrice;
            this.totalInvested = totalInvested;
            this.lastUpdated = LocalDateTime.now();
        }
        
        // Getters and setters
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public BigDecimal getAvgPrice() { return avgPrice; }
        public void setAvgPrice(BigDecimal avgPrice) { this.avgPrice = avgPrice; }
        public BigDecimal getTotalInvested() { return totalInvested; }
        public void setTotalInvested(BigDecimal totalInvested) { this.totalInvested = totalInvested; }
        public LocalDateTime getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    }
    
    public static class TradeRecord {
        private String symbol;
        private String type;
        private BigDecimal quantity;
        private BigDecimal price;
        private BigDecimal total;
        private LocalDateTime timestamp;
        private String status;
        
        public TradeRecord(String symbol, String type, BigDecimal quantity, BigDecimal price, BigDecimal total) {
            this.symbol = symbol;
            this.type = type;
            this.quantity = quantity;
            this.price = price;
            this.total = total;
            this.timestamp = LocalDateTime.now();
            this.status = "COMPLETED";
        }
        
        public TradeRecord(String symbol, String type, BigDecimal quantity, BigDecimal price, BigDecimal total, LocalDateTime timestamp) {
            this.symbol = symbol;
            this.type = type;
            this.quantity = quantity;
            this.price = price;
            this.total = total;
            this.timestamp = timestamp;
            this.status = "COMPLETED";
        }
        
        // Getters and setters
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public BigDecimal getTotal() { return total; }
        public void setTotal(BigDecimal total) { this.total = total; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    public static class AccountSummary {
        private BigDecimal balance;
        private BigDecimal initialBalance;
        private BigDecimal totalPortfolioValue;
        private BigDecimal totalPnL;
        private BigDecimal totalPnLPercent;
        private List<PortfolioHoldingWithCurrentValue> holdings;
        
        public AccountSummary() {
            this.holdings = new ArrayList<>();
        }
        
        // Getters and setters
        public BigDecimal getBalance() { return balance; }
        public void setBalance(BigDecimal balance) { this.balance = balance; }
        public BigDecimal getInitialBalance() { return initialBalance; }
        public void setInitialBalance(BigDecimal initialBalance) { this.initialBalance = initialBalance; }
        public BigDecimal getTotalPortfolioValue() { return totalPortfolioValue; }
        public void setTotalPortfolioValue(BigDecimal totalPortfolioValue) { this.totalPortfolioValue = totalPortfolioValue; }
        public BigDecimal getTotalPnL() { return totalPnL; }
        public void setTotalPnL(BigDecimal totalPnL) { this.totalPnL = totalPnL; }
        public BigDecimal getTotalPnLPercent() { return totalPnLPercent; }
        public void setTotalPnLPercent(BigDecimal totalPnLPercent) { this.totalPnLPercent = totalPnLPercent; }
        public List<PortfolioHoldingWithCurrentValue> getHoldings() { return holdings; }
        public void setHoldings(List<PortfolioHoldingWithCurrentValue> holdings) { this.holdings = holdings; }
    }
    
    public static class PortfolioHoldingWithCurrentValue extends PortfolioHolding {
        private BigDecimal currentPrice;
        private BigDecimal currentValue;
        private BigDecimal pnl;
        private BigDecimal pnlPercent;
        
        public PortfolioHoldingWithCurrentValue(PortfolioHolding holding, BigDecimal currentPrice) {
            super(holding.getSymbol(), holding.getQuantity(), holding.getAvgPrice(), holding.getTotalInvested());
            this.currentPrice = currentPrice;
            this.currentValue = holding.getQuantity().multiply(currentPrice);
            this.pnl = this.currentValue.subtract(holding.getTotalInvested());
            this.pnlPercent = holding.getTotalInvested().compareTo(BigDecimal.ZERO) > 0 
                ? this.pnl.divide(holding.getTotalInvested(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        }
        
        // Getters and setters
        public BigDecimal getCurrentPrice() { return currentPrice; }
        public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
        public BigDecimal getCurrentValue() { return currentValue; }
        public void setCurrentValue(BigDecimal currentValue) { this.currentValue = currentValue; }
        public BigDecimal getPnl() { return pnl; }
        public void setPnl(BigDecimal pnl) { this.pnl = pnl; }
        public BigDecimal getPnlPercent() { return pnlPercent; }
        public void setPnlPercent(BigDecimal pnlPercent) { this.pnlPercent = pnlPercent; }
    }
    
    public synchronized TradeRecord executeTrade(String symbol, String type, BigDecimal quantity, BigDecimal price) {
        UserRepository.User user = userRepository.createDemoUser();
        BigDecimal totalCost = quantity.multiply(price);
        
        if ("BUY".equalsIgnoreCase(type)) {
            return executeBuyTrade(user, symbol, quantity, price, totalCost);
        } else if ("SELL".equalsIgnoreCase(type)) {
            return executeSellTrade(user, symbol, quantity, price, totalCost);
        } else {
            throw new IllegalArgumentException("Invalid trade type: " + type);
        }
    }
    
    private TradeRecord executeBuyTrade(UserRepository.User user, String symbol, BigDecimal quantity, BigDecimal price, BigDecimal totalCost) {
        if (user.getCurrentBalance().compareTo(totalCost) < 0) {
            throw new RuntimeException(String.format(
                "Insufficient balance. Required: $%.2f, Available: $%.2f", 
                totalCost, user.getCurrentBalance()));
        }
        
        BigDecimal balanceBefore = user.getCurrentBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(totalCost);
        
        userRepository.updateBalance(user.getId(), balanceAfter);
        
        Optional<HoldingsRepository.Holding> existingHolding = holdingsRepository.findByUserIdAndSymbol(user.getId(), symbol);
        if (existingHolding.isPresent()) {
            HoldingsRepository.Holding holding = existingHolding.get();
            BigDecimal newTotalQuantity = holding.getQuantity().add(quantity);
            BigDecimal newTotalInvested = holding.getQuantity().multiply(holding.getAveragePrice()).add(totalCost);
            BigDecimal newAvgPrice = newTotalInvested.divide(newTotalQuantity, 8, RoundingMode.HALF_UP);
            
            holdingsRepository.upsertHolding(user.getId(), symbol, newTotalQuantity, newAvgPrice);
        } else {
            holdingsRepository.upsertHolding(user.getId(), symbol, quantity, price);
        }
        
        transactionRepository.saveTransaction(user.getId(), symbol, "BUY", quantity, price, totalCost, balanceBefore, balanceAfter);
        return new TradeRecord(symbol, "BUY", quantity, price, totalCost);
    }
    
    private TradeRecord executeSellTrade(UserRepository.User user, String symbol, BigDecimal quantity, BigDecimal price, BigDecimal totalCost) {
        Optional<HoldingsRepository.Holding> holdingOpt = holdingsRepository.findByUserIdAndSymbol(user.getId(), symbol);
        if (holdingOpt.isEmpty()) {
            throw new RuntimeException("No holdings found for " + symbol);
        }
        
        HoldingsRepository.Holding holding = holdingOpt.get();
        
        if (holding.getQuantity().compareTo(quantity) < 0) {
            throw new RuntimeException(String.format(
                "Insufficient holdings. Required: %.8f, Available: %.8f", 
                quantity, holding.getQuantity()));
        }
        
        BigDecimal balanceBefore = user.getCurrentBalance();
        BigDecimal balanceAfter = balanceBefore.add(totalCost);
        
        userRepository.updateBalance(user.getId(), balanceAfter);
        
        BigDecimal remainingQuantity = holding.getQuantity().subtract(quantity);
        
        if (remainingQuantity.compareTo(MIN_QUANTITY) <= 0) {
            holdingsRepository.upsertHolding(user.getId(), symbol, BigDecimal.ZERO, holding.getAveragePrice());
            holdingsRepository.deleteZeroQuantityHoldings(user.getId());
        } else {
            holdingsRepository.upsertHolding(user.getId(), symbol, remainingQuantity, holding.getAveragePrice());
        }
        
        transactionRepository.saveTransaction(user.getId(), symbol, "SELL", quantity, price, totalCost, balanceBefore, balanceAfter);
        return new TradeRecord(symbol, "SELL", quantity, price, totalCost);
    }
    
    public AccountSummary getAccountSummary() {
        UserRepository.User user = userRepository.createDemoUser();
        List<KrakenApiService.CryptoPrice> currentPrices = krakenApiService.getCachedPrices();
        
        AccountSummary summary = new AccountSummary();
        summary.setBalance(user.getCurrentBalance());
        summary.setInitialBalance(user.getInitialBalance());
        
        BigDecimal totalPortfolioValue = BigDecimal.ZERO;
        List<PortfolioHoldingWithCurrentValue> holdingsWithValue = new ArrayList<>();
        
        List<HoldingsRepository.Holding> holdings = holdingsRepository.findByUserId(user.getId());
        
        for (HoldingsRepository.Holding holding : holdings) {
            KrakenApiService.CryptoPrice currentPrice = currentPrices.stream()
                .filter(price -> price.getSymbol().equals(holding.getCryptoSymbol()))
                .findFirst()
                .orElse(null);
            
            BigDecimal price;
            if (currentPrice != null) {
                price = currentPrice.getPrice();
            } else {
                // Try to get current price from API for this specific symbol
                try {
                    KrakenApiService.CryptoPrice specificPrice = krakenApiService.getCryptoPrice(holding.getCryptoSymbol()).block();
                    price = specificPrice != null ? specificPrice.getPrice() : BigDecimal.ZERO;
                } catch (Exception e) {
                    log.warn("Could not fetch current price for symbol: {}", holding.getCryptoSymbol());
                    price = BigDecimal.ZERO;
                }
            }
            
            PortfolioHolding portfolioHolding = new PortfolioHolding(
                holding.getCryptoSymbol(), 
                holding.getQuantity(), 
                holding.getAveragePrice(), 
                holding.getQuantity().multiply(holding.getAveragePrice())
            );
            
            PortfolioHoldingWithCurrentValue holdingWithValue = new PortfolioHoldingWithCurrentValue(portfolioHolding, price);
            holdingsWithValue.add(holdingWithValue);
            totalPortfolioValue = totalPortfolioValue.add(holdingWithValue.getCurrentValue());
        }
        
        summary.setHoldings(holdingsWithValue);
        summary.setTotalPortfolioValue(totalPortfolioValue);
        
        BigDecimal totalValue = user.getCurrentBalance().add(totalPortfolioValue);
        BigDecimal totalPnL = totalValue.subtract(user.getInitialBalance());
        BigDecimal totalPnLPercent = user.getInitialBalance().compareTo(BigDecimal.ZERO) > 0 
            ? totalPnL.divide(user.getInitialBalance(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;
        
        summary.setTotalPnL(totalPnL);
        summary.setTotalPnLPercent(totalPnLPercent);
        
        return summary;
    }
    
    public List<TradeRecord> getTradeHistory() {
        UserRepository.User user = userRepository.createDemoUser();
        List<TransactionRepository.Transaction> transactions = transactionRepository.findByUserId(user.getId());
        
        return transactions.stream()
            .map(transaction -> new TradeRecord(
                transaction.getCryptoSymbol(),
                transaction.getTransactionType(),
                transaction.getQuantity(),
                transaction.getPricePerUnit(),
                transaction.getTotalAmount(),
                transaction.getCreatedAt()
            ))
            .toList();
    }
    
    public synchronized void resetAccount() {
        UserRepository.User user = userRepository.createDemoUser();
        userRepository.resetBalance(user.getId());
        holdingsRepository.deleteByUserId(user.getId());
        transactionRepository.deleteByUserId(user.getId());
    }
    
    public BigDecimal getCurrentBalance() {
        UserRepository.User user = userRepository.createDemoUser();
        return user.getCurrentBalance();
    }
} 