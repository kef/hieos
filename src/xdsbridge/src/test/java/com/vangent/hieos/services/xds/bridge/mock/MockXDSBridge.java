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

package com.vangent.hieos.services.xds.bridge.mock;

import com.vangent.hieos.services.xds.bridge.serviceimpl.XDSBridge;
import com.vangent.hieos.services.xds.bridge.support.XDSBridgeServiceContext;
import com.vangent.hieos.xutil.exception.SOAPFaultException;
import com.vangent.hieos.xutil.xlog.client.XLogger;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-17
 * @author         Jim Horner
 */
public class MockXDSBridge extends XDSBridge {

    /**
     * Constructs ...
     *
     *
     * @param config
     */
    public MockXDSBridge(XDSBridgeServiceContext config) {

        super();
        XDSBridge.serviceContext = config;
    }

    /**
     * Method description
     *
     *
     * @param service_name
     * @param request
     *
     * @return
     *
     * @throws AxisFault
     */
    @Override
    protected void beginTransaction(String service_name,
            OMElement request)
            throws SOAPFaultException {

        String remoteIP = "127.0.0.2";
        XLogger xlogger = XLogger.getInstance();

        log_message = xlogger.getNewMessage(remoteIP);
        log_message.setTestMessage("TEST");
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    protected MessageContext getCurrentMessageContext() {

        return new MockMessageContext();
    }
}
