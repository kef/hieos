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
package com.vangent.hieos.DocViewer.client.model.patient;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class PatientUtil {

	private static final int EUID_COMPONENT_COUNT = 4;
	private static final int UNIVERSALID_COMPONENT_COUNT = 3;
	private static final String EUID_SPLIT_CHAR = "\\^";
	private static final String UNIVERSALID_SPLIT_CHAR = "\\&";

	/**
	 * 
	 * @param pid
	 * @return
	 */
	public static boolean validatePIDStringFormat(String pid) {
		String[] pidSplit = pid.split(EUID_SPLIT_CHAR);
		if (pidSplit.length == EUID_COMPONENT_COUNT) {
			String aa = pidSplit[3];
			String aaSplit[] = aa.split(UNIVERSALID_SPLIT_CHAR);
			if (aaSplit.length == UNIVERSALID_COMPONENT_COUNT) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param pid
	 * @return
	 */
	public static String getIDFromPIDString(String pid) {
		String[] pidSplit = pid.split(EUID_SPLIT_CHAR);
		if (pidSplit.length == EUID_COMPONENT_COUNT) {
			return pidSplit[0];
		}
		return "UNKNOWN";
	}

	/**
	 * 
	 * @param pid
	 * @return
	 */
	public static String getAssigningAuthorityFromPIDString(String pid) {
		String[] pidSplit = pid.split(EUID_SPLIT_CHAR);
		if (pidSplit.length == EUID_COMPONENT_COUNT) {
			String aa = pidSplit[3];
			return aa;
			/*
			String aaSplit[] = aa.split(UNIVERSALID_SPLIT_CHAR);
			if (aaSplit.length == UNIVERSALID_COMPONENT_COUNT) {
				return aaSplit[1];
			}*/
		}
		return "UNKNOWN";
	}
	/**
	 * 
	 * @param pid
	 * @return
	 */
	public static String getUniversalIDFromPIDString(String pid) {
		String[] pidSplit = pid.split(EUID_SPLIT_CHAR);
		if (pidSplit.length == EUID_COMPONENT_COUNT) {
			String aa = pidSplit[3];
			String aaSplit[] = aa.split(UNIVERSALID_SPLIT_CHAR);
			if (aaSplit.length == UNIVERSALID_COMPONENT_COUNT) {
				return aaSplit[1];
			}
		}
		return "UNKNOWN";
	}
}
