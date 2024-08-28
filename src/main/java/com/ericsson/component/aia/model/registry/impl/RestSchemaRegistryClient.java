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

import static com.ericsson.component.aia.model.registry.utils.Constants.DEFAULT_REST_CLIENT_CACHE_SIZE;
import static com.ericsson.component.aia.model.registry.utils.Constants.INVALID_REST_ENDPOINT_MESSAGE;
import static com.ericsson.component.aia.model.registry.utils.Constants.REST_SCHEMA_REGISTRY_CLIENT_CACHE_MAX_SIZE_PARAMETER;
import static com.ericsson.component.aia.model.registry.utils.Constants.SCHEMA_REGISTRY_ADDRESS_PARAMETER;
import static com.ericsson.component.aia.model.registry.utils.Constants.SCHEMA_REGISTRY_CACHE_MAX_SIZE_PARAMETER;
import static com.ericsson.component.aia.model.registry.utils.Constants.SCHEMA_RETRIEVAL_MESSAGE;
import static com.ericsson.component.aia.model.registry.utils.Utils.checkArgumentIsNotNull;
import static com.ericsson.component.aia.model.registry.utils.Utils.getSchemaRegistrationExceptionMessage;
import static com.ericsson.component.aia.model.registry.utils.Utils.getSchemaRegistryCacheSizeProperty;
import static com.ericsson.component.aia.model.registry.utils.Utils.getSchemaRegistryUrlProperty;
import static com.ericsson.component.aia.model.registry.utils.Utils.getSchemaRetrievalExceptionMessage;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.rest.RestService;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.model.registry.client.SchemaRegistryClient;
import com.ericsson.component.aia.model.registry.exception.SchemaRegistrationException;
import com.ericsson.component.aia.model.registry.exception.SchemaRetrievalException;
import com.ericsson.component.aia.model.registry.utils.Utils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Schema registry client for interacting with Confluent schema registry. Uses a wrapped version of Confluent's schema registry client. Accepts a list
 * of schema registry instances as long as they are separated by commas.
 */
