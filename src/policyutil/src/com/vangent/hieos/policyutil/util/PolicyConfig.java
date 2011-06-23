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
public class PolicyConfig {
    // TBD: Pull from configuration file [allow for coded attributes].

    // FIXME: Need to make more complex ... need an AttributeType with enums, etc.
    static private PolicyConfig _instance = null;
    public enum IdType {
        SUBJECT_ID, RESOURCE_ID, ENVIRONMENT_ID
    };
    static private final String[] XSPA_SUBJECT_IDS = {
        PolicyConstants.XACML_SUBJECT_ID,
        PolicyConstants.XACML_SUBJECT_ORGANIZATION,
        PolicyConstants.XACML_SUBJECT_ORGANIZATION_ID,
        PolicyConstants.XACML_SUBJECT_HL7_PERMISSION,
        PolicyConstants.XACML_SUBJECT_ROLE,
        PolicyConstants.XACML_SUBJECT_PURPOSE_OF_USE,
        PolicyConstants.XACML_SUBJECT_NPI};
    static private final String[] XSPA_RESOURCE_IDS = {
        PolicyConstants.XACML_RESOURCE_ID,
        PolicyConstants.XACML_RESOURCE_HL7_TYPE};
    static private final String[] XSPA_ENVIRONMENT_IDS = {
        PolicyConstants.XACML_ENVIRONMENT_LOCALITY};
    static private final String[] XSPA_REQUIRED_CLAIM_IDS = {
        PolicyConstants.XACML_SUBJECT_ID,
        PolicyConstants.XACML_SUBJECT_ORGANIZATION,
        PolicyConstants.XACML_SUBJECT_ORGANIZATION_ID,
        PolicyConstants.XACML_SUBJECT_ROLE,
        PolicyConstants.XACML_SUBJECT_PURPOSE_OF_USE,
        PolicyConstants.XACML_RESOURCE_ID
    };
    // FUTURE(???):
    private final static String[] XACML_CODED_IDS = {
        "urn:oasis:names:tc:xacml:2.0:subject:role",
        "urn:oasis:names:tc:xspa:1.0:subject:purposeofuse"
    };
    // FUTURE (???):
    private final static String[] CODED_XML_NAMES = {
        "Role",
        "PurposeForUse"
    };
    // FUTURE (???):
    private final static String[] NHIN_IDS = {
        "urn:oasis:names:tc:xspa:1.0:subject:subject-id", // Differs from XSPA.
        "urn:oasis:names:tc:xspa:1.0:subject:organization",
        "urn:oasis:names:tc:xspa:1.0:subject:organization-id",
        "urn:nhin:names:saml:homeCommunityId", // Not in XSPA.
        "urn:oasis:names:tc:xacml:2.0:subject:role", // Coded value.
        "urn:oasis:names:tc:xspa:1.0:subject:purposeofuse", // Coded value (node is PurposeForUse).
        "urn:oasis:names:tc:xacml:2.0:resource:resource-id", // Differs from XSPA.
        "urn:oasis:names:tc:xspa:2.0:subject:npi"
    };

    /**
     *
     */
    private PolicyConfig() {
        // Do not allow.
    }

    /**
     *
     * @return
     */
    static public synchronized PolicyConfig getInstance() {
        if (_instance == null) {
            _instance = new PolicyConfig();
        }
        return _instance;
    }

    /**
     *
     * @return
     */
    public String[] getSubjectIds() {
        return PolicyConfig.XSPA_SUBJECT_IDS;
    }

    /**
     *
     * @return
     */
    public String[] getResourceIds() {
        return PolicyConfig.XSPA_RESOURCE_IDS;
    }

    /**
     *
     * @return
     */
    public String[] getEnvironmentIds() {
        return PolicyConfig.XSPA_ENVIRONMENT_IDS;
    }

    /**
     *
     * @return
     */
    public String[] getRequiredClaimIds() {
        return PolicyConfig.XSPA_REQUIRED_CLAIM_IDS;
    }

    /**
     *
     * @param id
     * @return
     */
    public IdType getIdType(String id) {
        // FIXME: Rewrite (it is a small list however, likely OK).
        boolean found = this.containsId(this.getSubjectIds(), id);
        if (found) {
            return IdType.SUBJECT_ID;
        }
        found = this.containsId(this.getResourceIds(), id);
        if (found) {
            return IdType.RESOURCE_ID;
        }
        // Default.
        return IdType.ENVIRONMENT_ID;
    }

    /**
     *
     * @param ids
     * @param id
     * @return
     */
    private boolean containsId(String[] ids, String id) {
        for (int i = 0; i < ids.length; i++) {
            if (ids[i].equals(id)) {
                return true;
            }
        }
        return false;
    }
}
