package com.github.crypto.to.moon.trading.service.aeronCluster.utils;

import io.aeron.ChannelUriStringBuilder;
import io.aeron.CommonContext;
import org.agrona.ErrorHandler;

import java.io.File;
import java.util.List;

import static io.aeron.samples.cluster.ClusterConfig.*;

public class AeronCommon {

    public static File baseDir;
    public static String aeronDirName;
    public static File archiveDir;
    public static File clusterDir;
    static int TERM_LENGTH = 1024 * 1024;

    private static final int PORT_BASE = 9000;

    public static final int LOG_CONTROL_PORT_OFFSET = 6;
    public static final int CLIENT_RESPONSE_PORT_OFFSET = 7;

    private static final String CONSENSUS_CHANNAL = "aeron:udp";




    public static ErrorHandler errorHandler(final String context) {
        return
                (Throwable throwable) ->
                {
                    System.err.println(context);
                    throwable.printStackTrace(System.err);
                };
    }

    public static String udpChannel(final int nodeId, final String hostname, final int portOffset) {
        final int port = calculatePort(nodeId, portOffset);
        return new ChannelUriStringBuilder()
                .media("udp")
                .termLength(TERM_LENGTH)
                .endpoint(hostname + ":" + port)
                .build();
    }

    public static String logReplicationChannel(final String hostname) {
        return new ChannelUriStringBuilder()
                .media("udp")
                .endpoint(hostname + ":0")
                .build();
    }

    public static String clusterMembers(List<String> hostnames) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hostnames.size(); i++) {
            String hostname = hostnames.get(i);
            sb.append(i).append(',')
                    .append(hostname).append(':').append(calculatePort(i, CLIENT_FACING_PORT_OFFSET)).append(',')
                    .append(hostname).append(':').append(calculatePort(i, MEMBER_FACING_PORT_OFFSET)).append(',')
                    .append(hostname).append(':').append(calculatePort(i, LOG_PORT_OFFSET)).append(',')
                    .append(hostname).append(':').append(calculatePort(i, TRANSFER_PORT_OFFSET)).append(',')
                    .append(hostname).append(':').append(calculatePort(i, ARCHIVE_CONTROL_PORT_OFFSET))
                    .append('|');
        }
        return sb.toString();
    }


    public static String logControlChannel(final int nodeId, final String hostname, final int portOffset)
    {
        final int port = calculatePort(nodeId, portOffset);
        return new ChannelUriStringBuilder()
                .media("udp")
                .termLength(TERM_LENGTH)
                .controlMode(CommonContext.MDC_CONTROL_MODE_MANUAL)
                .controlEndpoint(hostname + ":" + port)
                .build();
    }
    public static String consensusChannal(int nodeId, String hostname, final int portOffset) {
        final int port = calculatePort(nodeId, portOffset);
        return new ChannelUriStringBuilder(CONSENSUS_CHANNAL)
                .endpoint(hostname + ":" + port)
                .build();
    }
    public static String ingressEndpoints(List<String> hostnames) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hostnames.size(); i++) {
            String hostname = hostnames.get(i);
            sb.append(i).append('=').append(hostname).append(':').append(calculatePort(i, CLIENT_FACING_PORT_OFFSET));
            if (i != hostnames.size() - 1) {
                sb.append(',');
            }
        }
        return sb.toString();
    }

    public static String ingressChannel(int nodeId, String hostname,int offset){
        final int port = calculatePort(nodeId, offset);
        return new ChannelUriStringBuilder("aeron:udp")
                .endpoint(hostname + ":" + port)
                .build();
    }

    public static int calculatePort(final int nodeId, final int offset) {
        return PORT_BASE + (nodeId * 100) + offset;
    }
}
