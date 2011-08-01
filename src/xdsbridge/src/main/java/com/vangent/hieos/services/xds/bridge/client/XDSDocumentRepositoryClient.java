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

package com.vangent.hieos.services.xds.bridge.client;

import com.vangent.hieos.services.xds.bridge.message.XDSPnRMessage;
import com.vangent.hieos.services.xds.bridge.support.XDSBridgeConfig;
import com.vangent.hieos.services.xds.bridge.utils.DebugUtils;
import com.vangent.hieos.xutil.exception.SOAPFaultException;
import com.vangent.hieos.xutil.soap.Soap;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigTransaction;
import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-09
 * @author         Jim Horner
 */
public class XDSDocumentRepositoryClient extends AbstractClient {

    /** Field description */
    public static final String PNR_REQUEST_ACTION =
        "urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-b";

    /** Field description */
    public static final String PNR_RESPONSE_ACTION =
        "urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-bResponse";

    /** Field description */
    public static final String PNR_TRANS = "ProvideAndRegisterDocumentSet-b";

    /** Field description */
    private static final Logger logger =
        Logger.getLogger(XDSDocumentRepositoryClient.class);

    /**
     * Constructs ...
     *
     *
     *
     * @param xdsBridgeConfig
     * @param config
     */
    public XDSDocumentRepositoryClient(XDSBridgeConfig xdsBridgeConfig,
                                       XConfigActor config) {
        super(xdsBridgeConfig, config);
    }

    /**
     * Method description
     *
     *
     * @param request
     *
     * @return
     *
     * @throws SOAPFaultException
     */
    public OMElement submitProvideAndRegisterDocumentSet(XDSPnRMessage request)
            throws SOAPFaultException {

        OMElement result = null;

            XConfigActor config = getConfig();
            XConfigTransaction pnrTrans = config.getTransaction(PNR_TRANS);
            String url = pnrTrans.getEndpointURL();

            Soap soap = new Soap();

            soap.setAsync(pnrTrans.isAsyncTransaction());

            boolean soap12 = pnrTrans.isSOAP12Endpoint();
            boolean useMtom = true;
            boolean useWsa = soap12;

            if (logger.isDebugEnabled()) {
                logger.debug("== Sending to Repository");
                logger.debug(
                    DebugUtils.toPrettyString(request.getElement()));
            }

            result = soap.soapCall(request.getElement(), url, useMtom,
                                   useWsa, soap12, PNR_REQUEST_ACTION,
                                   PNR_RESPONSE_ACTION);

            if (logger.isDebugEnabled()) {
                logger.debug("== Received from Repository");
                logger.debug(DebugUtils.toPrettyString(result));
            }

        return result;
    }
}
