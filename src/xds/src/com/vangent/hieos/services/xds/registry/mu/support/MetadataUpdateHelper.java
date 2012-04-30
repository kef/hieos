/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2012 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.xds.registry.mu.support;

import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import java.util.ArrayList;

/**
 *
 * @author Bernie Thuman
 */
public class MetadataUpdateHelper {

    /**
     *
     * @param id
     * @return
     */
    public static boolean isUUID(String id) {
        return id.startsWith("urn:uuid:");
    }

    /**
     *
     * @param m
     * @throws MetadataException
     */
    public static void logMetadata(XLogMessage logMessage, Metadata m) throws MetadataException {
        // Log relevant data (if logger is turned on of course).
        if (logMessage.isLogEnabled()) {
            // Submission set unique id.
            if (m.getSubmissionSet() != null) {
                logMessage.addOtherParam("SSuid", m.getSubmissionSetUniqueId());
            }
            // Document unique ids.
            ArrayList<String> doc_uids = new ArrayList<String>();
            for (String id : m.getExtrinsicObjectIds()) {
                String uid = m.getUniqueIdValue(id);
                if (uid != null && !uid.equals("")) {
                    doc_uids.add(uid);
                }
            }
            logMessage.addOtherParam("DOCuids", doc_uids);
            // Document uuids.
            logMessage.addOtherParam("DOCuuids", m.getExtrinsicObjectIds());
            // Folder unique ids.
            ArrayList<String> fol_uids = new ArrayList<String>();
            for (String id : m.getFolderIds()) {
                String uid = m.getUniqueIdValue(id);
                if (uid != null && !uid.equals("")) {
                    fol_uids.add(uid);
                }
            }
            logMessage.addOtherParam("FOLuids", fol_uids);
            // Folder uuids.
            logMessage.addOtherParam("FOLuuids", m.getFolderIds());
            logMessage.addOtherParam("Structure", m.structure());
        }
    }
}
