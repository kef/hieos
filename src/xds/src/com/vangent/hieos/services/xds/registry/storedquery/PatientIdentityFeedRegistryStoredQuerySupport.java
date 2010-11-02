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
import com.vangent.hieos.xutil.exception.XDSRegistryOutOfResourcesException;
import com.vangent.hieos.xutil.exception.XMLParserException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.query.StoredQuery;
import com.vangent.hieos.xutil.response.ErrorLogger;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import java.util.ArrayList;
import java.util.List;
import org.freebxml.omar.server.persistence.rdb.RegistryCodedValueMapper;

/**
 *
 * @author Bernie Thuman
 */
public class PatientIdentityFeedRegistryStoredQuerySupport extends StoredQuery {

    /**
     *
     * @param response
     * @param log_message
     */
    public PatientIdentityFeedRegistryStoredQuerySupport(ErrorLogger response, XLogMessage log_message) {
        super(response, log_message);
    }

    /**
     *
     * @return
     * @throws XdsException
     * @throws XDSRegistryOutOfResourcesException
     */
    @Override
    public Metadata run_internal() throws XdsException, XDSRegistryOutOfResourcesException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * 
     * @param activePatientId
     * @param documentSourceIds
     * @return
     */
    public List<String> getExternalIdentifiersToSplitOut(String activePatientId, List documentSourceIds) {
        List<String> externalIdentifierUUIDs = new ArrayList<String>();
        try {
            // Get all submission sets for the "activePatientId" and set of document source ids:
            List<String> submissionSetUUIDs = this.getSubmissionSetUUIDs(activePatientId, documentSourceIds);
            if (submissionSetUUIDs.size() > 0) // See if any submission sets were found.
            {
                // Get documents for the given submission sets.
                List<String> documentUUIDs = this.getDocumentUUIDs(submissionSetUUIDs);

                // Get folders for the given submission sets.
                List<String> folderUUIDs = this.getRegistryPackageUUIDs(submissionSetUUIDs);

                // Now get all of the relevant external identifiers.

                // Get external identifiers for submission sets.
                externalIdentifierUUIDs.addAll(
                        this.getSubmissionSetExternalIdentifierUUIDs(submissionSetUUIDs));

                // Get external identifiers for documents.
                externalIdentifierUUIDs.addAll(
                        this.getDocumentExternalIdentifierUUIDs(documentUUIDs));

                // Get external identifiers for folders.
                externalIdentifierUUIDs.addAll(
                        this.getFolderExternalIdentifierUUIDs(folderUUIDs));
            }
        } catch (MetadataException ex) {
            // TBD
        } catch (XdsException ex) {
            //TBD
        }
        return externalIdentifierUUIDs;
    }

    /**
     *
     * @param activePatientId
     * @param documentSourceIds
     * @return
     */
    private List<String> getSubmissionSetUUIDs(String activePatientId, List documentSourceIds) throws MetadataException, XdsException {
        init();
        this.return_leaf_class = false;
        select("ss");
        append("FROM RegistryPackage ss, ExternalIdentifier ss_pid, ExternalIdentifier ss_sid");
        newline();
        append("WHERE (ss_pid.value = " + "'" + activePatientId + "'");
        append(" AND ss_pid.identificationscheme = '");
        append(RegistryCodedValueMapper.convertIdScheme_ValueToCode(MetadataSupport.XDSSubmissionSet_patientid_uuid));
        append("'");
        newline();
        append("AND ss_pid.registryobject = ss.id)");
        newline();
        append("AND (ss_sid.value IN ");
        append(documentSourceIds);
        append(" AND ss_sid.identificationscheme = '");
        append(RegistryCodedValueMapper.convertIdScheme_ValueToCode(MetadataSupport.XDSSubmissionSet_sourceid_uuid));
        append("'");
        append(" AND ss_sid.registryobject = ss.id)");
        newline();
        return this.queryForObjectRefs();
    }

