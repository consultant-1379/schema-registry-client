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

import static com.ericsson.component.aia.model.registry.testutils.TestConstants.*;
import static com.ericsson.component.aia.model.registry.testutils.TestUtil.createFileBasedSchemaRegistryClient;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.avro.Schema;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.component.aia.model.registry.exception.SchemaRegistrationException;
import com.ericsson.component.aia.model.registry.exception.SchemaRetrievalException;

public class FileBasedSchemaRegistryClientTest {

    private static final long INVALID_SCHEMA_ID = 10L;
    private static final String INVALID_SUBJECT = "someFakeSubject";
    private static final String INVALID_DIRECTORY = "someFakeDirectory";
    private static final long SAMPLE_SCHEMA_ID = -7450468758052677028L;
    private static final long FOURTH_LEVEL_SCHEMA_ID = -4783201673143535768L;

    private final ClassLoader classLoader = getClass().getClassLoader();
    private final File sampleSchemaFile = new File(classLoader.getResource(SAMPLE_SCHEMA_FILEPATH).getFile());
    private final File fourthLevelSchemaFile = new File(classLoader.getResource(FOURTH_LEVEL_SCHEMA_FILEPATH).getFile());
    private final File newSchemaFile = new File(classLoader.getResource(BATCH_IMPORTER_SCHEMA_FILEPATH).getFile());
    private static FileBasedSchemaRegistryClient client;
    private final String nullString = null;
    private final Properties nullProperties = null;

    @Before
    public void before() throws IOException {
        client = createFileBasedSchemaRegistryClient(sampleSchemaFile.getParent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_FileBasedSchemaRegistryClient_nullDirectorySpecified() throws IOException {
        new FileBasedSchemaRegistryClient(nullString);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_FileBasedSchemaRegistryClient_nullPropertiesSpecified() throws IOException {
        new FileBasedSchemaRegistryClient(nullProperties);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_getSchemaBySubject_nullSubject() throws SchemaRetrievalException {
        client.lookup(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_registerSchema_nullSchema() throws SchemaRegistrationException, IOException {
        client.put(null);
    }

    @Test
    public void test_FileBasedSchemaRegistryClient_invalidDirectorySpecified() throws IOException {
        //When a non-existent directory is specified when creating a FileBasedSchemaRegistryClient
        final FileBasedSchemaRegistryClient emptyClient = new FileBasedSchemaRegistryClient(INVALID_DIRECTORY);

        //Then the client should be created with an empty schema cache
        assertTrue("If a FileBasedSchemaRegistryClient is instantiated with an invalid directory, an empty Map should be returned.",
                emptyClient.getSchemaCache().isEmpty());
    }

    @Test
    public void test_FileBasedSchemaRegistryClient_validDirectorySpecified() throws IOException {
        //Given a valid directory containing avro schemas is specified when creating a FileBasedSchemaRegistryClient
        //Then all the avro schemas in the specified directory will be loaded into FileBasedSchemaRegistryClients schema cache.
        final Map<Long, Schema> cache = client.getSchemaCache();
        assertEquals("Incorrect number of schemas were loaded", 3, cache.size());
        final Schema sampleSchema = cache.get(SAMPLE_SCHEMA_ID);
        assertEquals(SAMPLE_SCHEMA1_SUBJECT, sampleSchema.getFullName());
        assertEquals(new Schema.Parser().parse(sampleSchemaFile), sampleSchema);

        final Schema fourthLevelSchema = cache.get(FOURTH_LEVEL_SCHEMA_ID);
        assertEquals(FOURTH_LEVEL_SCHEMA_SUBJECT, fourthLevelSchema.getFullName());
        assertEquals(new Schema.Parser().parse(fourthLevelSchemaFile), fourthLevelSchema);
    }

    @Test
    public void test_getSchemaById_validSchemaId() throws SchemaRetrievalException, IOException {
        //Given a FileBasedSchemaRegistryClient with a populated schema cache
        //When attempting to look up a schema using a valid schema id
        final Schema sampleSchema = client.lookup(SAMPLE_SCHEMA_ID);
        final Schema fourthLevelSchema = client.lookup(FOURTH_LEVEL_SCHEMA_ID);

        //Then the correct schemas will be returned
        assertEquals(SAMPLE_SCHEMA1_SUBJECT, sampleSchema.getFullName());
        assertEquals(new Schema.Parser().parse(sampleSchemaFile), sampleSchema);
        assertEquals(FOURTH_LEVEL_SCHEMA_SUBJECT, fourthLevelSchema.getFullName());
        assertEquals(new Schema.Parser().parse(fourthLevelSchemaFile), fourthLevelSchema);
    }

    @Test(expected = SchemaRetrievalException.class)
    public void test_getSchemaById_invalidSchemaId() throws SchemaRetrievalException {
        //Given a FileBasedSchemaRegistryClient with a populated schema cache
        //When attempting to look up a schema using an invalid schema id
        //Then a SchemaRetrievalException will be thrown
        client.lookup(INVALID_SCHEMA_ID);
    }

    @Test
    public void test_registerSchema_newSchema() throws SchemaRetrievalException, SchemaRegistrationException, IOException {
        //Given a FileBasedSchemaRegistryClient with a populated schema cache
        //When attempting to register a new schema
        final Schema newSchema = new Schema.Parser().parse(newSchemaFile);
        assertEquals(3, client.getSchemaCache().size());
        client.put(newSchema);

        //Then the new schema will be registered
        assertEquals("New schema hasn't been added to cache", 4, client.getSchemaCache().size());
    }

    @Test
    public void test_registerSchema_duplicateSchema() throws SchemaRetrievalException, SchemaRegistrationException {
        //Given a FileBasedSchemaRegistryClient with a populated schema cache
        //When attempting to register the same schema twice
        final Schema sampleSchema = client.lookup(SAMPLE_SCHEMA_ID);
        final long newSchemaId = client.put(sampleSchema);

        //Then the schema id of the existing registered schema will be returned and the new schema will not be re-registered
        assertEquals("Schema ids match", SAMPLE_SCHEMA_ID, newSchemaId);
        assertEquals("Schema are identical", sampleSchema, client.lookup(newSchemaId));
    }

    @Test
    public void test_getSchemaBySubject_validSubject() throws SchemaRetrievalException {
        //Given a FileBasedSchemaRegistryClient with a populated schema cache
        //When attempting to look up a schema using a valid subject
        final Schema sampleSchema = client.lookup(SAMPLE_SCHEMA_ID);
        final RegisteredSchema retrievedSchema = client.lookup(sampleSchema.getFullName());

        //Then the latest version of the schema registered under that subject will be returned.
        assertEquals(sampleSchema, retrievedSchema.getSchema());
        assertEquals(SAMPLE_SCHEMA_ID, retrievedSchema.getSchemaId());
    }

    @Test(expected = SchemaRetrievalException.class)
    public void test_getSchemaBySubject_invalidSubject() throws SchemaRetrievalException {
        //Given a FileBasedSchemaRegistryClient with a populated schema cache
        //When attempting to look up a schema using an invalid subject
        //Then a SchemaRetrievalException will be thrown
        client.lookup(INVALID_SUBJECT);
    }
}
