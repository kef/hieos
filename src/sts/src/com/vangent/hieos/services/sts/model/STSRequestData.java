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

import com.vangent.hieos.policyutil.util.PolicyConstants;
import com.vangent.hieos.services.sts.config.STSConfig;
import com.vangent.hieos.services.sts.exception.STSException;
import com.vangent.hieos.services.sts.util.STSUtil;
import com.vangent.hieos.xutil.exception.XPathHelperException;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xml.XPathHelper;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.security.auth.x500.X500Principal;
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
    private String subjectDN;
    private MessageContext mCtx;
    private XConfigActor stsConfigActor;
    private static STSConfig _stsConfig = null;

    /**
     * 
     */
    private STSRequestData() {
        // Do not allow.
    }

    /**
     *
     * @param stsConfigActor
     * @param mCtx
     * @param request
     */
    public STSRequestData(XConfigActor stsConfigActor, MessageContext mCtx, OMElement request) {
        this.mCtx = mCtx;
        this.request = request;
        this.stsConfigActor = stsConfigActor;
        this.headerData = new SOAPHeaderData(this.getSTSConfig(), this.mCtx);
    }

    /**
     *
     * @return
     */
    public synchronized STSConfig getSTSConfig() {
        if (_stsConfig == null) {
            _stsConfig = new STSConfig(stsConfigActor);
        }
        return _stsConfig;
    }

    /**
     *
     * @throws STSException
     */
    public void parseHeader() throws STSException {
        this.headerData.parse();
        //return this.headerData;
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
            ClaimBuilder claimBuilder = new ClaimBuilder();
            this.claims = claimBuilder.parse(this);
            String useSubjectNameFromClaimURI = this.getSTSConfig().getUseSubjectNameFromClaimURI();
            if (useSubjectNameFromClaimURI != null) {
                // Override any previously set SubjectName
                this.setComputedSubjectName(useSubjectNameFromClaimURI);
            }
        }
    }

    /**
     *
     * @return
     */
    private void setComputedSubjectName(String useSubjectNameFromClaimURI) {
        // PRECONDITION: Authentication already took place.
        String newSubjectName;

        // CN=jack,OU=Sun GlassFish Enterprise Server,O=Sun Microsystems,L=Santa Clara,ST=California,C=US
        // CN=urn:oasis:names:tc:xacml:1.0:subject:subject-id
        // OU=urn:oasis:names:tc:xspa:1.0:subject:organization-id
        // O=urn:oasis:names:tc:xspa:1.0:subject:organization
        // L=XXX
        // ST=XXX
        // C=XXX

        X509Certificate clientCert = headerData.getClientCertificate();
        if (clientCert != null) {
            X500Principal principal = clientCert.getSubjectX500Principal();
            String principalDN = principal.getName();
            X500Name x500Name = new X500Name(principalDN);

            // Just override the CN using value of configured claim URI (if using cert).
            String newCN = this.getClaimStringValue(useSubjectNameFromClaimURI);
            x500Name.replace("CN", newCN);
            newSubjectName = x500Name.toString();
        } else {
            // Assume userName/userPassword
            newSubjectName = this.getClaimStringValue(useSubjectNameFromClaimURI);
        }
        this.setSubjectDN(newSubjectName);
    }

    /**
     *
     * @param name
     * @return
     */
    // TBD: Move claim support to PolicyUtil?
    private String getClaimStringValue(String name) {
        for (Claim claim : claims) {
            String claimName = claim.getName();
            if (claimName.equalsIgnoreCase(name)) {
                return claim.getStringValue();
            }
        }
        return null;
    }

    /**
     *
     * @return
     */
    public List<Claim> getClaims() {
        return claims;
    }

    /**
     *
     * @return
     */
    public OMElement getRequest() {
        return request;
    }

    /**
     *
     * @return
     */
    public String getAppliesToAddress() {
        return appliesToAddress;
    }

    /**
     *
     * @return
     */
    public String getRequestType() {
        return requestType;
    }

    /**
     *
     * @return
     */
    public String getSoapAction() {
        return headerData.getSoapAction();
    }

    /**
     *
     * @return
     */
    public OMElement getClaimsNode() {
        return claimsNode;
    }

    /**
     *
     * @return
     */
    public SOAPHeaderData getHeaderData() {
        return headerData;
    }

    /**
     *
     * @return
     */
    public String getSubjectDN() {
        return subjectDN;
    }

    /**
     *
     * @param subjectDN
     */
    public void setSubjectDN(String subjectDN) {
        this.subjectDN = subjectDN;
    }

    /**
     *
     * @param request
     * @return
     * @throws STSException
     */
    private String getRequestType(OMElement request) throws STSException {
        return STSUtil.getRequestType(request);
        /*
        OMElement reqTypeElem = request.getFirstChildWithName(new QName(PolicyConstants.WSTRUST_NS,
        "RequestType"));
        if (reqTypeElem == null
        || reqTypeElem.getText() == null
        || reqTypeElem.getText().trim().length() == 0) {
        throw new STSException("Unable to locate RequestType on request");
        } else {
        return reqTypeElem.getText().trim();
        }*/
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
            // OK - can process without claims without failure.
            logger.warn("No Claims found: " + ex.getMessage());
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
