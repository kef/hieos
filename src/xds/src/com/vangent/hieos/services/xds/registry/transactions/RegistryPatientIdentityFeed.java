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
package com.vangent.hieos.services.xds.registry.transactions;

import com.vangent.hieos.services.xds.registry.storedquery.PatientIdentityFeedRegistryStoredQuerySupport;
import com.vangent.hieos.xutil.db.support.SQLConnectionWrapper;
import com.vangent.hieos.xutil.services.framework.XBaseTransaction;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import com.vangent.hieos.adt.db.AdtRecordBean;
import com.vangent.hieos.adt.db.AdtJdbcConnection;
import com.vangent.hieos.xutil.hl7.date.Hl7Date;
import com.vangent.hieos.xutil.exception.ExceptionUtil;
import com.vangent.hieos.xutil.uuid.UuidAllocator;

// XConfig.
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigRegistry;

// ATNA.
import com.vangent.hieos.xutil.atna.XATNALogger;

// Third-party.
import javax.xml.namespace.QName;
import org.apache.log4j.Logger;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.binary.Base64;

// Exceptions.
import com.vangent.hieos.xutil.exception.XdsInternalException;
import org.jaxen.JaxenException;
import java.sql.SQLException;

/**
 *
 * @author Bernie Thuman
 */
public class RegistryPatientIdentityFeed extends XBaseTransaction {

    private final static Logger logger = Logger.getLogger(RegistryPatientIdentityFeed.class);

    // Type type of message received.
    public enum MessageType {

        PatientRegistryRecordAdded,
        PatientRegistryRecordUpdated,
        PatientRegistryDuplicatesResolved,
        PatientRegistryRecordUnmerged
    };
    // XPath expressions:
    private final static String XPATH_PATIENT =
            "//*/ns:controlActProcess/ns:subject/ns:registrationEvent/ns:subject1/ns:patient[1]";
    private final static String XPATH_PRIOR_REGISTRATION_PATIENT_ID =
            "//*/ns:controlActProcess/ns:subject/ns:registrationEvent/ns:replacementOf/ns:priorRegistration/ns:subject1/ns:priorRegisteredRole/ns:id[1]";
    private final static String XPATH_PRIOR_REGISTRATION_PATIENT_ALL_IDS =
            "//*/ns:controlActProcess/ns:subject/ns:registrationEvent/ns:replacementOf/ns:priorRegistration/ns:subject1/ns:priorRegisteredRole/ns:id";
    private String xconfRegistryName = "localregistry";
    private boolean errorDetected = false;
    private String _patientId = "NOT ON REQUEST";
    private AdtJdbcConnection _adtConn = null;

    /**
     *
     * @param log_message
     */
    public RegistryPatientIdentityFeed(String xconfRegistryName, XLogMessage log_message) {
        this.log_message = log_message;
        this.xconfRegistryName = xconfRegistryName;
    }

    /**
     * Processs Patient ID Feeds for HL7 v3 messages
     *
     * @param request
     * @param messageType
     * @return
     */
    public OMElement run(OMElement request, MessageType messageType) {
        OMElement result = null;
        Exception ex = null;
        boolean updateMode = true;
        try {
            _adtConn = this.adtGetDatabaseConnection();  // Get ADT connection.
            switch (messageType) {
                case PatientRegistryRecordAdded:
                    updateMode = false;
                    this.processPatientRegistryRecordAdded(request);
                    break;
                case PatientRegistryRecordUpdated:
                    this.processPatientRegistryRecordUpdated(request);
                    break;
                case PatientRegistryDuplicatesResolved:
                    this.processPatientRegistyDuplicatesResolved(request);
                    break;
                case PatientRegistryRecordUnmerged:
                    this.processPatientRegistryRecordUnmerged(request);
                    break;
            }
            //this.logResponse(result, !this.errorDetected /* success */);
        } catch (PatientIdentityFeedException feedException) {
            ex = feedException;
        } catch (XdsInternalException internalException) {
            ex = internalException;
        } catch (Exception e) {
            ex = e;
            this.logException(e.getMessage());  // Some lower level exception.
        } finally {
            if (_adtConn != null) {
                _adtConn.closeConnection();
            }
        }

        // Generate the response.
        result = this.generateACK(request, (ex != null) ? ex.getMessage() : null);
        this.logResponse(result, !this.errorDetected /* success */);

        // ATNA log (Start)
        OMElement idNode = this.getFirstChildWithName(request, "id");
        String messageId = (idNode != null) ? idNode.getAttributeValue(new QName("root")) : "UNKNOWN";
        this.logPatientIdentityFeedToATNA(
                XATNALogger.TXN_ITI44,
                this._patientId,
                (messageId != null) ? messageId : "UNKNOWN",
                updateMode /* updateMode */,
                this.errorDetected ? XATNALogger.OutcomeIndicator.MINOR_FAILURE : XATNALogger.OutcomeIndicator.SUCCESS,
                null /* sourceIdentity */,
                null /* sourceIP */);
        // ATNA log (Stop)
        return result;
    }

