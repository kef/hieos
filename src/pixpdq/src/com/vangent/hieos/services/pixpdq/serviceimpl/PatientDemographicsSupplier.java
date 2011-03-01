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

import com.vangent.hieos.services.pixpdq.transactions.PDSRequestHandler;
import com.vangent.hieos.services.pixpdq.transactions.PIXPDSRequestHandler;
import com.vangent.hieos.xutil.services.framework.XAbstractService;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class PatientDemographicsSupplier extends PIXPDQServiceBaseImpl {

    private final static Logger logger = Logger.getLogger(PatientDemographicsSupplier.class);

    /**
     *
     * @param PRPA_IN201305UV02_Message
     * @return
     */
    public OMElement PatientRegistryFindCandidatesQuery(OMElement request) throws AxisFault {
        try {
            OMElement startup_error = beginTransaction("FindCandidatesQuery (PDQV3)", request, XAbstractService.ActorType.PDS);
            if (startup_error != null) {
                // TBD: FIXUP (XUA should be returning a SOAP fault!)
                return startup_error;
            }
            validateWS();
            validateNoMTOM();
            PDSRequestHandler handler = new PDSRequestHandler(this.log_message);
            OMElement result =
                    handler.run(request,
                    PIXPDSRequestHandler.MessageType.PatientRegistryFindCandidatesQuery);
            endTransaction(true);
            return result;
        } catch (Exception ex) {
            throw getAxisFault(ex);
        }
    }
}
