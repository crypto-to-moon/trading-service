FROM azul/zulu-openjdk:17.0.12-jdk

WORKDIR /app
CMD mvn clean install
COPY target/Node-1.0-SNAPSHOT.jar node.jar
COPY start.sh start.sh
EXPOSE 8080

RUN chmod a+x start.sh
CMD ["./start.sh"]