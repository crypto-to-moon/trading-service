FROM amazoncorretto:17-al2023-jdk

WORKDIR /app
COPY target/Node-1.0-SNAPSHOT.jar node.jar
COPY start.sh start.sh
EXPOSE 8080

RUN chmod a+x start.sh
CMD ["./start.sh"]