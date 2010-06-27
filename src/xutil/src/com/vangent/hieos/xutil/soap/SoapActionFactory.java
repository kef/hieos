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
package com.vangent.hieos.xutil.soap;

import java.util.HashMap;
import java.util.Map;

public class SoapActionFactory {

    public final static String XDSB_REPOSITORY_PNR_ACTION = "urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-b";
    public final static String XDSB_REPOSITORY_PNR_ACTION_RESPONSE = "urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-bResponse";
    public final static String XDSB_REPOSITORY_RET_ACTION = "urn:ihe:iti:2007:RetrieveDocumentSet";
    public final static String XDSB_REPOSITORY_RET_ACTION_RESPONSE = "urn:ihe:iti:2007:RetrieveDocumentSetResponse";
    public final static String XDSB_REGISTRY_SQ_ACTION = "urn:ihe:iti:2007:RegistryStoredQuery";
    public final static String XDSB_REGISTRY_SQ_ACTION_RESPONSE = "urn:ihe:iti:2007:RegistryStoredQueryResponse";
    public final static String XDSB_REGISTRY_REGISTER_ACTION = "urn:ihe:iti:2007:RegisterDocumentSet-b";
    public final static String XDSB_REGISTRY_REGISTER_ACTION_RESPONSE = "urn:ihe:iti:2007:RegisterDocumentSet-bResponse";
    public final static String XDSB_REGISTRY_PIDFEEDADD_ACTION = "urn:hl7-org:v3:PRPA_IN201301UV02";
    public final static String XDSB_REGISTRY_PIDFEEDUPDATE_ACTION = "urn:hl7-org:v3:PRPA_IN201302UV02";
    public final static String XDSB_REGISTRY_PIDFEEDMERGE_ACTION = "urn:hl7-org:v3:PRPA_IN201304UV02";
    public final static String XDSB_REGISTRY_PIDFEEDUNMERGE_ACTION = "urn:hl7-org:v3:PRPA_IN201304UV02UNMERGE";
    public final static String XDSB_REGISTRY_PIDFEED_ACTION_RESPONSE = "urn:hl7-org:v3:MCCI_IN000002UV01";
    public final static String XDSB_REGISTRY_MPQ_ACTION = "urn:ihe:iti:2009:MultiPatientStoredQuery";
    public final static String XDSB_REGISTRY_MPQ_ACTION_RESPONSE = "urn:ihe:iti:2009:MultiPatientStoredQueryResponse";
    public final static String XCA_GATEWAY_CGQ_ACTION = "urn:ihe:iti:2007:CrossGatewayQuery";
    public final static String XCA_GATEWAY_CGQ_ACTION_RESPONSE = "urn:ihe:iti:2007:CrossGatewayQueryResponse";
    public final static String XCA_GATEWAY_CGR_ACTION = "urn:ihe:iti:2007:CrossGatewayRetrieve";
    public final static String XCA_GATEWAY_CGR_ACTION_RESPONSE = "urn:ihe:iti:2007:CrossGatewayRetrieveResponse";
    public final static String XDS_REGISTRY_PIDFEEDSIMPLE_ACTION = "urn:hieos:xds:PatientFeedRequest";
    public final static String XDS_REGISTRY_PIDFEEDSIMPLE_ACTION_RESPONSE = "urn:hieos:xds:PatientFeedResponse";
    public final static String ANON_ACTION = "urn:anonOutInOp";
    private static final Map<String, String> actions =
            new HashMap<String, String>() {

                {
                    put(XDSB_REPOSITORY_PNR_ACTION, XDSB_REPOSITORY_PNR_ACTION_RESPONSE);
                    put(XDSB_REPOSITORY_RET_ACTION, XDSB_REPOSITORY_RET_ACTION_RESPONSE);
                    put(XDSB_REGISTRY_SQ_ACTION, XDSB_REGISTRY_SQ_ACTION_RESPONSE);
                    put(XDSB_REGISTRY_REGISTER_ACTION, XDSB_REGISTRY_REGISTER_ACTION_RESPONSE);
                    put(XDSB_REGISTRY_PIDFEEDADD_ACTION, XDSB_REGISTRY_PIDFEED_ACTION_RESPONSE);
                    put(XDSB_REGISTRY_PIDFEEDUPDATE_ACTION, XDSB_REGISTRY_PIDFEED_ACTION_RESPONSE);
                    put(XDSB_REGISTRY_PIDFEEDMERGE_ACTION, XDSB_REGISTRY_PIDFEED_ACTION_RESPONSE);
                    put(XDSB_REGISTRY_PIDFEEDUNMERGE_ACTION, XDSB_REGISTRY_PIDFEED_ACTION_RESPONSE);
                    put(XDSB_REGISTRY_MPQ_ACTION, XDSB_REGISTRY_MPQ_ACTION_RESPONSE);
                    put(XCA_GATEWAY_CGQ_ACTION, XCA_GATEWAY_CGQ_ACTION_RESPONSE);
                    put(XCA_GATEWAY_CGR_ACTION, XCA_GATEWAY_CGR_ACTION_RESPONSE);
                    put(XDS_REGISTRY_PIDFEEDSIMPLE_ACTION, XDS_REGISTRY_PIDFEEDSIMPLE_ACTION_RESPONSE);
                }
            };

    /**
     * Return the SOAP "response" action given a SOAP "request" action.
     *
     * @param requestAction The SOAP request action.
     * @return The SOAP response action.
     */
    static public String getResponseAction(String requestAction) {
        if (requestAction == null) {
            return null;
        }
        return actions.get(requestAction);
    }
}
