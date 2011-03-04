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
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201305UV02_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201305UV02_Message_Builder;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201306UV02_Message;
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
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class PDSClient extends Client {

    private final static Logger logger = Logger.getLogger(PDSClient.class);
    protected final static String PDS_PDQV3_ACTION = "urn:hl7-org:v3:PRPA_IN201305UV02";
    protected final static String PDS_PDQV3_ACTION_RESPONSE = "urn:hl7-org:v3:PRPA_IN201306UV02";

    /**
     *
     * @param pdsConfig
     */
    public PDSClient(XConfigActor pdsConfig) {
        super(pdsConfig);
    }

    /**
     *
     * @param PRPA_IN201305UV02_Message
     * @return PRPA_IN201306UV02_Message
     * @throws AxisFault
     */
    public PRPA_IN201306UV02_Message findCandidatesQuery(PRPA_IN201305UV02_Message request) throws AxisFault {
        PRPA_IN201306UV02_Message response = null;
        // TBD: Validate against schema.
        Soap soap = new Soap();
        try {
            XConfigActor config = this.getConfig();
            XConfigTransaction txn = config.getTransaction("PatientRegistryFindCandidatesQuery");
            soap.setAsync(txn.isAsyncTransaction());
            OMElement soapResponse = soap.soapCall(
                    request.getMessageNode(),
                    txn.getEndpointURL(),
                    false /* mtom */,
                    true /* addressing */,
                    true /* soap12 */,
                    PDSClient.PDS_PDQV3_ACTION /* SOAP action */,
                    PDSClient.PDS_PDQV3_ACTION_RESPONSE /* SOAP action response */);
            response = new PRPA_IN201306UV02_Message(soapResponse);
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
    public SubjectSearchResponse findCandidatesQuery(
            DeviceInfo senderDeviceInfo,
            DeviceInfo receiverDeviceInfo,
            SubjectSearchCriteria subjectSearchCriteria) throws AxisFault {
        SubjectSearchResponse subjectSearchResponse = new SubjectSearchResponse();
 
        // Build the HL7v3 message.
        PRPA_IN201305UV02_Message_Builder pdqQueryBuilder =
                new PRPA_IN201305UV02_Message_Builder(senderDeviceInfo, receiverDeviceInfo);

        PRPA_IN201305UV02_Message request =
                pdqQueryBuilder.getPRPA_IN201305UV02_Message(subjectSearchCriteria);
        try {
            PRPA_IN201306UV02_Message queryResponse = this.findCandidatesQuery(request);
            if (queryResponse != null) {
                SubjectBuilder subjectBuilder = new SubjectBuilder();
                subjectSearchResponse = subjectBuilder.buildSubjectSearchResponse(queryResponse);
            }

        } catch (ModelBuilderException ex) {
            throw new AxisFault(ex.getMessage());
        }
        return subjectSearchResponse;
    }
}
