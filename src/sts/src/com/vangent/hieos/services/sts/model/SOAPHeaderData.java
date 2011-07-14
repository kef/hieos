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
import com.vangent.hieos.services.sts.util.STSUtil;
import com.vangent.hieos.xutil.exception.XPathHelperException;
import com.vangent.hieos.xutil.xml.XPathHelper;
import java.security.cert.X509Certificate;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.context.MessageContext;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 *
 * @author Bernie Thuman
 */
public class SOAPHeaderData {

    // Examples:
    //
    // UserNameToken:
    //
    //<soapenv:Header xmlns:wsa="http://www.w3.org/2005/08/addressing">
    //  <wsse:Security xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
    //     <wsu:Timestamp wsu:Id="Timestamp-2" xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd">
    //        <wsu:Created>2011-06-01T20:45:49.881Z</wsu:Created>
    //        <wsu:Expires>2011-06-04T20:45:49.881Z</wsu:Expires>
    //     </wsu:Timestamp>
    //     <wsse:UsernameToken>
    //        <wsse:Username>stsclient</wsse:Username>
    //        <wsse:Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText">stsclient</wsse:Password>
    //     </wsse:UsernameToken>
    //  </wsse:Security>
    //</soapenv:Header>
    //
    // BinarySecurityToken (X.509 Certificate):
    //
    //<soapenv:Header xmlns:wsa="http://www.w3.org/2005/08/addressing">
    //  <wsse:Security xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
    //     <wsu:Timestamp wsu:Id="Timestamp-2" xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd">
    //        <wsu:Created>2011-06-01T20:45:49.881Z</wsu:Created>
    //        <wsu:Expires>2011-06-04T20:45:49.881Z</wsu:Expires>
    //     </wsu:Timestamp>
    //     <wsse:BinarySecurityToken wsu:Id="binarytoken"
    //         xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"
    //         ValueType="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3"
    //         EncodingType="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary">
    //        ...
    //     </wsse:BinarySecurityToken>
    //  </wsse:Security>
    //</soapenv:Header>
    //
    private DateTime timestampCreated;
    private DateTime timestampExpires;
    private String userName;
    private String userPassword;
    private STSConstants.AuthenticationType authenticationType;
    private X509Certificate certificate;
    private String soapAction;
    private MessageContext mCtx;
    private STSConfig stsConfig;

    private SOAPHeaderData() {
        // Do not allow.
    }

    /**
     *
     * @param stsConfig
     * @param mCtx
     */
    public SOAPHeaderData(STSConfig stsConfig, MessageContext mCtx) {
        this.stsConfig = stsConfig;
        this.mCtx = mCtx;
    }

    /**
     *
     * @return
     */
    public DateTime getTimestampCreated() {
        return timestampCreated;
    }

    /**
     *
     * @return
     */
    public DateTime getTimestampExpires() {
        return timestampExpires;
    }

    /**
     * 
     * @return
     */
    public String getUserName() {
        return userName;
    }

    /**
     *
     * @return
     */
    public String getUserPassword() {
        return userPassword;
    }

    /**
     *
     * @return
     */
    public X509Certificate getClientCertificate() {
        return certificate;
    }

    /**
     *
     * @return
     */
    public STSConstants.AuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    /**
     *
     * @return
     */
    public String getSoapAction() {
        return soapAction;
    }

    /**
     *
     * @throws STSException
     */
    public void parse() throws STSException {
        this.soapAction = mCtx.getSoapAction();
        SOAPEnvelope env = mCtx.getEnvelope();
        SOAPHeader header = env.getHeader();
        if (header == null) {
            throw new STSException("No SOAP header found");
        }
        OMElement securityHeader = header.getFirstChildWithName(new QName(STSConstants.WSSECURITY_NS, "Security"));
        if (securityHeader == null) {
            throw new STSException("No Security header found");
        }
        timestampCreated = this.getTimestampCreated(securityHeader);
        timestampExpires = this.getTimestampExpires(securityHeader);
        this.validateTimestamp();
        this.getAuthenticationInfo(securityHeader);
    }

    /**
     * 
     * @param securityHeader
     * @throws STSException
     */
    private void getAuthenticationInfo(OMElement securityHeader) throws STSException {
        // Check to see if UserNameToken is present or BinarySecurityToken.
        if (soapAction.equalsIgnoreCase(STSConstants.WSTRUST_ISSUE_ACTION)) {
            switch (stsConfig.getAuthenticationType()) {
                case USER_NAME_TOKEN:
                    if (!this.isUserNameToken(securityHeader)) {
                        throw new STSException("No UsernameToken found");
                    }
                    authenticationType = STSConstants.AuthenticationType.USER_NAME_TOKEN;
                    userName = this.getUsername(securityHeader);
                    if (userName == null) {
                        throw new STSException("No Username provided on UsernameToken");
                    }
                    userPassword = this.getUserPassword(securityHeader);
                    if (userPassword == null) {
                        throw new STSException("No Password provided on UsernameToken");
                    }
                    break;
                case X509_CERTIFICATE:
                    if (!this.isBinarySecurityToken(securityHeader)) {
                        throw new STSException("No BinarySecurityToken found");
                    }
                    authenticationType = STSConstants.AuthenticationType.X509_CERTIFICATE;
                    certificate = this.getX509Certificate(securityHeader);
                    if (certificate == null) {
                        throw new STSException("No Certificate provided on BinarySecurityToken");
                    }
                    break;
                default:
                    throw new STSException("Unknown Authentication Type");
            }
        }
    }

