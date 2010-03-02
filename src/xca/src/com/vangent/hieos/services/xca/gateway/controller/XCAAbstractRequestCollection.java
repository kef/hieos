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

import com.vangent.hieos.xutil.xconfig.XConfigEntity;
import com.vangent.hieos.xutil.atna.XATNALogger;

// Third party.
import java.util.ArrayList;
import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

// Exceptions.
import com.vangent.hieos.xutil.exception.XdsWSException;
import com.vangent.hieos.xutil.exception.XdsException;

/**
 *
 * @author Bernie Thuman
 */
abstract public class XCAAbstractRequestCollection {

    private final static Logger logger = Logger.getLogger(XCAAbstractRequestCollection.class);
    private String uniqueId = null;             // Target entity id (homeCommunityId, repositoryId, registryName.
    private boolean isLocalRequest = false;     // True if going to a local entity (e.g. registry/repository).
    private ArrayList<XCARequest> requests = new ArrayList<XCARequest>();   // Request nodes destined for a community.
    private XConfigEntity configEntity = null;  // Configuration for the target entity (e.g XConfigGateway or XConfigRepository).
    private OMElement result = null;
    private ArrayList<XCAErrorMessage> errors = new ArrayList<XCAErrorMessage>();

    abstract String getEndpointURL();

    /**
     * 
     * @return
     * @throws XdsWSException
     * @throws XdsException
     */
    abstract OMElement sendRequests() throws XdsWSException, XdsException;

    /**
     * 
     * @param uniqueId
     * @param request
     */
    public XCAAbstractRequestCollection(String uniqueId, XConfigEntity configEntity, boolean isLocalRequest) {
        this.uniqueId = uniqueId;
        this.configEntity = configEntity;
        this.isLocalRequest = isLocalRequest;
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
    public XConfigEntity getConfigEntity() {
        return this.configEntity;
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
    public void addErrorMessage(XCAErrorMessage error)
    {
        this.errors.add(error);
    }

    /**
     *
     * @param ATNAtxn
     * @param request
     * @param endpoint
     * @param successFlag
     * @param actor
     */
    protected void performAudit(String ATNAtxn, OMElement request, String endpoint, XATNALogger.OutcomeIndicator outcome) {
        try {
            XATNALogger xATNALogger = new XATNALogger(ATNAtxn, XATNALogger.ActorType.DOCCONSUMER);
            xATNALogger.performAudit(request, endpoint, outcome);
        } catch (Exception e) {
            // Eat exception.
            logger.error("Could not perform ATNA audit", e);
        }
    }
   
}
