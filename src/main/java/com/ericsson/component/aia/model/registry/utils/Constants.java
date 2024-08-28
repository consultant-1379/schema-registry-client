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
package com.ericsson.component.aia.model.registry.utils;

/**
 * Holds constants used throughout application for reuse.
 *
 */
public interface Constants {

    String DEFAULT_CACHE_SIZE = "500000";
    String DEFAULT_REST_CLIENT_CACHE_SIZE = "100000";
    String HTTP = "http://";
    String HTTPS = "https://";
    String SCHEMA_REGISTRY_ADDRESS_PARAMETER = "schemaRegistry.address";
    String SCHEMA_REGISTRY_CACHE_MAX_SIZE_PARAMETER = "schemaRegistry.cacheMaximumSize";
    String REST_SCHEMA_REGISTRY_CLIENT_CACHE_MAX_SIZE_PARAMETER = "restSchemaRegistryClient.cacheMaxiumSize";
    String DEFAULT_SCHEMA_REGISTRY_ADDRESS = "http://localhost:8081";
    String DEFAULT_SCHEMA_REGISTRY_DIRECTORY = "/tmp/";
    String EMPTY_SPACE = "";
    String PERIOD = ".";
    String COMMA = ",";
    String AVRO_FILE_EXTENSION = ".avsc";
    String INVALID_REST_ENDPOINT_MESSAGE = "registry endpoint must be start with http:// or https://";
    String SCHEMA_RETRIEVAL_MESSAGE = "Failed to retrieve schema";
}
