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
package com.vangent.hieos.services.xca.gateway.controller;

import com.vangent.hieos.xutil.atna.ATNAAuditEvent;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent.ActorType;
import com.vangent.hieos.xutil.xconfig.XConfigActor;

// Third party.
import java.util.ArrayList;
import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

// Exceptions.
import com.vangent.hieos.xutil.exception.SOAPFaultException;

/**
 *
 * @author Bernie Thuman
 */
abstract public class XCAAbstractRequestCollection {

    private final static Logger logger = Logger.getLogger(XCAAbstractRequestCollection.class);
    private String uniqueId = null;             // Target entity id (homeCommunityId, repositoryId, registryName.
    private boolean isLocalRequest = false;     // True if going to a local entity (e.g. registry/repository).
    private ArrayList<XCARequest> requests = new ArrayList<XCARequest>();   // Request nodes destined for a community.
    private XConfigActor configActor = null;  // Configuration for the target entity (e.g XConfigGateway or XConfigRepository).
    private OMElement result = null;
    private ArrayList<XCAErrorMessage> errors = new ArrayList<XCAErrorMessage>();
    private ATNAAuditEvent.ActorType gatewayActorType;

    abstract String getEndpointURL();

    /**
     * 
     * @return
     * @throws SOAPFaultException
     */
    abstract OMElement sendRequests() throws SOAPFaultException;

    /**
     *
     */
    private XCAAbstractRequestCollection() {
        // Do not allow.
    }

    /**
     *
     * @param uniqueId
     * @param configActor
     * @param isLocalRequest
     * @param gatewayActorType
     */
    public XCAAbstractRequestCollection(String uniqueId, XConfigActor configActor, boolean isLocalRequest, ATNAAuditEvent.ActorType gatewayActorType) {
        this.uniqueId = uniqueId;
        this.configActor = configActor;
        this.isLocalRequest = isLocalRequest;
        this.gatewayActorType = gatewayActorType;
    }

    /**
     * 
     * @return
     */
    public ArrayList<XCARequest> getRequests() {
        return this.requests;
    }

    /**
     * 
     * @param request
     */
    public void addRequest(XCARequest request) {
        requests.add(request);
    }

    /**
     *
     * @return
     */
    public boolean isLocalRequest() {
        return this.isLocalRequest;
    }

    /**
     *
     * @return
     */
    public String getUniqueId() {
        return uniqueId;
    }

    /**
     *
     * @return
     */
    public XConfigActor getXConfigActor() {
        return this.configActor;
    }

    /**
     *
     * @return
     */
    public OMElement getResult() {
        return result;
    }

    /**
     *
     * @param result
     */
    public void setResult(OMElement result) {
        this.result = result;
    }

    /**
     *
     * @return
     */
    public ArrayList<XCAErrorMessage> getErrors() {
        return errors;
    }

    /**
     * 
     * @param error
     */
    public void addErrorMessage(XCAErrorMessage error) {
        this.errors.add(error);
    }

    /**
     *
     * @return
     */
    public ActorType getGatewayActorType() {
        return gatewayActorType;
    }
}
