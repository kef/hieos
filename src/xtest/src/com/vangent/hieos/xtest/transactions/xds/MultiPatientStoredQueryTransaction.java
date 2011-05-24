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
package com.vangent.hieos.xtest.transactions.xds;

import com.vangent.hieos.xtest.framework.StepContext;

import com.vangent.hieos.xtest.framework.TestConfig;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.soap.SoapActionFactory;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class MultiPatientStoredQueryTransaction extends StoredQueryTransaction {

    /**
     * 
     * @param s_ctx
     * @param instruction
     * @param instruction_output
     */
    public MultiPatientStoredQueryTransaction(StepContext s_ctx, OMElement instruction, OMElement instruction_output) {
        super(s_ctx, instruction, instruction_output);
    }

    /**
     * 
     * @throws XdsException
     */
    @Override
     public void run() throws XdsException {
        parseRegistryEndpoint(TestConfig.defaultRegistry, "RegistryStoredQuery");
        super.run();
    }
    /**
     *
     * @return
     */
    @Override
    protected String getRequestAction() {
        return SoapActionFactory.XDSB_REGISTRY_MPQ_ACTION;
    }
}
