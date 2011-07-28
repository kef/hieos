/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2011 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.xds.policy;

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class DocumentResponseBuilder {

    /**
     * 
     * @param documentResponseElementList
     * @return
     */
    public List<DocumentResponse> buildDocumentResponseList(DocumentResponseElementList documentResponseElementList) {
        List<DocumentResponse> documentResponseList = new ArrayList<DocumentResponse>();
        List<OMElement> documentResponseNodes = documentResponseElementList.getElementList();
        for (OMElement documentResponseNode : documentResponseNodes) {
            DocumentResponse documentResponse = new DocumentResponse();
            documentResponse.setDocumentResponseObject(documentResponseNode);

            // FIXME: Do not hard-wire URIs.
            // Document id.
            OMElement documentUniqueIdNode = documentResponseNode.getFirstChildWithName(new QName("urn:ihe:iti:xds-b:2007", "DocumentUniqueId"));
            String documentId = documentUniqueIdNode.getText();
            documentResponse.setDocumentId(documentId);

            // Repository id.
            OMElement repositoryIdNode = documentResponseNode.getFirstChildWithName(new QName("urn:ihe:iti:xds-b:2007", "RepositoryUniqueId"));
            String repositoryId = repositoryIdNode.getText();
            documentResponse.setRepositoryId(repositoryId);

            documentResponseList.add(documentResponse);

            // TBD: Other ... also refactor above code into own method.
        }
        return documentResponseList;
    }
}
