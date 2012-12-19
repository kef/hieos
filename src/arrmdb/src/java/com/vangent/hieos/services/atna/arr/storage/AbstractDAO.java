/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2010 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.atna.arr.storage;

import com.vangent.hieos.services.atna.arr.support.AuditException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import org.apache.log4j.Logger;


/**
 * @author Adeola Odunlami
 *
 * Base class for all ATNA DAOs
 */
abstract class AbstractDAO {

    protected static int DAO_ACTION_QUERY = 0;
    protected static int DAO_ACTION_INSERT = 1;
    protected static int DAO_ACTION_UPDATE = 2;
    protected static int DAO_ACTION_DELETE = 3;
    protected int action = DAO_ACTION_QUERY;
    protected Object parent;
    protected Connection conn;
    
    private static final Logger log = Logger.getLogger(AbstractDAO.class);


    /**
     * Constructor
     */
    public AbstractDAO(Connection conn){
        this.conn = conn;
    }

    /**
     * Some DAOs are for objects composed with another parent object.
     * This method is to set the parent object.
     */
    public void setParent(Object parent) {
        this.parent = parent;
    }

    /*
     * Initialize an object from specified ResultSet.
     */
    abstract protected void loadObject(Object obj, ResultSet rs) throws AuditException;

    /**
     * Creates an unitialized object for the type supported by this DAO.
     */
    abstract Object createObject() throws AuditException;

    /**
     * Gets a List of binding objects from specified ResultSet.
     */
    public List getObjects(ResultSet rs, int startIndex, int maxResults) throws AuditException {
        List res = new java.util.ArrayList();

        try {
            if (startIndex > 0) {
                // calling rs.next() is a workaround for some drivers, such
                // as Derby's, that do not set the cursor during call to 
                // rs.relative(...)
                rs.next();
                boolean onRow = rs.relative(startIndex - 1);
            }

            int cnt = 0;
            while (rs.next()) {
                Object obj = createObject();
                loadObject(obj, rs);
                res.add(obj);

                if (++cnt == maxResults) {
                    break;
                }
            }
        } catch (SQLException e) {
            log.error(e);
            throw new AuditException(e);
        }

        return res;
    }

    /**
     * Does a bulk delete of a Collection of objects that match the type for this persister.
     *
     */
    public void delete(List objects) throws AuditException {
        //Return immediatley if no objects to insert
        if (objects.size() == 0) {
            return;
        }

        log.info("message.DeletingRowsInTable: " + getTableName() + " - Number of rows: " + objects.size());
        action = DAO_ACTION_DELETE;

        PreparedStatement stmt = null;

        try {
            stmt = createPreparedStatement();
            Iterator iter = objects.iterator();
            while (iter.hasNext()) {
                Object obj = iter.next();

                if (hasComposedObject()){
                    deleteComposedObjects(obj);
                }

                stmt = setPreparedStatement(stmt, obj);
                stmt.addBatch();
            }
            
            int[] updateCounts = stmt.executeBatch();
            log.info("Number of records deleted: " + updateCounts.length);

            iter = objects.iterator();
            while (iter.hasNext()) {
                Object obj = iter.next();
                onDelete(obj);
            }

        } catch (SQLException e) {
            log.error( e);
            throw new AuditException(e);
        } finally {
            closeStatement(stmt);
        }
    }

    /*
     * Gets the id (value) that is foreign key ref to parent table.
     * Must be overridden by derived class
     */
    protected String getParentId() {
        return "";
    }

    /*
     * Gets the column name that is foreign key ref into parent table.
     * Must be overridden by derived class if it is not 'parent'
     */
    protected String getParentAttribute() {
        return "amid";
    }

    /*
     * Indicate whether the type for this DAO has composed objects or not.
     * Used in deciding whether to deleteComposedObjects or not during delete.
     *
     */
    protected boolean hasComposedObject() {
        return false;
    }

