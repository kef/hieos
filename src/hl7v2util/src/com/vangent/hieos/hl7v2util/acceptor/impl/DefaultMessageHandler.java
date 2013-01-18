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

package com.vangent.hieos.hl7v2util.acceptor.impl;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.app.DefaultApplication;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.util.Terser;
import java.io.IOException;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class DefaultMessageHandler implements MessageHandler {
     private static final Logger logger = Logger.getLogger(DefaultMessageHandler.class);

    /**
     *
     * @param inMessage
     * @return
     * @throws HL7Exception
     * @throws IOException
     */
    private Message generateACK(Message inMessage) throws HL7Exception {
        Segment inHeader = (Segment) inMessage.get("MSH");
        Message retVal;
        try {
            retVal = DefaultApplication.makeACK(inHeader);
            Terser terser = new Terser(retVal);
            terser.set("/.MSA-3-1", "Success!");
        } catch (IOException e) {
            throw new HL7Exception(e);
        }
        return retVal;
    }

    /**
     *
     * @param connection
     * @param in
     * @return
     * @throws ApplicationException
     * @throws HL7Exception
     */
    public Message processMessage(Connection connection, Message in) throws ApplicationException, HL7Exception {
        //throw new UnsupportedOperationException("Not supported yet.");
        logger.info("Processing message ...");
        try {
            Terser terser = new Terser(in);
            String messageType = terser.get("/.MSH-9-2");
            //String patientId = terser.get("/.PID-3-1");
            //String assigningAuthorityUniversalId = terser.get("/PID-3-4-2");
            String messageControlId = terser.get("/.MSH-10");
            String sendingApplication = terser.get("/.MSH-3");
            String sendingFacility = terser.get("/.MSH-4");
            logger.info("Message Type = " + messageType);
            logger.info("Message Control Id = " + messageControlId);
            logger.info("Sending Application = " + sendingApplication);
            logger.info("Sending Facility = " + sendingFacility);
            //logger.info("Patient Id = " + patientId);
            //logger.info("Assigning Authority = " + assigningAuthorityUniversalId);
            return this.generateACK(in);
        } catch (Exception e) {
            logger.error("Exception: " + e.getMessage());
            throw new HL7Exception(e);
        }
    }

    /**
     *
     * @param connection
     * @param in
     * @return
     */
    public boolean canProcess(Connection connection, Message in) {
        return true;
    }

}