public class RestSchemaRegistryClient implements SchemaRegistryClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestSchemaRegistryClient.class);
    private static final String EVENT_ID_FIELD = "_ID";
    private static final int URL_NOT_FOUND = 404;
    protected Cache<String, RegisteredSchema> registeredSchemaCache;
    private final CachedSchemaRegistryClient client;
    private final RestService restService;

    /**
     * Default Constructor. Constructs an instance of RestSchemaRegistryClient that connects to a schema registry running on the url specified by the
     * "schemaRegistry.address" system property. If property is not set, a default value of "http://localhost:8081" will be used. Optional property:
     * "schemaRegistry.cacheMaximumSize".
     */
    public RestSchemaRegistryClient() {
        this(System.getProperties());
    }

    /**
     * Constructs an instance of RestSchemaRegistryClient that connects to a schema registry running on the specified {@code properties}.
     *
     * @param properties
     *            used to instantiate client. Should contain properties "schemaRegistry.address" and (optional) "schemaRegistry.cacheMaximumSize".
     */
    public RestSchemaRegistryClient(final Properties properties) {
        this(getSchemaRegistryUrlProperty(properties), getSchemaRegistryCacheSizeProperty(properties));
    }

    /**
     * Constructs an instance of RestSchemaRegistryClient to connect to schema registry client running on the specified cluster
     * {@code registryUrlString}. The cache size of the client for caching queries can be specified using {@code cacheSize}.
     *
     * @param registryUrlString
     *            can be a single url for a schema registry instance or a comma separated list of a schema registry cluster.
     * @param cacheSize
     *            the size of the registry client cache
     */
    public RestSchemaRegistryClient(final String registryUrlString, final int cacheSize) {
        checkArgumentIsNotNull("registryUrlString", registryUrlString);
        isValidRestEndpoint(registryUrlString);
        restService = new RestService(registryUrlString);
        LOGGER.info("Starting RestSchemaRegistryClient with following properties [{}={}, {}={}]", new Object[] { SCHEMA_REGISTRY_ADDRESS_PARAMETER,
            registryUrlString, SCHEMA_REGISTRY_CACHE_MAX_SIZE_PARAMETER, String.valueOf(cacheSize) });
        client = new CachedSchemaRegistryClient(restService, cacheSize);
        registeredSchemaCache =
                CacheBuilder.newBuilder().maximumSize(getRestSchemaRegistryClientCacheSize()).build();
    }

    @Override
    public Schema lookup(final long schemaId) throws SchemaRetrievalException {
        try {
            return client.getByID((int) schemaId);
        } catch (final IOException | RestClientException e) {
            if (e instanceof IOException || URL_NOT_FOUND == ((RestClientException) e).getErrorCode()) {
                throw new SchemaRetrievalException("Schema registry url not found " + restService.getBaseUrls(), e);
            } else {
                throw new SchemaRetrievalException(getSchemaRetrievalExceptionMessage(schemaId), e);
            }
        }
    }

    @Override
    public long put(final Schema schema) throws SchemaRegistrationException {
        checkArgumentIsNotNull("schema", schema);
        try {
            return client.register(schema.getFullName(), schema);
        } catch (IOException | RestClientException e) {
            throw new SchemaRegistrationException(getSchemaRegistrationExceptionMessage(schema), e);
        }
    }

    /**
     * Returns a set of all {@link RegisteredSchema} stored in schema registry. Additional functionality the Confluent
     * {@link CachedSchemaRegistryClient} lacks.
     *
     * @return set of registered schemas stored in registry.
     * @throws SchemaRetrievalException
     *             if the schemas cannot be retrieved from schema registry.
     */
    public synchronized Set<RegisteredSchema> getAllSchemas() throws SchemaRetrievalException {
        try {
            final List<String> subjects = restService.getAllSubjects();
            final Set<RegisteredSchema> schemas = new HashSet<>();
            for (final String subject : subjects) {
                final RegisteredSchema registeredSchema = lookup(subject);
                schemas.add(registeredSchema);
            }
            return schemas;
        } catch (IOException | RestClientException e) {
            throw new SchemaRetrievalException(SCHEMA_RETRIEVAL_MESSAGE, e);
        }
    }

    /**
     * Returns a set of all {@link RegisteredSchema} stored in schema registry which have an event_id matching an event_is in the specified
     * {@code whitelistedEventIds}. Additional functionality the Confluent {@link CachedSchemaRegistryClient} lacks.
     *
     * @param whitelistedEventIds
     *            list of eventIds to filter schemas on.
     * @return set of all registered schemas in schema registry with a specified event id.
     * @throws SchemaRetrievalException
     *             if the schemas cannot be retrieved from schema registry.
     */
    public synchronized Set<RegisteredSchema> getSchemasByEventIds(final List<Integer> whitelistedEventIds) throws SchemaRetrievalException {
        checkArgumentIsNotNull("whitelistedEventIds", whitelistedEventIds);
        try {
            final List<String> subjects = restService.getAllSubjects();
            final Set<RegisteredSchema> schemas = new HashSet<>();
            for (final String subject : subjects) {
                final RegisteredSchema registeredSchema = lookup(subject);
                final Field eventIdField = registeredSchema.getSchema().getField(EVENT_ID_FIELD);
                if (eventIdField != null && whitelistedEventIds.contains(eventIdField.defaultVal())) {
                    schemas.add(registeredSchema);
                }
            }
            return schemas;
        } catch (IOException | RestClientException e) {
            throw new SchemaRetrievalException(SCHEMA_RETRIEVAL_MESSAGE, e);
        }
    }

    private static void isValidRestEndpoint(final String registryUrlString) {
        if (!Utils.isValidRestEndpoint(registryUrlString)) {
            throw new IllegalArgumentException(INVALID_REST_ENDPOINT_MESSAGE);
        }
    }

    @Override
    public RegisteredSchema lookup(final String subject) throws SchemaRetrievalException {
        checkArgumentIsNotNull("subject", subject);
        RegisteredSchema registeredSchema = registeredSchemaCache.getIfPresent(subject);
        if (null == registeredSchema) {
            registeredSchema = lookupSchemaRegistry(subject);
            registeredSchemaCache.put(subject, registeredSchema);
        }
        return registeredSchema;
    }

    /**
     * Lookup schema registry.
     *
     * @param subject
     *            the subject to search for
     * @return the searched RegisteredSchema
     * @throws SchemaRetrievalException
     *             in case of problems
     */
    protected RegisteredSchema lookupSchemaRegistry(final String subject) throws SchemaRetrievalException {
        try {
            LOGGER.info("Loading Schemas from Registry with subject::{}", subject);
            final SchemaMetadata confluentSchemaMetadata = client.getLatestSchemaMetadata(subject);
            final Schema avroSchema = new Schema.Parser().parse(confluentSchemaMetadata.getSchema());
            return new RegisteredSchema(confluentSchemaMetadata.getId(), avroSchema);
        } catch (IOException | RestClientException e) {
            throw new SchemaRetrievalException(SCHEMA_RETRIEVAL_MESSAGE, e);
        }
    }

    private int getRestSchemaRegistryClientCacheSize() {
        return Integer.parseInt(System.getProperty(REST_SCHEMA_REGISTRY_CLIENT_CACHE_MAX_SIZE_PARAMETER, DEFAULT_REST_CLIENT_CACHE_SIZE));
    }

    protected Cache<String, RegisteredSchema> getCache() {
        return registeredSchemaCache;
    }

    protected void setCache(final Cache<String, RegisteredSchema> cache) {
        registeredSchemaCache = cache;
    }

}
