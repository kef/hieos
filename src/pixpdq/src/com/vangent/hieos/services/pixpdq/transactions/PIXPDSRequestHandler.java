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
package com.vangent.hieos.services.pixpdq.transactions;

import com.vangent.hieos.services.pixpdq.adapter.mpi.EMPIAdapter;
import com.vangent.hieos.services.pixpdq.adapter.mpi.EMPIFactory;
import com.vangent.hieos.services.pixpdq.exception.EMPIException;
import com.vangent.hieos.hl7v3util.model.message.HL7V3Message;
import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchCriteria;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchResponse;
import com.vangent.hieos.hl7v3util.xml.HL7V3SchemaValidator;
import com.vangent.hieos.xutil.exception.XConfigException;
import com.vangent.hieos.xutil.services.framework.XBaseTransaction;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import com.vangent.hieos.xutil.exception.XMLSchemaValidatorException;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;
import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public abstract class PIXPDSRequestHandler extends XBaseTransaction {

    private final static Logger logger = Logger.getLogger(PIXPDSRequestHandler.class);


    /**
     *
     */
    protected PIXPDSRequestHandler() {
        // Do nothing.
    }

    /**
     *
     * @param log_message
     */
    public PIXPDSRequestHandler(XLogMessage log_message) {
        this.log_message = log_message;
    }

    /**
     * 
     * @return
     */
    protected abstract XConfigActor getConfig();

    /**
     * 
     * @param message
     * @throws AxisFault
     */
    protected void validateHL7V3Message(HL7V3Message message) throws AxisFault {
        try {
            HL7V3SchemaValidator.validate(message.getMessageNode(), message.getType());
        } catch (XMLSchemaValidatorException ex) {
            log_message.setPass(false);
            log_message.addErrorParam("EXCEPTION", ex.getMessage());
            throw new AxisFault(ex.getMessage());
        }
    }

    /**
     *
     * @return
     */
    protected DeviceInfo getDeviceInfo() {
        DeviceInfo deviceInfo = new DeviceInfo();
        XConfigObject config = this.getConfig();
        String deviceId = config.getProperty("DeviceId");
        String deviceName = config.getProperty("DeviceName");
        //String homeCommunityId = config.getUniqueId();
        deviceInfo.setId(deviceId);
        deviceInfo.setName(deviceName);
        //deviceInfo.setHomeCommunityId(homeCommunityId);
        return deviceInfo;
    }

    /**
     *
     * @param name
     * @param type
     * @return
     */
    protected XConfigActor getConfig(String name, String type) {
        XConfigActor configActor = null;
        try {
            XConfig xconf = XConfig.getInstance();
            XConfigObject homeCommunityConfig = xconf.getHomeCommunityConfig();
            configActor = (XConfigActor) homeCommunityConfig.getXConfigObjectWithName(name, type);
        } catch (XConfigException ex) {
            // TBD: Do something.
        }
        return configActor;
    }

    /**
     *
     * @param subjectSearchCriteria
     * @return
     * @throws EMPIException
     */
    protected SubjectSearchResponse findSubjects(SubjectSearchCriteria subjectSearchCriteria) throws EMPIException {
        EMPIAdapter adapter = EMPIFactory.getInstance();
        SubjectSearchResponse subjectSearchResponse = adapter.findSubjects(subjectSearchCriteria);
        return subjectSearchResponse;
    }
}
