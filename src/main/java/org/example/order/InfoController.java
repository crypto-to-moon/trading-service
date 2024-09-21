package org.example.order;

import org.example.aeronCluster.ClusterClient;
import org.example.aeronCluster.ClusterService;
import org.example.aeronCluster.raftlog.RaftDataEncoderAndDecoder;
import org.example.aeronCluster.snapshot.SnapshotTrigger;
import org.example.nacos.NacosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

import static java.lang.Integer.parseInt;

@Controller
@ResponseBody
public class InfoController {
    @Autowired
    NacosService nacosService;

    @Autowired
    ClientService clientService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    SnapshotTrigger snapshotTrigger;

    @GetMapping("/")
    public String index() {
        final int nodeId = parseInt(System.getProperty("nodeId", "1024"));
        System.out.println("NodeId:" + nodeId);
        String info;

        if (!clientService.isInit()) {
            info = "此节点是 follower 节点。<br /> /nodeData 获取节点数据。";
        } else {
            info = "此节点是 leader 节点。<br /> /nodeData 获取节点数据。<br />/put 向aeron cluster 发送数据。例如：.../put?key=123&value=123String<br />";
        }
        info += "/takeSnapshot 开始集群打快照";
        return "this is node:" + nodeId + ", " + info;
    }

    @GetMapping("/instance")
    public String instance() {
        return String.valueOf(nacosService.getSelf());
    }

    @GetMapping("/nodeData")
    public String nodeData() {
        List<RaftDataEncoderAndDecoder.RaftData> clusterData = clusterService.getClusterData();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < clusterData.size(); i++) {
            RaftDataEncoderAndDecoder.RaftData raftData = clusterData.get(i);
            sb.append("[").append(i).append("] ").append(raftData).append("<br />");
        }
        return "节点数据:<br />" + sb;
    }

    @GetMapping("/takeSnapshot")
    public String TakeSnapshot() {
        String snapshotResult;
        if (snapshotTrigger.trigger()) {
            snapshotResult = "Success";
        } else {
            snapshotResult = "Fail";
        }
        return snapshotResult;
    }
}
