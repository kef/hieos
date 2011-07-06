/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2011 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.vangent.hieos.services.xds.bridge.activity;

import com.vangent.hieos.hl7v3util.model.builder.BuilderHelper;
import com.vangent.hieos.hl7v3util.model.message.MCCI_IN000002UV01_Message;
import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201301UV02_Message;
import com.vangent.hieos.hl7v3util.model.message
    .PRPA_IN201301UV02_Message_Builder;
import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.services.xds.bridge.client.XDSDocumentRegistryClient;
import com.vangent.hieos.services.xds.bridge.model.ResponseType
    .ResponseTypeStatus;
import com.vangent.hieos.services.xds.bridge.model.SubmitDocumentResponse;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.xml.XPathHelper;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;


/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-22
 * @author         Jim Horner
 */
public class AddPatientIdActivity implements ISubmitDocumentRequestActivity {

    /** Field description */
    private static final Logger logger =
        Logger.getLogger(AddPatientIdActivity.class);

    /** Field description */
    private final XDSDocumentRegistryClient registryClient;

    /**
     * Constructs ...
     *
     *
     *
     * @param client
     */
    public AddPatientIdActivity(XDSDocumentRegistryClient client) {

        super();
        this.registryClient = client;
    }

    /**
     * Method description
     *
     *
     * @param addPidResponse
     * @param context
     *
     * @return
     */
    private boolean checkForSuccess(MCCI_IN000002UV01_Message addPidResponse,
                                    SDRActivityContext context) {

        boolean result = false;

        SubmitDocumentResponse sdrResponse =
            context.getSubmitDocumentResponse();

        try {

            OMElement msgNode = addPidResponse.getMessageNode();

            String ackCode = XPathHelper.stringValueOf(msgNode,
                                 "./ns:acknowledgement/ns:typeCode/@code",
                                 BuilderHelper.HL7V3_NAMESPACE);

            // we always return true
            result = true;

            if ("CE".equals(ackCode)) {

                // there was a problem
                String ackDetail =
                    XPathHelper.stringValueOf(
                        msgNode,
                        "./ns:acknowledgement/ns:acknowledgementDetail/ns:text",
                        BuilderHelper.HL7V3_NAMESPACE);

                String warning =
                    String.format(
                        "Failed to add patient identifier to registry: %s",
                        ackDetail);

                // push warning to logger
                logger.warn(warning);
            }

        } catch (XdsInternalException e) {

            // log it
            logger.error(e, e);

            // capture in response
            StringBuilder sb = new StringBuilder();

            sb.append("Unable to parse registry response, exception follows. ");
            sb.append(e.getMessage());

            sdrResponse.addResponse(ResponseTypeStatus.Failure, sb.toString());
        }

        return result;
    }

    /**
     * Method description
     *
     *
     * @param context
     *
     * @return
     */
    @Override
    public boolean execute(SDRActivityContext context) {

        boolean result = false;

        try {

            SubjectIdentifier pid = context.getPatientId();

            DeviceInfo recDevice =
                this.registryClient.createReceiverDeviceInfo();

            DeviceInfo sndDevice = this.registryClient.createSenderDeviceInfo();

            PRPA_IN201301UV02_Message_Builder builder301 =
                new PRPA_IN201301UV02_Message_Builder(sndDevice, recDevice);

            PRPA_IN201301UV02_Message msg301 =
                builder301.buildPRPA_IN201301UV02_Message(pid);

            MCCI_IN000002UV01_Message registryResponse =
                this.registryClient.addPatientIdentity(msg301);

            result = checkForSuccess(registryResponse, context);

        } catch (AxisFault e) {

            SubmitDocumentResponse sdrResponse =
                context.getSubmitDocumentResponse();

            sdrResponse.addResponse(ResponseTypeStatus.Failure, e.getMessage());
        }

        return result;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public String getName() {

        return ClassUtils.getShortClassName(getClass());
    }
}
