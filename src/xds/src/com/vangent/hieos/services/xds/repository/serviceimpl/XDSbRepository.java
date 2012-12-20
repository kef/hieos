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
package com.vangent.hieos.services.xds.repository.serviceimpl;

import com.vangent.hieos.xutil.exception.XdsValidationException;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.services.xds.repository.transactions.ProvideAndRegisterDocumentSet;
import com.vangent.hieos.services.xds.repository.transactions.RetrieveDocumentSet;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent;

import org.apache.axis2.AxisFault;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;

import org.apache.log4j.Logger;

import com.vangent.hieos.xutil.exception.SOAPFaultException;
import com.vangent.hieos.xutil.exception.SchemaValidationException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.services.framework.XAbstractService;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;

/**
 *
 * @author Bernie Thuman
 */
public class XDSbRepository extends XAbstractService {

    private final static Logger logger = Logger.getLogger(XDSbRepository.class);
    private static XConfigActor config = null;  // Singleton.

    @Override
    protected XConfigActor getConfigActor() {
        return config;
    }

    //String alternateRegistryEndpoint = null;
    /**
     *
     */
    public XDSbRepository() {
        super();
    }

    /**
     *
     * @param sor
     * @return
     * @throws org.apache.axis2.AxisFault
     */
    public OMElement SubmitObjectsRequest(OMElement sor) throws AxisFault {
        return ProvideAndRegisterDocumentSetRequest(sor);
    }

    /**
     *
     * @param sor
     * @return
     * @throws org.apache.axis2.AxisFault
     */
    public OMElement ProvideAndRegisterDocumentSetRequest(OMElement sor) throws AxisFault {
        OMElement response = null;
        try {
            long start = System.currentTimeMillis();
            beginTransaction(getPnRTransactionName(), sor);
            validateWS();
            validateMTOM();
            validatePnRTransaction(sor);
            ProvideAndRegisterDocumentSet s = new ProvideAndRegisterDocumentSet(log_message);
            s.setConfigActor(this.getConfigActor());
            response = s.run(sor);
            endTransaction(s.getStatus());
            if (logger.isDebugEnabled()) {
                logger.debug("PNR TOTAL TIME - " + (System.currentTimeMillis() - start) + "ms.");
            }
        } catch (SOAPFaultException ex) {
            throwAxisFault(ex);
        } catch (XdsValidationException e) {
            response = endTransaction(sor, e, XAbstractService.ActorType.REPOSITORY, "");
        }
        return response;
    }

    /**
     * 
     * @param rdsr
     * @return
     * @throws org.apache.axis2.AxisFault
     */
    public OMElement RetrieveDocumentSetRequest(OMElement rdsr) throws AxisFault {
        OMElement response = null;
        try {
            long start = System.currentTimeMillis();
            beginTransaction(getRetTransactionName(), rdsr);
            validateWS();
            validateMTOM();
            validateRetTransaction(rdsr);
            OMNamespace ns = rdsr.getNamespace();
            String ns_uri = ns.getNamespaceURI();
            if (ns_uri == null || !ns_uri.equals(MetadataSupport.xdsB.getNamespaceURI())) {
                OMElement res = this.start_up_error(rdsr, "AbstractRepository.java", XAbstractService.ActorType.REPOSITORY, "Invalid namespace on RetrieveDocumentSetRequest (" + ns_uri + ")", true);
                endTransaction(false);
                return res;
            }
            RetrieveDocumentSet s = new RetrieveDocumentSet(log_message);
            s.setConfigActor(this.getConfigActor());
            response = s.run(rdsr, true /* optimize */, this);
            endTransaction(s.getStatus());
            if (logger.isDebugEnabled()) {
                logger.debug("RETRIEVE DOC TOTAL TIME - " + (System.currentTimeMillis() - start) + "ms.");
            }
        } catch (SOAPFaultException ex) {
            throwAxisFault(ex);
        } catch (XdsValidationException ex) {
            response = endTransaction(rdsr, ex, XAbstractService.ActorType.REPOSITORY, "");
        } catch (SchemaValidationException ex) {
            response = endTransaction(rdsr, ex, XAbstractService.ActorType.REPOSITORY, "");
        } catch (XdsInternalException ex) {
            response = endTransaction(rdsr, ex, XAbstractService.ActorType.REPOSITORY, "");
        }
        return response;
    }

    /**
     *
     * @return
     */
    protected String getPnRTransactionName() {
        return "PnR.b";
    }

    /**
     *
     * @return
     */
    protected String getRetTransactionName() {
        return "RET.b";
    }

    private void validatePnRTransaction(OMElement sor) throws XdsValidationException {
        OMNamespace ns = sor.getNamespace();
        String ns_uri = ns.getNamespaceURI();
        if (ns_uri == null || !ns_uri.equals(MetadataSupport.xdsB.getNamespaceURI())) {
            throw new XdsValidationException("Invalid namespace on " + sor.getLocalName() + " (" + ns_uri + ")");
        }
    }

    private void validateRetTransaction(OMElement rds) throws XdsValidationException {
        OMNamespace ns = rds.getNamespace();
        String ns_uri = ns.getNamespaceURI();
        if (ns_uri == null || !ns_uri.equals(MetadataSupport.xdsB.getNamespaceURI())) {
            throw new XdsValidationException("Invalid namespace on " + rds.getLocalName() + " (" + ns_uri + ")");
        }
    }

    /**
     * This will be called during the deployment time of the service.
     * Irrespective of the service scope this method will be called
     */
    @Override
    public void startup() {
        logger.info("DocumentRepository::startup()");
        try {
            XConfig xconf;
            xconf = XConfig.getInstance();
            XConfigObject homeCommunity = xconf.getHomeCommunityConfig();
            config = (XConfigActor) homeCommunity.getXConfigObjectWithName("repo", XConfig.XDSB_DOCUMENT_REPOSITORY_TYPE);
        } catch (Exception ex) {
            logger.fatal("Unable to get configuration for service", ex);
        }
        this.ATNAlogStart(ATNAAuditEvent.ActorType.REPOSITORY);
    }

    /**
     * This will be called during the system shut down time. Irrespective
     * of the service scope this method will be called
     */
    @Override
    public void shutdown() {
        logger.info("DocumentRepository::shutdown()");
        this.ATNAlogStop(ATNAAuditEvent.ActorType.REPOSITORY);
    }
}
