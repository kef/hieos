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
    private DateTime timestampCreated;
    private DateTime timestampExpires;
    private String userName;
    private String userPassword;
    private String soapAction;
    private MessageContext messageContext;

    public DateTime getTimestampCreated() {
        return timestampCreated;
    }

    public DateTime getTimestampExpires() {
        return timestampExpires;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public String getSoapAction() {
        return soapAction;
    }

    /**
     *
     * @param request
     */
    public void parse(MessageContext messageContext) throws STSException {
        this.messageContext = messageContext;
        this.soapAction = messageContext.getSoapAction();
        SOAPEnvelope env = messageContext.getEnvelope();
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
        userName = this.getUsername(securityHeader);
        userPassword = this.getUserPassword(securityHeader);

        this.validate();
    }

    /**
     *
     * @throws STSException
     */
    private void validate() throws STSException {
        if (this.timestampCreated == null || this.timestampExpires == null) {
            throw new STSException("No timestamp provided - rejecting request");
        }
        if (this.soapAction.equalsIgnoreCase(STSConstants.ISSUE_ACTION)) {
            // FIXME: Need to handle client-side certs also.
            if (userName == null || userPassword == null) {
                throw new STSException("No UserNameToken provided - rejecting request");
            }
            /* FIXME (DISABLED ONLY FOR TESTING)
            boolean isSecureTransport = messageContext.getTo().toString().indexOf("https://") != -1;
            if (!isSecureTransport)
            {
                 throw new STSException("Must use secure transport with UserNameToken - rejecting request");
            }*/
        }

        // Do some basic timestamp checking.
        if (this.timestampCreated.isAfter(this.timestampExpires))
        {
            throw new STSException("Timestamp created is > expires - rejecting request");
        }

        // Now check for message expiration.
        if (this.timestampExpires.isBeforeNow())
        {
            throw new STSException("Timestamp is expired - rejecting request");
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
            System.out.println("No Security/Timestamp/Created found");
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
            System.out.println("No Security/Timestamp/Expires found");
        }
        return time;
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
            System.out.println("No Security/UsernameToken/Username found");
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
            System.out.println("No Security/UsernameToken/Password found");
        }
        return result;
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
