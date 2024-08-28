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

import static com.ericsson.component.aia.model.registry.testutils.TestConstants.AVAILABLE_SCHEMA_REGISTRY_INSTANCES_URL_LIST;
import static com.ericsson.component.aia.model.registry.testutils.TestConstants.BATCH_IMPORTER_SCHEMA_FILEPATH;
import static com.ericsson.component.aia.model.registry.testutils.TestConstants.FOURTH_LEVEL_SCHEMA_FILEPATH;
import static com.ericsson.component.aia.model.registry.testutils.TestConstants.SAMPLE_SCHEMA_FILEPATH;
import static com.ericsson.component.aia.model.registry.testutils.TestConstants.SAMPLE_SCHEMA_TWO_FILEPATH;
import static com.ericsson.component.aia.model.registry.testutils.TestConstants.SCHEMA_REGISTRY1_PORT;
import static com.ericsson.component.aia.model.registry.testutils.TestConstants.SCHEMA_REGISTRY2_PORT;
import static com.ericsson.component.aia.model.registry.testutils.TestUtil.createRestSchemaRegistryClient;
import static com.ericsson.component.aia.model.registry.utils.Constants.SCHEMA_REGISTRY_ADDRESS_PARAMETER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.avro.Schema;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.component.aia.model.registry.exception.SchemaRegistrationException;
import com.ericsson.component.aia.model.registry.exception.SchemaRetrievalException;
import com.ericsson.component.aia.model.registry.impl.RegisteredSchema;
import com.ericsson.component.aia.model.registry.impl.RestSchemaRegistryClient;
import com.ericsson.component.aia.model.registry.importer.BatchSchemaImporter;
import com.ericsson.component.aia.model.registry.testutils.RestSchemaRegistryClientTestHepler;
import com.ericsson.component.aia.model.registry.testutils.SchemaRegistryEmbedded;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;

public class RestSchemaRegistryClientIntegrationTest {

    private static final String INVALID_SUBJECT = "someUnknownSubject";
    private static final SchemaRegistryEmbedded registry = new SchemaRegistryEmbedded(getSchemaRegistryPorts());
    private final File sampleSchema1File = getFile(SAMPLE_SCHEMA_FILEPATH);
    private final File sampleSchema2File = getFile(SAMPLE_SCHEMA_TWO_FILEPATH);
    private final File fourthLevelSchemaFile = getFile(FOURTH_LEVEL_SCHEMA_FILEPATH);
    private final String avroSchemasDirectory = getFile(BATCH_IMPORTER_SCHEMA_FILEPATH).getParentFile().getParent();
    private static final int FIRST_SCHEMA_ID_GENERATED_BY_FIRST_LEADER = 1;
    private static final int SECOND_SCHEMA_ID_GENERATED_BY_FIRST_LEADER = 2;
    private static final int FIRST_SCHEMA_ID_GENERATED_BY_SECOND_LEADER = 21;

    private RestSchemaRegistryClientTestHepler client;

    @Before
    public void before() throws Exception {
        registry.start();
        client = createRestSchemaRegistryClient(AVAILABLE_SCHEMA_REGISTRY_INSTANCES_URL_LIST);
    }

    @After
    public void tearDown() throws Exception {
        registry.stop();
    }

    @Test(expected = SchemaRetrievalException.class)
    public void test_getSchemaById_beforeRegisteringSchema() throws SchemaRetrievalException {
        //Given schema registry is available
        //When querying schema registry with a schema_id that isn't registered with schema in schema registry
        //Then a SchemaRetrievalException should be thrown.
        client.lookup(FIRST_SCHEMA_ID_GENERATED_BY_FIRST_LEADER);
    }

    @Test
    public void test_getSchemaById_afterRegisteringSchema() throws Exception {
        //Given schema registry is available
        //When querying schema registry with a schema_id that is registered with schema in schema registry
        final Schema schema = new Schema.Parser().parse(sampleSchema1File);
        final long schemaId = client.put(schema);
        assertEquals(FIRST_SCHEMA_ID_GENERATED_BY_FIRST_LEADER, schemaId);

        //Then the schema associated with that schema_id should be returned.
        final Schema retrievedSchema = client.lookup(schemaId);
        assertEquals(schema, retrievedSchema);
    }

