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
package com.vangent.hieos.services.sts.model;

import com.vangent.hieos.services.sts.exception.STSException;
import com.vangent.hieos.xutil.exception.XPathHelperException;
import com.vangent.hieos.xutil.xml.XPathHelper;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class STSRequestData {

    private final static Logger logger = Logger.getLogger(STSRequestData.class);
    private String requestType;
    private String appliesToAddress;
    private OMElement request;
    private OMElement claims;
    private SOAPHeaderData headerData;

    public OMElement getRequest() {
        return request;
    }

    public String getAppliesToAddress() {
        return appliesToAddress;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getSoapAction() {
        return headerData.getSoapAction();
    }

    public OMElement getClaims() {
        return claims;
    }

    public SOAPHeaderData getHeaderData() {
        return headerData;
    }

    public void setHeaderData(SOAPHeaderData headerData) {
        this.headerData = headerData;
    }

    /**
     * 
     * @param request
     */
    public void parse(OMElement request) throws STSException {
        this.request = request;
        this.requestType = this.getRequestType(request);
        this.appliesToAddress = this.getAppliesToAddress(request);
        this.claims = this.getClaims(request);
    }

    /**
     *
     * @param request
     * @return
     * @throws STSException
     */
    private String getRequestType(OMElement request) throws STSException {
        OMElement reqTypeElem = request.getFirstChildWithName(new QName(STSConstants.WSTRUST_NS,
                "RequestType"));
        if (reqTypeElem == null
                || reqTypeElem.getText() == null
                || reqTypeElem.getText().trim().length() == 0) {
            throw new STSException("Unable to locate RequestType on request");
        } else {
            return reqTypeElem.getText().trim();
        }
    }

    /**
     *
     * @param request
     * @return
     */
    private String getAppliesToAddress(OMElement request) {
        String result = null;
        try {
            String nameSpaceNames[] = {"wsp", "wsa"};
            String nameSpaceURIs[] = {STSConstants.WSPOLICY_NS, STSConstants.WSADDRESSING_NS};
            OMElement addressNode = XPathHelper.selectSingleNode(
                    request,
                    "./wsp:AppliesTo/wsa:EndpointReference/wsa:Address[1]",
                    nameSpaceNames, nameSpaceURIs);
            if (addressNode != null) {
                result = addressNode.getText();
            }
        } catch (XPathHelperException ex) {
            logger.warn("No AppliesTo reference found");
            //throw new STSException("Can not find AppliesTo reference: " + ex.getMessage());
        }
        return result;
    }

    /**
     *
     * @param request
     * @return
     */
    private OMElement getClaims(OMElement request) {
        OMElement claimsNode = null;
        try {
            claimsNode = XPathHelper.selectSingleNode(
                    request,
                    "./ns:Claims[1]",
                    STSConstants.WSTRUST_NS);
        } catch (XPathHelperException ex) {
            System.out.println("Exception: " + ex.getMessage());
            logger.warn("No Claims found");
        }
        return claimsNode;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("Request Type [").append(this.requestType).append("], AppliesToAddress [").append(this.appliesToAddress).append("]");
        return buf.toString();
    }
}
