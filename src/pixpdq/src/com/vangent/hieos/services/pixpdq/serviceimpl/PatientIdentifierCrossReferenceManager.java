/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2010 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.pixpdq.serviceimpl;

import com.vangent.hieos.services.pixpdq.transactions.PIXRequestHandler;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class PatientIdentifierCrossReferenceManager extends PIXPDQServiceBaseImpl {

    private final static Logger logger = Logger.getLogger(PatientIdentifierCrossReferenceManager.class);

    /**
     *
     * @param PRPA_IN201309UV02_Message
     * @return
     */
    public OMElement PatientRegistryGetIdentifiersQuery(OMElement request) throws AxisFault {
        beginTransaction("GetIdentifiersQuery (PIXV3)", request);
        validateWS();
        validateNoMTOM();
        PIXRequestHandler handler = new PIXRequestHandler(this.log_message);
        OMElement result = handler.run(request, PIXRequestHandler.MessageType.PatientRegistryGetIdentifiersQuery);
        endTransaction(handler.getStatus());
        return result;
    }

    /**
     * 
     * @param PRPA_IN201301UV02_Message
     * @return
     */
    public OMElement PatientRegistryRecordAdded(OMElement PRPA_IN201301UV02_Message) throws AxisFault {
        beginTransaction("PIDFEED.Add (PIXV3)", PRPA_IN201301UV02_Message);
        validateWS();
        validateNoMTOM();
        PIXRequestHandler handler = new PIXRequestHandler(this.log_message);
        OMElement result = handler.run(PRPA_IN201301UV02_Message, PIXRequestHandler.MessageType.PatientRegistryRecordAdded);
        endTransaction(handler.getStatus());
        return result;
    }
}
