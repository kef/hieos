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
package com.vangent.hieos.xutil.xlog.client;

import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.xconfig.XConfig;
import java.util.GregorianCalendar;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class XLogger {

    private final static Logger logger = Logger.getLogger(XLogger.class);
    private static XLogger _instance = null;  // Singleton.
    private boolean logEnabled = false;
    private XLogListener logListener;

    /**
     *
     * @return
     */
    synchronized static public XLogger getInstance() {
        if (_instance == null) {
            _instance = new XLogger();
            _instance.setLogEnabledState();
        }
        return _instance;
    }

    /**
     *
     */
    private XLogger() {
        // Do not allow construction by outside parties - Singleton.
    }

    /**
     *
     * @return
     */
    public boolean isLogEnabled() {
        return logEnabled;
    }

    /**
     * 
     */
    synchronized public void startup() {
        if (!logEnabled) {
            return;  // Early exit.
        }
        if (logListener == null) {
            logListener = new XLogListener();
            logListener.startup();
        }
    }

    /**
     *
     */
    synchronized public void shutdown() {
        if (!logEnabled) {
            return;  // Early exit.
        }
        if (logListener != null) {
            logListener.shutdownAndAwaitTermination();
            logListener = null;
        }
    }

    /**
     *
     * @param ipAddress
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
        if (logEnabled && (logListener != null)) {
            try {
                LinkedBlockingQueue<XLogMessage> logMessageQueue = logListener.getLogMessageQueue();
                boolean offerStatusResult = logMessageQueue.offer(messageData);
                //logger.info("Put message on XLogger message queue - " + messageData.getMessageID());
                if (!offerStatusResult) {
                    logger.warn("XLogger unable to place message on log queue");
                }
                //this.sendJMSMessageToXLogger(messageData);
            } catch (Exception ex) {
                logger.warn("XLogger exception trying to place message on log queue:", ex);
            }
        }
    }

    /**
     *
     */
    private void setLogEnabledState() {
        try {
            logEnabled = XConfig.getInstance().getHomeCommunityConfigPropertyAsBoolean("LogEnabled");
        } catch (XdsInternalException ex) {
            logger.warn("XLogger XLogger exception: " + ex.getMessage());
        }
    }
}
