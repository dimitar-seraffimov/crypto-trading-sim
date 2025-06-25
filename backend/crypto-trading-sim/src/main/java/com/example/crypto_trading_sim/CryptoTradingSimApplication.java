package com.example.crypto_trading_sim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling
public class CryptoTradingSimApplication {

	public static void main(String[] args) {
		SpringApplication.run(CryptoTradingSimApplication.class, args);
	}

}
