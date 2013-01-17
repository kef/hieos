/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2013 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.hl7v2util.client;

import com.vangent.hieos.hl7v2util.exception.HL7v2UtilException;

/**
 *
 * @author Bernie Thuman
 */
public class HL7v2Endpoint {

    private String endpoint;
    private int port;
    private String ipAddressOrHostName;
    private boolean tlsEnabled;

    /**
     *
     * @param endpoint
     * @throws HL7v2UtilException 
     */
    public HL7v2Endpoint(String endpoint) throws HL7v2UtilException {
        this.endpoint = endpoint;
        this.parse();
    }

    /**
     *
     * @return
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     *
     * @return
     */
    public String getIpAddressOrHostName() {
        return ipAddressOrHostName;
    }

    /**
     * 
     * @return
     */
    public int getPort() {
        return port;
    }

    /**
     *
     * @return
     */
    public boolean isTlsEnabled() {
        return tlsEnabled;
    }

    /**
     *
     */
    private void parse() throws HL7v2UtilException {
        // Format (examples): mllp://129.6.24.143:9080 or mllps://129.6.24.143:9080

        // First get protocol type:
        // Split into component parts.
        String components[] = endpoint.split(":");

        // Length should be 3
        if (components.length != 3) {
            throw new HL7v2UtilException(this.getParseExceptionText());
        }

        // Determine protocol and if TLS is enabled.
        String protocol = components[0];
        if (protocol.equalsIgnoreCase("mllp")) {
            tlsEnabled = false;
        } else if (protocol.equalsIgnoreCase("mllps")) {
            tlsEnabled = true;
        } else {
            throw new HL7v2UtilException(this.getParseExceptionText());
        }

        // Get port
        port = new Integer(components[2]);

        // Get IP or host name
        String ipOrHostNameComponent = components[1];
        if (ipOrHostNameComponent.startsWith("//")) {
            ipAddressOrHostName = ipOrHostNameComponent.substring(2);
        } else {
            throw new HL7v2UtilException(this.getParseExceptionText());
        }
    }

    /**
     *
     * @return
     */
    private String getParseExceptionText() {
        return "Unable to parse HL7v2 endpoint "
                + endpoint + " - format should be <mllp|mllps>://<host or ip>:<port>";
    }
}
