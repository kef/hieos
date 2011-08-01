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

import com.vangent.hieos.services.xds.bridge.client.XDSDocumentRegistryClient;
import com.vangent.hieos.services.xds.bridge.client.XDSDocumentRepositoryClient;
import com.vangent.hieos.services.xds.bridge.support.XDSBridgeConfig;
import com.vangent.hieos.services.xds.bridge.support.XDSBridgeServiceContext;
import com.vangent.hieos.services.xds.bridge.transactions.SubmitDocumentRequestHandler;
import com.vangent.hieos.xutil.exception.SOAPFaultException;
import com.vangent.hieos.xutil.services.framework.XAbstractService;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;
import com.vangent.hieos.xutil.xconfig.XConfigTransaction;
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
 * @author         Vangent
 */
public class XDSBridge extends XAbstractService {

    /** Field description */
    private static final Logger logger = Logger.getLogger(XDSBridge.class);
    /** Field description */
    protected static XDSBridgeServiceContext serviceContext;

    /**
     * Constructs ...
     *
     */
    public XDSBridge() {
        super();
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
        try {
            beginTransaction("xdsbridge:SubmitDocumentRequest", request);
            validateWS();
            validateMTOM();

            SubmitDocumentRequestHandler handler =
                    new SubmitDocumentRequestHandler(this.log_message, serviceContext);

            handler.setConfigActor(this.getConfigActor());

            OMElement result = handler.run(getMessageContext(), request);

            endTransaction(handler.getStatus());

            return result;
        } catch (SOAPFaultException ex) {
            throw new AxisFault(ex.getMessage());
        }
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    protected XConfigActor getConfigActor() {

        return serviceContext.getXdsBridgeConfig().getXdsBridgeActor();
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
                (XConfigActor) homeCommunity.getXConfigObjectWithName(bridgeName,
                "XDSBridgeType");

        if (xdsBridgeActor == null) {

            throw new IllegalStateException(
                    String.format(
                    "XDSBridge [%s] config is not found.", bridgeName));
        }

        // grab repository from xdsbridge actor
        String repoName = "repo";
        XConfigActor repositoryActor =
                (XConfigActor) xdsBridgeActor.getXConfigObjectWithName("repo",
                XConfig.XDSB_DOCUMENT_REPOSITORY_TYPE);

        startUpValidateRepositoryActor(repoName, repositoryActor);

        // grab registry from xdsbrige actor
        String registryName = "registry";
        XConfigActor registryActor =
                (XConfigActor) xdsBridgeActor.getXConfigObjectWithName(
                registryName, XConfig.XDSB_DOCUMENT_REGISTRY_TYPE);

        startUpValidateRegistryActor(registryName, registryActor);

        XDSBridgeConfig bridgeConfig = null;

        try {

            bridgeConfig = XDSBridgeConfig.newInstance(xdsBridgeActor);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Unable to process xdsbridge config file.", e);
        }

        // create wiring

        // set context for this service
        XDSBridge.serviceContext = new XDSBridgeServiceContext(registryActor,
                repositoryActor, bridgeConfig);
    }

    /**
     * Method description
     *
     *
     * @param regName
     * @param registryActor
     */
    private void startUpValidateRegistryActor(String regName,
            XConfigActor registryActor) {

        if (registryActor == null) {

            throw new IllegalStateException(
                    String.format("Registry [%s] config is not found.", regName));
        }

        String[] registryTransactions =
                new String[]{XDSDocumentRegistryClient.PID_ADD_TRANS,
            XDSDocumentRegistryClient.STORED_QUERY_TRANS};

        for (String transName : registryTransactions) {

            XConfigTransaction trans = registryActor.getTransaction(transName);

            if (trans == null) {

                throw new IllegalStateException(
                        String.format(
                        "Registry [%s] config requires transation [%s].",
                        regName, transName));
            }
        }
    }

    /**
     * Method description
     *
     *
     * @param repoName
     * @param repositoryActor
     */
    private void startUpValidateRepositoryActor(String repoName,
            XConfigActor repositoryActor) {

        if (repositoryActor == null) {

            throw new IllegalStateException(
                    String.format(
                    "Repository [%s] config is not found.", repoName));
        }

        String[] repositoryTransactions =
                new String[]{XDSDocumentRepositoryClient.PNR_TRANS};

        for (String transName : repositoryTransactions) {

            XConfigTransaction trans =
                    repositoryActor.getTransaction(transName);

            if (trans == null) {

                throw new IllegalStateException(
                        String.format(
                        "Repository [%s] config requires transation [%s].",
                        repoName, transName));
            }
        }
    }
}
