/*
 * @(#)XDSBridgeConfigFactory.java   2011-06-17
 *
 * Copyright (c) 2011
 *
 *
 *
 *
 */

package com.vangent.hieos.services.xds.bridge.mock;

import com.vangent.hieos.services.xds.bridge.support
    .XDSBridgeServiceContext;
import com.vangent.hieos.services.xds.bridge.support.XDSBridgeConfig;
import com.vangent.hieos.services.xds.bridge.utils.JUnitHelper;
import com.vangent.hieos.xutil.xconfig.XConfigActor;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-17
 * @author         Jim Horner
 */
public class XDSBridgeServiceContextFactory {

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws Exception
     */
    public static XDSBridgeServiceContext createServiceContext()
            throws Exception {

        XConfigActor xdsBridgeActor = JUnitHelper.createXDSBridgeActor();

        XDSBridgeConfig bridgeConfig = null;

        try {

            bridgeConfig = XDSBridgeConfig.newInstance(xdsBridgeActor);

        } catch (Exception e) {

            throw new IllegalStateException(
                "Unable to process xdsbridge config file.", e);
        }

        // create wiring

        return new XDSBridgeServiceContext(null, null, bridgeConfig);
    }
}
