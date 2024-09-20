package org.example.aeronCluster.snapshot;

import lombok.extern.slf4j.Slf4j;
import org.example.aeronCluster.raftlog.RaftData;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Deserializer {
    @SuppressWarnings("unchecked")
    public static List<RaftData> deserializeFromBytes(byte[] serializedBytes) {
        List<RaftData> deserializedList = new ArrayList<>();
        try (ByteArrayInputStream bais = new ByteArrayInputStream(serializedBytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            deserializedList = (List<RaftData>) ois.readObject();
            log.info("List deserialized from byte array successfully.");
        } catch (IOException | ClassNotFoundException e) {
            log.error("Error deserializing list from byte array.", e);
        }
        return deserializedList;
    }
}
