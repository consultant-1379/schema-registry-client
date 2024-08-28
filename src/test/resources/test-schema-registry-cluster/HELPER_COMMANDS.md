

docker stop zookeeper kafka
docker rm zookeeper kafka
docker run -d --name zookeeper jplock/zookeeper
docker run -d --name kafka --link zookeeper:zookeeper --env KAFKA_ADVERTISED_HOST_NAME=172.17.0.4 ches/kafka

docker logs zookeeper
docker logs kafka
docker inspect kafka




docker stop schema-registry-master-dc1-node1 schema-registry-master-dc1-node2 schema-registry-master-dc1-node3 schema-registry-slave-dc2-node1 schema-registry-slave-dc2-node2 schema-registry-slave-dc2-node3
docker rm schema-registry-master-dc1-node1 schema-registry-master-dc1-node2 schema-registry-master-dc1-node3 schema-registry-slave-dc2-node1 schema-registry-slave-dc2-node2 schema-registry-slave-dc2-node3

docker run -d --name schema-registry-master-dc1-node1 --link zookeeper:zookeeper --link kafka:kafka --env SCHEMA_REGISTRY_MASTER_ELIGIBILITY=true --env SCHEMA_REGISTRY_KAFKASTORE_TOPIC_REPLICATION_FACTOR=1 --env SCHEMA_REGISTRY_DEBUG=true confluent/schema-registry
docker run -d --name schema-registry-master-dc1-node2 --link schema-registry-master-dc1-node1:schema-registry-master-dc1-node1 --link zookeeper:zookeeper --link kafka:kafka --env SCHEMA_REGISTRY_MASTER_ELIGIBILITY=true --env SCHEMA_REGISTRY_KAFKASTORE_TOPIC_REPLICATION_FACTOR=1 --env SCHEMA_REGISTRY_DEBUG=true confluent/schema-registry

docker run -d --name schema-registry-master-dc1-node1 -p 10011:8081 --link zookeeper:zookeeper --link kafka:kafka --env SCHEMA_REGISTRY_MASTER_ELIGIBILITY=true --env SCHEMA_REGISTRY_KAFKASTORE_TOPIC_REPLICATION_FACTOR=1 --env SCHEMA_REGISTRY_DEBUG=true confluent/schema-registry
docker run -d --name schema-registry-master-dc1-node2 -p 10012:8081 --link zookeeper:zookeeper --link kafka:kafka --env SCHEMA_REGISTRY_MASTER_ELIGIBILITY=true --env SCHEMA_REGISTRY_KAFKASTORE_TOPIC_REPLICATION_FACTOR=1 --env SCHEMA_REGISTRY_DEBUG=true confluent/schema-registry

docker run -d --name schema-registry-master-dc1-node2 -p 10012:8081 --link zookeeper:zookeeper --link kafka:kafka --env SCHEMA_REGISTRY_MASTER_ELIGIBILITY=true confluent/schema-registry
docker run -d --name schema-registry-master-dc1-node3 -p 10013:8081 --link zookeeper:zookeeper --link kafka:kafka --env SCHEMA_REGISTRY_MASTER_ELIGIBILITY=true confluent/schema-registry
docker run -d --name schema-registry-slave-dc2-node1 -p 10021:8081 --link zookeeper:zookeeper --link kafka:kafka --env SCHEMA_REGISTRY_MASTER_ELIGIBILITY=false confluent/schema-registry
docker run -d --name schema-registry-slave-dc2-node2 -p 10022:8081 --link zookeeper:zookeeper --link kafka:kafka --env SCHEMA_REGISTRY_MASTER_ELIGIBILITY=false confluent/schema-registry
docker run -d --name schema-registry-slave-dc2-node3 -p 10023:8081 --link zookeeper:zookeeper --link kafka:kafka --env SCHEMA_REGISTRY_MASTER_ELIGIBILITY=false confluent/schema-registry




docker logs schema-registry-master-dc1-node1
docker logs schema-registry-master-dc1-node2



docker inspect schema-registry-master-dc1-node1
docker inspect schema-registry-master-dc1-node2


./import-from-10011/run.sh
./import-from-10012/run.sh


curl -v http://172.17.0.5:8081/subjects/

curl -v http://192.168.99.100:10012/subjects/



docker exec -it schema-registry-master-dc1-node2 bash
