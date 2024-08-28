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

package com.ericsson.component.aia.model.registry.impl;

import static com.ericsson.component.aia.model.registry.utils.Utils.checkArgumentIsNotNull;

import java.io.Serializable;

import org.apache.avro.Schema;

/**
 * DTO to store the logical pairing of {@code schema} and it's associated {@code schemaId}.
 */
public class RegisteredSchema implements Serializable {

    public static final char QUOTE = '"';
    private static final long serialVersionUID = 1L;
    private final long schemaId;
    private final Schema schema;

    /**
     * Creates a RegisteredSchema instance with the specified {@code schemaId} and {@code schema}.
     *
     * @param schemaId
     *            the unique identifier for {@code schema}.
     * @param schema
     *            avro schema.
     */
    public RegisteredSchema(final long schemaId, final Schema schema) {
        checkArgumentIsNotNull("schema", schema);
        this.schemaId = schemaId;
        this.schema = schema;
    }

    /**
     * @return the schemaId
     */
    public long getSchemaId() {
        return schemaId;
    }

    /**
     * @return the schema
     */
    public Schema getSchema() {
        return schema;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (schema == null ? 0 : schema.hashCode());
        result = prime * result + (int) (schemaId ^ schemaId >>> 32);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RegisteredSchema other = (RegisteredSchema) obj;
        if (schemaId != other.schemaId) {
            return false;
        }
        return schema.equals(other.schema);
    }

    @Override
    public String toString() {
        return "{" + QUOTE + "schemaId" + QUOTE + ":" + schemaId + "," + QUOTE + "schema" + QUOTE + ":" + schema + "}";
    }

}
