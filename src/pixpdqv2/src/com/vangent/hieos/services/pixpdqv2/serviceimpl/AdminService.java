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
package com.vangent.hieos.services.pixpdqv2.serviceimpl;

import com.vangent.hieos.hl7v2util.acceptor.config.AcceptorConfig;
import com.vangent.hieos.hl7v2util.acceptor.impl.HL7v2Acceptor;
import com.vangent.hieos.services.pixpdqv2.transactions.AdminRequestHandler;
import com.vangent.hieos.xutil.atna.ATNAAuditEvent;
import com.vangent.hieos.xutil.exception.SOAPFaultException;
import com.vangent.hieos.xutil.services.framework.XAbstractService;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class AdminService extends XAbstractService {

    private final static Logger logger = Logger.getLogger(AdminService.class);
    private static XConfigActor config = null;          // Singleton.
    private static HL7v2Acceptor hl7v2Acceptor = null;  // Singleton.

    @Override
    protected XConfigActor getConfigActor() {
        return config;
    }

    /**
     *
     * @param request
     * @return
     * @throws AxisFault
     */
    public OMElement GetConfig(OMElement request) throws AxisFault {
        OMElement response = null;
        try {
            beginTransaction("GetConfig (PIXPDQV2)", request);
            validateWS();
            validateNoMTOM();
            AdminRequestHandler handler = new AdminRequestHandler(this.log_message);
            handler.setConfigActor(config);
            response = handler.run(request, AdminRequestHandler.MessageType.GetConfig);
            endTransaction(handler.getStatus());
        } catch (SOAPFaultException ex) {
            throwAxisFault(ex);
        }
        return response;

    }

    /**
     * This will be called during the deployment time of the service.
     * Irrespective of the service scope this method will be called
     */
    @Override
    public void startup() {
        logger.info("PIXPDQv2:AdminService:startup()");
        try {
            //XConfig xconf;
            //xconf = XConfig.getInstance();
            //XConfigObject homeCommunity = xconf.getHomeCommunityConfig();

            // FIXME: Do we need XConfig here?
            //config = (XConfigActor) homeCommunity.getXConfigObjectWithName("pix", XConfig.PIX_MANAGER_TYPE);
            config = null;  // XConfig not really used here.
            
            // Start up listener.
            String empiConfigDir = XConfig.getConfigLocation(XConfig.ConfigItem.EMPI_DIR);
            AcceptorConfig acceptorConfig = new AcceptorConfig(
                    empiConfigDir + "/PIXPDQHL7v2AcceptorConfig.xml");
            AdminService.hl7v2Acceptor = new HL7v2Acceptor(acceptorConfig);
            AdminService.hl7v2Acceptor.startup();

        } catch (Exception ex) {
            logger.fatal("Unable to get configuration for service", ex);
        }
        this.ATNAlogStart(ATNAAuditEvent.ActorType.PIX_MANAGER_V2);
        this.ATNAlogStart(ATNAAuditEvent.ActorType.PATIENT_DEMOGRAPHICS_SUPPLIER_V2);
    }

    /**
     * This will be called during the system shut down time. Irrespective
     * of the service scope this method will be called
     */
    @Override
    public void shutdown() {
        logger.info("PIXPDQv2:AdminService:shutdown()");
        AdminService.hl7v2Acceptor.shutdown();
        this.ATNAlogStop(ATNAAuditEvent.ActorType.PIX_MANAGER_V2);
        this.ATNAlogStop(ATNAAuditEvent.ActorType.PATIENT_DEMOGRAPHICS_SUPPLIER_V2);
    }
}
