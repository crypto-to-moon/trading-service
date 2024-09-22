package com.github.crypto.to.moon.trading.service.order.persist;

import com.lmax.disruptor.EventHandler;
import com.github.crypto.to.moon.trading.service.account.Account;
import com.github.crypto.to.moon.trading.service.order.EventWrapper;

import java.util.ArrayList;
import java.util.List;

public class OrderPersistenceHandler implements EventHandler<EventWrapper> {

    private final List<EventWrapper> batchEvents = new ArrayList<>();
    private final int batchSize = 100; // 根据需要调整批量大小

    @Override
    public void onEvent(EventWrapper eventWrapper, long sequence, boolean endOfBatch) throws Exception {
        batchEvents.add(eventWrapper);

        if (batchEvents.size() >= batchSize || endOfBatch) {
            // 批量持久化
            persistOrders(batchEvents);
            // 更新账户信息
            updateAccounts(batchEvents);
            batchEvents.clear();


        }
    }


    private void persistOrders(List<EventWrapper> events) {
        // 实现订单的批量持久化，例如插入数据库
    }

    private void updateAccounts(List<EventWrapper> events) {
        // 实现账户余额的更新，例如冻结订单金额

        for (EventWrapper event : events) {
            // 获取用户账户信息
            Account account = getAccountByUserId(event.getEventMessage().getOrderRequest().getUserId());

            // 发布账户信息更新
            publishAccountUpdate(account);
        }
    }

    private Account getAccountByUserId(Long userId) {
        // 获取账户信息，可以从缓存或数据库中获取
        return new Account(userId/* 余额等信息 */);
    }

    private void publishAccountUpdate(Account account) {
        // 实现账户信息的发布，例如通过 WebSocket 或消息队列通知用户
    }
}
