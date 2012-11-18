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

import com.vangent.hieos.empi.adapter.EMPIAdapter;
import com.vangent.hieos.empi.adapter.EMPIAdapterFactory;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.hl7v3util.atna.ATNAAuditEventHelper;
import com.vangent.hieos.hl7v3util.model.message.HL7V3Message;
import com.vangent.hieos.hl7v3util.model.message.HL7V3MessageBuilderHelper;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201305UV02_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201306UV02_Message;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchCriteriaBuilder;
import com.vangent.hieos.hl7v3util.model.exception.ModelBuilderException;
import com.vangent.hieos.hl7v3util.model.message.HL7V3ErrorDetail;
import com.vangent.hieos.subjectmodel.SubjectSearchCriteria;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201306UV02_Message_Builder;
import com.vangent.hieos.subjectmodel.DeviceInfo;
import com.vangent.hieos.subjectmodel.SubjectSearchResponse;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent;
import com.vangent.hieos.xutil.atna.ATNAAuditEventQuery;
import com.vangent.hieos.xutil.atna.XATNALogger;
import com.vangent.hieos.xutil.exception.SOAPFaultException;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class PDSRequestHandler extends RequestHandler {

    private final static Logger logger = Logger.getLogger(PDSRequestHandler.class);

    // Type type of message received.
    /**
     *
     */
    public enum MessageType {

        /**
         *
         */
        PatientRegistryFindCandidatesQuery
    };

    /**
     *
     * @param log_message
     */
    public PDSRequestHandler(XLogMessage log_message) {
        super(log_message);
    }

    /**
     *
     * @param request
     * @param messageType
     * @return
     * @throws SOAPFaultException
     */
    public OMElement run(OMElement request, MessageType messageType) throws SOAPFaultException {
        HL7V3Message result = null;
        log_message.setPass(true);  // Hope for the best.
        switch (messageType) {
            case PatientRegistryFindCandidatesQuery:
                result = this.processPatientRegistryFindCandidatesQuery(
                        new PRPA_IN201305UV02_Message(request));
                break;
        }
        if (result != null) {
            log_message.addOtherParam("Response", result.getMessageNode());
        }
        return (result != null) ? result.getMessageNode() : null;
    }

    /**
     *
     * @param PRPA_IN201305UV02_Message
     * @return
     * @throws SOAPFaultException
     */
    private PRPA_IN201306UV02_Message processPatientRegistryFindCandidatesQuery(PRPA_IN201305UV02_Message request) throws SOAPFaultException {
        this.validateHL7V3Message(request);
        SubjectSearchResponse subjectSearchResponse = null;
        HL7V3ErrorDetail errorDetail = null;
        try {
            // FIXME: Validate minimum fields required!!!!
            SubjectSearchCriteria subjectSearchCriteria = this.getSubjectSearchCriteria(request);
            subjectSearchResponse = this.findSubjects(subjectSearchCriteria);
            this.performAuditPDQQueryProvider(request, subjectSearchResponse);
        } catch (EMPIException ex) {
            errorDetail = new HL7V3ErrorDetail(ex.getMessage(), ex.getCode());
        } catch (Exception ex) {
            // Other exceptions.
            errorDetail = new HL7V3ErrorDetail(ex.getMessage());
        }
        if (log_message.isLogEnabled() && errorDetail != null) {
            log_message.addErrorParam("EXCEPTION", errorDetail.getText());
        }
        PRPA_IN201306UV02_Message queryResponse = this.getPatientRegistryFindCandidatesQueryResponse(request, subjectSearchResponse, errorDetail);
        this.validateHL7V3Message(queryResponse);
        return queryResponse;
    }

    /**
     *
     * @param subjectSearchCriteria
     * @return
     * @throws EMPIException
     */
    private SubjectSearchResponse findSubjects(SubjectSearchCriteria subjectSearchCriteria) throws EMPIException {
        EMPIAdapter adapter = EMPIAdapterFactory.getInstance(this.getConfigActor());
        SubjectSearchResponse subjectSearchResponse = adapter.findSubjects(subjectSearchCriteria);
        return subjectSearchResponse;
    }

    /**
     *
     * @param PRPA_IN201305UV02_Message
     * @return
     * @throws ModelBuilderException
     */
    private SubjectSearchCriteria getSubjectSearchCriteria(PRPA_IN201305UV02_Message message) throws ModelBuilderException {
        SubjectSearchCriteriaBuilder builder = new SubjectSearchCriteriaBuilder();
        SubjectSearchCriteria subjectSearchCriteria = builder.buildSubjectSearchCriteria(message);
        return subjectSearchCriteria;
    }

    /**
     * 
     * @param request
     * @param subjectSearchResponse
     * @param errorDetail
     * @return
     */
    private PRPA_IN201306UV02_Message getPatientRegistryFindCandidatesQueryResponse(PRPA_IN201305UV02_Message request,
            SubjectSearchResponse subjectSearchResponse, HL7V3ErrorDetail errorDetail) {
        DeviceInfo senderDeviceInfo = this.getDeviceInfo();
        DeviceInfo receiverDeviceInfo = HL7V3MessageBuilderHelper.getSenderDeviceInfo(request);
        PRPA_IN201306UV02_Message_Builder builder =
                new PRPA_IN201306UV02_Message_Builder(senderDeviceInfo, receiverDeviceInfo);
        return builder.buildPRPA_IN201306UV02_Message(request, subjectSearchResponse, errorDetail);
    }

    /**
     * 
     * @param request
     * @param subjectSearchResponse
     */
    private void performAuditPDQQueryProvider(
            PRPA_IN201305UV02_Message request,
            SubjectSearchResponse subjectSearchResponse) {
        try {
            XATNALogger xATNALogger = new XATNALogger();
            if (xATNALogger.isPerformAudit()) {
                //String homeCommunityId = this.getGatewayConfig().getUniqueId();
                String homeCommunityId = null;  // FIXME: Should we set this?
                ATNAAuditEventQuery auditEvent = ATNAAuditEventHelper.getATNAAuditEventPDQQueryProvider(
                        ATNAAuditEvent.ActorType.PATIENT_DEMOGRAPHICS_SUPPLIER, request, subjectSearchResponse, homeCommunityId);
                xATNALogger.audit(auditEvent);
            }
        } catch (Exception ex) {
            logger.error("PatientDemographicsSupplier EXCEPTION: Could not perform ATNA audit", ex);
        }
    }
}
