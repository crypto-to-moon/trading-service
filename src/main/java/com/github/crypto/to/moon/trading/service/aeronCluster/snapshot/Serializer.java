package com.github.crypto.to.moon.trading.service.aeronCluster.snapshot;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.github.crypto.to.moon.trading.service.aeronCluster.raftlog.RaftDataEncoderAndDecoder;
import com.github.crypto.to.moon.trading.service.order.Order;
import com.github.crypto.to.moon.trading.service.order.Trade;
import com.github.crypto.to.moon.trading.service.trading.Trading;

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

    public static byte[] serialize(Message obj) {
        return obj.toByteArray();
    }

    public static Trading.EventMessage deserialize(byte[] data) throws InvalidProtocolBufferException {

        return Trading.EventMessage.parseFrom(data);
    }
}
