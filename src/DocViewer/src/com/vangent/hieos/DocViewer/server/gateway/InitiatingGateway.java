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
package com.vangent.hieos.DocViewer.server.gateway;

import org.apache.axiom.om.OMElement;

import com.vangent.hieos.DocViewer.server.framework.ServletUtilMixin;
import com.vangent.hieos.xutil.exception.SOAPFaultException;
import com.vangent.hieos.xutil.soap.Soap;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigTransaction;
import com.vangent.hieos.xutil.xua.utils.XUAObject;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public abstract class InitiatingGateway {
	
	private ServletUtilMixin servletUtil;
	private XUAObject xuaObj = null;

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
	 * @return
	 */
	public XUAObject getXuaObject() {
		return xuaObj;
	}

	/**
	 * 
	 * @param xuaObj
	 */
	public void setXuaObject(XUAObject xuaObj) {
		this.xuaObj = xuaObj;
	}

	/**
	 * 
	 * @param txnType
	 * @param request
	 * @return
	 * @throws XdsException
	 */
	public OMElement soapCall(TransactionType txnType, OMElement request)
			throws SOAPFaultException {
		// Wrapper the message (if necessary) ...
		OMElement outboundRequest = this
				.getSOAPRequestMessage(txnType, request);
		String endpointURL = this.getTransactionEndpointURL(txnType);

		// Issue Document Retrieve ...
		System.out.println("XCA Request ... endpoint = " + endpointURL
				+ ", action=" + this.getSOAPAction(txnType));
		Soap soap = new Soap();
		soap.setXUAObject(this.xuaObj);
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
	public boolean isMTOM(TransactionType txnType) {
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
	public String getTransactionEndpointURL(TransactionType txnType) {
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
	abstract public String getSOAPAction(TransactionType txnType);

	abstract public String getSOAPActionResponse(TransactionType txnType);

	abstract public OMElement getSOAPRequestMessage(TransactionType txnType,
			OMElement request);

	abstract public XConfigActor getIGConfig();
}
