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
import com.vangent.hieos.services.pixpdq.empi.exception.EMPIException;

import com.vangent.hieos.hl7v3util.model.message.MCCI_IN000002UV01_Message_Builder;
import com.vangent.hieos.hl7v3util.model.subject.SubjectBuilder;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.hl7v3util.model.message.HL7V3Message;
import com.vangent.hieos.hl7v3util.model.message.HL7V3MessageBuilderHelper;
import com.vangent.hieos.hl7v3util.model.message.MCCI_IN000002UV01_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201301UV02_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201309UV02_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201310UV02_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201310UV02_Message_Builder;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchCriteriaBuilder;
import com.vangent.hieos.hl7v3util.model.exception.ModelBuilderException;
import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchCriteria;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchResponse;

import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import java.util.List;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class PIXRequestHandler extends PIXPDSRequestHandler {

    private final static Logger logger = Logger.getLogger(PIXRequestHandler.class);

    // Type type of message received.
    public enum MessageType {

        PatientRegistryGetIdentifiersQuery,
        PatientRegistryRecordAdded
    };

    /**
     *
     * @param log_message
     */
    public PIXRequestHandler(XLogMessage log_message) {
        super(log_message);
    }

    /**
     *
     * @param request
     * @param messageType
     * @return
     */
    public OMElement run(OMElement request, MessageType messageType) throws AxisFault {
        HL7V3Message result = null;
        log_message.setPass(true);  // Hope for the best.
        switch (messageType) {
            case PatientRegistryGetIdentifiersQuery:
                result = this.processPatientRegistryGetIdentifiersQuery(new PRPA_IN201309UV02_Message(request));
                break;
            case PatientRegistryRecordAdded:
                result = this.processPatientRegistryRecordAdded(new PRPA_IN201301UV02_Message(request));
                break;
        }
        if (result != null) {
            log_message.addOtherParam("Response", result.getMessageNode());
        }
        return (result != null) ? result.getMessageNode() : null;
    }

    /**
     *
     * @param PRPA_IN201301UV02_Message
     * @return
     */
    private MCCI_IN000002UV01_Message processPatientRegistryRecordAdded(PRPA_IN201301UV02_Message request) throws AxisFault {
        this.validateHL7V3Message(request);
        String errorText = null;
        try {
            SubjectBuilder builder = new SubjectBuilder();
            Subject subject = builder.buildSubject(request);
            EMPIAdapter adapter = EMPIFactory.getInstance();
            Subject subjectAdded = adapter.addSubject(subject);
        } catch (Exception ex) {
            errorText = ex.getMessage();
            //log_message.setPass(false);
            log_message.addErrorParam("EXCEPTION", errorText);
        }
        MCCI_IN000002UV01_Message ackResponse = this.getPatientRegistryRecordAddedResponse(request, errorText);
        this.validateHL7V3Message(ackResponse);
        return ackResponse;
    }

    /**
     *
     * @param PRPA_IN201301UV02_Message
     * @param errorText
     * @return
     */
    private MCCI_IN000002UV01_Message getPatientRegistryRecordAddedResponse(PRPA_IN201301UV02_Message request, String errorText) {
        DeviceInfo senderDeviceInfo = this.getDeviceInfo();
        DeviceInfo receiverDeviceInfo = HL7V3MessageBuilderHelper.getSenderDeviceInfo(request);
        MCCI_IN000002UV01_Message_Builder ackBuilder =
                new MCCI_IN000002UV01_Message_Builder(senderDeviceInfo, receiverDeviceInfo);
        MCCI_IN000002UV01_Message ackResponse = ackBuilder.buildMCCI_IN000002UV01(request, errorText);
        return ackResponse;
    }

    /**
     *
     * @param PRPA_IN201309UV02_Message
     * @return
     */
    private PRPA_IN201310UV02_Message processPatientRegistryGetIdentifiersQuery(PRPA_IN201309UV02_Message request) throws AxisFault {
        this.validateHL7V3Message(request);
        SubjectSearchResponse subjectSearchResponse = null;
        String errorText = null;
        try {
            SubjectSearchCriteria subjectSearchCriteria = this.getSubjectSearchCriteria(request);
            subjectSearchResponse = this.findSubjectByIdentifier(subjectSearchCriteria);
        } catch (Exception ex) {
            errorText = ex.getMessage();
            //log_message.setPass(false);
            log_message.addErrorParam("EXCEPTION", errorText);
        }
        PRPA_IN201310UV02_Message pixResponse =
                this.getPatientRegistryGetIdentifiersQueryResponse(
                request, subjectSearchResponse, errorText);
        this.validateHL7V3Message(pixResponse);
        return pixResponse;
    }

    /**
     *
     * @param subjectSearchCriteria
     * @return
     * @throws EMPIException
     */
    private SubjectSearchResponse findSubjectByIdentifier(SubjectSearchCriteria subjectSearchCriteria) throws EMPIException {
        EMPIAdapter adapter = EMPIFactory.getInstance();
        SubjectSearchResponse subjectSearchResponse = adapter.findSubjectByIdentifier(subjectSearchCriteria);
        return subjectSearchResponse;
    }

    /**
     *
     * @param PRPA_IN201309UV02_Message
     * @param errorText
     * @return PRPA_IN201310UV02_Message
     */
    private PRPA_IN201310UV02_Message getPatientRegistryGetIdentifiersQueryResponse(PRPA_IN201309UV02_Message request, SubjectSearchResponse subjectSearchResponse, String errorText) {
        DeviceInfo senderDeviceInfo = this.getDeviceInfo();
        DeviceInfo receiverDeviceInfo = HL7V3MessageBuilderHelper.getSenderDeviceInfo(request);
        PRPA_IN201310UV02_Message_Builder builder =
                new PRPA_IN201310UV02_Message_Builder(senderDeviceInfo, receiverDeviceInfo);
        return builder.buildPRPA_IN201310UV02_Message(request, subjectSearchResponse, errorText);
    }

    /**
     *
     * @param PRPA_IN201309UV02_Message
     * @return
     * @throws ModelBuilderException
     */
    private SubjectSearchCriteria getSubjectSearchCriteria(PRPA_IN201309UV02_Message request) throws ModelBuilderException {
        SubjectSearchCriteriaBuilder builder = new SubjectSearchCriteriaBuilder();
        SubjectSearchCriteria subjectSearchCriteria = builder.buildSubjectSearchCriteria(request);
        return subjectSearchCriteria;
    }
}
