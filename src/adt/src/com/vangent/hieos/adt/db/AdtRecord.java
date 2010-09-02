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

/*
 * AdtRecord.java
 *
 * Created on January 6, 2005, 4:20 PM
 */
package com.vangent.hieos.adt.db;

import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.uuid.UuidAllocator;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import org.apache.log4j.Logger;

/**
 * Currently for the purposes of XDS, an ADT patient record consists of only a
 * patient ID and the patient's name.  This class gives access to both pieces of
 * information.
 * July 2005 -- we're adding to it now. Now it is more than just id/name.
 * @author Andrew McCaffrey
 */
public class AdtRecord {
    private final static Logger logger = Logger.getLogger(AdtRecord.class);

    private String uuid = null;
    private String patientId = null;
    private String patientStatus = "A";
    private String timestamp = null;

    /**
     * 
     */
    public AdtRecord() {
        this.generateAndSetNewUuid();
    }

    /**
     * Creates a new instance of AdtRecord, using parameters for patient ID
     * and patient name.
     * @param patientId The patient ID to set.
     * @param patientNames
     */
    public AdtRecord(String patientId, Collection patientNames) {
        this.setPatientId(patientId);
        this.generateAndSetNewUuid();
    }

    /**
     *
     * @param patientId
     * @throws java.sql.SQLException
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    public AdtRecord(String patientId) throws SQLException, XdsInternalException {
        this.setPatientId(patientId);
        this.generateAndSetNewUuid();
    }

    /**
     * Getter for property patientId.
     * @return Value of property patientId.
     */
    public java.lang.String getPatientId() {
        return patientId;
    }

    /**
     * Setter for property patientId.
     * @param patientId New value of property patientId.
     */
    public void setPatientId(java.lang.String patientId) {
        this.patientId = patientId;
    }

    /**
     *
     * @return
     */
    public String getUuid() {
        return uuid;
    }

    /**
     *
     * @param uuid
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     *
     */
    public void generateAndSetNewUuid() {
        this.setUuid(AdtRecord.getFreshUuid());
    }

    /**
     *
     * @return
     */
    static public String getFreshUuid() {
        /*
        UUID newUuid = AdtRecord.getUuidFactory().newUUID();
        return "urn:uuid:" + newUuid.toString();*/
        return UuidAllocator.allocate();
    }

    /**
     *
     * @return
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     *
     * @param timestamp
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     *
     * @throws java.sql.SQLException
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    public void saveToDatabase() throws XdsInternalException, SQLException {
        if (timestamp == null) {
            timestamp = new Date().toString();
        }
        AdtJdbcConnection con = new AdtJdbcConnection();
        try {
            con.addAdtRecord(this);
        } catch (SQLException e) {
            logger.error("Problem adding ADT record", e);
            throw e;
        } finally {
            con.closeConnection();
        }
    }
 
    /**
     * @return the patientStatus
     */
    public String getPatientStatus() {
        return patientStatus;
    }

    /**
     * @param patientStatus the patientStatus to set
     */
    public void setPatientStatus(String patientStatus) {
        this.patientStatus = patientStatus;
    }
}
