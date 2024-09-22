package com.github.crypto.to.moon.trading.service.order;

import com.lmax.disruptor.EventHandler;
import com.github.crypto.to.moon.trading.service.order.persist.OrderPersistenceDispatcher;
import com.github.crypto.to.moon.trading.service.order.processor.Processor;
import com.github.crypto.to.moon.trading.service.trading.Trading;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class OrderEventHandler implements EventHandler<EventWrapper> {

    @Autowired
    private Map<Trading.EventType, Processor> processorMap;

    @Autowired
    private OrderPersistenceDispatcher orderPersistenceDispatcher;

    public OrderEventHandler() {
    }

    @Override
    public void onEvent(EventWrapper event, long sequence, boolean endOfBatch) {
        Trading.EventMessage eventMessage = event.getEventMessage();

        Optional.ofNullable(processorMap.get(eventMessage.getEventType())).ifPresentOrElse(
                processor -> processor.process(event),
                () -> {
                    throw new IllegalArgumentException("Unsupported event type: " + eventMessage.getEventType());
                }
        );

        orderPersistenceDispatcher.publishOrderEvent(event);
    }
}
