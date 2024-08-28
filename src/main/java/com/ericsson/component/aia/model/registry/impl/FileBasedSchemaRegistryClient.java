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

import static com.ericsson.component.aia.model.registry.utils.AvroSchemaUtils.getSchemaId;
import static com.ericsson.component.aia.model.registry.utils.Constants.*;
import static com.ericsson.component.aia.model.registry.utils.Utils.*;

import java.io.IOException;
import java.util.*;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.model.registry.client.SchemaRegistryClient;
import com.ericsson.component.aia.model.registry.exception.SchemaRetrievalException;
import com.ericsson.component.aia.model.registry.utils.AvroSchemaUtils;

/**
 * File based schema registry client which instantiates an in-memory schema registry using the directory specified. Basically, creates a cache from
 * the *.avsc files found in the specified directory and treats cache as schema registry.
 *
 */
public class FileBasedSchemaRegistryClient implements SchemaRegistryClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedSchemaRegistryClient.class);
    private final Map<Long, Schema> schemaCache;

    /**
     * Default constructor. Constructs a FileBasedSchemaRegistryClient using the value of "schemaRegistry.address" system property as the directory to
     * look for files to populate schema registry. If property is not set, a default value "/tmp/" will be used.
     */
    public FileBasedSchemaRegistryClient() {
        this(System.getProperty(SCHEMA_REGISTRY_ADDRESS_PARAMETER, DEFAULT_SCHEMA_REGISTRY_DIRECTORY));
    }

    /**
     * Constructs an instance of FileBasedSchemaRegistryClient using the value of "schemaRegistry.address" property listed in the specified
     * {@code properties} as the directory to look for files to populate schema registry.
     *
     * @param properties
     *            used to instantiate client. Should contain property "schemaRegistry.address".
     */
    public FileBasedSchemaRegistryClient(final Properties properties) {
        this(getSchemaRegistryUrlProperty(properties));
    }

    /**
     * Constructs a FileBasedSchemaRegistryClient using the specified {@code schemaDirectory} as the directory to look for files to populate the
     * schema registry.
     *
     * @param schemaDirectory
     *            directory to load *.avsc files from.
     */
    public FileBasedSchemaRegistryClient(final String schemaDirectory) {
        checkArgumentIsNotNull("schemaDirectory", schemaDirectory);
        LOGGER.info("Starting FileBasedSchemaRegistryClient with following properties [{}={}]",
                new Object[] { SCHEMA_REGISTRY_ADDRESS_PARAMETER, schemaDirectory });
        this.schemaCache = createSchemaCache(schemaDirectory);
    }

    @Override
    public Schema lookup(final long schemaId) throws SchemaRetrievalException {
        if (!schemaCache.containsKey(schemaId)) {
            throw new SchemaRetrievalException(getSchemaRetrievalExceptionMessage(schemaId));
        }
        return schemaCache.get(schemaId);
    }

    /**
     * For testing purposes
     *
     * @return copy of schema registry cache.
     */
    Map<Long, Schema> getSchemaCache() {
        return new HashMap<>(schemaCache);
    }

    @Override
    public long put(final Schema schema) {
        checkArgumentIsNotNull("schema", schema);
        final long schemaId = getSchemaId(schema.getFullName());
        if (!schemaCache.containsKey(schemaId)) {
            schemaCache.put(schemaId, schema);
        }
        return schemaId;
    }

    @Override
    public RegisteredSchema lookup(final String subject) throws SchemaRetrievalException {
        checkArgumentIsNotNull("subject", subject);
        final long schemaId = getSchemaId(subject);
        if (!schemaCache.containsKey(schemaId)) {
            throw new SchemaRetrievalException("No schema exists with subject name: " + subject);
        }
        return new RegisteredSchema(schemaId, schemaCache.get(schemaId));
    }

    private Map<Long, Schema> createSchemaCache(final String schemaDirectory) {
        final Map<Long, Schema> schemaCache = new HashMap<>();
        try {
            schemaCache.putAll(AvroSchemaUtils.createSchemaCache(schemaDirectory));
        } catch (final IOException e) {
            LOGGER.error("Failed to populate Schema cache. Some schemas may be missing from cache.", e);
        }
        return schemaCache;
    }
}
