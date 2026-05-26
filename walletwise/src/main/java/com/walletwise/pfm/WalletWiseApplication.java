package com.walletwise.pfm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * WalletWise - Personal Finance Manager
 * A comprehensive finance tracking API with session-based authentication,
 * transaction management, savings goals, and monthly/yearly reports.
 */
@SpringBootApplication
public class WalletWiseApplication {

    public static void main(String[] args) {
        SpringApplication.run(WalletWiseApplication.class, args);
    }
}
