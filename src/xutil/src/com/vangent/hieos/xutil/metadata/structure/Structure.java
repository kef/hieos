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
package com.vangent.hieos.xutil.metadata.structure;

import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.exception.MetadataValidationException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.response.RegistryErrorList;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import java.util.ArrayList;

//import java.util.List;
import java.util.Iterator;
import java.util.List;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

/**
 *
 * @author NIST (Adapted by Bernie Thuman)
 */
public class Structure {

    private final static Logger logger = Logger.getLogger(Structure.class);
    private Metadata m;
    private RegistryErrorList rel;
    private boolean isSubmit = true;
    private XLogMessage logMessage;
    private List<String> associationIds;
    private List<String> extrinsicObjectIds;
    private List<String> folderIds;

    /**
     * 
     * @param m
     * @param isSubmit
     * @param rel
     * @param logMessage
     * @throws XdsInternalException
     */
    public Structure(Metadata m, boolean isSubmit, RegistryErrorList rel, XLogMessage logMessage) throws XdsInternalException {
        this.m = m;
        this.isSubmit = isSubmit;
        this.rel = rel;
        this.logMessage = logMessage;
    }

    /**
     *
     * @throws MetadataException
     * @throws MetadataValidationException
     * @throws XdsException
     */
    public void run() throws MetadataException, MetadataValidationException, XdsException {
        // To optimize.
        this.extrinsicObjectIds = m.getExtrinsicObjectIds();
        this.folderIds = m.getFolderIds();
        this.associationIds = m.getAssociationIds();

        // All metadata should have proper registry object ids.
        validateRegistryObjectsHaveIds();

        validateInternalClassifications();

        // FIXME: Still need to cleanup.

        // Now, run validations for submission sets.
        if (isSubmit) {
            validateAssociations(); // NEW
            validateSubmissionSetHasContent();
            validateDocumentImpliesSubmissionSet();
            validateFolderImpliesSubmissionSet();
            validateDocumentsInSubmissionSet();
            validateFoldersInSubmissionSet();
            validateSubmissionSetAssociations();
            validateByValueAssociationInSubmission();
            validateFolderAssociations();
            validateSubmissionSetHasSingleStatus();
        } else {
            // FIXME: Consider removing since only used for testing.
            validateSubmissionSetHasSingleStatus();
            //validateAssociationsHaveProperNamespace();
        }
    }

    /**
     *
     * @param id
     * @return
     * @throws XdsException
     */
    private boolean isDocument(String id) throws XdsException {
        return this.extrinsicObjectIds.contains(id);
    }

    /**
     *
     * @param id
     * @return
     * @throws XdsException
     */
    private boolean isFolder(String id) throws XdsException {
        return this.folderIds.contains(id);
    }

    /**
     *
     * @param id
     * @return
     * @throws XdsException
     */
    private boolean isAssociation(String id) throws XdsException {
        return this.associationIds.contains(id);
    }

    /**
     *
     */
    private void validateRegistryObjectsHaveIds() {
        // Check documents.
        List<OMElement> docs = m.getExtrinsicObjects();
        for (OMElement doc : docs) {
            String docId = doc.getAttributeValue(MetadataSupport.id_qname);
            if (docId == null
                    || docId.equals("")) {
                err("All ExtrinsicObject objects must have id attributes");
                return;
            }
        }
        // Check registry packages.
        List<OMElement> registryPackages = m.getRegistryPackages();
        for (OMElement registryPackage : registryPackages) {
            String registryPackageId = registryPackage.getAttributeValue(MetadataSupport.id_qname);
            if (registryPackageId == null
                    || registryPackageId.equals("")) {
                err("All RegistryPackage objects must have id attributes");
                return;
            }
        }
    }

    /**
     *
     */
    private void validateSubmissionSetHasContent() {
        if (m.getSubmissionSet() != null
                && (m.getExtrinsicObjects().isEmpty()
                && m.getFolders().isEmpty()
                && m.getAssociations().isEmpty())) {
            err("Submission contains Submission Set but no Documents, Folders or Associations");
        }
    }

    /**
     *
     */
    private void validateDocumentImpliesSubmissionSet() {
        if (!m.getExtrinsicObjects().isEmpty()
                && (m.getSubmissionSet() == null)) {
            err("Submission contains a Document but no Submission Set");
        }
    }

