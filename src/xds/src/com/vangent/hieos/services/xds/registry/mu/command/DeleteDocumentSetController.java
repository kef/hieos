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
package com.vangent.hieos.services.xds.registry.mu.command;

import com.vangent.hieos.services.xds.registry.mu.support.MetadataUpdateContext;
import com.vangent.hieos.services.xds.registry.mu.validation.DeleteDocumentSetCommandValidator;
import com.vangent.hieos.services.xds.registry.mu.validation.MetadataUpdateCommandValidator;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;

/**
 *
 * @author Bernie Thuman
 */
public class DeleteDocumentSetController extends MetadataUpdateController {

    /**
     *
     * @param metadataUpdateContext
     * @param submittedMetadata
     */
    public DeleteDocumentSetController(MetadataUpdateContext metadataUpdateContext, Metadata submittedMetadata) {
        super(metadataUpdateContext, submittedMetadata);
    }

    /**
     * 
     * @return
     * @throws XdsException
     */
    @Override
    public boolean update() throws XdsException {
        // Create and run command.
        MetadataUpdateCommandValidator validator = new DeleteDocumentSetCommandValidator();
        DeleteDocumentSetCommand cmd =
                new DeleteDocumentSetCommand(submittedMetadata, metadataUpdateContext, validator);
        // Execute command.
        return cmd.validateAndUpdate();
    }

    /**
     *
     * @return
     * @throws XdsException
     */
    @Override
    public boolean enforcePolicy() throws XdsException {
        // TBD: Implement.
        return true;
    }
}
