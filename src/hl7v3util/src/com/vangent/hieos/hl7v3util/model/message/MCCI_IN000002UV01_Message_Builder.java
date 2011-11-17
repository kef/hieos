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
package com.vangent.hieos.hl7v3util.model.message;

import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.xutil.hl7.date.Hl7Date;
import com.vangent.hieos.xutil.exception.XPathHelperException;
import java.util.UUID;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class MCCI_IN000002UV01_Message_Builder extends HL7V3MessageBuilderHelper {

    /**
     * 
     */
    private MCCI_IN000002UV01_Message_Builder() {
    }

    /**
     *
     * @param senderDeviceInfo
     * @param receiverDeviceInfo
     */
    public MCCI_IN000002UV01_Message_Builder(DeviceInfo senderDeviceInfo, DeviceInfo receiverDeviceInfo) {
        super(senderDeviceInfo, receiverDeviceInfo);
    }

    /**
     *
     * @param request
     * @param errorDetail
     * @return
     */
    // FIXME: may want to be able to support other requests.
    public MCCI_IN000002UV01_Message buildMCCI_IN000002UV01(
            HL7V3Message request,
            HL7V3ErrorDetail errorDetail) {
        // MCCI_IN000002UV01:
        OMElement ackResponseNode = this.createOMElement("MCCI_IN000002UV01");
        this.setAttribute(ackResponseNode, "ITSVersion", "XML_1.0");

        // /MCCI_IN000002UV01/id:
        OMElement idNode = this.addChildOMElement(ackResponseNode, "id");
        this.setAttribute(idNode, "root", UUID.randomUUID().toString());

        // /MCCI_IN000002UV01/creationTime:
        OMElement creationTimeNode = this.addChildOMElement(ackResponseNode, "creationTime");
        this.setAttribute(creationTimeNode, "value", Hl7Date.now());

        /* Transmission Wrapper */

        // /MCCI_IN000002UV01/versionCode (OK):
        OMElement versionCodeNode = this.addChildOMElement(ackResponseNode, "versionCode");
        this.setAttribute(versionCodeNode, "code", "V3PR1");  // Denotes HL7v3.

        // /MCCI_IN000002UV01/interactionId (?):
        OMElement interactionIdNode = this.addChildOMElement(ackResponseNode, "interactionId");
        this.setAttribute(interactionIdNode, "displayable", "true");
        this.setAttribute(interactionIdNode, "extension", "MCCI_IN000002UV01");
        this.setAttribute(interactionIdNode, "root", "2.16.840.1.113883"); // Denotes an HL7v3 interaction.

        // /MCCI_IN000002UV01/processingCode (?):
        this.addCode(ackResponseNode, "processingCode", "P");

        // /MCCI_IN000002UV01/processingModeCode (OK):
        this.addCode(ackResponseNode, "processingModeCode", "T");

        // //MCCI_IN000002UV01/acceptAckCode (OK):
        this.addCode(ackResponseNode, "acceptAckCode", "NE");

        // MCCI_IN000002UV01/receiver
        // MCCI_IN000002UV01/sender
        this.addReceiver(ackResponseNode);
        this.addSender(ackResponseNode);

        // /MCCI_IN000002UV01/acknowledgement:
        OMElement ackNode = this.addChildOMElement(ackResponseNode, "acknowledgement");

        // /MCCI_IN000002UV01/acknowledgement/typeCode
        if ((errorDetail == null) || (errorDetail.getText() == null) || errorDetail.getText().isEmpty()) {
            // Accept Acknoweledgement Commit Accept
            this.addCode(ackNode, "typeCode", "CA");
        } else {
            // Accept Acknoweledgement Commit Error
            this.addCode(ackNode, "typeCode", "CE");
        }

        // /MCCI_IN000002UV01/acknowledgement/targetMessage
        OMElement targetMessageNode = this.addChildOMElement(ackNode, "targetMessage");

        // /MCCI_IN000002UV01/acknowledgement/targetMessage/id
        // Need to put in the "id" from the request in the ACK.
        try {
            OMElement idNodeOnRequest = this.selectSingleNode(request.getMessageNode(), "./ns:id[1]");
            targetMessageNode.addChild(idNodeOnRequest);
        } catch (XPathHelperException ex) {
            // TBD: Do something!
        }

        // FOR ERROR REPORTING:
        // FIXME: Add "code" from errorDetail ...
        if ((errorDetail != null) && (errorDetail.getText() != null) && !errorDetail.getText().isEmpty()) {

            // /MCCI_IN000002UV01/acknowledgement/acknowledgementDetail
            OMElement acknowledgementDetail = this.addChildOMElement(ackNode, "acknowledgementDetail");

            // /MCCI_IN000002UV01/acknowledgement/acknowledgementDetail/text
            this.addChildOMElementWithValue(acknowledgementDetail, "text", errorDetail.getText());
        }
        return new MCCI_IN000002UV01_Message(ackResponseNode);
    }
}
