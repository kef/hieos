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
public class SubjectIdentifier {

    private String identifier;
    private SubjectIdentifierDomain identifierDomain;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public SubjectIdentifierDomain getIdentifierDomain() {
        return identifierDomain;
    }

    public void setIdentifierDomain(SubjectIdentifierDomain identifierDomain) {
        this.identifierDomain = identifierDomain;
    }
}
