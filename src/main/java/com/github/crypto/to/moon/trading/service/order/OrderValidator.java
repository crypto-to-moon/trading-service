package com.github.crypto.to.moon.trading.service.order;

import java.math.BigDecimal;

import static com.github.crypto.to.moon.trading.service.order.Order.OrderType.LIMIT;
import static com.github.crypto.to.moon.trading.service.order.Order.OrderType.MARKET;
import static com.github.crypto.to.moon.trading.service.order.Order.Side.BUY;
import static com.github.crypto.to.moon.trading.service.order.Order.Side.SELL;

public class OrderValidator {

    public static boolean validate(Order order) {
        // 检查订单必要字段是否为空
        if (order.getUserId() == null || order.getSymbol() == null ||
                order.getType() == null || order.getSide() == null ||
                order.getAmount() == null) {
            return false;
        }

        // 检查订单类型是否合法
        if (order.getType() != LIMIT && order.getType() != MARKET ) {
            return false;
        }

        // 检查买卖方向是否合法
        if (order.getSide() != BUY  && order.getSide() != SELL ) {
            return false;
        }

        // 检查数量和价格是否为正数
        if (order.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        if (order.getType() == LIMIT &&
                (order.getPrice() == null || order.getPrice().compareTo(BigDecimal.ZERO) <= 0)) {
            return false;
        }

        // TODO: 添加更多验证，例如账户余额检查等

        return true;
    }
}

