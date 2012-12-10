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
import com.vangent.hieos.hl7v2util.acceptor.Connection;
import com.vangent.hieos.hl7v2util.acceptor.MessageHandler;
import com.vangent.hieos.subjectmodel.DeviceInfo;

/**
 *
 * @author Bernie
 */
public class HL7V2MessageHandler implements MessageHandler {

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
        return getDeviceInfo(facility, application);
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
        System.out.println("Sending Application = " + application);
        System.out.println("Sending Facility = " + facility);
        return getDeviceInfo(facility, application);
    }

    /**
     *
     * @param facility
     * @param application
     * @return
     */
    private static DeviceInfo getDeviceInfo(String facility, String application) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setId(facility);
        deviceInfo.setName(application);
        return deviceInfo;
    }

}
