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
package com.vangent.hieos.logbrowser.servlets;

import com.vangent.hieos.logbrowser.log.db.Log;
import com.vangent.hieos.logbrowser.log.db.LoggerException;

import com.vangent.hieos.logbrowser.util.TableModel;
import com.vangent.hieos.logbrowser.util.TableSorter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

/**
 *  
 * This class is a servlet used to display xds messages received by the server implemented     <br/>
 * in the xdslog project. This servlet implement strictly a doPost method. This servlet should <br/>
 * receive several paramaters such as : <br/>
 * <ul>
 *	<li>sort : column number to sort  </li>
 *	<li>option : the word "count" can be used to have the number of result</li>
 *	<li>page : page number </li>
 *  <li>nbResByPage : number of result by pages (50 by default)</li>
 *  <li>optioni : option number i  </li> 
 *  <li>valuei  :  value number i . These values are the result of the user's choice in the web interface </li>
 * </ul>
 * 
 * @author jbmeyer
 *
 */
public class GetTableServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final static Logger logger = Logger.getLogger(GetTableServlet.class);
    private final int MAX_RESULTS_BY_PAGE = 50;

    private TableModel tableModel;
    private TableSorter sorter;
    private ServletConfig currentConfig;
    private HashMap<String, String> map;
    private boolean isAdmin;
    private String currentIP;

    /**
     *
     * @param config
     * @throws javax.servlet.ServletException
     */
    public void init(ServletConfig config) throws ServletException {
        currentConfig = config;
    }

    /**
     *
     * @param req
     * @param res
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res) {
        Log log = new Log();
        try {
            HttpSession session = req.getSession(true);
            session.setAttribute("systemType", "new");
            Boolean isAdmin_ = (Boolean) session.getAttribute("isAdmin");

            if (isAdmin_ == null) {
                isAdmin = false;
            } else {
                isAdmin = isAdmin_.booleanValue();
            }
            currentIP = req.getRemoteAddr();
            String sort = req.getParameter("sort");
            String option = req.getParameter("option");
            String page = req.getParameter("page");
            if (page == null) {
                page = (String) session.getAttribute("page");
            }
            String numberResultsByPage = req.getParameter("nbResByPage");

            String currentSqlCommand;
            Vector<HashMap> currentSqlParams = new Vector<HashMap>();
            Integer numberOfResults = (Integer) session.getAttribute("numberOfResults");
            if (logger.isDebugEnabled()){
                logger.debug("Option/Page/TotRows: " + option + "/" + page + "/" + numberOfResults);
            }

            // Write XML to response.
            res.setContentType("text/javascript");
            if (option != null && option.equals("count")) {

                // Only get Count if new search or the filter has changed
                if (page == null || page.equals("0")){
                    countResults(session, log.getConnection());
                    numberOfResults = (Integer) session.getAttribute("numberOfResults");
                    if (logger.isInfoEnabled()){
                        logger.info("New TotRows: " + numberOfResults);
                    }
                }
                
                numberResultsByPage = (String) session.getAttribute("numberResultsByPage");
                page = (String) session.getAttribute("page");
                StringBuffer buffer = new StringBuffer();
                buffer.append("{ \"result\" : \n");
                buffer.append(new JSONStringer().object().key("pageNumber").value(page).key("numberResultsByPage").value(numberResultsByPage).key("numberOfResults").value(numberOfResults).endObject().toString());
                buffer.append("}");
                if (logger.isDebugEnabled()){
                    logger.debug("PAGE HEADER:" + buffer.toString());
                }
 
                res.getWriter().println(buffer.toString());
            } else if (sort == null) {

                // Get search criteria and database type from session
                getOptions(req, session);
                String databaseType = (String)session.getAttribute("databaseType");
                if (databaseType == null){
                    databaseType = log.getDatabaseType();
                    session.setAttribute("databaseType", databaseType);
                }

                // Build the SQL Query based on search criteria
                sqlCommandProcessing(session, (Integer) session.getAttribute("optionNumber"), 
                        page, numberResultsByPage, databaseType);
                currentSqlCommand = (String) (session.getAttribute("currentSqlCommand"));
                currentSqlParams = (Vector) (session.getAttribute("currentSqlParams"));
 
                session.setAttribute("page", page);
                session.setAttribute("numberResultsByPage", numberResultsByPage);

                Map fieldsAndFormats = new HashMap();
                Format fmt = new SimpleDateFormat("EEE d MMM - HH:mm:ss.SSS");
                fieldsAndFormats.put("Timestamp", fmt);

                // Execute the SQL Query and Retrieve the log data
                Connection con = log.getConnection();
                executeCurrentSqlCommand(con, currentSqlCommand, currentSqlParams, fieldsAndFormats);

                // Format Output
                res.getWriter().write(toJSon(-1, -2));

            } else if (sort != null) {
                res.getWriter().write(sortColomn(sort));
            } else {
                throw new Exception(
                        "Error case unknown not sort either display table");
            }
        } catch (SQLException e) {
            getError(e, res);
            logger.error(e.getMessage());
        } catch (FileNotFoundException e) {
            getError(e, res);
            logger.error(e.getMessage());
        } catch (NumberFormatException e) {
            getError(e, res);
            logger.error(e.getMessage());
        } catch (IOException e) {
            getError(e, res);
            logger.error(e.getMessage());
        } catch (Exception e) {
            getError(e, res);
            logger.error(e.getMessage());
        } finally {
            try {
                log.closeConnection();
            } catch (LoggerException ex) {
                logger.error(ex);
            }
        }
    }

    /**
     * <b>sqlCommandProcessing</b><br/>
     * Create the sql command beginning with the sql command specified in the web.xml file and applying all the options <br/>
     * specified by the user. This command also apply a limit in the results to avoid to overload the server
     *
     */
    private void sqlCommandProcessing(HttpSession session, int optionNumberInt,
            String page, String numberResultsByPage, String databaseType) throws LoggerException{
        int parameterNumber = 1;
        reInitSqlCommand(session);
        String currentSqlCommand = (String) session.getAttribute("currentSqlCommand");
        Vector currentSqlParams = new Vector();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (logger.isDebugEnabled()){
            logger.debug("SQL-BASE: " + currentSqlCommand);
        }

        // If search criteria was entered, build the WHERE clause to
        // include the filter data entered
        if (map != null && optionNumberInt > 0) {
            String commandTemp = currentSqlCommand;
            if (currentSqlCommand.toLowerCase().indexOf("where") > -1) {
                commandTemp += " AND ";
            } else {
                commandTemp += " WHERE ";
            }

            while (parameterNumber <= optionNumberInt) {
                if (map.containsKey("option" + parameterNumber)) {
                    if (map.containsKey("and-or" + parameterNumber)) {
                        commandTemp += " " + map.get("and-or" + parameterNumber) + " ";
                    }

                    String parameterName = map.get("option" + parameterNumber).toString();

                    if (parameterName.equals("ip")) {
                        commandTemp += " main.ip LIKE ?";
                        currentSqlParams.add(TableModel.setSqlParam(TableModel.STRING, "%" + map.get("value" + parameterNumber).toString() + "%"));

                    } else if (parameterName.equals("pass")) {
                        commandTemp += " main.pass = ?";
                        currentSqlParams.add(TableModel.setSqlParam(TableModel.STRING, map.get("value" + parameterNumber).toString()));

                    } else if (parameterName.equals("test")) {
                        commandTemp += "  main.test = ?";
                        currentSqlParams.add(TableModel.setSqlParam(TableModel.STRING, map.get("value" + parameterNumber).toString()));

                    } else if (parameterName.equals("company")) {
                        commandTemp += "  ip.company_name = ?";
                        currentSqlParams.add(TableModel.setSqlParam(TableModel.STRING, map.get("value" + parameterNumber).toString()));
                        
                    } else if (parameterName.equals("date1")) {
                        String date1 = map.get("value" + parameterNumber).toString().trim();
                        java.util.Date d1;
                        GregorianCalendar endday = new GregorianCalendar();
                        try {
                            d1 = sdf.parse(date1);
                            if (map.containsKey("option" + (parameterNumber + 1)) && map.get(
                                    "option" + (parameterNumber + 1)).toString().equals("date2")) {
                                if (map.containsKey("value" + (parameterNumber + 1))) {
                                    String date2 = map.get(
                                            "value" + (parameterNumber + 1)).toString().trim();
                                    java.util.Date d2 = sdf.parse(date2);
                                    if (d2.getTime() > d1.getTime()) {
                                        endday.setTimeInMillis(d2.getTime());
                                        endday.roll(Calendar.DAY_OF_MONTH, +1);
                                        d2 = endday.getTime();
                                        commandTemp += " timereceived >= ? and timereceived < ?";
                                    } else if (d2.getTime() < d1.getTime()) {
                                        endday.setTimeInMillis(d1.getTime());
                                        endday.roll(Calendar.DAY_OF_MONTH, +1);
                                        d1 = endday.getTime();
                                        commandTemp += " timereceived < ? and timereceived >= ?";
                                    } else {
                                        // From Date same as To Date
                                        endday.setTimeInMillis(d2.getTime());
                                        endday.roll(Calendar.DAY_OF_MONTH, +1);
                                        d2 = endday.getTime();
                                        commandTemp += " timereceived >= ? and timereceived < ?";
                                    }
                                    currentSqlParams.add(TableModel.setSqlParam(TableModel.DATE, d1));
                                    currentSqlParams.add(TableModel.setSqlParam(TableModel.DATE, d2));

                                    map.remove("value" + (parameterNumber + 1));
                                    map.remove("option" + (parameterNumber + 1));
                                    if (map.containsKey("and-or" + (parameterNumber + 1))) {
                                        map.remove("and-or" + (parameterNumber + 1));
                                    }
                                }
                            } else {
                                // To Date not provided
                                commandTemp += " timereceived >= ? ";
                                currentSqlParams.add(TableModel.setSqlParam(TableModel.DATE, d1));
                            }
                        } catch (ParseException e) {
                            logger.error("Date Parsing Error: " + e);
                        }
                    } else if (parameterName.equals("date2")) {
                        // From Date not provided
                        String date1 = map.get("value" + parameterNumber).toString();
                        java.util.Date d1 = new  java.util.Date();
                        try {
                            d1 = sdf.parse(date1);
                        } catch (ParseException ex) {
                            logger.error("Date Parsing Error: " + ex);
                        }
                        GregorianCalendar endday = new GregorianCalendar();
                        endday.setTime(d1);
                        endday.roll(Calendar.DAY_OF_MONTH, +1);
                        commandTemp += " timereceived < ?";
                        currentSqlParams.add(TableModel.setSqlParam(TableModel.DATE, endday.getTime()));

                    } else if (parameterName.equals("date")) {
                        String value = map.get("value" + parameterNumber).toString();
                        GregorianCalendar today = new GregorianCalendar();
                        GregorianCalendar endday = new GregorianCalendar();

                        if (value.equals("today")) {
                            endday.roll(Calendar.DAY_OF_MONTH, +1);
                        } else if (value.equals("yesterday")) {
                            today.roll(Calendar.DAY_OF_MONTH, -1);
                        } else if (value.equals("2days")) {
                            today.roll(Calendar.DAY_OF_MONTH, -2);
                            endday.roll(Calendar.DAY_OF_MONTH, -1);
                        } else if (value.equals("3days")) {
                            today.roll(Calendar.DAY_OF_MONTH, -3);
                            endday.roll(Calendar.DAY_OF_MONTH, -2);
                        }
                        commandTemp += " timereceived >= ? and timereceived < ?";
                        currentSqlParams.add(TableModel.setSqlParam(TableModel.DATE, today.getTime()));
                        currentSqlParams.add(TableModel.setSqlParam(TableModel.DATE, endday.getTime()));
                    }
                }
                parameterNumber++;
            }
            if (logger.isDebugEnabled()){
                logger.debug("SQL-WITH-FILTERS: " + commandTemp);
            }
            currentSqlCommand = commandTemp;
        }


        // Save SQL for count(*) command
        session.setAttribute("countSqlCommand", currentSqlCommand);
        session.setAttribute("countSqlParams", currentSqlParams);

        // Add Paging logic to the SQL Statement
        if (logger.isDebugEnabled()){
            logger.debug("GetTableServlet: SQLRequest before paging: >" + currentSqlCommand);
        }
        
        int nbResByPage = MAX_RESULTS_BY_PAGE;
        if (numberResultsByPage != null) {
                nbResByPage = new Integer(numberResultsByPage).intValue();
        }
        int pageNumber = 0;
        if (page != null) {
            pageNumber = new Integer(page).intValue();
        }
        
        // Add database specific paging logic to the SQL command
        HashMap sqlMap = TableModel.getSQLWithPaging(databaseType, currentSqlCommand,
                currentSqlParams, pageNumber, nbResByPage);
        StringBuffer completeSQL = (StringBuffer) sqlMap.get("completeSQL");
        Vector<HashMap> completeSqlParams = (Vector<HashMap>) sqlMap.get("completeSqlParams");

        // Save the SQL Command
        session.setAttribute("currentSqlCommand", completeSQL.toString());
        session.setAttribute("currentSqlParams", completeSqlParams);
        if (logger.isInfoEnabled()){
            logger.debug("LogBrowser Page: " + page);
            logger.info("LogBrowser SQL (with paging): " + completeSQL);
        }
    }

    /**
     * 
     * @param sqlCommand
     * @param fieldsAndFormats
     * @throws com.vangent.hieos.logbrowser.log.db.LoggerException
     */
    private void executeCurrentSqlCommand(Connection con, String sqlCommand, Vector<HashMap> sqlParams, Map fieldsAndFormats) throws SQLException {
        tableModel = new TableModel(sqlCommand, sqlParams, fieldsAndFormats, con);
        sorter = new TableSorter(tableModel);
    }

    /**
     *
     * @param session
     */
    private void reInitSqlCommand(HttpSession session) {
        session.setAttribute("currentSqlCommand", currentConfig.getInitParameter("sqlCommand"));
    }

    /**
     *
     * Use the current sql command with a Count(*) to return the number of results of the current <br/>
     * sql command
     * @throws SQLException
     */
    private void countResults(HttpSession session, Connection con) throws SQLException {
        String currentSqlCommand = (String) session.getAttribute("countSqlCommand");
        Vector sqlParams = (Vector) session.getAttribute("countSqlParams");
        int fromPosition = currentSqlCommand.toLowerCase().indexOf("from");
        String secondPart = currentSqlCommand.substring(fromPosition);

        // AMS - MySQL does not like spaces between COUNT and (*)
        String SQLCommandCountStar = "SELECT COUNT(*) " + secondPart;
        if (logger.isDebugEnabled()){
            logger.debug("GetTableServlet: SQLCommandCountStar: " + SQLCommandCountStar);
        }
        logger.info("GetTableServlet: SQLCommandCountStar: " + SQLCommandCountStar);
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try{
            pstmt = con.prepareStatement(SQLCommandCountStar);
            pstmt = TableModel.setPStmtParameters(pstmt, sqlParams);
            
            resultSet = pstmt.executeQuery();
            resultSet.next();
            int numberOfResults = resultSet.getInt(1);
            session.setAttribute("numberOfResults", new Integer(numberOfResults));
        }catch (SQLException ex) {
            throw ex;
        }finally {
            try{
                if (resultSet!= null ){
                    resultSet.close();
                }
                if (pstmt != null ){
                    pstmt.close();
                }
            }catch(SQLException ex) {
                logger.error(ex);
            }
        }
    }

    /**
     *
     * @param sort, the column number to sort
     * @return the new xml string of the array sorted
     * @throws NumberFormatException
     *
     * The sort of this array is cyclic : it begins by UNSORTED, then if the user click again the array will be <br />
     * sorted ASCENDING, then DESCENDING, and finally UNSORTED.
     */
    private String sortColomn(String sort) throws NumberFormatException {
        if (tableModel != null && sorter != null) {
            int sortingColumn = new Integer(sort).intValue();
            int sortingStatus = -2;

            if (logger.isDebugEnabled()){
                logger.debug("GetTableServlet: Sort column :" + sortingColumn);
            }
            if (sorter.getSortingStatus(sortingColumn) == TableSorter.ASCENDING) {
                for (int i = 0; i < sorter.getColumnCount(); i++) {
                    sorter.setSortingStatus(i, TableSorter.NOT_SORTED);
                }
                sorter.setSortingStatus(sortingColumn, TableSorter.DESCENDING);
                sortingStatus = TableSorter.DESCENDING;
            } else if (sorter.getSortingStatus(sortingColumn) == TableSorter.DESCENDING) {
                for (int i = 0; i < sorter.getColumnCount(); i++) {
                    sorter.setSortingStatus(i, TableSorter.NOT_SORTED);
                }
                sorter.setSortingStatus(sortingColumn, TableSorter.NOT_SORTED);
                sortingStatus = TableSorter.NOT_SORTED;
            } else if (sorter.getSortingStatus(sortingColumn) == TableSorter.NOT_SORTED) {
                for (int i = 0; i < sorter.getColumnCount(); i++) {
                    sorter.setSortingStatus(i, TableSorter.NOT_SORTED);
                }
                sorter.setSortingStatus(sortingColumn, TableSorter.ASCENDING);
                sortingStatus = TableSorter.ASCENDING;
            }
            if (logger.isDebugEnabled()){
                logger.debug("GetTableServlet: sorting status " + sortingStatus);
            }

            return toJSon(sortingColumn, sortingStatus);
        }
        return null;
    }

    /**
     * Allows to get all options given by the user and passed to the server with the post method
     * @param req
     */
    private void getOptions(HttpServletRequest req, HttpSession session) {
        String optionNumber = req.getParameter("optionsNumber");
        int optionNumberInt = 0;
        if (optionNumber != null) {
            map = new HashMap<String, String>();
            optionNumberInt = new Integer(optionNumber).intValue();
            for (int i = 1; i < optionNumberInt + 1; i++) {
                if (req.getParameter("option" + i) != null) {
                    map.put("option" + i, req.getParameter("option" + i));
                }
                if (req.getParameter("value" + i) != null) {
                    map.put("value" + i, req.getParameter("value" + i));
                }

                if (i > 1 && req.getParameter("and-or" + i) != null) {
                    map.put("and-or" + i, req.getParameter("and-or" + i));
                }
            }
        }

        if (!isAdmin && currentIP != null && !currentIP.equals("")) {
            optionNumberInt++;
            if (!map.containsValue("ip")) {
                map.put("option" + optionNumberInt, "ip");
                map.put("value" + optionNumberInt, currentIP);
                if (optionNumberInt > 1) {
                    map.put("and-or" + optionNumberInt, "and");
                }
            }
        }
        session.setAttribute("optionNumber", (Integer) optionNumberInt);
    }

    /**
     *
     * @param column
     * @param sortingStatus
     * @return
     */
    private String toJSon(int column, int sortingStatus) {
        try {
            JSONObject response = new JSONObject();
            JSONObject content = new JSONObject();
            JSONArray array = new JSONArray();
            if (column == -1 || sortingStatus == -2) {
                array.put(tableModel.getHeaderVector());
            } else if (column > -1 && sortingStatus > -2 && sortingStatus < 2 /*-1,0 or 1 */) {
                Vector<String> vectorCopy = (Vector<String>) tableModel.getHeaderVector().clone();
                for (int header = 0; header < tableModel.getHeaderVector().size(); header++) {
                    if (header == column) {
                        switch (sortingStatus) {
                            case -1:
                                vectorCopy.set(header, vectorCopy.elementAt(header) + " &#8595;");
                                break;
                            case 0:
                                vectorCopy.set(header, vectorCopy.elementAt(header) + " &#8593;&#8595;");
                                break;
                            case 1:
                                vectorCopy.set(header, vectorCopy.elementAt(header) + " &#8593;");
                                break;
                        }
                    }
                }
                array.put(vectorCopy);
            }
            for (int row = 0; row < tableModel.getDataVector().size(); row++) {
                array.put((Vector<Object>) tableModel.getDataVector().get(row));
            }
            content.put("table", array);
            content.put("isAdmin", new Boolean(isAdmin).toString());
            response.put("result", content);
            return response.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param e
     * @param response
     */
    private void getError(Exception e, HttpServletResponse response) {
        PrintWriter print;
        // Do not display database or other system errors to the client
        final String errormessage = "A System Error Occurred, Contact the Administrator";

        try {
            print = response.getWriter();
            response.setContentType("text/javascript");
            StringBuffer toPrint = new StringBuffer();
            StringBuffer toPrint2 = new StringBuffer();
            toPrint.append("{ \"result\":");

            JSONStringer stringer = new JSONStringer();
            stringer.object();
            stringer.key("error");
            //stringer.value(e.getClass().toString() + ":" + e.getMessage());
            stringer.value(errormessage);
            stringer.endObject();
            toPrint.append(stringer.toString());
            toPrint2.append(e.getClass().toString() + ":" + e.getMessage() + "\n");

            StackTraceElement[] stack = e.getStackTrace();
            for (int i = 0; i < stack.length; i++) {
                toPrint2.append(stack[i].toString() + "\n");

            }
            toPrint.append("}");
            print.write(toPrint.toString());
            logger.error(toPrint2.toString());
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (JSONException e2) {
            e2.printStackTrace();
        }
    }
}
