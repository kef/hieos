/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2011 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vangent.hieos.services.xds.bridge.serviceimpl;

import com.vangent.hieos.services.xds.bridge.support.AbstractHandlerService;
import com.vangent.hieos.services.xds.bridge.support.IMessageHandler;
import com.vangent.hieos.services.xds.bridge.transactions
    .SubmitDocumentRequestHandler;
import com.vangent.hieos.services.xds.bridge.utils.DebugUtils;
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
    private static final Logger logger = Logger.getLogger(XDSBridge.class);

    /** Field description */
    protected static XDSBridgeServiceContext serviceContext;

    /**
     * Constructs ...
     *
     */
    public XDSBridge() {

        super(ActorType.DOCRECIPIENT);
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

        XDSBridge.serviceContext = null;
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

        // check environment

        XConfig xconfig = null;

        try {

            xconfig = XConfig.getInstance();

        } catch (Exception e) {

            // refuse to deploy
            throw new IllegalStateException(e);
        }

        XConfigObject homeCommunity = xconfig.getHomeCommunityConfig();

        String bridgeName = "xdsbridge";
        XConfigActor xdsBridgeActor =
            (XConfigActor) homeCommunity.getXConfigObjectWithName("xdsbridge",
                "XDSBridgeType");

        if (xdsBridgeActor == null) {

            throw new IllegalStateException(
                String.format(
                    "XDSBridge [%s] config is not found.", bridgeName));
        }

        // grab repository from xdsbridge actor
        String repoName = "repo";
        XConfigActor repositoryActor =
            (XConfigActor) homeCommunity.getXConfigObjectWithName("repo",
                XConfig.XDSB_DOCUMENT_REPOSITORY_TYPE);

        if (repositoryActor == null) {

            throw new IllegalStateException(
                String.format(
                    "Repository [%s] config is not found.", repoName));
        }

        // grab registry from xdsbrige actor
        String regName = "registry";
        XConfigActor registryActor =
            (XConfigActor) xdsBridgeActor.getXConfigObjectWithName(regName,
                XConfig.XDSB_DOCUMENT_REGISTRY_TYPE);

        if (registryActor == null) {

            throw new IllegalStateException(
                String.format("Registry [%s] config is not found.", regName));
        }

        XDSBridgeConfig bridgeConfig = null;

        try {

            bridgeConfig = XDSBridgeConfig.parseConfigFile(xdsBridgeActor);

        } catch (Exception e) {

            throw new IllegalStateException(
                "Unable to process xdsbridge config file.", e);
        }

        // create wiring

        // set context for this service
        XDSBridge.serviceContext = new XDSBridgeServiceContext(
                registryActor, repositoryActor, bridgeConfig);
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

        IMessageHandler handler =
            new SubmitDocumentRequestHandler(getLogMessage(), serviceContext);

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
