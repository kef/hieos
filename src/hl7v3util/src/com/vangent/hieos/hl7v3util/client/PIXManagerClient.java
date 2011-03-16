/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2010 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.hl7v3util.client;

import com.vangent.hieos.hl7v3util.model.exception.ModelBuilderException;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201309UV02_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201309UV02_Message_Builder;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201310UV02_Message;
import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.model.subject.SubjectBuilder;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchCriteria;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchResponse;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.soap.Soap;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigTransaction;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;

/**
 *
 * @author Bernie Thuman
 */
public class PIXManagerClient extends Client {
    public final static String XREFMGR_PIXV3QUERY_ACTION = "urn:hl7-org:v3:PRPA_IN201309UV02";
    public final static String XREFMGR_PIXV3QUERY_ACTION_RESPONSE = "urn:hl7-org:v3:PRPA_IN201310UV02";

    /**
     *
     * @param gatewayConfig
     */
    public PIXManagerClient(XConfigActor gatewayConfig) {
        super(gatewayConfig);
    }

    /**
     *
     * @param PRPA_IN201309UV02_Message
     * @return PRPA_IN201310UV02_Message
     * @throws AxisFault
     */
    public PRPA_IN201310UV02_Message getIdentifiersQuery(PRPA_IN201309UV02_Message request) throws AxisFault {
        PRPA_IN201310UV02_Message response = null;
        // TBD: Validate against schema.
        Soap soap = new Soap();
        try {
            XConfigActor config = this.getConfig();
            XConfigTransaction txn = config.getTransaction("PatientRegistryGetIdentifiersQuery");
            soap.setAsync(txn.isAsyncTransaction());
            OMElement soapResponse = soap.soapCall(
                    request.getMessageNode(),
                    txn.getEndpointURL(),
                    false /* mtom */,
                    true /* addressing */,
                    true /* soap12 */,
                    PIXManagerClient.XREFMGR_PIXV3QUERY_ACTION /* SOAP action */,
                    PIXManagerClient.XREFMGR_PIXV3QUERY_ACTION_RESPONSE /* SOAP action response */);
            response = new PRPA_IN201310UV02_Message(soapResponse);
        } catch (XdsException ex) {
            throw new AxisFault(ex.getMessage());
        }
        return response;
    }

   /**
    *
    * @param senderDeviceInfo
    * @param receiverDeviceInfo
    * @param subjectSearchCriteria
    * @return
    * @throws AxisFault
    */
    public SubjectSearchResponse getIdentifiersQuery(
            DeviceInfo senderDeviceInfo,
            DeviceInfo receiverDeviceInfo,
            SubjectSearchCriteria subjectSearchCriteria) throws AxisFault {
        SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();

        // Build the HL7v3 message.
        PRPA_IN201309UV02_Message_Builder pixQueryBuilder =
                new PRPA_IN201309UV02_Message_Builder(senderDeviceInfo, receiverDeviceInfo);

        PRPA_IN201309UV02_Message request =
                pixQueryBuilder.buildPRPA_IN201309UV02_Message(subjectSearchCriteria);
        try {
            PRPA_IN201310UV02_Message pixQueryResponse = this.getIdentifiersQuery(request);
            if (pixQueryResponse != null) {
                SubjectBuilder subjectBuilder = new SubjectBuilder();
                subjectSearchResponse = subjectBuilder.buildSubjectSearchResponse(pixQueryResponse);
            }

        } catch (ModelBuilderException ex) {
            throw new AxisFault(ex.getMessage());
        }
        return subjectSearchResponse;
    }
}
