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

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;

import static com.ericsson.component.aia.model.registry.utils.Constants.*;

import java.util.Properties;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.model.registry.exception.*;

/**
 * Generic utility methods used throughout application.
 */
public final class Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    private static final String INTEGER_REGEX = "\\d+";
    private static final String REGEX_FOR_ONLY_DECIMAL = "^\\d+$";

    private Utils() {}

    /**
     * Returns default exception message for a {@link SchemaRetrievalException}, with formatting for specified {@code schemaId}.
     *
     * @param schemaId
     *            used for retrieval of schema.
     * @return formatted string.
     */
    public static String getSchemaRetrievalExceptionMessage(final long schemaId) {
        return String.format("Could not retrieve schema with schema id: %s", schemaId);
    }

    /**
     * Returns default exception message for a {@link SchemaRegistrationException}, with formatting for specified {@code schema}.
     *
     * @param schema
     *            schema which could not be registered.
     * @return formatted string.
     */
    public static String getSchemaRegistrationExceptionMessage(final Schema schema) {
        checkArgumentIsNotNull("schema", schema);
        return String.format("Could not register schema %s under subject %s", schema.getName(), schema.getFullName());
    }

    /**
     * Useful method to ensure null arguments aren't passed into a method. Like guava preconditions library.
     *
     * @param argumentName
     *            the name of the argument being checked.
     * @param argument
     *            the argument to check.
     * @throws IllegalArgumentException
     *             if either {@code argumentName} or {@code argument} are null.
     */
    public static void checkArgumentIsNotNull(final String argumentName, final Object argument) throws IllegalArgumentException {
        if (argumentName == null) {
            throw new IllegalArgumentException("Name of argument cannot be null");
        }

        if (argument == null) {
            final String exceptionMessage = String.format("Argument '%s' cannot be null", argumentName);
            throw new IllegalArgumentException(exceptionMessage);
        }
    }

    /**
     * Method to ensure string is numeric
     *
     * @param argumentName
     *            the name of the argument being checked.
     * @param argument
     *            the argument to check.
     * @throws IllegalArgumentException
     *             if {@code argumentName} is null or {@code argument} does not represent a number.
     */
    public static void checkArgumentIsNumeric(final String argumentName, final String argument) throws IllegalArgumentException {
        checkArgumentIsNotNull(argumentName, argument);
        if (!isStringNumeric(argument)) {
            final String exceptionMessage = String.format("Argument '%s' does not represent a number", argumentName);
            throw new IllegalArgumentException(exceptionMessage);
        }

    }

    /**
     * Check if the specified {@code schemaRegistryUrl} denotes web URL.
     *
     * @param schemaRegistryUrl
     *            to check.
     * @return true if the specified {@code schemaRegistryUrl} begins with either a "http://" or "https://" prefix. Otherwise null. Check is case
     *         insensitive.
     */
    public static boolean isValidRestEndpoint(final String schemaRegistryUrl) {
        checkArgumentIsNotNull("schemaRegistryUrl", schemaRegistryUrl);
        return schemaRegistryUrl.toLowerCase().startsWith(HTTP) || schemaRegistryUrl.toLowerCase().startsWith(HTTPS);
    }

    /**
     * Returns the value of the "schemaRegistry.address" value if it exists in the specified {@code properties}. Otherwise, returns the default
     * schemaRegistry.address value and logs it.
     *
     * @param properties
     *            to check for the "schemaRegistry.address" property
     * @return String value
     */
    public static String getSchemaRegistryUrlProperty(final Properties properties) {
        checkArgumentIsNotNull("properties", properties);
        return getSchemaRegistryStringProperty(properties, SCHEMA_REGISTRY_ADDRESS_PARAMETER, DEFAULT_SCHEMA_REGISTRY_ADDRESS);
    }

    /**
     * Returns the value of the "schemaRegistry.cacheMaximumSize" value if it exists in the specified {@code properties}. Otherwise, returns the
     * default schemaRegistry.cacheMaximumSize value and logs it.
     *
     * @param properties
     *            to check for the "schemaRegistry.cacheMaximumSize" property
     * @return Integer value
     */
    public static int getSchemaRegistryCacheSizeProperty(final Properties properties) {
        checkArgumentIsNotNull("properties", properties);
        return Integer.parseInt(getSchemaRegistryIntegerProperty(properties, SCHEMA_REGISTRY_CACHE_MAX_SIZE_PARAMETER, DEFAULT_CACHE_SIZE));
    }

    /**
     * Execute a command with a timeout constraint set.
     *
     * @param <T>
     *            the generic type
     * @param callable
     *            the callable to be executed
     * @param timeoutInSeconds
     *            the timeout in seconds
     * @return T
     */
    public static <T> T executeCommandWithTimeOutConstraint(final Callable<T> callable, final long timeoutInSeconds) {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future<T> future = executor.submit(callable);
        try {
            return future.get(timeoutInSeconds, SECONDS);
        } catch (final TimeoutException exception) {
            future.cancel(true);
            throw new UploadTimeoutException(
                    format("Timeout (%d seconds) occurred while executing command; execution will be terminated.", timeoutInSeconds), exception);
        } catch (ExecutionException | InterruptedException exception) {
            throw new SchemaImporterException("Unexpected exception while running BatchSchemaImporter", exception);
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * Checks if a string represents a number.
     *
     * @param number
     *            the number to be test
     * @return true, if the string is a number
     */
    public static boolean isStringNumeric(final String number) {
        final Pattern regexCompiled = Pattern.compile(REGEX_FOR_ONLY_DECIMAL);
        final Matcher matchResult = regexCompiled.matcher(number);
        return matchResult.find();
    }

    private static String getSchemaRegistryStringProperty(final Properties properties, final String propertyName, final String defaultValue) {
        return properties.containsKey(propertyName) ? properties.getProperty(propertyName)
                : getDefaultStringValueWithLogging(propertyName, defaultValue);
    }

    private static String getSchemaRegistryIntegerProperty(final Properties properties, final String propertyName, final String defaultValue) {
        return isPropertyInteger(properties, propertyName) ? properties.getProperty(propertyName)
                : getDefaultStringValueWithLogging(propertyName, defaultValue);
    }

    private static boolean isPropertyInteger(final Properties properties, final String propertyName) {
        return properties.containsKey(propertyName) && properties.getProperty(propertyName).matches(INTEGER_REGEX);
    }

    private static String getDefaultStringValueWithLogging(final String propertyName, final String defaultValue) {
        LOGGER.info("{} property has not been set. Using default value: {}", propertyName, defaultValue);
        return defaultValue;
    }

}