    /**
     *
     */
    private void validateFolderImpliesSubmissionSet() {
        if (!m.getFolders().isEmpty()
                && (m.getSubmissionSet() == null)) {
            err("Submission contains a Folder but no Submission Set");
        }
    }

    /**
     *
     */
    private void validateDocumentsInSubmissionSet() {
        List<OMElement> docs = m.getExtrinsicObjects();
        String submissionSetId = m.getSubmissionSetId();
        for (OMElement doc : docs) {
            if (!hasAssociation(submissionSetId, MetadataSupport.xdsB_eb_assoc_type_has_member,
                    doc.getAttributeValue(MetadataSupport.id_qname))) {
                err("Document " + doc.getAttributeValue(MetadataSupport.id_qname) + " is not linked to Submission Set with " + MetadataSupport.xdsB_eb_assoc_type_has_member + " Association");
            }
        }
    }

    /**
     *
     */
    private void validateFoldersInSubmissionSet() {
        List<OMElement> fols = m.getFolders();
        String submissionSetId = m.getSubmissionSetId();
        for (OMElement fol : fols) {
            if (!hasAssociation(submissionSetId, MetadataSupport.xdsB_eb_assoc_type_has_member,
                    fol.getAttributeValue(MetadataSupport.id_qname))) {
                err("Folder " + fol.getAttributeValue(MetadataSupport.id_qname) + " is not linked to Submission Set with " + MetadataSupport.xdsB_eb_assoc_type_has_member + " Association");
            }
        }
    }

    /**
     *
     * @throws XdsException
     */
    private void validateAssociations() throws XdsException {
        String submissionSetId = m.getSubmissionSetId();
        List<OMElement> assocs = m.getAssociations();
        for (OMElement assoc : assocs) {
            String sourceId = assoc.getAttributeValue(MetadataSupport.source_object_qname);
            String targetId = assoc.getAttributeValue(MetadataSupport.target_object_qname);
            String assocType = assoc.getAttributeValue(MetadataSupport.association_type_qname);
            if (targetId.equals(submissionSetId)) {
                // Target is submission set (this is not allowed).
                err("SubmissionSet may not be the target of an Association");
            } else if (Metadata.isValidDocumentAssociationType(assocType)) {
                // Source and target's must be documents (but could be references).
                if (!(isDocument(sourceId) || m.isReferencedObject(sourceId))) {
                    err("Association has type " + assocType + " but source must be a document (could be by reference)");
                }
                if (!(isDocument(targetId) || m.isReferencedObject(targetId))) {
                    err("Association has type " + assocType + " but target must be a document (could be by reference)");
                }
                if ((MetadataSupport.xdsB_ihe_assoc_type_rplc.equals(assocType))
                        && !m.isReferencedObject(targetId)) {
                    err("Replaced document (RPLC association type) cannot be in submission\nThe following objects were found in the submission: "
                            + m.getReferencedObjects().toString());
                }
            } else if (assocType.equals(MetadataSupport.xdsB_eb_assoc_type_has_member)) {
                // Source must be the submission set, folder or reference.
                if (!(sourceId.equals(submissionSetId) || isFolder(sourceId) || m.isReferencedObject(sourceId))) {
                    err("Association has type " + assocType + " but source must be the submission set or a folder (could be by reference)");
                }
                // Target must be a folder, document, association or reference.
                if (!(isFolder(targetId) || isDocument(targetId) || isAssociation(targetId) || m.isReferencedObject(targetId))) {
                    err("Association has type " + assocType + " but target must a folder or document (could be by reference)");
                }
                // If 1) the source is a folder (not a reference), 2) the metadata includes the target object and 3) the
                // target object is not a document ... then, emit an error.
                if (isFolder(sourceId) && m.containsObject(targetId) && !isDocument(targetId)) {
                    err("Association has type " + assocType + " and source is a folder but target (by value) is not a document");
                }
            } else if (assocType.equals(MetadataSupport.xdsB_ihe_assoc_type_submit_association)) {
                // Source must be the submission set.
                if (!sourceId.equals(submissionSetId)) {
                    err("Association has type " + assocType + " but source must be the submission set");
                }
                // Target must be an association.
                if (!isAssociation(targetId)) {
                    err("Association has type " + assocType + " but target must be an association");
                }
            } else if (assocType.equals(MetadataSupport.xdsB_ihe_assoc_type_update_availability_status)) {
                // Source must be the submission set.
                if (!sourceId.equals(submissionSetId)) {
                    err("Association has type " + assocType + " but source must be the submission set");
                }
                // Target must be a referenced object (and not the submission set).
                if (m.containsObject(targetId)) {
                    err("Association has type " + assocType + " but target must not be included in submission");
                }
            } else {
                // Error - unknown association type.
                err("Unknown association type " + assocType);
            }
        }
    }

