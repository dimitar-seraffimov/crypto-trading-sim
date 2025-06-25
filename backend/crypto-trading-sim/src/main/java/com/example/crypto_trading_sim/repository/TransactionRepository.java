package com.example.crypto_trading_sim.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TransactionRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    public static class Transaction {
        private Long id;
        private Long userId;
        private Long cryptoId;
        private String cryptoSymbol;
        private String transactionType;
        private BigDecimal quantity;
        private BigDecimal pricePerUnit;
        private BigDecimal totalAmount;
        private BigDecimal balanceBefore;
        private BigDecimal balanceAfter;
        private LocalDateTime createdAt;
        
        public Transaction() {}
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getCryptoId() { return cryptoId; }
        public void setCryptoId(Long cryptoId) { this.cryptoId = cryptoId; }
        public String getCryptoSymbol() { return cryptoSymbol; }
        public void setCryptoSymbol(String cryptoSymbol) { this.cryptoSymbol = cryptoSymbol; }
        public String getTransactionType() { return transactionType; }
        public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public BigDecimal getPricePerUnit() { return pricePerUnit; }
        public void setPricePerUnit(BigDecimal pricePerUnit) { this.pricePerUnit = pricePerUnit; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public BigDecimal getBalanceBefore() { return balanceBefore; }
        public void setBalanceBefore(BigDecimal balanceBefore) { this.balanceBefore = balanceBefore; }
        public BigDecimal getBalanceAfter() { return balanceAfter; }
        public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
    
    private final RowMapper<Transaction> transactionRowMapper = (rs, rowNum) -> {
        Transaction transaction = new Transaction();
        transaction.setId(rs.getLong("id"));
        transaction.setUserId(rs.getLong("user_id"));
        transaction.setCryptoId(rs.getLong("crypto_id"));
        transaction.setCryptoSymbol(rs.getString("symbol"));
        transaction.setTransactionType(rs.getString("transaction_type"));
        transaction.setQuantity(rs.getBigDecimal("quantity"));
        transaction.setPricePerUnit(rs.getBigDecimal("price_per_unit"));
        transaction.setTotalAmount(rs.getBigDecimal("total_amount"));
        transaction.setBalanceBefore(rs.getBigDecimal("balance_before"));
        transaction.setBalanceAfter(rs.getBigDecimal("balance_after"));
        transaction.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return transaction;
    };
    
    public Long saveTransaction(Long userId, String cryptoSymbol, String transactionType, 
                               BigDecimal quantity, BigDecimal pricePerUnit, BigDecimal totalAmount,
                               BigDecimal balanceBefore, BigDecimal balanceAfter) {
        
        Long cryptoId = getOrCreateCrypto(cryptoSymbol);
        
        String sql = "INSERT INTO transactions (user_id, crypto_id, transaction_type, quantity, " +
                    "price_per_unit, total_amount, balance_before, balance_after) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        
        try {
            return jdbcTemplate.queryForObject(sql, Long.class, 
                userId, cryptoId, transactionType, quantity, pricePerUnit, totalAmount, balanceBefore, balanceAfter);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save transaction", e);
        }
    }
    
    public List<Transaction> findByUserId(Long userId) {
        String sql = "SELECT t.*, c.symbol FROM transactions t " +
                    "JOIN cryptocurrencies c ON t.crypto_id = c.id " +
                    "WHERE t.user_id = ? ORDER BY t.created_at DESC";
        
        return jdbcTemplate.query(sql, transactionRowMapper, userId);
    }
    
    public void deleteByUserId(Long userId) {
        String sql = "DELETE FROM transactions WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }
    
    private Long getOrCreateCrypto(String symbol) {
        String selectSql = "SELECT id FROM cryptocurrencies WHERE symbol = ?";
        try {
            return jdbcTemplate.queryForObject(selectSql, Long.class, symbol);
        } catch (Exception e) {
            String insertSql = "INSERT INTO cryptocurrencies (symbol, name) VALUES (?, ?) RETURNING id";
            String name = getCryptoName(symbol);
            return jdbcTemplate.queryForObject(insertSql, Long.class, symbol, name);
        }
    }
    
    private String getCryptoName(String symbol) {
        return switch (symbol) {
            case "BTC" -> "Bitcoin";
            case "ETH" -> "Ethereum";
            case "SOL" -> "Solana";
            case "ADA" -> "Cardano";
            case "AVAX" -> "Avalanche";
            case "DOT" -> "Polkadot";
            case "LINK" -> "Chainlink";
            case "UNI" -> "Uniswap";
            case "LTC" -> "Litecoin";
            case "ATOM" -> "Cosmos";
            case "ALGO" -> "Algorand";
            case "FIL" -> "Filecoin";
            case "TRX" -> "TRON";
            case "XLM" -> "Stellar";
            case "XRP" -> "Ripple";
            case "BCH" -> "Bitcoin Cash";
            case "NEAR" -> "NEAR Protocol";
            case "APT" -> "Aptos";
            case "MANA" -> "Decentraland";
            default -> symbol;
        };
    }
} 