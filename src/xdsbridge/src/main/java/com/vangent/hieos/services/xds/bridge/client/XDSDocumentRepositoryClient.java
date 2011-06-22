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

import com.vangent.hieos.hl7v3util.client.Client;
import com.vangent.hieos.services.xds.bridge.model.XDSPnR;
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
public class XDSDocumentRepositoryClient extends Client {

    /** Field description */
    private final static String PNR_REQUEST_ACTION =
        "urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-b";

    /** Field description */
    private final static String PNR_RESPONSE_ACTION =
        "urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-bResponse";

    /** Field description */
    private final static String PNR_TRANS = "ProvideAndRegisterDocumentSet-b";

    /** Field description */
    private final static Logger logger =
        Logger.getLogger(XDSDocumentRepositoryClient.class);

    /**
     * Constructs ...
     *
     *
     * @param config
     */
    public XDSDocumentRepositoryClient(XConfigActor config) {
        super(config);
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
    public OMElement submitProvideAndRegisterDocumentSet(XDSPnR request)
            throws AxisFault {

        // TODO Copied from hl7v3util/Validate against schema??

        OMElement result = null;

        Soap soap = new Soap();

        try {

            XConfigActor config = getConfig();
            XConfigTransaction pnrTrans = config.getTransaction(PNR_TRANS);

            soap.setAsync(pnrTrans.isAsyncTransaction());

            boolean soap12 = pnrTrans.isSOAP12Endpoint();
            boolean useMtom = true;
            boolean useWsa = true;

            result = soap.soapCall(request.getNode(),
                                   pnrTrans.getEndpointURL(), useMtom, useWsa,
                                   soap12, PNR_REQUEST_ACTION,
                                   PNR_RESPONSE_ACTION);

        } catch (XdsException ex) {

            logger.error(ex, ex);

            throw new AxisFault(ex.getMessage(), ex);
        }

        return result;
    }
}