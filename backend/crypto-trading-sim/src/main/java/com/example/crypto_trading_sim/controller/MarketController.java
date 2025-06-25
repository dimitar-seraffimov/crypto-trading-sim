package com.example.crypto_trading_sim.controller;

import com.example.crypto_trading_sim.service.KrakenApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
public class MarketController {
    
    private final KrakenApiService krakenApiService;
    private final ObjectMapper objectMapper;
    
    /**
     * Get current prices for all supported cryptocurrencies
     */
    @GetMapping("/prices")
    public Mono<ResponseEntity<List<KrakenApiService.CryptoPrice>>> getAllPrices() {
        return krakenApiService.getAllCryptoPrices()
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.internalServerError().build());
    }
    
    /**
     * Get current price for a specific cryptocurrency
     */
    @GetMapping("/prices/{symbol}")
    public Mono<ResponseEntity<KrakenApiService.CryptoPrice>> getCryptoPrice(@PathVariable String symbol) {
        return krakenApiService.getCryptoPrice(symbol)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.notFound().build());
    }
    
    /**
     * Get cached prices (synchronous endpoint for immediate response)
     */
    @GetMapping("/prices/cached")
    public ResponseEntity<List<KrakenApiService.CryptoPrice>> getCachedPrices() {
        try {
            return ResponseEntity.ok(krakenApiService.getCachedPrices());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Server-Sent Events stream for real-time price updates
     */
    @GetMapping(value = "/prices/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getPriceStream() {
        return krakenApiService.startPriceStream()
            .map(priceList -> {
                try {
                    return objectMapper.writeValueAsString(priceList);
                } catch (Exception e) {
                    return "[]";
                }
            });
    }
} 