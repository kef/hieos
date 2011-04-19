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

import com.vangent.hieos.DocViewer.client.model.authentication.AuthenticationContext;
import com.vangent.hieos.DocViewer.client.model.authentication.Permission;
import com.vangent.hieos.DocViewer.client.model.authentication.UserProfile;
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
			com.vangent.hieos.authutil.model.AuthenticationContext authCtxt) {
		AuthenticationContext guiAuthCtxt = new AuthenticationContext();
		if (authCtxt != null) {
			setDate(authCtxt, guiAuthCtxt);
			setStatus(authCtxt, guiAuthCtxt);
			setUserProfile(authCtxt, guiAuthCtxt);
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
			AuthenticationContext guiAuthCtxt) {
		if (authCtxt.getUserProfile() != null) {
			com.vangent.hieos.authutil.model.UserProfile userProfile = authCtxt
					.getUserProfile();
			UserProfile guiUserProfile = new UserProfile();
			setNames(userProfile, guiUserProfile);
			setPermissions(userProfile, guiUserProfile);
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
			UserProfile guiUserProfile) {
		if (userProfile.getPermissions() == null) {
			guiUserProfile.setPermissions(null);
		} else {
			Iterator<com.vangent.hieos.authutil.model.Permission> it = userProfile
					.getPermissions().iterator();
			List<Permission> guiPermissions = new ArrayList<Permission>();
			while (it.hasNext()) {
				addPermission(it.next(), guiPermissions);
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
			List<Permission> guiPermissions) {
		Permission guiPermission = new Permission();

		guiPermission.setName(authPermission.getPermissionName());

		if (authPermission.isPermitted()) {
			guiPermission.setAccess(true);
		} else {
			guiPermission.setAccess(false);
		}

		guiPermissions.add(guiPermission);

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
