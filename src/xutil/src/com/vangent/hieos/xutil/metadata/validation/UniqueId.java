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
package com.vangent.hieos.xutil.metadata.validation;

import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.response.RegistryErrorList;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author NIST (Adapted for HIEOS).
 */
public class UniqueId {

    private Metadata m;
    private RegistryErrorList rel;
    private boolean isSubmit = true;

    /**
     * 
     * @param m
     * @param isSubmit
     * @param rel
     */
    public UniqueId(Metadata m, boolean isSubmit, RegistryErrorList rel) {
        this.m = m;
        this.isSubmit = isSubmit;
        this.rel = rel;
    }

    /**
     *
     * @throws MetadataException
     */
    public void run() throws MetadataException {
        Set<String> uniqueIds = new HashSet<String>();
        for (String id : m.getFolderIds()) {
            String uid = m.getExternalIdentifierValue(id, MetadataSupport.XDSFolder_uniqueid_uuid);
            if (isSubmit) {
                if (uniqueIds.contains(uid)) {
                    rel.add_error(MetadataSupport.XDSRegistryDuplicateUniqueIdInMessage,
                            "UniqueId " + uid + " is not unique within the submission",
                            this.getClass().getName(), null);
                } else {
                    uniqueIds.add(uid);
                }
            }
            validateFormat(uid);
        }

        for (String id : m.getSubmissionSetIds()) {
            String uid = m.getExternalIdentifierValue(id, MetadataSupport.XDSSubmissionSet_uniqueid_uuid);
            if (isSubmit) {
                if (uniqueIds.contains(uid)) {
                    rel.add_error(MetadataSupport.XDSRegistryDuplicateUniqueIdInMessage,
                            "UniqueId " + uid + " is not unique within the submission",
                            this.getClass().getName(), null);
                } else {
                    uniqueIds.add(uid);
                }
            }
            validateFormat(uid);
        }

        for (String id : m.getExtrinsicObjectIds()) {
            String uid = m.getExternalIdentifierValue(id, MetadataSupport.XDSDocumentEntry_uniqueid_uuid);
            if (uid == null) {
                rel.add_error(MetadataSupport.XDSRegistryError,
                        "Document unique ID is null",
                        this.getClass().getName(), null);
                return;
            }
            if (isSubmit) {
                if (uniqueIds.contains(uid)) {
                    rel.add_error(MetadataSupport.XDSRegistryDuplicateUniqueIdInMessage,
                            "UniqueId " + uid + " is not unique within the submission",
                            this.getClass().getName(), null);
                } else {
                    uniqueIds.add(uid);
                }
            }
            validateFormatForDocuments(uid);
        }
    }

    /**
     *
     * @param uid
     */
    private void validateFormat(String uid) {
        if (!Attribute.isOID(uid)) {
            rel.add_error(MetadataSupport.XDSRegistryMetadataError,
                    "UniqueId " + uid + " is not formatted as an OID",
                    "validation/UniqueId.java", null);
        }
    }

    /**
     *
     * @param uid
     */
    private void validateFormatForDocuments(String uid) {
        if (uid.length() > 0 && uid.indexOf('^') == uid.length() - 1) {
            rel.add_error(MetadataSupport.XDSRegistryMetadataError,
                    "UniqueId " + uid + ": EXT part is empty but ^ is present",
                    "validation/UniqueId.java:validate_format_for_documents", null);
        }
        String[] parts = uid.split("\\^");
        if (parts.length == 2) {
            String oid = parts[0];
            String ext = parts[1];
            if (oid.length() > 64) {
                rel.add_error(MetadataSupport.XDSRegistryMetadataError,
                        "UniqueId " + uid + ": OID part is larger than the allowed 64 characters",
                        "validation/UniqueId.java:validate_format_for_documents", null);
            }
            if (ext.length() > 16) {
                rel.add_error(MetadataSupport.XDSRegistryMetadataError,
                        "UniqueId " + uid + ": EXT part is larger than the allowed 16 characters",
                        "validation/UniqueId.java:validate_format_for_documents", null);
            }
            if (ext.length() == 0) {
                rel.add_error(MetadataSupport.XDSRegistryMetadataError,
                        "UniqueId " + uid + ": should not have ^ since no EXT is coded",
                        "validation/UniqueId.java:validate_format_for_documents", null);
            }
            if (!Attribute.isOID(oid)) {
                rel.add_error(MetadataSupport.XDSRegistryMetadataError,
                        "The OID part of UniqueId, " + oid + " is not formatted as an OID (uid = " + uid + " oid = " + oid + " ext = " + ext + ")",
                        "validation/UniqueId.java:validate_format_for_documents", null);
            }

        } else {
            if (!Attribute.isOID(uid)) {
                rel.add_error(MetadataSupport.XDSRegistryMetadataError,
                        "UniqueId " + uid + " is not formatted as an OID",
                        "validation/UniqueId.java:validate_format_for_documents", null);
            }
        }
    }
}
