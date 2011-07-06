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

package com.vangent.hieos.services.xds.bridge.message;

import com.vangent.hieos.services.xds.bridge.support.XDSBridgeConfig;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-29
 * @author         Jim Horner
 */
public abstract class AbstractXdsBridgeMessageBuilder {

    /** Field description */
    public static final String XDSBRIDGE_URI =
        "http://schemas.hieos.vangent.com/xdsbridge";

    /** Field description */
    private final XDSBridgeConfig xdsBridgeConfig;

    /**
     * Constructs ...
     *
     *
     *
     * @param xdsBridgeConfig
     */
    public AbstractXdsBridgeMessageBuilder(XDSBridgeConfig xdsBridgeConfig) {

        super();
        this.xdsBridgeConfig = xdsBridgeConfig;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public OMNamespace createOMNamespace() {

        OMFactory fac = OMAbstractFactory.getOMFactory();

        return fac.createOMNamespace(XDSBRIDGE_URI, "xdsbridge");
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public XDSBridgeConfig getXdsBridgeConfig() {
        return xdsBridgeConfig;
    }
}
