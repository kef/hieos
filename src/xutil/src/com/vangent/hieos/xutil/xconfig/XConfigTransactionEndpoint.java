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
 * XConfigTransactionEndpoint encapsulates a transaction endpoint as configured
 * in xconfig.xml.
 *
 * The following XML fragment is an example of a typical configuration, that states that
 * the endpoint is not secure and does not support asynchronous behaviors.
 * <Transaction....
 *    <Endpoint secure="false"
 *           async="false">
 *           http://localhost:8080/axis2/services/xdsregistryb
 *    </Endpoint>
 * </Transaction....
 * 
 * @author Anand Sastry
 */
public class XConfigTransactionEndpoint {

    private String  endpointURL = "";
    private boolean secureEndpoint;
    private boolean asyncEndpoint;

    /**
     * Returns information whether the endpoint is on a secured transport.
     *
     * @return a boolean value.
     */
    public boolean isSecureEndpoint() {
        return secureEndpoint;
    }

    /**
     * Returns information whether the endpoint is for an asynchronous transaction.
     *
     * @return a boolean value.
     */
    public boolean isAsyncEndpoint() {
        return asyncEndpoint;
    }


    /**
     * Returns the endpoint's URL.
     *
     * @return a String value.
     */
    public String getEndpointURL() {
        return endpointURL;
    }

    /**
     * This method, populates the XConfigTransactionEndpoint class by evaluating the input OMElement.
     *
     * @param rootNode, an OMElement, representing the endpoint.
     */
    protected void parse(OMElement rootNode) {
        this.endpointURL = rootNode.getText();
        this.secureEndpoint =
                ("true".equalsIgnoreCase(rootNode.getAttributeValue(new QName("secure")))) ? true : false;
        this.asyncEndpoint =
                ("true".equalsIgnoreCase(rootNode.getAttributeValue(new QName("async")))) ? true : false;

    }

    /**
     * 
     * @return a String representing the state of the XConfigTransactionEndpoint object.
     */
    @Override
    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("\n Transaction Endpoint:");
        sbuf.append("\n   endpointURL: " + this.getEndpointURL());
        sbuf.append("\n   secureEndpoint: " + (this.isSecureEndpoint() ? "true" : "false"));
        sbuf.append("\n   asyncEndpoint: " + (this.isAsyncEndpoint() ? "true" : "false"));
        return sbuf.toString();
    }
}