    /**
     * 
     * @return
     * @throws XMLParserException
     * @throws XdsException
     */
    private List<String> queryForObjectRefs() throws XMLParserException, XdsException {
        System.out.println("QUERY -> " + this.query.toString());
        List<String> queryResult = br.queryForObjectRefs(query.toString());
        System.out.println("QUERY RESULT -> " + queryResult);
        return queryResult;
    }

    /**
     *
     * @param submissionSetUUIDs
     * @return
     * @throws MetadataException
     * @throws XdsException
     */
    private List<String> getDocumentUUIDs(List<String> submissionSetUUIDs) throws MetadataException, XdsException {
        return this.getRegistryObjectIds("ExtrinsicObject", submissionSetUUIDs);
    }

    /**
     *
     * @param submissionSetUUIDs
     * @return
     * @throws MetadataException
     * @throws XdsException
     */
    private List<String> getRegistryPackageUUIDs(List<String> submissionSetUUIDs) throws MetadataException, XdsException {
        return this.getRegistryObjectIds("RegistryPackage", submissionSetUUIDs);
    }

    /**
     *
     * @param submissionSetUUIDs
     * @return
     * @throws MetadataException
     * @throws XdsException
     */
    private List<String> getSubmissionSetExternalIdentifierUUIDs(List<String> submissionSetUUIDs) throws MetadataException, XdsException {
        return this.getExternalIdentifierIds(MetadataSupport.XDSSubmissionSet_patientid_uuid, submissionSetUUIDs);
    }

    /**
     *
     * @param documentUUIDs
     * @return
     * @throws MetadataException
     * @throws XdsException
     */
    private List<String> getDocumentExternalIdentifierUUIDs(List<String> documentUUIDs) throws MetadataException, XdsException {
        return this.getExternalIdentifierIds(MetadataSupport.XDSDocumentEntry_patientid_uuid, documentUUIDs);
    }

    /**
     *
     * @param folderUUIDs
     * @return
     * @throws MetadataException
     * @throws XdsException
     */
    private List<String> getFolderExternalIdentifierUUIDs(List<String> folderUUIDs) throws MetadataException, XdsException {
        return this.getExternalIdentifierIds(MetadataSupport.XDSFolder_patientid_uuid, folderUUIDs);
    }

    /**
     * 
     * @param identificationScheme
     * @param uuids
     * @return
     * @throws MetadataException
     * @throws XdsException
     */
    private List<String> getExternalIdentifierIds(String identificationScheme, List<String> uuids) throws MetadataException, XdsException {
        if (uuids == null || uuids.size() == 0) {
            return new ArrayList<String>();
        }
        init();
        this.return_leaf_class = false;
        select("ei");
        append("FROM ExternalIdentifier ei");
        newline();
        append("WHERE ei.registryobject IN ");
        append(uuids);
        newline();
        append("AND ei.identificationscheme = '");
        append(RegistryCodedValueMapper.convertIdScheme_ValueToCode(identificationScheme));
        append("'");
        newline();
        return this.queryForObjectRefs();
    }

    /**
     *
     * @param objectType
     * @param submissionSetUUIDs
     * @return
     * @throws MetadataException
     * @throws XdsException
     */
    private List<String> getRegistryObjectIds(String objectType, List<String> submissionSetUUIDs) throws MetadataException, XdsException {
        if (submissionSetUUIDs == null || submissionSetUUIDs.size() == 0) {
            return new ArrayList<String>();
        }
        init();
        this.return_leaf_class = false;
        select("obj");
        append("FROM " + objectType + " obj, Association assoc");
        newline();
        append("WHERE assoc.sourceobject IN ");
        append(submissionSetUUIDs);
        newline();
        append("AND assoc.associationtype = '");
        append(RegistryCodedValueMapper.convertAssocType_ValueToCode(MetadataSupport.xdsB_eb_assoc_type_has_member));
        append("'");
        append(" AND assoc.targetobject = obj.id");
        newline();
        return this.queryForObjectRefs();
    }
}
