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

import java.util.HashMap;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;

import com.vangent.hieos.DocViewer.server.framework.ServletUtilMixin;
import com.vangent.hieos.authutil.model.AuthenticationContext;
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
			OMElement request, AuthenticationContext authCtxt, String patientId) {
		OMElement messageWrapper = null;
		// Need to wrapper request ...
		//System.out.println("Transaction Type :     "+txnType.toString());
		if (txnType != TransactionType.XCPD_QUERY)
			messageWrapper = this.getMessageWrapper(txnType); // This is
																// specific to
																// DocQuery &
																// DocRetrieve
		else{
			//System.out.println("*************************** before calling getXCPDMessageWrapper*********************************");
			messageWrapper = this.getXCPDMessageWrapper(); // This is specific
		}
															// to XCPD message

		// First get Assertion object.
		// FIXME: Will need to ultimately replace with real values.
		OMElement assertion = this.getNHINCAssertionDocument(authCtxt,
				patientId,txnType);

		// Now add the parts.
		messageWrapper.addChild(request);
		messageWrapper.addChild(assertion);

		//System.out.println("*********************** After completing the request creation ********************");
		//System.out.println(messageWrapper.toString());
		//System.out.println("*********************************************************");
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
		OMElement wrapperNode = omfactory.createOMElement(wrapperElementName,
				ns);
		return wrapperNode;
	}

	/**
	 * 
	 * @param txnType
	 * @return
	 */
	private OMElement getXCPDMessageWrapper() {
		//System.out.println("***************************getXCPDMessageWrapper*********************************");
		OMFactory omfactory = OMAbstractFactory.getOMFactory();
		OMNamespace ns = omfactory.createOMNamespace(
				"urn:gov:hhs:fha:nhinc:common:nhinccommon", "nhinc_entity");
				

		String wrapperElementName = "RespondingGateway_PRPA_IN201305UV02Request";

		OMElement wrapperNode = omfactory.createOMElement(wrapperElementName,
				ns);
		wrapperNode.addAttribute("ns4",
				"urn:gov:hhs:fha:nhinc:common:patientcorrelationfacade", ns);
		wrapperNode.addAttribute("ns3", "urn:hl7-org:v3", ns);
		wrapperNode.addAttribute("ns2",
				"http://schemas.xmlsoap.org/ws/2004/08/addressing", ns);
		return wrapperNode;
	}

	/**
	 * 
	 * @return
	 */
	private OMElement getNHINCAssertionDocument(AuthenticationContext authCtxt,
			String patientId,TransactionType txnType) {
		// FIXME: Will need to ultimately replace with real values.
		ServletUtilMixin servletUtil = this.getServletUtil();
		HashMap<String, String> replacements = new HashMap<String, String>();
		String 	template = servletUtil
				.getTemplateString(servletUtil
						.getProperty(NHINExchangeInitiatingGateway.PROP_NHINC_ASSERTION_TEMPLATE));
		if (txnType == TransactionType.XCPD_QUERY)
			replacements.put("NAMESPACE","urn:hl7-org:v3");
		else 
			replacements.put("NAMESPACE",  "urn:gov:hhs:fha:nhinc:common:nhinccommonentity");
			
		
	//	System.out.println("******" + "TEMPLATE    "+ template);
		/*
		 * replacements.put("HOME_COMMUNITY_ID", homeCommunityID);
		 * replacements.put("REPOSITORY_UNIQUE_ID", repositoryID);
		 * replacements.put("DOCUMENT_UNIQUE_ID", documentID);
		 */
		/*
		replacements.put("SECOND_WITNESS_SIGNATURE", "false");
		replacements.put("HAVE_SIGNATURE", "false");
		replacements.put("HAVE_WITNESS_SIGNATURE", "false");
		*/
		replacements.put("HOME_COMMUNITY_ID", this.getIGConfig().getUniqueId()); // This
																					// is
																					// the
																					// home
																					// community
																					// as
																					// described
																					// in
																					// xconfig
																					// for
																					// this
																					// gateway
		//System.out.println("******" + "HOME_COMMUNITY_ID    "
		//		+ getIGConfig().getUniqueId());
		replacements.put("UNIQUE_PATIENT_ID", patientId);
		//System.out.println("******" + "UNIQUE_PATIENT_ID    " + patientId);

		String familyName = authCtxt.getUserProfile().getFamilyName();
		replacements.put("FAMILY_NAME", (familyName != null) ? familyName : "");
		//System.out.println("FAMILY_NAME  " + familyName);

		String givenName = authCtxt.getUserProfile().getGivenName();
		replacements.put("GIVEN_NAME", (givenName != null) ? givenName : "");
		//System.out.println("GIVEN_NAME   " + givenName);

		String initials = authCtxt.getUserProfile().getInitials();
		replacements.put("SECOND_NAME", (initials != null) ? initials : "");
		//System.out.println("SECOND_NAME  " + initials);

		String userName = authCtxt.getUserProfile().getUserName();
		replacements.put("USER_NAME", (userName != null) ? userName : "");
		//System.out.println("USER_NAME  "
		//		+ authCtxt.getUserProfile().getUserName());

		String userOrganization = authCtxt.getUserProfile().getOrganization();
		replacements.put("USER_ORGANIZATION",
				(userOrganization != null) ? userOrganization : "");
		//System.out.println("USER_ORGANIZATION  " + userOrganization);
		

		//System.out.println("******************** Assertions Added ***********************");
		//System.out.println(TemplateUtil.getOMElementFromTemplate(template,
		//		replacements).toString());
		//System.out.println("*******************************************");

		return TemplateUtil.getOMElementFromTemplate(template, replacements);
	}

	@Override
	public String getSOAPAction(TransactionType txnType) {
		if (txnType == TransactionType.DOC_QUERY) {
			//return "urn:gov:hhs:fha:nhinc:entitydocquery:RespondingGateway_CrossGatewayQueryRequest";
			return "urn:RespondingGateway_CrossGatewayQuery";//got from onboarding test with CONNECT 3.3 using SOAPUI
			
			        
		} else if (txnType == TransactionType.XCPD_QUERY) {
			//return "urn:hl7-org:v3:PRPA_IN201305UV02:CrossGatewayPatientDiscovery";
			return "urn:RespondingGateway_PRPA_IN201305UV02";//got from onboarding test with CONNECT 3.3 using SOAPUI
		}
		{
			//return "urn:gov:hhs:fha:nhinc:entitydocretrieve:RespondingGateway_CrossGatewayRetrieveRequestMessage";
			return "urn:RespondingGateway_CrossGatewayRetrieve";
			        
		}
	}

	@Override
	public String getSOAPActionResponse(TransactionType txnType) {
		if (txnType == TransactionType.DOC_QUERY) {
			return "urn:gov:hhs:fha:nhinc:entitydocquery:RespondingGateway_CrossGatewayQueryResponse";			        
					
		} else if (txnType == TransactionType.XCPD_QUERY) {
			//return "urn:hl7-org:v3:PRPA_IN201306UV02:CrossGatewayPatientDiscovery";
			return "urn:gov:hhs:fha:nhinc:entitypatientdiscovery:RespondingGateway_PRPA_IN201306UV02ResponseMessage";
			//got from onboarding test with CONNECT 3.3  using SOAPUI
		}
		{
			return "urn:gov:hhs:fha:nhinc:entitydocretrieve:RespondingGateway_CrossGatewayRetrieveResponseMessage";
		}
	}

	@Override
	public XConfigActor getIGConfig() {
		return this.getServletUtil().getActorConfig("nhinig",
				"NHINExchangeInitiatingGatewayType");
	}
	
	
	
}
