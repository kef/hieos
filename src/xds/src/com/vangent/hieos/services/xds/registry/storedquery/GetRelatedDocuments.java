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
package com.vangent.hieos.services.xds.registry.storedquery;

import com.vangent.hieos.services.xds.registry.backend.BackendRegistry;
import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.exception.MetadataValidationException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.exception.XdsResultNotSinglePatientException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.metadata.structure.SqParams;
import com.vangent.hieos.xutil.response.Response;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import java.util.ArrayList;
import java.util.List;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author NIST (Adapted by Bernie Thuman).
 */
public class GetRelatedDocuments extends StoredQuery {

    /**
     * 
     * @param params
     * @param returnLeafClass
     * @param response
     * @param logMessage
     * @param backendRegistry
     * @throws MetadataValidationException
     */
    public GetRelatedDocuments(SqParams params, boolean returnLeafClass, Response response, XLogMessage logMessage, BackendRegistry backendRegistry)
            throws MetadataValidationException {
        super(params, returnLeafClass, response, logMessage, backendRegistry);

        // param name, required?, multiple?, is string?, is code?, support AND/OR, alternative
        validateQueryParam("$XDSDocumentEntryUniqueId", true, false, true, false, false, "$XDSDocumentEntryEntryUUID");
        validateQueryParam("$XDSDocumentEntryEntryUUID", true, false, true, false, false, "$XDSDocumentEntryUniqueId");
        validateQueryParam("$AssociationTypes", true, true, true, false, false, (String[]) null);
        validateQueryParam("$XDSAssociationStatus", false, true, true, false, false, (String[]) null);
        validateQueryParam("$MetadataLevel", false, false, false, false, false, (String[]) null);

        if (this.hasValidationErrors()) {
            throw new MetadataValidationException("Metadata Validation error present");
        }
    }

    /**
     *
     * @return
     * @throws XdsException
     */
    public Metadata runInternal() throws XdsException {
        Metadata metadata = new Metadata();
        SqParams params = this.getSqParams();
        String metadataLevel = params.getIntParm("$MetadataLevel");
        String uid = params.getStringParm("$XDSDocumentEntryUniqueId");
        String uuid = params.getStringParm("$XDSDocumentEntryEntryUUID");
        List<String> assocTypes = params.getListParm("$AssociationTypes");
        if (assocTypes == null || assocTypes.isEmpty()) {
            throw new XdsInternalException("No $AssociationTypes specified in query");
        }
        List<String> assocStatusValues = params.getListParm("$XDSAssociationStatus");
        if (assocStatusValues == null || assocStatusValues.isEmpty()) {
            // association status not specified.
            // Default association status to "Approved" if not specified.
            assocStatusValues = new ArrayList<String>();
            assocStatusValues.add(MetadataSupport.status_type_approved);
        }

        // filter HasMember out of assoc_types if it exists
        List<String> assocTypes2 = new ArrayList<String>();
        for (String type : assocTypes) {
            if (!MetadataSupport.xdsB_eb_assoc_type_has_member.equals(type)) {
                assocTypes2.add(type);
            }
        }
        assocTypes = assocTypes2;

        // if uuid supplied, save it in originalDocId
        // if uid supplied, query to get it and save its id in originalDocId
        String originalDocId;
        if (uid != null) {
            OMElement ele = this.getDocumentByUID(uid);
            Metadata originalDocMetadata = MetadataParser.parseNonSubmission(ele);
            if (originalDocMetadata.getExtrinsicObjects().size() > 0) {
                metadata.addExtrinsicObjects(originalDocMetadata.getExtrinsicObjects());
                originalDocId = originalDocMetadata.getExtrinsicObjectIds().get(0);
            } else if (originalDocMetadata.getObjectRefs().size() > 0) {
                metadata.addObjectRefs(originalDocMetadata.getObjectRefs());
                originalDocId = originalDocMetadata.getObjectRefIds().get(0);
            } else {
                return metadata;   // original document not found - return empty
            }
        } else {
            originalDocId = uuid;
        }

        XLogMessage logMessage = this.getLogMessage();
        logMessage.addOtherParam("originalDocId", originalDocId);
        logMessage.addOtherParam("structure", metadata.structure());

        // at this point result_metadata contains either a single ObjectRef or a single
        // ExtrinsicObject representing the target document.  originalDocId has its
        // id


        // load all associations related to combined_ids
        List<String> targetIds = new ArrayList<String>();
        targetIds.add(originalDocId);

        boolean oldQueryType = this.isReturnLeafClass();
        this.setReturnLeafClass(true);
        OMElement associations = this.getAssociations(targetIds, assocStatusValues, assocTypes);
        this.setReturnLeafClass(oldQueryType);

        Metadata assocMetadata = MetadataParser.parseNonSubmission(associations);

        // no associations => return nothing
        if (assocMetadata.getAssociations().isEmpty()) {
            return new Metadata();
        }

        // add associations to final result
        metadata.addToMetadata(assocMetadata.getLeafClassObjects(), true);

        logMessage.addOtherParam("with Associations", metadata.structure());

        // discover ids (potentially for documents) that are referenced by the associations
        List<String> assocReferences = assocMetadata.getAssocReferences();

        metadata.addObjectRefs(assocReferences);

        if (this.isReturnLeafClass()) {
            return this.convertToLeafClass(metadata);
        } else {
            return this.convertToObjectRefs(metadata, false);
        }
    }

