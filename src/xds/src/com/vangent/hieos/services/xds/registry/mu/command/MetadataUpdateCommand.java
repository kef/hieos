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
import com.vangent.hieos.services.xds.registry.mu.validation.MetadataUpdateCommandValidator;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

/**
 *
 * @author Bernie Thuman
 */
public abstract class MetadataUpdateCommand {

    private Metadata metadata;
    private MetadataUpdateContext metadataUpdateContext;

    /**
     *
     * @param metadata
     * @param metadataUpdateContext
     */
    public MetadataUpdateCommand(Metadata metadata, MetadataUpdateContext metadataUpdateContext) {
        this.metadata = metadata;
        this.metadataUpdateContext = metadataUpdateContext;
    }

    /**
     *
     * @return
     */
    public Metadata getMetadata() {
        return metadata;
    }

    /**
     *
     * @return
     */
    public MetadataUpdateContext getMetadataUpdateContext() {
        return metadataUpdateContext;
    }

    /**
     *
     * @return
     * @throws XdsException
     */
    public boolean run() throws XdsException {
        // FIXME: Probably can't put here (since a transaction can include > 1 command).
        XLogMessage logMessage = this.getMetadataUpdateContext().getLogMessage();
        if (logMessage.isLogEnabled()) {
            String className = this.getClass().getSimpleName();
            logMessage.setTestMessage("MU." + className);
            logMessage.addOtherParam("Command", className);
        }
        MetadataUpdateCommandValidator validator = this.getCommandValidator();
        boolean runStatus = validator.validate();
        if (runStatus) {
            runStatus = this.execute(validator);
        }
        return runStatus;
    }

    /**
     * 
     * @return
     * @throws XdsException
     */
    abstract protected boolean execute(MetadataUpdateCommandValidator validator) throws XdsException;

    /**
     *
     * @return
     * @throws XdsException
     */
    abstract protected MetadataUpdateCommandValidator getCommandValidator();
}
