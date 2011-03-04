/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2010 Vangent, Inc.  All rights reserved.
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

    public final static String PIXPDQ_XREFMGR_PIXV3QUERY_ACTION = "urn:hl7-org:v3:PRPA_IN201309UV02";
    public final static String PIXPDQ_XREFMGR_PIXV3QUERY_ACTION_RESPONSE = "urn:hl7-org:v3:PRPA_IN201310UV02";
    public final static String PIXPDQ_XREFMGR_PIXV3PIDFEED_ACTION = "urn:hl7-org:v3:PRPA_IN201301UV02";
    public final static String PIXPDQ_XREFMGR_PIXV3PIDFEED_ACTION_RESPONSE = "urn:hl7-org:v3:MCCI_IN000002UV01";
    public final static String PIXPDQ_PDS_PDQQUERY_ACTION = "urn:hl7-org:v3:PRPA_IN201305UV02";
    public final static String PIXPDQ_PDS_PDQQUERY_ACTION_RESPONSE = "urn:hl7-org:v3:PRPA_IN201306UV02";
    private static final Map<String, String> actions =
            new HashMap<String, String>() {

                {
                    put(PIXPDQ_XREFMGR_PIXV3QUERY_ACTION, PIXPDQ_XREFMGR_PIXV3QUERY_ACTION_RESPONSE);
                    put(PIXPDQ_XREFMGR_PIXV3PIDFEED_ACTION, PIXPDQ_XREFMGR_PIXV3PIDFEED_ACTION_RESPONSE);
                    put(PIXPDQ_PDS_PDQQUERY_ACTION, PIXPDQ_PDS_PDQQUERY_ACTION_RESPONSE);
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
