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

/**
 *
 * @author Bernie Thuman
 */
public class STSConstants {
    // Possible SOAP actions.
    public final static String ISSUE_ACTION = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/RST/Issue";
    public final static String VALIDATE_ACTION = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/RST/Validate";

    // Namespaces.
    public final static String WSTRUST_NS = "http://docs.oasis-open.org/ws-sx/ws-trust/200512";
    public final static String XSPA_NS = "urn:oasis:names:tc:xspa:1.0:claims";
    public final static String WSPOLICY_NS = "http://schemas.xmlsoap.org/ws/2004/09/policy";
    public final static String WSSECURITY_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    public final static String WSSECURITY_UTILITY_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
    public final static String WSADDRESSING_NS = "http://www.w3.org/2005/08/addressing";
    public final static String XSPA_CLAIMS_NS = "urn:oasis:names:tc:xspa:1.0:claims";
    public final static String SAML2_NS = "urn:oasis:names:tc:SAML:2.0:assertion";

    // Request Types
    public final static String ISSUE_REQUEST_TYPE = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue";
    public final static String VALIDATE_REQUEST_TYPE = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/Validate";

    // Token Types
    public final static String SAML2_TOKEN_TYPE = "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0";
    public final static String STATUS_TOKEN_TYPE = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/RSTR/Status";
    // Status Values
    public final static String TOKEN_VALID = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/status/valid";
    public final static String TOKEN_INVALID = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/status/invalid";
}
