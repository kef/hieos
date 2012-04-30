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
package com.vangent.hieos.services.xds.registry.mu.validation;

import com.vangent.hieos.services.xds.registry.mu.command.DeleteDocumentSetCommand;
import com.vangent.hieos.services.xds.registry.mu.command.MetadataUpdateCommand;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import java.util.List;

/**
 *
 * @author Bernie Thuman
 */
public class DeleteDocumentSetCommandValidator extends MetadataUpdateCommandValidator {

    /**
     * 
     * @param metadataUpdateCommand
     */
    public DeleteDocumentSetCommandValidator(MetadataUpdateCommand metadataUpdateCommand) {
        super(metadataUpdateCommand);
    }

    /**
     * 
     * @throws XdsException
     */
    public boolean validate() throws XdsException {
        DeleteDocumentSetCommand cmd = (DeleteDocumentSetCommand) this.getMetadataUpdateCommand();
        Metadata submittedMetadata = cmd.getMetadata();
        // Get list of object references.
        List<String> objectRefIds = submittedMetadata.getObjectRefIds();
        if (objectRefIds.isEmpty()) {
            throw new XdsException("No object references specified");
        }
        boolean validationSuccess = true;
        // TBD: implement.
        return validationSuccess;
    }
}
