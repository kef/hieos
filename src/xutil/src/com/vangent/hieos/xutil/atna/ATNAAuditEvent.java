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
package com.vangent.hieos.xutil.atna;

/**
 *
 * @author Bernie Thuman
 */
public class ATNAAuditEvent {

    private String targetEndpoint = null;
    private IHETransaction transaction;
    private String homeCommunityId = null;
    private ActorType actorType = ActorType.UNKNOWN;
    private OutcomeIndicator outcomeIndicator = OutcomeIndicator.SUCCESS;
    private AuditEventType auditEventType = AuditEventType.UNKNOWN;

    /**
     *
     */
    public enum AuditEventType {

        /**
         * 
         */
        IMPORT,
        /**
         *
         */
        EXPORT,
        /**
         * 
         */
        QUERY_PROVIDER,
        /**
         *
         */
        QUERY_INITIATOR,
        /**
         *
         */
        UNKNOWN
    };

    /**
     *
     */
    public enum ActorType {

        /**
         * 
         */
        REGISTRY,
        /**
         * 
         */
        REPOSITORY,
        /**
         *
         */
        DOCCONSUMER,
        /**
         * 
         */
        DOCSOURCE,
        /**
         *
         */
        INITIATING_GATEWAY,
        /**
         *
         */
        RESPONDING_GATEWAY,
        /**
         * 
         */
        UNKNOWN,
        /**
         *
         */
        DOCRECIPIENT,
        /**
         *
         */
        PATIENT_DEMOGRAPHICS_SUPPLIER,
        /**
         *
         */
        PIX_MANAGER
    }

    // BHT: Deals with OutcomeIndicator as defined by DICOM Supplement 95
    /**
     *
     */
    public enum OutcomeIndicator {

        /**
         * 
         */
        SUCCESS(0),
        /**
         *
         */
        MINOR_FAILURE(4),
        /**
         * 
         */
        SERIOUS_FAILURE(8),
        /**
         *
         */
        MAJOR_FAILURE(12);
        private final int value;

        OutcomeIndicator(int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return Integer.toString(value);
        }
    }

    /**
     *
     */
    public enum IHETransaction {

        /**
         *
         */
        ITI8("ITI-8"),
        /**
         *
         */
        ITI18("ITI-18"),
        /**
         *
         */
        ITI38("ITI-38"),
        /**
         *
         */
        ITI39("ITI-39"),
        /**
         *
         */
        ITI41("ITI-41"),
        /**
         *
         */
        ITI42("ITI-42"),
        /**
         *
         */
        ITI43("ITI-43"),
        /**
         *
         */
        ITI44("ITI-44"),
        /**
         *
         */
        ITI45("ITI-45"),
        /**
         *
         */
        ITI46("ITI-46"),
        /**
         *
         */
        ITI47("ITI-47"),
        /**
         *
         */
        ITI51("ITI-51"),
        /**
         *
         */
        ITI55("ITI-55"),
        /**
         *
         */
        START("START"),
        /**
         *
         */
        STOP("STOP");
        private final String value;

        IHETransaction(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    /**
     *
     * @return
     */
    public String getTargetEndpoint() {
        return targetEndpoint;
    }

    /**
     *
     * @param targetEndpoint
     */
    public void setTargetEndpoint(String targetEndpoint) {
        this.targetEndpoint = targetEndpoint;
    }

    /**
     *
     * @return
     */
    public AuditEventType getAuditEventType() {
        return auditEventType;
    }

    /**
     *
     * @param auditEventType
     */
    public void setAuditEventType(AuditEventType auditEventType) {
        this.auditEventType = auditEventType;
    }

    /**
     *
     * @return
     */
    public IHETransaction getTransaction() {
        return transaction;
    }

    /**
     *
     * @param transaction
     */
    public void setTransaction(IHETransaction transaction) {
        this.transaction = transaction;
    }

    /**
     *
     * @return
     */
    public ActorType getActorType() {
        return actorType;
    }

    /**
     *
     * @param actorType
     */
    public void setActorType(ActorType actorType) {
        this.actorType = actorType;
    }

    /**
     *
     * @return
     */
    public String getHomeCommunityId() {
        return homeCommunityId;
    }

    /**
     *
     * @param homeCommunityId
     */
    public void setHomeCommunityId(String homeCommunityId) {
        this.homeCommunityId = homeCommunityId;
    }

    /**
     * 
     * @return
     */
    public OutcomeIndicator getOutcomeIndicator() {
        return outcomeIndicator;
    }

    /**
     *
     * @param outcomeIndicator
     */
    public void setOutcomeIndicator(OutcomeIndicator outcomeIndicator) {
        this.outcomeIndicator = outcomeIndicator;
    }

    /**
     * 
     * @return
     */
    public String getTransactionDisplayName() {
        String displayName;
        switch (this.transaction) {
            case ITI8:
            case ITI44:
                displayName = "Patient Identity Feed";
                break;
            case ITI18:
                displayName = "Registry Stored Query";
                break;
            case ITI38:
                displayName = "Cross Gateway Query";
                break;
            case ITI39:
                displayName = "Cross Gateway Retrieve";
                break;
            case ITI41:
                displayName = "Provide and Register Document Set b";
                break;
            case ITI42:
                displayName = "Register Document Set-b";
                break;
            case ITI43:
                displayName = "Retrieve Document Set";
                break;
            case ITI45:
                displayName = "PIX Query";
                break;
            case ITI46:
                displayName = "PIX Update Notification";
                break;
            case ITI47:
                displayName = "Patient Demographics Query";
                break;
            case ITI51:
                displayName = "Multi-Patient Query";
                break;
            case ITI55:
                displayName = "Cross Gateway Patient Discovery";
                break;
            case START:
                displayName = "Application Start";
                break;
            case STOP:
                displayName = "Application Stop";
                break;
            default:
                displayName = "UNKNOWN";
                break;

        }
        return displayName;
    }
}