    /**
     * Processs Patient ID Feeds for HL7 v2 messages
     * @param request
     * @param messageType
     * @return
     */
    public OMElement run_Simple(OMElement patientFeedRequest) {
        OMElement result = null;
        Exception ex = null;
        if (log_message.isLogEnabled()) {
            // Log the raw message.
            OMElement rawMessageNode = this.getFirstChildWithName(patientFeedRequest, "RawMessage");
            if (rawMessageNode != null) {
                byte[] base64 = Base64.decodeBase64(rawMessageNode.getText().getBytes());
                this.logInfo("Raw Message", new String(base64));
            }
            OMElement sourceIPNode = this.getFirstChildWithName(patientFeedRequest, "SourceIPAddress");
            String sourceIP = sourceIPNode.getText();
            this.logInfo("Source IP", sourceIP);
        }

        // First determine what kind of request we have.
        OMElement actionNode = this.getFirstChildWithName(patientFeedRequest, "Action");
        String action = actionNode.getText().toUpperCase();
        if (log_message.isLogEnabled()) {
            String logServiceText = log_message.getTestMessage() + "-" + action;
            log_message.setTestMessage(logServiceText);
        }
        boolean updateMode = true;
        try {
            _adtConn = this.adtGetDatabaseConnection();  // Get ADT connection.
            if (action.equals("ADD")) {
                updateMode = false;
                this.processPatientRegistryRecordAdded_Simple(patientFeedRequest);
            } else if (action.equals("MERGE")) {
                this.processPatientRegistyDuplicatesResolved_Simple(patientFeedRequest);
            } else {
                throw new PatientIdentityFeedException("Action not known");
            }
        } catch (PatientIdentityFeedException feedException) {
            ex = feedException;
        } catch (XdsInternalException internalException) {
            ex = internalException;
        } catch (Exception e) {
            ex = e;
            this.logException(e.getMessage());  // Some lower level exception.
        } finally {
            if (_adtConn != null) {
                _adtConn.closeConnection();
            }
        }
        /* FIXME */
        result = patientFeedRequest;
        this.logResponse(null /* response */, !this.errorDetected /* success */);

        // Generate the response.
        result = this.createPatientFeedResponse_Simple((ex != null) ? ex.getMessage() : null);
        this.logResponse(result, !this.errorDetected /* success */);

        // ATNA log (Start)
        String messageId = this.getPatientFeedRequestNodeText(patientFeedRequest, "MessageControlId");
        this.logPatientIdentityFeedToATNA(
                XATNALogger.TXN_ITI8,
                this._patientId,
                (messageId != null) ? messageId : "UNKNOWN",
                updateMode /* updateMode */,
                this.errorDetected ? XATNALogger.OutcomeIndicator.MINOR_FAILURE : XATNALogger.OutcomeIndicator.SUCCESS,
                this.getPatientFeedRequestNodeText(patientFeedRequest, "SourceIdentity") /* sourceIdentity */,
                this.getPatientFeedRequestNodeText(patientFeedRequest, "SourceIPAddress") /* sourceIP */);
        // ATNA log (Stop)
        return result;
    }

