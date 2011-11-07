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
package com.vangent.hieos.services.pixpdq.empi.factory;

import com.vangent.hieos.services.pixpdq.empi.api.EMPIAdapter;
import com.vangent.hieos.services.pixpdq.empi.impl.base.BaseEMPIAdapter;

// FIXME: Replace by Spring loaded or from xconfig file.
//import com.vangent.hieos.services.pixpdq.empi.impl.openempi.OpenEMPIAdapter;

/**
 *
 * @author Bernie Thuman
 */
public class EMPIFactory {

    /**
     * 
     * @return
     */
    static public EMPIAdapter getInstance() {
        // FIXME: Replace by Spring loaded or from xconfig file.
        //return new OpenEMPIAdapter();
        return new BaseEMPIAdapter();
    }
}
