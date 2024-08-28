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

import static com.ericsson.component.aia.model.registry.utils.Constants.SCHEMA_REGISTRY_ADDRESS_PARAMETER;

import java.io.IOException;
import java.util.Properties;

import com.ericsson.component.aia.model.registry.exception.SchemaRegistrationException;
import com.ericsson.component.aia.model.registry.impl.FileBasedSchemaRegistryClient;
import com.ericsson.component.aia.model.registry.impl.RestSchemaRegistryClient;

public class TestUtil {

    public static FileBasedSchemaRegistryClient createFileBasedSchemaRegistryClient(final String directory) throws IOException {
        final Properties properties = new Properties();
        properties.put(SCHEMA_REGISTRY_ADDRESS_PARAMETER, directory);
        return new FileBasedSchemaRegistryClient(properties);
    }

    public static RestSchemaRegistryClientTestHepler createRestSchemaRegistryClient(final String url) throws SchemaRegistrationException {
        final Properties properties = new Properties();
        properties.put(SCHEMA_REGISTRY_ADDRESS_PARAMETER, url);
        return new RestSchemaRegistryClientTestHepler(properties);
    }

}
