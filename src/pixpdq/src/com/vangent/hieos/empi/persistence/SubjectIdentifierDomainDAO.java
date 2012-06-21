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

import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifierDomain;
import com.vangent.hieos.empi.exception.EMPIException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectIdentifierDomainDAO extends AbstractDAO {

    private final static Logger logger = Logger.getLogger(SubjectIdentifierDomainDAO.class);

    /**
     *
     * @param connection
     */
    public SubjectIdentifierDomainDAO(Connection connection) {
        super(connection);
    }

    /**
     * 
     * @param subjectIdentifierDomain
     * @return
     * @throws EMPIException
     */
    public int getId(SubjectIdentifierDomain subjectIdentifierDomain) throws EMPIException {
        int id = -1;  // Not found if -1.
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            // FIXME: Just using universalid right now
            String sql = "SELECT id FROM subject_identifier_domain WHERE universal_id=?";
            System.out.println("SQL = " + sql);
            stmt = this.getPreparedStatement(sql);
            stmt.setString(1, subjectIdentifierDomain.getUniversalId());
            // Execute query.
            rs = stmt.executeQuery();
            if (rs.next()) {
                id = rs.getInt(1);
            }
        } catch (SQLException ex) {
            throw PersistenceHelper.getEMPIException("Exception reading subject identifier domain from database", ex);
        } finally {
            this.close(stmt);
            this.close(rs);
        }
        return id;
    }

    /**
     *
     * @param subjectIdentifierDomain
     * @return
     * @throws EMPIException
     */
    public SubjectIdentifierDomain load(SubjectIdentifierDomain subjectIdentifierDomain) throws EMPIException {
        SubjectIdentifierDomain loadedSubjectIdentifierDomain = null;
        // First get the id.
        int id = this.getId(subjectIdentifierDomain);
        if (id != -1) {
            loadedSubjectIdentifierDomain = this.load(id);
        }
        return loadedSubjectIdentifierDomain;
    }

    /**
     * 
     * @param id
     * @return
     * @throws EMPIException
     */
    public SubjectIdentifierDomain load(int id) throws EMPIException {
        SubjectIdentifierDomain subjectIdentifierDomain = new SubjectIdentifierDomain();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT universal_id,universal_id_type,namespace_id FROM subject_identifier_domain WHERE id=?";
            System.out.println("SQL = " + sql);
            stmt = this.getPreparedStatement(sql);
            stmt.setInt(1, id);
            // Execute query.
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new EMPIException("id = " + id + " not found in subject_identifier_domain table");
            } else {
                subjectIdentifierDomain.setId(id);
                subjectIdentifierDomain.setUniversalId(rs.getString(1));
                subjectIdentifierDomain.setUniversalIdType(rs.getString(2));
                subjectIdentifierDomain.setNamespaceId(rs.getString(3));
            }
        } catch (SQLException ex) {
            throw PersistenceHelper.getEMPIException("Exception reading from subject_identifier_domain table", ex);
        } finally {
            this.close(stmt);
            this.close(rs);
        }
        return subjectIdentifierDomain;
    }
}