    /**
     * Does a bulk delete of objects based upon parent set for this DAO
     *
     */
    public void deleteByParent() throws AuditException {
        PreparedStatement stmt = null;

        try {

            if (!hasComposedObject()) {
                //Do simple deletion if there are no composed objects for this type

                String str = "DELETE from " + getTableName() +
                        " WHERE " + getParentAttribute() + " = ? ";
                
                // Alaways display delete details in log file
                log.info("DELETE SQL for: " + getParentId() + " = " + str);

                stmt = conn.prepareStatement(str);
                stmt.setString(1, getParentId());
                stmt.execute();
            } else {
                //If there are composed objects for this type then
                //we must first fetch the objects and then use the
                //delete(List objects) method so that composed objects
                //are deleted.
                List objects = getByParent();
                delete(objects);
            }
        } catch (SQLException e) {
            throw new AuditException(e);
        } finally {
            closeStatement(stmt);
        }
    }

    /**
     * Gets objects based upon parent set for this DAO
     *
     */
    public List getByParent() throws AuditException {
        List objects = new ArrayList();
        PreparedStatement stmt = null;
        try {
            String sql = "SELECT * FROM " + getTableName() +
                    " WHERE " + getParentAttribute() + " = ? ";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, getParentId());
            if (log.isTraceEnabled()) {
                log.trace("SQL = " + sql);
            }

            ResultSet rs = stmt.executeQuery();

            objects = getObjects(rs, 0, -1);
        } catch (SQLException e) {
            log.error( e);
            throw new AuditException(e);
        } finally {
            closeStatement(stmt);
        }

