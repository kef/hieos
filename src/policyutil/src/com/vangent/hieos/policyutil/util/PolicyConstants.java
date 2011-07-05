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
package com.vangent.hieos.policyutil.util;

/**
 *
 * @author Bernie Thuman
 */
public class PolicyConstants {
    static public final String XACML_ACTION_ID = "urn:oasis:names:tc:xacml:1.0:action:action-id";
    static public final String XACML_RESOURCE_ID = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";

    // Pulled from the PIP (FIXME: put somewhere in policyConfig.xml)
    static public final String XACML_RESOURCE_ALLOWED_ORGANIZATIONS = "urn:oasis:names:tc:xspa:1.0:resource:org:allowed-organizations";
    static public final String XACML_RESOURCE_BLOCKED_ORGANIZATIONS = "urn:oasis:names:tc:xspa:1.0:resource:org:blocked-organizations";
    static public final String XACML_RESOURCE_SENSITIVE_DOCUMENT_TYPES = "urn:oasis:names:tc:xspa:1.0:resource:sensitive-document-types";
    
    static public final String XACML_SUBJECT_CATEGORY = "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject";

    // Namespaces.
    public final static String WSTRUST_NS = "http://docs.oasis-open.org/ws-sx/ws-trust/200512";
    public final static String WSPOLICY_NS = "http://schemas.xmlsoap.org/ws/2004/09/policy";
    public final static String WSSECURITY_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    public final static String WSSECURITY_UTILITY_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
    public final static String WSADDRESSING_NS = "http://www.w3.org/2005/08/addressing";
    public final static String XML_DSIG_NS = "http://www.w3.org/2000/09/xmldsig#";

    public final static String XSPA_CLAIMS_NS = "urn:oasis:names:tc:xspa:1.0:claims";

    public final static String XACML_CONTEXT_NS = "urn:oasis:names:tc:xacml:2.0:context:schema:os";
    public final static String XACML_SAML_NS = "urn:oasis:names:tc:xacml:2.0:saml:assertion:schema:os";
    public final static String XACML_SAML_PROTOCOL_NS ="urn:oasis:names:tc:xacml:2.0:saml:protocol:schema:os";
    
    public final static String SAML2_NS = "urn:oasis:names:tc:SAML:2.0:assertion";
    public final static String SAML2_PROTOCOL_NS ="urn:oasis:names:tc:SAML:2.0:protocol";

    // Namespace prefixes.
    public final static String XACML_CONTEXT_NS_PREFIX = "xacml-context";
    public final static String XACML_SAML_NS_PREFIX = "xacml-saml";
    public final static String XACML_SAML_PROTOCOL_NS_PREFIX ="xacml-samlp";

    public final static String SAML2_NS_PREFIX = "saml";
    public final static String SAML2_PROTOCOL_NS_PREFIX ="samlp";


    // STS Request Types
    public final static String ISSUE_REQUEST_TYPE = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue";
    public final static String VALIDATE_REQUEST_TYPE = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/Validate";

    // STS Token Types
    public final static String SAML2_TOKEN_TYPE = "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0";
    public final static String STATUS_TOKEN_TYPE = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/RSTR/Status";

    // STS Status Values
    public final static String WSTRUST_TOKEN_VALID = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/status/valid";
    public final static String WSTRUST_TOKEN_INVALID = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/status/invalid";

    // Subject name format.
    public final static String SUBJECT_NAME_FORMAT = "urn:oasis:names:tc:SAML:1.1:nameid-format:X509SubjectName";

    public enum AuthenticationType {
        USER_NAME_TOKEN, X509_CERTIFICATE, NONE
    };

    // Possible STS SOAP actions.
    public final static String WSTRUST_ISSUE_ACTION = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/RST/Issue";
    public final static String WSTRUST_VALIDATE_ACTION = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/RST/Validate";

}
