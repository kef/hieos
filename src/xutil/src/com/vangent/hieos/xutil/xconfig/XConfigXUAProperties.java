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
package com.vangent.hieos.xutil.xconfig;

import java.util.ArrayList;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class XConfigXUAProperties {

    private XConfigProperties properties = new XConfigProperties();
    ArrayList soapActionsList = null;

    /**
     *
     * @param propKey
     * @return
     */
    public String getProperty(String propKey) {
        return (String) properties.getProperty(propKey);
    }

    /**
     *
     * @param propKey
     * @return
     */
    public boolean getPropertyAsBoolean(String propKey) {
        return properties.getPropertyAsBoolean(propKey);
    }

    /**
     *
     * @param soapAction
     * @return
     */
    public boolean containsSOAPAction(String soapAction) {
        return this.soapActionsList.contains(soapAction.toLowerCase());
    }

    /**
     *
     * @param rootNode
     */
    protected void parse(OMElement rootNode) {
        properties.parse(rootNode);
        String soapActionsUnParsed = this.getProperty("SOAPActions");
        String[] soapActions = soapActionsUnParsed.split(";");
        this.soapActionsList = new ArrayList();
        for (int i = 0; i < soapActions.length; i++) {
            soapActionsList.add(soapActions[i].toLowerCase());
        }
    }
}
