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
package com.vangent.hieos.hl7v3util.client;

import com.vangent.hieos.hl7v3util.model.message.HL7V3MessageBuilderHelper;
import com.vangent.hieos.hl7v3util.model.message.MCCI_IN000002UV01_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201302UV02_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201302UV02_Message_Builder;
import com.vangent.hieos.subjectmodel.DeviceInfo;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.xutil.exception.SOAPFaultException;
import com.vangent.hieos.xutil.soap.Soap;
import com.vangent.hieos.xutil.soap.WebServiceClient;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigTransaction;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class PIXConsumerClient extends WebServiceClient {

    public final static String XREFCONSUMER_UPDATE_ACTION = "urn:hl7-org:v3:PRPA_IN201302UV02";
    public final static String XREFCONSUMER_UPDATE_ACTION_RESPONSE = "urn:hl7-org:v3:MCCI_IN000002UV01";
    private XLogMessage logMessage;

    /**
     *
     * @param configActor
     */
    public PIXConsumerClient(XConfigActor configActor, XLogMessage logMessage) {
        super(configActor);
        // FIXME: Move logMessage support to base class.
        this.logMessage = logMessage;
    }

    /**
     *
     * @param request
     * @return
     * @throws SOAPFaultException
     */
    public HL7V3ClientResponse patientRegistryRecordRevised(PRPA_IN201302UV02_Message request) throws SOAPFaultException {
        // TBD: Validate against schema.
        Soap soap = new Soap();
        XConfigActor config = this.getConfig();
        XConfigTransaction txn = config.getTransaction("PatientRegistryRecordRevised");
        soap.setAsync(txn.isAsyncTransaction());
        boolean soap12 = txn.isSOAP12Endpoint();
        if (logMessage != null && logMessage.isLogEnabled()) {
            logMessage.addOtherParam("PIX Update Notification Request to " + txn.getEndpointURL(),
                    request.getMessageNode().toString());
        }
        OMElement soapResponse = soap.soapCall(
                request.getMessageNode(),
                txn.getEndpointURL(),
                false, /* MTOM */
                soap12, /* Addressing - Only if SOAP 1.2 */
                soap12,
                PIXConsumerClient.XREFCONSUMER_UPDATE_ACTION /* SOAP action */,
                PIXConsumerClient.XREFCONSUMER_UPDATE_ACTION_RESPONSE /* SOAP action response */);
        if (logMessage != null && logMessage.isLogEnabled()) {
            logMessage.addOtherParam("PIX Update Notification Response from " + txn.getEndpointURL(),
                    soapResponse.toString());
        }
        HL7V3ClientResponse clientResponse = new HL7V3ClientResponse();
        clientResponse.setClientMessage(request);
        clientResponse.setMessageId(HL7V3MessageBuilderHelper.getMessageId(request));
        clientResponse.setTargetEndpoint(txn.getEndpointURL());
        clientResponse.setTargetResponse(new MCCI_IN000002UV01_Message(soapResponse));
        return clientResponse;
    }

    /**
     *
     * @param senderDeviceInfo
     * @param receiverDeviceInfo
     * @param subject
     * @return
     * @throws SOAPFaultException
     */
    public HL7V3ClientResponse patientRegistryRecordRevised(
            DeviceInfo senderDeviceInfo,
            DeviceInfo receiverDeviceInfo,
            Subject subject) throws SOAPFaultException {

        // Build the HL7v3 message.
        PRPA_IN201302UV02_Message_Builder messageBuilder =
                new PRPA_IN201302UV02_Message_Builder(senderDeviceInfo, receiverDeviceInfo);

        PRPA_IN201302UV02_Message request =
                messageBuilder.buildPRPA_IN201302UV02_Message(subject);

        // FIXME: Should build a converter of the ACK response.
        return this.patientRegistryRecordRevised(request);
    }
}
