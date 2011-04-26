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

import com.vangent.hieos.DocViewer.client.model.authentication.AuthenticationContext;
import com.vangent.hieos.DocViewer.client.model.authentication.Permission;
import com.vangent.hieos.DocViewer.client.model.authentication.UserProfile;
import com.vangent.hieos.DocViewer.server.framework.ServletUtilMixin;
/**
 * 
 * @author Anand Sastry
 * @author Reworked by Bernie Thuman, Adeola Odunlami
 *
 */
public class AuthenticationContextTransform {
	private com.vangent.hieos.authutil.model.AuthenticationContext authContext;
	private ServletUtilMixin servletUtil;
	
	/**
	 * 
	 * @param authContext
	 * @param servletContext
	 */
	public AuthenticationContextTransform(com.vangent.hieos.authutil.model.AuthenticationContext authContext, ServletContext servletContext)
	{
		this.authContext = authContext;
		this.servletUtil = new ServletUtilMixin();
		servletUtil.init(servletContext);
	}
	
	/**
	 * 
	 * @return
	 */
	public AuthenticationContext doWork() {
		AuthenticationContext guiAuthCtxt = new AuthenticationContext();
		if (this.authContext != null) {
			setDate(guiAuthCtxt);
			setUserProfile(guiAuthCtxt);
			setStatus(guiAuthCtxt);
		}

		return guiAuthCtxt;
	}

	/**
	 * 
	 * @param guiAuthCtxt
	 */
	private void setUserProfile(
			AuthenticationContext guiAuthCtxt) {
		if (this.authContext.getUserProfile() != null) {
			UserProfile guiUserProfile = new UserProfile();
			setNames(guiUserProfile);
			setPermissions(guiUserProfile);
			guiAuthCtxt.setUserProfile(guiUserProfile);
		} else {
			guiAuthCtxt.setUserProfile(null);
		}

	}

	/**
	 * 
	 * @param guiUserProfile
	 */
	private void setPermissions(UserProfile guiUserProfile) {

		com.vangent.hieos.authutil.model.UserProfile userProfile = this.authContext.getUserProfile();
		if (userProfile.getRoles() == null) {
			guiUserProfile.setPermissions(null);
		} else {
			Iterator<com.vangent.hieos.authutil.model.Role> it = userProfile
					.getRoles().iterator();
			List<Permission> guiPermissions = new ArrayList<Permission>();
			while (it.hasNext()) {
				addPermission(it.next(), guiPermissions);
			}
			guiUserProfile.setPermissions(guiPermissions);
		}

	}

	/**
	 * 
	 * @param role
	 * @param guiPermissions
	 */
	private void addPermission(
			com.vangent.hieos.authutil.model.Role role,
			List<Permission> guiPermissions) {
		
		// Get the permission mappings from xconfig			
		System.out.println("Role: " + role.getName());
		//System.out.println("Permission Permitted: " + authPermission.isPermitted());
		
		String roleName = role.getName();
		setPermissions("PermittedRoles_ViewDocs", guiPermissions, roleName, AuthenticationContext.PERMISSION_VIEWDOCS);
		setPermissions("PermittedRoles_ViewConsent", guiPermissions, roleName, AuthenticationContext.PERMISSION_VIEWCONSENT);
		setPermissions("PermittedRoles_EditConsent", guiPermissions, roleName, AuthenticationContext.PERMISSION_EDITCONSENT);
	}
	
	/**
	 * 
	 * @param propName
	 * @param guiPermissions
	 * @param roleName
	 * @param permissionName
	 */
	private void setPermissions(String propName, List<Permission> guiPermissions,
			String roleName, String permissionName)
	{
		String permittedRoles = servletUtil.getProperty(propName);	
		System.out.println("propName = " + propName + ", Permitted Roles: " + permittedRoles);
		StringTokenizer tokenizer = new StringTokenizer(permittedRoles, ";");
		Permission guiPermission = new Permission();
		while (tokenizer.hasMoreElements()){
			String stRoleMapping = tokenizer.nextToken();
			System.out.println("Role Mapping: " + stRoleMapping);
			if (roleName.equals(stRoleMapping)) {
				System.out.println(".. Role Matched: " + stRoleMapping);
				guiPermission.setName(permissionName);
				guiPermission.setAccess(true);
				guiPermissions.add(guiPermission);
				break;  // No reason to continue - we matched on the given role.
			}			
		}	
	}

	/**
	 * 
	 * @param guiUserProfile
	 */
	private void setNames(UserProfile guiUserProfile) {
		com.vangent.hieos.authutil.model.UserProfile userProfile = this.authContext.getUserProfile();
		guiUserProfile.setDistinguishedName(userProfile.getDistinguishedName());
		guiUserProfile.setFamilyName(userProfile.getFamilyName());
		guiUserProfile.setGivenName(userProfile.getGivenName());
		guiUserProfile.setFullName(userProfile.getFullName());
	}

	/**
	 * 
	 * @param guiAuthCtxt
	 */
	private void setStatus(AuthenticationContext guiAuthCtxt) {
		if (this.authContext.hasSuccessStatus()) {
			guiAuthCtxt.setSuccessFlag(true);
		} else {
			guiAuthCtxt.setSuccessFlag(false);
		}
	}

	/**
	 * 
	 * @param guiAuthCtxt
	 */
	private void setDate(AuthenticationContext guiAuthCtxt) {
		guiAuthCtxt.setCreationDate(this.authContext.getCreationDate());
	}

}
