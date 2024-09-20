mvn clean install -DskipTests
docker compose stop
docker rm node0 node1 node2
docker rmi node:2.0
docker build -t node:2.0 .
docker compose up --build