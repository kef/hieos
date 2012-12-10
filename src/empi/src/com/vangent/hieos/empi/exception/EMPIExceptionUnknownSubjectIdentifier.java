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

/**
 *
 * @author Bernie Thuman
 */
public class EMPIExceptionUnknownSubjectIdentifier extends Exception {

    public static final String UNKNOWN_KEY_IDENTIFIER_ERROR_CODE = "204";

    /**
     *
     * @param msg
     */
    public EMPIExceptionUnknownSubjectIdentifier(String msg) {
        super(msg);
    }

    /**
     *
     * @param msg
     * @param cause
     */
    public EMPIExceptionUnknownSubjectIdentifier(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     *
     * @param exception
     */
    public EMPIExceptionUnknownSubjectIdentifier(Exception exception) {
        super(exception);
    }
}
