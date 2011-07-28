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
package com.vangent.hieos.hl7v3util.model.message;

import com.vangent.hieos.xutil.xml.OMElementWrapper;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class HL7V3Message extends OMElementWrapper {

    private String type;

    /**
     *
     * @param messageNode
     */
    public HL7V3Message(OMElement messageNode, String type) {
        super(messageNode);
        this.type = type;
    }

    /**
     * 
     * @return
     */
    public OMElement getMessageNode() {
        return this.getElement();
    }

    /**
     *
     * @return
     */
    public String getType() {
        return this.type;
    }
}
