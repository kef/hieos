/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2008-2009 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.hl7v3util.model.message;

import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class HL7V3Message {

    private OMElement messageNode;
    private String type;

    /**
     * 
     */
    private HL7V3Message() {
    }

    /**
     *
     * @param messageNode
     */
    public HL7V3Message(OMElement messageNode, String type) {
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
