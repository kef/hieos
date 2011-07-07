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

import com.vangent.hieos.services.xds.bridge.client.XDSDocumentRegistryClient;
import com.vangent.hieos.services.xds.bridge.message
    .GetDocumentsSQRequestBuilder;
import com.vangent.hieos.services.xds.bridge.message
    .GetDocumentsSQRequestMessage;
import com.vangent.hieos.services.xds.bridge.message
    .GetDocumentsSQResponseMessage;
import com.vangent.hieos.services.xds.bridge.model.Document;
import com.vangent.hieos.services.xds.bridge.model.ResponseType
    .ResponseTypeStatus;
import com.vangent.hieos.services.xds.bridge.model.SubmitDocumentResponse;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.response.RegistryResponseParser;
import com.vangent.hieos.xutil.xml.XPathHelper;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;

import java.util.List;


/**
 * Class description
 *
 *
 * @version        v1.0, 2011-07-06
 * @author         Jim Horner
 */
public class DocumentExistsCheckActivity
        implements ISubmitDocumentRequestActivity {

    /** Field description */
    private static final Logger logger =
        Logger.getLogger(DocumentExistsCheckActivity.class);

    /** Field description */
    private final XDSDocumentRegistryClient registryClient;

    /**
     * Constructs ...
     *
     *
     * @param client
     */
    public DocumentExistsCheckActivity(XDSDocumentRegistryClient client) {

        super();
        this.registryClient = client;
    }

    /**
     * Method description
     *
     *
     * @param registryResponse
     * @param context
     *
     * @return
     */
    private boolean checkForSuccess(
            GetDocumentsSQResponseMessage registryResponse,
            SDRActivityContext context) {

        boolean result = false;

        SubmitDocumentResponse sdrResponse =
            context.getSubmitDocumentResponse();
        Document document = context.getDocument();
        OMElement rootNode = registryResponse.getMessageNode();

        try {

            RegistryResponseParser parser =
                new RegistryResponseParser(rootNode);

            if (parser.is_error()) {

                String errmsg = parser.get_regrep_error_msg();

                sdrResponse.addResponse(document, ResponseTypeStatus.Failure,
                                        errmsg);

            } else {

                // search for any nodes
                String expr = "./ns:RegistryObjectList/ns:ObjectRef";
                List<OMElement> docs =
                    XPathHelper.selectNodes(
                        rootNode, expr, GetDocumentsSQRequestBuilder.RIM_URI);

                if (docs.isEmpty()) {
                    
                    result = true;
                    
                } else {

                    String errmsg =
                        "Document already exists in the registry. It will not be processed.";

                    logger.error(errmsg);
                    sdrResponse.addResponse(document,
                                            ResponseTypeStatus.Failure, errmsg);
                }
            }

        } catch (XdsInternalException e) {

            // log it
            logger.error(e, e);

            // capture in response
            StringBuilder sb = new StringBuilder();

            sb.append(
                "Unable to parse repository response, exception follows. ");
            sb.append(e.getMessage());

            sdrResponse.addResponse(document, ResponseTypeStatus.Failure,
                                    sb.toString());
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

        Document document = context.getDocument();

        if (document.isGeneratedDocumentId()) {

            // if we generated the document id then it is unique
            // no need to check
            result = true;

        } else {

            try {

                GetDocumentsSQRequestBuilder builder =
                    new GetDocumentsSQRequestBuilder();

                GetDocumentsSQRequestMessage msg =
                    builder.buildMessage(document.getDocumentIdAsOID());

                GetDocumentsSQResponseMessage registryResponse =
                    this.registryClient.getDocuments(msg);

                result = checkForSuccess(registryResponse, context);

            } catch (AxisFault e) {

                SubmitDocumentResponse sdrResponse =
                    context.getSubmitDocumentResponse();

                sdrResponse.addResponse(ResponseTypeStatus.Failure,
                                        e.getMessage());
            }
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