    /**
     *
     * @param PRPA_IN201301UV02_Message
     * @return
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    private void processPatientRegistryRecordAdded(OMElement PRPA_IN201301UV02_Message)
            throws PatientIdentityFeedException, XdsInternalException {
        // Pull out the patient from the request.
        OMElement patientNode = this.selectSingleNode(PRPA_IN201301UV02_Message, RegistryPatientIdentityFeed.XPATH_PATIENT);
        OMElement idNode = this.getFirstChildWithName(patientNode, "id");
        if (idNode != null) {
            this._patientId = this.getPatientIdFromIIType(idNode);
            this.logInfo("Patient ID", this._patientId);

            // First see if the patient id already exists.
            if (!this.adtPatientExists(this._patientId)) {
                this.adtAddPatientId(this._patientId);
            } else {
                // Patient Id already exists (ignore request).
                throw this.logException("Patient ID " + this._patientId + " already exists - skipping ADD!");
            }
        } else {
            throw this.logException("No patient id found on request");
        }
    }

    /**
     *
     * @param patientFeedRequest
     * @return
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    private void processPatientRegistryRecordAdded_Simple(OMElement patientFeedRequest)
            throws PatientIdentityFeedException, XdsInternalException {
        // Pull out the patient from the request.
        String id = this.getPatientFeedRequestNodeText(patientFeedRequest, "PatientId");
        if (id != null) {
            this._patientId = id;
            this.logInfo("Patient ID", this._patientId);

            // First see if the patient id already exists.
            if (!this.adtPatientExists(this._patientId)) {
                this.adtAddPatientId(this._patientId);
            } else {
                // Patient Id already exists (ignore request).
                throw this.logException("Patient ID " + this._patientId + " already exists - skipping ADD!");
            }
        } else {
            throw this.logException("No patient id found on request");
        }
    }

    /**
     *
     * @param PRPA_IN201302UV02_Message
     * @return
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    private void processPatientRegistryRecordUpdated(OMElement PRPA_IN201302UV02_Message)
            throws PatientIdentityFeedException, XdsInternalException {
        // Pull out the patient from the request.
        OMElement patientNode = this.selectSingleNode(PRPA_IN201302UV02_Message, RegistryPatientIdentityFeed.XPATH_PATIENT);
        OMElement idNode = this.getFirstChildWithName(patientNode, "id");
        if (idNode != null) {
            this._patientId = this.getPatientIdFromIIType(idNode);
            this.logInfo("Patient ID", this._patientId);

            // First see if the patient id exists and is active.
            if (this.adtDoesActivePatientExist(this._patientId)) {
                // BHT - Turn into NO-OP (should not support UPDATE).
                // this.adtAddPatientId(patientNode, this._patientId, true /* updateMode */);
            } else {
                // Patient Id does not exist or is not active (ignore request).
                throw this.logException("Patient ID " + this._patientId + " does not exist or is not active - skipping UPDATE!");
            }
        } else {
            throw this.logException("No patient id found on request");
        }
    }

    /**
     *
     * @param PRPA_IN201304UV02_Message
     * @return
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    private void processPatientRegistyDuplicatesResolved(OMElement PRPA_IN201304UV02_Message)
            throws PatientIdentityFeedException, XdsInternalException {
        OMElement patientNode = this.selectSingleNode(PRPA_IN201304UV02_Message, RegistryPatientIdentityFeed.XPATH_PATIENT);
        OMElement idNode = this.getFirstChildWithName(patientNode, "id");
        String survivingPatientId = this.getPatientIdFromIIType(idNode);
        // Get the patient Id that will be subsumed (this is the duplicate).
        String priorRegistrationPatientId = this.getPriorRegistrationPatientId(PRPA_IN201304UV02_Message);
        this.doMerge(survivingPatientId, priorRegistrationPatientId);
    }

    /**
     *
     * @param patientFeedRequest
     * @return
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    private void processPatientRegistyDuplicatesResolved_Simple(OMElement patientFeedRequest)
            throws PatientIdentityFeedException, XdsInternalException {
        String survivingPatientId = this.getPatientFeedRequestNodeText(patientFeedRequest, "PatientId");
        // Get the patient Id that will be subsumed (this is the duplicate).
        String priorRegistrationPatientId = this.getPatientFeedRequestNodeText(patientFeedRequest, "MergePatientId");
        this.doMerge(survivingPatientId, priorRegistrationPatientId);
    }

    /**
     *
     * @param PRPA_IN201304UV02UNMERGE_Message
     * @return
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    private void processPatientRegistryRecordUnmerged(OMElement PRPA_IN201304UV02UNMERGE_Message)
            throws PatientIdentityFeedException, XdsInternalException {
        // Get the Active patient Id
        OMElement patientNode = this.selectSingleNode(PRPA_IN201304UV02UNMERGE_Message, RegistryPatientIdentityFeed.XPATH_PATIENT);
        OMElement idNode = this.getFirstChildWithName(patientNode, "id");
        String survivingPatientId = this.getPatientIdFromIIType(idNode);
        // Get the patient Id that will be unmerged.
        String priorRegistrationPatientId = this.getPriorRegistrationPatientId(PRPA_IN201304UV02UNMERGE_Message);
        this.doUnmerge(PRPA_IN201304UV02UNMERGE_Message, survivingPatientId, priorRegistrationPatientId);
    }

    /**
     *
     * @param survivingPatientId
     * @param priorRegistrationPatientId
     */
    private void doMerge(String survivingPatientId, String priorRegistrationPatientId) throws PatientIdentityFeedException, XdsInternalException {
        // Check existance of surviving patient id on request.
        if (survivingPatientId == null) {
            throw this.logException("Surviving Patient ID not present on request- skipping MERGE!");
        }
        this._patientId = survivingPatientId;
        this.logInfo("Patient ID (Surviving)", survivingPatientId);

        // Check existance of priorRegistration on request.
        if (priorRegistrationPatientId == null) {
            throw this.logException("Prior Registration (to be subsumed) Patient ID not present on request - skipping MERGE!");
        }
        this.logInfo("Prior Registration Patient ID (Subsumed)", priorRegistrationPatientId);

        // See if they are the same patient (if so, skip merge).
        if (survivingPatientId.equals(priorRegistrationPatientId)) {
            throw this.logException("Surviving Patient ID and Prior Registration (to be subsumed) ID are the same - skipping MERGE!");
        }

        // Check that both the Surviving and to be Subsumed Patients both exist and are Active
        if (!this.adtDoesActivePatientExist(survivingPatientId)) {
            throw this.logException("Surviving Patient ID " + survivingPatientId + " is not active or is not known to registry - skipping MERGE!");
        }
        if (!this.adtDoesActivePatientExist(priorRegistrationPatientId)) {
            throw this.logException("Prior Registration (to be subsumed) Patient ID " + priorRegistrationPatientId + " is not active or is not known to registry - skipping MERGE!");
        }

        // Now, we can do some updates.
        // TODO: updates are in 2 databases may need 2 phase commit***

        // First get a list of all external identifier ids that will be affected by the merge
        List externalIdentifierIds = regGetExternalIdentifierIDs(priorRegistrationPatientId);
        logger.debug("Number of Objects being merged: " + externalIdentifierIds.size());

        // Take care of the registry by updating the patient id on the external identifiers.
        this.regUpdateExternalIdentifiers(survivingPatientId, priorRegistrationPatientId);

        // Create a history of the Merge
        this.adtCreateMergeHistory(survivingPatientId, priorRegistrationPatientId, "M", externalIdentifierIds);

        // Disable the ADT record for the priorRegistrationPatientId.
        this.adtUpdatePatientStatus(priorRegistrationPatientId, "I");
    }

    /**
     * 
     * @param PRPA_IN201304UV02UNMERGE_Message
     * @param survivingPatientId
     * @param unmergedPatientId
     * @throws com.vangent.hieos.services.xds.registry.transactions.RegistryPatientIdentityFeed.PatientIdentityFeedException
     * @throws XdsInternalException
     */
    private void doUnmerge(OMElement PRPA_IN201304UV02UNMERGE_Message, String survivingPatientId, String unmergedPatientId) throws PatientIdentityFeedException, XdsInternalException {
        // Check existance of active patient id on request.
        if (survivingPatientId == null) {
            throw this.logException("Active Patient ID not present on request- skipping UNMERGE!");
        }
        this._patientId = survivingPatientId;
        this.logInfo("Patient ID (Active) ", survivingPatientId);

        // Check existance of the patient being unmerged on request.
        if (unmergedPatientId == null) {
            throw this.logException("Prior Registration (to be unmerged) Patient ID not present on request - skipping UNMERGE!");
        }
        this.logInfo("Prior Registration (to be unmerged or split) Patient ID ", unmergedPatientId);

        // See if they are the same patient (if so, skip unmerge).
        if (survivingPatientId.equals(unmergedPatientId)) {
            throw this.logException("Active Patient ID and Patient being Unmerged ID are the same - skipping UNMERGE!");
        }

        // Check that the Active (survived) patient identifier is still active
        if (!this.adtDoesActivePatientExist(survivingPatientId)) {
            throw this.logException("Survived (Active) Patient ID " + survivingPatientId + " is not active or is not known to registry - skipping UNMERGE!");
        }

        // Check to see if the patient id to "unmerge" is active.  If so, it is an error.
        String unmergedPatientIdStatus = this.adtGetPatientStatus(unmergedPatientId);
        if ((unmergedPatientIdStatus != null) && unmergedPatientIdStatus.equals("A")) {
            throw this.logException("Patient ID to be unmerged " + unmergedPatientId + " is active - skipping UNMERGE!");
        }

        // Check to see if the patient id to "unmerge" exists at all.
        if (unmergedPatientIdStatus == null) {
            // Treat as a "SPLIT"
            this.doSplit(PRPA_IN201304UV02UNMERGE_Message, survivingPatientId, unmergedPatientId);

        } else {

            // Check if there is a record of prior registration (to be unmerged) patient id being merged into the surviving patient id
            // Also check that this is the most recent merge - only the most recent merge can be unmerged
            if (!this.adtCheckMergeHistory(survivingPatientId, unmergedPatientId)) {
                throw this.logException("Invalid UnMerge Request for Surviving Patient ID " + survivingPatientId +
                        " and UnMerged Patient: " + unmergedPatientId + "  - skipping UNMERGE!");
            }

            // Patients IDs are valid, now perform the unmerge

            // Get a list of all external identifier ids that were affected by the merge
            List externalIdentifierIds = adtRetrieveMergedRecords(survivingPatientId, unmergedPatientId);
            logger.debug("Number of Objects being unmerged: " + externalIdentifierIds.size());

            // Update the registry by updating the patient id on the external
            // identifiers involved in the previous merge.
            this.regUpdateExternalIdentifiers(survivingPatientId, unmergedPatientId, externalIdentifierIds);

            // Create a history of the unmerge
            this.adtCreateMergeHistory(survivingPatientId, unmergedPatientId, "U", externalIdentifierIds);

            // Activate the ADT Patient record for the unmerged Patient.
            this.adtUpdatePatientStatus(unmergedPatientId, "A");
        }
    }

    /**
     * Perform a SPLIT operation.  All records designated in the PriorRegistration
     * will be moved to the new patient id.
     *
     * @param PRPA_IN201304UV02UNMERGE_Message The received UNMERGE message.
     * @param activePatientId The active patient identifier (fully qualified).
     * @param newPatientId The new patient identifier (fully qualified).
     * @throws com.vangent.hieos.services.xds.registry.transactions.RegistryPatientIdentityFeed.PatientIdentityFeedException
     * @throws XdsInternalException
     */
    private void doSplit(OMElement PRPA_IN201304UV02UNMERGE_Message, String activePatientId, String newPatientId) throws PatientIdentityFeedException, XdsInternalException {
        this.logInfo("SPLIT (New Patient)", newPatientId);

        // Add new patient ID to ADT.
        this.adtAddPatientId(newPatientId);

        // Get list of all prior registration id nodes.
        List priorRegistrationIdNodes = this.getPriorRegistrationPatientIdNodes(PRPA_IN201304UV02UNMERGE_Message);
        int priorRegistrationIdNodesSize = priorRegistrationIdNodes.size();

        // Now SPLIT out records (only from document sources (in root of ids on prior registration)
        // on the request).

        // Prepare list of external identifier ids (uuids).
        List<String> externalIdentifierIds = new ArrayList<String>();

        if (priorRegistrationIdNodesSize > 1) {

            // Get all document source ids that are subject to the split.
            List<String> documentSourceIds = new ArrayList<String>();
            for (int i = 1; i < priorRegistrationIdNodesSize; i++) {
                OMElement idNode = (OMElement) priorRegistrationIdNodes.get(i);
                //String extension = idNode.getAttributeValue(new QName("extension"));
                String documentSourceId = idNode.getAttributeValue(new QName("root"));

                // Add selected document source ids to the list (skip any dups).
                if (!documentSourceIds.contains(documentSourceId)) {
                    documentSourceIds.add(documentSourceId);
                }
            }
            
            // Update the registry by updating the patient id on the external
            // identifiers involved in the split.
            if (documentSourceIds.size() > 0) {
                externalIdentifierIds = this.getExternalIdentifiersToSplitOut(activePatientId, documentSourceIds);
                if (externalIdentifierIds.size() > 0) {
                    // Found some records that are subject to the split.
                    this.regUpdateExternalIdentifiers(activePatientId, newPatientId, externalIdentifierIds);
                }
            }
        }

        // Create a history of the SPLIT (always record the SPLIT even though records
        // may not have been moved to the new patient id).
        this.adtCreateMergeHistory(activePatientId, newPatientId, "S", externalIdentifierIds);
    }

