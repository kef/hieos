/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2012 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.pixpdqv2.test.sender;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.ConnectionHub;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.llp.MinLowerLayerProtocol;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.GenericParser;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
import java.io.IOException;

/**
 *
 * @author Bernie Thuman
 */
public class TestSender {

    /**
     *
     * @param args
     */
    public static void main(String[] args) throws HL7Exception, LLPException, IOException {
        // Create a message to send
        String msg = "MSH|^~\\&|HIS|RIH|EKG|EKG|199904140038||ADT^A01|12345|P|2.2\r"
                + "PID|0001|00009874|00001122|A00977|SMITH^JOHN^M|MOM|19581119|F|NOTREAL^LINDA^M|C|564 SPRING ST^^NEEDHAM^MA^02494^US|0002|(818)565-1551|(425)828-3344|E|S|C|0000444444|252-00-4414||||SA|||SA||||NONE|V1|0001|I|D.ER^50A^M110^01|ER|P00055|11B^M011^02|070615^BATMAN^GEORGE^L|555888^NOTREAL^BOB^K^DR^MD|777889^NOTREAL^SAM^T^DR^MD^PHD|ER|D.WT^1A^M010^01|||ER|AMB|02|070615^NOTREAL^BILL^L|ER|000001916994|D||||||||||||||||GDD|WA|NORM|02|O|02|E.IN^02D^M090^01|E.IN^01D^M080^01|199904072124|199904101200|199904101200||||5555112333|||666097^NOTREAL^MANNY^P\r"
                + "NK1|0222555|NOTREAL^JAMES^R|FA|STREET^OTHER STREET^CITY^ST^55566|(222)111-3333|(888)999-0000|||||||ORGANIZATION\r"
                + "PV1|0001|I|D.ER^1F^M950^01|ER|P000998|11B^M011^02|070615^BATMAN^GEORGE^L|555888^OKNEL^BOB^K^DR^MD|777889^NOTREAL^SAM^T^DR^MD^PHD|ER|D.WT^1A^M010^01|||ER|AMB|02|070615^VOICE^BILL^L|ER|000001916994|D||||||||||||||||GDD|WA|NORM|02|O|02|E.IN^02D^M090^01|E.IN^01D^M080^01|199904072124|199904101200|||||5555112333|||666097^DNOTREAL^MANNY^P\r"
                + "PV2|||0112^TESTING|55555^PATIENT IS NORMAL|NONE|||19990225|19990226|1|1|TESTING|555888^NOTREAL^BOB^K^DR^MD||||||||||PROD^003^099|02|ER||NONE|19990225|19990223|19990316|NONE\r"
                + "AL1||SEV|001^POLLEN\r"
                + "GT1||0222PL|NOTREAL^BOB^B||STREET^OTHER STREET^CITY^ST^77787|(444)999-3333|(222)777-5555||||MO|111-33-5555||||NOTREAL GILL N|STREET^OTHER STREET^CITY^ST^99999|(111)222-3333\r"
                + "IN1||022254P|4558PD|BLUE CROSS|STREET^OTHER STREET^CITY^ST^00990||(333)333-6666||221K|LENIX|||19980515|19990515|||PATIENT01 TEST D||||||||||||||||||02LL|022LP554";

        String pixQuery = "MSH|^~\\&|NIST_SENDER^^|NIST^^|NIST_RECEIVER^^|NIST^^|20101101160655||QBP^Q23^QBP_Q21|NIST-101101160655565|P|2.5\r"
                + "QPD|IHE PIX Query|QRY124518648946312|14583058^^^NIST2010&2.16.840.1.113883.3.72.5.9.1&ISO|^^^&2.16.840.1.113883.3.72.5.9.1&ISO~^^^&2.16.840.1.113883.3.72.5.9.2&ISO\r"
                + "RCP|I\r";
        Parser p = new GenericParser();
        Message pixQueryMessage = p.parse(pixQuery);

        // The connection hub connects to listening servers
        ConnectionHub connectionHub = ConnectionHub.getInstance();

        // A connection object represents a socket attached to an HL7 server
        Connection connection = connectionHub.attach("localhost", 5051, new PipeParser(), MinLowerLayerProtocol.class, true /* tls */);

        // The initiator is used to transmit unsolicited messages
        Initiator initiator = connection.getInitiator();
        Message response = initiator.sendAndReceive(pixQueryMessage);

        PipeParser parser = new PipeParser(); // The message parser
        String responseString = parser.encode(response);
        System.out.println("[response] RAW HL7v2 Message:\n" + responseString);

        Parser xmlParser = new DefaultXMLParser();
        String xmlEncodedMessage = xmlParser.encode(response);
        System.out.println("[response] XML Encoded Message:\n" + xmlEncodedMessage);

        /*
         * MSH|^~\&|||||20070218200627.515-0500||ACK|54|P|2.2 MSA|AA|12345
         */

        /*
         * Close the connection and server. Note that this method may close
         * the connection. If you are designing a system which will continuously
         * send out messages, you may want to consider keeping a copy of the
         * Connection object between messages. That way, the same connection
         * will be reused.
         *
         * See
         * http://hl7api.sourceforge.net/xref/ca/uhn/hl7v2/examples/SendLotsOfMessages.html
         * for an example of this.
         */
        connectionHub.discard(connection);
        ConnectionHub.shutdown();
    }
}
