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
package com.ericsson.component.aia.model.registry.client;

import org.apache.avro.Schema;

import com.ericsson.component.aia.model.registry.exception.SchemaRegistrationException;
import com.ericsson.component.aia.model.registry.exception.SchemaRetrievalException;
import com.ericsson.component.aia.model.registry.impl.RegisteredSchema;
import com.ericsson.component.aia.model.registry.impl.SchemaRegistryClientFactory;

/**
 *
 * Common interface for all schema registry clients
 *
 */
public interface SchemaRegistryClient {

    SchemaRegistryClient INSTANCE = SchemaRegistryClientFactory.newSchemaRegistryClientInstance();

    /**
     * Queries schema registry for a schema with the specified {@code schemaId}.
     *
     * @param schemaId
     *            the unique identifier for the schema.
     * @return the schema containing that schema id.
     * @throws SchemaRetrievalException
     *             if no schema with that id exists or if the registry is unavailable.
     */
    Schema lookup(final long schemaId) throws SchemaRetrievalException;

    /**
     * Queries schema registry for the latest version of the schema with the specified {@code subject}. Returns a {@link RegisteredSchema} instance,
     * which contains the avro schema and its associated schemaId.
     *
     * @param subject
     *            the subject for a schema.
     * @return the latest version of the schema registered under that subject wrapped in a registeredSchema object.
     * @throws SchemaRetrievalException
     *             if no schema under that subject exists or if the registry is unavailable.
     */
    RegisteredSchema lookup(final String subject) throws SchemaRetrievalException;

    /**
     * Registers the specified {@code schema} with schema registry under the relevant subject. Returns the schema id for that schema if the operation
     * is successful. If the schema already exists in schema registry, the schema id of the existing schema will be returned, e.g. the schema in the
     * registry will not be replaced by the specified {@code schema}.
     *
     * @param schema
     *            to be registered.
     * @return the schema id of the registered schema
     * @throws SchemaRegistrationException
     *             if the schema cannot be registered or if the registry is unavailable.
     */
    long put(final Schema schema) throws SchemaRegistrationException;
}
