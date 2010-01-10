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
package com.vangent.hieos.services.xds.registry.transactions.hl7v2;

import java.io.IOException;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.llp.LowerLayerProtocol;
import ca.uhn.hl7v2.parser.PipeParser;

/**
 *
 * @author Bernie Thuman
 */
public class HL7ServerDaemon {

    private static final String SOCKET_PORT = "socket_port";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws HL7Exception, LLPException, IOException, InterruptedException, Exception {
        if (args.length < 2) {
            HL7ServerDaemon.printUsage();
            System.exit(1);
        }
        if (!args[0].equalsIgnoreCase("-p")) {
            HL7ServerDaemon.printUsage();
            System.exit(1);
        }
        String propertyFilename = args[1];
        HL7ServerProperties props = new HL7ServerProperties(propertyFilename);

        // Create server to process inbound messages.
        LowerLayerProtocol llp = LowerLayerProtocol.makeLLP(); // The transport protocol
        PipeParser parser = new PipeParser(); // The message parser
        String port = props.getProperty(SOCKET_PORT);
        HL7SimpleServer server = new HL7SimpleServer(
                props,
                new Integer(port).intValue(), llp, parser);

        // Set up message handlers:
        Application addHandler = new HL7ADTPatientAddMessageHandler(props);
        server.registerApplication("ADT", "A01", addHandler);  // ADD
        server.registerApplication("ADT", "A04", addHandler);  // ADD
        server.registerApplication("ADT", "A05", addHandler);  // ADD
        /* DO NOT SUPPORT UPDATE
        server.registerApplication("ADT", "A08", handler);  // UPDATE
         */
        Application mergeHandler = new HL7ADTPatientMergeMessageHandler(props);
        server.registerApplication("ADT", "A40", mergeHandler);  // MERGE

        /*
         * Another option would be to specify a single application to handle all messages, like
         * this:
         *
         * server.registerApplication("*", "*", handler);
         */

        // Start the server listening for messages
        server.start();
        System.out.println("Server started ...");
        //Thread.sleep(50000000);  // FIXME!!!!
        Thread.currentThread().wait();
        System.out.println("finished sleeping ");
        server.stop();
    }

    /**
     *
     */
    private static void printUsage() {
        System.out.println("Usage: HL7ServerDaemon -p <properties_file>");
    }
}
