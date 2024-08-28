

docker stop zookeeper kafka  schema-registry-master-dc1-node1 schema-registry-master-dc1-node2 schema-registry-master-dc1-node3 schema-registry-slave-dc2-node1 schema-registry-slave-dc2-node2 schema-registry-slave-dc2-node3
docker rm zookeeper kafka  schema-registry-master-dc1-node1 schema-registry-master-dc1-node2 schema-registry-master-dc1-node3 schema-registry-slave-dc2-node1 schema-registry-slave-dc2-node2 schema-registry-slave-dc2-node3
docker run -d -p 2181:2181 --name zookeeper jplock/zookeeper
docker run -d -p 9092:9092 --name kafka --link zookeeper:zookeeper --env KAFKA_ADVERTISED_HOST_NAME=192.168.99.100 ches/kafka

docker run -d --name schema-registry-master-dc1-node1 -p 10011:8081 --link zookeeper:zookeeper --link kafka:kafka --env SCHEMA_REGISTRY_MASTER_ELIGIBILITY=true confluent/schema-registry
docker run -d --name schema-registry-master-dc1-node2 -p 10012:8081 --link schema-registry-master-dc1-node1:schema-registry-master-dc1-node1 --link zookeeper:zookeeper --link kafka:kafka --env SCHEMA_REGISTRY_MASTER_ELIGIBILITY=true confluent/schema-registry
docker run -d --name schema-registry-master-dc1-node3 -p 10013:8081 --link schema-registry-master-dc1-node1:schema-registry-master-dc1-node1 --link zookeeper:zookeeper --link kafka:kafka --env SCHEMA_REGISTRY_MASTER_ELIGIBILITY=true confluent/schema-registry
docker run -d --name schema-registry-slave-dc2-node1 -p 10021:8081 --link schema-registry-master-dc1-node1:schema-registry-master-dc1-node1 --link zookeeper:zookeeper --link kafka:kafka --env SCHEMA_REGISTRY_MASTER_ELIGIBILITY=false confluent/schema-registry
docker run -d --name schema-registry-slave-dc2-node2 -p 10022:8081 --link schema-registry-master-dc1-node1:schema-registry-master-dc1-node1 --link zookeeper:zookeeper --link kafka:kafka --env SCHEMA_REGISTRY_MASTER_ELIGIBILITY=false confluent/schema-registry
docker run -d --name schema-registry-slave-dc2-node3 -p 10023:8081 --link schema-registry-master-dc1-node1:schema-registry-master-dc1-node1 --link zookeeper:zookeeper --link kafka:kafka --env SCHEMA_REGISTRY_MASTER_ELIGIBILITY=false confluent/schema-registry



./import-from-10011/run.sh
./import-from-10012/run.sh
./import-from-10013/run.sh
./import-from-10021/run.sh
./import-from-10022/run.sh
./import-from-10023/run.sh

curl -v http://192.168.99.100:10011/subjects/
curl -v http://192.168.99.100:10012/subjects/
curl -v http://192.168.99.100:10013/subjects/
curl -v http://192.168.99.100:10021/subjects/
curl -v http://192.168.99.100:10022/subjects/
curl -v http://192.168.99.100:10023/subjects/

