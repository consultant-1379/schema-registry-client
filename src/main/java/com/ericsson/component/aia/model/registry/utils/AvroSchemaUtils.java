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

import static com.ericsson.component.aia.model.registry.utils.Constants.AVRO_FILE_EXTENSION;
import static com.ericsson.component.aia.model.registry.utils.Utils.checkArgumentIsNotNull;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.avro.Schema;
import org.apache.avro.SchemaParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper methods for parsing *.avsc files.
 *
 */
public final class AvroSchemaUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(AvroSchemaUtils.class);
    private static final RabinHashFunction hashFunction = new RabinHashFunction();

    private AvroSchemaUtils() {
    }

    /**
     * Scans the specified {@code directory} for any *.avsc files, parses the files to avro schemas and loads the schemas into a cache using a hash id
     * generated from a combination of the schema namespace and name.
     *
     * @param directory
     *            directory containing avro schemas.
     * @return the cache of schemas and their associated unique schema ids.
     * @throws IOException
     *             if specified {@code directory} wasn't processed successfully.
     */
    public static Map<Long, Schema> createSchemaCache(final String directory) throws IOException {
        final AtomicBoolean errors = new AtomicBoolean();
        checkArgumentIsNotNull("directory", directory);
        LOGGER.debug("Scanning files in directory: [{}]", directory);
        final Map<Long, Schema> schemaCache = new HashMap<>();
        Files.walkFileTree(Paths.get(directory), new FileVisitor<Path>() {

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                LOGGER.info("Finished processing directory: [{}]", dir.toString());
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                LOGGER.debug("Processing directory: [{}] ...", dir.toString());
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(AVRO_FILE_EXTENSION)) {
                    LOGGER.debug("Processing file: [{}]", file.toString());
                    try {
                        final Schema schema = new Schema.Parser().parse(file.toFile());
                        final long schemaId = getSchemaId(schema.getFullName());
                        if (schemaCache.containsKey(schemaId)) {
                            LOGGER.warn("Schema [{}] and [{}] have the same hash! What are the odds?!", schema.getFullName(),
                                    schemaCache.get(schemaId).getFullName());
                        } else {
                            schemaCache.put(schemaId, schema);
                        }
                    } catch (final SchemaParseException e) {
                        LOGGER.warn("Couldn't parse file [{}] to schema, skipping malformed file ", file.toString());
                        errors.set(true);
                        return FileVisitResult.CONTINUE;
                    }

                } else {
                    LOGGER.debug("Skipping file: [{}] ", file.toString());
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
                throw exc;
            }
        });
        final String logMessage = errors.get() ? "Errors occurred when processing directory. Check logs for more details."
                : "Finished processing directories without any errors";
        LOGGER.info(logMessage);
        return schemaCache;
    }

    /**
     * Calculated the unique id for a schema subject using the RabinHashFunction algorithm.
     *
     * @param subject
     *            used to generate id.
     * @return unique id for schema
     */
    public static long getSchemaId(final String subject) {
        return hashFunction.hash(subject);
    }
}
