package com.example.crypto_trading_sim.controller;

import com.example.crypto_trading_sim.service.SimpleTradingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/simple-trading")
@RequiredArgsConstructor
public class SimpleTradingController {
    
    private final SimpleTradingService tradingService;
    
    /**
     * Execute a trade (buy or sell) without requiring user account
     */
    @PostMapping("/trade")
    public ResponseEntity<?> executeTrade(@RequestBody Map<String, Object> request) {
        try {
            String symbol = (String) request.get("symbol");
            String type = (String) request.get("type");
            BigDecimal quantity = new BigDecimal(request.get("quantity").toString());
            BigDecimal price = new BigDecimal(request.get("price").toString());
            
            SimpleTradingService.TradeRecord trade = tradingService.executeTrade(symbol, type, quantity, price);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", String.format("%s trade completed successfully", type),
                "trade", Map.of(
                    "symbol", trade.getSymbol(),
                    "type", trade.getType(),
                    "quantity", trade.getQuantity(),
                    "price", trade.getPrice(),
                    "total", trade.getTotal(),
                    "timestamp", trade.getTimestamp(),
                    "status", trade.getStatus()
                )
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Get account summary including portfolio and P&L
     */
    @GetMapping("/account")
    public ResponseEntity<?> getAccount() {
        try {
            SimpleTradingService.AccountSummary summary = tradingService.getAccountSummary();
            
            Map<String, Object> accountData = new HashMap<>();
            accountData.put("balance", summary.getBalance());
            accountData.put("initialBalance", summary.getInitialBalance());
            accountData.put("portfolioValue", summary.getTotalPortfolioValue());
            accountData.put("totalValue", summary.getBalance().add(summary.getTotalPortfolioValue()));
            accountData.put("totalPnL", summary.getTotalPnL());
            accountData.put("totalPnLPercentage", summary.getTotalPnLPercent());
            accountData.put("totalInvested", summary.getBalance().add(summary.getTotalPortfolioValue()).subtract(summary.getTotalPnL()));
            accountData.put("realizedPnL", BigDecimal.ZERO);
            accountData.put("unrealizedPnL", summary.getTotalPnL());
            accountData.put("transactionCount", tradingService.getTradeHistory().size());
            accountData.put("portfolio", summary.getHoldings().stream().map(holding -> {
                Map<String, Object> portfolioItem = new HashMap<>();
                portfolioItem.put("symbol", holding.getSymbol());
                portfolioItem.put("quantity", holding.getQuantity());
                portfolioItem.put("avgPrice", holding.getAvgPrice());
                portfolioItem.put("totalInvested", holding.getTotalInvested());
                portfolioItem.put("currentPrice", holding.getCurrentPrice());
                portfolioItem.put("currentValue", holding.getCurrentValue());
                portfolioItem.put("pnl", holding.getPnl());
                portfolioItem.put("pnlPercent", holding.getPnlPercent());
                portfolioItem.put("lastUpdated", holding.getLastUpdated());
                return portfolioItem;
            }).toList());
            
            return ResponseEntity.ok(Map.of("success", true, "account", accountData));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Reset account to initial state
     */
    @PostMapping("/account/reset")
    public ResponseEntity<?> resetAccount() {
        try {
            tradingService.resetAccount();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Account has been reset to initial state"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Get transaction history
     */
    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactionHistory() {
        try {
            List<SimpleTradingService.TradeRecord> trades = tradingService.getTradeHistory();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "transactions", trades.stream().map(trade -> Map.of(
                    "symbol", trade.getSymbol(),
                    "type", trade.getType(),
                    "quantity", trade.getQuantity(),
                    "price", trade.getPrice(),
                    "total", trade.getTotal(),
                    "timestamp", trade.getTimestamp(),
                    "status", trade.getStatus()
                )).toList()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Get current balance only
     */
    @GetMapping("/balance")
    public ResponseEntity<?> getBalance() {
        try {
            BigDecimal balance = tradingService.getCurrentBalance();
            return ResponseEntity.ok(Map.of("success", true, "balance", balance));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
} 