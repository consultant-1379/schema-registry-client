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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class RabinHashFunctionTest {

    private static final String STRING_TO_BE_HASHED = "Foo";
    private RabinHashFunction rabinHashFunction;

    @Before
    public void setUp() throws Exception {
        rabinHashFunction = new RabinHashFunction();
    }

    @Test
    public void test_hash_with_int() throws Exception {
        assertEquals(4294967298L, rabinHashFunction.hash(new int[] { 1, 2 }));
    }

    @Test
    public void test_hash_with_long() throws Exception {
        assertEquals(-1406115773L, rabinHashFunction.hash(new long[] { 1, 2 }));
    }

    @Test
    public void test_hash_with_object() throws Exception {
        assertEquals(-8597568670489642382L, rabinHashFunction.hash(new StringBuffer(STRING_TO_BE_HASHED)));
    }

}
