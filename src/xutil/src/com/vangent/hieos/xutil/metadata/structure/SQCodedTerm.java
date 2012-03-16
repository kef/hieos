/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2008-2009 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.xutil.metadata.structure;

/**
 *
 * @author Bernie Thuman
 */
public abstract class SQCodedTerm {

    /**
     *
     * @return
     */
    public abstract boolean isEmpty();


	static String[] codeParameters = { 
			"$XDSDocumentEntryClassCode",
			"$XDSDocumentEntryTypeCode"	,
			"$XDSDocumentEntryPracticeSettingCode",
			"$XDSDocumentEntryHealthcareFacilityTypeCode",
			"$XDSDocumentEntryEventCodeList",
			"$XDSDocumentEntryConfidentialityCode",
			"$XDSDocumentEntryFormatCode",
			"$XDSSubmissionSetContentType",
			"$XDSFolderCodeList"
	};

        // Trimmed down various parameters to get around an Oracle alias length problem.
	static String[] codeParmVarName = {
			"classCode",
			"typeCode",
			"psCode",        // practiceSettingCode
			"hfTypeCode",    // healthcareFacilityTypeCode
			"eCodeList",     // eventCodeList
			"cCode",         // confidentialityCode
			"formatCode",
			"contentType",
			"codeList"
	};

	static String[] codeParmUUID = {
			"urn:uuid:41a5887f-8865-4c09-adf7-e362475b143a",
			"urn:uuid:f0306f51-975f-434e-a61c-c59651d33983"	,
			"urn:uuid:cccf5598-8b07-4b77-a05e-ae952c785ead",
			"urn:uuid:f33fb8ac-18af-42cc-ae0e-ed0b0bdb91e1",
			"urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4",
			"urn:uuid:f4f85eac-e6cb-4883-b524-f2705394840f",
			"urn:uuid:a09d5840-386c-46f2-b5ad-9c3699a4309d",
			"urn:uuid:aa543740-bdda-424e-8c96-df4873be8500",
			"urn:uuid:1ba97051-7806-41a8-a48b-8fce7af683c5"
	};

        /**
         *
         * @param name
         * @return
         */
        public static int codeIndex(String name) {
		for (int i=0; i<codeParameters.length; i++)
			if (codeParameters[i].equals(name))
				return i;
		return -1;
	}

        /**
         *
         * @param name
         * @return
         */
        public static boolean isCodeParameter(String name) {
		return (codeIndex(name) != -1);
	}

        /**
         *
         * @param codeName
         * @return
         */
        public static String codeVarName(String codeName) {
		return codeParmVarName[codeIndex(codeName)];
	}

        /**
         *
         * @param codeName
         * @return
         */
        public static String codeUUID(String codeName) {
		return codeParmUUID[codeIndex(codeName)];
	}
	
}
