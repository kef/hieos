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
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;

// Axis2 LifeCycle support:
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;

import com.vangent.hieos.xutil.atna.XATNALogger;
import com.vangent.hieos.xutil.soap.SoapActionFactory;

public class XDSbRegistry extends XAbstractService {

    private final static Logger logger = Logger.getLogger(XDSbRegistry.class);

    /**
     *
     * @param sor
     * @return
     * @throws org.apache.axis2.AxisFault
     */
    public OMElement SubmitObjectsRequest(OMElement sor) throws AxisFault {
        long start = System.currentTimeMillis();
        try {
            OMElement startup_error = beginTransaction(getRTransactionName(sor), sor, XAbstractService.ActorType.REGISTRY);
            if (startup_error != null) {
                return startup_error;
            }
            validateWS();
            validateNoMTOM();
            validateSubmitTransaction(sor);
            SubmitObjectsRequest s = new SubmitObjectsRequest(log_message, getMessageContext());
            OMElement result = s.submitObjectsRequest(sor);
            endTransaction(s.getStatus());
            if (logger.isDebugEnabled()) {
                logger.debug("SOR TOTAL TIME - " + (System.currentTimeMillis() - start) + "ms.");
            }
            return result;
        } catch (Exception e) {
            return endTransaction(sor, e, XAbstractService.ActorType.REGISTRY, "");
        }
    }

    /**
     *
     * @param ahqr
     * @return
     * @throws org.apache.axis2.AxisFault
     */
    public OMElement AdhocQueryRequest(OMElement ahqr) throws AxisFault {
        long start = System.currentTimeMillis();
        OMElement startup_error = beginTransaction(getRTransactionName(ahqr), ahqr, XAbstractService.ActorType.REGISTRY);
        if (startup_error != null) {
            return startup_error;
        }
        AdhocQueryRequest a = new AdhocQueryRequest(this.getRegistryXConfigName(), log_message, getMessageContext());
        try {
            validateWS();
            validateNoMTOM();
            validateQueryTransaction(ahqr);
        } catch (Exception e) {
            return endTransaction(ahqr, e, XAbstractService.ActorType.REGISTRY, "");
        }
        a.setServiceName(this.getServiceName());
        a.setIsMPQRequest(this.isMPQRequest());
        OMElement result = a.adhocQueryRequest(ahqr);
        endTransaction(a.getStatus());
        if (logger.isDebugEnabled()) {
            logger.debug("ADHOC QUERY TOTAL TIME - " + (System.currentTimeMillis() - start) + "ms.");
        }
        return result;
    }

    /**
     * Processes Patient ID feeds for HL7 V2 messages
     * 
     * @param patientFeedRequest
     * @return
     * @throws AxisFault
     */
    public OMElement PatientFeedRequest(OMElement patientFeedRequest) throws AxisFault {
        OMElement startup_error = beginTransaction("PIDFEED.V2", patientFeedRequest, XAbstractService.ActorType.REGISTRY);
        if (startup_error != null) {
            return startup_error;
        }
        logger.debug("*** PID Feed: SIMPLE ***");
        logger.debug("*** XConfig Registry Name = " + this.getRegistryXConfigName());
        RegistryPatientIdentityFeed rpif = new RegistryPatientIdentityFeed(this.getRegistryXConfigName(), log_message);
        OMElement result = rpif.run_Simple(patientFeedRequest);
        endTransaction(rpif.getStatus());
        return result;
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
        OMElement startup_error = beginTransaction("PIDFEED.Add", PRPA_IN201301UV02_Message, XAbstractService.ActorType.REGISTRY);
        if (startup_error != null) {
            return startup_error;
        }

        try {
            //validateWS();
            validateNoMTOM();
        } catch (Exception e) {
            logger.error("ERROR VALIDATING ADD PATIENT REQUEST");
            return endTransaction(PRPA_IN201301UV02_Message, e, XAbstractService.ActorType.REGISTRY, "");
        }

        logger.debug("*** PID Feed: Patient Registry Record Added ***");
        logger.debug("*** XConfig Registry Name = " + this.getRegistryXConfigName());
        RegistryPatientIdentityFeed rpif = new RegistryPatientIdentityFeed(this.getRegistryXConfigName(), log_message);
        OMElement result = rpif.run(PRPA_IN201301UV02_Message, RegistryPatientIdentityFeed.MessageType.PatientRegistryRecordAdded);
        this.forceAnonymousReply();  // BHT (FIXME)
        endTransaction(rpif.getStatus());
        return result;
    }

    /**
     * Patient Registry Record Updated - HL7 V3
     *
     * @param PRPA_IN201302UV02_Message
     * @return
     * @throws org.apache.axis2.AxisFault
     */
    public OMElement DocumentRegistry_PRPA_IN201302UV02(OMElement PRPA_IN201302UV02_Message) throws AxisFault {
        OMElement startup_error = beginTransaction("PIDFEED.Update", PRPA_IN201302UV02_Message, XAbstractService.ActorType.REGISTRY);
        if (startup_error != null) {
            return startup_error;
        }

        try {
            //validateWS();
            validateNoMTOM();
        } catch (Exception e) {
            logger.error("ERROR VALIDATING UPDATE PATIENT REQUEST");
            return endTransaction(PRPA_IN201302UV02_Message, e, XAbstractService.ActorType.REGISTRY, "");
        }

        logger.debug("*** PID Feed: Patient Registry Record Updated ***");
        logger.debug("*** XConfig Registry Name = " + this.getRegistryXConfigName());
        RegistryPatientIdentityFeed rpif = new RegistryPatientIdentityFeed(this.getRegistryXConfigName(), log_message);
        OMElement result = rpif.run(PRPA_IN201302UV02_Message, RegistryPatientIdentityFeed.MessageType.PatientRegistryRecordUpdated);
        this.forceAnonymousReply();  // BHT (FIXME)
        endTransaction(rpif.getStatus());
        return result;
    }

