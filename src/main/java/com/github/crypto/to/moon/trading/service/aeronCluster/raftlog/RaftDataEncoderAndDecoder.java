package com.github.crypto.to.moon.trading.service.aeronCluster.raftlog;

import lombok.Data;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

import java.io.Serializable;
import java.util.List;


public class RaftDataEncoderAndDecoder {
    public static int encoder(MutableDirectBuffer buffer, Long key, String value) {
        buffer.putLong(0, key);
        buffer.putInt(8, value.length());
        buffer.putBytes(12, value.getBytes());
        return 12 + value.length();
    }

    public static RaftData decoder(DirectBuffer buffer, int offset, List<RaftData> clusterData) {
        long key = buffer.getLong(offset);
        int valueLength = buffer.getInt(offset + 8);

        byte[] valueBytesArray = new byte[valueLength];
        buffer.getBytes(offset + 12, valueBytesArray);
        String value = new String(valueBytesArray);
        RaftData raftData = new RaftData(key, value);
        clusterData.add(new RaftData(key, value));
        return raftData;
    }


    @Data
    public static class RaftData implements Serializable {
        long key;
        String value;

        public RaftData(long key, String value) {
            this.key = key;
            this.value = value;
        }

    }
}
