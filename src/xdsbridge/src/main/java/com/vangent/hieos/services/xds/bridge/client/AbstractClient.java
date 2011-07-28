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

import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.services.xds.bridge.support.XDSBridgeConfig;
import com.vangent.hieos.xutil.soap.WebServiceClient;
import com.vangent.hieos.xutil.xconfig.XConfigActor;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-29
 * @author         Jim Horner
 */
public abstract class AbstractClient extends WebServiceClient {

    /** Field description */
    private final XDSBridgeConfig xdsBridgeConfig;

    /**
     * Constructs ...
     *
     *
     *
     *
     * @param xdsBridgeConfig
     * @param config
     */
    public AbstractClient(XDSBridgeConfig xdsBridgeConfig,
                          XConfigActor config) {

        super(config);
        this.xdsBridgeConfig = xdsBridgeConfig;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public DeviceInfo createReceiverDeviceInfo() {

        XConfigActor actor = getConfig();
        String recId = actor.getProperty("ReceiverDeviceId");
        String recName = actor.getProperty("ReceiverDeviceName");

        DeviceInfo result = new DeviceInfo();

        result.setId(recId);
        result.setName(recName);

        return result;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public DeviceInfo createSenderDeviceInfo() {

        return new DeviceInfo(getXdsBridgeConfig().getXdsBridgeActor());
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
