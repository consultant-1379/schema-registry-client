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

import static com.ericsson.component.aia.model.registry.utils.Constants.SCHEMA_REGISTRY_ADDRESS_PARAMETER;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.ericsson.component.aia.model.registry.client.SchemaRegistryClient;

public class SchemaRegistryClientFactoryTest {

    private static final String VALID_HTTP_ENDPOINT = "http://localhost:8081";
    private static final String VALID_HTTPS_ENDPOINT = "https://localhost:8081";
    private static final String VALID_DIRECTORY = "src/main/resources";
    private static final String INVALID_DIRECTORY = "someFakeDirectory";

    @Test(expected = IllegalArgumentException.class)
    public void test_SchemaRegistryClientFactory_nullArgument() {
        SchemaRegistryClientFactory.newSchemaRegistryClientInstance(null);
    }

    @Test
    public void test_SchemaRegistryClientFactory_noProperties() throws IOException {
        final SchemaRegistryClient client = SchemaRegistryClientFactory.newSchemaRegistryClientInstance();
        assertTrue(client instanceof RestSchemaRegistryClient);
    }

    @Test
    public void test_SchemaRegistryClientFactory_validHTTPEndpointSpecified() throws IOException {
        System.setProperty(SCHEMA_REGISTRY_ADDRESS_PARAMETER, VALID_HTTP_ENDPOINT);

        final SchemaRegistryClient defaultClient = SchemaRegistryClientFactory.newSchemaRegistryClientInstance();
        assertTrue(defaultClient instanceof RestSchemaRegistryClient);

        final SchemaRegistryClient configuredClient = SchemaRegistryClientFactory.newSchemaRegistryClientInstance(System.getProperties());
        assertTrue(configuredClient instanceof RestSchemaRegistryClient);
    }

    @Test
    public void test_SchemaRegistryClientFactory_validHTTPSEndpointSpecified() throws IOException {
        System.setProperty(SCHEMA_REGISTRY_ADDRESS_PARAMETER, VALID_HTTPS_ENDPOINT);

        final SchemaRegistryClient defaultClient = SchemaRegistryClientFactory.newSchemaRegistryClientInstance();
        assertTrue(defaultClient instanceof RestSchemaRegistryClient);

        final SchemaRegistryClient configuredClient = SchemaRegistryClientFactory.newSchemaRegistryClientInstance(System.getProperties());
        assertTrue(configuredClient instanceof RestSchemaRegistryClient);
    }

    @Test
    public void test_SchemaRegistryClientFactory_validDirectorySpecified() throws IOException {
        System.setProperty(SCHEMA_REGISTRY_ADDRESS_PARAMETER, VALID_DIRECTORY);

        final SchemaRegistryClient defaultClient = SchemaRegistryClientFactory.newSchemaRegistryClientInstance();
        assertTrue(defaultClient instanceof FileBasedSchemaRegistryClient);

        final SchemaRegistryClient configuredClient = SchemaRegistryClientFactory.newSchemaRegistryClientInstance(System.getProperties());
        assertTrue(configuredClient instanceof FileBasedSchemaRegistryClient);
    }

    @Test
    public void test_SchemaRegistryClientFactory_invalidDirectorySpecified_emptyCacheReturned() throws IOException {
        System.setProperty(SCHEMA_REGISTRY_ADDRESS_PARAMETER, INVALID_DIRECTORY);

        final SchemaRegistryClient defaultClient = SchemaRegistryClientFactory.newSchemaRegistryClientInstance();
        assertTrue(defaultClient instanceof FileBasedSchemaRegistryClient);
        assertTrue(((FileBasedSchemaRegistryClient) defaultClient).getSchemaCache().isEmpty());

        final SchemaRegistryClient configuredClient = SchemaRegistryClientFactory.newSchemaRegistryClientInstance(System.getProperties());

        assertTrue(configuredClient instanceof FileBasedSchemaRegistryClient);
        assertTrue(((FileBasedSchemaRegistryClient) configuredClient).getSchemaCache().isEmpty());

    }

}
