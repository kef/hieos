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
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class XConfigObject {

    private String name = "";
    private String type = "";
    private String uniqueId = "";
    private XConfigProperties properties = new XConfigProperties();
    private List<XConfigObjectRef> objectRefs = new ArrayList<XConfigObjectRef>();

    /**
     * Get the value of uniqueId
     *
     * @return the value of uniqueId
     */
    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * Get the value of name
     *
     * @return the value of name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the value of type
     *
     * @return the value of type
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @return
     */
    public List<XConfigObjectRef> getObjectRefs() {
        return this.objectRefs;
    }

    /**
     *
     * @param type
     * @return
     */
    public List<XConfigObject> getXConfigObjectsWithType(String type) {
        List<XConfigObject> configObjects = new ArrayList<XConfigObject>();
        for (XConfigObjectRef objRef : this.objectRefs) {
            if (objRef.getRefType().equals(type)) {
                XConfigObject configObject = objRef.getXConfigObject();
                if (configObject != null) {
                    configObjects.add(configObject);
                }
            }
        }
        return configObjects;
    }

    /**
     *
     * @param name
     * @param type
     * @return
     */
    public XConfigObject getXConfigObjectWithName(String name, String type) {
        XConfigObject configObject = null;
        for (XConfigObjectRef objRef : this.objectRefs) {
            if (objRef.getRefType().equals(type)) {
                if (objRef.getName().equals(name)) {
                    configObject = objRef.getXConfigObject();
                    break;
                }
            }
        }
        return configObject;
    }

    /**
     *
     * @param propKey The property key to find.
     * @return true if property key is found, false otherwise.
     */
    public boolean containsProperty(String propKey) {
        return properties.containsProperty(propKey);
    }

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
     * @param propKey
     * @param defaultValue
     * @return
     */
    public boolean getPropertyAsBoolean(String propKey, boolean defaultValue) {
        return properties.getPropertyAsBoolean(propKey, defaultValue);
    }

    /**
     * Fills in the instance from a given AXIOM node.
     *
     * @param rootNode Starting point.
     */
    protected void parse(OMElement rootNode, XConfig xconf) {
        OMElement node;
        this.name = rootNode.getAttributeValue(new QName("name"));
        this.type = rootNode.getAttributeValue(new QName("type"));
        node = rootNode.getFirstChildWithName(new QName("UniqueId"));
        if (node != null) {
            this.uniqueId = node.getText();
            if (this.uniqueId.equals("NA")) {
                this.uniqueId = null;
            }
        }
        parseObjectRefs(rootNode);
        properties.parse(rootNode);
    }

    /**
     * Fills in the instance from a given AXIOM node.
     *
     * @param rootNode Starting point.
     */
    private void parseObjectRefs(OMElement rootNode) {
        List<OMElement> list = XConfig.parseLevelOneNode(rootNode, "ObjectReference");
        for (OMElement currentNode : list) {
            XConfigObjectRef objRef = new XConfigObjectRef();
            objRef.parse(currentNode);
            this.objectRefs.add(objRef);
        }
    }
}
