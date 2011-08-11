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
package com.vangent.hieos.services.xds.registry.serviceimpl;

import com.vangent.hieos.xutil.exception.XdsValidationException;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.services.xds.registry.transactions.AdhocQueryRequest;
import com.vangent.hieos.xutil.services.framework.XAbstractService;
import com.vangent.hieos.services.xds.registry.transactions.SubmitObjectsRequest;
import com.vangent.hieos.services.xds.registry.transactions.RegistryPatientIdentityFeed;

import org.apache.log4j.Logger;
import org.apache.axis2.AxisFault;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;

// Axis2 LifeCycle support:
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;

import com.vangent.hieos.xutil.atna.XATNALogger;
import com.vangent.hieos.xutil.exception.SOAPFaultException;
import com.vangent.hieos.xutil.soap.SoapActionFactory;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;

/**
 *
 * @author Bernie Thuman
 */
public class XDSbRegistry extends XAbstractService {

    private final static Logger logger = Logger.getLogger(XDSbRegistry.class);
    private static XConfigActor config = null;  // Singleton.

    @Override
    protected XConfigActor getConfigActor() {
        return config;
    }

    /**
     *
     * @param sor
     * @return
     * @throws org.apache.axis2.AxisFault
     */
    public OMElement SubmitObjectsRequest(OMElement sor) throws AxisFault {
        OMElement response = null;
        try {
            long start = System.currentTimeMillis();
            beginTransaction(getRTransactionName(sor), sor);
            validateWS();
            validateNoMTOM();
            validateSubmitTransaction(sor);
            SubmitObjectsRequest s = new SubmitObjectsRequest(log_message, getMessageContext());
            s.setConfigActor(this.getConfigActor());
            response = s.run(sor);
            endTransaction(s.getStatus());
            if (logger.isDebugEnabled()) {
                logger.debug("SOR TOTAL TIME - " + (System.currentTimeMillis() - start) + "ms.");
            }
            //return endTransaction(sor, e, XAbstractService.ActorType.REGISTRY, "");
        } catch (SOAPFaultException ex) {
            throwAxisFault(ex);
        } catch (XdsValidationException ex) {
            response = endTransaction(sor, ex, XAbstractService.ActorType.REGISTRY, "");
        }
        return response;
    }

    /**
     *
     * @param ahqr
     * @return
     * @throws org.apache.axis2.AxisFault
     */
    public OMElement AdhocQueryRequest(OMElement ahqr) throws AxisFault {
        OMElement response = null;
        try {
            long start = System.currentTimeMillis();
            beginTransaction(getRTransactionName(ahqr), ahqr);
            validateWS();
            validateNoMTOM();
            AdhocQueryRequest a = new AdhocQueryRequest(log_message, getMessageContext());
            a.setConfigActor(this.getConfigActor());
            validateQueryTransaction(ahqr);
            //return endTransaction(ahqr, e, XAbstractService.ActorType.REGISTRY, "");
            a.setServiceName(this.getServiceName());
            a.setIsMPQRequest(this.isMPQRequest());
            response = a.run(ahqr);
            endTransaction(a.getStatus());
            if (logger.isDebugEnabled()) {
                logger.debug("ADHOC QUERY TOTAL TIME - " + (System.currentTimeMillis() - start) + "ms.");
            }
        } catch (SOAPFaultException ex) {
            throwAxisFault(ex);
        } catch (XdsValidationException ex) {
            response = endTransaction(ahqr, ex, XAbstractService.ActorType.REGISTRY, "");
        }
        return response;
    }

    /**
     * Processes Patient ID feeds for HL7 V2 messages
     * 
     * @param patientFeedRequest
     * @return
     * @throws AxisFault
     */
    public OMElement PatientFeedRequest(OMElement patientFeedRequest) throws AxisFault {
        OMElement response = null;
        try {
            beginTransaction("PIDFEED.V2", patientFeedRequest);
            logger.debug("*** PID Feed: SIMPLE ***");
            RegistryPatientIdentityFeed rpif = new RegistryPatientIdentityFeed(log_message);
            rpif.setConfigActor(this.getConfigActor());
            response = rpif.run_Simple(patientFeedRequest);
            endTransaction(rpif.getStatus());
        } catch (SOAPFaultException ex) {
            throwAxisFault(ex);
        }
        return response;
    }

