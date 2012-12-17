/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2012 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.empi.exception;

import com.vangent.hieos.subjectmodel.SubjectIdentifierDomain;

/**
 *
 * @author Bernie Thuman
 */
public class EMPIExceptionUnknownIdentifierDomain extends Exception {

    /**
     *
     */
    public static final String UNKNOWN_KEY_IDENTIFIER_ERROR_CODE = "204";
    private SubjectIdentifierDomain subjectIdentifierDomain;
    private int listPosition;
    private IdentifierDomainType identifierDomainType = IdentifierDomainType.SUBJECT_IDENTIFIER_DOMAIN;

    /**
     *
     */
    public enum IdentifierDomainType {

        /**
         *
         */
        SUBJECT_IDENTIFIER_DOMAIN,
        /**
         *
         */
        SCOPING_IDENTIFIER_DOMAIN
    };

    /**
     *
     * @param msg
     */
    public EMPIExceptionUnknownIdentifierDomain(String msg) {
        super(msg);
    }

    /**
     *
     * @param msg
     * @param cause
     */
    public EMPIExceptionUnknownIdentifierDomain(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     *
     * @param exception
     */
    public EMPIExceptionUnknownIdentifierDomain(Exception exception) {
        super(exception);
    }

    /**
     *
     * @return
     */
    public int getListPosition() {
        return listPosition;
    }

    /**
     *
     * @param listPosition
     */
    public void setListPosition(int listPosition) {
        this.listPosition = listPosition;
    }

    /**
     * 
     * @return
     */
    public SubjectIdentifierDomain getSubjectIdentifierDomain() {
        return subjectIdentifierDomain;
    }

    /**
     *
     * @param subjectIdentifierDomain
     */
    public void setSubjectIdentifierDomain(SubjectIdentifierDomain subjectIdentifierDomain) {
        this.subjectIdentifierDomain = subjectIdentifierDomain;
    }

    /**
     *
     * @return
     */
    public IdentifierDomainType getIdentifierDomainType() {
        return identifierDomainType;
    }

    /**
     * 
     * @param identifierDomainType
     */
    public void setIdentifierDomainType(IdentifierDomainType identifierDomainType) {
        this.identifierDomainType = identifierDomainType;
    }
}
