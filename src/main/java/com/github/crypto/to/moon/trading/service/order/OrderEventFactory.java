package com.github.crypto.to.moon.trading.service.order;

import com.lmax.disruptor.EventFactory;

public class OrderEventFactory implements EventFactory<EventWrapper> {

    @Override
    public EventWrapper newInstance() {
        return new EventWrapper();
    }
}
