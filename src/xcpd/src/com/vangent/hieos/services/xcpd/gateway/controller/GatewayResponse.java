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

import com.vangent.hieos.hl7v3util.model.message.PRPA_IN201306UV02_Message;

/**
 *
 * @author Bernie Thuman
 */
/**
 *
 */
public class GatewayResponse {

    private PRPA_IN201306UV02_Message response;
    private GatewayRequest request;

    public PRPA_IN201306UV02_Message getResponse() {
        return response;
    }

    public void setResponse(PRPA_IN201306UV02_Message response) {
        this.response = response;
    }

    public void setRequest(GatewayRequest request) {
        this.request = request;
    }

    public GatewayRequest getRequest() {
        return this.request;
    }
}
