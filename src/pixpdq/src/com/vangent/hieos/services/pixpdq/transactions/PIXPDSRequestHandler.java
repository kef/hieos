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

import com.vangent.hieos.services.pixpdq.empi.api.EMPIAdapter;
import com.vangent.hieos.services.pixpdq.empi.factory.EMPIFactory;
import com.vangent.hieos.empi.exception.EMPIException;

import com.vangent.hieos.hl7v3util.model.message.HL7V3Message;
import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchCriteria;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchResponse;
import com.vangent.hieos.hl7v3util.xml.HL7V3SchemaValidator;
import com.vangent.hieos.xutil.exception.SOAPFaultException;

import com.vangent.hieos.xutil.services.framework.XBaseTransaction;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import com.vangent.hieos.xutil.exception.XMLSchemaValidatorException;
import com.vangent.hieos.xutil.xconfig.XConfigObject;

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
    @Override
    public boolean getStatus()
    {
        return log_message.isPass();
    }

    /**
     * 
     * @param message
     * @throws SOAPFaultException
     */
    protected void validateHL7V3Message(HL7V3Message message) throws SOAPFaultException {
        try {
            HL7V3SchemaValidator.validate(message.getMessageNode(), message.getType());
        } catch (XMLSchemaValidatorException ex) {
            //log_message.setPass(false);
            log_message.addErrorParam("EXCEPTION", ex.getMessage());
            throw new SOAPFaultException(ex.getMessage());
        }
    }

    /**
     *
     * @return
     */
    protected DeviceInfo getDeviceInfo() {
        DeviceInfo deviceInfo = new DeviceInfo();
        XConfigObject config = this.getConfigActor();
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
