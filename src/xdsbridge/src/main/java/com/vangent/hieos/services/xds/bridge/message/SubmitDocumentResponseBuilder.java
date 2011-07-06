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

package com.vangent.hieos.services.xds.bridge.message;

import com.vangent.hieos.services.xds.bridge.model.ResponseType;
import com.vangent.hieos.services.xds.bridge.model.SubmitDocumentResponse;
import com.vangent.hieos.services.xds.bridge.support.XDSBridgeConfig;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.lang.StringUtils;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-29
 * @author         Jim Horner
 */
public class SubmitDocumentResponseBuilder
        extends AbstractXdsBridgeMessageBuilder {

    /**
     * Constructs ...
     *
     *
     * @param xdsBridgeConfig
     */
    public SubmitDocumentResponseBuilder(XDSBridgeConfig xdsBridgeConfig) {
        super(xdsBridgeConfig);
    }

    /**
     * Method description
     *
     *
     *
     * @param sdrResponse
     *
     * @return
     */
    public SubmitDocumentResponseMessage buildMessage(
            SubmitDocumentResponse sdrResponse) {

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = createOMNamespace();
        OMElement result =
            fac.createOMElement(SubmitDocumentResponseMessage.MESSAGE_TYPE, ns);

        result.addAttribute("status", sdrResponse.getStatus().toString(), null);

        for (ResponseType respType : sdrResponse.getResponses()) {

            OMElement respTypeElem = fac.createOMElement("Response", ns);

            respTypeElem.addAttribute("status",
                                      respType.getStatus().toString(), null);

            // document id is optional
            String docId = respType.getDocumentId();

            if (StringUtils.isNotBlank(docId)) {

                OMElement docElem = fac.createOMElement("DocumentId", ns);

                docElem.setText(docId);

                respTypeElem.addChild(docElem);
            }

            // document id as oid is optional
            String docIdAsOid = respType.getDocumentIdAsOID();

            if (StringUtils.isNotBlank(docId)) {

                OMElement docElem = fac.createOMElement("DocumentIdAsOID", ns);

                docElem.setText(docIdAsOid);

                respTypeElem.addChild(docElem);
            }
            
            
            // message is optional
            String msg = respType.getErrorMessage();

            if (StringUtils.isNotBlank(msg)) {

                OMElement msgElem = fac.createOMElement("ErrorMessage", ns);

                msgElem.setText(respType.getErrorMessage());
                respTypeElem.addChild(msgElem);
            }

            result.addChild(respTypeElem);
        }

        return new SubmitDocumentResponseMessage(result);
    }
}
