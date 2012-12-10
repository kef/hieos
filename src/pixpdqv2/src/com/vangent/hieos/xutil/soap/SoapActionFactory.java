/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2012 Vangent, Inc.  All rights reserved.
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

/**
 *
 * @author Bernie Thuman
 */
public class SoapActionFactory {

    /**
     *
     */
    public final static String PIXPDQV2_GET_CONFIG_REQUEST = "urn:hieos:pixpdqv2:GetConfigRequest";
    /**
     *
     */
    public final static String PIXPDQV2_GET_CONFIG_RESPONSE = "urn:hieos:pixpdqv2:GetConfigRequest";
   
    /**
     *
     */
    public final static String PDS_PDQQUERY_ACTION_RESPONSE = "urn:hl7-org:v3:PRPA_IN201306UV02";
    private static final Map<String, String> actions =
            new HashMap<String, String>() {

                {
                    put(PIXPDQV2_GET_CONFIG_REQUEST, PIXPDQV2_GET_CONFIG_RESPONSE);
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
