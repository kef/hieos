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
    static public final String XACML_SUBJECT_ID = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";
    static public final String XACML_RESOURCE_ID = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
    static public final String XACML_SUBJECT_CATEGORY = "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject";

    // Namespaces.
    public final static String XACML_NS = "urn:oasis:names:tc:xacml:2.0:policy:schema:os";
    public final static String XACML_CONTEXT_NS = "urn:oasis:names:tc:xacml:2.0:context:schema:os";
    public final static String XACML_SAML_NS = "urn:oasis:names:tc:xacml:2.0:saml:assertion:schema:os";
    public final static String XACML_SAML_PROTOCOL_NS = "urn:oasis:names:tc:xacml:2.0:saml:protocol:schema:os";
    public final static String HIEOS_PIP_NS = "urn:hieos:policy:pip";
    public final static String SAML2_NS = "urn:oasis:names:tc:SAML:2.0:assertion";
    public final static String SAML2_PROTOCOL_NS = "urn:oasis:names:tc:SAML:2.0:protocol";

    // Namespace prefixes.
    public final static String XACML_NS_PREFIX = "xacml";
    public final static String XACML_CONTEXT_NS_PREFIX = "xacml-context";
    public final static String XACML_SAML_NS_PREFIX = "xacml-saml";
    public final static String XACML_SAML_PROTOCOL_NS_PREFIX = "xacml-samlp";
    public final static String HIEOS_PIP_NS_PREFIX = "pip";
    public final static String SAML2_NS_PREFIX = "saml";
    public final static String SAML2_PROTOCOL_NS_PREFIX = "samlp";

    public final static String PIP_GET_CONSENT_DIRECTIVES_SOAP_ACTION = "urn:hieos:policy:pip:GetConsentDirectivesRequest";

    // Obligation names (same as policy set actions).
    public final static String PEP_OBLIGATION_EVALUATE_SAME_PID_POLICY = "evaluate-same-pid-policy";
    public final static String PEP_OBLIGATION_EVALUATE_DOCUMENT_POLICY = "evaluate-document-policy";
}
