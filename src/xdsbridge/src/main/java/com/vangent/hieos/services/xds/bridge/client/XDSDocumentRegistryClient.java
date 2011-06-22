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

package com.vangent.hieos.services.xds.bridge.client;

import com.vangent.hieos.hl7v3util.client.Client;
import com.vangent.hieos.services.xds.bridge.model.PatientIdentityFeed;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-09
 * @author         Jim Horner
 */
public class XDSDocumentRegistryClient extends Client {

    /**
     * Constructs ...
     *
     *
     * @param config
     */
    public XDSDocumentRegistryClient(XConfigActor config) {
        super(config);
    }

    /**
     * Method description
     *
     *
     * @param pif
     *
     * @return
     *
     * @throws AxisFault
     */
    public OMElement sendPatientIdentity(PatientIdentityFeed pif)
            throws AxisFault {
    
        return null;
    }
}
