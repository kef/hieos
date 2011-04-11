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
package com.vangent.hieos.services.xds.repository.support;

// NOTE: BHT (FIXME) - eventually, we will need to deal with multiple repositories in the same instance.
// This code was based on original NIST code full of static methods.
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;
import com.vangent.hieos.xutil.xconfig.XConfigTransaction;

public class Repository {

    /**
     * This method returns an endpoint URL for the local registry.
     * @return a String representing the endpoint URL.
     * @throws XdsInternalException
     */
    static public String getRegisterTransactionEndpoint() throws XdsInternalException {
        return getRegisterTransaction().getEndpointURL();
    }

    /**
     * This method returns whether the local Registry endpoint is asynchronous.
     * @return a booolean value.
     * @throws XdsInternalException
     */
    static public boolean isRegisterTransactionAsync() throws XdsInternalException {
        return getRegisterTransaction().isAsyncTransaction();
    }

    /**
     * Returns true if SOAP 1.2 should be used, otherwise SOAP 1.1 for the
     * RegisterDocumentSet-b transaction.
     * 
     * @return true if SOAP 1.2 should be used, otherwise SOAP1.1
     */
    static public boolean isRegisterTransactionSOAP12() throws XdsInternalException {
        return getRegisterTransaction().isSOAP12Endpoint();
    }

    /**
     * This method returns the Unique Id for the local repository.
     * @return a String value.
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    static public String getRepositoryUniqueId() throws XdsInternalException {
        XConfigActor repository = Repository.getRepositoryConfig();
        return repository.getUniqueId();
    }

    /**
     * This private utility method returns the local repository.
     * @return XConfigActor.
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    static private XConfigActor getRepositoryConfig() throws XdsInternalException {
        try {
            XConfig xconf = XConfig.getInstance();
            XConfigObject homeCommunity = xconf.getHomeCommunityConfig();
            XConfigActor repository = (XConfigActor) homeCommunity.getXConfigObjectWithName("repo", XConfig.XDSB_DOCUMENT_REPOSITORY_TYPE);
            return repository;
        } catch (Exception e) {
            throw new XdsInternalException("Unable to get Repository configuration + " + e.getMessage());
        }
    }

    /**
     * This private utility method returns a transaction configuration definition for
     * the "RegisterDocumentSet-b" transaction from the local registry.
     * @return XConfigTransaction.
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    static private XConfigTransaction getRegisterTransaction() throws XdsInternalException {
        XConfigActor repo = Repository.getRepositoryConfig();
        XConfigActor localRegistry = (XConfigActor) repo.getXConfigObjectWithName("registry", XConfig.XDSB_DOCUMENT_REGISTRY_TYPE);
        XConfigTransaction txn = localRegistry.getTransaction("RegisterDocumentSet-b");
        return txn;
    }
}
