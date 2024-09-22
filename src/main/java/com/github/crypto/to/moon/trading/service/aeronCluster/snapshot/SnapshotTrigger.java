package com.github.crypto.to.moon.trading.service.aeronCluster.snapshot;

import io.aeron.cluster.ClusterTool;
import lombok.extern.slf4j.Slf4j;
import com.github.crypto.to.moon.trading.service.aeronCluster.utils.AeronCommon;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.PrintStream;

@Service
@Slf4j
public class SnapshotTrigger {
    public boolean trigger() {
        try {
            String snapshotPath = AeronCommon.clusterDir.getPath() + "/snapshot.log";
            File file = new File(snapshotPath);
            if (!file.exists()) {
                AeronCommon.clusterDir.mkdirs();
                file.createNewFile();
            }
            return ClusterTool.snapshot(AeronCommon.clusterDir, new PrintStream(snapshotPath));
        } catch (Exception e) {
            log.error("[snapshotTrigger] error",e);
        }
        return false;
    }
}
