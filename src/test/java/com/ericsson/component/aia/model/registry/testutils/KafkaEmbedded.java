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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.network.ListenerName;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.utils.Time;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kafka.server.KafkaConfig;
import kafka.server.KafkaConfig$;
import kafka.server.KafkaServer;
import kafka.utils.TestUtils;

/**
 * Runs an in-memory, "embedded" instance of a Kafka broker, which listens at `127.0.0.1:9092` by default.
 *
 * Requires a running ZooKeeper instance to connect to. By default, it expects a ZooKeeper instance running at `127.0.0.1:2181`. You can specify a
 * different ZooKeeper instance by setting the `zookeeper.connect` parameter in the broker's configuration.
 */
public class KafkaEmbedded {

    private static final Logger log = LoggerFactory.getLogger(KafkaEmbedded.class);

    private static final String DEFAULT_ZK_CONNECT = "127.0.0.1:2181";

    private final Properties effectiveConfig;
    private final File logDir;
    private final TemporaryFolder tmpFolder;
    private final KafkaServer kafka;

    /**
     * Creates and starts an embedded Kafka broker.
     *
     * @param config
     *            Broker configuration settings. Used to modify, for example, on which port the broker should listen to. Note that you cannot change
     *            some settings such as `log.dirs`, `port`.
     */
    public KafkaEmbedded(final Properties config) throws IOException {
        tmpFolder = new TemporaryFolder();
        tmpFolder.create();
        logDir = tmpFolder.newFolder();
        effectiveConfig = effectiveConfigFrom(config);
        final boolean loggingEnabled = true;

        final KafkaConfig kafkaConfig = new KafkaConfig(effectiveConfig, loggingEnabled);
        log.debug("Starting embedded Kafka broker (with log.dirs={} and ZK ensemble at {}) ...", logDir, zookeeperConnect());
        kafka = TestUtils.createServer(kafkaConfig, Time.SYSTEM);
        log.debug("Startup of embedded Kafka broker at {} completed (with ZK ensemble at {}) ...", brokerList(), zookeeperConnect());
    }

    private Properties effectiveConfigFrom(final Properties initialConfig) throws IOException {
        final Properties effectiveConfig = new Properties();
        effectiveConfig.put(KafkaConfig$.MODULE$.BrokerIdProp(), 0);
        effectiveConfig.put(KafkaConfig$.MODULE$.ListenersProp(), "PLAINTEXT://127.0.0.1:9092");
        effectiveConfig.put(KafkaConfig$.MODULE$.NumPartitionsProp(), 1);
        effectiveConfig.put(KafkaConfig$.MODULE$.AutoCreateTopicsEnableProp(), true);
        effectiveConfig.put(KafkaConfig$.MODULE$.MessageMaxBytesProp(), 1000000);
        effectiveConfig.put(KafkaConfig$.MODULE$.ControlledShutdownEnableProp(), true);
        effectiveConfig.putAll(initialConfig);
        effectiveConfig.setProperty(KafkaConfig$.MODULE$.LogDirProp(), logDir.getAbsolutePath());
        return effectiveConfig;
    }

    /**
     * This broker's `metadata.broker.list` value. Example: `127.0.0.1:9092`.
     *
     * You can use this to tell Kafka producers and consumers how to connect to this instance.
     */
    public String brokerList() {
        return "127.0.0.1" + ":" + kafka.boundPort(ListenerName.forSecurityProtocol(SecurityProtocol.PLAINTEXT));
    }

    /**
     * The ZooKeeper connection string aka `zookeeper.connect`.
     */
    public String zookeeperConnect() {
        return effectiveConfig.getProperty("zookeeper.connect", DEFAULT_ZK_CONNECT);
    }

    /**
     * Stop the broker.
     */
    public void stop() {
        log.debug("Shutting down embedded Kafka broker at {} (with ZK ensemble at {}) ...", brokerList(), zookeeperConnect());
        kafka.shutdown();
        kafka.awaitShutdown();
        log.debug("Removing temp folder {} with logs.dir at {} ...", tmpFolder, logDir);
        tmpFolder.delete();
        log.debug("Shutdown of embedded Kafka broker at {} completed (with ZK ensemble at {}) ...", brokerList(), zookeeperConnect());
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
        log.debug("Creating topic { name: {}}", topic);
        final AdminClient adminClient = AdminClient.create(topicConfig);
        final NewTopic newTopic = new NewTopic(topic, partitions, (short) replication);
        adminClient.createTopics(Collections.singleton(newTopic)).all().get();
    }

}
