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
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class HoldingsRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    public static class Holding {
        private Long id;
        private Long userId;
        private Long cryptoId;
        private String cryptoSymbol;
        private BigDecimal quantity;
        private BigDecimal averagePrice;
        private LocalDateTime updatedAt;
        
        public Holding() {}
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getCryptoId() { return cryptoId; }
        public void setCryptoId(Long cryptoId) { this.cryptoId = cryptoId; }
        public String getCryptoSymbol() { return cryptoSymbol; }
        public void setCryptoSymbol(String cryptoSymbol) { this.cryptoSymbol = cryptoSymbol; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public BigDecimal getAveragePrice() { return averagePrice; }
        public void setAveragePrice(BigDecimal averagePrice) { this.averagePrice = averagePrice; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }
    
    private final RowMapper<Holding> holdingRowMapper = (rs, rowNum) -> {
        Holding holding = new Holding();
        holding.setId(rs.getLong("id"));
        holding.setUserId(rs.getLong("user_id"));
        holding.setCryptoId(rs.getLong("crypto_id"));
        holding.setCryptoSymbol(rs.getString("symbol"));
        holding.setQuantity(rs.getBigDecimal("quantity"));
        holding.setAveragePrice(rs.getBigDecimal("average_price"));
        holding.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return holding;
    };
    
    public List<Holding> findByUserId(Long userId) {
        String sql = "SELECT h.*, c.symbol FROM user_holdings h " +
                    "JOIN cryptocurrencies c ON h.crypto_id = c.id " +
                    "WHERE h.user_id = ? AND h.quantity > 0 " +
                    "ORDER BY h.updated_at DESC";
        
        return jdbcTemplate.query(sql, holdingRowMapper, userId);
    }
    
    public Optional<Holding> findByUserIdAndSymbol(Long userId, String cryptoSymbol) {
        String sql = "SELECT h.*, c.symbol FROM user_holdings h " +
                    "JOIN cryptocurrencies c ON h.crypto_id = c.id " +
                    "WHERE h.user_id = ? AND c.symbol = ?";
        
        try {
            Holding holding = jdbcTemplate.queryForObject(sql, holdingRowMapper, userId, cryptoSymbol);
            return Optional.of(holding);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    public void upsertHolding(Long userId, String cryptoSymbol, BigDecimal quantity, BigDecimal averagePrice) {
        Long cryptoId = getOrCreateCrypto(cryptoSymbol);
        
        String checkSql = "SELECT id FROM user_holdings WHERE user_id = ? AND crypto_id = ?";
        try {
            Long existingId = jdbcTemplate.queryForObject(checkSql, Long.class, userId, cryptoId);
            String updateSql = "UPDATE user_holdings SET quantity = ?, average_price = ?, updated_at = CURRENT_TIMESTAMP " +
                              "WHERE user_id = ? AND crypto_id = ?";
            jdbcTemplate.update(updateSql, quantity, averagePrice, userId, cryptoId);
        } catch (Exception e) {
            String insertSql = "INSERT INTO user_holdings (user_id, crypto_id, quantity, average_price) " +
                              "VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(insertSql, userId, cryptoId, quantity, averagePrice);
        }
    }
    
    public void deleteByUserId(Long userId) {
        String sql = "DELETE FROM user_holdings WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }
    
    public void deleteZeroQuantityHoldings(Long userId) {
        String sql = "DELETE FROM user_holdings WHERE user_id = ? AND quantity <= 0.00000001";
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