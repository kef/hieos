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
package com.vangent.hieos.xutil.atna;

import com.vangent.hieos.xutil.xconfig.XConfig;
import org.apache.log4j.Logger;
import org.productivity.java.syslog4j.SyslogConfigIF;
import org.productivity.java.syslog4j.SyslogIF;
import org.productivity.java.syslog4j.Syslog;

/**
 *
 * @author Adeola O.
 */
public class SysLogAdapter {

    private SyslogIF syslog = null;
    private final static Logger logger = Logger.getLogger(SysLogAdapter.class);

    /**
     *
     * @param host
     * @param port
     * @param protocol
     */
    public SysLogAdapter(String host, int port, String protocol) {
        if (logger.isTraceEnabled()) {
            logger.trace("SYSLOG USING Protocol=" + protocol + ", Host=" + host + ", Port=" + port);
        }
        initialize(host, port, protocol);
    }

    /**
     *
     * @param host
     * @param port
     * @param protocol
     */
    private void initialize(String host, int port, String protocol) {
        syslog = Syslog.getInstance(protocol);
        if (logger.isTraceEnabled()) {
            logger.trace("INITIALIZING SYSLOG USING Protocol=" + protocol);
        }

        // Initialize the Syslog message length
        int syslogMaxLength = 1024;
        try {
            syslogMaxLength = new Integer(XConfig.getInstance().getHomeCommunityProperty("ATNAsyslogMaxLength")).intValue();
        } catch (Exception ex) {
            logger.error("Error retrieving SysLogMaxlength from Config file: " + ex);
        }
        SyslogConfigIF config = syslog.getConfig();
        config.setHost(host);
        config.setPort(port);
        config.setMaxMessageLength(syslogMaxLength);
        config.setUseStructuredData(true);
        syslog.initialize("udp", config);
        if (logger.isTraceEnabled()) {
            logger.trace("SYSLOG USING Protocol=" + syslog.getProtocol() + ", Host=" +
                    syslog.getConfig().getHost() + ", Port=" + syslog.getConfig().getPort());
        }
    }

    /**
     *
     * @param msg
     */
    public void writeLog(String msg) {
        if (syslog == null) {
            logger.error("ERROR: Fatal: SyslogWriter is null. Will not write audit message");
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("LOG USING: Max length=" + syslog.getConfig().getMaxMessageLength() + ",Host=" +
                        syslog.getConfig().getHost() + ", Port=" + syslog.getConfig().getPort() +
                        ", Protocol=" + syslog.getProtocol());
            }
            syslog.info(msg);
        }
    }

    /**
     *
     */
    public void close() {
        if (syslog != null) {
            syslog.flush();
        }
    }
}
