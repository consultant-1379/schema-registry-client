/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.model.registry.testutils;

import static com.ericsson.component.aia.model.registry.testutils.TestConstants.SCHEMA_REGISTRY1_PORT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.confluent.kafka.schemaregistry.RestApp;
import io.confluent.kafka.schemaregistry.avro.AvroCompatibilityLevel;
import kafka.server.KafkaConfig$;

/**
 * Runs an in-memory, "embedded" Schema registry cluster with 1 ZooKeeper instance and 1 Kafka broker.
 */
public class SchemaRegistryEmbedded extends ExternalResource {

    private static final Logger log = LoggerFactory.getLogger(SchemaRegistryEmbedded.class);
    private static final int DEFAULT_BROKER_PORT = 0; // 0 results in a random port being selected
    private static final String KAFKA_SCHEMAS_TOPIC = "_schemas";
    private static final String AVRO_COMPATIBILITY_TYPE = AvroCompatibilityLevel.NONE.name;

    private ZooKeeperEmbedded zookeeper;
    private KafkaEmbedded broker;
    private final List<Integer> schemaRegistryPorts;
    private final List<RestApp> schemaRegistryInstances = new ArrayList<>();
    private final Properties brokerConfig;

    /**
     * Default Constructor. Creates a single instance of schema registry
     */
    public SchemaRegistryEmbedded() {
        this(getDefaultSchemaRegistryPorts());
    }

    /**
     * Constructs an instance of schema registry per port specified
     */
    public SchemaRegistryEmbedded(final List<Integer> schemaRegistryPorts) {
        this(new Properties(), schemaRegistryPorts);
    }

    /**
     * Constructor with additional broker config.
     *
     * @param brokerConfig
     *            Additional broker configuration settings.
     */
    public SchemaRegistryEmbedded(final Properties brokerConfig, final List<Integer> schemaRegistryPorts) {
        this.brokerConfig = new Properties();
        this.brokerConfig.putAll(brokerConfig);
        this.schemaRegistryPorts = schemaRegistryPorts;
    }

    /**
     * Starts schema registry, kafka & zookeeper.
     */
    public void start() throws Exception {
        log.debug("Initiating embedded Kafka cluster startup");
        log.debug("Starting a ZooKeeper instance...");
        zookeeper = new ZooKeeperEmbedded();
        log.debug("ZooKeeper instance is running at {}", zookeeper.connectString());

        final Properties effectiveBrokerConfig = effectiveBrokerConfigFrom(brokerConfig, zookeeper);
        log.debug("Starting a Kafka instance on port {} ...", effectiveBrokerConfig.getProperty("port"));
        broker = new KafkaEmbedded(effectiveBrokerConfig);
        log.debug("Kafka instance is running at {}, connected to ZooKeeper at {}", broker.brokerList(), broker.zookeeperConnect());

        for (final Integer schemaRegistryPort : schemaRegistryPorts) {
            final RestApp schemaRegistry = new RestApp(schemaRegistryPort, zookeeperConnect(), KAFKA_SCHEMAS_TOPIC, AVRO_COMPATIBILITY_TYPE, null);
            schemaRegistry.start();
            this.schemaRegistryInstances.add(schemaRegistry);
        }

    }

    private Properties effectiveBrokerConfigFrom(final Properties brokerConfig, final ZooKeeperEmbedded zookeeper) {
        final Properties effectiveConfig = new Properties();
        effectiveConfig.putAll(brokerConfig);
        effectiveConfig.put(KafkaConfig$.MODULE$.ZkConnectProp(), zookeeper.connectString());
        effectiveConfig.put("port", DEFAULT_BROKER_PORT);
        effectiveConfig.put(KafkaConfig$.MODULE$.DeleteTopicEnableProp(), true);
        effectiveConfig.put(KafkaConfig$.MODULE$.LogCleanerDedupeBufferSizeProp(), 2 * 1024 * 1024L);
        return effectiveConfig;
    }

    @Override
    protected void before() throws Exception {
        start();
    }

    @Override
    protected void after() {
        stop();
    }

    /**
     * Stops schema registry, kafka & zookeeper.
     */
    public void stop() {
        try {
            stopAllSchemaRegistryInstances();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        if (broker != null) {
            broker.stop();
        }
        try {
            if (zookeeper != null) {
                zookeeper.stop();
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This cluster's ZK connection string aka `zookeeper.connect` in `hostnameOrIp:port` format. Example: `127.0.0.1:2181`.
     *
     * You can use this to e.g. tell Kafka consumers how to connect to this cluster.
     */
    public String zookeeperConnect() {
        return zookeeper.connectString();
    }

    /**
     * The "schema.registry.url" setting of this schema registry instance.
     */
    public String schemaRegistryUrl() {
        return schemaRegistryInstances.get(0).restConnect;
    }

    /**
     * Create a Kafka topic with 1 partition and a replication factor of 1.
     *
     * @param topic
     *            The name of the topic.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void createTopic(final String topic) throws InterruptedException, ExecutionException {
        createTopic(topic, 1, 1, new Properties());
    }

    /**
     * Create a Kafka topic with the given parameters.
     *
     * @param topic
     *            The name of the topic.
     * @param partitions
     *            The number of partitions for this topic.
     * @param replication
     *            The replication factor for (the partitions of) this topic.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void createTopic(final String topic, final int partitions, final int replication) throws InterruptedException, ExecutionException {
        createTopic(topic, partitions, replication, new Properties());
    }

    /**
     * Create a Kafka topic with the given parameters.
     *
     * @param topic
     *            The name of the topic.
     * @param partitions
     *            The number of partitions for this topic.
     * @param replication
     *            The replication factor for (partitions of) this topic.
     * @param topicConfig
     *            Additional topic-level configuration settings.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void createTopic(final String topic, final int partitions, final int replication, final Properties topicConfig)
            throws InterruptedException, ExecutionException {
        broker.createTopic(topic, partitions, replication, topicConfig);
    }

    private static List<Integer> getDefaultSchemaRegistryPorts() {
        final List<Integer> ports = new ArrayList<>();
        ports.add(SCHEMA_REGISTRY1_PORT);
        return ports;
    }

    public void stopSchemaRegistryInstance() throws Exception {
        if (schemaRegistryInstances != null && !schemaRegistryInstances.isEmpty()) {
            schemaRegistryInstances.get(0).stop();
        }
    }

    public void stopAllSchemaRegistryInstances() throws Exception {
        for (final RestApp schemaRegistry : schemaRegistryInstances) {
            if (schemaRegistry != null) {
                schemaRegistry.stop();
            }
        }
    }

    public void startSchemaRegistryInstance() throws Exception {
        if (schemaRegistryInstances != null && !schemaRegistryInstances.isEmpty()) {
            schemaRegistryInstances.get(0).start();
        }
    }

    public void startAllSchemaRegistryInstances() throws Exception {
        stopAllSchemaRegistryInstances();
        schemaRegistryInstances.clear();
        for (final Integer schemaRegistryPort : schemaRegistryPorts) {
            final RestApp schemaRegistry = new RestApp(schemaRegistryPort, zookeeperConnect(), KAFKA_SCHEMAS_TOPIC, AVRO_COMPATIBILITY_TYPE, null);
            schemaRegistry.start();
            this.schemaRegistryInstances.add(schemaRegistry);
        }
    }

}
