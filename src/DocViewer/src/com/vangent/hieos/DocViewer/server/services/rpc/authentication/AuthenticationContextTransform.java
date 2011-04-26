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
package com.vangent.hieos.DocViewer.server.services.rpc.authentication;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;

import com.smartgwt.client.util.SC;
import com.vangent.hieos.DocViewer.client.model.authentication.AuthenticationContext;
import com.vangent.hieos.DocViewer.client.model.authentication.Permission;
import com.vangent.hieos.DocViewer.client.model.authentication.UserProfile;
import com.vangent.hieos.DocViewer.client.model.config.Config;
import com.vangent.hieos.DocViewer.server.framework.ServletUtilMixin;
/**
 * 
 * @author Anand Sastry
 *
 */
public class AuthenticationContextTransform {
	/**
	 * 
	 * @param authCtxt
	 * @return
	 */
	public static AuthenticationContext doWork(
			com.vangent.hieos.authutil.model.AuthenticationContext authCtxt, 
			ServletContext servletContext) {
		AuthenticationContext guiAuthCtxt = new AuthenticationContext();
		if (authCtxt != null) {
			setDate(authCtxt, guiAuthCtxt);
			setUserProfile(authCtxt, guiAuthCtxt, servletContext);
			setStatus(authCtxt, guiAuthCtxt);
		}

		return guiAuthCtxt;
	}

	/**
	 * 
	 * @param authCtxt
	 * @param guiAuthCtxt
	 */
	private static void setUserProfile(
			com.vangent.hieos.authutil.model.AuthenticationContext authCtxt,
			AuthenticationContext guiAuthCtxt,
			ServletContext servletContext) {
		if (authCtxt.getUserProfile() != null) {
			com.vangent.hieos.authutil.model.UserProfile userProfile = authCtxt
					.getUserProfile();
			UserProfile guiUserProfile = new UserProfile();
			setNames(userProfile, guiUserProfile);
			setPermissions(userProfile, guiUserProfile, servletContext);
			guiAuthCtxt.setUserProfile(guiUserProfile);
		} else {
			guiAuthCtxt.setUserProfile(null);
		}

	}

	/**
	 * 
	 * @param userProfile
	 * @param guiUserProfile
	 */
	private static void setPermissions(
			com.vangent.hieos.authutil.model.UserProfile userProfile,
			UserProfile guiUserProfile,
			ServletContext servletContext) {
		if (userProfile.getPermissions() == null) {
			guiUserProfile.setPermissions(null);
		} else {
			Iterator<com.vangent.hieos.authutil.model.Permission> it = userProfile
					.getPermissions().iterator();
			List<Permission> guiPermissions = new ArrayList<Permission>();
			while (it.hasNext()) {
				addPermission(it.next(), guiPermissions, servletContext);
			}
			guiUserProfile.setPermissions(guiPermissions);
		}

	}

	/**
	 * 
	 * @param authPermission
	 * @param guiPermissions
	 */
	private static void addPermission(
			com.vangent.hieos.authutil.model.Permission authPermission,
			List<Permission> guiPermissions,
			ServletContext servletContext) {
		Permission guiPermission = new Permission();
		
		// Get the permission mappings from xconfig
		ServletUtilMixin servletUtil = new ServletUtilMixin();
		servletUtil.init(servletContext);
		String viewDocPermissions = servletUtil.getProperty("ViewDocs");
		String viewConsentPermissions = servletUtil.getProperty("ViewConsent");
		String editConsentPermissions = servletUtil.getProperty("EditConsent");
		
		System.out.println("ViewDocs Permission: " + viewDocPermissions);
		System.out.println("ViewConsent Permission: " + viewConsentPermissions);
		System.out.println("EditConsent Permission: " + editConsentPermissions);
		
		StringTokenizer stViewDocPermissions = new StringTokenizer(viewDocPermissions, ";");
		//StringTokenizer stViewConsentPermissions = new StringTokenizer(viewConsentPermissions, ";");
		//StringTokenizer stEditConsentPermissions = new StringTokenizer(editConsentPermissions, ";");
		
		System.out.println("Permission: " + authPermission.getPermissionName());
		System.out.println("Permission Permitted: " + authPermission.isPermitted());
		
		String permission = authPermission.getPermissionName();
		// Check View Docs Permission
		while (stViewDocPermissions.hasMoreElements()){
			String stPermMapping = stViewDocPermissions.nextToken();
			System.out.println("Permission Mapping: " + stPermMapping);
			if (permission.equals(stPermMapping)) {
				System.out.println("Permission Matched: " + stPermMapping);
				guiPermission.setName("ViewDocs");
				guiPermission.setAccess(true);
				guiPermissions.add(guiPermission);
			}			
		}
		
		// Check View Consent Permission
		/*while (stViewConsentPermissions.hasMoreElements()){
			String stPermMapping = stViewConsentPermissions.nextToken();
			System.out.println("View Consent Permission Mapping: " + stPermMapping);
			if (permission.equals(stPermMapping)) {
				System.out.println("Permission Matched: " + stPermMapping);
				guiPermission.setName("ViewConsent");
				guiPermission.setAccess(true);
				guiPermissions.add(guiPermission);
			}			
		}*/

		// Check Edit Consent Permission
	/*	while (stEditConsentPermissions.hasMoreElements()){
			String stPermMapping = stEditConsentPermissions.nextToken();
			System.out.println("Edit Consent Permission Mapping: " + stPermMapping);
			if (permission.equals(stPermMapping)) {
				System.out.println("Permission Matched: " + stPermMapping);
				guiPermission.setName("EditConsent");
				guiPermission.setAccess(true);
				guiPermissions.add(guiPermission);
			}			
		}*/

	}

	/**
	 * 
	 * @param userProfile
	 * @param guiUserProfile
	 */
	private static void setNames(
			com.vangent.hieos.authutil.model.UserProfile userProfile,
			UserProfile guiUserProfile) {
		guiUserProfile.setDistinguishedName(userProfile.getDistinguishedName());
		guiUserProfile.setFamilyName(userProfile.getFamilyName());
		guiUserProfile.setGivenName(userProfile.getGivenName());
		guiUserProfile.setFullName(userProfile.getFullName());
	}

	/**
	 * 
	 * @param authCtxt
	 * @param guiAuthCtxt
	 */
	private static void setStatus(
			com.vangent.hieos.authutil.model.AuthenticationContext authCtxt,
			AuthenticationContext guiAuthCtxt) {
		if (authCtxt.hasSuccessStatus()) {
			guiAuthCtxt.setSuccessFlag(true);
		} else {
			guiAuthCtxt.setSuccessFlag(false);
		}
	}

	/**
	 * 
	 * @param authCtxt
	 * @param guiAuthCtxt
	 */
	private static void setDate(
			com.vangent.hieos.authutil.model.AuthenticationContext authCtxt,
			AuthenticationContext guiAuthCtxt) {
		guiAuthCtxt.setCreationDate(authCtxt.getCreationDate());
	}

}
