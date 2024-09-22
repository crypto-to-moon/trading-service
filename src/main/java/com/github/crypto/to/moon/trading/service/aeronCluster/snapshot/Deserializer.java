package com.github.crypto.to.moon.trading.service.aeronCluster.snapshot;

import lombok.extern.slf4j.Slf4j;
import com.github.crypto.to.moon.trading.service.aeronCluster.raftlog.RaftDataEncoderAndDecoder;
import com.github.crypto.to.moon.trading.service.order.Order;
import com.github.crypto.to.moon.trading.service.order.Trade;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Deserializer {
    @SuppressWarnings("unchecked")
    public static List<RaftDataEncoderAndDecoder.RaftData> deserializeFromBytes(byte[] serializedBytes) {
        List<RaftDataEncoderAndDecoder.RaftData> deserializedList = new ArrayList<>();
        try (ByteArrayInputStream bais = new ByteArrayInputStream(serializedBytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            deserializedList = (List<RaftDataEncoderAndDecoder.RaftData>) ois.readObject();
            log.info("List deserialized from byte array successfully.");
        } catch (IOException | ClassNotFoundException e) {
            log.error("Error deserializing list from byte array.", e);
        }
        return deserializedList;
    }

    public static Order deserializeOrder(byte[] data) {
        Order order = null;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            order = (Order) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return order;
    }

    public static Trade deserializeTrade(byte[] data) {
        Trade trade = null;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            trade = (Trade) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return trade;
    }
}
