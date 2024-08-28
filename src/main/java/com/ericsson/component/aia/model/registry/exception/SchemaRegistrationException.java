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
package com.ericsson.component.aia.model.registry.exception;

/**
 * Thrown when schema registry client cannot register a schema.
 *
 */
public class SchemaRegistrationException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs SchemaRegistrationException with specified {@code message}.
     *
     * @param message
     *            detailing reason for exception.
     */
    public SchemaRegistrationException(final String message) {
        super(message);
    }

    /**
     * Constructs SchemaRegistrationException with specified {@code message} and {@code cause}.
     *
     * @param message
     *            detailing reason for exception.
     * @param cause
     *            underlying exception that caused it.
     */
    public SchemaRegistrationException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
