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

import com.vangent.hieos.services.xds.bridge.model.XDSPnR;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-22
 * @author         Jim Horner
 */
public class MockRepositoryClient extends XDSDocumentRepositoryClient {

    /** Field description */
    public int count;

    /** Field description */
    public final boolean throwsExceptionAlways;

    /** Field description */
    public final boolean throwsExceptionOnEven;

    /**
     * Constructs ...
     *
     *
     * @param throwsAlways
     * @param throwsEven
     */
    public MockRepositoryClient(boolean throwsAlways, boolean throwsEven) {

        super(null);
        this.throwsExceptionAlways = throwsAlways;
        this.throwsExceptionOnEven = throwsEven;
        this.count = 0;
    }

    /**
     * Method description
     *
     *
     * @param request
     *
     * @return
     *
     * @throws AxisFault
     */
    @Override
    public OMElement submitProvideAndRegisterDocumentSet(XDSPnR request)
            throws AxisFault {

        OMElement result = null;

        if (this.throwsExceptionAlways) {

            throw new AxisFault("MockClient thows exception always.");
        } else if (this.throwsExceptionOnEven && (this.count % 2) == 0) {

            this.count++;

            throw new AxisFault("MockClient throws exception event.");
        }

        return result;
    }
}