    /**
     * 
     */
    private void validateSubmissionSetAssociations() throws XdsException {
        String submissionSetId = m.getSubmissionSetId();
        List<OMElement> assocs = m.getAssociations();
        for (OMElement assoc : assocs) {
            String targetId = assoc.getAttributeValue(MetadataSupport.target_object_qname);
            String assocType = assoc.getAttributeValue(MetadataSupport.association_type_qname);
            String sourceId = assoc.getAttributeValue(MetadataSupport.source_object_qname);
            if (sourceId.equals(submissionSetId)) {
                if (!Metadata.isValidSubmissionSetAssociationType(assocType)) {
                    err("Association referencing Submission Set has type " + assocType
                            + " but only the following association types are allowed: "
                            + Metadata.getValidSubmissionSetAssociationTypes());
                }
                if (isDocument(targetId)) {
                    if (!m.hasSlot(assoc, "SubmissionSetStatus")) {
                        err("Association "
                                + assoc.getAttributeValue(MetadataSupport.id_qname)
                                + " has sourceObject pointing to Submission Set but contains no SubmissionSetStatus Slot");
                    }
                }
                //else if (isFolder(targetId)) {
                //} else {
                //}
            } else {
                if (m.hasSlot(assoc, "SubmissionSetStatus") && !"Reference".equals(m.getSlotValue(assoc, "SubmissionSetStatus", 0))) {
                    err("Association "
                            + assoc.getAttributeValue(MetadataSupport.id_qname)
                            + " does not have sourceObject pointing to Submission Set but contains SubmissionSetStatus Slot with value Original");
                }
            }
        }
    }

    /**
     *
     * @throws MetadataValidationException
     * @throws MetadataException
     */
    private void validateByValueAssociationInSubmission() throws MetadataValidationException, MetadataException, XdsException {
        List<OMElement> assocs = m.getAssociations();
        String submissionSetId = m.getSubmissionSetId();
        for (OMElement assoc : assocs) {
            String sourceId = assoc.getAttributeValue(MetadataSupport.source_object_qname);
            String targetId = assoc.getAttributeValue(MetadataSupport.target_object_qname);
            if (sourceId.equals(submissionSetId)) {
                String submissionSetStatus = m.getSlotValue(assoc, "SubmissionSetStatus", 0);
                if (isDocument(targetId)) {
                    if (submissionSetStatus == null || submissionSetStatus.equals("")) {
                        err("SubmissionSetStatus Slot on Submission Set association has no value");
                    } else if (submissionSetStatus.equals("Original")) {
                        if (!m.containsObject(targetId)) {
                            err("SubmissionSetStatus Slot on Submission Set association has value 'Original' but the targetObject " + targetId + " references an object not in the submission");
                        }
                    } else if (submissionSetStatus.equals("Reference")) {
                        if (m.containsObject(targetId)) {
                            err("SubmissionSetStatus Slot on Submission Set association has value 'Reference' but the targetObject " + targetId + " references an object in the submission");
                        }
                    } else {
                        err("SubmissionSetStatus Slot on Submission Set association has unrecognized value: " + submissionSetStatus);
                    }
                } else {
                    if (submissionSetStatus != null && !submissionSetStatus.equals("Reference")) {
                        err("A SubmissionSet Association has the SubmissionSetStatus Slot but the targetObject is not part of the Submission");
                    }
                }
            }
        }
    }

    /**
     *
     */
    private void validateSubmissionSetHasSingleStatus() {
        List<OMElement> assocs = m.getAssociations();
        String submissionSetId = m.getSubmissionSetId();
        for (OMElement assoc : assocs) {
            String sourceId = assoc.getAttributeValue(MetadataSupport.source_object_qname);
            if (sourceId.equals(submissionSetId)) {
                String submissionSetStatus = m.getSlotValue(assoc, "SubmissionSetStatus", 1);
                if (submissionSetStatus != null) {
                    err("SubmissionSetStatus Slot on Submission Set association has more than one value");
                }
            }
        }
    }

