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
public class UserRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    public static class User {
        private Long id;
        private String username;
        private String email;
        private BigDecimal currentBalance;
        private BigDecimal initialBalance;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        public User() {}
        
        public User(Long id, String username, String email, BigDecimal currentBalance, BigDecimal initialBalance, LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.currentBalance = currentBalance;
            this.initialBalance = initialBalance;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public BigDecimal getCurrentBalance() { return currentBalance; }
        public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }
        public BigDecimal getInitialBalance() { return initialBalance; }
        public void setInitialBalance(BigDecimal initialBalance) { this.initialBalance = initialBalance; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }
    
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setCurrentBalance(rs.getBigDecimal("current_balance"));
        user.setInitialBalance(rs.getBigDecimal("initial_balance"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return user;
    };
    
    public Optional<User> findById(Long id) {
        try {
            String sql = "SELECT * FROM users WHERE id = ?";
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);
            return Optional.of(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    public Optional<User> findByUsername(String username) {
        try {
            String sql = "SELECT * FROM users WHERE username = ?";
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, username);
            return Optional.of(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    public User createDemoUser() {
        String username = "demo-user";
        String email = "demo@example.com";
        BigDecimal initialBalance = new BigDecimal("10000.00");
        
        Optional<User> existingUser = findByUsername(username);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }
        
        String sql = "INSERT INTO users (username, email, password_hash, current_balance, initial_balance) " +
                    "VALUES (?, ?, ?, ?, ?) RETURNING id, created_at, updated_at";
        
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(username);
                user.setEmail(email);
                user.setCurrentBalance(initialBalance);
                user.setInitialBalance(initialBalance);
                user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                return user;
            }, username, email, "demo-hash", initialBalance, initialBalance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create demo user", e);
        }
    }
    
    public void updateBalance(Long userId, BigDecimal newBalance) {
        String sql = "UPDATE users SET current_balance = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, newBalance, userId);
        if (rowsAffected == 0) {
            throw new RuntimeException("User not found with id: " + userId);
        }
    }
    
    public void resetBalance(Long userId) {
        String sql = "UPDATE users SET current_balance = initial_balance, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, userId);
        if (rowsAffected == 0) {
            throw new RuntimeException("User not found with id: " + userId);
        }
    }
} 