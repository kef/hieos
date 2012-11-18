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
package com.vangent.hieos.empi.exception;

/**
 *
 * @author Bernie Thuman
 */
public class EMPIException extends Exception {

    /**
     * 
     */
    public static String ERROR_CODE_UNKNOWN_KEY_IDENTIFIER = "204";
    private String code = null;

    /**
     *
     * @param msg
     */
    public EMPIException(String msg) {
        super(msg);
    }

    /**
     *
     * @param msg
     * @param code
     */
    public EMPIException(String msg, String code) {
        super(msg);
        this.code = code;
    }

    /**
     *
     * @param msg
     * @param cause
     */
    public EMPIException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     *
     * @param exception
     */
    public EMPIException(Exception exception) {
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
