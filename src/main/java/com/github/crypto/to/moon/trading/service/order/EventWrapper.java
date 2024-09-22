package com.github.crypto.to.moon.trading.service.order;

import lombok.Data;
import com.github.crypto.to.moon.trading.service.trading.Trading;

@Data
public class EventWrapper {

    private Trading.EventMessage eventMessage;
    private long createTime;
    private long eventTime;

    public EventWrapper() {
        this.createTime = System.currentTimeMillis();
    }

    public void setEventMessage(Trading.EventMessage eventMessage) {
        this.eventMessage = eventMessage;
        this.eventTime = System.currentTimeMillis();
    }
}
