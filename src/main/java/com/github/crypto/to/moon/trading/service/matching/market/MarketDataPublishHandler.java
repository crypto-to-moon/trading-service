package com.github.crypto.to.moon.trading.service.matching.market;

import com.lmax.disruptor.EventHandler;
import com.github.crypto.to.moon.trading.service.matching.MatchingEngineGateway;
import com.github.crypto.to.moon.trading.service.matching.MatchingEvent;
import com.github.crypto.to.moon.trading.service.order.Order;
import com.github.crypto.to.moon.trading.service.order.Trade;

import java.util.ArrayList;
import java.util.List;

public class MarketDataPublishHandler implements EventHandler<MatchingEvent> {

    private List<MatchingResult> matchingResults;

    public MarketDataPublishHandler() {
        this.matchingResults = new ArrayList<>(100);
    }
    @Override
    public void onEvent(MatchingEvent event, long sequence, boolean endOfBatch) throws Exception {
        List<Trade> trades = event.getTrades();
        Order updatedOrder = event.getUpdatedOrder();

        matchingResults.add(new MatchingResult(updatedOrder, trades));
        if (matchingResults.size() == 100) {
            // 发布市场数据更新
            publishMarketData(trades);

            // 返回撮合结果给订单模块
            sendMatchingResult(updatedOrder, trades);
            matchingResults.clear();
        }


    }

    private void publishMarketData(List<Trade> trades) {
        // 实现市场数据的发布，例如通过消息队列或 WebSocket 广播
    }

    private void sendMatchingResult(Order order, List<Trade> trades) {
        // 使用 Aeron 将撮合结果返回给订单模块
        MatchingResult result = new MatchingResult(order, trades);
        MatchingEngineGateway.getInstance().sendMatchingResult(result);
    }
}

