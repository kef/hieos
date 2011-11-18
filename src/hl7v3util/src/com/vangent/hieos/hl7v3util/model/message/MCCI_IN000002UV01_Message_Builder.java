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
        String messageName = "MCCI_IN000002UV01";
        OMElement ackResponseNode = this.getResponseNode(messageName, "P", "T", "NE");

        // /MCCI_IN000002UV01/acknowledgement:
        this.addAcknowledgementToRequest(request.getMessageNode(), ackResponseNode, errorDetail, "CA", "CE");

        return new MCCI_IN000002UV01_Message(ackResponseNode);
    }
}
