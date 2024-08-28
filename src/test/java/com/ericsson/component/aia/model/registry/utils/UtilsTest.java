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

import static com.ericsson.component.aia.model.registry.utils.Constants.*;
import static com.ericsson.component.aia.model.registry.utils.Utils.*;
import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Test;

public class UtilsTest {

    private static final int EXPECTED_CACHE_SIZE = Integer.parseInt(DEFAULT_CACHE_SIZE);

    @Test(expected = IllegalArgumentException.class)
    public void test_checkArgumentIsNotNull_argumentNameIsNull() {
        checkArgumentIsNotNull(null, "argumentValue");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_checkArgumentIsNotNull_argumentValueIsNull() {
        checkArgumentIsNotNull("argumentName", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_isWebService_nullUrl() {
        isValidRestEndpoint(null);
    }

    @Test
    public void test_isWebService_validHTTPUrl() {
        assertTrue(isValidRestEndpoint("http://"));
        assertTrue(isValidRestEndpoint("HTTP://"));
        assertTrue(isValidRestEndpoint("HttP://"));
    }

    @Test
    public void test_isWebService_validHTTPSUrl() {
        assertTrue(isValidRestEndpoint("https://"));
        assertTrue(isValidRestEndpoint("HTTPS://"));
        assertTrue(isValidRestEndpoint("HttPS://"));
    }

    @Test
    public void test_isWebService_invalidUrl() {
        assertFalse(isValidRestEndpoint(""));
        assertFalse(isValidRestEndpoint("http: //"));
        assertFalse(isValidRestEndpoint(" http://"));
        assertFalse(isValidRestEndpoint("htpp://"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_getSchemaRegistryUrlProperty_nullProperties() {
        getSchemaRegistryUrlProperty(null);
    }

    @Test
    public void test_getSchemaRegistryUrlProperty_noPropertySet() {
        assertEquals(DEFAULT_SCHEMA_REGISTRY_ADDRESS, getSchemaRegistryUrlProperty(new Properties()));
    }

    @Test
    public void test_getSchemaRegistryUrlProperty_validProperty() {
        final Properties properties = new Properties();
        final String schemaRegistryUrl = "http://localhost:9090";
        properties.put(SCHEMA_REGISTRY_ADDRESS_PARAMETER, schemaRegistryUrl);
        assertEquals(schemaRegistryUrl, getSchemaRegistryUrlProperty(properties));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_getSchemaRegistryCacheSizeProperty_nullProperties() {
        getSchemaRegistryCacheSizeProperty(null);
    }

    @Test
    public void test_getSchemaRegistryCacheSizeProperty_noPropertySet() {
        assertEquals(EXPECTED_CACHE_SIZE, getSchemaRegistryCacheSizeProperty(new Properties()));
    }

    @Test
    public void test_getSchemaRegistryCacheSizeProperty_propertySetButEmpty() {
        final Properties properties = new Properties();
        final String cacheSize = "";
        properties.put(SCHEMA_REGISTRY_CACHE_MAX_SIZE_PARAMETER, cacheSize);
        assertEquals(EXPECTED_CACHE_SIZE, getSchemaRegistryCacheSizeProperty(properties));
    }

    @Test
    public void test_getSchemaRegistryCacheSizeProperty_propertySetButNotAValidInteger() {
        final Properties properties = new Properties();
        final String cacheSize = "234r";
        properties.put(SCHEMA_REGISTRY_CACHE_MAX_SIZE_PARAMETER, cacheSize);
        assertEquals(EXPECTED_CACHE_SIZE, getSchemaRegistryCacheSizeProperty(properties));
    }

    @Test
    public void test_getSchemaRegistryCacheSizeProperty_validProperty() {
        final Properties properties = new Properties();
        final String cacheSize = "300";
        properties.put(SCHEMA_REGISTRY_CACHE_MAX_SIZE_PARAMETER, cacheSize);
        assertEquals((int) Integer.valueOf(cacheSize), getSchemaRegistryCacheSizeProperty(properties));
    }

    @Test
    public void test_checkArgumentIsNumeric_validProperty() {
        final String numericValue = "300";
        checkArgumentIsNumeric("Value Name", numericValue);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_checkArgumentIsNumeric_invalidProperty() {
        final String noneNumericValue = "300x";
        checkArgumentIsNumeric("Value Name", noneNumericValue);
    }

}
