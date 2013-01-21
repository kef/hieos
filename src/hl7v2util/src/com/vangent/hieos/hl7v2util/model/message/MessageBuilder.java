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
package com.vangent.hieos.hl7v2util.model.message;

import ca.uhn.hl7v2.model.Message;
import com.vangent.hieos.hl7v2util.model.builder.BuilderConfig;

/**
 *
 * @author Bernie Thuman
 */
public class MessageBuilder {

    private BuilderConfig builderConfig;
    private Message inMessage;

    /**
     *
     * @param builderConfig
     * @param inMessage
     */
    public MessageBuilder(BuilderConfig builderConfig, Message inMessage) {
        this.builderConfig = builderConfig;
        this.inMessage = inMessage;
    }

    /**
     * 
     * @return
     */
    public BuilderConfig getBuilderConfig() {
        return builderConfig;
    }

    /**
     * 
     * @return 
     */
    public Message getInMessage() {
        return inMessage;
    }
}
