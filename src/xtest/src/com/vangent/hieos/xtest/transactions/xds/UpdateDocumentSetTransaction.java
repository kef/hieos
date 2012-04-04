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
package com.vangent.hieos.xtest.transactions.xds;

import com.vangent.hieos.xtest.framework.BasicTransaction;
import com.vangent.hieos.xtest.framework.StepContext;
import com.vangent.hieos.xutil.soap.SoapActionFactory;

import org.apache.axiom.om.OMElement;

/**
 *
 * @author thumbe
 */
public class UpdateDocumentSetTransaction extends RegisterTransaction {

    /**
     *
     * @param s_ctx
     * @param instruction
     * @param instruction_output
     */
    public UpdateDocumentSetTransaction(StepContext s_ctx, OMElement instruction, OMElement instruction_output) {
        super(s_ctx, instruction, instruction_output);
    }

    /**
     *
     * @return
     */
    @Override
    protected String getTransactionName() {
        return "UpdateDocumentSet";
    }

    /**
     *
     * @return
     */
    @Override
    protected String getRequestAction() {
        if (xds_version == BasicTransaction.xds_b) {
            return SoapActionFactory.XDSB_REGISTRY_UPDATE_ACTION;
        } else {
            return SoapActionFactory.ANON_ACTION;
        }
    }
}
