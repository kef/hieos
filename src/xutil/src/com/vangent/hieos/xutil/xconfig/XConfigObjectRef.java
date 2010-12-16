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

import javax.xml.namespace.QName;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class XConfigObjectRef {

    XConfigObject objectRef = null;
    private String name = null;
    private String refName = null;
    private String refType = null;

    /**
     *
     * @return
     */
    public XConfigObject getXConfigObject() {
        return objectRef;
    }

    /**
     *
     * @return
     */
    public void setXConfigObject(XConfigObject configObject) {
        this.objectRef = configObject;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return this.name;
    }

    /**
     *
     * @return
     */
    public String getRefName() {
        return this.refName;
    }

    /**
     * 
     * @return
     */
    public String getRefType() {
        return this.refType;
    }

    /**
     * Fills in the instance from a given AXIOM node.
     *
     * @param rootNode Starting point.
     */
    protected void parse(OMElement rootNode) {
        this.name = rootNode.getAttributeValue(new QName("name"));
        this.refName = rootNode.getAttributeValue(new QName("refname"));
        this.refType = rootNode.getAttributeValue(new QName("reftype"));
    }
}
