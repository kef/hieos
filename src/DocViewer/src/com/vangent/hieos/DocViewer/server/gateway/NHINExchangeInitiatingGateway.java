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

import java.util.HashMap;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;

import com.vangent.hieos.xutil.template.TemplateUtil;
import com.vangent.hieos.xutil.xconfig.XConfigActor;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class NHINExchangeInitiatingGateway extends InitiatingGateway {

	// FIXME: Specific to NHIN CONNECT ...
	static final String PROP_NHINC_ASSERTION_TEMPLATE = "NHINCAssertionTemplate";

	/**
	 * 
	 * @param servletUtil
	 */
	public NHINExchangeInitiatingGateway(ServletUtilMixin servletUtil) {
		super(servletUtil);
	}

	@Override
	public OMElement getSOAPRequestMessage(TransactionType txnType,
			OMElement request) {
		// Need to wrapper request ...
		OMElement messageWrapper = this.getMessageWrapper(txnType);

		// First get Assertion object.
		// FIXME: Will need to ultimately replace with real values.
		OMElement assertion = this.getNHINCAssertionDocument();

		// Now add the parts.
		messageWrapper.addChild(request);
		messageWrapper.addChild(assertion);
		return messageWrapper;
	}

	/**
	 * 
	 * @param txnType
	 * @return
	 */
	private OMElement getMessageWrapper(TransactionType txnType) {
		OMFactory omfactory = OMAbstractFactory.getOMFactory();
		OMNamespace ns = omfactory.createOMNamespace(
				"urn:gov:hhs:fha:nhinc:common:nhinccommonentity",
				"nhinc_entity");
		String wrapperElementName;
		if (txnType == TransactionType.DOC_QUERY) {
			wrapperElementName = "RespondingGateway_CrossGatewayQueryRequest";
		} else {
			wrapperElementName = "RespondingGateway_CrossGatewayRetrieveRequest";
		}
		OMElement wrapperNode = omfactory.createOMElement(wrapperElementName, ns);
		return wrapperNode;
	}

	/**
	 * 
	 * @return
	 */
	private OMElement getNHINCAssertionDocument() {
		// FIXME: Will need to ultimately replace with real values.
		ServletUtilMixin servletUtil = this.getServletUtil();
		String template = servletUtil
				.getTemplateString(servletUtil
						.getProperty(NHINExchangeInitiatingGateway.PROP_NHINC_ASSERTION_TEMPLATE));
		HashMap<String, String> replacements = new HashMap<String, String>();
		/*
		 * replacements.put("HOME_COMMUNITY_ID", homeCommunityID);
		 * replacements.put("REPOSITORY_UNIQUE_ID", repositoryID);
		 * replacements.put("DOCUMENT_UNIQUE_ID", documentID);
		 */
		return TemplateUtil.getOMElementFromTemplate(template, replacements);
	}

	@Override
	public String getSOAPAction(TransactionType txnType) {
		if (txnType == TransactionType.DOC_QUERY) {
			return "urn:gov:hhs:fha:nhinc:entitydocquery:RespondingGateway_CrossGatewayQueryRequest";
		} else {
			return "urn:gov:hhs:fha:nhinc:entitydocretrieve:RespondingGateway_CrossGatewayRetrieveRequestMessage";
		}
	}

	@Override
	public String getSOAPActionResponse(TransactionType txnType) {
		if (txnType == TransactionType.DOC_QUERY) {
			return "urn:gov:hhs:fha:nhinc:entitydocquery:RespondingGateway_CrossGatewayQueryResponse";
		} else {
			return "urn:gov:hhs:fha:nhinc:entitydocretrieve:RespondingGateway_CrossGatewayRetrieveResponseMessage";
		}
	}

	@Override
	protected XConfigActor getIGConfig() {
		return this.getServletUtil().getActorConfig("nhinig",
				"NHINExchangeInitiatingGatewayType");
	}
}