    @Test
    public void test_registerSchema_differentSchemaUnderDifferentSubjects()
            throws IOException, SchemaRegistrationException, SchemaRetrievalException {
        //Given schema registry is available
        //When registering more than one schema
        final Schema schema1 = new Schema.Parser().parse(sampleSchema1File);
        final Schema schema2 = new Schema.Parser().parse(fourthLevelSchemaFile);
        final long schemaId1 = client.put(schema1);
        final long schemaId2 = client.put(schema2);

        //Then unique schema ids are returned for each registration, provided the schemas are unique and haven't been registered before
        assertEquals(FIRST_SCHEMA_ID_GENERATED_BY_FIRST_LEADER, schemaId1);
        assertEquals(SECOND_SCHEMA_ID_GENERATED_BY_FIRST_LEADER, schemaId2);
        assertEquals(schema1, client.lookup(schemaId1));
        assertEquals(schema2, client.lookup(schemaId2));
    }

    @Test
    public void test_registerSchema_duplicateSchemaRegistration() throws IOException, SchemaRegistrationException, SchemaRetrievalException {
        //Given schema registry is available
        //When registering a duplicate schema under the same subject as an existing schema
        final Schema schema = new Schema.Parser().parse(sampleSchema1File);
        final long firstSchemaId = client.put(schema);
        final long secondSchemaId = client.put(schema);

        //Then the registration request will be ignored and a copy of the original schemaId will be returned
        assertEquals(FIRST_SCHEMA_ID_GENERATED_BY_FIRST_LEADER, firstSchemaId);
        assertEquals(firstSchemaId, secondSchemaId);
        assertEquals(schema, client.lookup(firstSchemaId));
    }

    @Test
    public void test_schemaRegistryShouldBeAvailableToQueryWhenOneInstanceGoesDown() throws Exception {
        //Given schema registry is available
        //And schemas have been registered successfully
        final Schema schema1 = new Schema.Parser().parse(sampleSchema1File);
        final Schema schema2 = new Schema.Parser().parse(fourthLevelSchemaFile);
        final long schemaId1 = client.put(schema1);
        final long schemaId2 = client.put(schema2);

        //If one instance of schema registry in a schema registry cluster goes down
        registry.stopSchemaRegistryInstance();

        //Then schema registry should still return me the correct data when queried
        assertEquals(FIRST_SCHEMA_ID_GENERATED_BY_FIRST_LEADER, schemaId1);
        assertEquals(SECOND_SCHEMA_ID_GENERATED_BY_FIRST_LEADER, schemaId2);
        assertEquals(schema1, client.lookup(schemaId1));
        assertEquals(schema2, client.lookup(schemaId2));
    }

    @Test
    public void test_schemaRegistryShouldReturnCorrectSchemasAfterRestartOfAllInstances() throws Exception {
        //Given schema registry is available
        //And schemas have been registered successfully
        final Schema schema1 = new Schema.Parser().parse(sampleSchema1File);
        final Schema schema2 = new Schema.Parser().parse(fourthLevelSchemaFile);
        final long schemaId1 = client.put(schema1);
        final long schemaId2 = client.put(schema2);

        //If all instances of schema registry goes down
        registry.stopAllSchemaRegistryInstances();

        //And brought back online
        registry.startAllSchemaRegistryInstances();

        //Then schema registry should still return me the correct data when I query it
        assertEquals(FIRST_SCHEMA_ID_GENERATED_BY_FIRST_LEADER, schemaId1);
        assertEquals(SECOND_SCHEMA_ID_GENERATED_BY_FIRST_LEADER, schemaId2);
        assertEquals(schema1, client.lookup(schemaId1));
        assertEquals(schema2, client.lookup(schemaId2));
    }

    @Test
    public void test_schemaRegistryShouldBeAvailableToRegisterSchemasWhenOneInstanceGoesDown() throws Exception {
        //Given schema registry is available
        //And schemas have been registered successfully
        final Schema schema1 = new Schema.Parser().parse(sampleSchema1File);
        final long schemaId1 = client.put(schema1);
        assertEquals(FIRST_SCHEMA_ID_GENERATED_BY_FIRST_LEADER, schemaId1);
        assertEquals(schema1, client.lookup(schemaId1));

        //If the leader goes down
        registry.stopSchemaRegistryInstance();
        Thread.sleep(3000); //Zookeeper takes a few secs to realize an instance has gone down then elect a new leader.

        //And I try to register a schema
        final Schema schema2 = new Schema.Parser().parse(fourthLevelSchemaFile);
        final long schemaId2 = client.put(schema2);

        //Then schema registry should still register that schema,
        //but it won't necessarily have the same id that would have been generated by the previous leader
        assertEquals(FIRST_SCHEMA_ID_GENERATED_BY_SECOND_LEADER, schemaId2);
        assertEquals(schema2, client.lookup(schemaId2));
    }

