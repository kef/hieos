/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2012 Vangent, Inc.  All rights reserved.
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
import com.vangent.hieos.empi.model.SubjectReviewItem;
import com.vangent.hieos.subjectmodel.Address;
import com.vangent.hieos.subjectmodel.InternalId;
import com.vangent.hieos.subjectmodel.Subject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectReviewItemDAO extends AbstractDAO {

    private static final Logger logger = Logger.getLogger(SubjectReviewItemDAO.class);

    /**
     *
     * @param persistenceManager
     */
    public SubjectReviewItemDAO(PersistenceManager persistenceManager) {
        super(persistenceManager);
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
            throw PersistenceManager.getEMPIException("Exception reading SubjectAddress(s) from database", ex);
        } finally {
            this.close(stmt);
            this.close(rs);
        }
    }

    /**
     *
     * @param subjectReviewItems
     * @throws EMPIException
     */
    public void insert(List<SubjectReviewItem> subjectReviewItems) throws EMPIException {
        if (subjectReviewItems.isEmpty()) {
            return;  // Early exit!
        }
        PreparedStatement stmt = null;
        try {
            String sql = "INSERT INTO subject_review_item(subject_id_left,subject_id_right,review_type) values(?,?,?)";
            stmt = this.getPreparedStatement(sql);
            for (SubjectReviewItem subjectReviewItem : subjectReviewItems) {
                if (logger.isTraceEnabled()) {
                    logger.trace("SQL = " + sql);
                }
                stmt.setLong(1, subjectReviewItem.getInternalId().getId());
                stmt.setLong(2, subjectReviewItem.getOtherSubjectId().getId());
                stmt.setString(3, SubjectReviewItemDAO.getSubjectReviewItemTypeValue(subjectReviewItem.getReviewType()));
                stmt.addBatch();
            }
            long startTime = System.currentTimeMillis();
            int[] insertCounts = stmt.executeBatch();
            long endTime = System.currentTimeMillis();
            if (logger.isTraceEnabled()) {
                logger.trace("SubjectReviewItemDAO.insert: done executeBatch elapedTimeMillis=" + (endTime - startTime)
                        + " Number Records Added: " + insertCounts.length);
            }
        } catch (SQLException ex) {
            throw PersistenceManager.getEMPIException("Exception inserting into subject_review_item table", ex);
        } finally {
            this.close(stmt);
        }
    }

    /**
     * 
     * @param reviewType
     * @return
     */
    private static String getSubjectReviewItemTypeValue(SubjectReviewItem.ReviewType reviewType) {
        String value = "";
        switch (reviewType) {
            case POTENTIAL_DUPLICATE:
                value = "PD";
                break;
            case POTENTIAL_MATCH:
                value = "PM";
                break;
        }
        return value;
    }

}
