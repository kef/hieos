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

import com.vangent.hieos.hl7v3util.model.exception.ModelBuilderException;
import com.vangent.hieos.services.xds.bridge.client.XDSDocumentRegistryClient;
import com.vangent.hieos.services.xds.bridge.client.XDSDocumentRepositoryClient;
import com.vangent.hieos.services.xds.bridge.mapper.MapperFactory;
import com.vangent.hieos.services.xds.bridge.model.Document;
import com.vangent.hieos.services.xds.bridge.model.ResponseType;
import com.vangent.hieos.services.xds.bridge.model.ResponseType
    .ResponseTypeStatus;
import com.vangent.hieos.services.xds.bridge.model.SubmitDocumentRequest;
import com.vangent.hieos.services.xds.bridge.model.SubmitDocumentRequestBuilder;
import com.vangent.hieos.services.xds.bridge.model.SubmitDocumentResponse;
import com.vangent.hieos.services.xds.bridge.model.SubmitDocumentResponse
    .Status;
import com.vangent.hieos.services.xds.bridge.support.IMessageHandler;
import com.vangent.hieos.services.xds.bridge.transactions.activity
    .CDAToXDSMapperActivity;
import com.vangent.hieos.services.xds.bridge.transactions.activity
    .ISubmitDocumentRequestActivity;
import com.vangent.hieos.services.xds.bridge.transactions.activity
    .SDRActivityContext;
import com.vangent.hieos.services.xds.bridge.transactions.activity
    .SubmitPatientIdActivity;
import com.vangent.hieos.services.xds.bridge.transactions.activity
    .SubmitPnRActivity;
import com.vangent.hieos.services.xds.bridge.utils.StringUtils;
import com.vangent.hieos.xutil.services.framework.XBaseTransaction;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.context.MessageContext;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

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
    private final static Logger logger =
        Logger.getLogger(SubmitDocumentRequestHandler.class);

    /** Field description */
    private final List<ISubmitDocumentRequestActivity> processActivities;

    /** Field description */
    private final SubmitDocumentRequestBuilder sdrBuilder;

    /**
     * Constructs ...
     *
     *
     * @param logMessage
     * @param builder
     * @param mapFactory
     * @param regClient
     * @param repoClient
     */
    public SubmitDocumentRequestHandler(XLogMessage logMessage,
            SubmitDocumentRequestBuilder builder, MapperFactory mapFactory,
            XDSDocumentRegistryClient regClient,
            XDSDocumentRepositoryClient repoClient) {

        super();

        // super(logMessage); ??
        this.log_message = logMessage;

        this.sdrBuilder = builder;

        this.processActivities =
            new ArrayList<ISubmitDocumentRequestActivity>();
        this.processActivities.add(new CDAToXDSMapperActivity(mapFactory));
        this.processActivities.add(new SubmitPatientIdActivity(regClient));
        this.processActivities.add(new SubmitPnRActivity(repoClient));
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

        OMFactory fac = OMAbstractFactory.getOMFactory();

        String uri = SubmitDocumentRequestBuilder.URI;
        OMNamespace ns = fac.createOMNamespace(uri, "xdsbridge");
        OMElement result = fac.createOMElement("SubmitDocumentResponse", ns);

        result.addAttribute("status", sdrResponse.getStatus().toString(), null);

        for (ResponseType respType : sdrResponse.getResponses()) {

            OMElement respTypeElem = fac.createOMElement("Response", ns);

            respTypeElem.addAttribute("status",
                                      respType.getStatus().toString(), null);

            // document id is optional
            String docId = respType.getDocumentId();

            if (StringUtils.isNotBlank(docId)) {

                OMElement docElem = fac.createOMElement("DocumentId", ns);

                docElem.setText(docId);

                respTypeElem.addChild(docElem);
            }

            // message is optional
            String msg = respType.getMessage();

            if (StringUtils.isNotBlank(msg)) {

                OMElement msgElem = fac.createOMElement("Message", ns);

                msgElem.setText(respType.getMessage());
                respTypeElem.addChild(msgElem);
            }

            result.addChild(respTypeElem);
        }

        return result;
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

        SubmitDocumentResponse sdrResponse = new SubmitDocumentResponse();

        SubmitDocumentRequest sdrRequest = null;

        try {

            // unmarshal request
            sdrRequest = this.sdrBuilder.buildSubmitDocumentRequest(request);

        } catch (ModelBuilderException e) {

            // this request failed validation (most likely)
            String errmsg =
                String.format(
                    "Request could not be parsed. Failure(s) to follow. %s",
                    e.getMessage());

            sdrResponse.setStatus(Status.Failure);
            sdrResponse.addResponse(ResponseTypeStatus.Failure, errmsg);
        }

        if (sdrRequest != null) {

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

                    logger.debug(String.format("Executing %s",
                                               activity.getName()));

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

        // marshal response
        return marshalResponse(sdrResponse);
    }
}
