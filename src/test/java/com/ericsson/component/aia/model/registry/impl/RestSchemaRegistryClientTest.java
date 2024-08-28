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
package com.ericsson.component.aia.model.registry.impl;

import static com.ericsson.component.aia.model.registry.testutils.TestConstants.SAMPLE_SCHEMA_FILEPATH;
import static com.ericsson.component.aia.model.registry.testutils.TestConstants.UNAVAILABLE_SCHEMA_REGISTRY_URL;
import static com.ericsson.component.aia.model.registry.testutils.TestUtil.createRestSchemaRegistryClient;
import static com.ericsson.component.aia.model.registry.utils.Constants.SCHEMA_REGISTRY_ADDRESS_PARAMETER;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.avro.Schema;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.component.aia.model.registry.exception.SchemaRegistrationException;
import com.ericsson.component.aia.model.registry.exception.SchemaRetrievalException;

public class RestSchemaRegistryClientTest {

    private static final String INVALID_REGISTRY_URL = "htpp://";
    private final ClassLoader classLoader = getClass().getClassLoader();
    private final File schemaFile = new File(classLoader.getResource(SAMPLE_SCHEMA_FILEPATH).getFile());
    private static RestSchemaRegistryClient client;

    @Before
    public void before() throws SchemaRegistrationException {
        client = createRestSchemaRegistryClient(UNAVAILABLE_SCHEMA_REGISTRY_URL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_RestSchemaRegistryClient_nullRegistry() {
        new RestSchemaRegistryClient(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_RestSchemaRegistryClient_invalidRegistryEndpoint() {
        final Properties properties = new Properties();
        properties.put(SCHEMA_REGISTRY_ADDRESS_PARAMETER, INVALID_REGISTRY_URL);
        new RestSchemaRegistryClient(properties);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_registerSchema_nullSchema() throws SchemaRegistrationException, IOException {
        client.put(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_getSchemasByEventIds_nullEventIdList() throws SchemaRetrievalException {
        client.getSchemasByEventIds(null);
    }

    @Test(expected = SchemaRegistrationException.class)
    public void test_registerSchema_whenSchemaRegistryIsUnavailable() throws SchemaRegistrationException, IOException {
        final Schema schema = new Schema.Parser().parse(schemaFile);
        client.put(schema);
    }

    @Test(expected = SchemaRetrievalException.class)
    public void test_getSchemaById_whenSchemaRegistryIsUnavailable() throws SchemaRetrievalException {
        client.lookup(0L);
    }

    @Test(expected = SchemaRetrievalException.class)
    public void test_getAllSchemas_whenSchemaRegistryIsUnavailable() throws SchemaRetrievalException {
        client.getAllSchemas();
    }

    @Test(expected = SchemaRetrievalException.class)
    public void test_getSchemasByEventIds_whenSchemaRegistryIsUnavailable() throws SchemaRetrievalException {
        client.getSchemasByEventIds(new ArrayList<Integer>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_getSchemaBySubject_nullSubject() throws SchemaRetrievalException {
        client.lookup(null);
    }

    @Test
    public void test_schemaLookUp_withInvalidRegistry_URL_IOException() {
        final Properties properties = new Properties();
        properties.put(SCHEMA_REGISTRY_ADDRESS_PARAMETER, "http://unavailableTestServer:9999/");
        final RestSchemaRegistryClient restSchemaRegistryClient = new RestSchemaRegistryClient(properties);
        try {
            restSchemaRegistryClient.lookup(9138);
        } catch (final SchemaRetrievalException e) {
            assertTrue(e.getCause() instanceof IOException);
        }
    }
}
