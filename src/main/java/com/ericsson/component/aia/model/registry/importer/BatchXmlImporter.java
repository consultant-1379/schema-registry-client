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

import static java.lang.Long.parseLong;
import static java.lang.String.valueOf;
import static java.util.concurrent.TimeUnit.HOURS;

import static com.ericsson.component.aia.model.registry.utils.Utils.checkArgumentIsNotNull;
import static com.ericsson.component.aia.model.registry.utils.Utils.checkArgumentIsNumeric;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.jaxen.JaxenException;
import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.model.base.config.bean.SchemaEnum;
import com.ericsson.component.aia.model.base.exception.SchemaException;
import com.ericsson.component.aia.model.generation.avro.json.GenericAvroSchemaGenerator;
import com.ericsson.component.aia.model.registry.exception.SchemaRegistrationException;
import com.ericsson.component.aia.model.registry.impl.RegisteredSchema;

/**
 * API for converting node xmls to avro schemas and importing those avro schemas into schema registry via POST requests.
 * Sample usage: <code>
 * java -classpath "..." com.ericsson.component.aia.model.registry.importer.BatchXmlImporter
 * /esn/unprocessedXmls /esn/temp/avro http://str-1-reg1:8081,http://str-2-reg1:8081
 * </code>
 */
public class BatchXmlImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchXmlImporter.class);
    private static final long DEFAULT_TIMEOUT_IN_SECONDS = HOURS.toSeconds(1);
    private final SchemaEnum schemaType;
    private final String xmlDirectory;
    private final String avroDirectory;
    private final String registry;
    private final String timeout;

    /**
     * Constructor.
     *
     * @param schemaType
     *            the parser type used to parser the schema xml.
     * @param xmlDirectory
     *            the directory containing the xml file to parse to avro schemas.
     * @param avroDirectory
     *            the directory where the avro schemas will be stored on the file system.
     * @param registry
     *            the url of the schema registry the avro schemas will be loaded into.
     */
    public BatchXmlImporter(final SchemaEnum schemaType, final String xmlDirectory, final String avroDirectory, final String registry) {
        this(schemaType, xmlDirectory, avroDirectory, registry, valueOf(DEFAULT_TIMEOUT_IN_SECONDS));
    }

    /**
     * Constructor.
     *
     * @param schemaType
     *            the parser type used to parser the schema xml.
     * @param xmlDirectory
     *            the directory containing the xml file to parse to avro schemas.
     * @param avroDirectory
     *            the directory where the avro schemas will be stored on the file system.
     * @param registry
     *            the url of the schema registry the avro schemas will be loaded into.
     * @param timeout
     *            time in seconds before import process is interrupted by timeout.
     */
    public BatchXmlImporter(final SchemaEnum schemaType, final String xmlDirectory, final String avroDirectory, final String registry,
            final String timeout) {
        checkArgumentIsNotNull("schemaType", schemaType);
        checkArgumentIsNotNull("xmlDirectory", xmlDirectory);
        checkArgumentIsNotNull("avroDirectory", avroDirectory);
        checkArgumentIsNotNull("registry", registry);
        checkArgumentIsNumeric("timeout", timeout);
        this.schemaType = schemaType;
        this.xmlDirectory = xmlDirectory;
        this.avroDirectory = avroDirectory;
        this.registry = registry;
        this.timeout = timeout;
    }

    /**
     * Entry point for cmd line xml import tool.
     *
     * @param args
     *            arguments to be processed (schemaType name, xml directory, avro directory, registry address, timeout in seconds (optional, default 1
     *            hour)).
     * @throws SchemaException
     *             if schema cannot be parsed.
     * @throws JDOMException
     *             if schema cannot be parsed.
     * @throws JaxenException
     *             if schema cannot be parsed.
     * @throws IOException
     *             if the directory specified couldn't be processed.
     * @throws SchemaRegistrationException
     *             if not all schemas were imported.
     */
    public static void main(final String[] args) throws JaxenException, IOException, JDOMException, SchemaException, SchemaRegistrationException {
        if (args == null || !(args.length == 4 || args.length == 5)) {
            throw new IllegalArgumentException("Main method takes four or five arguments: <schematype as specified in the SchemaTypes.xml> "
                    + "<input xml directory> <avro schema directory> <schema registry url> <timeout in seconds (optional, default 1 hour)>");
        }
        if (args.length == 4) {
            new BatchXmlImporter(SchemaEnum.fromValue(args[0]), args[1], args[2], args[3]).importXmls();
        }
        if (args.length == 5) {
            new BatchXmlImporter(SchemaEnum.fromValue(args[0]), args[1], args[2], args[3], args[4]).importXmls();
        }
        System.exit(0);
    }

    /**
     * Converts node xmls to avro schemas and importing those avro schemas into schema registry via POST requests.
     *
     * @return a set of {@link RegisteredSchema} that were successfully imported into schema registry.
     * @throws IOException
     *             if directory can't be written/read from
     * @throws JaxenException
     *             if schema can't be parsed
     * @throws JDOMException
     *             if schema can't be parsed
     * @throws SchemaException
     *             if schema can't be parsed
     * @throws SchemaRegistrationException
     *             if not all schemas were imported.
     */
    public Set<RegisteredSchema> importXmls() throws IOException, JaxenException, JDOMException, SchemaException, SchemaRegistrationException {
        validateXmlDirectory();
        parseXmls();
        return loadSchemas();
    }

    private void validateXmlDirectory() {
        if (!new File(xmlDirectory).exists()) {
            throw new IllegalArgumentException(xmlDirectory + " is not a valid directory");
        }
    }

    private void parseXmls() throws IOException, JaxenException, JDOMException, SchemaException {
        LOGGER.info("Converting xmls in directory {} to avro schemas and outputting new avro schemas to {}", xmlDirectory, avroDirectory);
        new GenericAvroSchemaGenerator(schemaType).generate(xmlDirectory, avroDirectory);
    }

    private Set<RegisteredSchema> loadSchemas() throws IOException, SchemaRegistrationException {
        LOGGER.info("Loading avro schemas from {} into schema registry deployed on {}", avroDirectory, registry);
        return new BatchSchemaImporter(avroDirectory, registry, false, parseLong(timeout)).importSchemas();
    }

}
