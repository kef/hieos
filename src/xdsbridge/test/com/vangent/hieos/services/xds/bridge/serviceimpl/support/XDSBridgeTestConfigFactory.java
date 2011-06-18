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
import com.vangent.hieos.xutil.exception.XConfigException;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-17
 * @author         Jim Horner
 */
public class XDSBridgeTestConfigFactory {

    /**
     * Method description
     *
     *
     * @return
     */
    public static XDSBridgeConfig createConfig() {

        // create wiring, check environment
        SubmitDocumentRequestBuilder sdrBuilder =
            new SubmitDocumentRequestBuilder();
        ContentParser conParser = new ContentParser();
        MapperFactory mapFactory = new MapperFactory(conParser);

        XConfig xconfig = null;

        try {

            String configLoc = String.format("%s/test-resources",
                                             System.getProperty("user.dir"));

            System.setProperty(XConfig.SYSPROP_HIEOS_CONFIG_DIR, configLoc);

            xconfig = XConfig.getInstance();

        } catch (XConfigException e) {

            // refuse to deploy
            throw new IllegalStateException(e);
        }

        // TODO is this configurable???? pull from AxisService parameter?
        String repoName = "repo";
        XConfigObject homeCommunity = xconfig.getHomeCommunityConfig();
        XConfigActor repositoryActor =
            (XConfigActor) homeCommunity.getXConfigObjectWithName(repoName,
                XConfig.XDSB_DOCUMENT_REPOSITORY_TYPE);

        if (repositoryActor == null) {

            throw new IllegalStateException(
                String.format(
                    "Repository [%s] config is not found.", repoName));
        }

        XDSDocumentRepositoryClient repoClient =
            new XDSDocumentRepositoryClient(repositoryActor);

        // grab registry from repository actor stanza
        String regName = "registry";
        XConfigActor registryActor =
            (XConfigActor) repositoryActor.getXConfigObjectWithName(regName,
                XConfig.XDSB_DOCUMENT_REGISTRY_TYPE);

        if (registryActor == null) {

            throw new IllegalStateException(
                String.format("Registry [%s] config is not found.", regName));
        }

        XDSDocumentRegistryClient regClient =
            new XDSDocumentRegistryClient(registryActor);

        // set configuration for this service
        return new XDSBridgeConfig(mapFactory, sdrBuilder, regClient,
                                   repoClient);
    }
}
