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
package com.vangent.hieos.services.xcpd.gateway.serviceimpl;

import com.vangent.hieos.xutil.services.framework.XAbstractService;

import org.apache.log4j.Logger;

/**
 * Common base class for all XCPD Gateway web service handlers.
 *
 * @author Bernie Thuman
 */
abstract class XCPDGateway extends XAbstractService {

    private final static Logger logger = Logger.getLogger(XCPDGateway.class);

    /**
     * Returns the name of the current transaction for logging purposes.
     *
     * @param name Name of transaction.
     * @return Name to use in log.
     */
    protected String getTransactionName(String name)
    {
        String txnName = name;
        if (isAsync()) {
            txnName = name + " ASync";
        }
        return txnName;
    }
}