    @Test
    public void test_schemaRegistryClientCachesQueryResults() throws Exception {
        //Given schema registry is available
        //And schemas have been registered successfully
        final Schema schema1 = new Schema.Parser().parse(sampleSchema1File);
        final Schema schema2 = new Schema.Parser().parse(fourthLevelSchemaFile);
        final long schemaId1 = client.put(schema1);
        final long schemaId2 = client.put(schema2);

        //And at some point while schema registry is up we query some schemas
        assertEquals(schema1, client.lookup(schemaId1));
        assertEquals(schema2, client.lookup(schemaId2));

        //If after that, all instances of schema registry go down
        registry.stopAllSchemaRegistryInstances();

        //Then schema registry client should return those same schemas if queried again, because they're cached in memory
        assertEquals(FIRST_SCHEMA_ID_GENERATED_BY_FIRST_LEADER, schemaId1);
        assertEquals(SECOND_SCHEMA_ID_GENERATED_BY_FIRST_LEADER, schemaId2);
    }

    @Test
    public void test_schemasArePersistedWhenAllSchemaRegistryInstancesAndClientsAreRestarted() throws Exception {
        //Given schema registry is available
        //And schemas have been registered successfully
        final Schema schema1 = new Schema.Parser().parse(sampleSchema1File);
        final Schema schema2 = new Schema.Parser().parse(fourthLevelSchemaFile);
        final long schemaId1 = client.put(schema1);
        final long schemaId2 = client.put(schema2);

        //If after that, all instances of schema registry go down
        registry.stopAllSchemaRegistryInstances();

        //And the client crashes and is restarted
        client = createRestSchemaRegistryClient(AVAILABLE_SCHEMA_REGISTRY_INSTANCES_URL_LIST);

        //Once the registry comes back online
        registry.startAllSchemaRegistryInstances();

        //Then schema registry client should be able to return previously registered schemas, since they have been persisted to a Kafka topic
        assertEquals(schema1, client.lookup(schemaId1));
        assertEquals(schema2, client.lookup(schemaId2));

    }

    @Test
    public void test_getAllSchemas() throws Exception {
        //Given schema registry is available
        //And schemas have been registered successfully
        final BatchSchemaImporter importer = new BatchSchemaImporter(avroSchemasDirectory, registry.schemaRegistryUrl(), false);
        final Set<RegisteredSchema> registeredSchemas = importer.importSchemas();

        //If I query for all schemas
        final Set<RegisteredSchema> retrievedSchemas = client.getAllSchemas();

        //I should get back all the schemas registered
        assertEquals("Mismatch in amount of schemas retrieved", registeredSchemas.size(), retrievedSchemas.size());
        assertEquals(registeredSchemas, retrievedSchemas);
    }

    @Test
    public void test_getAllSchemas_emptyRegistry() throws SchemaRetrievalException {
        //Given schema registry is available
        //When querying an empty schema registry for all schemas
        //An empty list should be returned
        assertTrue("An empty list should be returned", client.getAllSchemas().isEmpty());
    }

    @Test
    public void test_getSchemasWithEventIds() throws Exception {
        //Given schema registry is available
        //And schemas have been registered successfully
        final BatchSchemaImporter importer = new BatchSchemaImporter(avroSchemasDirectory, registry.schemaRegistryUrl(), false);
        importer.importSchemas();

        //If I query for all schemas with eventids = [5211,5213] [INTERNAL_EVENT_ADMISSION_BLOCKING_STARTED, INTERNAL_EVENT_COV_CELL_DISCOVERY_END]
        final List<Integer> whitelistedEvents = new ArrayList<>();
        whitelistedEvents.add(5211);
        whitelistedEvents.add(5213);
        final Set<RegisteredSchema> retrievedSchemas = client.getSchemasByEventIds(whitelistedEvents);

        //I should get back 2 schemas
        assertEquals("Mismatch in amount of schemas retrieved", 2, retrievedSchemas.size());
    }

    @Test(expected = SchemaRetrievalException.class)
    public void test_getSchemaBySubject_invalidSubject() throws SchemaRetrievalException {
        //Given schema registry is available
        //When querying schema registry for a schema using a subject that hasn't been registered before
        //Then a SchemaRetrievalException should be thrown.
        client.lookup(INVALID_SUBJECT);
    }

