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
package com.vangent.hieos.empi.impl.base;

import com.vangent.hieos.empi.validator.Validator;
import com.vangent.hieos.empi.config.EMPIConfig;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.empi.persistence.PersistenceManager;
import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.empi.api.EMPINotification;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class BaseHandler {

    private static final Logger logger = Logger.getLogger(BaseHandler.class);
    private XConfigActor configActor = null;
    private PersistenceManager persistenceManager = null;
    private DeviceInfo senderDeviceInfo = null;

    /**
     * 
     * @param configActor
     * @param persistenceManager
     * @param senderDeviceInfo
     */
    protected BaseHandler(XConfigActor configActor,
            PersistenceManager persistenceManager, DeviceInfo senderDeviceInfo) {
        this.configActor = configActor;
        this.persistenceManager = persistenceManager;
        this.senderDeviceInfo = senderDeviceInfo;
    }

    /**
     * 
     * @return
     */
    protected Validator getValidator() {
        return new Validator(this.persistenceManager, this.senderDeviceInfo);
    }

    /**
     *
     * @return
     */
    protected XConfigActor getConfigActor() {
        return this.configActor;
    }

    /**
     *
     * @return
     */
    public DeviceInfo getSenderDeviceInfo() {
        return senderDeviceInfo;
    }

    /**
     * 
     * @return
     */
    protected PersistenceManager getPersistenceManager() {
        return persistenceManager;
    }

    /**
     * 
     * @param notification
     * @param enterpriseSubjectId
     * @throws EMPIException
     */
    protected void addSubjectToNotification(EMPINotification notification, String enterpriseSubjectId) throws EMPIException {
        EMPIConfig empiConfig = EMPIConfig.getInstance();
        if (!empiConfig.isUpdateNotificationEnabled()) {
            // Notifications are turned off -- get out now.
            return; // Early exit!!
        }
        PersistenceManager pm = this.getPersistenceManager();
        Subject subject = pm.loadEnterpriseSubjectIdentifiersAndNamesOnly(enterpriseSubjectId);
        notification.addSubject(subject);
    }
}
