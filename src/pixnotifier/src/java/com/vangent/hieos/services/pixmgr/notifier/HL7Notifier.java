/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vangent.hieos.services.pixmgr.notifier;

import com.vangent.hieos.pixnotifierutil.config.CrossReferenceConsumerConfig;
import com.vangent.hieos.subjectmodel.DeviceInfo;
import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.xutil.exception.SOAPFaultException;

/**
 *
 * @author Bernie Thuman
 */
abstract public class HL7Notifier {

    private DeviceInfo senderDeviceInfo;
    private CrossReferenceConsumerConfig crossReferenceConsumerConfig;

    /**
     *
     * @param crossReferenceConsumerConfig
     */
    public HL7Notifier(DeviceInfo senderDeviceInfo, CrossReferenceConsumerConfig crossReferenceConsumerConfig) {
        this.senderDeviceInfo = senderDeviceInfo;
        this.crossReferenceConsumerConfig = crossReferenceConsumerConfig;
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
    public CrossReferenceConsumerConfig getCrossReferenceConsumerConfig() {
        return crossReferenceConsumerConfig;
    }

    /**
     * 
     * @param notificationSubject
     */
    abstract public void sendNotification(Subject notificationSubject);
}