// Helper methods:

    /**
     * Get all external identifier UUIDs related to the supplied "activePatientId"
     * and list of document source identifiers.
     *
     * @param activePatientId Fully qualified patient identifier.
     * @param documentSourceIds  List of document source identifiers.
     * @return List of UUIDs for ExternalIdentifiers found in Registry.
     */
    private List<String> getExternalIdentifiersToSplitOut(String activePatientId, List documentSourceIds) throws PatientIdentityFeedException {
        PatientIdentityFeedRegistryStoredQuerySupport sq = new PatientIdentityFeedRegistryStoredQuerySupport(null, log_message);
        List<String> externalIdentifierIds = new ArrayList<String>();
        try {
            externalIdentifierIds = sq.getExternalIdentifiersToSplitOut(activePatientId, documentSourceIds);
        } catch (XdsInternalException ex) {
            throw this.logException(ex.getMessage());
        }
        return externalIdentifierIds;
    }

    /**
     *
     * @param patientFeedRequest
     * @param name
     * @return
     */
    String getPatientFeedRequestNodeText(OMElement patientFeedRequest, String name) {
        String result = null;
        OMElement node = this.getFirstChildWithName(patientFeedRequest, name);
        if (node != null) {
            result = node.getText();
        }
        return result;
    }

    /**
     *
     * @param idNode
     * @return
     */
    String getPatientIdFromIIType(OMElement idNode) {
        if (idNode == null) {
            return null;  // GUARD: Early exit.
        }
        String extension = idNode.getAttributeValue(new QName("extension"));
        String root = idNode.getAttributeValue(new QName("root"));
        String patientId = this.formattedPatientId(root, extension);
        // DEBUG (Start)
        logger.debug("  extension = " + extension);
        logger.debug("  root = " + root);
        logger.debug("*** Patient ID = " + patientId);
        // DEBUG (Stop)
        return patientId;
    }

    /**
     *
     * @param rootNode
     * @return
     */
    String getPriorRegistrationPatientId(OMElement rootNode) {
        OMElement idNode = this.selectSingleNode(rootNode, RegistryPatientIdentityFeed.XPATH_PRIOR_REGISTRATION_PATIENT_ID);
        String patientId = null;
        if (idNode != null) {
            patientId = this.getPatientIdFromIIType(idNode);
        }
        return patientId;
    }

    /**
     * 
     * @param rootNode
     * @return
     */
    private List getPriorRegistrationPatientIdNodes(OMElement rootNode) {
        return this.selectNodes(rootNode, RegistryPatientIdentityFeed.XPATH_PRIOR_REGISTRATION_PATIENT_ALL_IDS);
    }

    /**
     * NOTE (BHT): Obviously, this routine is long and leborious.  Should change over to a light-weight
     * HL7 v3 Java Library (JAXB generated code may be an option, but on first attempt, code generated
     * was massive.
     *
     * @param request
     * @return
     */
    private OMElement generateACK(OMElement request, String errorString) {
        OMFactory omfactory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = omfactory.createOMNamespace("urn:hl7-org:v3", "ns");

        // MCCI_IN000002UV01:
        OMElement ackResponseNode = omfactory.createOMElement("MCCI_IN000002UV01", ns);
        ackResponseNode.addAttribute("ITSVersion", "XML_1.0", null);

        // /MCCI_IN000002UV01/id:
        OMElement idNode = omfactory.createOMElement("id", ns);
        ackResponseNode.addChild(idNode);
        idNode.addAttribute("root", this.getUUID(), null);

        // /MCCI_IN000002UV01/creationTime:
        OMElement creationTimeNode = omfactory.createOMElement("creationTime", ns);
        ackResponseNode.addChild(creationTimeNode);
        creationTimeNode.addAttribute("value", this.getHL7Date(), null);

        /* Transmission Wrapper */

        // /MCCI_IN000002UV01/versionCode (OK):
        OMElement versionCodeNode = omfactory.createOMElement("versionCode", ns);
        ackResponseNode.addChild(versionCodeNode);
        versionCodeNode.addAttribute("code", "V3PR1", null);  // Denotes HL7v3.

        // /MCCI_IN000002UV01/interactionId (?):
        OMElement interactionIdNode = omfactory.createOMElement("interactionId", ns);
        ackResponseNode.addChild(interactionIdNode);
        interactionIdNode.addAttribute("displayable", "true", null);
        interactionIdNode.addAttribute("extension", "MCCI_IN000002UV01", null);
        interactionIdNode.addAttribute("root", "2.16.840.1.113883", null);  // Denotes an HL7v3 interaction.

        // /MCCI_IN000002UV01/processingCode (?):
        OMElement processingCodeNode = omfactory.createOMElement("processingCode", ns);
        ackResponseNode.addChild(processingCodeNode);
        processingCodeNode.addAttribute("code", "P", null);  // ????? Should value be "T"?

        // /MCCI_IN000002UV01/processingModeCode (OK):
        OMElement processingModeCodeNode = omfactory.createOMElement("processingModeCode", ns);
        ackResponseNode.addChild(processingModeCodeNode);
        processingModeCodeNode.addAttribute("code", "T", null);

        // //MCCI_IN000002UV01/acceptAckCode (OK):
        OMElement acceptAckCodeNode = omfactory.createOMElement("acceptAckCode", ns);
        ackResponseNode.addChild(acceptAckCodeNode);
        acceptAckCodeNode.addAttribute("code", "NE", null);

        // Build "Receiver" (Patient Identity Source):
        OMElement senderOnRequestNode = MetadataSupport.firstChildWithLocalName(request, "sender");
        OMElement senderDeviceOnRequestNode =
                MetadataSupport.firstChildWithLocalName(senderOnRequestNode, "device");

        // /MCCI_IN000002UV01/receiver
        OMElement receiverNode = omfactory.createOMElement("receiver", ns);
        ackResponseNode.addChild(receiverNode);
        receiverNode.addAttribute("typeCode", "RCV", null);

        // /MCCI_IN000002UV01/receiver/device
        receiverNode.addChild(senderDeviceOnRequestNode);  // Sender is now receiver in ACK.

        // Build "Sender" (the Registry):

        // /MCCI_IN000002UV01/sender
        OMElement senderNode = omfactory.createOMElement("sender", ns);
        ackResponseNode.addChild(senderNode);
        senderNode.addAttribute("typeCode", "SND", null);

        // Add "device" for Sender:
        // /MCCI_IN000002UV01/sender/device
        OMElement deviceNode = omfactory.createOMElement("device", ns);
        senderNode.addChild(deviceNode);
        deviceNode.addAttribute("classCode", "DEV", null);
        deviceNode.addAttribute("determinerCode", "INSTANCE", null);

        // /MCCI_IN000002UV01/sender/device/id
        idNode = omfactory.createOMElement("id", ns);
        deviceNode.addChild(idNode);
        idNode.addAttribute("root", this.getRegistryReceiverDeviceId(), null);

        // /MCCI_IN000002UV01/sender/device/name
        OMElement nameNode = omfactory.createOMElement("name", ns);
        deviceNode.addChild(nameNode);
        nameNode.setText(this.getRegistryReceiverDeviceName());

        // /MCCI_IN000002UV01/acknowledgement:
        OMElement ackNode = omfactory.createOMElement("acknowledgement", ns);
        ackResponseNode.addChild(ackNode);

        // /MCCI_IN000002UV01/acknowledgement/typeCode
        OMElement typeCodeNode = omfactory.createOMElement("typeCode", ns);
        ackNode.addChild(typeCodeNode);
        if (errorString == null) {
            // Accept Acknoweledgement Commit Accept
            typeCodeNode.addAttribute("code", "CA", null);
        } else {
            // Accept Acknoweledgement Commit Error
            typeCodeNode.addAttribute("code", "CE", null);
        }

        // /MCCI_IN000002UV01/acknowledgement/targetMessage
        OMElement targetMessageNode = omfactory.createOMElement("targetMessage", ns);
        ackNode.addChild(targetMessageNode);

        // /MCCI_IN000002UV01/acknowledgement/targetMessage/id
        // Need to put in the "id" from the request in the ACK.
        OMElement idNodeOnRequest = MetadataSupport.firstChildWithLocalName(request, "id");
        targetMessageNode.addChild(idNodeOnRequest);

        // FOR ERROR REPORTING:
        if (errorString != null) {

            // /MCCI_IN000002UV01/acknowledgement/acknowledgementDetail
            OMElement acknowledgementDetail = omfactory.createOMElement("acknowledgementDetail", ns);
            ackNode.addChild(acknowledgementDetail);

            // /MCCI_IN000002UV01/acknowledgement/acknowledgementDetail/text
            OMElement textNode = omfactory.createOMElement("text", ns);
            acknowledgementDetail.addChild(textNode);
            textNode.setText(errorString);
        }
        return ackResponseNode;
    }

    /**
     *
     * @param errorString
     * @return
     */
    private OMElement createPatientFeedResponse_Simple(String errorString) {
        // <PatientFeedResponse>
        //    <DeviceId>TBD</DeviceId>
        //    <DeviceName>TBD</DeviceName>
        //    <!-- 0 - Pass, 1 - Fail -->
        //    <ResponseCode>0</ResponseCode>
        //    <ResponseText>TBD</ResponseText>
        //    <ErrorText>TBD</ErrorText>
        // </PatientFeedResponse>
        OMFactory fact = OMAbstractFactory.getOMFactory();
        OMNamespace ns = this.getHIEOS_OMNamespace(fact);
        OMElement patientFeedResponseNode = fact.createOMElement("PatientFeedResponse", ns);

        // PatientFeedResponse/DeviceId
        OMElement deviceIdNode = fact.createOMElement("DeviceId", ns);
        deviceIdNode.setText(this.getRegistryReceiverDeviceId());
        patientFeedResponseNode.addChild(deviceIdNode);

        // PatientFeedResponse/DeviceName
        OMElement deviceNameNode = fact.createOMElement("DeviceName", ns);
        deviceNameNode.setText(this.getRegistryReceiverDeviceName());
        patientFeedResponseNode.addChild(deviceNameNode);

        // PatientFeedResponse/ResponseCode
        OMElement responseCodeNode = fact.createOMElement("ResponseCode", ns);
        patientFeedResponseNode.addChild(responseCodeNode);

        // PatientFeedResponse/ResponseText
        OMElement responseTextNode = fact.createOMElement("ResponseText", ns);
        patientFeedResponseNode.addChild(responseTextNode);

        // No set the text in ResponseText and ErrorText (on error).
        if (errorString == null) {
            responseCodeNode.setText("0");
            responseTextNode.setText("PASS");
        } else {
            responseCodeNode.setText("1");
            responseTextNode.setText("FAIL");
            // PatientFeedResponse/ErrorText
            OMElement errorTextNode = fact.createOMElement("ErrorText", ns);
            patientFeedResponseNode.addChild(errorTextNode);
            errorTextNode.setText(errorString);
        }

        // Return the response node.
        return patientFeedResponseNode;
    }

    /**
     *
     * @param fact
     * @return
     */
    private OMNamespace getHIEOS_OMNamespace(OMFactory fact) {
        String hieos_uri = "urn:hieos:1.0";
        return fact.createOMNamespace(hieos_uri, "hieos");
    }

    /**
     *
     * @return  Value of the property, null if not found.
     */
    private String getRegistryReceiverDeviceId() {
        return this.getXConfigRegistryProperty("ReceiverDeviceId");
    }

    /**
     *
     * @return  Value of the property, null if not found.
     */
    private String getRegistryReceiverDeviceName() {
        return this.getXConfigRegistryProperty("ReceiverDeviceName");
    }

    /**
     *
     * @param rootNode
     * @param xpathExpression
     * @return
     */
    private OMElement selectSingleNode(OMElement rootNode, String xpathExpression) {
        OMElement resultNode = null;
        try {
            AXIOMXPath xpath = new AXIOMXPath(xpathExpression);
            xpath.addNamespace("ns", "urn:hl7-org:v3");
            resultNode = (OMElement) xpath.selectSingleNode(rootNode);
            if (resultNode != null) {
                logger.debug("*** Found node for XPATH: " + xpathExpression);
            } else {
                logger.error("*** Could not find node for XPATH: " + xpathExpression);
            }
            return resultNode;
        } catch (JaxenException e) {
            this.logInternalException(e, "Problem with xpathExpression: " + xpathExpression);
        }
        return resultNode;
    }

    /**
     *
     * @param rootNode
     * @param xpathExpression
     * @return
     */
    private List selectNodes(OMElement rootNode, String xpathExpression) {
        List resultNodes = null;
        try {
            AXIOMXPath xpath = new AXIOMXPath(xpathExpression);
            xpath.addNamespace("ns", "urn:hl7-org:v3");
            resultNodes = xpath.selectNodes(rootNode);
            if (resultNodes != null) {
                logger.debug("*** Found nodes for XPATH: " + xpathExpression);
            } else {
                logger.error("*** Could not find nodes for XPATH: " + xpathExpression);
            }
            return resultNodes;
        } catch (JaxenException e) {
            this.logInternalException(e, "Problem with xpathExpression: " + xpathExpression);
        }
        return resultNodes;
    }

    /**
     *
     * @param rootNode
     * @param localName
     * @return
     */
    private OMElement getFirstChildWithName(OMElement rootNode, String localName) {
        OMElement resultNode = null;
        if (rootNode != null) {
            resultNode = MetadataSupport.firstChildWithLocalName(rootNode, localName);
        }
        return resultNode;
    }

    /**
     *
     * @param domain
     * @param pid
     * @return
     */
    private String formattedPatientId(String domain, String pid) {
        // fd8c812cae1645e^^^&1.3.6.1.4.1.21367.2009.1.2.315&ISO
        return (pid + "^^^" + this.formattedAssigningAuthority(domain));
    }

    /**
     *
     * @param domain
     * @return
     */
    private String formattedAssigningAuthority(String domain) {
        //fd8c812cae1645e^^^&1.3.6.1.4.1.21367.2009.1.2.315&ISO
        return "&" + domain + "&ISO";
    }

    /**
     * Return a UUID.
     *
     * @return The UUID as a String.
     */
    private String getUUID() {
        return UuidAllocator.allocate();
        /*
        UUIDFactory factory = UUIDFactory.getInstance();
        UUID uuid = factory.newUUID();
        return uuid.toString(); */
    }

