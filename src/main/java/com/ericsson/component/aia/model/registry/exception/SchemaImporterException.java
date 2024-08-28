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
 * Thrown when schema registry client did not finish the batch import due to an unexpected error.
 */
public class SchemaImporterException extends RuntimeException {

    private static final long serialVersionUID = -8341834821826292347L;

    /**
     * Instantiates a new SchemaImporterException.
     *
     * @param message
     *            the message
     * @param cause
     *            the cause
     */
    public SchemaImporterException(final String message, final Throwable cause) {
        super(message, cause);

    }

}
