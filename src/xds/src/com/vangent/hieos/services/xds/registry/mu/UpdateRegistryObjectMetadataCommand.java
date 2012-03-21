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
public abstract class UpdateRegistryObjectMetadataCommand extends MetadataUpdateCommand {

    private OMElement targetObject;
    private String previousVersion;
    private boolean associationPropagation;

    /**
     *
     * @param metadata
     * @param metadataUpdateContext
     */
    public UpdateRegistryObjectMetadataCommand(Metadata metadata, MetadataUpdateContext metadataUpdateContext) {
        super(metadata, metadataUpdateContext);
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
    public boolean isAssociationPropagation() {
        return associationPropagation;
    }

    /**
     *
     * @param associationPropagation
     */
    public void setAssociationPropagation(boolean associationPropagation) {
        this.associationPropagation = associationPropagation;
    }
}
