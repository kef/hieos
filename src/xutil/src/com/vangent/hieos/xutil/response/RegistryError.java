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

package com.vangent.hieos.xutil.response;

/**
 *
 * @author Jim Horner
 */
public class RegistryError {

    /** Field description */
    private String code;

    /** Field description */
    private String context;

    /** Field description */
    private String location;

    /** Field description */
    private ErrorSeverity severity;

    /**
     * Constructs ...
     *
     */
    public RegistryError() {
        super();
        this.severity = ErrorSeverity.Unknown;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getCode() {
        return code;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getContext() {
        return context;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getLocation() {
        return location;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public ErrorSeverity getSeverity() {
        return severity;
    }

    /**
     * Method description
     *
     *
     * @param code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Method description
     *
     *
     * @param context
     */
    public void setContext(String context) {
        this.context = context;
    }

    /**
     * Method description
     *
     *
     * @param location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Method description
     *
     *
     * @param severity
     */
    public void setSeverity(ErrorSeverity severity) {
        this.severity = severity;
    }
}
