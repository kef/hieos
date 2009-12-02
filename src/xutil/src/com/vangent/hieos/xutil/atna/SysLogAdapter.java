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
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * ATNA Syslog Messages over UDP (RFC5426) with The Syslog Protocol (RFC5424)
 *
 * @author Adeola O. / Bernie Thuman
 */
public class SysLogAdapter {

    private final static Logger logger = Logger.getLogger(SysLogAdapter.class);
    private final static String APP_NAME = "HIEOS";
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private InetAddress syslogHostAddress = null;
    private String localHostName = null;
    private DatagramSocket socket = null;
    private int syslogPort = 514;

    /**
     * Initialize the SysLogAdaptor with the target host, port and protocl (now only "udp").
     *
     * @param syslogHost Target syslog host.
     * @param syslogPort Target syslog port.
     * @param syslogProtocol Syslog protocol to use (now only "udp").
     */
    public SysLogAdapter(String syslogHost, int syslogPort, String syslogProtocol) {
        if (logger.isTraceEnabled()) {
            logger.trace("SYSLOG USING Protocol=" + syslogProtocol + ", Host=" + syslogHost + ", Port=" + syslogPort);
        }
        initialize(syslogHost, syslogPort, syslogProtocol);
    }

     /**
     * Initialize the SysLogAdaptor with the target host, port and protocl (now only "udp").
     *
     * @param syslogHost Target syslog host.
     * @param syslogPort Target syslog port.
     * @param syslogProtocol Syslog protocol to use (now only "udp").
     */
    private void initialize(String syslogHost, int syslogPort, String syslogProtocol) {
        if (logger.isTraceEnabled()) {
            logger.trace("Initializing ATNA/syslog using protocol=" + syslogProtocol);
        }
        this.syslogPort = syslogPort;

        // Initialize the Syslog message length (not used right now).
        /*
        int syslogMaxLength = 1024;
        try {
        syslogMaxLength = new Integer(XConfig.getInstance().getHomeCommunityProperty("ATNAsyslogMaxLength")).intValue();
        } catch (Exception ex) {
        logger.error("Error retrieving SysLogMaxlength from Config file: " + ex);
        }*/

        // Get the address for the target syslog host:
        try {
            this.syslogHostAddress = InetAddress.getByName(syslogHost);
        } catch (UnknownHostException e) {
            logger.error("Could not find " + syslogHost + ". All ATNA logging will fail!", e);
        }

        // Get the host name for the local host:
        try {
            this.localHostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.error("Could not find localhost name", e);
            this.localHostName = "localhost";
        }

        // Create new datagram socket:
        try {
            this.socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
            logger.error("Could not instantiate DatagramSocket. All ATNA logging will fail!", e);
        }
    }

    /**
     * Write the message to the syslog.
     *
     * @param msg The message to write.
     */
    public void write(String msg) {
        if (this.socket != null) {
            java.util.Date date = new java.util.Date();
            String currentDateTime = dateFormat.format(date);

            // See http://www.faqs.org/rfcs/rfc5424.html for format:
            // PRI = <85> (10 * 8 + 5)
            // VERSION = 1
            // TIMESTAMP
            // HOSTNAME
            // APP-NAME
            // PROCID
            // MSGID
            String syslogMsg = "<85>1 " + currentDateTime + " " + this.localHostName + " " + APP_NAME + " " + "- " + "- " + "- " + msg;
            //System.out.println("xxx: Syslog msg=[" + syslogMsg + "]");

            byte[] bytes = syslogMsg.getBytes();
            // syslog packets must be less than 1024 bytes (Ignore for now).
            int bytesLength = bytes.length;
            DatagramPacket packet = new DatagramPacket(bytes, bytesLength,
                    this.syslogHostAddress, this.syslogPort);
            try {
                this.socket.send(packet);
                this.socket.close();
            } catch (IOException e) {
                logger.error("Unable to send ATNA message.", e);
            }
        }
    }
}
