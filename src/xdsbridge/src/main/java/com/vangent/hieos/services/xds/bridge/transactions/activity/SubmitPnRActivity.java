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

package com.vangent.hieos.services.xds.bridge.transactions.activity;

import com.vangent.hieos.services.xds.bridge.client.XDSDocumentRepositoryClient;
import com.vangent.hieos.services.xds.bridge.model.SubmitDocumentResponse;
import com.vangent.hieos.services.xds.bridge.model.XDSPnR;
import com.vangent.hieos.services.xds.bridge.utils.DebugUtils;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
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
    private final static Logger logger =
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
     * @param context
     *
     * @return
     */
    @Override
    public boolean execute(SDRActivityContext context) {

        boolean result = false;

        // send PNR
        XDSPnR pnr = context.getXdspnr();

        try {

            OMElement pnrResponse =
                this.repositoryClient.submitProvideAndRegisterDocumentSet(pnr);

            logger.debug(DebugUtils.toPrettyString(pnrResponse));

            // TODO parse response

        } catch (AxisFault e) {

            SubmitDocumentResponse resp = context.getSubmitDocumentResponse();

            resp.addError(context.getDocument(), "E003", e.getMessage());
        }

        return result;
    }
}
