/*
 * @(#)MockXConfigActor.java   2011-06-24
 * 
 * Copyright (c) 2011
 *
 *
 *
 *
 */

package com.vangent.hieos.services.xds.bridge.serviceimpl.support;

import com.vangent.hieos.services.xds.bridge.utils.StringUtils;
import com.vangent.hieos.xutil.xconfig.XConfigActor;

import java.util.Properties;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-24
 * @author         Jim Horner    
 */
public class MockXConfigActor extends XConfigActor {

    /** Field description */
    private final Properties properties;

    /**
     * Constructs ...
     *
     *
     * @param properties
     */
    public MockXConfigActor(Properties properties) {

        super();
        this.properties = properties;
    }

    /**
     * Method description
     *
     *
     * @param propKey
     *
     * @return
     */
    @Override
    public boolean containsProperty(String propKey) {
        return this.properties.contains(propKey);
    }

    /**
     * Method description
     *
     *
     * @param propKey
     *
     * @return
     */
    @Override
    public String getProperty(String propKey) {
        return this.properties.getProperty(propKey);
    }

    /**
     * Method description
     *
     *
     * @param propKey
     *
     * @return
     */
    @Override
    public boolean getPropertyAsBoolean(String propKey) {

        return getPropertyAsBoolean(propKey, false);
    }

    /**
     * Method description
     *
     *
     * @param propKey
     * @param defaultValue
     *
     * @return
     */
    @Override
    public boolean getPropertyAsBoolean(String propKey, boolean defaultValue) {

        boolean result = defaultValue;
        String value = this.properties.getProperty(propKey);

        if (StringUtils.isNotBlank(value)) {

            result = Boolean.parseBoolean(value);
        }

        return result;
    }
}
