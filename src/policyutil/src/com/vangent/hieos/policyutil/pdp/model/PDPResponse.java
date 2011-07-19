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
package com.vangent.hieos.policyutil.pdp.model;

import oasis.names.tc.xacml._2_0.context.schema.os.RequestType;
import oasis.names.tc.xacml._2_0.context.schema.os.ResponseType;

/**
 *
 * @author Bernie Thuman
 */
public class PDPResponse {

    private RequestType requestType;
    private ResponseType responseType;

    /**
     *
     * @return
     */
    public RequestType getRequestType() {
        return requestType;
    }

    /**
     * 
     * @param requestType
     */
    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    /**
     *
     * @return
     */
    public ResponseType getResponseType() {
        return responseType;
    }

    /**
     *
     * @param responseType
     */
    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }
}
