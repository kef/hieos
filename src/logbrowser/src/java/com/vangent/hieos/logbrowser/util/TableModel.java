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
package com.vangent.hieos.logbrowser.util;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.Format;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

public class TableModel extends AbstractTableModel
        implements TableModelListener {

    private Vector<Vector<Object>> dataVector = new Vector<Vector<Object>>();
    private Vector<String> headerVector = new Vector<String>();
    private String xmlString;
    private Map fieldsAndFormats = null;
    private final static Logger logger = Logger.getLogger(TableModel.class);
    private static final long serialVersionUID = 1L;
    public final static String STRING = "String";
    public final static String INTEGER = "Integer";
    public final static String DATE = "Date";
    public final static String TIMESTAMP = "Timestamp";
    private final static String ROW_NUM_SELECT = "select * from (select a.*, rownum rnum from (";
    private final static String ROW_NUM_RANGE = ")a where rownum <= ?) where rnum > ?";
    private final static String ROW_LIMIT_OFFSET = " limit ? offset ?";

    public TableModel() throws SQLException {
    }

    /**
     * @param sqlRequest - SQL Prepared Statement
     * @param sqlParams - Prepared Statement variables
     * @param fieldsAndFormats - Formats for fields
     * @param c - connection
     * @throws SQLException
     */
    public TableModel(String sqlRequest, Vector<HashMap> sqlParams, Map fieldsAndFormats, Connection c) throws SQLException {
        this.fieldsAndFormats = fieldsAndFormats;
        ResultSet statementResult = null;
        PreparedStatement pstmt = null;
        try {
            pstmt = c.prepareStatement(sqlRequest);
            if (logger.isDebugEnabled()) {
                logger.debug("TABLE_MODEL: (SQL) -> " + sqlRequest);
            }

            // Set Parameters and Exceute Statement
            logger.info("TABLE_MODEL: Start Query: " + new GregorianCalendar().getTime());
            java.util.Date startTime = new GregorianCalendar().getTime();
            pstmt = setPStmtParameters(pstmt, sqlParams);
            statementResult = pstmt.executeQuery();

            ResultSetMetaData metaData = statementResult.getMetaData();
            int columnCount = metaData.getColumnCount();

            dataVector = new Vector<Vector<Object>>();
            headerVector = new Vector<String>();

            if (logger.isDebugEnabled()) {
                logger.debug("TABLE_MODEL: column count : " + columnCount);
            }
            for (int i = 0; i < columnCount; i++) {
                headerVector.add(metaData.getColumnLabel((i + 1)));
                if (logger.isDebugEnabled()) {
                    logger.debug("LABEL: " + metaData.getColumnLabel((i + 1)));
                }
            }

            while (statementResult.next()) {
                Vector<Object> tmp = new Vector<Object>(columnCount);
                for (int j = 0; j < columnCount; j++) {
                    String columnName = getColumnName(j);
                    Object columnData;
                    if (metaData.getColumnType(j + 1) == Types.TIMESTAMP) {
                        columnData = statementResult.getTimestamp(columnName);
                    } else {
                        columnData = statementResult.getObject(columnName);
                    }
                    columnData = getFormattedData(columnName, columnData);
                    tmp.add(columnData);
                    if (logger.isTraceEnabled()) {
                        logger.trace("COLUMN DATA: " + columnData);
                    }
                }
                dataVector.add(tmp);
            }
            logger.info("TABLE_MODEL: Query executed: " + new GregorianCalendar().getTime() + ", Time (MS): " + (new GregorianCalendar().getTime().getTime() - startTime.getTime()));
        } catch (SQLException ex) {
            throw ex;
        } finally {
            try {
                if (statementResult != null) {
                    statementResult.close();
                }
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ex) {
                logger.error(ex);
            }
        }
    }

    /**
     *
     * @return
     */
    public int getColumnCount() {
        return headerVector.size();
    }

    /**
     *
     * @return
     */
    public int getRowCount() {
        return dataVector.size();
    }

    /**
     * 
     * @param col
     * @return
     */
    public String getColumnName(int col) {
        return (String) headerVector.get(col);
    }

    /**
     *
     * @param c
     * @return
     */
    public Class<?> getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /**
     *
     * @param rowIndex
     * @param columnIndex
     * @return
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        return ((Vector) (dataVector.get(rowIndex))).get(columnIndex);
    }

    /**
     *
     * @return
     */
    public Vector<Vector<Object>> getDataVector() {
        return dataVector;
    }

    /**
     *
     * @return
     */
    public Vector getHeaderVector() {
        return headerVector;
    }

    /**
     *
     * @param row
     * @param col
     * @return
     */
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    /**
     *
     * @param e
     */
    public void tableChanged(TableModelEvent e) {
        fireTableChanged(e);
    }
    Color curColor;

    /**
     *
     * @return
     */
    public String getXmlString() {
        return xmlString;
    }

    /**
     *
     * @param fieldName
     * @param fieldData
     * @return
     */
    private Object getFormattedData(String fieldName, Object fieldData) {
        Object formattedData = fieldData;
        Format fmt = (Format) fieldsAndFormats.get(fieldName);
        if (fmt != null) {
            formattedData = fmt.format(fieldData);
        }
        return formattedData;
    }

    /**
     * Checks the database type and adds the database specific paging syntax
     * to the current SQL statement
     *
     * @param databaseType
     * @param currentSqlCommand
     * @param currentSqlParams
     * @param pageNumber
     * @param nbResByPage
     * @return HashMap - contains the prepared statement with paging and list of parameters
     */
    public static HashMap getSQLWithPaging(String databaseType, String currentSqlCommand,
            Vector<HashMap> currentSqlParams, int pageNumber, int nbResByPage) {

        HashMap<String, Object> result = new HashMap<String, Object>();
        StringBuffer completeSQL = new StringBuffer();
        Vector<HashMap> completeSqlParams = new Vector<HashMap>();
        completeSqlParams.addAll(currentSqlParams);

        if (databaseType.toLowerCase().contains("oracle")) {
            // Oracle Paging - Wraps a SQL statement with the ROWNUM command to enable retrieval of a
            // specified range of records.
            completeSQL.append(ROW_NUM_SELECT);
            completeSQL.append(currentSqlCommand);
            completeSQL.append(ROW_NUM_RANGE);
            completeSqlParams.add(setSqlParam(INTEGER, (nbResByPage * pageNumber) + nbResByPage));
            completeSqlParams.add(setSqlParam(INTEGER, nbResByPage * pageNumber));
        } else {
            // Appends the Limit and Offset commands to a SQL statement to enable retrieval of a
            // specified range of records.
            completeSQL.append(currentSqlCommand);
            completeSQL.append(ROW_LIMIT_OFFSET);
            completeSqlParams.add(setSqlParam(INTEGER, nbResByPage));
            completeSqlParams.add(setSqlParam(INTEGER, nbResByPage * pageNumber));
        }

        // Return the SQL Statement and Prepared Statement Parameters
        result.put("completeSQL", completeSQL);
        result.put("completeSqlParams", completeSqlParams);

        return result;
    }

    /**
     * Sets the bindings for a prepared statement
     *
     * @param pstmt - Prepared Statement
     * @param params - List of parameters for prepared statement
     * @return PreparedStatement - with bindings set
     */
    public static PreparedStatement setPStmtParameters(PreparedStatement pstmt, Vector<HashMap> params)
            throws SQLException {
        //logger.info("Num of Params: " + params.size() + ", Params: " + params);
        int j = 0;
        for (HashMap param : params) {
            j = j + 1;
            if (param.containsKey(INTEGER)) {
                pstmt.setInt(j, (Integer) param.get(INTEGER));
            } else if (param.containsKey(TIMESTAMP)) {
                //logger.debug("Param Timestamp: " + new java.sql.Timestamp(((java.util.Date) param.get(TIMESTAMP)).getTime()));
                pstmt.setTimestamp(j, new java.sql.Timestamp(((java.util.Date) param.get(TIMESTAMP)).getTime()));
            } else if (param.containsKey(DATE)) {
                //logger.debug("Param Date: " + new java.sql.Date(((java.util.Date) param.get(DATE)).getTime()));
                pstmt.setDate(j, new java.sql.Date(((java.util.Date) param.get(DATE)).getTime()));
            } else {
                pstmt.setString(j, (String) param.get(STRING));
            }
        }
        return pstmt;
    }

    /**
     *
     * @param type - parameter data type
     * @param param - parameter value
     * @return HashMap - contains the parameter value and it's data type
     */
    public static HashMap setSqlParam(String type, Object param) {
        HashMap<String, Object> sqlParam = new HashMap<String, Object>();
        sqlParam.put(type, param);
        return sqlParam;
    }
}
