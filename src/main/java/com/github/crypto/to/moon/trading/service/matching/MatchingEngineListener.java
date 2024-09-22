package com.github.crypto.to.moon.trading.service.matching;

import com.github.crypto.to.moon.trading.service.order.OrderService;
import com.github.crypto.to.moon.trading.service.order.OrderUpdate;
import com.github.crypto.to.moon.trading.service.order.Trade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MatchingEngineListener {

    @Autowired
    private OrderService orderService;

    public void onOrderUpdate(OrderUpdate orderUpdate) {
        // 更新订单状态
        orderService.updateOrderStatus(orderUpdate.getOrderId(), orderUpdate.getNewStatus());
        // 如果有成交信息，处理成交记录
        if (orderUpdate.getTrade() != null) {
            processTrade(orderUpdate.getTrade());
        }
    }

    private void processTrade(Trade trade) {
        // 处理成交记录
        // 更新用户账户余额、发送通知等
        System.out.println("处理成交记录：tradeId=" + trade.getTradeId());
    }
}