// ADT methods (keep here for now, but should move ultimately into a well factored structure.
    /**
     *
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    private AdtJdbcConnection adtGetDatabaseConnection() throws XdsInternalException {
        // Open the connection to the ADT database.
        AdtJdbcConnection conn = null;
        try {
            conn = new AdtJdbcConnection();
        } catch (Exception e) {
            throw this.logInternalException(e, "ADT EXCEPTION: Problem getting ADT database connection");
        }
        return conn;
    }

    /**
     * Diasables or Activates a Patient record
     * @param patientId
     */
    private void adtUpdatePatientStatus(String patientId, String status) throws XdsInternalException {
        String uuid = this.adtGetPatientUUID(patientId);
        try {
            _adtConn.updateAdtRecordStatus(uuid, status);
        } catch (SQLException e) {
            throw this.logInternalException(e, "ADT EXCEPTION: Problem updating status for patient ID = " + patientId);
        }
    }

    /**
     *
     * @param patientId
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    private void adtAddPatientId(String patientId) throws XdsInternalException {
        // Get the ADT record:
        AdtRecordBean arb;
        //String uuid;
        // Are we in "update" mode (i.e. patient id exists already)

        // We are in "insert mode" (i.e. patient id does not exist)
        arb = new AdtRecordBean();
        //uuid = arb.getPatientUUID();  // This is a new UUID.
        arb.setPatientStatus("A");    // Set the patient status.
        arb.setPatientId(patientId);  // Set the patient id.

        // Get the demographic data.
        /*
        OMElement patientPersonNode = this.getFirstChildWithName(patientNode, "patientPerson");
        if (patientPersonNode == null) {
        this.logInfo("Note", "Request does not contain <patientPerson>");
        // Just keep going since we do have a patient id.
        } else {
        // Update patient demographic data.
        // BHT: NO LONGER STORE DEMOGRAPHICS
        }*/
        // Store (which should have at least the patient id) to the database.
        this.adtSavePatientRecord(arb);
    }

    /**
     *
     * @param arb
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    private void adtSavePatientRecord(AdtRecordBean arb) throws XdsInternalException {
        try {
            arb.saveToDatabase();
        } catch (Exception e) {
            throw this.logInternalException(e, "ADT EXCEPTION: Problem saving patient record");
        }
    }

    /**
     *
     * @param patientId
     * @return
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    private boolean adtPatientExists(String patientId) throws XdsInternalException {
        boolean patientExists = false;
        try {
            patientExists = _adtConn.doesIdExist(patientId);
        } catch (SQLException e) {
            throw this.logInternalException(e, "ADT EXCEPTION: Problem checking for patient ID existence = " + patientId);
        }
        return patientExists;
    }

    /**
     *
     * @param patientId
     * @return
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    private String adtGetPatientUUID(String patientId) throws XdsInternalException {
        String uuid = null;
        try {
            uuid = _adtConn.getPatientUUID(patientId);
        } catch (SQLException e) {
            throw this.logInternalException(e, "ADT EXCEPTION: Problem getting patient UUID for patient ID existence = " + patientId);
        }
        return uuid;
    }

    /**
     *
     * @param patientId
     * @return
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    private boolean adtDoesActivePatientExist(String patientId) throws XdsInternalException {
        boolean patientActive = false;
        try {
            patientActive = _adtConn.doesActiveIdExist(patientId);
        } catch (SQLException e) {
            throw this.logInternalException(e, "ADT EXCEPTION: Problem checking if active patient exists = " + patientId);
        }
        return patientActive;
    }

    /**
     *
     * @param patientId
     * @return
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    private String adtGetPatientStatus(String patientId) throws XdsInternalException {
        String status = null;
        try {
            status = _adtConn.getPatientStatus(patientId);
        } catch (SQLException e) {
            throw this.logInternalException(e, "ADT EXCEPTION: Problem getting Patient Status for patient ID existence = " + patientId);
        }
        return status;
    }

    /**
     * Creates a history of the Patient IDs and external identifiers IDs involved in a merge or unmerge
     *
     * @param survivingPatientId
     * @param priorRegistrationPatientId
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    private void adtCreateMergeHistory(String survivingPatientId, String priorRegistrationPatientId,
            String action, List externalIdentifierIds) throws XdsInternalException {
        try {
            _adtConn.createMergeHistory(survivingPatientId, priorRegistrationPatientId, action, externalIdentifierIds);
        } catch (SQLException e) {
            throw this.logInternalException(e, "ADT EXCEPTION: Problem creating merge history for Patient IDs = " +
                    survivingPatientId + " / " + priorRegistrationPatientId);
        }
    }

    /**
     * Retreives the external identifier IDs involved in a merge or unmerge
     *
     * @param survivingPatientId
     * @param priorRegistrationPatientId
     * @param action - M (merge) or U (unmerge)
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    private List<String> adtRetrieveMergedRecords(String survivingPatientId, String priorRegistrationPatientId)
            throws XdsInternalException {
        List<String> externalIdentifierIds = new ArrayList<String>();
        try {
            externalIdentifierIds = _adtConn.retrieveMergedRecords(survivingPatientId, priorRegistrationPatientId, "M");
        } catch (SQLException e) {
            throw this.logInternalException(e, "ADT EXCEPTION: Problem retrieving merge history for Patient IDs = " +
                    survivingPatientId + " / " + priorRegistrationPatientId);
        }
        return externalIdentifierIds;
    }

    /**
     * Checks if a patient to be unmerged has been previously merged into the surviving patient
     *
     * @param survivingPatientId
     * @param priorRegistrationPatientId
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    private boolean adtCheckMergeHistory(String survivingPatientId, String priorRegistrationPatientId)
            throws XdsInternalException {
        boolean merge = false;
        try {
            merge = _adtConn.isPatientMerged(survivingPatientId, priorRegistrationPatientId);
        } catch (SQLException e) {
            throw this.logInternalException(e, "ADT EXCEPTION: Problem checking merge history for Patient IDs = " +
                    survivingPatientId + " / " + priorRegistrationPatientId);
        }
        return merge;
    }

    // Registry helper methods.
    /**
     *
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    private Connection regGetDatabaseConnection() throws XdsInternalException {
        return new SQLConnectionWrapper().getConnection(SQLConnectionWrapper.registryJNDIResourceName);
    }

    /**
     *
     * @param survivingPatientId
     * @param priorRegistrationPatientId
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    private void regUpdateExternalIdentifiers(String survivingPatientId, String priorRegistrationPatientId)
            throws XdsInternalException {
        Connection conn = this.regGetDatabaseConnection();
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = conn.prepareStatement("UPDATE EXTERNALIDENTIFIER SET VALUE = ? WHERE VALUE = ?");
            preparedStatement.setString(1, survivingPatientId);
            preparedStatement.setString(2, priorRegistrationPatientId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw this.logInternalException(e, "REGISTRY EXCEPTION: Problem with updating Registry external identifiers");
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Could not close connection", e);
            }
        }
    }

    /**
     * This method updates the patient id on the external identifiers during an unmerge action
     * The list of externalIdentifier Ids for the affected records is provided as a parameter.
     *
     * @param survivingPatientId
     * @param priorRegistrationPatientId
     * @param externalIdentifierIds
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    private void regUpdateExternalIdentifiers(String currentPatientId, String newPatientId,
            List<String> externalIdentifierIds) throws XdsInternalException {
        Connection conn = this.regGetDatabaseConnection();
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = conn.prepareStatement("UPDATE EXTERNALIDENTIFIER SET VALUE = ? WHERE VALUE = ? AND ID = ?");
            for (String id : externalIdentifierIds) {
                preparedStatement.setString(1, newPatientId);
                preparedStatement.setString(2, currentPatientId);
                preparedStatement.setString(3, id);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw this.logInternalException(e, "REGISTRY EXCEPTION: Problem with updating Registry external identifiers for an unmerge");
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Could not close connection", e);
            }
        }
    }

    /**
     * Retrieves a list of ExternalIdentifier IDs associated with a Patient Id
     *  - each ExternalIdentifier is associated with an ExternalObject (Document) or
     * a RegistryPackage (SubmissionSet or Folder)
     *
     * @param priorRegistrationPatientId
     * @return List (ExternalIdentifierIDs)
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    private List regGetExternalIdentifierIDs(String priorRegistrationPatientId)
            throws XdsInternalException {
        Connection conn = this.regGetDatabaseConnection();
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        List externalIds = new ArrayList<String>();

        try {
            preparedStatement = conn.prepareStatement("SELECT ID FROM EXTERNALIDENTIFIER WHERE VALUE = ?");
            preparedStatement.setString(1, priorRegistrationPatientId);
            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                externalIds.add(rs.getString(1));
            }
            return externalIds;
        } catch (SQLException e) {
            throw this.logInternalException(e, "REGISTRY EXCEPTION: Problem retrieving Registry external identifier ids");
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Could not close connection", e);
            }
        }
    }

    /**
     *
     * @param propertyName
     * @return
     */
    private String getXConfigRegistryProperty(String propertyName) {
        String propertyValue = null;
        try {
            // Get the registry's configuration (registry name came from axis2 "services.xml").
            XConfig xconf = XConfig.getInstance();
            XConfigRegistry registry = xconf.getRegistryByName(this.xconfRegistryName);
            if (registry != null) {
                propertyValue = registry.getProperty(propertyName);
            }
        } catch (XdsInternalException e) {
            // FIXME? - not forwarding along exception.
            this.logInternalException(e, "Unable to get XConfig property");
        }
        return propertyValue;
    }

    /**
     *
     * @return
     */
    private String getHL7Date() {
        return Hl7Date.now();
    }