        return objects;
    }

    /**
     *
     * @return String - the name of the table being processed
     */
    public abstract String getTableName();

    /**
     * This method will insert all the instances of a particular object in the database
     *
     * @param objects - List of objects being inserted into the database
     * @throws AuditException
     */
    public void insert(List objects) throws AuditException {
        if (log.isTraceEnabled()) {
            log.trace("NUMBER OF OBJECTS BEING ADDED TO THE DB: " + objects.size());
        }

        //Return immediatley if no objects to insert
        if (objects.size() == 0) {
            return;
        }

        if (log.isTraceEnabled()) {
            log.trace("message.InsertingRowsInTable" + getTableName());
        }
        action = DAO_ACTION_INSERT;

        PreparedStatement stmt = null;
        try {
            stmt = createPreparedStatement();

            Iterator iter = objects.iterator();
            while (iter.hasNext()) {
                Object obj = iter.next();
                stmt = setPreparedStatement(stmt, obj);
                stmt.addBatch();
            }
            
            long startTime = System.currentTimeMillis();
            int[] updateCounts = stmt.executeBatch();
            long endTime = System.currentTimeMillis();
            if (log.isTraceEnabled()) {
                log.trace("AbstractDAO.insert: done executeBatch elapedTimeMillis=" + (endTime - startTime)
                    + " Number Records Added: " + updateCounts.length);
            }

            // Walks through the Object and inserts sub objects within the hierarchy
            iter = objects.iterator();
            while (iter.hasNext()) {
                Object obj = iter.next();
                insertComposedObjects(obj);
            }

        } catch (SQLException e) {
              System.out.println("\n--- SQLException caught ---\n");
              SQLException e1 = e;
            while (e1 != null) {
                System.out.println("Message:   "
                        + e1.getMessage());
                e1 = e1.getNextException();
                System.out.println("");
            }
            log.error(e);
            throw new AuditException(e);
        } finally {
            closeStatement(stmt);
        }
    }

    protected void onDelete(Object object) throws AuditException {
    }

    protected void deleteComposedObjects(Object object) throws AuditException {
    }

    protected void insertComposedObjects(Object object) throws AuditException {
    }

    /**
     * Creates a SQL statement for the specific table with the object data
     *
     */
    protected String getSQLStatementFragment(Object object)
            throws AuditException {
        throw new AuditException("message.getSQLStatementFragmentMissing: " + getTableName());
    }

    /**
     * Creates the prepared statement with the SQL code
     *
     */
    protected PreparedStatement createPreparedStatement() throws AuditException{
        throw new AuditException("message.createPreparedStatementMissing: " + getTableName());
    }

    /**
     * Sets the bind variables on the prepared statement with data from the object
     *
     */
    protected PreparedStatement setPreparedStatement(PreparedStatement pstmt, Object object) throws AuditException {
        throw new AuditException("message.setPreparedStatementMissing: " + getTableName());
    }

    /**
     * This method will update instances of a particular object in the database
     *
     * @param objects - List of objects that should be updated in the database
     * @throws AuditException
     */
    public void update(List objects) throws AuditException {

        //Return immediatley if no objects to insert
        if (objects.size() == 0) {
            return;
        }

        if (log.isTraceEnabled()) {
            log.trace("message.UpdatingRowsInTable" + getTableName());
        }
        action = DAO_ACTION_UPDATE;

        PreparedStatement stmt = null;

        try {
            // Create the prepared statement instance with the Update SQL Code
            stmt = createPreparedStatement();

            // Iterate through the object instances to be updated and batch up the Prepared Statements
            Iterator iter = objects.iterator();
            while (iter.hasNext()) {
                Object obj = iter.next();
                setPreparedStatement(stmt, obj);
                stmt.addBatch();
            }

            int[] updateCounts = stmt.executeBatch();
            if (log.isTraceEnabled()) {
                log.trace("AbstractDAO.update: Number Records Updated: " + updateCounts.length);
            }

        } catch (SQLException e) {
            log.error(e);
            throw new AuditException(e);
        } finally {
            closeStatement(stmt);
        }
    }

    /**
     * A convenience method to close a <code>Statement</code> stmt.
     * Calls <code>close()</code> method if stmt is not NULL. Logs
     * <code>SQLException</code> as error.
     *
     * @param stmt Statement to be closed.
     */
    public final void closeStatement(Statement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException sqle) {
            log.error("message.CaughtException: " + sqle);
        }
    }

    /**
     * Executes a Select statment that has an IN clause while
     * taking care to execute it in chunks to avoid scalability limits
     * in some databases (Oracle 10g limits terms in IN clause to 1000)
     *
     * Note: Caller is responsible for closing statement associated with each resultSet
     * in resultSets. 
     *
     * @param selectStmtTemplate a string representing the SELECT statment in a parameterized format consistent with ebRR parameterized queries.
     * @return a List of Objects
     */
    public List executeBufferedSelectWithINClause(String selectStmtTemplate, List terms, int termLimit)
            throws AuditException {
        List resultSets = new ArrayList();

        if (terms.size() == 0) {
            return resultSets;
        }

        Iterator iter = terms.iterator();

        try {
            //We need to count the number of terms in "IN" list. 
            //We need to split the SQL Strings into chunks if there are too many terms. 
            //Reason is that some database such as Oracle, do not allow the IN list is too long
            int termCounter = 0;

            StringBuffer inTerms = new StringBuffer();
            while (iter.hasNext()) {
                String term = (String) iter.next();

                if (iter.hasNext() && (termCounter < termLimit)) {
                    inTerms.append("'" + term + "',");
                } else {
                    inTerms.append("'" + term + "' ");
                    String sql = selectStmtTemplate.replaceAll("\\$InClauseTerms", inTerms.toString());

                    Statement stmt = conn.createStatement();
                    log.trace("SQL = " + sql);  // HIEOS/BHT: (DEBUG)
                    ResultSet rs = stmt.executeQuery(sql);
                    resultSets.add(rs);

                    termCounter = 0;
                    inTerms = new StringBuffer();
                }

                termCounter++;
            }

        } catch (SQLException e) {
            throw new AuditException(e);
        } finally {
            //Do not close stmt as that will close resultSet. Caller needs to do this after reading resultSets.
        }

        return resultSets;
    }

}