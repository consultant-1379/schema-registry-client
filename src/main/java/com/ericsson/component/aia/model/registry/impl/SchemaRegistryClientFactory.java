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

import static com.ericsson.component.aia.model.registry.utils.Utils.*;

import java.util.Properties;

import com.ericsson.component.aia.model.registry.client.SchemaRegistryClient;

/**
 * Factory for creating schema registry clients.
 *
 */
public final class SchemaRegistryClientFactory {

    private SchemaRegistryClientFactory() {
    }

    /**
     * Creates a particular schema registry instance depending on the "schemaRegistry.address" system property value. If the value is a valid rest
     * registry url, i.e. it begins with either "http://" or "https://", an instance of a {@link RestSchemaRegistryClient} will be returned. Otherwise
     * an instance of {@link FileBasedSchemaRegistryClient} will be returned, with the value being used as the directory to load the *.avsc files
     * from.
     *
     * If "schemaRegistry.address" system property is not set, a default value "http://localhost:8081" will be used a {@link RestSchemaRegistryClient}
     * will be returned.
     *
     * @return a SchemaRegistryClient instance.
     */
    public static SchemaRegistryClient newSchemaRegistryClientInstance() {
        final String schemaRegistryUrl = getSchemaRegistryUrlProperty(System.getProperties());
        return isValidRestEndpoint(schemaRegistryUrl) ? new RestSchemaRegistryClient() : new FileBasedSchemaRegistryClient();
    }

    /**
     * Creates a particular schema registry instance depending on the value of "schemaRegistry.address" specified in the {@code properties} argument.
     * If the value is a valid rest registry url, i.e. it begins with either "http://" or "https://", an instance of a
     * {@link RestSchemaRegistryClient} will be returned. Otherwise an instance of {@link FileBasedSchemaRegistryClient} will be returned, with the
     * value being used as the directory to load the *.avsc files from.
     *
     * If "schemaRegistry.address" property is not set in the specified {@code properties} , a default value "http://localhost:8081" will be used a
     * {@link RestSchemaRegistryClient} will be returned.
     *
     * @param properties
     *            the properties to confifure the client with.
     *
     * @return a SchemaRegistryClient passing in the specified {@code properties} as an argument to their constructor.
     */
    public static SchemaRegistryClient newSchemaRegistryClientInstance(final Properties properties) {
        checkArgumentIsNotNull("properties", properties);
        final String schemaRegistryUrl = getSchemaRegistryUrlProperty(properties);
        return isValidRestEndpoint(schemaRegistryUrl) ? new RestSchemaRegistryClient(properties) : new FileBasedSchemaRegistryClient(properties);
    }

}
