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

package com.vangent.hieos.services.xds.bridge.transactions;

import java.util.ArrayList;
import java.util.List;
import com.vangent.hieos.hl7v3util.model.exception.ModelBuilderException;
import com.vangent.hieos.services.xds.bridge.message
    .SubmitDocumentRequestBuilder;
import com.vangent.hieos.services.xds.bridge.message
    .SubmitDocumentResponseBuilder;
import com.vangent.hieos.services.xds.bridge.message
    .SubmitDocumentResponseMessage;
import com.vangent.hieos.services.xds.bridge.model.Document;
import com.vangent.hieos.services.xds.bridge.model.ResponseType
    .ResponseTypeStatus;
import com.vangent.hieos.services.xds.bridge.model.SubmitDocumentRequest;
import com.vangent.hieos.services.xds.bridge.model.SubmitDocumentResponse;
import com.vangent.hieos.services.xds.bridge.model.SubmitDocumentResponse
    .Status;
import com.vangent.hieos.services.xds.bridge.support
    .XDSBridgeServiceContext;
import com.vangent.hieos.services.xds.bridge.support.IMessageHandler;
import com.vangent.hieos.services.xds.bridge.activity
    .AddPatientIdActivity;
import com.vangent.hieos.services.xds.bridge.activity
    .CDAToXDSMapperActivity;
import com.vangent.hieos.services.xds.bridge.activity
    .ISubmitDocumentRequestActivity;
import com.vangent.hieos.services.xds.bridge.activity
    .SDRActivityContext;
import com.vangent.hieos.services.xds.bridge.activity
    .SubmitPnRActivity;
import com.vangent.hieos.xutil.services.framework.XBaseTransaction;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;
import org.apache.log4j.Logger;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-09
 * @author         Jim Horner
 */
public class SubmitDocumentRequestHandler extends XBaseTransaction
        implements IMessageHandler {

    /** Field description */
    private static final Logger logger =
        Logger.getLogger(SubmitDocumentRequestHandler.class);

    /** Field description */
    private final AddPatientIdActivity addPatientIdActivity;

    /** Field description */
    private final List<ISubmitDocumentRequestActivity> processActivities;

    /** Field description */
    private final SubmitDocumentRequestBuilder requestBuilder;

    /** Field description */
    private final SubmitDocumentResponseBuilder responseBuilder;

    /**
     * Constructs ...
     *
     *
     * @param logMessage
     * @param context
     */
    public SubmitDocumentRequestHandler(XLogMessage logMessage,
            XDSBridgeServiceContext context) {

        super();

        // super(logMessage); ??
        this.log_message = logMessage;

        this.requestBuilder = context.getSubmitDocumentRequestBuilder();
        this.responseBuilder = context.getSubmitDocumentResponseBuilder();

        this.addPatientIdActivity =
            new AddPatientIdActivity(context.getRegistryClient());

        this.processActivities =
            new ArrayList<ISubmitDocumentRequestActivity>();
        this.processActivities.add(
            new CDAToXDSMapperActivity(context.getMapperFactory()));
        this.processActivities.add(
            new SubmitPnRActivity(context.getRepositoryClient()));
    }

    /**
     * Method description
     *
     *
     * @param sdrRequest
     * @param sdrResponse
     *
     * @return
     */
    private boolean addPatientIdToRegistry(SubmitDocumentRequest sdrRequest,
            SubmitDocumentResponse sdrResponse) {

        SDRActivityContext context = new SDRActivityContext(sdrRequest, null,
                                         sdrResponse);

        return this.addPatientIdActivity.execute(context);
    }

    /**
     * Method description
     *
     *
     * @param sdrResponse
     *
     * @return
     */
    private OMElement marshalResponse(SubmitDocumentResponse sdrResponse) {

        // marshal response
        SubmitDocumentResponseMessage result =
            this.responseBuilder.buildMessage(sdrResponse);

        return result.getMessageNode();
    }

    /**
     * Method description
     *
     *
     * @param messageContext
     * @param request
     *
     * @return
     *
     * @throws Exception
     */
    @Override
    public OMElement run(MessageContext messageContext, OMElement request)
            throws Exception {

        SubmitDocumentResponse sdrResponse =
            new SubmitDocumentResponse(Status.Failure);

        SubmitDocumentRequest sdrRequest = unmarshalRequest(request,
                                               sdrResponse);

        if (sdrRequest != null) {

            boolean pidAdded = addPatientIdToRegistry(sdrRequest, sdrResponse);

            if (pidAdded) {

                runActivities(sdrRequest, sdrResponse);
            }
        }

        return marshalResponse(sdrResponse);
    }

    /**
     * Method description
     *
     *
     * @param sdrRequest
     * @param sdrResponse
     */
    private void runActivities(SubmitDocumentRequest sdrRequest,
                               SubmitDocumentResponse sdrResponse) {

        // from here we need to start tracking exceptions per document
        // to send back a proper response of success, partial, failure

        int failureCount = 0;
        int documentCount = 0;

        for (Document document : sdrRequest.getDocuments()) {

            // each activity will return success/failure
            // each activity will update the response w/ error

            SDRActivityContext context = new SDRActivityContext(sdrRequest,
                                             document, sdrResponse);

            boolean success = true;

            for (ISubmitDocumentRequestActivity activity :
                    this.processActivities) {

                logger.debug(String.format("Executing %s", activity.getName()));

                success = activity.execute(context);

                if (success == false) {

                    logger.info(String.format("Activity %s failed.",
                                              activity.getName()));

                    ++failureCount;

                    break;
                }
            }

            if (success) {

                sdrResponse.addSuccess(document);
            }

            ++documentCount;
        }

        // set the final status
        if (failureCount == 0) {

            sdrResponse.setStatus(Status.Success);

        } else if (failureCount == documentCount) {

            sdrResponse.setStatus(Status.Failure);
        } else {

            sdrResponse.setStatus(Status.PartialSuccess);
        }
    }

    /**
     * Method description
     *
     *
     * @param request
     * @param sdrResponse
     *
     * @return
     */
    private SubmitDocumentRequest unmarshalRequest(OMElement request,
            SubmitDocumentResponse sdrResponse) {

        SubmitDocumentRequest result = null;

        try {

            // unmarshal request
            result = this.requestBuilder.buildSubmitDocumentRequest(request);

        } catch (ModelBuilderException e) {

            // this request failed validation (most likely)
            String errmsg =
                String.format(
                    "Request could not be parsed. Failure(s) to follow. %s",
                    e.getMessage());

            sdrResponse.setStatus(Status.Failure);
            sdrResponse.addResponse(ResponseTypeStatus.Failure, errmsg);
        }

        return result;
    }
}
