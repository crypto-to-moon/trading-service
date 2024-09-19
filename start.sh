#!/bin/bash
echo "nodeid = $nodeId"

# 等待 Nacos 服务健康
while ! curl "nacos:8848" -m 1; do
  echo "Waiting for Nacos to start..."
  sleep 2
done

#curl http://nacos:8848/nacos
java -jar -DnodeId=${nodeId} node.jar -Daeron.event.cluster.log=all --add-opens java.base/java.lang=ALL-UNNAMED --add-exports java.base/sun.reflect.annotation=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED --add-exports java.base/jdk.internal.misc=ALL-UNNAMED