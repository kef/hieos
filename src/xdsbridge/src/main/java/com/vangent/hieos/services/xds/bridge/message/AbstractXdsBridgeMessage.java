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

import org.apache.axiom.om.OMElement;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-29
 * @author         Jim Horner    
 */
public abstract class AbstractXdsBridgeMessage {

    /** Field description */
    private final OMElement messageNode;

    /** Field description */
    private final String type;

    /**
     *
     * @param messageNode
     * @param type
     */
    public AbstractXdsBridgeMessage(OMElement messageNode, String type) {

        super();
        this.messageNode = messageNode;
        this.type = type;
    }

    /**
     *
     * @return
     */
    public OMElement getMessageNode() {
        return this.messageNode;
    }

    /**
     *
     * @return
     */
    public String getType() {
        return this.type;
    }
}
