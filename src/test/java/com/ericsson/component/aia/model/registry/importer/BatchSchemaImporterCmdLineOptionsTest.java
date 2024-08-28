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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.kohsuke.args4j.CmdLineException;

public class BatchSchemaImporterCmdLineOptionsTest {

    private static final String UNKNOWN_ARGUMENT = "-unknown=";
    private static final String REGISTRY_ARGUMENT = "-registry=";
    private static final String DIRECTORY_ARGUMENT = "-dir=";
    private static final String CHECK_ONLY_ARGUMENT = "-checkOnly";
    private static final String TIMEOUT_ARGUMENT = "-timeout=";
    private static final String VALID_DIRECTORY = "src/test/resources/avro/schemas";
    private static final String INVALID_DIRECTORY = "src/test/resources/avro/schemas/unknown";
    private static final String VALID_REGISTRY = "http://192.168.99.100:8081/";
    private static final String INVALID_REGISTRY = "file://foo/bar";
    private static final String VALID_TIMEOUT = "1";
    private static final String INVALID_TIMEOUT_LETTER = "a";
    private static final String INVALID_TIMEOUT_NEGATIVE_NUMBER = "-1";
    private static final String INVALID_TIMEOUT_NUMBERS_MIXED_WITH_LETTERS = "1a";
    private static final String INVALID_TIMEOUT_NUMBERS_MIXED_WITH_SYMBOLS = "1.";

    @Test
    public void test_parse_with_minimal_options() throws Exception {
        final BatchSchemaImporterCmdLineOptions cmdLineOptions = BatchSchemaImporterCmdLineOptions
                .parse(new String[] { DIRECTORY_ARGUMENT + VALID_DIRECTORY, REGISTRY_ARGUMENT + VALID_REGISTRY, });
        assertEquals(VALID_DIRECTORY, cmdLineOptions.getDirectory());
        assertEquals(VALID_REGISTRY, cmdLineOptions.getRegistry());
        assertFalse(cmdLineOptions.isCheckOnly());
    }

    @Test
    public void test_parse_with_all_options() throws Exception {
        final BatchSchemaImporterCmdLineOptions cmdLineOptions = BatchSchemaImporterCmdLineOptions
                .parse(new String[] { DIRECTORY_ARGUMENT + VALID_DIRECTORY, REGISTRY_ARGUMENT + VALID_REGISTRY, CHECK_ONLY_ARGUMENT, });
        assertEquals(VALID_DIRECTORY, cmdLineOptions.getDirectory());
        assertEquals(VALID_REGISTRY, cmdLineOptions.getRegistry());
        assertTrue(cmdLineOptions.isCheckOnly());
    }

    @Test(expected = CmdLineException.class)
    public void test_parse_without_any_arguments() throws Exception {
        BatchSchemaImporterCmdLineOptions.parse(new String[] {});
    }

    @Test(expected = CmdLineException.class)
    public void test_parse_with_an_invalid_directory() throws Exception {
        BatchSchemaImporterCmdLineOptions.parse(new String[] { DIRECTORY_ARGUMENT + INVALID_DIRECTORY, REGISTRY_ARGUMENT + VALID_REGISTRY, });
    }

    @Test(expected = CmdLineException.class)
    public void test_parse_without_a_registry() throws Exception {
        BatchSchemaImporterCmdLineOptions.parse(new String[] { DIRECTORY_ARGUMENT + VALID_DIRECTORY, });
    }

    @Test(expected = CmdLineException.class)
    public void test_parse_with_an_invalid_registry() throws Exception {
        BatchSchemaImporterCmdLineOptions.parse(new String[] { DIRECTORY_ARGUMENT + VALID_DIRECTORY, REGISTRY_ARGUMENT + INVALID_REGISTRY });
    }

    @Test(expected = CmdLineException.class)
    public void test_parse_with_unknown_args() throws Exception {
        BatchSchemaImporterCmdLineOptions.parse(new String[] { UNKNOWN_ARGUMENT + VALID_DIRECTORY, });
    }

    @Test
    public void test_parse_with_valid_timeout() throws Exception {
        BatchSchemaImporterCmdLineOptions.parse(new String[] { DIRECTORY_ARGUMENT + VALID_DIRECTORY, REGISTRY_ARGUMENT + VALID_REGISTRY,
            CHECK_ONLY_ARGUMENT, TIMEOUT_ARGUMENT + VALID_TIMEOUT });
    }

    @Test(expected = CmdLineException.class)
    public void test_parse_with_invalid_timeout_letter() throws Exception {
        BatchSchemaImporterCmdLineOptions.parse(new String[] { DIRECTORY_ARGUMENT + VALID_DIRECTORY, REGISTRY_ARGUMENT + VALID_REGISTRY,
            CHECK_ONLY_ARGUMENT, TIMEOUT_ARGUMENT + INVALID_TIMEOUT_LETTER });
    }

    @Test(expected = CmdLineException.class)
    public void test_parse_with_invalid_timeout_negative_number() throws Exception {
        BatchSchemaImporterCmdLineOptions.parse(new String[] { DIRECTORY_ARGUMENT + VALID_DIRECTORY, REGISTRY_ARGUMENT + VALID_REGISTRY,
            CHECK_ONLY_ARGUMENT, TIMEOUT_ARGUMENT + INVALID_TIMEOUT_NEGATIVE_NUMBER });
    }

    @Test(expected = CmdLineException.class)
    public void test_parse_with_invalid_timeout_numbers_mixed_with_letters() throws Exception {
        BatchSchemaImporterCmdLineOptions.parse(new String[] { DIRECTORY_ARGUMENT + VALID_DIRECTORY, REGISTRY_ARGUMENT + VALID_REGISTRY,
            CHECK_ONLY_ARGUMENT, TIMEOUT_ARGUMENT + INVALID_TIMEOUT_NUMBERS_MIXED_WITH_LETTERS });
    }

    @Test(expected = CmdLineException.class)
    public void test_parse_with_invalid_timeout_numbers_mixed_with_symbols() throws Exception {
        BatchSchemaImporterCmdLineOptions.parse(new String[] { DIRECTORY_ARGUMENT + VALID_DIRECTORY, REGISTRY_ARGUMENT + VALID_REGISTRY,
            CHECK_ONLY_ARGUMENT, TIMEOUT_ARGUMENT + INVALID_TIMEOUT_NUMBERS_MIXED_WITH_SYMBOLS });
    }

}
