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

import com.vangent.hieos.services.xds.bridge.model.ResponseType
    .ResponseTypeStatus;

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
    private final List<ResponseType> responses;

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
        this.responses = new ArrayList<ResponseType>();
        this.status = status;
    }

    /**
     * Method description
     *
     *
     *
     * @param response
     */
    public void addResponse(ResponseType response) {

        if (response != null) {
            responses.add(response);
        }
    }

    /**
     * Method description
     *
     *
     *
     * @param status
     * @param message
     */
    public void addResponse(ResponseTypeStatus status, String message) {

        addResponse(new ResponseType(status, message));
    }

    /**
     * Method description
     *
     *
     * @param document
     * @param status
     * @param message
     */
    public void addResponse(Document document, ResponseTypeStatus status,
                            String message) {

        addResponse(new ResponseType(document.getId(), status, message));
    }

    /**
     * Method description
     *
     *
     * @param document
     */
    public void addSuccess(Document document) {

        addResponse(new ResponseType(document.getId(),
                                     ResponseTypeStatus.Success));
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public List<ResponseType> getResponses() {
        return this.responses;
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
