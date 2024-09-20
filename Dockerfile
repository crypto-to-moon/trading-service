FROM amazoncorretto:17-al2023-jdk

WORKDIR /app
COPY target/Node-1.0-SNAPSHOT.jar node.jar
EXPOSE 8080

CMD echo "nodeid = $nodeId" && \
    while ! curl "nacos:8848" -m 1; do \
        echo "Waiting for Nacos to start..." && \
        sleep 2; \
    done && \
    java -jar -DnodeId=${nodeId} node.jar -Daeron.event.cluster.log=all \
    --add-opens java.base/java.lang=ALL-UNNAMED \
    --add-exports java.base/sun.reflect.annotation=ALL-UNNAMED \
    --add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
    --add-opens=java.base/java.util=ALL-UNNAMED \
    --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED \
    --add-exports java.base/jdk.internal.misc=ALL-UNNAMED \
    --add-opens java.base/sun.nio.ch=ALL-UNNAMED