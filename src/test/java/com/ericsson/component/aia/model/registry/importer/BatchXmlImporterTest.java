/*
 * ------------------------------------------------------------------------------
 *  *******************************************************************************
 *  * COPYRIGHT Ericsson 2017
 *  *
 *  * The copyright to the computer program(s) herein is the property of
 *  * Ericsson Inc. The programs may be used and/or copied only with written
 *  * permission from Ericsson Inc. or in accordance with the terms and
 *  * conditions stipulated in the agreement/contract under which the
 *  * program(s) have been supplied.
 *  *******************************************************************************
 *  *----------------------------------------------------------------------------
 */

package com.ericsson.component.aia.model.registry.importer;

import java.io.IOException;

import org.jaxen.JaxenException;
import org.jdom.JDOMException;
import org.junit.Test;

import com.ericsson.component.aia.model.base.exception.SchemaException;
import com.ericsson.component.aia.model.registry.exception.SchemaRegistrationException;

public class BatchXmlImporterTest {

    @Test(expected = IllegalArgumentException.class)
    public void no_args_in_main_throws_an_illegalArgumentException()
            throws JDOMException, SchemaException, JaxenException, SchemaRegistrationException, IOException {
        String[] noArgs = {};
        BatchXmlImporter.main(noArgs);
    }

    @Test(expected = IllegalArgumentException.class)
    public void not_enough_args_in_main_throws_an_illegalArgumentException()
            throws JDOMException, SchemaException, JaxenException, SchemaRegistrationException, IOException {
        String[] notEnoughArgs = { "arg1", "arg2", "arg3" };
        BatchXmlImporter.main(notEnoughArgs);
    }

    @Test(expected = IllegalArgumentException.class)
    public void too_many_args_in_main_throws_an_illegalArgumentException()
            throws JDOMException, SchemaException, JaxenException, SchemaRegistrationException, IOException {
        String[] tooManyArgs = { "arg1", "arg2", "arg3", "arg4", "arg5", "arg6" };
        BatchXmlImporter.main(tooManyArgs);
    }
}
