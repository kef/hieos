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

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

public class GetCompanyServlet extends HttpServlet {

    private final static Logger logger = Logger.getLogger(GetCompanyServlet.class);

    /**
     *
     * @param config
     * @throws javax.servlet.ServletException
     */
    public void init(ServletConfig config) throws ServletException {
        // Do nothing.
    }

    /**
     *
     * @param req
     * @param res
     */
    public void doGet(HttpServletRequest req, HttpServletResponse res) {
        // Create prepared statements.
        PreparedStatement selectACompanyName = null;
        PreparedStatement selectAllCompanyName = null;
        Log log = new Log();
        try {
            Connection con = log.getConnection();
            selectACompanyName = con.prepareStatement("SELECT distinct company_name FROM ip WHERE ip = ?");
            selectAllCompanyName = con.prepareStatement("SELECT distinct company_name FROM ip WHERE company_name != 'Unknown' order by company_name");

            // Write XML to response.
            String option = req.getParameter("option");
            String ip = req.getParameter("ip");
            if (ip != null) {
                res.setContentType("text/xml");
                res.getWriter().write("<result>");
                selectACompanyName.setString(1, ip);
                ResultSet result = selectACompanyName.executeQuery();
                while (result.next()) {
                    res.getWriter().write(
                            "<ip " + (req.getParameter("colID") != null ? "colID='" + req.getParameter("colID") + "'"
                            : "") + ">" + result.getString(1) + "</ip>");
                }
                res.getWriter().write("</result>");
            } else if (option.equals("all")) {
                res.setContentType("text/xml");
                StringBuffer buff = new StringBuffer();
                buff.append("<result>");
                ResultSet result = null;
                result = selectAllCompanyName.executeQuery();
                while (result != null && result.next()) {
                    buff.append("<company >" + result.getString(1) + "</company>");
                }
                buff.append("</result>");
                res.getWriter().write(buff.toString());
            }
        } catch (LoggerException e) {
            getError(e, res);
            e.printStackTrace();
        } catch (SQLException e) {
            getError(e, res);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally{
            try {
                log.closeConnection();
            } catch (LoggerException ex) {
                logger.error(ex);
            }
        }
    }

    /**
     *
     * @param e
     * @param response
     */
    private void getError(Exception e, HttpServletResponse response) {
        PrintWriter print;
        try {
            print = response.getWriter();
            response.setContentType("text/xml");
            StringBuffer toPrint = new StringBuffer();
            StringBuffer toPrint2 = new StringBuffer();
            toPrint.append("<result>");
            toPrint.append("<error>");
            toPrint.append(e.getClass().toString() + ":" + e.getMessage());
            toPrint2.append(e.getClass().toString() + ":" + e.getMessage() + "\n");
            StackTraceElement[] stack = e.getStackTrace();
            for (int i = 0; i < stack.length; i++) {
                toPrint2.append(stack[i].toString() + "\n");
            }
            toPrint.append("</error>");
            toPrint.append("</result>");
            print.write(toPrint.toString());
        } catch (IOException e1) {
            logger.error(e1);
        }
    }
}