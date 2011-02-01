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
package com.vangent.hieos.hl7v3util.model.subject;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectSearchCriteria {

    private Subject subject = null;
    private String communityPatientIdAssigningAuthority;

    /**
     *
     * @return
     */
    public Subject getSubject() {
        return subject;
    }

    /**
     *
     * @param subject
     */
    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    /**
     *
     * @return
     */
    public String getCommunityPatientIdAssigningAuthority() {
        return communityPatientIdAssigningAuthority;
    }

    /**
     *
     * @param communityPatientIdAssigningAuthority
     */
    public void setCommunityPatientIdAssigningAuthority(String communityPatientIdAssigningAuthority) {
        this.communityPatientIdAssigningAuthority = communityPatientIdAssigningAuthority;
    }
}
