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
import com.vangent.hieos.authutil.model.AuthenticationContext;
import com.vangent.hieos.xutil.soap.SoapActionFactory;
import com.vangent.hieos.xutil.xconfig.XConfigActor;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class IHEInitiatingGateway extends InitiatingGateway {
	public IHEInitiatingGateway(ServletUtilMixin servletUtil) {
		super(servletUtil);
	}

	@Override
	public String getSOAPAction(TransactionType txnType) {

		if (txnType == TransactionType.DOC_QUERY) {
			return SoapActionFactory.XDSB_REGISTRY_SQ_ACTION;
		} else if (txnType == TransactionType.XCPD_QUERY) {
			//These values are not added to SOAP action factory since these are not standard IHE SOAP actions
			return "urn:RespondingGateway_PRPA_IN201305UV02";
		} else {
			return SoapActionFactory.XDSB_REPOSITORY_RET_ACTION;
		}
	}

	@Override
	public String getSOAPActionResponse(TransactionType txnType) {

		if (txnType == TransactionType.DOC_QUERY) {
			return SoapActionFactory.XDSB_REGISTRY_SQ_ACTION_RESPONSE;
		} else if (txnType == TransactionType.XCPD_QUERY) {
			//These values are not added to SOAP action factory since these are not standard IHE SOAP actions
			return "urn:gov:hhs:fha:nhinc:entitypatientdiscovery:RespondingGateway_PRPA_IN201306UV02ResponseMessage";
		} else {
			return SoapActionFactory.XDSB_REPOSITORY_RET_ACTION_RESPONSE;
		}
	}

	@Override
	public XConfigActor getIGConfig() {
		return this.getServletUtil().getActorConfig("ig",
				"InitiatingGatewayType");
	}

	@Override
	public OMElement getSOAPRequestMessage(TransactionType txnType,
			OMElement request, AuthenticationContext authContext,
			String patientId) {
		// No need to wrap request here -- just echo back request.
		return request;
	}
}
