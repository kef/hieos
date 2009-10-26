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

import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.SqParams;
import com.vangent.hieos.xutil.services.framework.XBaseTransaction;

/**
 *
 * @author NIST (Adapted by Bernie Thuman).
 */
public class SQFactory {

    boolean leafClass = true;
    XBaseTransaction common;

    /**
     *
     * @param common
     * @param leaf_class
     */
    public SQFactory(XBaseTransaction common, boolean leaf_class) {
        leafClass = leaf_class;
        this.common = common;
    }

    /**
     *
     * @param uuid
     * @return
     * @throws XdsException
     */
    public Metadata findFoldersForDocumentByUuid(String uuid)
            throws XdsException {
        SqParams parms = new SqParams();
        parms.addStringParm("$XDSDocumentEntryEntryUUID", uuid);
        //Response response, Message log_message
        GetFoldersForDocument sffd = new GetFoldersForDocument(parms, leafClass, common.response, common.log_message);
        return sffd.run_internal();
    }
}