    /**
     *
     * @throws STSException
     */
    private void validateTimestamp() throws STSException {
        if (this.timestampCreated == null) {
            throw new STSException("No Timestamp 'Created' provided");
        }

        if (this.timestampExpires == null) {
            throw new STSException("No Timestamp 'Expired' provided");
        }
        // Do some basic timestamp checking.
        if (this.timestampCreated.isAfter(this.timestampExpires)) {
            throw new STSException("Timestamp 'Created' is > 'Expires'");
        }

        // Now check for message expiration.
        if (this.timestampExpires.isBeforeNow()) {
            throw new STSException("Timestamp is expired");
        }
    }

    /**
     *
     * @param securityHeader
     * @return
     */
    private DateTime getTimestampCreated(OMElement securityHeader) {
        DateTime time = null;
        try {
            OMElement timeNode = XPathHelper.selectSingleNode(
                    securityHeader,
                    "./ns:Timestamp/ns:Created[1]",
                    STSConstants.WSSECURITY_UTILITY_NS);
            if (timeNode != null) {
                String timeString = timeNode.getText();
                DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
                time = fmt.parseDateTime(timeString);
            }
        } catch (XPathHelperException ex) {
            // Do nothing - will be validated later.
        }
        return time;
    }

    /**
     *
     * @param securityHeader
     * @return
     */
    private DateTime getTimestampExpires(OMElement securityHeader) {
        DateTime time = null;
        try {
            OMElement timeNode = XPathHelper.selectSingleNode(
                    securityHeader,
                    "./ns:Timestamp/ns:Expires[1]",
                    STSConstants.WSSECURITY_UTILITY_NS);
            if (timeNode != null) {
                String timeString = timeNode.getText();
                DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
                time = fmt.parseDateTime(timeString);
            }
        } catch (XPathHelperException ex) {
            // Do nothing - will be validated later.
        }
        return time;
    }

    /**
     * 
     * @param securityHeader
     * @return
     */
    private boolean isUserNameToken(OMElement securityHeader) {
        boolean result = false;
        try {
            OMElement node = XPathHelper.selectSingleNode(
                    securityHeader,
                    "./ns:UsernameToken[1]",
                    STSConstants.WSSECURITY_NS);
            if (node != null) {
                result = true;
            }
        } catch (XPathHelperException ex) {
            // Do nothing - will be validated later.
        }
        return result;
    }

    /**
     * 
     * @param securityHeader
     * @return
     */
    private boolean isBinarySecurityToken(OMElement securityHeader) {
        boolean result = false;
        try {
            OMElement node = XPathHelper.selectSingleNode(
                    securityHeader,
                    "./ns:BinarySecurityToken[1]",
                    STSConstants.WSSECURITY_NS);
            if (node != null) {
                result = true;
            }
        } catch (XPathHelperException ex) {
            // Do nothing - will be validated later.
        }
        return result;
    }

    /**
     *
     * @param securityHeader
     * @return
     */
    private String getUsername(OMElement securityHeader) {
        String result = null;
        try {
            OMElement node = XPathHelper.selectSingleNode(
                    securityHeader,
                    "./ns:UsernameToken/ns:Username[1]",
                    STSConstants.WSSECURITY_NS);
            if (node != null) {
                result = node.getText();
            }
        } catch (XPathHelperException ex) {
            // Do nothing - will be validated later.
        }
        return result;
    }

    /**
     *
     * @param securityHeader
     * @return
     */
    private String getUserPassword(OMElement securityHeader) {
        String result = null;
        try {
            OMElement node = XPathHelper.selectSingleNode(
                    securityHeader,
                    "./ns:UsernameToken/ns:Password[1]",
                    STSConstants.WSSECURITY_NS);
            if (node != null) {
                result = node.getText();
            }
        } catch (XPathHelperException ex) {
            // Do nothing - will be validated later.
        }
        return result;
    }

    /**
     * 
     * @param securityHeader
     * @return
     */
    private X509Certificate getX509Certificate(OMElement securityHeader) throws STSException {
        X509Certificate cert = null;

        try {
            OMElement node = XPathHelper.selectSingleNode(
                    securityHeader,
                    "./ns:BinarySecurityToken[1]",
                    STSConstants.WSSECURITY_NS);
            if (node != null) {
                String base64Text = node.getText();
                cert = STSUtil.getCertificate(base64Text);
            }
        } catch (XPathHelperException ex) {
            // Do nothing - will be validated later.
        }
        return cert;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("SOAPAction [").append(this.soapAction).append("], timestampCreated [").append(this.timestampCreated).append("], timestampExpires [").append(this.timestampExpires).append("], userName [").append(this.userName) //.append("], userPassword [")
                //.append(this.userPassword)
                .append("]");
        return buf.toString();
    }
}