    // Folder Assocs must be linked to SS by a secondary Assoc
    /**
     * 
     * @throws XdsException
     */
    private void validateFolderAssociations() throws XdsException {
        String submissionSetId = m.getSubmissionSetId();
        List<OMElement> folderAssocs = new ArrayList<OMElement>();
        for (OMElement assoc : m.getAssociations()) {
            String sourceId = m.getAssocSource(assoc);
            if (m.getAssocTarget(assoc).equals(submissionSetId)) {
                err("SubmissionSet may not the be target of an Association");
            }
            // if sourceId points to a SubmissionSet in this metadata then no further work is needed
            // if sourceId points to a Folder (in or out of this metadata) then secondary Assoc required
            if (sourceId.equals(submissionSetId)) {
                continue;
            }
            if (isFolder(sourceId)) {
                folderAssocs.add(assoc);
            }
        }
        if (folderAssocs.isEmpty()) {
            return;
        }

        // Show that the non-ss associations are linked to ss via a HasMember association
        // This only applies when the association's sourceObject is a Folder
        for (OMElement folderAssoc : folderAssocs) {
            String folderAssocId = folderAssoc.getAttributeValue(MetadataSupport.id_qname);
            boolean good = false;
            for (OMElement assoc : m.getAssociations()) {
                if (m.getAssocSource(assoc).equals(submissionSetId)
                        && m.getAssocTarget(assoc).equals(folderAssocId)
                        && MetadataSupport.xdsB_eb_assoc_type_has_member.equals(m.getAssocType(assoc))) {
                    if (good) {
                        err("Multiple HasMember Associations link Submission Set " + submissionSetId
                                + " and Association\n" + folderAssoc);
                    } else {
                        good = true;
                    }
                }
            }
            if (good == false) {
                err("A HasMember Association is required to link Submission Set " + submissionSetId
                        + " and Folder/Document Association\n" + folderAssoc);
            }
        }
    }

    /**
     *
     * @param source
     * @param type
     * @param target
     * @return
     */
    private boolean hasAssociation(String source, String type, String target) {
        List<OMElement> assocs = m.getAssociations();
        for (OMElement assoc : assocs) {
            String assocTarget = assoc.getAttributeValue(MetadataSupport.target_object_qname);
            String assocType = assoc.getAttributeValue(MetadataSupport.association_type_qname);
            String assocSource = assoc.getAttributeValue(MetadataSupport.source_object_qname);
            if (assocTarget != null && assocTarget.equals(target)
                    && assocType != null && assocType.equals(type)
                    && assocSource != null && assocSource.equals(source)) {
                return true;
            }
        }
        return false;
    }

    // internal classifications must point to object that contains them
    /**
     * 
     * @throws MetadataValidationException
     * @throws MetadataException
     */
    private void validateInternalClassifications() throws MetadataValidationException, MetadataException {
        for (OMElement ele : m.getRegistryPackages()) {
            validateInternalClassifications(ele);
        }
        for (OMElement ele : m.getExtrinsicObjects()) {
            validateInternalClassifications(ele);
        }
    }

    /**
     *
     * @param e
     * @throws MetadataValidationException
     * @throws MetadataException
     */
    private void validateInternalClassifications(OMElement e) throws MetadataValidationException, MetadataException {
        String e_id = e.getAttributeValue(MetadataSupport.id_qname);
        if (e_id == null || e_id.equals("")) {
            return;
        }
        for (Iterator it = e.getChildElements(); it.hasNext();) {
            OMElement child = (OMElement) it.next();
            OMAttribute classified_object_att = child.getAttribute(MetadataSupport.classified_object_qname);
            if (classified_object_att != null) {
                String value = classified_object_att.getAttributeValue();
                if (!e_id.equals(value)) {
                    throw new MetadataValidationException("Classification " + m.getIdentifyingString(child)
                            + "\n is nested inside " + m.getIdentifyingString(e)
                            + "\n but classifies object " + m.getIdentifyingString(value));
                }
            }
        }
    }

    /**
     * 
     * @param msg
     */
    private void err(String msg) {
        rel.add_error(MetadataSupport.XDSRegistryMetadataError, msg, this.getClass().getName(), null);
    }
}
