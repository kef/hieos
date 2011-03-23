/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2010 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.xcpd.gateway.controller;

import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201305UV02_Message;
import com.vangent.hieos.hl7v3util.model.subject.SubjectSearchCriteria;

import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigTransaction;

/**
 *
 * @author Bernie Thuman
 */
/**
 *
 */
public class GatewayRequest {

    private XConfigActor rgConfig;
    private PRPA_IN201305UV02_Message request;
    private SubjectSearchCriteria subjectSearchCriteria;

    public XConfigActor getRGConfig() {
        return rgConfig;
    }

    public String getEndpoint() {
        XConfigTransaction txn = rgConfig.getTransaction("CrossGatewayPatientDiscovery");
        return txn.getEndpointURL();
    }

    public void setRGConfig(XConfigActor rgConfig) {
        this.rgConfig = rgConfig;
    }

    public PRPA_IN201305UV02_Message getRequest() {
        return request;
    }

    public void setRequest(PRPA_IN201305UV02_Message request) {
        this.request = request;
    }

    public SubjectSearchCriteria getSubjectSearchCriteria() {
        return subjectSearchCriteria;
    }

    public void setSubjectSearchCriteria(SubjectSearchCriteria subjectSearchCriteria) {
        this.subjectSearchCriteria = subjectSearchCriteria;
    }

    /**
     *
     * @return
     */
    public String getVitals() {
        return "(community: " + this.getRGConfig().getUniqueId() + ", endpoint: " + this.getEndpoint() + ")";
    }
}
