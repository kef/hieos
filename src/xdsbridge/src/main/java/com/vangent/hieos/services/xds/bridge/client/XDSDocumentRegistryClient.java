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

import com.vangent.hieos.hl7v3util.model.message.MCCI_IN000002UV01_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201301UV02_Message;
import com.vangent.hieos.services.xds.bridge.message
    .GetDocumentsSQRequestMessage;
import com.vangent.hieos.services.xds.bridge.message
    .GetDocumentsSQResponseMessage;
import com.vangent.hieos.services.xds.bridge.support.XDSBridgeConfig;
import com.vangent.hieos.services.xds.bridge.utils.DebugUtils;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.soap.Soap;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigTransaction;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;


/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-09
 * @author         Jim Horner
 */
public class XDSDocumentRegistryClient extends AbstractClient {

    /** Field description */
    public static final String PID_ADD_REQUEST_ACTION =
        "urn:hl7-org:v3:PRPA_IN201301UV02";

    /** Field description */
    public static final String PID_ADD_RESPONSE_ACTION =
        "urn:hl7-org:v3:MCCI_IN000002UV01";

    /** Name of Transaction for service endpoints in xconfig.xml */
    public static final String PID_ADD_TRANS = "PatientIdentityFeed";

    /** Field description */
    public static final String STORED_QUERY_REQUEST_ACTION =
        "urn:ihe:iti:2007:RegistryStoredQuery";

    /** Field description */
    public static final String STORED_QUERY_RESPONSE_ACTION =
        "urn:ihe:iti:2007:RegistryStoredQueryResponse";

    /** Name of Transaction for service endpoints in xconfig.xml */
    public static final String STORED_QUERY_TRANS = "RegistryStoredQuery";

    /** The logger instance. */
    private static final Logger logger =
        Logger.getLogger(XDSDocumentRegistryClient.class);

    /**
     * Constructs ...
     *
     *
     *
     * @param xdsBridgeConfig
     * @param config
     */
    public XDSDocumentRegistryClient(XDSBridgeConfig xdsBridgeConfig,
                                     XConfigActor config) {
        super(xdsBridgeConfig, config);
    }

    /**
     * Method description
     *
     *
     *
     * @param request
     *
     * @return
     *
     * @throws AxisFault
     */
    public MCCI_IN000002UV01_Message addPatientIdentity(
            PRPA_IN201301UV02_Message request)
            throws AxisFault {

        MCCI_IN000002UV01_Message result = null;

        try {

            XConfigActor config = getConfig();
            XConfigTransaction pidAddTrans =
                config.getTransaction(PID_ADD_TRANS);
            String url = pidAddTrans.getEndpointURL();
            Soap soap = new Soap();

            soap.setAsync(pidAddTrans.isAsyncTransaction());

            boolean soap12 = pidAddTrans.isSOAP12Endpoint();
            boolean useMtom = false;
            boolean useWsa = soap12;

            if (logger.isDebugEnabled()) {

                logger.debug("== Sending to Registry");
                logger.debug(
                    DebugUtils.toPrettyString(request.getMessageNode()));
            }

            OMElement responseElem = soap.soapCall(request.getMessageNode(),
                                         url, useMtom, useWsa, soap12,
                                         PID_ADD_REQUEST_ACTION,
                                         PID_ADD_RESPONSE_ACTION);

            if (logger.isDebugEnabled()) {

                logger.debug("== Received from Registry");
                logger.debug(DebugUtils.toPrettyString(responseElem));
            }

            result = new MCCI_IN000002UV01_Message(responseElem);

        } catch (XdsException ex) {

            logger.error(ex, ex);

            throw new AxisFault(ex.getMessage(), ex);
        }

        return result;
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
    public GetDocumentsSQResponseMessage getDocuments(
            GetDocumentsSQRequestMessage request)
            throws AxisFault {

        GetDocumentsSQResponseMessage result = null;

        try {

            XConfigActor config = getConfig();
            XConfigTransaction sqTrans =
                config.getTransaction(STORED_QUERY_TRANS);
            String url = sqTrans.getEndpointURL();
            Soap soap = new Soap();

            soap.setAsync(sqTrans.isAsyncTransaction());

            boolean soap12 = sqTrans.isSOAP12Endpoint();
            boolean useMtom = false;
            boolean useWsa = soap12;

            if (logger.isDebugEnabled()) {

                logger.debug("== Sending to Registry");
                logger.debug(
                    DebugUtils.toPrettyString(request.getMessageNode()));
            }

            OMElement responseElem = soap.soapCall(request.getMessageNode(),
                                         url, useMtom, useWsa, soap12,
                                         STORED_QUERY_REQUEST_ACTION,
                                         STORED_QUERY_RESPONSE_ACTION);

            if (logger.isDebugEnabled()) {

                logger.debug("== Received from Registry");
                logger.debug(DebugUtils.toPrettyString(responseElem));
            }

            result = new GetDocumentsSQResponseMessage(responseElem);

        } catch (XdsException ex) {

            logger.error(ex, ex);

            throw new AxisFault(ex.getMessage(), ex);
        }

        return result;
    }
}