    /**
     * Patient Registry Duplicates Resolved - HL7 V3
     *
     * @param PRPA_IN201304UV02_Message
     * @return
     * @throws org.apache.axis2.AxisFault
     */
    public OMElement DocumentRegistry_PRPA_IN201304UV02(OMElement PRPA_IN201304UV02_Message) throws AxisFault {
        OMElement startup_error = beginTransaction("PIDFEED.Merge", PRPA_IN201304UV02_Message, XAbstractService.ActorType.REGISTRY);
        if (startup_error != null) {
            return startup_error;
        }

        try {
            //validateWS();
            validateNoMTOM();
        } catch (Exception e) {
            logger.error("ERROR VALIDATING MERGE PATIENT REQUEST");
            return endTransaction(PRPA_IN201304UV02_Message, e, XAbstractService.ActorType.REGISTRY, "");
        }

        logger.debug("*** PID Feed: Patient Registry Duplicates Resolved (MERGE) ***");
        logger.debug("*** XConfig Registry Name = " + this.getRegistryXConfigName());
        RegistryPatientIdentityFeed rpif = new RegistryPatientIdentityFeed(this.getRegistryXConfigName(), log_message);
        OMElement result = rpif.run(PRPA_IN201304UV02_Message, RegistryPatientIdentityFeed.MessageType.PatientRegistryDuplicatesResolved);
        this.forceAnonymousReply();  // BHT (FIXME)
        endTransaction(rpif.getStatus());
        return result;
    }

    /**
     * Patient Registry Unmerge Record - HL7 V3
     *
     * @param PRPA_IN201304UV02_Message
     * @return
     * @throws org.apache.axis2.AxisFault
     */
    public OMElement DocumentRegistry_PRPA_IN201304UV02UNMERGE(OMElement PRPA_IN201304UV02UNMERGE_Message) throws AxisFault {
        OMElement startup_error = beginTransaction("PIDFEED.Unmerge", PRPA_IN201304UV02UNMERGE_Message, XAbstractService.ActorType.REGISTRY);
        if (startup_error != null) {
            return startup_error;
        }

        try {
            //validateWS();
            validateNoMTOM();
        } catch (Exception e) {
            logger.error("ERROR VALIDATING UNMERGE PATIENT REQUEST");
            return endTransaction(PRPA_IN201304UV02UNMERGE_Message, e, XAbstractService.ActorType.REGISTRY, "");
        }

        logger.debug("*** PID Feed: Patient Registry Record Unmerged ***");
        logger.debug("*** XConfig Registry Name = " + this.getRegistryXConfigName());
        RegistryPatientIdentityFeed rpif = new RegistryPatientIdentityFeed(this.getRegistryXConfigName(), log_message);
        OMElement result = rpif.run(PRPA_IN201304UV02UNMERGE_Message, RegistryPatientIdentityFeed.MessageType.PatientRegistryRecordUnmerged);
        this.forceAnonymousReply();  // BHT (FIXME)
        endTransaction(rpif.getStatus());
        return result;
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
        String type = getRTransactionName(sor);
        if (!this.isSQ(sor)) {
            throw new XdsValidationException("Only StoredQuery is acceptable on this endpoint");
        }
        //new StoredQueryRequestSoapValidator(getXdsVersion(), getMessageContext()).runWithException();
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
     * @param ahqr
     * @return
     */
    /*
    private boolean isSQL(OMElement ahqr) {
    return MetadataSupport.firstChildWithLocalName(ahqr, "SQLQuery") != null;
    }*/
    /**
     *
     */
    private void forceAnonymousReply() {
        // FIXME: Need to look at why we need to do this at all.
        EndpointReference epr = new EndpointReference("http://www.w3.org/2005/08/addressing/anonymous");
        try {
            this.getResponseMessageContext().setTo(epr);
        } catch (AxisFault ex) {
            logger.error("Unable to force anonymous reply", ex);
        }
    }

    /**
     * Return the name of the registry in XConfig.
     *
     * @return  The logical name stored in XConfig that represents the registry.
     */
    private String getRegistryXConfigName() {
        return (String) this.getMessageContext().getParameter("XConfigName").getValue();
    }

    // BHT (ADDED Axis2 LifeCycle methods):
    /**
     * This will be called during the deployment time of the service.
     * Irrespective of the service scope this method will be called
     */
    @Override
    public void startUp(ConfigurationContext configctx, AxisService service) {
        logger.info("RegistryB::startUp()");
        this.ATNAlogStart(XATNALogger.ActorType.REGISTRY);
    }

    /**
     * This will be called during the system shut down time. Irrespective
     * of the service scope this method will be called
     */
    @Override
    public void shutDown(ConfigurationContext configctx, AxisService service) {
        logger.info("RegistryB::shutDown()");
        this.ATNAlogStop(XATNALogger.ActorType.REGISTRY);
    }
}
