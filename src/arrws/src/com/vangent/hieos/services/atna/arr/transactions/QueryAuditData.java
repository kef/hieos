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
package com.vangent.hieos.services.atna.arr.transactions;

import com.vangent.hieos.services.atna.arr.support.ATNARecord;
import com.vangent.hieos.services.atna.arr.support.AuditException;
import com.vangent.hieos.services.atna.arr.support.AuditMessageHandler;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;


/**
 * Supports the ATNA Repository Web Service for Retrieving records
 * from the ATNA Repository
 *
 * @author Adeola Odunlami
 */
public class QueryAuditData {

    private static final Logger logger = Logger.getLogger(QueryAuditData.class);

    /**
     * Retrieves a list of IP Addressses that have sent audit logs
     * to the repository
     *
     * @return List<String>
     */
    public List<String> retrieveIPAddresses() {
        try {
            logger.info("QueryAuditData - retrieveIPAddresses");
            AuditMessageHandler handler = new AuditMessageHandler();
            List ipAddresses = handler.retrieveIPAddresses();
            return ipAddresses;
        } catch (AuditException ex) {
            logger.error(ex);
            return new ArrayList();
        }
    }

    /**
     * Retrieves a list of audit records for an IP Address
     *
     * @param  ipAddress
     * @return List<ATNARecord>
     */
    public List<ATNARecord> retrieveAuditList(String ipAddress) {
        try {
            logger.info("QueryAuditData - retrieveAuditList: " + ipAddress);
            AuditMessageHandler handler = new AuditMessageHandler();
            ATNARecord searchCriteria = new ATNARecord();
            searchCriteria.setClientIPAddress(ipAddress);
            List<ATNARecord> atnaRecords = handler.retrieveATNARecords(searchCriteria);
            return atnaRecords;
        } catch (AuditException ex) {
            logger.error(ex);
            return new ArrayList();
        }
    }

    /**
     * Retrieves the Audit Data for a specified audit record
     *
     * @param  auditUniqueId
     * @return Object - ATNARecord
     */
    public ATNARecord retrieveAuditDetail(String auditUniqueId) {
        try {
            logger.info("QueryAuditData - retrieveAuditDetails: " + auditUniqueId);
            AuditMessageHandler handler = new AuditMessageHandler();
            ATNARecord atnaRecord = handler.retrieveATNARecord(auditUniqueId);
            return atnaRecord;
        } catch (AuditException ex) {
            logger.error(ex);
            return new ATNARecord();
        }
    }

}