    /**
     * Returns true if this is an MPQ SOAP request.  Otherwise, returns false.
     * @return Boolean result indicating MPQ SOAP request status.
     */
    private boolean isMPQRequest() {
        boolean isMPQ = false;
        String soapAction = this.getSOAPAction();
        if (soapAction.equals(SoapActionFactory.XDSB_REGISTRY_MPQ_ACTION)) {
            isMPQ = true;
        }
        return isMPQ;
    }

    // Added (BHT): Patient Identity Feed:
    /**
     * Patient Registry Record Added - HL7 V3
     *
     * @param PRPA_IN201301UV02_Message
     * @return
     * @throws org.apache.axis2.AxisFault
     */
    public OMElement DocumentRegistry_PRPA_IN201301UV02(OMElement PRPA_IN201301UV02_Message) throws AxisFault {
        OMElement response = null;
        try {
            beginTransaction("PIDFEED.Add", PRPA_IN201301UV02_Message);
            validateNoMTOM();

            logger.debug("*** PID Feed: Patient Registry Record Added ***");
            RegistryPatientIdentityFeed rpif = new RegistryPatientIdentityFeed(log_message);
            rpif.setConfigActor(this.getConfigActor());
            response = rpif.run(PRPA_IN201301UV02_Message, RegistryPatientIdentityFeed.MessageType.PatientRegistryRecordAdded);
            //this.forceAnonymousReply();  // BHT (FIXME)
            endTransaction(rpif.getStatus());
        } catch (SOAPFaultException ex) {
            throwAxisFault(ex);
        }
        return response;
    }

    /**
     * Patient Registry Record Updated - HL7 V3
     *
     * @param PRPA_IN201302UV02_Message
     * @return
     * @throws org.apache.axis2.AxisFault
     */
    public OMElement DocumentRegistry_PRPA_IN201302UV02(OMElement PRPA_IN201302UV02_Message) throws AxisFault {
        OMElement response = null;
        try {
            beginTransaction("PIDFEED.Update", PRPA_IN201302UV02_Message);
            validateNoMTOM();
            logger.debug("*** PID Feed: Patient Registry Record Updated ***");
            RegistryPatientIdentityFeed rpif = new RegistryPatientIdentityFeed(log_message);
            rpif.setConfigActor(this.getConfigActor());
            response = rpif.run(PRPA_IN201302UV02_Message, RegistryPatientIdentityFeed.MessageType.PatientRegistryRecordUpdated);
            //this.forceAnonymousReply();  // BHT (FIXME)
            endTransaction(rpif.getStatus());
        } catch (SOAPFaultException ex) {
            throwAxisFault(ex);
        }
        return response;
    }

    /**
     * Patient Registry Duplicates Resolved - HL7 V3
     *
     * @param PRPA_IN201304UV02_Message
     * @return
     * @throws org.apache.axis2.AxisFault
     */
    public OMElement DocumentRegistry_PRPA_IN201304UV02(OMElement PRPA_IN201304UV02_Message) throws AxisFault {
        OMElement response = null;
        try {
            beginTransaction("PIDFEED.Merge", PRPA_IN201304UV02_Message);
            validateNoMTOM();
            logger.debug("*** PID Feed: Patient Registry Duplicates Resolved (MERGE) ***");
            RegistryPatientIdentityFeed rpif = new RegistryPatientIdentityFeed(log_message);
            rpif.setConfigActor(this.getConfigActor());
            response = rpif.run(PRPA_IN201304UV02_Message, RegistryPatientIdentityFeed.MessageType.PatientRegistryDuplicatesResolved);
            //this.forceAnonymousReply();  // BHT (FIXME)
            endTransaction(rpif.getStatus());
        } catch (SOAPFaultException ex) {
            throwAxisFault(ex);
        }
        return response;
    }

