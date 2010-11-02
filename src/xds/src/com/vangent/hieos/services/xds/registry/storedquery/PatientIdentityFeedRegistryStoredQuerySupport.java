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
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.query.StoredQuery;
import com.vangent.hieos.xutil.response.ErrorLogger;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.freebxml.omar.server.persistence.rdb.RegistryCodedValueMapper;

/**
 * Performs Registry stored queries in support of the Patient Identity Feed
 * transaction - most notably - handling SPLITs.
 *
 * @author Bernie Thuman
 */
public class PatientIdentityFeedRegistryStoredQuerySupport extends StoredQuery {

    private final static Logger logger = Logger.getLogger(PatientIdentityFeedRegistryStoredQuerySupport.class);

    /**
     * Constructor.
     *
     * @param response
     * @param log_message
     */
    public PatientIdentityFeedRegistryStoredQuerySupport(ErrorLogger response, XLogMessage log_message) {
        super(response, log_message);
    }

    /**
     * Not used.
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
     * Get all external identifier UUIDs related to the supplied "activePatientId"
     * and list of document source identifiers.
     *
     * @param activePatientId Fully qualified patient identifier.
     * @param documentSourceIds  List of document source identifiers.
     * @return List of UUIDs for ExternalIdentifiers found in Registry.
     */
    public List<String> getExternalIdentifiersToSplitOut(String activePatientId, List documentSourceIds) throws XdsInternalException {
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
            throw new XdsInternalException("Registry Error: " + ex.getMessage());
        } catch (XdsException ex) {
            throw new XdsInternalException("Registry Error: " + ex.getMessage());
        }
        return externalIdentifierUUIDs;
    }

    /**
     * Get all submission set UUIDs for the given "active patient id" and list
     * of document source ids.
     *
     * @param activePatientId Fully qualified patient id.
     * @param documentSourceIds List of document source identifiers.
     * @return List of submission set uuids.
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
     * Get list of document UUIDs for the set of submission set UUIDs supplied.
     *
     * @param submissionSetUUIDs List of submission set UUIDs.
     * @return List of document UUIDs.
     * @throws MetadataException
     * @throws XdsException
     */
    private List<String> getDocumentUUIDs(List<String> submissionSetUUIDs) throws MetadataException, XdsException {
        return this.getRegistryObjectUUIDs("ExtrinsicObject", submissionSetUUIDs);
    }

    /**
     * Get list of registry package UUIDs for the set of submission set UUIDs supplied.
     *
     * @param submissionSetUUIDs List of submission set UUIDs.
     * @return List of registry package UUIDs.
     * @throws MetadataException
     * @throws XdsException
     */
    private List<String> getRegistryPackageUUIDs(List<String> submissionSetUUIDs) throws MetadataException, XdsException {
        return this.getRegistryObjectUUIDs("RegistryPackage", submissionSetUUIDs);
    }

    /**
     * Get list of UUIDs for external identifiers given the list of submission set UUIDs.
     *
     * @param submissionSetUUIDs List of submission set UUIDs.
     * @return List of external identifier UUIDs.
     * @throws MetadataException
     * @throws XdsException
     */
    private List<String> getSubmissionSetExternalIdentifierUUIDs(List<String> submissionSetUUIDs) throws MetadataException, XdsException {
        return this.getExternalIdentifierIds(MetadataSupport.XDSSubmissionSet_patientid_uuid, submissionSetUUIDs);
    }

    /**
     * Get list of UUIDs for external identifiers given the list of document UUIDs.
     *
     * @param documentUUIDs List of document UUIDs.
     * @return List of external identifier UUIDs.
     * @throws MetadataException
     * @throws XdsException
     */
    private List<String> getDocumentExternalIdentifierUUIDs(List<String> documentUUIDs) throws MetadataException, XdsException {
        return this.getExternalIdentifierIds(MetadataSupport.XDSDocumentEntry_patientid_uuid, documentUUIDs);
    }

    /**
     * Get list of UUIDs for external identifiers given the list of folder UUIDs.
     *
     * @param folderUUIDs List of folder UUIDs.
     * @return List of external identifier UUIDs.
     * @throws MetadataException
     * @throws XdsException
     */
    private List<String> getFolderExternalIdentifierUUIDs(List<String> folderUUIDs) throws MetadataException, XdsException {
        return this.getExternalIdentifierIds(MetadataSupport.XDSFolder_patientid_uuid, folderUUIDs);
    }

    /**
     * Get list of external identifier UUIDs for the supplied "identification scheme"
     * and list of registry object UUIDs.
     *
     * @param identificationScheme Supplied identification scheme.
     * @param uuids List of UUIDs.
     * @return List of external identifier UUIDs.
     * @throws MetadataException
     * @throws XdsException
     */
    private List<String> getExternalIdentifierIds(String identificationScheme, List<String> uuids) throws MetadataException, XdsException {
        if (uuids == null || uuids.size() == 0) {
            // Return an empty list of no UUIDs in request.
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
     * Get list of UUIDs for the given "object type" and list of submission set UUIDs.
     *
     * @param objectType "ExtrinsicObject" or "RegistryPackage".
     * @param submissionSetUUIDs List of submission set UUIDs.
     * @return List of UUIDs for found registry objects.
     * @throws MetadataException
     * @throws XdsException
     */
    private List<String> getRegistryObjectUUIDs(String objectType, List<String> submissionSetUUIDs) throws MetadataException, XdsException {
        if (submissionSetUUIDs == null || submissionSetUUIDs.size() == 0) {
            // Return an empty list of no submission set UUIDs in request.
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

    /**
     * Run the current query and return object references (or an empty list).
     *
     * @return List of object references (UUIDs).
     *
     * @throws XMLParserException
     * @throws XdsException
     */
    private List<String> queryForObjectRefs() throws XMLParserException, XdsException {
        if (logger.isDebugEnabled()) {
            logger.debug("REGISTRY QUERY -> " + this.query.toString());
        }
        List<String> queryResult = br.queryForObjectRefs(query.toString());
        if (logger.isDebugEnabled()) {
            logger.debug("REGISTRY QUERY RESULT -> " + queryResult);
        }
        return queryResult;
    }
}
