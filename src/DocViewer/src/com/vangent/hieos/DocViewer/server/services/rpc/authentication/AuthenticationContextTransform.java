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

import com.vangent.hieos.DocViewer.client.model.authentication.AuthenticationContextDTO;
import com.vangent.hieos.DocViewer.client.model.authentication.PermissionDTO;
import com.vangent.hieos.DocViewer.client.model.authentication.UserProfileDTO;
import com.vangent.hieos.DocViewer.server.framework.ServletUtilMixin;
import com.vangent.hieos.authutil.model.AuthenticationContext;
import com.vangent.hieos.authutil.model.Role;
import com.vangent.hieos.authutil.model.UserProfile;
/**
 * 
 * @author Anand Sastry
 * @author Reworked by Bernie Thuman, Adeola Odunlami
 *
 */
public class AuthenticationContextTransform {
	private AuthenticationContext authContext;
	private ServletUtilMixin servletUtil;
	
	/**
	 * 
	 * @param authContext
	 * @param servletUtil
	 */
	public AuthenticationContextTransform(AuthenticationContext authContext, ServletUtilMixin servletUtil)
	{
		this.authContext = authContext;
		this.servletUtil = servletUtil;
	}
	
	/**
	 * 
	 * @return
	 */
	public AuthenticationContextDTO doWork() {
		AuthenticationContextDTO authCtxtDTO = new AuthenticationContextDTO();
		if (this.authContext != null) {
			setDate(authCtxtDTO);
			setUserProfile(authCtxtDTO);
			setStatus(authCtxtDTO);
		}

		return authCtxtDTO;
	}

	/**
	 * 
	 * @param authCtxtDTO
	 */
	private void setUserProfile(
			AuthenticationContextDTO authCtxtDTO) {
		if (this.authContext.getUserProfile() != null) {
			UserProfileDTO userProfileDTO = new UserProfileDTO();
			setNames(userProfileDTO);
			setPermissions(userProfileDTO);
			authCtxtDTO.setUserProfile(userProfileDTO);
		} else {
			authCtxtDTO.setUserProfile(null);
		}

	}

	/**
	 * 
	 * @param userProfileDTO
	 */
	private void setPermissions(UserProfileDTO userProfileDTO) {

		UserProfile userProfile = this.authContext.getUserProfile();
		if (userProfile.getRoles() == null) {
			userProfileDTO.setPermissions(null);
		} else {
			Iterator<Role> it = userProfile.getRoles().iterator();
			List<PermissionDTO> permissionsDTO = new ArrayList<PermissionDTO>();
			while (it.hasNext()) {
				addPermission(it.next(), permissionsDTO);
			}
			userProfileDTO.setPermissions(permissionsDTO);
		}

	}

	/**
	 * 
	 * @param role
	 * @param permissionsDTO
	 */
	private void addPermission(
			com.vangent.hieos.authutil.model.Role role,
			List<PermissionDTO> permissionsDTO) {
		
		// Get the permission mappings from xconfig			
		System.out.println("Role: " + role.getName());
		//System.out.println("Permission Permitted: " + authPermission.isPermitted());
		
		String roleName = role.getName();
		setPermissions("PermittedRoles_ViewDocs", permissionsDTO, roleName, AuthenticationContextDTO.PERMISSION_VIEWDOCS);
		setPermissions("PermittedRoles_ViewConsent", permissionsDTO, roleName, AuthenticationContextDTO.PERMISSION_VIEWCONSENT);
		setPermissions("PermittedRoles_EditConsent", permissionsDTO, roleName, AuthenticationContextDTO.PERMISSION_EDITCONSENT);
	}
	
	/**
	 * 
	 * @param propName
	 * @param permissionsDTO
	 * @param roleName
	 * @param permissionName
	 */
	private void setPermissions(String propName, List<PermissionDTO> permissionsDTO,
			String roleName, String permissionName)
	{
		String permittedRoles = servletUtil.getProperty(propName);	
		System.out.println("propName = " + propName + ", Permitted Roles: " + permittedRoles);
		StringTokenizer tokenizer = new StringTokenizer(permittedRoles, ";");
		PermissionDTO permissionDTO = new PermissionDTO();
		while (tokenizer.hasMoreElements()){
			String stRoleMapping = tokenizer.nextToken();
			System.out.println("Role Mapping: " + stRoleMapping);
			if (roleName.equals(stRoleMapping)) {
				System.out.println(".. Role Matched: " + stRoleMapping);
				permissionDTO.setName(permissionName);
				permissionDTO.setAccess(true);
				permissionsDTO.add(permissionDTO);
				break;  // No reason to continue - we matched on the given role.
			}			
		}	
	}

	/**
	 * 
	 * @param userProfileDTO
	 */
	private void setNames(UserProfileDTO userProfileDTO) {
		UserProfile userProfile = this.authContext.getUserProfile();
		userProfileDTO.setDistinguishedName(userProfile.getDistinguishedName());
		userProfileDTO.setFamilyName(userProfile.getFamilyName());
		userProfileDTO.setGivenName(userProfile.getGivenName());
		userProfileDTO.setFullName(userProfile.getFullName());
		userProfileDTO.setUserName(userProfile.getUserName());
		userProfileDTO.setOrganizationName(userProfile.getOrganization());
	}

	/**
	 * 
	 * @param authCtxtDTO
	 */
	private void setStatus(AuthenticationContextDTO authCtxtDTO) {
		if (this.authContext.hasSuccessStatus()) {
			authCtxtDTO.setSuccessFlag(true);
		} else {
			authCtxtDTO.setSuccessFlag(false);
		}
	}

	/**
	 * 
	 * @param authCtxtDTO
	 */
	private void setDate(AuthenticationContextDTO authCtxtDTO) {
		authCtxtDTO.setCreationDate(this.authContext.getCreationDate());
	}

}
