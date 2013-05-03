/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2013 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.DocViewer.server.xua;

import java.util.HashMap;

import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

import com.vangent.hieos.DocViewer.client.model.config.Config;
import com.vangent.hieos.DocViewer.server.framework.ServletUtilMixin;
import com.vangent.hieos.DocViewer.server.gateway.InitiatingGateway;
import com.vangent.hieos.authutil.model.AuthenticationContext;
import com.vangent.hieos.authutil.model.Credentials;
import com.vangent.hieos.xutil.template.TemplateUtil;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xua.utils.XUAObject;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class XUAService {
	private final static Logger logger = Logger.getLogger(XUAService.class);
	private ServletUtilMixin servletUtil;
	private Credentials authCreds;
	private AuthenticationContext authCtxt;

	/**
	 * 
	 * @param servletUtil
	 * @param authCreds
	 * @param authCtxt
	 */
	public XUAService(ServletUtilMixin servletUtil, Credentials authCreds,
			AuthenticationContext authCtxt) {
		this.servletUtil = servletUtil;
		this.authCreds = authCreds;
		this.authCtxt = authCtxt;
	}

	/**
	 * 
	 * @param ig
	 * @param txnType
	 * @return
	 */
	public static boolean isXUAEnabled(InitiatingGateway ig,
			InitiatingGateway.TransactionType txnType) {
		boolean xuaEnabled = false;
		XConfigActor igConfig = ig.getIGConfig();
		
		if (igConfig.isXUAEnabled()) {
			XUAObject xuaObj = new XUAObject();
			xuaObj.setXUASupportedSOAPActions(igConfig
					.getProperty("XUAEnabledSOAPActions"));
			xuaEnabled = xuaObj.containsSOAPAction(ig.getSOAPAction(txnType));
		}
		return xuaEnabled;
	}

	/**
	 * 
	 * @param ig
	 * @param txnType
	 * @return
	 */
	public XUAObject getXUAObject(InitiatingGateway ig,
			InitiatingGateway.TransactionType txnType) {
		XUAObject xuaObj = new XUAObject();
		XConfigActor igConfig = ig.getIGConfig();
		xuaObj.setXUASupportedSOAPActions(igConfig.getProperty("XUAEnabledSOAPActions"));
		xuaObj.setUserName(authCreds.getUserId());
		xuaObj.setPassword(authCreds.getPassword());
		xuaObj.setXUAEnabled(true);
		xuaObj.setSTSUri("http://www.vangent.com/X-ServiceProvider-HIEOS"); // FIXME?
		XConfigActor stsConfig = this.getSTSConfig();
		String stsEndpointURL = stsConfig.getTransaction("IssueToken")
				.getEndpointURL();
		logger.info("STS endpoint URL: " + stsEndpointURL);
		xuaObj.setSTSUrl(stsEndpointURL);
		// Claims to be filled in later.
		// xuaObj.setClaims(null);

		return xuaObj;
	}

	/**
	 * 
	 * @return
	 */
	private XConfigActor getSTSConfig() {
		return servletUtil.getActorConfig("sts", XConfig.STS_TYPE);
	}

	// FIXME: Complete .. remove hard-coded values and pull from authCtxt where
	// applicable.

	/**
	 * 
	 * @param patientID
	 * @return
	 */
	public OMElement getSAMLClaims(String patientID) {
		String template = servletUtil.getTemplateString(servletUtil
				.getProperty(Config.KEY_SAML_CLAIMS_TEMPLATE));
		HashMap<String, String> replacements = new HashMap<String, String>();
		
		// FIXME: Need to complete and pull from authentication context!!!
		
		// SUBJECT_ID
		replacements.put("SUBJECT_ID", authCreds.getUserId());
		// SUBJECT_ORGANIZATION_ID
		replacements.put("SUBJECT_ORGANIZATION_ID", "^^^^^^^^^1.1.1");
		// SUBJECT_ORGANIZATION
		replacements.put("SUBJECT_ORGANIZATION", "GDIT");
		// SUBJECT_PURPOSE_OF_USE
		replacements.put("SUBJECT_PURPOSE_OF_USE", "TREATMENT");
		// SUBJECT_ROLE
		replacements.put("SUBJECT_ROLE", "DOCTOR");
		// RESOURCE_ID = Patient ID (CX formatted)
		System.out.println("SAML Claims RESOURCE_ID = "
				+ patientID);
		replacements.put("RESOURCE_ID", patientID);
		return TemplateUtil.getOMElementFromTemplate(template, replacements);
	}
}
