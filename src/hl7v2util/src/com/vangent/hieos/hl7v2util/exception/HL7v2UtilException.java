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
package com.vangent.hieos.hl7v2util.exception;

/**
 *
 * @author Bernie Thuman
 */
public class HL7v2UtilException extends Exception {

    /**
     * 
     */
    public static String ERROR_CODE_UNKNOWN_KEY_IDENTIFIER = "204";
    private String code = null;

    /**
     *
     * @param msg
     */
    public HL7v2UtilException(String msg) {
        super(msg);
    }

    /**
     *
     * @param msg
     * @param code
     */
    public HL7v2UtilException(String msg, String code) {
        super(msg);
        this.code = code;
    }

    /**
     *
     * @param msg
     * @param cause
     */
    public HL7v2UtilException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     *
     * @param exception
     */
    public HL7v2UtilException(Exception exception) {
        super(exception);
    }

    /**
     *
     * @return
     */
    public String getCode() {
        return code;
    }
}
