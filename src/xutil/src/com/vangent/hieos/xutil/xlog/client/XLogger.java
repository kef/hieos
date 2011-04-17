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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vangent.hieos.xutil.xlog.client;

import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.jms.JMSHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.naming.NamingException;

import java.util.GregorianCalendar;
import java.util.UUID;

import com.vangent.hieos.xutil.xconfig.XConfig;

/**
 *
 * @author Bernie Thuman
 */
public class XLogger {

    static XLogger _instance = null;
    boolean logEnabled = false;  // Default (no logging).

    /**
     *
     * @return
     */
    synchronized static public XLogger getInstance() {
        if (_instance == null) {
            _instance = new XLogger();
        }
        return _instance;
    }

    /**
     *
     */
    private XLogger() {
        this.setLogEnabled();
    }

    /**
     * 
     * @return
     */
    public XLogMessage getNewMessage(String ipAddress) {
        String id = UUID.randomUUID().toString();
        // Create the corresponding log message:
        XLogMessage m = new XLogMessage(this, id);
        // Now set a reasonable timestamp (down to milliseconds):
        GregorianCalendar cal = new GregorianCalendar();
        long currentTime = cal.getTimeInMillis();
        m.setIpAddress(ipAddress);
        m.setTimeStamp(currentTime);
        return m;
    }

    /**
     *
     * @param messageData
     */
    protected void store(XLogMessage messageData) {
        if (this.logEnabled == true) {
            try {
                this.sendJMSMessageToXLogger(messageData);
            } catch (NamingException ex) {
                Logger.getLogger(XLogger.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JMSException ex) {
                Logger.getLogger(XLogger.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     *
     * @param messageData
     * @throws javax.naming.NamingException
     * @throws javax.jms.JMSException
     */
    private void sendJMSMessageToXLogger(XLogMessage messageData) throws NamingException, JMSException {
        JMSHandler jms = new JMSHandler("jms/XLoggerFactory", "jms/XLogger");
        try {
            jms.createConnectionFactoryFromPool();
            jms.createJMSSession();
            jms.sendMessage(messageData);
        } finally {
            jms.close();
        }
    }

    /**
     * 
     * @return
     */
    public boolean isLogEnabled() {
        return this.logEnabled;
    }

    /**
     *
     */
    private void setLogEnabled() {
        try {
            this.logEnabled = XConfig.getInstance().getHomeCommunityConfigPropertyAsBoolean("LogEnabled");
        } catch (XdsInternalException ex) {
            Logger.getLogger(XLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
