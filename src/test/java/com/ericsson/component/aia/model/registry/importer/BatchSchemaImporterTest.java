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
package com.ericsson.component.aia.model.registry.importer;

import static com.ericsson.component.aia.model.registry.testutils.TestConstants.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.junit.Test;

import com.ericsson.component.aia.model.registry.exception.SchemaRegistrationException;
import com.ericsson.component.aia.model.registry.impl.RegisteredSchema;

public class BatchSchemaImporterTest {

    private final ClassLoader classLoader = getClass().getClassLoader();
    private final String directory = new File(classLoader.getResource(SAMPLE_SCHEMA_FILEPATH).getFile()).getParent();

    @Test(expected = IllegalArgumentException.class)
    public void test_BatchSchemaImporter_nullDirectory() {
        new BatchSchemaImporter(null, AVAILABLE_SCHEMA_REGISTRY_INSTANCES_URL_LIST, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_BatchSchemaImporter_nullRegistry() {
        new BatchSchemaImporter(directory, null, false);
    }

    @Test
    public void test_BatchSchemaImporter_checkOnly() throws IOException, SchemaRegistrationException {
        final BatchSchemaImporter importer = new BatchSchemaImporter(directory, AVAILABLE_SCHEMA_REGISTRY_INSTANCES_URL_LIST, true);
        final Set<RegisteredSchema> registeredSchemas = importer.importSchemas();
        assertEquals("The expected amount of schemas weren't loaded from the file system", 3, importer.getSchemasLoadedFromFileSystem().size());
        assertTrue("There shouldn't be any schemas in Schema Registry", registeredSchemas.isEmpty());
    }

}
