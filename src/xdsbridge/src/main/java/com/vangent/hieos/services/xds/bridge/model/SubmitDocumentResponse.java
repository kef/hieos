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

import java.util.ArrayList;
import java.util.List;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-22
 * @author         Jim Horner
 */
public class SubmitDocumentResponse {

    /** Field description */
    private final List<SDRError> errors;

    /** Field description */
    private Status status;

    /**
     * Enum description
     *
     */
    public enum Status {
        Success, PartialSuccess, Failure, Unknown
    }

    /**
     * Constructs ...
     *
     */
    public SubmitDocumentResponse() {

        this(Status.Unknown);
    }

    /**
     * Constructs ...
     *
     *
     * @param status
     */
    public SubmitDocumentResponse(Status status) {

        super();
        this.errors = new ArrayList<SDRError>();
        this.status = status;
    }

    /**
     * Method description
     *
     *
     * @param error
     */
    public void addError(SDRError error) {

        if (error != null) {
            errors.add(error);
        }
    }

    /**
     * Method description
     *
     *
     * @param code
     * @param message
     */
    public void addError(String code, String message) {

        addError(new SDRError(code, message));
    }

    /**
     * Method description
     *
     *
     * @param document
     * @param code
     * @param message
     */
    public void addError(Document document, String code, String message) {

        String errmsg =
            String.format("Document %s had the following error: %s",
                          document.getId(), message);

        addError(code, errmsg);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public List<SDRError> getErrors() {
        return this.errors;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Method description
     *
     *
     * @param status
     */
    public void setStatus(Status status) {
        this.status = status;
    }
}
