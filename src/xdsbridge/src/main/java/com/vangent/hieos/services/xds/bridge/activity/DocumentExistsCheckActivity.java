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

/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */

package com.vangent.hieos.services.xds.bridge.activity;

import org.apache.commons.lang.ClassUtils;

/**
 *
 * @author hornja
 */
public class DocumentExistsCheckActivity
        implements ISubmitDocumentRequestActivity {

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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public String getName() {
        return ClassUtils.getShortClassName(getClass());
    }
}
