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
package com.vangent.hieos.DocViewer.server;

import org.apache.axiom.om.OMElement;

import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.soap.Soap;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigTransaction;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public abstract class InitiatingGateway {
	private ServletUtilMixin servletUtil;

	public enum TransactionType {
		DOC_QUERY, DOC_RETRIEVE
	};

	/**
	 * 
	 * @param servletUtil
	 */
	public InitiatingGateway(ServletUtilMixin servletUtil) {
		this.servletUtil = servletUtil;
	}

	/**
	 * 
	 * @return
	 */
	public ServletUtilMixin getServletUtil() {
		return servletUtil;
	}

	/**
	 * 
	 * @param txnType
	 * @param request
	 * @return
	 * @throws XdsException
	 */
	public OMElement soapCall(TransactionType txnType, OMElement request)
			throws XdsException {
		// Wrapper the message (if necessary) ...
		OMElement outboundRequest = this
				.getSOAPRequestMessage(txnType, request);
		String endpointURL = this.getTransactionEndpointURL(txnType);

		// Issue Document Retrieve ...
		System.out.println("XCA Request ... endpoint = " + endpointURL
				+ ", action=" + this.getSOAPAction(txnType));
		Soap soap = new Soap();
		OMElement response = soap.soapCall(outboundRequest, endpointURL,
				this.isMTOM(txnType), true /* addressing */,
				true /* SOAP1.2 */, this.getSOAPAction(txnType),
				this.getSOAPActionResponse(txnType));
		System.out.println("XCA Request ... complete!");
		return response;
	}

	/**
	 * 
	 * @param txnType
	 * @return
	 */
	private boolean isMTOM(TransactionType txnType) {
		if (txnType == TransactionType.DOC_RETRIEVE) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param txn
	 * @return
	 */
	private String getTransactionEndpointURL(TransactionType txnType) {
		String txn = "";
		if (txnType == TransactionType.DOC_QUERY) {
			txn = "RegistryStoredQuery";
		} else {
			txn = "RetrieveDocumentSet";
		}
		XConfigActor igConfig = this.getIGConfig();
		XConfigTransaction txnConfig = igConfig.getTransaction(txn);
		return txnConfig.getEndpointURL();
	}

	// Methods that must be implemented by subclasses.
	abstract protected String getSOAPAction(TransactionType txnType);

	abstract protected String getSOAPActionResponse(TransactionType txnType);

	abstract protected OMElement getSOAPRequestMessage(TransactionType txnType,
			OMElement request);

	abstract protected XConfigActor getIGConfig();
}
