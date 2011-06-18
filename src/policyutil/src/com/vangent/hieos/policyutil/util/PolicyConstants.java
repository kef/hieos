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
    // PDP SOAP Action (FIXME!!!)
    static public final String PDP_SOAP_ACTION = "urn:Authorize";

    static public final String XACML_ACTION_ID = "urn:oasis:names:tc:xacml:1.0:action:action-id";
    static public final String XACML_SUBJECT_CATEGORY = "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject";

    // Subject attribute identifiers
    static public final String XACML_SUBJECT_ID = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";
    static public final String XACML_SUBJECT_ORGANIZATION = "urn:oasis:names:tc:xspa:1.0:subject:organization";
    static public final String XACML_SUBJECT_ORGANIZATION_ID = "urn:oasis:names:tc:xspa:1.0:subject:organization-id";
    static public final String XACML_SUBJECT_ROLE = "urn:oasis:names:tc:xacml:2.0:subject:role";
    static public final String XACML_SUBJECT_PURPOSE_OF_USE = "urn:oasis:names:tc:xspa:1.0:subject:purposeofuse";
    static public final String XACML_SUBJECT_NPI = "urn:oasis:names:tc:xspa:2.0:subject:npi";
    static public final String XACML_SUBJECT_HL7_PERMISSION = "urn:oasis:names:tc:xspa:1.0:subject:hl7:permission";

    // Resource attribute identifiers
    static public final String XACML_RESOURCE_ID = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
    static public final String XACML_RESOURCE_HL7_TYPE = "urn:oasis:names:tc:xspa:1.0:resource:hl7:type";

    // Environment attribute identifiers
    static public final String XACML_ENVIRONMENT_LOCALITY = "urn:oasis:names:tc:xspa:1.0:environment:locality";

    // Pulled from the PIP
    static public final String XACML_RESOURCE_ALLOWED_ORGANIZATIONS = "urn:oasis:names:tc:xspa:1.0:resource:org:allowed-organizations";
    static public final String XACML_RESOURCE_BLOCKED_ORGANIZATIONS = "urn:oasis:names:tc:xspa:1.0:resource:org:blocked-organizations";
    static public final String XACML_RESOURCE_SENSITIVE_DOCUMENT_TYPES = "urn:oasis:names:tc:xspa:1.0:resource:sensitive-document-types";
}
