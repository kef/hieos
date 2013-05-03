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
package com.vangent.hieos.DocViewer.client.model.authentication;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author Anand Sastry
 */
public class AuthenticationContextDTO implements IsSerializable {
	public final static String PERMISSION_VIEWDOCS = "ViewDocs";
	public final static String PERMISSION_VIEWCONSENT = "ViewConsent";
	public final static String PERMISSION_EDITCONSENT = "EditConsent";

	private boolean successStatus;
	private Date creationDate;
	private UserProfileDTO userProfile;

	/**
     * 
     */
	public AuthenticationContextDTO() {
	}

	/**
	 * 
	 * @param creationDate
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * 
	 * @return
	 */
	public Date getCreationDate() {
		return this.creationDate;
	}

	/**
	 * 
	 * @param successFlag
	 */
	public void setSuccessFlag(boolean successFlag) {
		this.successStatus = successFlag;
	}

	/**
	 * 
	 * @return
	 */
	public boolean getSuccessStatus() {
		return this.successStatus;
	}

	/**
	 * 
	 * @param userProfile
	 */
	public void setUserProfile(UserProfileDTO userProfile) {
		this.userProfile = userProfile;
	}

	/**
	 * 
	 * @return
	 */
	public UserProfileDTO getUserProfile() {
		return this.userProfile;
	}

	/**
	 * 
	 * @return
	 */
	public boolean hasPermissionToApplication() {
		// Check if the user has permission to use the application
		List<PermissionDTO> permissions = this.getUserProfile().getPermissions();
		boolean permitted = false;
		for (PermissionDTO perm : permissions) {
			String permissionName = perm.getName();
			if (permissionName.equals(PERMISSION_VIEWDOCS)
					|| permissionName.equals(PERMISSION_VIEWCONSENT)
					|| permissionName.equals(PERMISSION_EDITCONSENT)) {
				permitted = true;
				break;
			}
		}
		return permitted;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean hasPermissionToConsentManagementFeature() {
		// Check if the user has permission to use the application
		List<PermissionDTO> permissions = this.getUserProfile().getPermissions();
		boolean permitted = false;
		for (PermissionDTO perm : permissions) {
			String permissionName = perm.getName();
			if (permissionName.equals(PERMISSION_VIEWCONSENT)
					|| permissionName.equals(PERMISSION_EDITCONSENT)) {
				permitted = true;
				break;
			}
		}
		return permitted;
	}

	/**
	 * 
	 * @param permissionRequest
	 * @return
	 */
	public boolean hasPermissionToFeature(String permissionRequest) {
		// Check if the user has permission to the requested feature.
		List<PermissionDTO> permissions = this.getUserProfile().getPermissions();
		boolean permitted = false;
		for (PermissionDTO perm : permissions) {
			String permissionName = perm.getName();
			if (permissionName.equals(permissionRequest)) {
				permitted = true;
				break;
			}
		}
		return permitted;
	}

	/**
     * 
     */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("CreationDate [").append(creationDate)
				.append("], Success Status [").append(this.successStatus)
				.append("], User Profile [").append(this.userProfile)
				.append("]");
		return buf.toString();
	}
}
