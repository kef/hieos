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

import org.apache.axis2.AxisFault;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;

import org.apache.log4j.Logger;

// Axis2 LifeCycle support:
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;

import com.vangent.hieos.xutil.atna.XATNALogger;
import com.vangent.hieos.xutil.services.framework.XAbstractService;

public class XDSbRepository extends XAbstractService {

    private final static Logger logger = Logger.getLogger(XDSbRepository.class);

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
        long start = System.currentTimeMillis();
        try {
            OMElement startup_error = beginTransaction(getPnRTransactionName(), sor, XAbstractService.ActorType.REPOSITORY);
            if (startup_error != null) {
                return startup_error;
            }
            validateWS();
            validateMTOM();
            validatePnRTransaction(sor);
            ProvideAndRegisterDocumentSet s = new ProvideAndRegisterDocumentSet(log_message, getMessageContext());
            /*if (alternateRegistryEndpoint != null) {
            s.setRegistryEndPoint(alternateRegistryEndpoint);
            }*/
            OMElement result = s.provideAndRegisterDocumentSet(sor);
            endTransaction(s.getStatus());
            if (logger.isDebugEnabled()) {
                logger.debug("PNR TOTAL TIME - " + (System.currentTimeMillis() - start) + "ms.");
            }
            return result;
        } catch (Exception e) {
            return endTransaction(sor, e, XAbstractService.ActorType.REPOSITORY, "");
        }
    }

    /**
     * 
     * @param rdsr
     * @return
     * @throws org.apache.axis2.AxisFault
     */
    public OMElement RetrieveDocumentSetRequest(OMElement rdsr) throws AxisFault {
        long start = System.currentTimeMillis();
        try {
            OMElement startup_error = beginTransaction(getRetTransactionName(), rdsr, XAbstractService.ActorType.REPOSITORY);
            if (startup_error != null) {
                return startup_error;
            }
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
            RetrieveDocumentSet s = new RetrieveDocumentSet(log_message, getMessageContext());
            OMElement result = s.retrieveDocumentSet(rdsr, true /* optimize */, this);
            endTransaction(s.getStatus());
            if (logger.isDebugEnabled()) {
                logger.debug("RETRIEVE DOC TOTAL TIME - " + (System.currentTimeMillis() - start) + "ms.");
            }
            return result;
        } catch (Exception e) {
            return endTransaction(rdsr, e, XAbstractService.ActorType.REPOSITORY, "");
        }
    }

    /**
     *
     * @param endpoint
     */
    /*
    private void setAlternateRegistryEndpoint(String endpoint) {
    alternateRegistryEndpoint = endpoint;
    }*/
    /**
     *
     * @param opt
     */
    /*
    public void optimize_retrieve(boolean opt) {
    optimize_retrieve = opt;
    }*/
    protected String getPnRTransactionName() {
        return "PnR.b";
    }

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

    // BHT (ADDED Axis2 LifeCycle methods):
    /**
     * This will be called during the deployment time of the service.
     * Irrespective of the service scope this method will be called
     */
    @Override
    public void startUp(ConfigurationContext configctx, AxisService service) {
        logger.info("Repository::startUp()");
        this.ATNAlogStart(XATNALogger.ActorType.REPOSITORY);
    }

    /**
     * This will be called during the system shut down time. Irrespective
     * of the service scope this method will be called
     */
    @Override
    public void shutDown(ConfigurationContext configctx, AxisService service) {
        logger.info("RepositoryB::shutDown()");
        this.ATNAlogStop(XATNALogger.ActorType.REPOSITORY);
    }
}