    /**
     * Load LeafClass form for all objects.  New Metadata object returned.
     * @param m - metadata to fill in with LeafClass objects. Unaltered.
     * @return new Metadata object
     * @throws LoggerException
     * @throws XdsException
     */
    private Metadata convertToLeafClass(Metadata metadata) throws XdsException {
        Metadata m = metadata.makeClone();
        List<String> objectRefIds = m.getObjectRefIds();
        // these could reference any type of object.  Only interested in:
        // RegistryPackage, ExtrinsicObject, Association

        List<String> idsToRemove = new ArrayList<String>();
        for (String id : objectRefIds) {
            if (m.containsObject(id)) {
                idsToRemove.add(id);
            }
            if (!this.isUUID(id)) {
                throw new MetadataException("Cannot load object with id " + id + " from Registry, this id is not in UUID format (probabaly a symbolic name)");
            }
        }
        objectRefIds.removeAll(idsToRemove);

        // nothing left to load
        if (objectRefIds.isEmpty()) {
            return m;
        }
        m.clearObjectRefs();
        boolean discardDuplicates = true;
        boolean origLeafClass = this.isReturnLeafClass();
        this.setReturnLeafClass(true);
        m.addMetadata(this.getDocumentByUUID(objectRefIds), discardDuplicates);
        m.addMetadata(this.getRegistryPackageByUUID(objectRefIds, null), discardDuplicates);
        m.addMetadata(this.getAssociationByUUID(objectRefIds), discardDuplicates);
        this.setReturnLeafClass(origLeafClass); // Reset.
        return m;
    }

    /**
     *
     * @param metadata
     * @param ignoreExistingObjectRefs
     * @return
     * @throws XdsInternalException
     * @throws MetadataException
     * @throws MetadataValidationException
     */
    private Metadata convertToObjectRefs(Metadata metadata, boolean ignoreExistingObjectRefs) throws XdsInternalException, MetadataException, MetadataValidationException {
        Metadata m = metadata.makeClone();
        if (ignoreExistingObjectRefs) {
            m.clearObjectRefs();
        }
        List<OMElement> leafClasses = m.getAllLeafClasses();
        List<String> ids = m.getObjectIds(leafClasses);
        m.makeObjectRefs(ids);
        m.clearLeafClassObjects();
        return m;
    }

    /**
     *
     * @param id
     * @return
     */
    // FIXME: MOVE
    private boolean isUUID(String id) {
        return id.startsWith("urn:uuid:");
    }

    /**
     *
     * @param validateConsistentPatientId
     * @param metadata
     * @throws XdsException
     * @throws XdsResultNotSinglePatientException
     */
    @Override
    public void validateConsistentPatientId(boolean validateConsistentPatientId, Metadata metadata)
            throws XdsException, XdsResultNotSinglePatientException {
        // Default implementation.
        // Can't really do anything here, since metadata update is implemented.
    }
}
