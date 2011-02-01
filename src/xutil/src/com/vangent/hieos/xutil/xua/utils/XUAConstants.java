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

package com.vangent.hieos.xutil.xua.utils;

/**
 * 
 * @author Fred Aabedi
 */
public interface XUAConstants {
    public static final String STSURL_PROPERTY = "STSValidatorURL";
    public static final String SERVICEURI_PROPERTY = "STSValidatorServiceURI";
    public static final String XUAENABLED_PROPERTY = "XUAEnabled";
    public static final String XUASOAPACTIONS_PROPERTY = "SOAPActions";

    /**
     * WS-Security namespace URL
     */
    public static final String WS_SECURITY_NS_URL = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

    /**
     * SOAPAction to get the token
     */
     //public static final String SOAP_ACTION_ISSUE_TOKEN = "IssueToken";

     public static final String SOAP_ACTION_ISSUE_TOKEN ="http://docs.oasis-open.org/ws-sx/ws-trust/200512/RST/Issue";

    /**
     * WS-Security base element name
     */
    public static final String WS_SECURITY_ELEMENT_NAME = "Security";

    /**
     * WS-Security namespace prefix
     */
    public static final String WS_SECURITY_NS_PREFIX = "wsse";

     /**
      * SOAPAction to validate the token
      */
     public static final String SOAP_ACTION_VALIDATE_TOKEN = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/RST/Validate";



     /**
     * WS-Trust Token Request body template
     */
     public static final String WS_TRUST_TOKEN_REQUEST_BODY =
        "<wst:RequestSecurityToken xmlns:wst=\"http://docs.oasis-open.org/ws-sx/ws-trust/200512\">" +
        "<wst:RequestType xmlns:wst=\"http://docs.oasis-open.org/ws-sx/ws-trust/200512\">http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue</wst:RequestType>" +
        "<wsp:AppliesTo xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">" +
        "<wsa:EndpointReference xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">"+
        "<wsa:Address>__SERVICE__</wsa:Address>"+
        "</wsa:EndpointReference>"+
        "</wsp:AppliesTo>"+
        "<wst:Claims Dialect=\"urn:ibm:names:ITFIM:saml\" xmlns:wst=\"http://schemas.xmlsoap.org/ws/2005/02/trust\">"+
        "<fimsaml:Saml20Claims xmlns:fimsaml=\"urn:ibm:names:ITFIM:saml\">"+
        "<fimsaml:ConfirmationMethod>urn:oasis:names:tc:SAML:2.0:cm:bearer</fimsaml:ConfirmationMethod>"+
        "<saml:NameID xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\" Format=\"urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress\">__USERNAME__</saml:NameID>"+
        "</fimsaml:Saml20Claims>"+
        "</wst:Claims>"+
        "</wst:RequestSecurityToken>";

     /**
      * WS-Trust Token Request header template
      */
//<wsu:Timestamp xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd" wsu:Id="Timestamp-2">
// <wsu:Created>2011-01-20T17:23:33.011Z</wsu:Created>
// <wsu:Expires>2011-01-20T17:54:33.011Z</wsu:Expires>
//</wsu:Timestamp>
     public static final String WS_TRUST_TOKEN_REQUEST_HEADER =
        "<wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">"+
        "<wsu:Timestamp xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" wsu:Id=\"Timestamp-2\">" +
        "<wsu:Created>__CREATEDTIME__</wsu:Created>" +
        "<wsu:Expires>__EXPIREDTIME__</wsu:Expires>" +
        "</wsu:Timestamp>" +
        "<wsse:UsernameToken xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">"+
        "<wsse:Username>__USERNAME__</wsse:Username>"+
        "<wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">__PASSWORD__</wsse:Password>"+
        "</wsse:UsernameToken>"+
        "</wsse:Security>";
        //+
        //"<wsa:To>__SERVICE__</wsa:To>"+
        //"<wsa:MessageID>urn:uuid:ECC30223BD5F378D231254838390566</wsa:MessageID>"+
        //"<wsa:Action>\"http://docs.oasis-open.org/ws-sx/ws-trust/200512/RST/Issue\"</wsa:Action>";

      public static final String WS_TRUST_TOKEN_VALIDATE_HEADER =
        "<wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">"+
        "<wsu:Timestamp xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" wsu:Id=\"Timestamp-2\">" +
        "<wsu:Created>__CREATEDTIME__</wsu:Created>" +
        "<wsu:Expires>__EXPIREDTIME__</wsu:Expires>" +
        "</wsu:Timestamp>" +
        "</wsse:Security>";

     /**
      * WS-Trust Token Validate Request body template
      */
     public static final String WS_TRUST_TOKEN_VALIDATE_REQUEST_BODY =
        "<wst:RequestSecurityToken xmlns:wst=\"http://docs.oasis-open.org/ws-sx/ws-trust/200512\">"+
        "<wst:TokenType>urn:oasis:names:tc:SAML:2.0:assertion</wst:TokenType>"+
        "<wst:RequestType xmlns:wst=\"http://docs.oasis-open.org/ws-sx/ws-trust/200512\">http://docs.oasis-open.org/ws-sx/ws-trust/200512/Validate</wst:RequestType>"+
        "<wst:ValidateTarget xmlns:wst=\"http://docs.oasis-open.org/ws-sx/ws-trust/200512\">__TOKEN__</wst:ValidateTarget>"+
        "</wst:RequestSecurityToken>";   
    
}
