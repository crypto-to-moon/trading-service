package com.github.crypto.to.moon.trading.service.order.processor;

import com.github.crypto.to.moon.trading.service.trading.Trading;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class ProcessorConfig {

    private final List<Processor> processorList;

    public ProcessorConfig(List<Processor> processorList) {
        this.processorList = processorList;
    }

    @Bean
    public Map<Trading.EventType, Processor> processorMap() {
        Map<Trading.EventType, Processor> processorMap = new HashMap<>();
        for (Processor processor : processorList) {
            processorMap.put(processor.getEventType(), processor);
        }
        return processorMap;
    }
}
