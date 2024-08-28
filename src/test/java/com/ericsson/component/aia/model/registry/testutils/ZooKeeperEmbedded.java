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

import java.io.IOException;

import org.apache.curator.test.TestingServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs an in-memory, "embedded" instance of a ZooKeeper server.
 *
 * The ZooKeeper server instance is automatically started when you create a new instance of this class.
 */
public class ZooKeeperEmbedded {

    private static final Logger log = LoggerFactory.getLogger(ZooKeeperEmbedded.class);

    private final TestingServer server;

    /**
     * Creates and starts a ZooKeeper instance.
     *
     * @throws Exception
     */
    public ZooKeeperEmbedded() throws Exception {
        log.debug("Starting embedded ZooKeeper server...");
        this.server = new TestingServer();
        log.debug("Embedded ZooKeeper server at {} uses the temp directory at {}", server.getConnectString(), server.getTempDirectory());
    }

    public void stop() throws IOException {
        log.debug("Shutting down embedded ZooKeeper server at {} ...", server.getConnectString());
        server.close();
        log.debug("Shutdown of embedded ZooKeeper server at {} completed", server.getConnectString());
    }

    /**
     * The ZooKeeper connection string aka `zookeeper.connect` in `hostnameOrIp:port` format. Example: `127.0.0.1:2181`.
     *
     * You can use this to e.g. tell Kafka brokers how to connect to this instance.
     */
    public String connectString() {
        return server.getConnectString();
    }

    /**
     * The hostname of the ZooKeeper instance. Example: `127.0.0.1`
     */
    public String hostname() {
        return connectString().substring(0, connectString().lastIndexOf(':'));
    }

}
