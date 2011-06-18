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
package com.vangent.hieos.policyutil.model.patientconsent;

import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;

/**
 *
 * @author Bernie Thuman
 */
public class Organization {

    private SubjectIdentifier id;

    public SubjectIdentifier getId() {
        return id;
    }

    public void setId(SubjectIdentifier id) {
        this.id = id;
    }
}
