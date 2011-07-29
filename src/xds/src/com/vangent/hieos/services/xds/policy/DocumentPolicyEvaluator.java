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
import java.util.ArrayList;
import java.util.List;
import oasis.names.tc.xacml._2_0.context.schema.os.RequestType;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class DocumentPolicyEvaluator {

    // FIXME: Can use higher level interface ... look @ callers.
    
    /**
     *
     * @param requestType
     * @param registryObjects
     * @return
     * @throws PolicyException
     */
    public RegistryObjectElementList evaluate(RequestType requestType, RegistryObjectElementList registryObjectElementList) throws PolicyException {
        List<OMElement> permittedRegistryObjects = new ArrayList<OMElement>();

        // First convert registryObjects into DocumentMetadata instances.
        DocumentMetadataBuilder documentMetadataBuilder = new DocumentMetadataBuilder();
        List<DocumentMetadata> documentMetadataList = documentMetadataBuilder.buildDocumentMetadataList(registryObjectElementList);

        // Now, filter results based upon policy evaluation.
        for (DocumentMetadata documentMetadata : documentMetadataList) {
            if (!documentMetadata.isExtrinsicObject()) {
                // We do not evaluate policy for anything other than ExtrinsicObjects
                permittedRegistryObjects.add(documentMetadata.getRegistryObject());
            } else {
                // Create PDP request.
                PDPRequest pdpRequest = new PDPRequest();
                pdpRequest.setRequestType(requestType);
                // Pass in document meta-data as resource content.
                DocumentMetadataElement documentMetadataElement = documentMetadataBuilder.buildDocumentMetadataElement(documentMetadata);
                pdpRequest.addResourceContent(documentMetadataElement.getElement(), true);
                pdpRequest.setAction("evaluate-document");
                System.out.println("Document " + documentMetadata.getDocumentId());

                // Run the policy evaluation.
                PEP pep = new PEP(null);
                PDPResponse pdpResponse = pep.evaluate(pdpRequest);

                // Evaluate results (Obligations are not used here).
                if (pdpResponse.isPermitDecision()) {
                    System.out.println("... PERMIT");
                    permittedRegistryObjects.add(documentMetadata.getRegistryObject());
                } else {
                    // Consider this as a deny
                    System.out.println("... DENY");
                }
            }
        }
        return new RegistryObjectElementList(permittedRegistryObjects);
    }
}
