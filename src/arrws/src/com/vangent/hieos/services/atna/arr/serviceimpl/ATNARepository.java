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
package com.vangent.hieos.services.atna.arr.serviceimpl;

import com.vangent.hieos.services.atna.arr.support.ATNARecord;
import com.vangent.hieos.services.atna.arr.transactions.QueryAuditData;
import java.text.SimpleDateFormat;
import java.util.List;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

/**
 *
 * Webservice for  ATNA Repository Queries
 *
 *
 * @author Adeola Odunlami
 */
public class ATNARepository {

    private static final Logger logger = Logger.getLogger(ATNARepository.class);

    /**
     * Retrieves a list of IP Addressses that have sent audit logs
     * to the repository
     *
     * @param request - no input the OMElement is empty
     * @return OMElement - List<String>
     */
    public OMElement retrieveIPAddresses(OMElement request) {
        logger.info("ARRRepository - retrieveIPAddresses");
        OMFactory omfactory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = omfactory.createOMNamespace("urn:hieos:atna", "atna");
        QueryAuditData query = new QueryAuditData();
        List<String> ipAddresses = query.retrieveIPAddresses();
        OMElement ipAddressesNode = omfactory.createOMElement("ipAddresses", ns);
        for (String ipAddress : ipAddresses) {
            OMElement ipAddressNode = omfactory.createOMElement("ipAddress", ns);
            ipAddressNode.setText(ipAddress);
            ipAddressesNode.addChild(ipAddressNode);
        }
        return ipAddressesNode;
    }

    /**
     * Retrieves a list of audit records for an IP Address
     *
     * @param  request - ipAddress
     * @return OMElement - List<ATNARecord>
     */
    public OMElement retrieveAuditList(OMElement request) {
        logger.info("ARRRepository - retrieveAuditList");
        OMFactory omfactory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = omfactory.createOMNamespace("urn:hieos:atna", "atna");
        String ipAddress = request.getFirstElement().getText();
        logger.info("ARRRepository - retrieveAuditListRequest: " + ipAddress);

        QueryAuditData query = new QueryAuditData();
        List<ATNARecord> atnaRecords = query.retrieveAuditList(ipAddress);
        OMElement atnaRecordsNode = omfactory.createOMElement("atnaRecords", ns);
        for (ATNARecord atnaRecord : atnaRecords) {
            OMElement atnaRecordNode = omfactory.createOMElement("atnaRecord", ns);

            atnaRecordNode.addChild(createChildNode("uniqueId", ns, atnaRecord.getUniqueID()));
            atnaRecordNode.addChild(createChildNode("clientIPAddress", ns, atnaRecord.getClientIPAddress()));
            atnaRecordNode.addChild(createChildNode("protocol", ns, atnaRecord.getProtocol()));
            atnaRecordNode.addChild(createChildNode("status", ns, atnaRecord.getStatus()));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss.SSS");
            //String receivedDateTimeStr = "2011-02-19T16:35:15.828";
            String receivedDateTimeStr = sdf.format(atnaRecord.getReceivedDateTime());
            atnaRecordNode.addChild(createChildNode("receivedDateTime", ns, receivedDateTimeStr));
            //TODO - Retrieve Event Date Time from the database
            atnaRecord.setEventDateTime(atnaRecord.getReceivedDateTime());
            String eventDateTimeStr = sdf.format(atnaRecord.getEventDateTime());
            atnaRecordNode.addChild(createChildNode("eventDateTime", ns, eventDateTimeStr));

            atnaRecordNode.addChild(createChildNode("transactionShortName", ns, atnaRecord.getTransactionShortName()));
            atnaRecordNode.addChild(createChildNode("transactionLongName", ns, atnaRecord.getTransactionLongName()));

            System.out.println("XML log entry = " + atnaRecord.getXml());
            byte[] syslogBase64Bytes = Base64.encodeBase64(atnaRecord.getXml().getBytes());
            System.out.println("XML log entry (base64) = " + new String(syslogBase64Bytes));
            atnaRecordNode.addChild(createChildNode("syslog", ns, new String(syslogBase64Bytes)));

            atnaRecordNode.addChild(createChildNode("errorMessage", ns, atnaRecord.getErrorMessage()));

            atnaRecordsNode.addChild(atnaRecordNode);
        }
        return atnaRecordsNode;
    }

    /**
     * Retrieves the Audit Data for a specified audit record
     *
     * @param  request
     * @return OMElement
     */
    public OMElement retrieveAuditDetail(OMElement request) {
        logger.info("ARRRepository - retrieveAuditDetails");
        OMFactory omfactory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = omfactory.createOMNamespace("urn:hieos:atna", "atna");
        String auditUniqueId = request.getText();

        QueryAuditData query = new QueryAuditData();
        ATNARecord atnaRecord = query.retrieveAuditDetail(auditUniqueId);

        OMElement atnaRecordNode = omfactory.createOMElement("atnaRecord", ns);
        // populate node with atnaRecord data

        return atnaRecordNode;
    }

    /**
     *
     * @param nodeName
     * @param ns
     * @param value
     * @return
     */
    private OMElement createChildNode(String nodeName, OMNamespace ns, String value) {
        OMFactory omfactory = OMAbstractFactory.getOMFactory();
        OMElement atnaRecordChildNode = omfactory.createOMElement(nodeName, ns);
        atnaRecordChildNode.setText(value);
        return atnaRecordChildNode;
    }
}
