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

import com.vangent.hieos.policyutil.exception.PolicyException;
import com.vangent.hieos.policyutil.pdp.model.PDPRequest;
import com.vangent.hieos.policyutil.pdp.model.PDPResponse;
import com.vangent.hieos.policyutil.pep.impl.PEP;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import java.util.List;
import oasis.names.tc.xacml._2_0.context.schema.os.RequestType;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class DocumentPolicyEvaluator {

    private XLogMessage logMessage;

    /**
     *
     */
    private DocumentPolicyEvaluator() {
        // Do not allow.
    }

    /**
     * 
     * @param logMessage
     */
    public DocumentPolicyEvaluator(XLogMessage logMessage) {
        this.logMessage = logMessage;
    }

    /**
     * 
     * @param action
     * @param requestType
     * @param registryObjectElementList
     * @return
     * @throws PolicyException
     */
    public DocumentPolicyResult evaluate(
            String action, RequestType requestType, RegistryObjectElementList registryObjectElementList) throws PolicyException {

        // First convert registryObjects into DocumentMetadata instances.
        List<OMElement> registryObjects = registryObjectElementList.getElementList();
        DocumentMetadataBuilder documentMetadataBuilder = new DocumentMetadataBuilder();
        List<DocumentMetadata> documentMetadataList = documentMetadataBuilder.buildDocumentMetadataList(new RegistryObjectElementList(registryObjects));

        // Conduct policy evaluation.
        return this.evaluate(action, requestType, documentMetadataList);
    }

    /**
     *
     * @param action
     * @param requestType
     * @param documentMetadataList
     * @return
     * @throws PolicyException
     */
    public DocumentPolicyResult evaluate(
            String action, RequestType requestType, List<DocumentMetadata> documentMetadataList) throws PolicyException {

        StringBuilder logsb = new StringBuilder();  // For debug logging.
        DocumentMetadataBuilder documentMetadataBuilder = new DocumentMetadataBuilder();

        DocumentPolicyResult policyResult = new DocumentPolicyResult();
        
        // Now, filter results based upon "document-level" policy evaluation.
        for (DocumentMetadata documentMetadata : documentMetadataList) {
            if (!documentMetadata.isExtrinsicObject()) {
                // We do not evaluate policy for anything other than ExtrinsicObjects
                policyResult.addPermittedDocument(documentMetadata);
            } else {
                // Create PDP request.
                PDPRequest pdpRequest = new PDPRequest();
                pdpRequest.setReturnContext(false);  // Save some processing time here.
                pdpRequest.setRequestType(requestType);
                // Pass in document meta-data as resource content.
                DocumentMetadataElement documentMetadataElement = documentMetadataBuilder.buildDocumentMetadataElement(documentMetadata);
                pdpRequest.addResourceContent(documentMetadataElement.getElement(), true);
                pdpRequest.setAction(action);

                // Run the policy evaluation.
                PEP pep = new PEP(null);  // Note: we don't need the configuration for the current actor.
                PDPResponse pdpResponse = pep.evaluate(pdpRequest);

                // Evaluate results (Obligations are not used here).
                boolean permittedAccessToDocument = pdpResponse.isPermitDecision();
                if (permittedAccessToDocument) {
                    policyResult.addPermittedDocument(documentMetadata);
                } else {
                    policyResult.addDeniedDocument(documentMetadata);
                }
                if (logMessage != null && logMessage.isLogEnabled()) {
                    if (permittedAccessToDocument) {
                        logsb.append("...PERMIT" + "[doc_id=").append(documentMetadata.getDocumentId()).append(", repo_id=").append(documentMetadata.getRepositoryId()).append("]");
                    } else {
                        logsb.append("...DENY" + "[doc_id=").append(documentMetadata.getDocumentId()).append(", repo_id=").append(documentMetadata.getRepositoryId()).append("]");
                    }
                }
            }
        }
        if (logMessage != null && logMessage.isLogEnabled()) {
            logMessage.addOtherParam("Policy:Note", logsb.toString());
        }
        return policyResult;
    }
}
