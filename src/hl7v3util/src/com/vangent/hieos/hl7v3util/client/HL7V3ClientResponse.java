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

import com.vangent.hieos.hl7v3util.model.message.HL7V3Message;

/**
 *
 * @author Bernie Thuman
 */
public class HL7V3ClientResponse {

    private String messageId;
    private HL7V3Message clientMessage;
    private HL7V3Message targetResponse;
    private String targetEndpoint;

    /**
     *
     * @return
     */
    public HL7V3Message getClientMessage() {
        return clientMessage;
    }

    /**
     * 
     * @param clientMessage
     */
    public void setClientMessage(HL7V3Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    /**
     *
     * @return
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     *
     * @param messageId
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     *
     * @return
     */
    public String getTargetEndpoint() {
        return targetEndpoint;
    }

    /**
     *
     * @param targetEndpoint
     */
    public void setTargetEndpoint(String targetEndpoint) {
        this.targetEndpoint = targetEndpoint;
    }

    /**
     *
     * @return
     */
    public HL7V3Message getTargetResponse() {
        return targetResponse;
    }

    /**
     *
     * @param targetResponse
     */
    public void setTargetResponse(HL7V3Message targetResponse) {
        this.targetResponse = targetResponse;
    }
}
