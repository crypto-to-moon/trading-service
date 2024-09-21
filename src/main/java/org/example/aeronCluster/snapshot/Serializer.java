package org.example.aeronCluster.snapshot;

import org.example.aeronCluster.raftlog.RaftDataEncoderAndDecoder;
import org.example.order.Order;
import org.example.order.Trade;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

public class Serializer {
    public static byte[] serializeToBytes(List<RaftDataEncoderAndDecoder.RaftData> list) {
        byte[] serializedBytes = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(list);
            serializedBytes = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serializedBytes;
    }

    public static byte[] serializeOrder(Order order) {
        byte[] serializedBytes = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(order);
            serializedBytes = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serializedBytes;
    }

    public static byte[] serializeTrade(Trade trade) {
        byte[] serializedBytes = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(trade);
            serializedBytes = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serializedBytes;
    }
}
