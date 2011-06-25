/*
 * @(#)XDSBridgeConfigFactory.java   2011-06-17
 *
 * Copyright (c) 2011
 *
 *
 *
 *
 */

package com.vangent.hieos.services.xds.bridge.serviceimpl.support;

import com.vangent.hieos.services.xds.bridge.client.XDSDocumentRegistryClient;
import com.vangent.hieos.services.xds.bridge.client.XDSDocumentRepositoryClient;
import com.vangent.hieos.services.xds.bridge.mapper.ContentParser;
import com.vangent.hieos.services.xds.bridge.mapper.MapperFactory;
import com.vangent.hieos.services.xds.bridge.model.SubmitDocumentRequestBuilder;
import com.vangent.hieos.services.xds.bridge.serviceimpl.XDSBridgeConfig;
import com.vangent.hieos.services.xds.bridge.serviceimpl
    .XDSBridgeServiceContext;
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

            bridgeConfig = XDSBridgeConfig.parseConfigFile(xdsBridgeActor);

        } catch (Exception e) {

            throw new IllegalStateException(
                "Unable to process xdsbridge config file.", e);
        }

        // create wiring

        SubmitDocumentRequestBuilder sdrBuilder =
            new SubmitDocumentRequestBuilder(bridgeConfig);

        ContentParser conParser = new ContentParser();

        MapperFactory mapFactory = new MapperFactory(bridgeConfig, conParser);

        XDSDocumentRepositoryClient repoClient = null;

//      new XDSDocumentRepositoryClient(repositoryActor);

        XDSDocumentRegistryClient regClient = null;

//      new XDSDocumentRegistryClient(registryActor);

        return new XDSBridgeServiceContext(mapFactory, sdrBuilder, regClient,
                                           repoClient);

    }
}
