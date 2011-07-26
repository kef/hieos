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

import java.util.List;
import oasis.names.tc.xacml._2_0.context.schema.os.DecisionType;
import oasis.names.tc.xacml._2_0.context.schema.os.RequestType;
import oasis.names.tc.xacml._2_0.context.schema.os.ResponseType;
import oasis.names.tc.xacml._2_0.context.schema.os.ResultType;
import oasis.names.tc.xacml._2_0.policy.schema.os.ObligationType;
import oasis.names.tc.xacml._2_0.policy.schema.os.ObligationsType;

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

    /**
     *
     * @return
     */
    public ResultType getResult() {
        List<ResultType> resultTypes = responseType.getResult();
        // Although there may be many results, we will only use the first one.
        return resultTypes.get(0);
    }

    /**
     *
     * @return
     */
    public DecisionType getDecision() {
        ResultType resultType = this.getResult();
        return resultType.getDecision();
    }

    /**
     *
     * @return
     */
    public boolean isDenyDecision() {
        // Treat all other cases (i.e. NOT_APPLICABLE, INDETERMINATE) as a DENY in this context.
        return this.getDecision() != DecisionType.PERMIT;
    }

    /**
     *
     * @return
     */
    public boolean isPermitDecision() {
        return this.getDecision() == DecisionType.PERMIT;
    }


    /**
     *
     * @return
     */
    public boolean hasObligations() {
        ResultType resultType = this.getResult();
        ObligationsType obligationsType = resultType.getObligations();
        if (obligationsType != null) {
            List<ObligationType> obligationTypes = obligationsType.getObligation();
            boolean hasObligations = obligationTypes != null && !obligationTypes.isEmpty();
            if (hasObligations == true) {
                return true;  // Get out.
            }
        }
        return false;
    }
}
