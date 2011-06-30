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
import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.xutil.xconfig.XConfigActor;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-29
 * @author         Jim Horner
 */
public abstract class AbstractClient extends Client {

    /**
     * Constructs ...
     *
     *
     * @param config
     */
    public AbstractClient(XConfigActor config) {
        super(config);
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

        XConfigActor actor = getConfig();
        String recId = actor.getProperty("ReceiverDeviceId");
        String recName = actor.getProperty("ReceiverDeviceName");

        DeviceInfo result = new DeviceInfo();

        result.setId(recId);
        result.setName(recName);

        return result;
    }
}
