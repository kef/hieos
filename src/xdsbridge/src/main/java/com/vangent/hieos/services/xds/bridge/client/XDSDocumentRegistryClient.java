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

    /** TODO fix this, needs to be in xconfig??? */
    public static final String PID_ADD_TRANS = "RegisterDocumentSet-b";

    /** The logger instance. */
    private static final Logger logger =
        Logger.getLogger(XDSDocumentRegistryClient.class);

    /**
     * Constructs ...
     *
     *
     * @param config
     */
    public XDSDocumentRegistryClient(XConfigActor config) {
        super(config);
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

            OMElement responseElem = soap.soapCall(request.getMessageNode(),
                                         url, useMtom, useWsa, soap12,
                                         PID_ADD_REQUEST_ACTION,
                                         PID_ADD_RESPONSE_ACTION);
            
            result = new MCCI_IN000002UV01_Message(responseElem);

        } catch (XdsException ex) {

            logger.error(ex, ex);

            throw new AxisFault(ex.getMessage(), ex);
        }

        return result;
    }
}
