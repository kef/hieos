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
package com.vangent.hieos.empi.persistence;

import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.subject.TelecomAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectTelecomAddressDAO extends AbstractDAO {

    private static final Logger logger = Logger.getLogger(SubjectTelecomAddressDAO.class);

    /**
     *
     * @param connection
     */
    public SubjectTelecomAddressDAO(Connection connection) {
        super(connection);
    }

    /**
     *
     * @param parentSubject
     * @throws EMPIException
     */
    public void load(Subject parentSubject) throws EMPIException {
        List<TelecomAddress> telecomAddresses = parentSubject.getTelecomAddresses();
        // Load the subject addresses.
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT id,use,value FROM subject_telecom_address WHERE subject_id=?";
            stmt = this.getPreparedStatement(sql);
            stmt.setString(1, parentSubject.getInternalId());
            // Execute query.
            rs = stmt.executeQuery();
            while (rs.next()) {
                TelecomAddress telecomAddress = new TelecomAddress();
                telecomAddress.setInternalId(rs.getString(1));
                telecomAddress.setUse(rs.getString(2));
                telecomAddress.setValue(rs.getString(3));
                telecomAddresses.add(telecomAddress);
            }
        } catch (SQLException ex) {
            throw PersistenceHelper.getEMPIException("Exception reading subject TelecomAddresses(s) from database", ex);
        } finally {
            this.close(stmt);
            this.close(rs);
        }
    }

    /**
     *
     * @param telecomAddresses
     * @param parentSubject
     * @throws EMPIException
     */
    public void insert(List<TelecomAddress> telecomAddresses, Subject parentSubject) throws EMPIException {
        PreparedStatement stmt = null;
        try {
            String sql = "INSERT INTO subject_telecom_address(id,use,value,subject_id) values(?,?,?,?)";
            stmt = this.getPreparedStatement(sql);
            for (TelecomAddress telecomAddress : telecomAddresses) {
                telecomAddress.setInternalId(PersistenceHelper.getUUID());
                stmt.setString(1, telecomAddress.getInternalId());
                stmt.setString(2, telecomAddress.getUse());
                stmt.setString(3, telecomAddress.getValue());
                stmt.setString(4, parentSubject.getInternalId());
                stmt.addBatch();
            }
            long startTime = System.currentTimeMillis();
            int[] insertCounts = stmt.executeBatch();
            long endTime = System.currentTimeMillis();
            if (logger.isTraceEnabled()) {
                logger.trace("SubjectTelecomAddressDAO.insert: done executeBatch elapedTimeMillis=" + (endTime - startTime)
                        + " Number Records Added: " + insertCounts.length);
            }
        } catch (SQLException ex) {
            throw PersistenceHelper.getEMPIException("Exception inserting telecom addresses", ex);
        } finally {
            this.close(stmt);
        }
    }

    /**
     *
     * @param subjectId
     * @throws EMPIException
     */
    public void deleteSubjectRecords(String subjectId) throws EMPIException {
        this.deleteRecords(subjectId, "subject_telecom_address", "subject_id", this.getClass().getName());
    }
}
