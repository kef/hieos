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
package com.vangent.hieos.services.xds.registry.transactions.hl7v2;

import java.io.IOException;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.app.DefaultApplication;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;
import java.net.Socket;
import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class HL7ADTPatientMergeMessageHandler extends HL7ADTPatientIdentityFeedMessageHandler {

    private static final Logger log = Logger.getLogger(HL7ADTPatientMergeMessageHandler.class);

    /**
     * 
     * @param props
     */
    public HL7ADTPatientMergeMessageHandler(HL7ServerProperties props) {
        super(props);
    }

    /**
     * Process the inbound HL7v2 message and return an ACK.
     *
     * @param inMessage The inbound HL7v2 message.
     * @param socket The socket that accepted the inbound connection.
     * @return The outbound message (ACK).
     * @throws ApplicationException
     * @throws HL7Exception
     */
    public Message handleMessage(Message inMessage, Socket socket) throws ApplicationException, HL7Exception {
        log.info("++++ PATIENT MERGE ++++\n");
        try {
            Terser terser = new Terser(inMessage);
            String messageType = terser.get("/.MSH-9-2");
            String patientId = terser.get("/.PID-3-1");
            String assigningAuthorityUniversalId = terser.get("/.PID-3-4-2");
            String mergePatientId = terser.get("/.MRG-1-1");
            String mergeAssigningAuthorityUniversalId = terser.get("/.MRG-1-4-2");
            log.info("Message Type = " + messageType);
            log.info("Patient Id = " + patientId);
            log.info("Assigning Authority = " + assigningAuthorityUniversalId);
            log.info("Patient Id (merge) = " + mergePatientId);
            log.info("Assigning Authority (merge) = " + mergeAssigningAuthorityUniversalId);
            OMElement registryRequest =
                    this.createRegistryMergeRequest(
                    this.getRemoteIPAddress(socket),
                    inMessage,
                    this.formatPatientId(patientId, assigningAuthorityUniversalId),
                    this.formatPatientId(mergePatientId, mergeAssigningAuthorityUniversalId));
            log.info("Registry Request:\n");
            log.info(registryRequest.toString());
            this.sendRequestToRegistry(registryRequest);
        } catch (Exception e) {
            log.error("Exception: " + e.getMessage());
            throw new HL7Exception(e);
        }

        // Generate the ACK.
        Segment inHeader = (Segment) inMessage.get("MSH");
        Message retVal;
        try {
            retVal = DefaultApplication.makeACK(inHeader);
        } catch (IOException e) {
            throw new HL7Exception(e);
        }
        return retVal;
    }
}
