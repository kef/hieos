/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2013 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.pixnotifierutil.client;

import com.vangent.hieos.xutil.jms.JMSHandler;
import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 *
 * @author Bernie Thuman
 */
public class PIXNotifierClient {

    /**
     *
     * @param notification
     * @throws PIXNotifierClientException
     */
    public void sendNotification(PIXUpdateNotification notification) throws PIXNotifierClientException {
        // FIXME: configure parameters.
        JMSHandler jms = new JMSHandler("jms/PIXNotifierMsgQFactory", "jms/PIXNotifierMsgQ");
        try {
            jms.createConnectionFactoryFromPool();
            jms.createJMSSession();
            jms.sendMessage(notification);
        } catch (JMSException ex) {
            throw new PIXNotifierClientException("Unable to send PIX notification to PIX Notifier", ex);
        } catch (NamingException ex) {
            throw new PIXNotifierClientException("Unable to send PIX notification to PIX Notifier", ex);
        } finally {
            jms.close();
        }
    }
}
