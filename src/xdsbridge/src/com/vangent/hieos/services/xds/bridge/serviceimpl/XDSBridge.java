/*
 * @(#)XDSBridge.java   2011-06-08
 *
 * Copyright (c) 2011
 *
 *
 *
 *
 */

package com.vangent.hieos.services.xds.bridge.serviceimpl;

import com.vangent.hieos.services.xds.bridge.client.XDSDocumentRegistryClient;
import com.vangent.hieos.services.xds.bridge.client.XDSDocumentRepositoryClient;
import com.vangent.hieos.services.xds.bridge.mapper.ContentParser;
import com.vangent.hieos.services.xds.bridge.mapper.MapperFactory;
import com.vangent.hieos.services.xds.bridge.model.SubmitDocumentRequestBuilder;
import com.vangent.hieos.services.xds.bridge.support.AbstractHandlerService;
import com.vangent.hieos.services.xds.bridge.support.IMessageHandler;
import com.vangent.hieos.services.xds.bridge.transactions
    .SubmitDocumentRequestHandler;
import com.vangent.hieos.services.xds.bridge.utils.DebugUtils;
import com.vangent.hieos.xutil.exception.XConfigException;
import com.vangent.hieos.xutil.exception.XdsValidationException;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.log4j.Logger;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-09
 * @author         Jim Horner
 */
public class XDSBridge extends AbstractHandlerService {

    /** Field description */
    private final static Logger logger = Logger.getLogger(XDSBridge.class);

    /** Field description */
    private static XDSBridgeConfig bridgeConfig;

    /**
     * Constructs ...
     *
     */
    public XDSBridge() {

        super(ActorType.DOCRECIPIENT);
    }

    /**
     * Constructs ...
     *
     *
     * @param config
     */
    protected XDSBridge(XDSBridgeConfig config) {

        this();
        XDSBridge.bridgeConfig = config;
    }

    /**
     * Method description
     *
     *
     * @param request
     *
     * @return
     *
     * @throws AxisFault
     */
    public OMElement SubmitDocumentRequest(OMElement request) throws AxisFault {

        // THIS IS NOT A CONSTRUCTOR
        // THIS IS NOT A CONSTRUCTOR
        // THIS IS NOT A CONSTRUCTOR
        // This method is named uncoventionally for the benefit of wsdl parsers
        // Axis2 will publish a "HIEOS convention" operation name

        return submitDocumentRequest(request);
    }

    /**
     * Method description
     *
     *
     * @param configctx
     * @param service
     */
    @Override
    public void shutDown(ConfigurationContext configctx, AxisService service) {

        super.shutDown(configctx, service);

        XDSBridge.bridgeConfig = null;
    }

    /**
     * Method description
     *
     *
     * @param configctx
     * @param service
     */
    @Override
    public void startUp(ConfigurationContext configctx, AxisService service) {

        super.startUp(configctx, service);

        // create wiring, check environment
        SubmitDocumentRequestBuilder sdrBuilder =
            new SubmitDocumentRequestBuilder();
        ContentParser conParser = new ContentParser();
        MapperFactory mapFactory = new MapperFactory(conParser);

        XConfig xconfig = null;

        try {

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
        XDSBridge.bridgeConfig = new XDSBridgeConfig(mapFactory, sdrBuilder,
                regClient, repoClient);
    }

    /**
     * Method description
     *
     *
     * @param request
     *
     * @return
     *
     * @throws AxisFault
     */
    private OMElement submitDocumentRequest(OMElement request)
            throws AxisFault {

        OMElement response = null;

        if (logger.isDebugEnabled()) {

            logger.debug("-=> Recevied");
            logger.debug(DebugUtils.toPrettyString(request));
        }

        SubmitDocumentRequestBuilder builder =
            bridgeConfig.getSubmitDocumentRequestBuilder();

        MapperFactory mapFact = bridgeConfig.getMapperFactory();

        XDSDocumentRegistryClient regClient = bridgeConfig.getRegistryClient();
        XDSDocumentRepositoryClient repoClient =
            bridgeConfig.getRepositoryClient();

        IMessageHandler handler =
            new SubmitDocumentRequestHandler(getLogMessage(), builder, mapFact,
                regClient, repoClient);

        response = handleMessage(request, handler);

        if (logger.isDebugEnabled()) {

            logger.debug("-=> Response");
            logger.debug(DebugUtils.toPrettyString(response));
        }

        return response;
    }

    /**
     * Method description
     *
     *
     *
     * @throws XdsValidationException
     */
    @Override
    protected void validate() throws XdsValidationException {

        try {

            validateWS();
            validateMTOM();

        } catch (Exception e) {

            // log error
            logger.error("Failed validation", e);

            // rethrow
            throw new XdsValidationException(e.getMessage(), e);
        }
    }
}
