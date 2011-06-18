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
package com.vangent.hieos.policyutil.model.pdp;

import com.vangent.hieos.policyutil.model.attribute.Attribute;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bernie Thuman
 */
public class PDPRequest {

    private SubjectIdentifier patientId;
    private String issuer;
    private String action;
    private List<Attribute> subjectAttributes = new ArrayList<Attribute>();
    private List<Attribute> resourceAttributes = new ArrayList<Attribute>();

    public SubjectIdentifier getPatientId() {
        return patientId;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setPatientId(SubjectIdentifier patientId) {
        this.patientId = patientId;
    }

    public List<Attribute> getResourceAttributes() {
        return resourceAttributes;
    }

    public List<Attribute> getSubjectAttributes() {
        return subjectAttributes;
    }
}
