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

import java.io.IOException;
import org.apache.log4j.Logger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.vangent.hieos.xutil.socket.UDPSocketSupport;
import com.vangent.hieos.xutil.socket.TLSSocketSupport;

/**
 * Supports ATNA Syslog Messages over:
 * UDP (RFC5426) with the Syslog Protocol (RFC5424) or
 * TLS (RFC5425) with the Syslog Protocol (RFC5424)
 *
 * @author Adeola O. / Bernie Thuman
 */
public class SysLogAdapter {

    private final static Logger logger = Logger.getLogger(SysLogAdapter.class);
    private final static String APP_NAME = "HIEOS";
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private String syslogHost = null;
    private int syslogPort = 514;
    private String protocol = null;
    private UDPSocketSupport udpSocket;
    private TLSSocketSupport tlsSocket;

    /**
     * Initialize the SysLogAdaptor with the target host, port and protocol.
     *
     * @param syslogHost Target syslog host.
     * @param syslogPort Target syslog port.
     * @param syslogProtocol Syslog protocol to use.
     */
    public SysLogAdapter(String syslogHost, int syslogPort, String syslogProtocol) {
        if (logger.isTraceEnabled()) {
            logger.trace("SYSLOG USING Protocol=" + syslogProtocol + ", Host=" + syslogHost + ", Port=" + syslogPort);
        }
        initialize(syslogHost, syslogPort, syslogProtocol);
    }

    /**
     * Initialize the SysLogAdaptor with the target host, port and protocol.
     *
     * @param syslogHost Target syslog host.
     * @param syslogPort Target syslog port.
     * @param syslogProtocol Syslog protocol to use.
     */
    private void initialize(String syslogHost, int syslogPort, String syslogProtocol) {
        if (logger.isTraceEnabled()) {
            logger.trace("Initializing ATNA/syslog using protocol=" + syslogProtocol);
        }
        this.protocol = syslogProtocol;
        this.syslogPort = syslogPort;
        this.syslogHost = syslogHost;

        // Instantiate either a UDP or TLS Socket
        if (this.protocol.equalsIgnoreCase("UDP")) {
            udpSocket = new UDPSocketSupport(syslogHost, syslogPort);

        } else {
            tlsSocket = new TLSSocketSupport(syslogHost, syslogPort);
        }
    }

    /**
     * Write the message using eith UDP or TLS.
     *
     * @param msg The message to write.
     */
    public void write(String msg) {
        // Format message in RFC5424 syslog format
        String syslogMsg = buildSysLogMessage(msg);

        if (this.protocol.equalsIgnoreCase("UDP")) {
            // UDP Protocol
            udpSocket.write(syslogMsg);
        } else {
            // TLS Protocol
            try {
                tlsSocket.sendSecureMessage(syslogHost, syslogPort, syslogMsg);
            } catch (IOException ex) {
                logger.error("TLS Connection could not be established with Audit Repository" +
                        ", NO TLS ATNA logs will be captured: " + ex);
            }
        }
    }

    /**
     * Creates the Syslog (RFC 5424) message.
     *
     * @param msg The message to write.
     * @param String - the syslog message
     *
     */
    private String buildSysLogMessage(String msg) {
        java.util.Date date = new java.util.Date();
        String currentDateTime = dateFormat.format(date);

        // Get the host name for the local host:
        String localHostName;
        try {
            localHostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.error("Could not find localhost name", e);
            localHostName = "localhost";
        }

        // See http://www.faqs.org/rfcs/rfc5424.html for format:
        // PRI = <85> (10 * 8 + 5)
        // VERSION = 1
        // TIMESTAMP
        // HOSTNAME
        // APP-NAME
        // PROCID
        // MSGID
        String syslogMsg = "<85>1 " + currentDateTime + " " + localHostName + " " + APP_NAME + " " + "- " + "- " + "- " + msg;
        return syslogMsg;
    }
}
