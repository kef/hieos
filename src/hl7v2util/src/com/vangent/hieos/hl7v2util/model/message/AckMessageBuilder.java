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

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.DefaultApplication;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.util.Terser;
import com.vangent.hieos.hl7v2util.model.builder.BuilderConfig;
import java.io.IOException;

/**
 *
 * @author Bernie Thuman
 */
public class AckMessageBuilder extends MessageBuilder {

    /**
     *
     * @param builderConfig
     * @param inMessage
     */
    public AckMessageBuilder(BuilderConfig builderConfig, Message inMessage) {
        super(builderConfig, inMessage);
    }

    /**
     *
     * @param responseText
     * @param errorText
     * @param errorCode
     * @return
     * @throws HL7Exception
     */
    public Message buildAck(String responseText, String errorText, String errorCode) throws HL7Exception {
        Message inMessage = this.getInMessage();
        Segment inHeader = (Segment) inMessage.get("MSH");
        Message retVal;
        try {
            // FIXME: use errorCode????
            retVal = DefaultApplication.makeACK(inHeader);
            Terser terser = new Terser(retVal);
            if (errorText == null) {
                terser.set("/.MSA-3-1", responseText);
            } else {
                //logger.error("ErrorText = " + errorText);
                terser.set("/.MSA-1", "AR");
                terser.set("/.MSA-3-1", errorText);
                // ERR|^^^207&Application Internal Error&HL70357
                terser.set("/.ERR-1-4-1", "207");
                terser.set("/.ERR-1-4-2", "Application Internal Error");
                terser.set("/.ERR-1-4-3", "HL70357");
            }
        } catch (IOException e) {
            throw new HL7Exception(e);
        }
        return retVal;
    }
}
