package com.github.crypto.to.moon.trading.service.order.processor;

import com.github.crypto.to.moon.trading.service.order.EventWrapper;
import com.github.crypto.to.moon.trading.service.trading.Trading;
import org.springframework.stereotype.Component;

@Component
public class PlaceOrderProcessor implements Processor {

    @Override
    public void process(EventWrapper event) {

        // check balance ?


    }

    @Override
    public Trading.EventType getEventType() {
        return Trading.EventType.PLACE_ORDER;
    }
}
