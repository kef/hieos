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
package com.vangent.hieos.xutil.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.apache.log4j.Logger;

/**
 * Send Messages over UDP (RFC5426) Protocol
 *
 * @author Adeola Odunlami / Bernie Thuman
 */
public class UDPSocketSupport {

    private final static Logger logger = Logger.getLogger(UDPSocketSupport.class);
    private InetAddress hostAddress = null;
    private DatagramSocket socket = null;
    private int port = 514;

    public UDPSocketSupport(String syslogHost, int syslogPort) {
        initialize(syslogHost, syslogPort);
    }

    /**
     * Initialize the class with the target host and port.
     *
     * @param syslogHost Target syslog host.
     * @param syslogPort Target syslog port.
     */
    private void initialize(String syslogHost, int syslogPort) {
        if (logger.isTraceEnabled()) {
            logger.trace("Initializing syslog using UDP protocol=");
        }
        this.port = syslogPort;

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
            this.hostAddress = InetAddress.getByName(syslogHost);
        } catch (UnknownHostException e) {
            logger.error("Could not find " + syslogHost + ". All ATNA UDP logging will fail!", e);
        }

        // Create new datagram socket:
        try {
            this.socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
            logger.error("Could not instantiate DatagramSocket. All ATNA UDP logging will fail!", e);
        }
    }

    /**
     * Write the message to the syslog.
     *
     * @param msg The message to write.
     */
    public void write(String msg) {
        if (this.socket != null) {
            byte[] bytes = msg.getBytes();
            // syslog packets must be less than 1024 bytes (Ignore for now).
            int bytesLength = bytes.length;
            DatagramPacket packet = new DatagramPacket(bytes, bytesLength,
                    this.hostAddress, this.port);
            try {
                this.socket.send(packet);
                this.socket.close();
            } catch (IOException e) {
                logger.error("Unable to send UDP message.", e);
            }
        }
    }
}
