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
package com.vangent.hieos.hl7v3util.model.message;

import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class PRPA_IN201302UV02_Message_Builder extends HL7V3MessageBuilderHelper {

    /**
     *
     */
    private PRPA_IN201302UV02_Message_Builder() {
    }

    /**
     *
     * @param senderDeviceInfo
     * @param receiverDeviceInfo
     */
    public PRPA_IN201302UV02_Message_Builder(DeviceInfo senderDeviceInfo, DeviceInfo receiverDeviceInfo) {
        super(senderDeviceInfo, receiverDeviceInfo);
    }

    /**
     * 
     * @param request
     * @param subject
     * @param errorDetail
     * @return
     */
    public PRPA_IN201302UV02_Message buildPRPA_IN201302UV02_Message(Subject subject) {
        String messageName = "PRPA_IN201302UV02";

        // PRPA_IN201302UV02
        OMElement requestNode = this.getRequestNode(messageName, "T", "T", "AL");

        // PRPA_IN201302UV02/controlActProcess
        OMElement controlActProcessNode = this.addControlActProcess(requestNode, "PRPA_TE201302UV02");

        // PRPA_IN201302UV02/controlActProcess/subject
        this.addSubjectWithIdsAndNamesOnly(controlActProcessNode, subject);

        return new PRPA_IN201302UV02_Message(requestNode);
    }
}
