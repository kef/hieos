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

import com.vangent.hieos.xutil.xconfig.XConfigTransaction;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-07-28
 * @author         Vangent
 */
public class MockXConfigTransaction extends XConfigTransaction {

    /** Field description */
    private final String name;

    /**
     * Constructs ...
     *
     *
     * @param name
     */
    public MockXConfigTransaction(String name) {

        super();
        this.name = name;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public String getEndpointURL() {
        return "http://nowhere";
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public String getName() {
        return this.name;
    }
}
