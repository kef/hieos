/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2008-2009 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.pixpdq.serviceimpl;

import com.vangent.hieos.services.pixpdq.transactions.PIXPDSRequestHandler;
import com.vangent.hieos.services.pixpdq.transactions.PIXRequestHandler;
import com.vangent.hieos.xutil.exception.XdsFormatException;
import com.vangent.hieos.xutil.services.framework.XAbstractService;
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
        try {
            OMElement startup_error = beginTransaction("GetIdentifiers (PIXV3)", request, XAbstractService.ActorType.PIXMGR);
            if (startup_error != null) {
                return startup_error;
            }
            validateWS();
            validateNoMTOM();
            PIXRequestHandler handler = new PIXRequestHandler(this.log_message);
            OMElement result = handler.run(request, PIXPDSRequestHandler.MessageType.PatientRegistryGetIdentifiersQuery);
            endTransaction(true);
            return result;
        } catch (XdsFormatException ex) {
            log_message.addErrorParam("EXCEPTION", ex.getMessage());
            log_message.setPass(false);
            endTransaction(false);
            throw new AxisFault(ex.getMessage());
        } catch (Exception ex) {
            throw getAxisFault(ex);
        }
    }

    /**
     * 
     * @param PRPA_IN201301UV02_Message
     * @return
     */
    public OMElement PatientRegistryRecordAdded(OMElement PRPA_IN201301UV02_Message) throws AxisFault {
        try {
            OMElement startup_error = beginTransaction("PIDFEED.Add (PIXV3)", PRPA_IN201301UV02_Message, XAbstractService.ActorType.PIXMGR);
            if (startup_error != null) {
                return startup_error;
            }
            validateWS();
            validateNoMTOM();
            PIXRequestHandler handler = new PIXRequestHandler(this.log_message);
            OMElement result = handler.run(PRPA_IN201301UV02_Message, PIXPDSRequestHandler.MessageType.PatientRegistryRecordAdded);
            endTransaction(true);
            return result;
        } catch (Exception ex) {
            throw getAxisFault(ex);
        }
    }
}
