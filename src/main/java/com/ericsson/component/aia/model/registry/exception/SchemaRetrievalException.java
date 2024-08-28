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
 * Thrown when schema registry client cannot retrieved a schema from schema registry.
 *
 */
public class SchemaRetrievalException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs SchemaRetrievalException with specified {@code message} and {@code cause}.
     *
     * @param message
     *            detailing reason for exception.
     * @param cause
     *            underlying exception that caused it.
     */
    public SchemaRetrievalException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs SchemaRetrievalException with specified {@code message}.
     *
     * @param message
     *            detailing reason for exception.
     */
    public SchemaRetrievalException(final String message) {
        super(message);
    }

}
