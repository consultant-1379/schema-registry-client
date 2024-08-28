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

import static java.util.concurrent.TimeUnit.HOURS;

import static com.ericsson.component.aia.model.registry.utils.AvroSchemaUtils.createSchemaCache;
import static com.ericsson.component.aia.model.registry.utils.Constants.DEFAULT_CACHE_SIZE;
import static com.ericsson.component.aia.model.registry.utils.Utils.checkArgumentIsNotNull;
import static com.ericsson.component.aia.model.registry.utils.Utils.executeCommandWithTimeOutConstraint;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;

import org.apache.avro.Schema;
import org.kohsuke.args4j.CmdLineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.model.registry.client.SchemaRegistryClient;
import com.ericsson.component.aia.model.registry.exception.SchemaRegistrationException;
import com.ericsson.component.aia.model.registry.impl.RegisteredSchema;
import com.ericsson.component.aia.model.registry.impl.RestSchemaRegistryClient;

/**
 * A command line tool to import schema to remote schema registry using {@link RestSchemaRegistryClient}. Sample usage: <code>
 * java -classpath "..." com.ericsson.component.aia.model.registry.importer.BatchSchemaImporter
 * -dir=target/generated-test-data/avro -registry=http://192.168.99.100:8081/
 * </code>
 */
public class BatchSchemaImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchSchemaImporter.class);
    private static final long DEFAULT_TIMEOUT_IN_SECONDS = HOURS.toSeconds(1);

    private String directory;
    private boolean checkOnly;
    private  SchemaRegistryClient client;
    private final Collection<Schema> schemas = new ArrayList<>();
    private long timeout;

    /**
     * Constructs a BatchSchemaRegistry instance.
     *
     * @param directory
     *            the location of the *.avsc files on the file system.
     * @param registry
     *            the registry to load the *.avsc file into.
     * @param checkOnly
     *            false if the user wishes to import files into directory, true if the user only wishes to check if the files can't be parsed to avro
     *            schemas.
     */
    public BatchSchemaImporter(final String directory, final String registry, final boolean checkOnly) {
        this(directory, registry, checkOnly, DEFAULT_TIMEOUT_IN_SECONDS);
    }

    /**
     * Constructs a BatchSchemaRegistry instance.
     *
     * @param directory
     *            the location of the *.avsc files on the file system.
     * @param registry
     *            the registry to load the *.avsc file into.
     * @param checkOnly
     *            false if the user wishes to import files into directory, true if the user only wishes to check if the files can't be parsed to avro
     *            schemas.
     * @param timeout
     *            the limit of execution time in seconds
     */
    public BatchSchemaImporter(final String directory, final String registry, final boolean checkOnly, final long timeout) {
        checkArgumentIsNotNull("directory", directory);
        checkArgumentIsNotNull("registry", registry);
        this.directory = directory;
        client = new RestSchemaRegistryClient(registry, Integer.parseInt(DEFAULT_CACHE_SIZE));
        this.checkOnly = checkOnly;
        this.timeout = timeout;
    }

    /**
     * Import schemas into schema registry.
     *
     * @return a list of {@link RegisteredSchema} objects imported into schema registry.
     * @throws IOException
     *             if the directory specified couldn't be processed.
     * @throws SchemaRegistrationException
     *             if not all schemas were correctly imported.
     */
    public Set<RegisteredSchema> importSchemas() throws IOException, SchemaRegistrationException {
        final Callable<Set<RegisteredSchema>> callableForImportSchemas = wrapImportSchemasInCallable();
        return executeCommandWithTimeOutConstraint(callableForImportSchemas, timeout);
    }

    private Callable<Set<RegisteredSchema>> wrapImportSchemasInCallable() {
        final Callable<Set<RegisteredSchema>> callableForMain = new Callable<Set<RegisteredSchema>>() {
            @Override
            public Set<RegisteredSchema> call() throws Exception {
                return importSchemasIntoSchemaRegistry();
            }
        };
        return callableForMain;
    }

    private Set<RegisteredSchema> importSchemasIntoSchemaRegistry() throws IOException, SchemaRegistrationException {
        final Set<RegisteredSchema> registeredSchemas = new HashSet<>();
        final Set<Schema> registrationFailureSchemas = new HashSet<>();
        schemas.addAll(createSchemaCache(directory).values());
        final int expectedNumberOfSchemasImported = schemas.size();
        LOGGER.info("Number of schemas loaded from filesystem: {}", expectedNumberOfSchemasImported);

        if (!checkOnly) {
            LOGGER.info("Starting to import schemas into schema registry...");
            loadSchemasIntoRegistry(registeredSchemas, registrationFailureSchemas);
            LOGGER.info("Finished importing schemas into schema registry");
            checkBatchImportWasSuccessful(expectedNumberOfSchemasImported, registeredSchemas.size(), registrationFailureSchemas);
        }
        return registeredSchemas;
    }

    private void checkBatchImportWasSuccessful(final int expectedNumberOfSchemasImported, final int actualNumberOfSchemasImported,
            final Set<Schema> registrationFailureSchemas) throws SchemaRegistrationException {
        LOGGER.info("Number of schemas loaded into schema registry: {}", actualNumberOfSchemasImported);
        if (actualNumberOfSchemasImported != expectedNumberOfSchemasImported) {
            final String exceptionMessage = String.format("Not all schemas were loaded into schema registry. Expected: %s , Was: %s",
                    expectedNumberOfSchemasImported, actualNumberOfSchemasImported);
            LOGGER.error(exceptionMessage);
            logRegistrationFailureSchemas(registrationFailureSchemas);
            throw new SchemaRegistrationException(exceptionMessage);
        }
    }

    private void loadSchemasIntoRegistry(final Set<RegisteredSchema> registeredSchemas, final Set<Schema> registrationFailureSchemas) {
        for (final Schema schema : schemas) {
            try {
                LOGGER.debug("Importing {} schema into schema registry", schema.getFullName());
                final long generatedSchemaId = client.put(schema);
                final RegisteredSchema registeredSchema = new RegisteredSchema(generatedSchemaId, schema);
                registeredSchemas.add(registeredSchema);
                LOGGER.debug("Successfully imported schema: {}", registeredSchema);
            } catch (final SchemaRegistrationException e) {
                registrationFailureSchemas.add(schema);
                LOGGER.error("Failed to register schema {}", schema.getFullName(), e);
            }
        }
    }

    /**
     * Entry point for cmd line schema import tool.
     *
     * @param args
     *            arguments to be processed (directory, registry address, checkOnly flag).
     * @throws CmdLineException
     *             if the arguments could not be parsed correctly.
     * @throws IOException
     *             if the directory specified couldn't be processed.
     * @throws SchemaRegistrationException
     *             if not all schemas were correctly imported.
     */
    public static void main(final String[] args) throws CmdLineException, IOException, SchemaRegistrationException {
        final BatchSchemaImporterCmdLineOptions options = BatchSchemaImporterCmdLineOptions.parse(args);
        LOGGER.info("Starting batch import: [{}]", options);
        new BatchSchemaImporter(options.getDirectory(), options.getRegistry(), options.isCheckOnly(), options.getTimeout()).importSchemas();
        LOGGER.info("Finished batch import");
    }

    public Collection<Schema> getSchemasLoadedFromFileSystem() {
        return schemas;
    }

    private void logRegistrationFailureSchemas(final Set<Schema> registrationFailureSchemas) {
        final StringBuffer stringBuffer = new StringBuffer("Summary of Failure, unable to register the following schemas:");
        stringBuffer.append("\n");
        for (final Schema schema : registrationFailureSchemas) {
            stringBuffer.append(schema.getFullName());
            stringBuffer.append("\n");
        }
        LOGGER.error(stringBuffer.toString());
    }

}
