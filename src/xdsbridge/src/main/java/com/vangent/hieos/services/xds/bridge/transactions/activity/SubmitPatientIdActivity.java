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

import com.vangent.hieos.services.xds.bridge.client.XDSDocumentRegistryClient;
import org.apache.log4j.Logger;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-22
 * @author         Jim Horner
 */
public class SubmitPatientIdActivity implements ISubmitDocumentRequestActivity {

    /** Field description */
    private final static Logger logger =
        Logger.getLogger(SubmitPatientIdActivity.class);

    /** Field description */
    private final XDSDocumentRegistryClient registryClient;

    /**
     * Constructs ...
     *
     *
     * @param client
     */
    public SubmitPatientIdActivity(XDSDocumentRegistryClient client) {

        super();
        this.registryClient = client;
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

//      boolean result = false;
//
//      try {
//
//          Identifier pid = context.getPatientId();
//          OMElement regResponse =
//              this.registryClient.sendPatientIdentity(null);
//
//          // TODO parse response
//
//      } catch (AxisFault e) {
//
//          SubmitDocumentResponse resp = context.getSubmitDocumentResponse();
//
//          resp.addError(context.getDocument(), "E002", e.getMessage());
//      }
//
//      return result;

        return true;
    }
}
