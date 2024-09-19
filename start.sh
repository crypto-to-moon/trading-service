#!/bin/bash
echo "nodeid = $nodeId"
hostname -I
java -jar -DnodeId=${nodeId}  node.jar -Daeron.event.cluster.log=all
#--add-opens java.base/java.lang=ALL-UNNAMED --add-exports java.base/sun.reflect.annotation=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED --add-exports java.base/jdk.internal.misc=ALL-UNNAMED