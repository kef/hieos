/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2008-2009 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.atna.arr.support;

import java.util.Date;

/**
 *
 * @author Adeola Odunlami
 */
public class ATNARecord {

    private String uniqueID;
    private String clientIPAddress;
    private String clientPort;
    private String protocol;
    private Date receivedDateTime;
    private Date eventDateTime;
    private String transactionShortName;
    private String transactionLongName;
    private String status;
    private String xml;
    private String errorMessage;

    /**
     * @return the uniqueID
     */
    public String getUniqueID() {
        return uniqueID;
    }

    /**
     * @param uniqueID the uniqueID to set
     */
    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    /**
     * @return the clientIPAddress
     */
    public String getClientIPAddress() {
        return clientIPAddress;
    }

    /**
     * @param clientIPAddress the clientIPAddress to set
     */
    public void setClientIPAddress(String clientIPAddress) {
        this.clientIPAddress = clientIPAddress;
    }

    /**
     * @return the clientPort
     */
    public String getClientPort() {
        return clientPort;
    }

    /**
     * @param clientPort the clientPort to set
     */
    public void setClientPort(String clientPort) {
        this.clientPort = clientPort;
    }

    /**
     * @return the protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * @param protocol the protocol to set
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * @return the receivedDateTime
     */
    public Date getReceivedDateTime() {
        return receivedDateTime;
    }

    /**
     * @param receivedDateTime the receivedDateTime to set
     */
    public void setReceivedDateTime(Date receivedDateTime) {
        this.receivedDateTime = receivedDateTime;
    }

    /**
     * @return the transactionShortName
     */
    public String getTransactionShortName() {
        return transactionShortName;
    }

    /**
     * @param transactionShortName the transactionShortName to set
     */
    public void setTransactionShortName(String transactionShortName) {
        this.transactionShortName = transactionShortName;
    }

    /**
     * @return the transactionLongName
     */
    public String getTransactionLongName() {
        return transactionLongName;
    }

    /**
     * @param transactionLongName the transactionLongName to set
     */
    public void setTransactionLongName(String transactionLongName) {
        this.transactionLongName = transactionLongName;
    }

    /**
     * @return the xml
     */
    public String getXml() {
        return xml;
    }

    /**
     * @param xml the xml to set
     */
    public void setXml(String xml) {
        this.xml = xml;
    }

    /**
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param errorMessage the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the eventDateTime
     */
    public Date getEventDateTime() {
        return eventDateTime;
    }

    /**
     * @param eventDateTime the eventDateTime to set
     */
    public void setEventDateTime(Date eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

}
