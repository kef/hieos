/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2010 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.empi.factory;

import com.vangent.hieos.empi.api.EMPIAdapter;
import com.vangent.hieos.empi.impl.base.BaseEMPIAdapter;
import com.vangent.hieos.xutil.xconfig.XConfigActor;

/**
 *
 * @author Bernie Thuman
 */
public class EMPIFactory {

    /**
     *
     * @param configActor
     * @return
     */
    static public EMPIAdapter getInstance(XConfigActor configActor) {
        // FIXME: Replace by dynamic load using xconfig file.
        EMPIAdapter empiAdaptor = new BaseEMPIAdapter();
        empiAdaptor.setConfig(configActor);
        return empiAdaptor;
    }
}
