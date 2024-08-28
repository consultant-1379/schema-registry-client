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
package com.ericsson.component.aia.model.registry.testutils;

public interface TestConstants {

    final int SCHEMA_REGISTRY1_PORT = 8081;
    final int SCHEMA_REGISTRY2_PORT = 8082;
    final int UNAVAILABLE_SCHEMA_REGISTRY_PORT = 8765;
    final String SCHEMA_REGISTRY1_PORT_STRING = String.valueOf(SCHEMA_REGISTRY1_PORT);
    final String SCHEMA_REGISTRY2_PORT_STRING = String.valueOf(SCHEMA_REGISTRY2_PORT);
    final String SCHEMA_REGISTRY_INSTANCES_PORT_STRING = SCHEMA_REGISTRY1_PORT_STRING + "," + SCHEMA_REGISTRY1_PORT_STRING;
    final String LOCALHOST_PREFIX = "http://localhost:";
    final String SCHEMA_REGISTRY1_URL = LOCALHOST_PREFIX + SCHEMA_REGISTRY1_PORT;
    final String SCHEMA_REGISTRY2_URL = LOCALHOST_PREFIX + SCHEMA_REGISTRY2_PORT;
    final String UNAVAILABLE_SCHEMA_REGISTRY_URL = LOCALHOST_PREFIX + UNAVAILABLE_SCHEMA_REGISTRY_PORT;
    final String AVAILABLE_SCHEMA_REGISTRY_INSTANCES_URL_LIST = SCHEMA_REGISTRY1_URL + "," + SCHEMA_REGISTRY2_URL;
    final String FOURTH_LEVEL_SCHEMA_FILEPATH = "avro/schemas/one.two.three/FourthLevelSchema.avsc";
    final String SAMPLE_SCHEMA_FILEPATH = "avro/schemas/SampleSchema1.avsc";
    final String SAMPLE_SCHEMA_TWO_FILEPATH = "avro/schemas/SampleSchema2.avsc";
    final String BATCH_IMPORTER_SCHEMA_FILEPATH = "batchImporterSchemas/celltrace.t.r12a.v26/INTERNAL_EVENT_ADMISSION_BLOCKING_STOPPED.avsc";
    final String BATCH_XML_SCHEMA_FILEPATH = "batchImporterXmls/T_R12A_26.xml";
    final String TEMP_AVRO_DIRECTORY = "target/avroSchemas";
    final String SAMPLE_SCHEMA1_SUBJECT = "com.ericsson.oss.avro.example.SampleSchema";
    final String FOURTH_LEVEL_SCHEMA_SUBJECT = "com.ericsson.oss.avro.example.FourthLevelSchema";

}
