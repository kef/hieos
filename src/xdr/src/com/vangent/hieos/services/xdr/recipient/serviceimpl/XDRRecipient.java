/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2008-2009 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.xdr.recipient.serviceimpl;

import com.vangent.hieos.xutil.exception.XdsValidationException;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.services.xdr.recipient.transactions.ProcessXDRPackage;

import org.apache.axis2.AxisFault;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;

import org.apache.log4j.Logger;

// Axis2 LifeCycle support:
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;

import com.vangent.hieos.xutil.atna.XATNALogger;
import com.vangent.hieos.xutil.services.framework.XAbstractService;

/**
 * Processes XDR Transactions 
 *
 * @author Adeola Odunlami
 */
public class XDRRecipient extends XAbstractService {

    private final static Logger logger = Logger.getLogger(XDRRecipient.class);


    /**
     *
     */
    public XDRRecipient() {
        super();
    }

    /**
     * Parse and Process the XDR Request
     * @param xdr
     * @return
     * @throws org.apache.axis2.AxisFault
     */
    public OMElement XDRDocRecipientRequest(OMElement xdr) throws AxisFault {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("XDR Request Received: " + xdr.toString());
            }
            OMElement startup_error = beginTransaction(getXDRTransactionName(), xdr, XAbstractService.ActorType.DOCRECIPIENT);
            if (startup_error != null) {
                logger.info("Begin Trans Error: ");
                return startup_error;
            }

            // Validate the XDR package
            validateWS();
            validateMTOM();
            validateXDRTransaction(xdr);

            // Process the Request
            ProcessXDRPackage s = new ProcessXDRPackage(log_message, getMessageContext());
            OMElement result = s.processXDRPackage(xdr);

            endTransaction(s.getStatus());
            return result;
        } catch (Exception e) {
            return endTransaction(xdr, e, XAbstractService.ActorType.DOCRECIPIENT, "");
        }
    }

    protected String getXDRTransactionName() {
        return "XDRRecipient";
    }

    /**
     * Checks the XDR request contains as XDS.b transaction
     * @param xdr
     * @throws XdsValidationException
     */
    private void validateXDRTransaction(OMElement xdr) throws XdsValidationException {
        OMNamespace ns = xdr.getNamespace();
        String ns_uri = ns.getNamespaceURI();
        if (ns_uri == null || !ns_uri.equals(MetadataSupport.xdsB.getNamespaceURI())) {
            throw new XdsValidationException("Invalid namespace on " + xdr.getLocalName() + " (" + ns_uri + ")");
        }
    }

    /**
     * This will be called during the deployment time of the service.
     * Irrespective of the service scope this method will be called
     */
    @Override
    public void startUp(ConfigurationContext configctx, AxisService service) {
        logger.info("XDRRecipient::startUp()");
        this.ATNAlogStart(XATNALogger.ActorType.DOCRECIPIENT);
    }

    /**
     * This will be called during the system shut down time. Irrespective
     * of the service scope this method will be called
     */
    @Override
    public void shutDown(ConfigurationContext configctx, AxisService service) {
        logger.info("XDRRecipient::shutDown()");
        this.ATNAlogStop(XATNALogger.ActorType.DOCRECIPIENT);
    }
}
