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

import com.vangent.hieos.services.xds.registry.mu.command.MetadataUpdateCommand;
import com.vangent.hieos.services.xds.registry.mu.command.UpdateStatusCommand;
import com.vangent.hieos.xutil.exception.XdsException;

/**
 *
 * @author Bernie Thuman
 */
public class UpdateStatusCommandValidator extends UpdateDocumentSetCommandValidator {

    /**
     * 
     * @param metadataUpdateCommand
     */
    public UpdateStatusCommandValidator(MetadataUpdateCommand metadataUpdateCommand) {
        super(metadataUpdateCommand);
    }

    /**
     * 
     * @throws XdsException
     */
    public boolean validate() throws XdsException {
        // Not really used here, but framework is in-place.
        UpdateStatusCommand cmd = (UpdateStatusCommand)this.getMetadataUpdateCommand();
        return true;
    }
}
