package org.example.aeronCluster;

import com.alibaba.nacos.api.naming.pojo.Instance;
import io.aeron.archive.Archive;
import io.aeron.archive.ArchiveThreadingMode;
import io.aeron.archive.client.AeronArchive;
import io.aeron.cluster.ClusteredMediaDriver;
import io.aeron.cluster.ConsensusModule;
import io.aeron.cluster.service.ClusteredServiceContainer;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.MinMulticastFlowControlSupplier;
import io.aeron.driver.ThreadingMode;
import lombok.extern.slf4j.Slf4j;
import org.agrona.concurrent.NoOpLock;
import org.agrona.concurrent.ShutdownSignalBarrier;
import org.example.aeronCluster.utils.AeronCommon;
import org.example.nacos.NacosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.aeron.samples.cluster.ClusterConfig.*;
import static java.lang.Integer.parseInt;
import static org.example.aeronCluster.utils.AeronCommon.LOG_CONTROL_PORT_OFFSET;
import static org.example.aeronCluster.utils.AeronCommon.udpChannel;

@Slf4j
@Component
public class Node {
    @Autowired
    ClusterService clusterService;

    @Autowired
    NacosService nacosService;

    public void init() {
        final int nodeId = parseInt(System.getenv("nodeId"));
        List<Instance> allInstance;
        while (true) {
            allInstance = nacosService.getAllInstance();
            if (allInstance.size() == 3) {
                break;
            }
            log.info("等待 Node 启动: size:{}", allInstance.size());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

//        String[] hostnames = Lists.newArrayList("localhost").toArray(new String[]{});
        String hostname = System.getenv("HOST_NAME");

        final File baseDir = new File(System.getProperty("user.dir"), "node" + nodeId);
        final String aeronDirName = baseDir + "/aeronDirName";

        final File archiveDir = new File(baseDir, "archive");

        final File clusterDir = new File(baseDir, "cluster");
        AeronCommon.clusterDir = clusterDir;
        log.info("[config] hostname:{}", hostname);
        log.info("[config] baseDir:{}", baseDir);
        log.info("[config] aeronDirName:{}", aeronDirName);
        log.info("[config] archiveDir:{}", archiveDir.getAbsolutePath());
        log.info("[config] clusterDir:{}", clusterDir.getAbsolutePath());

        final ShutdownSignalBarrier barrier = new ShutdownSignalBarrier();
        // end::main[]

        // tag::media_driver[]
        final MediaDriver.Context mediaDriverContext = new MediaDriver.Context()
                .aeronDirectoryName(aeronDirName)
                .threadingMode(ThreadingMode.SHARED)
                .termBufferSparseFile(true)
                .multicastFlowControlSupplier(new MinMulticastFlowControlSupplier())
                .terminationHook(barrier::signal).errorHandler(AeronCommon.errorHandler("Media Driver"));
        // end::media_driver[]

        // todo 这里为什么有两个archive client contest
        final AeronArchive.Context replicationArchiveContext = new AeronArchive.Context().controlResponseChannel("aeron:udp?endpoint=" + hostname + ":0");

        // tag::archive[]
        final Archive.Context archiveContext = new Archive.Context().aeronDirectoryName(aeronDirName).segmentFileLength(1024 * 1024).archiveDir(archiveDir).controlChannel(udpChannel(nodeId, hostname, ARCHIVE_CONTROL_PORT_OFFSET)).localControlChannel("aeron:ipc?term-length=64k").archiveClientContext(replicationArchiveContext).replicationChannel(AeronCommon.logReplicationChannel(hostname)).recordingEventsEnabled(false).threadingMode(ArchiveThreadingMode.SHARED);
        // end::archive[]
//
        // tag::archive_client[]
        final AeronArchive.Context aeronArchiveContext = new AeronArchive.Context().messageTimeoutNs(TimeUnit.SECONDS.toNanos(50)).lock(NoOpLock.INSTANCE).controlRequestChannel(archiveContext.localControlChannel()).controlResponseChannel(archiveContext.localControlChannel()).aeronDirectoryName(aeronDirName);
        // end::archive_client[]
//
        String clusterMembers = AeronCommon.clusterMembers(List.of("node0", "node1", "node2"));
        log.info("[config]clusterMembers: {}", clusterMembers);
        // tag::consensus_module[]
        final ConsensusModule.Context consensusModuleContext = new ConsensusModule.Context()
                .errorHandler(AeronCommon.errorHandler("Consensus Module"))
                .clusterMemberId(nodeId)
                .clusterMembers(clusterMembers)
                .clusterDir(clusterDir)
                .replicationChannel(AeronCommon.logReplicationChannel(hostname))
                .logChannel(AeronCommon.logControlChannel(nodeId, hostname, LOG_CONTROL_PORT_OFFSET))
                .consensusChannel(AeronCommon.consensusChannal(nodeId, hostname, MEMBER_FACING_PORT_OFFSET))
                .ingressChannel(AeronCommon.ingressChannel(nodeId, hostname, CLIENT_FACING_PORT_OFFSET))
                .archiveContext(aeronArchiveContext.clone())
                .sessionTimeoutNs(TimeUnit.SECONDS.toNanos(30))
                .isIpcIngressAllowed(true);
        // end::consensus_module[]

        List<ClusteredServiceContainer.Context> serviceContexts = new ArrayList<>();

        // tag::clustered_service[]
        final ClusteredServiceContainer.Context clusteredServiceContext = new ClusteredServiceContainer.Context().aeronDirectoryName(aeronDirName)
                .archiveContext(aeronArchiveContext.clone())
                .clusterDir(clusterDir).clusteredService(clusterService).errorHandler(AeronCommon.errorHandler("Clustered ServiceImp"));


        serviceContexts.add(clusteredServiceContext);

        ClusteredMediaDriver.launch(mediaDriverContext, archiveContext, consensusModuleContext);

        ClusteredServiceContainer.launch(clusteredServiceContext);

        // end::running[]
        log.info("[{}] Started Cluster Node on " + hostname + "...", nodeId);
    }

}