    /**
     * Patient Registry Unmerge Record - HL7 V3
     *
     * @param PRPA_IN201304UV02UNMERGE_Message 
     * @return
     * @throws org.apache.axis2.AxisFault
     */
    public OMElement DocumentRegistry_PRPA_IN201304UV02UNMERGE(OMElement PRPA_IN201304UV02UNMERGE_Message) throws AxisFault {
        OMElement response = null;
        try {
            beginTransaction("PIDFEED.Unmerge", PRPA_IN201304UV02UNMERGE_Message);
            validateNoMTOM();

            logger.debug("*** PID Feed: Patient Registry Record Unmerged ***");
            RegistryPatientIdentityFeed rpif = new RegistryPatientIdentityFeed(log_message);
            rpif.setConfigActor(this.getConfigActor());
            response = rpif.run(PRPA_IN201304UV02UNMERGE_Message, RegistryPatientIdentityFeed.MessageType.PatientRegistryRecordUnmerged);
            //this.forceAnonymousReply();  // BHT (FIXME)
            endTransaction(rpif.getStatus());
        } catch (SOAPFaultException ex) {
            throwAxisFault(ex);
        }
        return response;
    }

    /**
     *
     * @param sor
     * @throws XdsValidationException
     */
    private void validateSubmitTransaction(OMElement sor)
            throws XdsValidationException {
        OMNamespace ns = sor.getNamespace();
        String ns_uri = ns.getNamespaceURI();
        if (ns_uri == null || !ns_uri.equals(MetadataSupport.ebLcm3.getNamespaceURI())) {
            throw new XdsValidationException("Invalid namespace on " + sor.getLocalName() + " (" + ns_uri + ")");
        }
        String type = getRTransactionName(sor);
        if (!type.startsWith("SubmitObjectsRequest")) {
            throw new XdsValidationException("Only SubmitObjectsRequest is acceptable on this endpoint, found " + sor.getLocalName());
        }
    }

    /**
     *
     * @param sor
     * @throws com.vangent.hieos.xutil.exception.XdsValidationException
     */
    private void validateQueryTransaction(OMElement sor)
            throws XdsValidationException {
        OMNamespace ns = sor.getNamespace();
        String ns_uri = ns.getNamespaceURI();
        if (ns_uri == null || !ns_uri.equals(MetadataSupport.ebQns3.getNamespaceURI())) {
            throw new XdsValidationException("Invalid namespace on " + sor.getLocalName() + " (" + ns_uri + ")");
        }
        //String type = getRTransactionName(sor);
        if (!this.isSQ(sor)) {
            throw new XdsValidationException("Only StoredQuery is acceptable on this endpoint");
        }
    }

    /**
     *
     * @param ahqr
     * @return
     */
    protected String getRTransactionName(OMElement ahqr) {
        OMElement ahq = MetadataSupport.firstChildWithLocalName(ahqr, "AdhocQuery");
        if (ahq != null) {
            return "SQ.b";
        } else if (ahqr.getLocalName().equals("SubmitObjectsRequest")) {
            return "SubmitObjectsRequest.b";
        } else {
            return "Unknown";
        }
    }

    /**
     *
     * @param ahqr
     * @return
     */
    private boolean isSQ(OMElement ahqr) {
        return MetadataSupport.firstChildWithLocalName(ahqr, "AdhocQuery") != null;
    }

    /**
     *
     */
    /*
    private void forceAnonymousReply() {
    // FIXME: Need to look at why we need to do this at all.
    EndpointReference epr = new EndpointReference("http://www.w3.org/2005/08/addressing/anonymous");
    try {
    this.getResponseMessageContext().setTo(epr);
    } catch (AxisFault ex) {
    logger.error("Unable to force anonymous reply", ex);
    }
    }
     */
    // BHT (ADDED Axis2 LifeCycle methods):
    /**
     * This will be called during the deployment time of the service.
     * Irrespective of the service scope this method will be called
     */
    @Override
    public void startUp(ConfigurationContext configctx, AxisService service) {
        logger.info("DocumentRegistry::startUp()");
        try {
            XConfig xconf = XConfig.getInstance();
            String xConfigName = (String) service.getParameter("XConfigName").getValue();
            config = xconf.getRegistryConfigByName(xConfigName);
        } catch (Exception ex) {
            logger.fatal("Unable to get configuration for service", ex);
        }
        this.ATNAlogStart(XATNALogger.ActorType.REGISTRY);
    }

    /**
     * This will be called during the system shut down time. Irrespective
     * of the service scope this method will be called
     */
    @Override
    public void shutDown(ConfigurationContext configctx, AxisService service) {
        logger.info("DocumentRegistry::shutDown()");
        this.ATNAlogStop(XATNALogger.ActorType.REGISTRY);
    }
}
