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

import static com.ericsson.component.aia.model.registry.utils.Constants.INVALID_REST_ENDPOINT_MESSAGE;
import static com.ericsson.component.aia.model.registry.utils.Utils.isValidRestEndpoint;

import java.io.File;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * The command line options for the batch schema importer.
 */
public class BatchSchemaImporterCmdLineOptions {

    @Option(name = "-dir", usage = "Directory to scan for schemas", required = true)
    private String directory;

    @Option(name = "-registry", usage = "Schema registry", required = true)
    private String registry;

    @Option(name = "-checkOnly", usage = "Just check if schemas are valid and don't perform import")
    private boolean checkOnly;

    @Option(name = "-timeout", usage = "Timeout in seconds to define a time limit for the execution of the import. If the timeout expires, "
            + "the import process is terminated and a Runtime Exception is thrown.")
    private long timeout;

    /**
     * Parses supplied arguments for {@link BatchSchemaImporter}.
     *
     * @param args
     *            arguments to be parsed.
     * @return a {@link BatchSchemaImporterCmdLineOptions} instance
     * @throws CmdLineException
     *             if the arguments could not be parsed or were invalid.
     */
    public static BatchSchemaImporterCmdLineOptions parse(final String[] args) throws CmdLineException {
        final BatchSchemaImporterCmdLineOptions options = new BatchSchemaImporterCmdLineOptions();
        final CmdLineParser parser = new CmdLineParser(options);
        parser.parseArgument(args);
        if (!new File(options.directory).exists()) {
            throw new CmdLineException(parser, "avro schema input directory " + options.directory + " does not exist!", null);
        }
        if (!isValidRestEndpoint(options.registry)) {
            throw new CmdLineException(parser, INVALID_REST_ENDPOINT_MESSAGE, null);
        }
        if (options.getTimeout() < 0) {
            throw new CmdLineException(parser, "Timeout value cannot be negative", null);
        }
        return options;
    }

    public String getDirectory() {
        return directory;
    }

    public String getRegistry() {
        return registry;
    }

    public boolean isCheckOnly() {
        return checkOnly;
    }

    public long getTimeout() {
        return timeout;
    }

    @Override
    public String toString() {
        return "Cmd line arguments specified: [directory=" + directory + ", registry=" + registry + ", checkOnly=" + checkOnly + ", timeout="
                + timeout + "]";
    }

}
