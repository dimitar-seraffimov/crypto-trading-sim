package com.example.crypto_trading_sim.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class KrakenApiService {
    
    public static class CryptoPrice {
        private String symbol;
        private String name;
        private BigDecimal price;
        private BigDecimal change24h;
        private BigDecimal change24hPercent;
        private BigDecimal volume24h;
        private BigDecimal high24h;
        private BigDecimal low24h;
        private LocalDateTime lastUpdated;
        
        public CryptoPrice() {}
        
        public CryptoPrice(String symbol, String name, BigDecimal price, BigDecimal change24h, 
                          BigDecimal change24hPercent, BigDecimal volume24h, LocalDateTime lastUpdated) {
            this.symbol = symbol;
            this.name = name;
            this.price = price;
            this.change24h = change24h;
            this.change24hPercent = change24hPercent;
            this.volume24h = volume24h;
            this.lastUpdated = lastUpdated;
        }
        
        // Getters and setters
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public BigDecimal getChange24h() { return change24h; }
        public void setChange24h(BigDecimal change24h) { this.change24h = change24h; }
        public BigDecimal getChange24hPercent() { return change24hPercent; }
        public void setChange24hPercent(BigDecimal change24hPercent) { this.change24hPercent = change24hPercent; }
        public BigDecimal getVolume24h() { return volume24h; }
        public void setVolume24h(BigDecimal volume24h) { this.volume24h = volume24h; }
        public BigDecimal getHigh24h() { return high24h; }
        public void setHigh24h(BigDecimal high24h) { this.high24h = high24h; }
        public BigDecimal getLow24h() { return low24h; }
        public void setLow24h(BigDecimal low24h) { this.low24h = low24h; }
        public LocalDateTime getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    }
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final Map<String, CryptoPrice> cache = new ConcurrentHashMap<>();
    private final Sinks.Many<List<CryptoPrice>> priceStream = Sinks.many().multicast().onBackpressureBuffer();
    private volatile List<String> topUsdPairs = new ArrayList<>();
    private volatile Map<String, String> pairToSymbol = new HashMap<>();
    
    public KrakenApiService() {
        this.webClient = WebClient.builder()
            .baseUrl("https://api.kraken.com")
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)) // 1MB buffer
            .build();
        this.objectMapper = new ObjectMapper();
        initializeTopPairs();
    }
    
    private void initializeTopPairs() {
        fetchTopUsdPairs()
            .doOnNext(pairs -> {
                this.topUsdPairs = pairs;
                startPriceUpdates();
            })
            .doOnError(error -> {
                throw new RuntimeException("Failed to initialize Kraken API: " + error.getMessage());
            })
            .subscribe();
    }
    
    private Mono<List<String>> fetchTopUsdPairs() {
        return webClient.get()
            .uri("/0/public/AssetPairs")
            .retrieve()
            .bodyToMono(String.class)
            .flatMap(this::extractTopUsdPairs);
    }
    
    private Mono<List<String>> extractTopUsdPairs(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode result = root.get("result");
            if (result == null) throw new RuntimeException("Invalid Kraken API response");
            
            Map<String, String> usdPairs = new HashMap<>();
            result.fieldNames().forEachRemaining(pairName -> {
                JsonNode pair = result.get(pairName);
                if ("ZUSD".equals(pair.path("quote").asText()) && "online".equals(pair.path("status").asText())) {
                    String symbol = convertKrakenSymbol(pair.path("base").asText());
                    usdPairs.put(pairName, symbol);
                    pairToSymbol.put(pairName, symbol);
                }
            });
            
            if (usdPairs.isEmpty()) throw new RuntimeException("No USD trading pairs found");
            return getTickerDataAndSortByVolumeWithPriceFilter(usdPairs);
            
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Failed to parse Kraken response: " + e.getMessage(), e));
        }
    }
    
    private Mono<List<String>> getTickerDataAndSortByVolumeWithPriceFilter(Map<String, String> usdPairs) {
        String pairs = String.join(",", usdPairs.keySet());
        return webClient.get()
            .uri("/0/public/Ticker?pair=" + pairs)
            .retrieve()
            .bodyToMono(String.class)
            .map(tickerResponse -> {
                try {
                    JsonNode tickerRoot = objectMapper.readTree(tickerResponse);
                    JsonNode tickerResult = tickerRoot.get("result");
                    if (tickerResult == null) throw new RuntimeException("Invalid ticker response");
                    
                    return usdPairs.keySet().stream()
                        .filter(tickerResult::has)
                        .filter(pair -> {
                            try {
                                // Filter out micro-cap tokens with very low prices (likely meme coins)
                                BigDecimal price = new BigDecimal(tickerResult.get(pair).get("c").get(0).asText());
                                return price.compareTo(new BigDecimal("0.01")) >= 0; // Price >= $0.01
                            } catch (Exception e) {
                                return false;
                            }
                        })
                        .sorted((pair1, pair2) -> {
                            try {
                                // Sort by USD volume (volume * price for meaningful comparison)
                                JsonNode data1 = tickerResult.get(pair1);
                                JsonNode data2 = tickerResult.get(pair2);
                                
                                BigDecimal volume1 = new BigDecimal(data1.get("v").get(1).asText());
                                BigDecimal price1 = new BigDecimal(data1.get("c").get(0).asText());
                                BigDecimal usdVolume1 = volume1.multiply(price1);
                                
                                BigDecimal volume2 = new BigDecimal(data2.get("v").get(1).asText());
                                BigDecimal price2 = new BigDecimal(data2.get("c").get(0).asText());
                                BigDecimal usdVolume2 = volume2.multiply(price2);
                                
                                return usdVolume2.compareTo(usdVolume1);
                            } catch (Exception e) {
                                return 0;
                            }
                        })
                        .limit(20)
                        .collect(Collectors.toList());
                        
                } catch (Exception e) {
                    throw new RuntimeException("Failed to sort pairs by volume: " + e.getMessage(), e);
                }
            });
    }
    
    private String convertKrakenSymbol(String krakenSymbol) {
        return switch (krakenSymbol) {
            case "XXBT" -> "BTC";
            case "XETH" -> "ETH";
            case "XXRP" -> "XRP";
            case "XXLM" -> "XLM";
            case "XLTC" -> "LTC";
            default -> krakenSymbol;
        };
    }
    
    public Mono<List<CryptoPrice>> getAllCryptoPrices() {
        if (topUsdPairs.isEmpty()) {
            return Mono.error(new RuntimeException("No trading pairs available"));
        }
        
        String pairs = String.join(",", topUsdPairs);
        return webClient.get()
            .uri("/0/public/Ticker?pair=" + pairs)
            .retrieve()
            .bodyToMono(String.class)
            .map(this::parseResponse)
            .doOnNext(prices -> {
                prices.forEach(price -> cache.put(price.getSymbol(), price));
                priceStream.tryEmitNext(prices);
            });
    }
    
    public Mono<CryptoPrice> getCryptoPrice(String symbol) {
        return getAllCryptoPrices()
            .map(prices -> prices.stream()
                .filter(p -> p.getSymbol().equals(symbol.toUpperCase()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Price not found for " + symbol)));
    }
    
    public List<CryptoPrice> getCachedPrices() {
        if (cache.isEmpty()) throw new RuntimeException("No price data available");
        return new ArrayList<>(cache.values());
    }
    
    public Flux<List<CryptoPrice>> startPriceStream() {
        return priceStream.asFlux();
    }
    
    private void startPriceUpdates() {
        Flux.interval(Duration.ofSeconds(10))
            .flatMap(tick -> getAllCryptoPrices())
            .subscribe();
    }
    
    private List<CryptoPrice> parseResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode result = root.get("result");
            if (result == null) throw new RuntimeException("Invalid response format");
            
            List<CryptoPrice> prices = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();
            
            topUsdPairs.forEach(pair -> {
                JsonNode data = result.get(pair);
                if (data != null) {
                    try {
                        String symbol = pairToSymbol.get(pair);
                        if (symbol == null) return;
                        
                        BigDecimal price = new BigDecimal(data.get("c").get(0).asText());
                        BigDecimal high = new BigDecimal(data.get("h").get(1).asText());
                        BigDecimal low = new BigDecimal(data.get("l").get(1).asText());
                        BigDecimal volume = new BigDecimal(data.get("v").get(1).asText());
                        BigDecimal open = new BigDecimal(data.get("o").asText());
                        
                        BigDecimal change = price.subtract(open);
                        BigDecimal changePercent = open.compareTo(BigDecimal.ZERO) > 0 
                            ? change.divide(open, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                                : BigDecimal.ZERO;
                            
                        CryptoPrice crypto = new CryptoPrice(symbol, symbol, price, change, changePercent, volume, now);
                        crypto.setHigh24h(high);
                        crypto.setLow24h(low);
                        prices.add(crypto);
                        
                        } catch (Exception e) {
                        // Skip invalid data
                    }
                }
            });
            
            if (prices.isEmpty()) throw new RuntimeException("No valid price data found");
            return prices;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse price data: " + e.getMessage(), e);
        }
    }
    

} 