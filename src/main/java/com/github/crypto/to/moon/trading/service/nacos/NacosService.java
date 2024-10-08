package com.github.crypto.to.moon.trading.service.nacos;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static java.lang.Integer.parseInt;

@Service
@Slf4j
public class NacosService {


    public List<Instance> getAllInstance() {
        List<Instance> allInstances = new ArrayList<>();
        try {
            Properties properties = new Properties();
            properties.setProperty("serverAddr", "nacos:8848");
            properties.setProperty("namespace", "public");
            properties.setProperty("username", "nacos");
            properties.setProperty("password", "nacos");
            NamingService nacosNamingService = NamingFactory.createNamingService(properties);
            allInstances = nacosNamingService.getAllInstances("node");
        } catch (NacosException e) {
            log.error("NacosException:", e);
        }
        return allInstances;
    }

    public Instance getSelf() {
        final int nodeId = parseInt(System.getenv("nodeId"));
        for (Instance instance : this.getAllInstance()) {
            int id = parseInt(instance.getMetadata().get("nodeId"));
            if (id == nodeId) {
                return instance;
            }
        }
        return null;
    }

    public void update(long size)  {
        final int nodeId = parseInt(System.getenv("nodeId"));
        for (Instance instance : this.getAllInstance()) {
            int id = parseInt(instance.getMetadata().get("nodeId"));
            if (id == nodeId) {
                Map<String, String> metadata = instance.getMetadata();
                metadata.put("raftDataSize", String.valueOf(size));
                NamingService namingService = this.getNamingService();
                try {
                    namingService.registerInstance("node", instance);
                } catch (NacosException e) {
                    log.error("update error, ", e);
                }
            }
        }
    }

    public void updateRole(boolean isLeader)  {
        final int nodeId = parseInt(System.getProperty("nodeId", "1024"));
        for (Instance instance : this.getAllInstance()) {
            int id = parseInt(instance.getMetadata().get("nodeId"));
            if (id == nodeId) {
                Map<String, String> metadata = instance.getMetadata();
                metadata.put("isLeader", String.valueOf(isLeader));
                NamingService namingService = this.getNamingService();
                try {
                    namingService.registerInstance("node", instance);
                } catch (NacosException e) {
                    log.error("update error", e);
                }
            }
        }
    }
    private NamingService getNamingService() {
        Properties properties = new Properties();
        properties.setProperty("serverAddr", "nacos:8848");
        properties.setProperty("namespace", "public");
        properties.setProperty("username", "nacos");
        properties.setProperty("password", "nacos");
        NamingService nacos = null;
        try {
            nacos = NamingFactory.createNamingService(properties);
        } catch (NacosException e) {
            log.error("Cannot get Naming service", e);
        }
        return nacos;
    }


    @Bean
    public NacosDiscoveryProperties nacosDiscoveryProperties() {
        NacosDiscoveryProperties nacosDiscoveryProperties = new NacosDiscoveryProperties();
        nacosDiscoveryProperties.setServerAddr("nacos:8848");
        nacosDiscoveryProperties.setUsername("nacos");
        nacosDiscoveryProperties.setPassword("nacos");
        Map<String, String> metadata = nacosDiscoveryProperties.getMetadata();
        metadata.put("nodeId", System.getProperty("nodeId", "0"));
        metadata.put("isLeader", "false");
        return nacosDiscoveryProperties;
    }

}