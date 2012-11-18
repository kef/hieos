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

import com.vangent.hieos.subjectmodel.Address;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.subjectmodel.InternalId;
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
public class SubjectAddressDAO extends AbstractDAO {

    private static final Logger logger = Logger.getLogger(SubjectAddressDAO.class);

    /**
     *
     * @param connection
     */
    public SubjectAddressDAO(Connection connection) {
        super(connection);
    }

    /**
     * 
     * @param parentSubject
     * @throws EMPIException
     */
    public void load(Subject parentSubject) throws EMPIException {
        List<Address> addresses = parentSubject.getAddresses();
        // Load the subject addresses.
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT seq_no,street_address_line1,street_address_line2,street_address_line3,city,state,postal_code,use FROM subject_address WHERE subject_id=?";
            if (logger.isTraceEnabled()) {
                logger.trace("SQL = " + sql);
            }
            stmt = this.getPreparedStatement(sql);
            Long subjectId = parentSubject.getInternalId().getId();
            stmt.setLong(1, subjectId);
            // Execute query.
            rs = stmt.executeQuery();
            while (rs.next()) {
                Address address = new Address();
                int seqNo = rs.getInt(1);
                InternalId internalId = new InternalId(subjectId, seqNo);
                address.setInternalId(internalId);
                address.setStreetAddressLine1(rs.getString(2));
                address.setStreetAddressLine2(rs.getString(3));
                address.setStreetAddressLine3(rs.getString(4));
                address.setCity(rs.getString(5));
                address.setState(rs.getString(6));
                address.setPostalCode(rs.getString(7));
                address.setUse(rs.getString(8));
                addresses.add(address);
            }
        } catch (SQLException ex) {
            throw PersistenceHelper.getEMPIException("Exception reading SubjectAddress(s) from database", ex);
        } finally {
            this.close(stmt);
            this.close(rs);
        }
    }

    /**
     *
     * @param addresses
     * @param parentSubject
     * @throws EMPIException
     */
    public void insert(List<Address> addresses, Subject parentSubject) throws EMPIException {
        if (addresses.isEmpty()) {
            return;  // Early exit!
        }
        PreparedStatement stmt = null;
        try {
            String sql = "INSERT INTO subject_address(subject_id,seq_no,street_address_line1,street_address_line2,street_address_line3,city,state,postal_code,use) values(?,?,?,?,?,?,?,?,?)";
            stmt = this.getPreparedStatement(sql);
            Long subjectId = parentSubject.getInternalId().getId();
            int seqNo = 0;
            for (Address address : addresses) {
                if (logger.isTraceEnabled()) {
                    logger.trace("SQL = " + sql);
                }
                InternalId internalId = new InternalId(subjectId, seqNo);
                address.setInternalId(internalId);
                stmt.setLong(1, subjectId);
                stmt.setInt(2, seqNo++);
                stmt.setString(3, address.getStreetAddressLine1());
                stmt.setString(4, address.getStreetAddressLine2());
                stmt.setString(5, address.getStreetAddressLine3());
                stmt.setString(6, address.getCity());
                stmt.setString(7, address.getState());
                stmt.setString(8, address.getPostalCode());
                stmt.setString(9, address.getUse());
                stmt.addBatch();
            }
            long startTime = System.currentTimeMillis();
            int[] insertCounts = stmt.executeBatch();
            long endTime = System.currentTimeMillis();
            if (logger.isTraceEnabled()) {
                logger.trace("SubjectAddressDAO.insert: done executeBatch elapedTimeMillis=" + (endTime - startTime)
                        + " Number Records Added: " + insertCounts.length);
            }
        } catch (SQLException ex) {
            throw PersistenceHelper.getEMPIException("Exception inserting subject addresses", ex);
        } finally {
            this.close(stmt);
        }
    }

    /**
     *
     * @param subjectId
     * @throws EMPIException
     */
    public void deleteSubjectRecords(InternalId subjectId) throws EMPIException {
        this.deleteRecords(subjectId, "subject_address", "subject_id", this.getClass().getName());
    }
}
