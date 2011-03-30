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

import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.exception.MetadataValidationException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
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
     * @param return_objects
     * @param response
     * @param log_message
     * @param is_secure
     * @throws MetadataValidationException
     */
    public GetRelatedDocuments(SqParams params, boolean return_objects, Response response, XLogMessage log_message)
            throws MetadataValidationException {
        super(params, return_objects, response, log_message);

        // param name, required?, multiple?, is string?, is code?, support AND/OR, alternative
        validateQueryParam("$XDSDocumentEntryUniqueId", true, false, true, false, false, "$XDSDocumentEntryEntryUUID");
        validateQueryParam("$XDSDocumentEntryEntryUUID", true, false, true, false, false, "$XDSDocumentEntryUniqueId");
        validateQueryParam("$AssociationTypes", true, true, true, false, false, (String[]) null);
        if (this.has_validation_errors) {
            throw new MetadataValidationException("Metadata Validation error present");
        }
    }

    /**
     *
     * @return
     * @throws XdsException
     */
    public Metadata run_internal() throws XdsException {
        Metadata metadata = new Metadata();
        String uid = params.getStringParm("$XDSDocumentEntryUniqueId");
        String uuid = params.getStringParm("$XDSDocumentEntryEntryUUID");
        List<String> assoc_types = params.getListParm("$AssociationTypes");
        if (assoc_types == null || assoc_types.size() == 0) {
            throw new XdsInternalException("No $AssociationTypes specified in query");
        }

        // filter HasMember out of assoc_types if it exists
        List<String> assoc_types2 = new ArrayList<String>();
        for (String type : assoc_types) {
            if (!MetadataSupport.xdsB_eb_assoc_type_has_member.equals(type)) {
                assoc_types2.add(type);
            }
        }
        assoc_types = assoc_types2;

        // if uuid supplied, save it in originalDocId
        // if uid supplied, query to get it and save its id in originalDocId
        String originalDocId;
        if (uid != null) {
            OMElement ele = this.getDocumentByUID(uid);
            Metadata orig_doc_metadata = MetadataParser.parseNonSubmission(ele);
            if (orig_doc_metadata.getExtrinsicObjects().size() > 0) {
                metadata.addExtrinsicObjects(orig_doc_metadata.getExtrinsicObjects());
                originalDocId = orig_doc_metadata.getExtrinsicObjectIds().get(0);
            } else if (orig_doc_metadata.getObjectRefs().size() > 0) {
                metadata.addObjectRefs(orig_doc_metadata.getObjectRefs());
                originalDocId = orig_doc_metadata.getObjectRefIds().get(0);
            } else {
                return metadata;   // original document not found - return empty
            }
        } else {
            originalDocId = uuid;
        }

        this.log_message.addOtherParam("originalDocId", originalDocId);
        this.log_message.addOtherParam("structure", metadata.structure());

        // at this point result_metadata contains either a single ObjectRef or a single
        // ExtrinsicObject representing the target document.  originalDocId has its
        // id


        // load all associations related to combined_ids
        List<String> targetIds = new ArrayList<String>();
        targetIds.add(originalDocId);

        boolean oldQueryType = this.return_leaf_class;
        this.return_leaf_class = true;
        OMElement associations = this.getAssociations(targetIds, assoc_types);
        this.return_leaf_class = oldQueryType;

        Metadata association_metadata = MetadataParser.parseNonSubmission(associations);

        // no associations => return nothing
        if (association_metadata.getAssociations().size() == 0) {
            return new Metadata();
        }

        // add associations to final result
        metadata.addToMetadata(association_metadata.getLeafClassObjects(), true);

        this.log_message.addOtherParam("with Associations", metadata.structure());

        // discover ids (potentially for documents) that are referenced by the associations
        List<String> assocReferences = association_metadata.getAssocReferences();

        metadata.addObjectRefs(assocReferences);

        if (this.return_leaf_class) {
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
            if (!this.isUuid(id)) {
                throw new MetadataException("Cannot load object with id " + id + " from Registry, this id is not in UUID format (probabaly a symbolic name)");
            }
        }
        objectRefIds.removeAll(idsToRemove);

        // nothing left to load
        if (objectRefIds.size() == 0) {
            return m;
        }
        m.clearObjectRefs();
        boolean discard_duplicates = true;
        boolean origLeafClass = this.return_leaf_class;
        this.return_leaf_class = true;
        m.addMetadata(this.getDocumentByUUID(objectRefIds), discard_duplicates);
        m.addMetadata(this.getRegistryPackageByID(objectRefIds, null), discard_duplicates);
        m.addMetadata(this.getAssociationByID(objectRefIds), discard_duplicates);
        this.return_leaf_class = origLeafClass;  // Reset.
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
        List<String> ids = m.getIdsForObjects(leafClasses);
        m.makeObjectRefs(ids);
        m.clearLeafClassObjects();
        return m;
    }

    /**
     *
     * @param id
     * @return
     */
    private boolean isUuid(String id) {
        return id.startsWith("urn:uuid:");
    }
}