    @Test
    public void test_getSchemaBySubject_validSubject() throws SchemaRetrievalException, IOException, SchemaRegistrationException {
        //Given schema registry is available
        //When querying schema registry for a schema using a subject that has been registered before
        final Schema schema = new Schema.Parser().parse(sampleSchema1File);
        final long schemaId = client.put(schema);

        final RegisteredSchema retrievedRegisteredSchema = client.lookup(schema.getFullName());
        //Then the correct schema should be returned
        assertEquals(schemaId, retrievedRegisteredSchema.getSchemaId());
        assertEquals(schema, retrievedRegisteredSchema.getSchema());
    }

    @Test
    public void test_getSchemaBySubject_validSubjectReturnedFromCacheOnSecondCall()
            throws SchemaRetrievalException, IOException, SchemaRegistrationException {
        final Cache<String, RegisteredSchema> registeredSchemaCache = CacheBuilder.newBuilder().recordStats().maximumSize(10).build();
        client.setCache(registeredSchemaCache);
        //Given schema registry is available
        //When querying schema registry for a schema using a subject that has been registered before
        final Schema schema = new Schema.Parser().parse(sampleSchema1File);
        final long schemaId = client.put(schema);
        //size of cache should be zero as it is empty
        assertEquals(0, client.getCache().stats().hitCount());
        assertEquals(0, client.getCache().asMap().size());

        RegisteredSchema retrievedRegisteredSchema = client.lookup(schema.getFullName());
        //Then the correct schema should be returned
        assertEquals(schemaId, retrievedRegisteredSchema.getSchemaId());
        assertEquals(schema, retrievedRegisteredSchema.getSchema());

        //cache sould be populated with one value
        assertEquals(1, client.getCache().asMap().size());
        //hit count stays at zero as the cache went to schema registry to look up value for RegisteredSchema
        assertEquals(0, client.getCache().stats().hitCount());

        retrievedRegisteredSchema = client.lookup(schema.getFullName());
        //Then the correct schema should be returned
        assertEquals(schemaId, retrievedRegisteredSchema.getSchemaId());
        assertEquals(schema, retrievedRegisteredSchema.getSchema());

        //hit count increases and will continue to increase for all subsequent calls as the the cache will return RegisteredSchema
        assertEquals(1, client.getCache().stats().hitCount());
        //subsequent lookup call should not change the size of cache
        assertEquals(1, client.getCache().asMap().size());

        //Creating a new schema
        final Schema schema2 = new Schema.Parser().parse(sampleSchema2File);
        final long schemaId2 = client.put(schema2);

        RegisteredSchema retrievedRegisteredSchema2 = client.lookup(schema2.getFullName());
        assertEquals(schemaId2, retrievedRegisteredSchema2.getSchemaId());
        assertEquals(schema2, retrievedRegisteredSchema2.getSchema());
        //subsequent call with new schema increases cache to two
        assertEquals(2, client.getCache().asMap().size());

        //hit count stays at one as the cache went to schema registry to look up value for new Registered Schema
        assertEquals(1, client.getCache().stats().hitCount());

        //second call
        retrievedRegisteredSchema2 = client.lookup(schema2.getFullName());
        assertEquals(schemaId2, retrievedRegisteredSchema2.getSchemaId());
        assertEquals(schema2, retrievedRegisteredSchema2.getSchema());

        //hit count increases and will continue to increase for all subsequent calls as the the cache will return RegisteredSchema
        assertEquals(2, client.getCache().stats().hitCount());
        retrievedRegisteredSchema2 = client.lookup(schema2.getFullName());
        assertEquals(3, client.getCache().stats().hitCount());
    }

    @Test
    public void test_schemaLookUp_withInvalidRegistry_URL_Service_Not_Found() throws IOException, SchemaRegistrationException {
        final Schema schema = new Schema.Parser().parse(sampleSchema1File);
        final long schemaId = client.put(schema);
        final Properties properties = new Properties();
        properties.put(SCHEMA_REGISTRY_ADDRESS_PARAMETER, registry.schemaRegistryUrl() + "/");
        final RestSchemaRegistryClient restSchemaRegistryClient = new RestSchemaRegistryClient(properties);
        try {
            restSchemaRegistryClient.lookup(schemaId);
        } catch (final SchemaRetrievalException e) {
            assertTrue(404 == ((RestClientException) e.getCause()).getErrorCode());
        }
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