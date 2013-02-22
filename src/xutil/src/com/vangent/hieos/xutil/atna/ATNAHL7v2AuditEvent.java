/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2013 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.xutil.atna;

/**
 * Covers ITI-8 and ITI-44.
 *
 * @author Bernie Thuman
 */
public class ATNAHL7v2AuditEvent extends ATNAAuditEvent {

    private String sendingApplication;
    private String sendingFacility;
    private String receivingApplication;
    private String receivingFacility;
    private String messageControlId;
    private String sourceIP = null;

    /**
     *
     * @return
     */
    public String getSourceUserId() {
        return this.formatUserId(sendingFacility, sendingApplication);
    }

    /**
     *
     * @return
     */
    public String getDestinationUserId() {
        return this.formatUserId(receivingFacility, receivingApplication);
    }

    /**
     *
     * @return
     */
    public String getMessageControlId() {
        return messageControlId;
    }

    /**
     *
     * @return
     */
    public String getMessageControlIdType() {
        return "MSH-10";
    }

    /**
     *
     * @param messageControlId
     */
    public void setMessageControlId(String messageControlId) {
        this.messageControlId = messageControlId;
    }

    /**
     *
     * @return
     */
    public String getReceivingApplication() {
        return receivingApplication;
    }

    /**
     *
     * @param receivingApplication
     */
    public void setReceivingApplication(String receivingApplication) {
        this.receivingApplication = receivingApplication;
    }

    /**
     *
     * @return
     */
    public String getReceivingFacility() {
        return receivingFacility;
    }

    /**
     *
     * @param receivingFacility
     */
    public void setReceivingFacility(String receivingFacility) {
        this.receivingFacility = receivingFacility;
    }

    /**
     *
     * @return
     */
    public String getSendingApplication() {
        return sendingApplication;
    }

    /**
     *
     * @param sendingApplication
     */
    public void setSendingApplication(String sendingApplication) {
        this.sendingApplication = sendingApplication;
    }

    /**
     *
     * @return
     */
    public String getSendingFacility() {
        return sendingFacility;
    }

    /**
     *
     * @param sendingFacility
     */
    public void setSendingFacility(String sendingFacility) {
        this.sendingFacility = sendingFacility;
    }

    /**
     *
     * @return
     */
    public String getSourceIP() {
        return sourceIP;
    }

    /**
     *
     * @param sourceIP
     */
    public void setSourceIP(String sourceIP) {
        this.sourceIP = sourceIP;
    }

    /**
     * 
     * @param facility
     * @param application
     * @return
     */
    private String formatUserId(String facility, String application) {
        if (application == null) {
            application = "";
        }
        if (facility == null) {
            facility = "";
        }
        return facility + "|" + application;
    }
}
