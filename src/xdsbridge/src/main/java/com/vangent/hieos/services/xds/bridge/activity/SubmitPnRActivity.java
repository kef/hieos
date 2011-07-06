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

import com.vangent.hieos.services.xds.bridge.client.XDSDocumentRepositoryClient;
import com.vangent.hieos.services.xds.bridge.model.Document;
import com.vangent.hieos.services.xds.bridge.model.ResponseType
    .ResponseTypeStatus;
import com.vangent.hieos.services.xds.bridge.model.SubmitDocumentResponse;
import com.vangent.hieos.services.xds.bridge.message.XDSPnRMessage;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.response.RegistryResponseParser;
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
public class SubmitPnRActivity implements ISubmitDocumentRequestActivity {

    /** Field description */
    private static final Logger logger =
        Logger.getLogger(SubmitPnRActivity.class);

    /** Field description */
    private final XDSDocumentRepositoryClient repositoryClient;

    /**
     * Constructs ...
     *
     *
     * @param client
     */
    public SubmitPnRActivity(XDSDocumentRepositoryClient client) {

        super();
        this.repositoryClient = client;
    }

    /**
     * Method description
     *
     *
     * @param pnrResponse
     * @param context
     *
     * @return
     */
    private boolean checkForSuccess(OMElement pnrResponse,
                                    SDRActivityContext context) {

        boolean result = false;

        SubmitDocumentResponse sdrResponse =
            context.getSubmitDocumentResponse();
        Document document = context.getDocument();

        try {

            RegistryResponseParser parser =
                new RegistryResponseParser(pnrResponse);

            if (parser.is_error()) {

                String errmsg = parser.get_regrep_error_msg();

                sdrResponse.addResponse(document, ResponseTypeStatus.Failure,
                                        errmsg);

            } else {

                result = true;
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

        // send PNR
        XDSPnRMessage pnr = context.getXdspnr();

        try {

            OMElement pnrResponse =
                this.repositoryClient.submitProvideAndRegisterDocumentSet(pnr);

            result = checkForSuccess(pnrResponse, context);

        } catch (AxisFault e) {

            SubmitDocumentResponse resp = context.getSubmitDocumentResponse();

            resp.addResponse(context.getDocument(), ResponseTypeStatus.Failure,
                             e.getMessage());
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
