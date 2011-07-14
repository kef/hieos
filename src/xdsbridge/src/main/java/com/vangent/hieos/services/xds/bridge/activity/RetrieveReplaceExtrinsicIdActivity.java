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

import java.util.List;
import com.vangent.hieos.services.xds.bridge.client.XDSDocumentRegistryClient;
import com.vangent.hieos.services.xds.bridge.message
    .GetDocumentsSQResponseMessage;
import com.vangent.hieos.services.xds.bridge.model.Document;
import com.vangent.hieos.services.xds.bridge.model.ResponseType
    .ResponseTypeStatus;
import com.vangent.hieos.services.xds.bridge.model.SubmitDocumentResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-07-13
 * @author         Vangent
 */
public class RetrieveReplaceExtrinsicIdActivity
        extends AbstractGetDocumentsSQActivity {

    /** Field description */
    private static final Logger logger =
        Logger.getLogger(RetrieveReplaceExtrinsicIdActivity.class);

    /**
     * Constructs ...
     *
     *
     * @param client
     */
    public RetrieveReplaceExtrinsicIdActivity(
            XDSDocumentRegistryClient client) {

        super(client);
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

        List<String> objectRefs = parseObjectRefs(registryResponse, context);

        if (objectRefs.isEmpty() == false) {

            // sync with the document
            String replExtObjId = objectRefs.get(0);

            logger.debug(
                String.format(
                    "Setting replaceExtrinsicObjectId [%s].", replExtObjId));

            document.setReplaceExtrinsicObjectId(replExtObjId);
            result = true;

        } else {

            String errmsg =
                String.format(
                    "Replace document id [%s] does not exist in the registry.",
                    document.getId());

            logger.error(errmsg);
            sdrResponse.addResponse(document, ResponseTypeStatus.Failure,
                                    errmsg);
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

        if (StringUtils.isBlank(document.getReplaceId())) {

            // no replace id, don't check
            result = true;

        } else {

            GetDocumentsSQResponseMessage getDocsResp =
                callGetDocumentsSQ(context, document.getReplaceIdAsOID());

            result = checkForSuccess(getDocsResp, context);
            
            if (result) {
                
                context.getXdspnr().addReplaceAssociation(document);
            }
        }

        return result;
    }
}
