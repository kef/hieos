/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2011 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vangent.hieos.xutil.soap;

import java.util.UUID;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.log4j.Logger;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-07-18
 * @author         Vangent
 */
public class XUtilSimpleHttpConnectionManager
        extends SimpleHttpConnectionManager {

    /** Field description */
    private static final Logger logger =
        Logger.getLogger(XUtilSimpleHttpConnectionManager.class);

    /** Field description */
    private final String name = UUID.randomUUID().toString();

    /**
     * Constructs ...
     *
     */
    public XUtilSimpleHttpConnectionManager() {
        super();
    }

    /**
     * Constructs ...
     *
     *
     * @param alwaysClose
     */
    public XUtilSimpleHttpConnectionManager(boolean alwaysClose) {
        super(alwaysClose);
    }

    /**
     * Method description
     *
     *
     * @param hostConfiguration
     *
     * @return
     */
    @Override
    public HttpConnection getConnection(HostConfiguration hostConfiguration) {

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Opening connection %s.", this.name));
        }

        return super.getConnection(hostConfiguration);
    }

    /**
     * Method description
     *
     *
     * @param hostConfiguration
     * @param timeout
     *
     * @return
     */
    @Override
    public HttpConnection getConnectionWithTimeout(
            HostConfiguration hostConfiguration, long timeout) {

        if (logger.isDebugEnabled()) {

            logger.debug(String.format("Opening connection %s / Timeout %d.",
                                       this.name, timeout));
        }

        return super.getConnectionWithTimeout(hostConfiguration, timeout);
    }

    /**
     * Method description
     *
     *
     * @param conn
     */
    @Override
    public void releaseConnection(HttpConnection conn) {

        super.releaseConnection(conn);

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Closing connection %s.", this.name));
        }

        // close the connection
        conn.close();
    }
}
