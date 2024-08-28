/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.model.registry.testutils;

import java.util.Properties;

import com.ericsson.component.aia.model.registry.impl.RegisteredSchema;
import com.ericsson.component.aia.model.registry.impl.RestSchemaRegistryClient;
import com.google.common.cache.Cache;

public class RestSchemaRegistryClientTestHepler extends RestSchemaRegistryClient {

    public RestSchemaRegistryClientTestHepler(final Properties properties) {
         super(properties);
    }

    public void setCache(Cache<String, RegisteredSchema> cache){
        super.setCache(cache);
    }

    public Cache<String, RegisteredSchema> getCache(){
        return registeredSchemaCache;
    } 
}
