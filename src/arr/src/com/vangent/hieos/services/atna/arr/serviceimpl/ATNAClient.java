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
package com.vangent.hieos.services.atna.arr.serviceimpl;

import java.io.*;
import java.net.*;

import com.vangent.hieos.xutil.atna.SysLogAdapter;
import com.vangent.hieos.xutil.socket.TLSSocketSupport;

/**
 *
 * @author Adeola Odunlami
 */
public class ATNAClient {

    public static void main(String[] args) throws IOException {

        try {
            System.out.println("ATNA CLIENT");
            if (args.length != 8) {
                ATNAClient.printUsage();
                System.exit(1);
            }
            if (!args[0].equalsIgnoreCase("-host") ||
                    !args[2].equalsIgnoreCase("-port") ||
                    !args[4].equalsIgnoreCase("-protocol") ||
                    !args[6].equalsIgnoreCase("-secure")) {
                ATNAClient.printUsage();
                System.exit(1);
            }

            String hostname = args[1];
            int port = Integer.parseInt(args[3]);
            String protocol = args[5];
            System.out.println("ATNA CLIENT, host: " + hostname + "; port: " + port + "; protocol: " + protocol);

            String secure = args[7];

            if ("UDP".equalsIgnoreCase(protocol)) {
                sendUDPMessage(hostname, port, protocol);
            } else {
                // TCP protocol, check whether secure or non-secure
                if (secure.equalsIgnoreCase("true")) {
                    sendTLSMessage(hostname, port, "TCP");
                } else {
                    sendNonTLSMessage(hostname, port, "TCP");
                }
            }

            System.out.println("ATNA CLIENT - Done ");
        } catch (Exception ex) {
            System.err.println("Error: " + ex);
            System.exit(1);
        }

    }

    /**
     *
     */
    private static void sendUDPMessage(String hostname, int port, String protocol) throws IOException {
        try {
            for (int i = 1; i < 5; i++) {
                System.out.println("UDP CLIENT Establishing Connection");
                SysLogAdapter syslog = new SysLogAdapter(hostname, port, protocol);
                String message = "|" + i + "|TEST MESSAGE FROM UDP CLIENT|" + i + "|";
                syslog.write(message);
                System.out.println("UDP Message: " + message);
                System.out.println("UDP Message Sent to Server");
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    /**
     *
     */
    private static void sendNonTLSMessage(String hostname, int port, String protocol) throws IOException {
        System.out.println("Non TLS CLIENT Establishing Connection");
        Socket clientSocket = new Socket(hostname, port);
        PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true);
        String message = "TEST MESSAGE FROM Non TLS CLIENT ";
        pw.print(message);
        pw.close();
        System.out.println("Non TLS Message Sent to Server");
    }

    /**
     *
     */
    private static void sendTLSMessage(String hostname, int port, String protocol)
            throws IOException {
        try {
            SysLogAdapter syslog = new SysLogAdapter(hostname, port, protocol);
            for (int i = 1; i < 5; i++) {
                String message = "|" + i + "|TEST MESSAGE FROM TLS CLIENT|" + i + "|";
                syslog.write(message);
                
                /*System.out.println("TLS CLIENT Establishing Connection");
                TLSSocketSupport tss = new TLSSocketSupport();
                Socket clientSocket = tss.getSecureClientSocket(hostname, port);
                PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true);
                pw.print(message);
                pw.close(); */
                
                System.out.println("TLS Message Sent to Server");
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    /**
     *
     */
    private static void printUsage() {
        System.out.println("Usage: ATNAClient -host <host> -port <port> -protocol <protocol> -secure <true|false>");
    }
}
