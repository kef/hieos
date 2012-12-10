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
package com.vangent.hieos.services.pixpdqv2.transactions;

import com.vangent.hieos.xutil.services.framework.XBaseTransaction;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public abstract class RequestHandler extends XBaseTransaction {

    private final static Logger logger = Logger.getLogger(RequestHandler.class);

    /**
     *
     */
    protected RequestHandler() {
        // Do nothing.
    }

    /**
     *
     * @param log_message
     */
    public RequestHandler(XLogMessage log_message) {
        this.log_message = log_message;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean getStatus() {
        return log_message.isPass();
    }
}
