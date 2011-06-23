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
package com.vangent.hieos.services.sts.transactions;

import com.vangent.hieos.services.sts.model.STSRequestData;
import com.vangent.hieos.services.sts.exception.STSException;
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public abstract class SAML2TokenHandler {

    /**
     *
     * @param requestData
     * @return
     * @throws STSException
     */
    abstract protected OMElement handle(STSRequestData requestData) throws STSException;
}
