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

package com.vangent.hieos.services.xds.bridge.model;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-22
 * @author         Jim Horner
 */
public class ResponseType {

    /** Field description */
    private final String documentId;

    /** Field description */
    private final String documentIdAsOID;

    /** Field description */
    private final String errorMessage;

    /** Field description */
    private final ResponseTypeStatus status;

    /**
     * Enum description
     *
     */
    public enum ResponseTypeStatus { Success, Failure, Unknown }

    /**
     * Constructs ...
     *
     *
     *
     * @param status
     * @param message
     */
    public ResponseType(ResponseTypeStatus status, String message) {

        this(null, null, status, message);
    }

    /**
     * Constructs ...
     *
     *
     * @param documentId
     * @param status
     */
    public ResponseType(String documentId, String documentOID,
            ResponseTypeStatus status) {

        this(documentId, documentOID, status, null);
    }

    /**
     * Constructs ...
     *
     *
     * @param documentId
     * @param documentOID
     * @param status
     * @param message
     */
    public ResponseType(String documentId, String documentOID,
                        ResponseTypeStatus status, String message) {

        super();
        this.documentId = documentId;
        this.documentIdAsOID = documentOID;
        this.status = ((status == null) ? ResponseTypeStatus.Unknown : status);
        this.errorMessage = message;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getDocumentId() {
        return documentId;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getDocumentIdAsOID() {
        return documentIdAsOID;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public ResponseTypeStatus getStatus() {
        return status;
    }
}
