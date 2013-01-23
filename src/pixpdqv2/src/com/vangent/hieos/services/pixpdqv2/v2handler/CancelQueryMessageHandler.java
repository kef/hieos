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
package com.vangent.hieos.services.pixpdqv2.v2handler;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.datatype.ST;
import ca.uhn.hl7v2.model.v25.segment.QID;
import ca.uhn.hl7v2.util.Terser;
import com.vangent.hieos.empi.adapter.EMPIAdapter;
import com.vangent.hieos.empi.adapter.EMPIAdapterFactory;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.hl7v2util.acceptor.impl.Connection;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class CancelQueryMessageHandler extends HL7V2MessageHandler {

    private final static Logger logger = Logger.getLogger(CancelQueryMessageHandler.class);

    /**
     *
     * @param connection
     * @param inMessage
     * @return
     * @throws ApplicationException
     * @throws HL7Exception
     */
    @Override
    public Message processMessage(Connection connection, Message inMessage) throws ApplicationException, HL7Exception {
        logger.info("Processing CancelQuery message ...");
        Message outMessage;
        try {
            // Get Terser.
            Terser terser = new Terser(inMessage);

            // Not used ...
            //DeviceInfo senderDeviceInfo = getSenderDeviceInfo(terser);
            //DeviceInfo receiverDeviceInfo = getReceiverDeviceInfo(terser);

            // SEGMENT: QID
            QID qid = (QID) terser.getSegment("/QID");

            // Get query id to cancel.
            ST queryTagST = qid.getQueryTag();
            if (queryTagST == null || queryTagST.getValue() == null) {
                throw new HL7Exception("No query tag specified");
            }
            String queryId = queryTagST.getValue();

            // Cancel the query.
            EMPIAdapter adapter = EMPIAdapterFactory.getInstance();
            adapter.cancelQuery(queryId);

            // Build response.
            outMessage = this.buildAck(inMessage, null /* responseText */, /* errorText */ null, /* errorCode */ null);
        } catch (EMPIException ex) {
            outMessage = this.buildAck(inMessage, null /* responseText */, ex.getMessage(), null /* errorCode */);
        } catch (Exception ex) {
            outMessage = this.buildAck(inMessage, null /* responseText */, "Exception: " + ex.getClass().getName() + " - " + ex.getMessage(), null /* errorCode */);
        }
        return outMessage;
    }

    /**
     *
     * @param connection
     * @param in
     * @return
     */
    @Override
    public boolean canProcess(Connection connection, Message in) {
        return true;
    }
}
