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
import org.apache.axiom.om.OMElement;

/**
 *
 * @author Bernie Thuman
 */
public class UpdateFolderMetadataCommand extends MetadataUpdateCommand {

    private String previousVersion;
    private OMElement targetObject;

    /**
     *
     * @param metadata
     * @param metadataUpdateContext
     */
    public UpdateFolderMetadataCommand(Metadata metadata, MetadataUpdateContext metadataUpdateContext) {
        super(metadata, metadataUpdateContext);
    }

    /**
     *
     * @return
     */
    public String getPreviousVersion() {
        return previousVersion;
    }

    /**
     *
     * @param previousVersion
     */
    public void setPreviousVersion(String previousVersion) {
        this.previousVersion = previousVersion;
    }

    /**
     *
     * @return
     */
    public OMElement getTargetObject() {
        return targetObject;
    }

    /**
     *
     * @param targetObject
     */
    public void setTargetObject(OMElement targetObject) {
        this.targetObject = targetObject;
    }

    @Override
    public void execute() {
        //throw new UnsupportedOperationException("Not supported yet.");
         this.getMetadataUpdateContext().getLogMessage().addOtherParam("Command", "Update Folder Metadata");
    }
}
