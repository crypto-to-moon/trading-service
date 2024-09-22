package com.github.crypto.to.moon.trading.service.order.processor;

import com.github.crypto.to.moon.trading.service.order.EventWrapper;
import com.github.crypto.to.moon.trading.service.trading.Trading;

public interface Processor {

    void process(EventWrapper event);

    Trading.EventType getEventType();
}
