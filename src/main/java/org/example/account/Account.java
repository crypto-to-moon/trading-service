package org.example.account;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Account {
    private Long userId;
    private BigDecimal availableBalance = BigDecimal.ZERO;
    private BigDecimal frozenBalance = BigDecimal.ZERO;
    private BigDecimal position = BigDecimal.ZERO;

    public Account(Long userId) {
        this.userId = userId;
    }
}

