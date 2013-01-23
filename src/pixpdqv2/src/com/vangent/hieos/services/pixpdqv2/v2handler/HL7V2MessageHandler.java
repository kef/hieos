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
import ca.uhn.hl7v2.util.Terser;
import com.vangent.hieos.empi.config.EMPIConfig;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.hl7v2util.acceptor.impl.Connection;
import com.vangent.hieos.hl7v2util.acceptor.impl.MessageHandler;
import com.vangent.hieos.hl7v2util.model.builder.BuilderConfig;
import com.vangent.hieos.hl7v2util.model.message.AckMessageBuilder;
import com.vangent.hieos.subjectmodel.DeviceInfo;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class HL7V2MessageHandler implements MessageHandler {

    private final static Logger logger = Logger.getLogger(HL7V2MessageHandler.class);
    private BuilderConfig builderConfig;

    /**
     * 
     * @return
     */
    public BuilderConfig getBuilderConfig() {
        return builderConfig;
    }

    /**
     *
     * @param builderConfig
     * @param inMessage
     * @param responseText
     * @param errorText
     * @param errorCode
     * @return
     * @throws HL7Exception
     */
    public Message buildAck(Message inMessage, String responseText, String errorText, String errorCode) throws HL7Exception {
        AckMessageBuilder ackMessageBuilder = new AckMessageBuilder(this.getBuilderConfig(), inMessage);
        return ackMessageBuilder.buildAck(responseText, errorText, errorCode);
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     *
     * @param connection
     * @param in
     * @return
     */
    public boolean canProcess(Connection connection, Message in) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     *
     * @param terser
     * @return
     * @throws HL7Exception
     */
    public static DeviceInfo getSenderDeviceInfo(Terser terser) throws HL7Exception {
        String application = terser.get("/.MSH-3");
        String facility = terser.get("/.MSH-4");
        System.out.println("Sending Application = " + application);
        System.out.println("Sending Facility = " + facility);
        return getDeviceInfo(application, facility);
    }

    /**
     *
     * @param terser
     * @return
     * @throws HL7Exception
     */
    public static DeviceInfo getReceiverDeviceInfo(Terser terser) throws HL7Exception {
        String application = terser.get("/.MSH-5");
        String facility = terser.get("/.MSH-6");
        System.out.println("Receiver Application = " + application);
        System.out.println("Receiver Facility = " + facility);
        return getDeviceInfo(application, facility);
    }

    /**
     * 
     * @param application
     * @param facility
     * @return
     */
    private static DeviceInfo getDeviceInfo(String application, String facility) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setId(application);
        deviceInfo.setName(facility);
        return deviceInfo;
    }

    /**
     * 
     * @throws HL7Exception
     */
    public void init() throws HL7Exception {
        builderConfig = new BuilderConfig();
        try {
            // Convert EMPI configuration items to Builder config items for HL7v2 processing.
            EMPIConfig empiConfig = EMPIConfig.getInstance();
            builderConfig.setDefaultAccountNumberUniversalId(empiConfig.getAccountNumberTreatmentConfig().getDefaultUniversalId());
        } catch (EMPIException ex) {
            logger.error("Unable to get EMPI Configuration", ex);
            throw new HL7Exception("Unable to get EMPI Configuration", ex);
        }
    }
}
