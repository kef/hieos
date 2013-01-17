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

package com.vangent.hieos.hl7v2util.acceptor.impl;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.model.Message;

/**
 *
 * @author Bernie Thuman
 */
public interface MessageHandler {
     /**
     * Uses the contents of the message for whatever purpose the Application
     * has for this message, and returns an appropriate response message.
      * @param connection
      * @param in 
      * @return 
      * @throws ApplicationException
      * @throws HL7Exception
      */
    public Message processMessage(Connection connection, Message in) throws ApplicationException, HL7Exception;

    /**
     * Returns true if this Application wishes to accept the message.  By returning
     * true, this Application declares itself the recipient of the message, accepts
     * responsibility for it, and must be able to respond appropriately to the sending system.
     * @param connection
     * @param in
     * @return
     */
    public boolean canProcess(Connection connection, Message in);

}
