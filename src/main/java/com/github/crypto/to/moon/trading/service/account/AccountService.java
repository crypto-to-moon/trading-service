package com.github.crypto.to.moon.trading.service.account;

import com.github.crypto.to.moon.trading.service.order.Trade;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

public class AccountService {

    private static AccountService instance = new AccountService();

    // 用户账户信息，Key: userId，Value: 账户余额等信息
    private ConcurrentHashMap<Long, Account> accounts = new ConcurrentHashMap<>();

    public static AccountService getInstance() {
        return instance;
    }

    public boolean checkBalance(long userId, BigDecimal amount) {
        Account account = accounts.get(userId);
        return account.getAvailableBalance().compareTo(amount) >= 0;
    }

    public void freezeBalance(long userId, BigDecimal amount) {
        Account account = accounts.get(userId);
        account.setAvailableBalance(account.getAvailableBalance().subtract(amount));
        account.setFrozenBalance(account.getFrozenBalance().add(amount));
    }

    public void updateBalancesAfterTrade(long buyUserId, long sellUserId, Trade trade) {
        // 更新买方和卖方账户余额
        Account buyAccount = accounts.get(buyUserId);
        Account sellAccount = accounts.get(sellUserId);

        // 买方解冻资金，增加持仓
        buyAccount.setFrozenBalance(buyAccount.getFrozenBalance().subtract(trade.getQuantity()));
        buyAccount.setPosition(buyAccount.getPosition().add(trade.getQuantity()));

        // 卖方减少持仓，增加资金
        sellAccount.setPosition(sellAccount.getPosition().subtract(trade.getQuantity()));
        sellAccount.setAvailableBalance(sellAccount.getAvailableBalance().add(trade.getQuantity()));
    }
}

