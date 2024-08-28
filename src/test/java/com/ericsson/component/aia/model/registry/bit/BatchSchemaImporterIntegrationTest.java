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

package com.ericsson.component.aia.model.registry.bit;

import static java.util.concurrent.TimeUnit.MINUTES;

import static org.junit.Assert.assertEquals;

import static com.ericsson.component.aia.model.registry.testutils.TestConstants.*;
import static com.ericsson.component.aia.model.registry.testutils.TestUtil.createRestSchemaRegistryClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.component.aia.model.registry.exception.UploadTimeoutException;
import com.ericsson.component.aia.model.registry.impl.RestSchemaRegistryClient;
import com.ericsson.component.aia.model.registry.importer.BatchSchemaImporter;
import com.ericsson.component.aia.model.registry.testutils.SchemaRegistryEmbedded;

public class BatchSchemaImporterIntegrationTest {

    private static final int NUMBER_OF_SCHEMAS_IN_DIRECTORY = 20;
    private static final SchemaRegistryEmbedded registry = new SchemaRegistryEmbedded(getSchemaRegistryPorts());
    private final String avroSchemasDirectory = getFile(BATCH_IMPORTER_SCHEMA_FILEPATH).getParentFile().getParent();

    @Before
    public void setUp() throws Exception {
        registry.start();
    }

    @After
    public void tearDown() throws Exception {
        registry.stop();
    }

    @Test
    public void whenImportingSchemas_shouldCompleteSucessfully() throws Exception {
        // Given schema registry is available

        // When I run BatchSchemaImporter's main method with the correct arguments;
        BatchSchemaImporter.main(new String[] { "-dir=" + avroSchemasDirectory, "-registry=" + AVAILABLE_SCHEMA_REGISTRY_INSTANCES_URL_LIST,
            "-timeout=" + MINUTES.toSeconds(30) });

        // Then all the schemas in that directory should be loaded into the registry
        final RestSchemaRegistryClient client = createRestSchemaRegistryClient(AVAILABLE_SCHEMA_REGISTRY_INSTANCES_URL_LIST);
        final long numberOfSchemasImported = client.getAllSchemas().size();
        assertEquals("Correct number of schemas were not retrieved", NUMBER_OF_SCHEMAS_IN_DIRECTORY, numberOfSchemasImported);
    }

    @Test(expected = UploadTimeoutException.class)
    public void whenImportingSchemasTakesLongerThanTimeout_shouldInterruptByTimeout() throws Exception {
        // Given schema registry is available

        // When I run BatchSchemaImporter's main method with a timeout insufficient for the operation to conclude;
        BatchSchemaImporter.main(new String[] { "-dir=" + avroSchemasDirectory, "-registry=" + AVAILABLE_SCHEMA_REGISTRY_INSTANCES_URL_LIST,
            "-timeout=" + 0 });

        // Then an UploadTimeoutException is thrown
    }

    private static List<Integer> getSchemaRegistryPorts() {
        final List<Integer> ports = new ArrayList<>();
        ports.add(SCHEMA_REGISTRY1_PORT);
        ports.add(SCHEMA_REGISTRY2_PORT);
        return ports;
    }

    private File getFile(final String filePath) {
        final ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(filePath).getFile());
    }

}
