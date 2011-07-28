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
package com.vangent.hieos.xutil.xml;

import java.util.List;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
abstract public class OMElementListWrapper extends OMElementWrapper {

    private List<OMElement> elementList;

    /**
     *
     * @param elementList
     * @param type
     */
    public OMElementListWrapper(List<OMElement> elementList, String type) {
        super(type);
        this.elementList = elementList;
    }

    /**
     *
     * @return
     */
    public List<OMElement> getElementList() {
        return elementList;
    }

    /**
     *
     * @param elementList
     */
    public void setElementList(List<OMElement> elementList) {
        this.elementList = elementList;
    }
}
