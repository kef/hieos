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

import com.vangent.hieos.services.sts.config.STSConfig;
import com.vangent.hieos.services.sts.exception.STSException;
import com.vangent.hieos.xutil.exception.XPathHelperException;
import com.vangent.hieos.xutil.xml.XPathHelper;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;
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
    private OMElement claimsNode;
    private List<Claim> claims;
    private SOAPHeaderData headerData;
    private String subjectName;
    private MessageContext mCtx;
    private STSConfig stsConfig;

    /**
     * 
     */
    private STSRequestData() {
        // Do not allow.
    }

    /**
     *
     * @param stsConfig
     * @param mCtx
     * @param request
     */
    public STSRequestData(STSConfig stsConfig, MessageContext mCtx, OMElement request) {
        this.stsConfig = stsConfig;
        this.mCtx = mCtx;
        this.request = request;

        // Just parse the SOAP header.
        this.headerData = new SOAPHeaderData(this.stsConfig, this.mCtx);
    }

    /**
     *
     * @return
     */
    public STSConfig getSTSConfig() {
        return stsConfig;
    }

    /**
     *
     * @return
     * @throws STSException
     */
    public SOAPHeaderData parseHeader() throws STSException {
        this.headerData.parse();
        return this.headerData;
    }

    /**
     *
     * @throws STSException
     */
    public void parseBody() throws STSException {
        this.requestType = this.getRequestType(request);
        this.appliesToAddress = this.getAppliesToAddress(request);
        if (this.requestType.equalsIgnoreCase(STSConstants.ISSUE_REQUEST_TYPE)) {
            this.claimsNode = this.getClaimsNode(request);
            System.out.println("Claims = " + claimsNode.toString());
            ClaimBuilder claimBuilder = new ClaimBuilder();
            this.claims = claimBuilder.parse(this);
            if (stsConfig.getComputeSubjectNameFromClaims()) {
                // Override any previously set SubjectName
                this.setSubjectName(this.getComputedSubjectName());
            }
        }
    }

    /**
     *
     * @return
     */
    private String getComputedSubjectName() {
        // CN=jack,OU=Sun GlassFish Enterprise Server,O=Sun Microsystems,L=Santa Clara,ST=California,C=US
        // CN=urn:oasis:names:tc:xacml:1.0:subject:subject-id
        // OU=urn:oasis:names:tc:xspa:1.0:subject:organization-id
        // O=urn:oasis:names:tc:xspa:1.0:subject:organization
        // L=XXX
        // ST=XXX
        // C=XXX
        String CN = this.getClaimStringValue("urn:oasis:names:tc:xacml:1.0:subject:subject-id");
        String OU = this.getClaimStringValue("urn:oasis:names:tc:xspa:1.0:subject:organization-id");
        String O = this.getClaimStringValue("urn:oasis:names:tc:xspa:1.0:subject:organization");
        return "CN=" + CN + ", OU=" + OU + ", O=" + O;
    }

    /**
     *
     * @param name
     * @return
     */
    private String getClaimStringValue(String name) {
        for (Claim claim : claims) {
            String claimName = claim.getName();
            if (claimName.equalsIgnoreCase(name)) {
                return claim.getStringValue();
            }
        }
        return null;
    }

    public List<Claim> getClaims() {
        return claims;
    }

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

    public OMElement getClaimsNode() {
        return claimsNode;
    }

    public SOAPHeaderData getHeaderData() {
        return headerData;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
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
    private OMElement getClaimsNode(OMElement request) {
        OMElement node = null;
        try {
            node = XPathHelper.selectSingleNode(
                    request,
                    "./ns:Claims[1]",
                    STSConstants.WSTRUST_NS);
        } catch (XPathHelperException ex) {
            System.out.println("Exception: " + ex.getMessage());
            logger.warn("No Claims found");
        }
        return node;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("Request Type [").append(this.requestType).append("], AppliesToAddress [").append(this.appliesToAddress).append("]");
        return buf.toString();
    }
}
