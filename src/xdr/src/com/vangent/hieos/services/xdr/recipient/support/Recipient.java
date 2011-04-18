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
package com.vangent.hieos.services.xdr.recipient.support;

import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;
import com.vangent.hieos.xutil.xconfig.XConfigTransaction;

/**
 *
 * @author Adeola Odunlami
 */
public class Recipient {

    /**
     * This method returns an endpoint URL for the XDR Doc Recipient.
     * @return a String representing the endpoint URL.
     * @throws XdsInternalException
     */
    static public String getDocRecipientTransactionEndpoint() throws XdsInternalException {
        return getDocRecipientTransaction().getEndpointURL();
    }

    /**
     * This method returns whether the Doc Recipient endpoint is asynchronous.
     * @return a booolean value.
     * @throws XdsInternalException
     */
    static public boolean isDocRecipientTransactionAsync() throws XdsInternalException {
        return getDocRecipientTransaction().isAsyncTransaction();
    }

    /**
     * This method returns the Unique Id for the Doc Recipient.
     * @return a String value.
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    static public String getDocRecipientUniqueId() throws XdsInternalException {
        XConfigActor docRecipient = Recipient.getDocRecipientConfig();
        return docRecipient.getUniqueId();
    }

    /**
     * This private utility method returns a transaction configuration definition for
     * the XDR ProvideAndRegisterDocumentSet-b transaction from the DocRecipient config.
     * @return XConfigTransaction.
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    static private XConfigTransaction getDocRecipientTransaction() throws XdsInternalException {
        XConfigActor docRecipient = Recipient.getDocRecipientConfig();
        XConfigTransaction txn = docRecipient.getTransaction("ProvideAndRegisterDocumentSet-b");
        return txn;
    }

    /**
     * This private utility method returns the local XDR Recipient.
     * @return XConfigActor.
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    static private XConfigActor getDocRecipientConfig() throws XdsInternalException {
        try {
            XConfig xconf = XConfig.getInstance();
            XConfigObject homeCommunity = xconf.getHomeCommunityConfig();
            XConfigActor docRecipient = (XConfigActor)homeCommunity.getXConfigObjectWithName("docrecipient", XConfig.XDR_DOCUMENT_RECIPIENT_TYPE);
            return docRecipient;
        } catch (Exception e) {
            throw new XdsInternalException("Unable to get Doc Recepient configuration + " + e.getMessage());
        }
    }
}
