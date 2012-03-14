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
package com.vangent.hieos.services.xds.registry.mu;

import com.vangent.hieos.xutil.metadata.structure.Metadata;

/**
 * 
 * @author Bernie Thuman
 */
public class UpdateStatusCommand extends MetadataUpdateCommand {

    private String targetObjectId;
    private String newStatus;
    private String originalStatus;

    /**
     *
     * @param metadata
     * @param metadataUpdateContext
     */
    public UpdateStatusCommand(Metadata metadata, MetadataUpdateContext metadataUpdateContext) {
        super(metadata, metadataUpdateContext);
    }

    /**
     *
     * @return
     */
    public String getTargetObjectId() {
        return targetObjectId;
    }

    /**
     *
     * @param targetObjectId
     */
    public void setTargetObjectId(String targetObjectId) {
        this.targetObjectId = targetObjectId;
    }

    /**
     *
     * @return
     */
    public String getNewStatus() {
        return newStatus;
    }

    /**
     *
     * @param newStatus
     */
    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    /**
     *
     * @return
     */
    public String getOriginalStatus() {
        return originalStatus;
    }

    /**
     *
     * @param originalStatus
     */
    public void setOriginalStatus(String originalStatus) {
        this.originalStatus = originalStatus;
    }

    @Override
    public void execute() {
        //throw new UnsupportedOperationException("Not supported yet.");
        // TBD: Implement.
        // Query registry and see what kind of object (Folder, ExtrinsicObject, Association) is being updated.
        // Should do in the following order:
        //   1. ExtrinsicObject
        //   2. Folder
        //   3. Association
        System.out.println("Executing command ... " + this.getClass().getName());
        this.getMetadataUpdateContext().getLogMessage().addOtherParam("Command", "Update Status");
    }
}
