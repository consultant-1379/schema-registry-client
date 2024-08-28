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

import static org.junit.Assert.assertEquals;

import static com.ericsson.component.aia.model.registry.testutils.TestConstants.*;
import static com.ericsson.component.aia.model.registry.testutils.TestUtil.createRestSchemaRegistryClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.*;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import com.ericsson.component.aia.model.base.config.bean.SchemaEnum;
import com.ericsson.component.aia.model.registry.exception.UploadTimeoutException;
import com.ericsson.component.aia.model.registry.impl.RestSchemaRegistryClient;
import com.ericsson.component.aia.model.registry.importer.BatchXmlImporter;
import com.ericsson.component.aia.model.registry.testutils.SchemaRegistryEmbedded;

public class BatchXmlImporterIntegrationTest {

    private static final int NUMBER_OF_SCHEMAS_IN_DIRECTORY = 840;
    private static final SchemaRegistryEmbedded registry = new SchemaRegistryEmbedded(getSchemaRegistryPorts());
    private final String xmlDirectory = getFile(BATCH_XML_SCHEMA_FILEPATH).getParent();

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Before
    public void setUp() throws Exception {
        registry.start();
    }

    @After
    public void tearDown() throws Exception {
        registry.stop();
    }

    @Test
    public void whenImportingXmls_shouldCompleteSucessfully() throws Exception {
        // Given schema registry is available

        // When I import an xml using BatchXmlImporter
        new BatchXmlImporter(SchemaEnum.CELLTRACE, xmlDirectory, TEMP_AVRO_DIRECTORY, AVAILABLE_SCHEMA_REGISTRY_INSTANCES_URL_LIST).importXmls();
        // Then all the schemas in that directory should be loaded into the registry
        final RestSchemaRegistryClient client = createRestSchemaRegistryClient(AVAILABLE_SCHEMA_REGISTRY_INSTANCES_URL_LIST);
        final long numberOfSchemasImported = client.getAllSchemas().size();
        assertEquals("Correct number of schemas were not retrieved", NUMBER_OF_SCHEMAS_IN_DIRECTORY, numberOfSchemasImported);
    }

    @Test(expected = UploadTimeoutException.class)
    public void whenUploadingSchemasGenerateFromXmlsTakesLongerThanTimeout_shouldInterruptByTimeout() throws Exception {
        // Given schema registry is available

        // When I run BatchXmlImporter's main method with a timeout insufficient for the operation to conclude;
        new BatchXmlImporter(SchemaEnum.CELLTRACE, xmlDirectory, TEMP_AVRO_DIRECTORY, AVAILABLE_SCHEMA_REGISTRY_INSTANCES_URL_LIST, "0").importXmls();

        // Then an UploadTimeoutException is thrown
    }

    @Test
    public void whenImportingXmlsThroughMainMethodWith4Parameters_shouldCompleteSucessfully() throws Exception {
        // Given schema registry is available

        // and I am expecting a sytem exit
        exit.expectSystemExit();

        // When I import an xml using BatchXmlImporter
        BatchXmlImporter
                .main(new String[] { SchemaEnum.CELLTRACE.value(), xmlDirectory, TEMP_AVRO_DIRECTORY, AVAILABLE_SCHEMA_REGISTRY_INSTANCES_URL_LIST });

        // Then should exit with status 0
    }

    @Test(expected = UploadTimeoutException.class)
    public void whenImportingXmlsThroughMainMethodWith5Parameters_andSettingShortTimeOut_shouldInterruptByTimeout() throws Exception {
        // Given schema registry is available

        // and a very short timeout
        final String timeout = "0";

        // When I import an xml using BatchXmlImporter
        BatchXmlImporter.main(new String[] { SchemaEnum.CELLTRACE.value(), xmlDirectory, TEMP_AVRO_DIRECTORY,
            AVAILABLE_SCHEMA_REGISTRY_INSTANCES_URL_LIST, timeout });

        // Then should exit with status 0
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