// All of the log methods below should not generate exceptions if problems occur.
    /**
     *
     * @param patientId
     * @param messageId
     * @param updateMode
     */
    private void logPatientIdentityFeedToATNA(
            String transactionId,
            String patientId, String messageId, boolean updateMode, XATNALogger.OutcomeIndicator outcome,
            String sourceIdentity, String sourceIP) {
        try {
            //this.selectSingleNode(request, this.XPATH_MESSAGE_ID);
            XATNALogger xATNALogger = new XATNALogger(transactionId, XATNALogger.ActorType.REGISTRY);
            xATNALogger.auditPatientIdentityFeedToRegistry(
                    patientId, messageId, updateMode, outcome,
                    sourceIdentity, sourceIP);
        } catch (Exception e) {
            this.logInternalException(e, "Error trying to perform ATNA logging for Patient Identity Feed");
        }
    }

    /**
     *
     * @param response
     * @param status
     */
    private void logResponse(OMElement response, boolean status) {
        if (response != null) {
            log_message.addOtherParam("Response", response);
        }
        log_message.setPass(status);
    }

    /**
     *
     * @param errorString
     */
    private void logError(String errorString) {
        this.errorDetected = true;  // Make note of a problem.
        log_message.addErrorParam("Errors", errorString);
        logger.error(errorString);
    }

    /**
     *
     * @param errorString
     * @return
     */
    private PatientIdentityFeedException logException(String errorString) {
        this.logError(errorString);
        return new PatientIdentityFeedException(errorString);
    }

    /**
     *
     * @param e
     * @param errorString
     * @return
     */
    private XdsInternalException logInternalException(Exception e, String errorString) {
        String exceptionString = ExceptionUtil.exception_details(e, errorString);
        this.logError(exceptionString);
        return new XdsInternalException(errorString);
    }

    /**
     *
     * @param logLabel
     * @param infoString
     */
    private void logInfo(String logLabel, String infoString) {
        log_message.addOtherParam(logLabel, infoString);
        logger.debug(logLabel + " : " + infoString);
    }

    // Inner class
    public class PatientIdentityFeedException extends Exception {

        public PatientIdentityFeedException(String msg) {
            super(msg);
        }
    }
}
