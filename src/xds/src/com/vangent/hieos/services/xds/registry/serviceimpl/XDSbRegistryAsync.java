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
package com.vangent.hieos.services.xds.registry.serviceimpl;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;

/**
 * XDSbRegistryAsync is a simple class that overrides a few methods in
 * its superclass, to provide certain asynchronous behaviors.
 *
 * @author Anand Sastry
 */
public class XDSbRegistryAsync extends XDSbRegistry {

    /**
     * This method ensures that an asynchronous request has been sent. It evaluates the message
     * context to dtermine if "ReplyTo" is non-null and is not anonymous. It also ensures that
     * "MessageID" is non-null. It throws an exception if that is not the case.
     * @throws XdsWSException
     */
    @Override
    protected void validateWS() throws AxisFault {
        validateAsyncWS();
    }

    /**
     * This method returns the transaction names supported by the Async Registry.
     * @return a String value representing a transaction name.
     */
    @Override
    protected String getRTransactionName(OMElement ahqr) {
        return super.getRTransactionName(ahqr) + " ASync";
    }
}
