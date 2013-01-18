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

import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class ConnectionManager {

    private static final Logger logger = Logger.getLogger(ConnectionManager.class);
    private ConcurrentLinkedQueue<Connection> connections;

    /**
     * 
     */
    public ConnectionManager() {
        this.connections = new ConcurrentLinkedQueue<Connection>();
    }

    /**
     *
     * @return
     */
    public int getConnectionCount() {
        return this.connections.size();
    }

    /**
     *
     * @param connection
     */
    public void addConnection(Connection connection) {
        this.connections.add(connection);
    }

    /**
     * 
     * @param connection
     */
    public void removeConnection(Connection connection) {
        // Cleanup connection resources.
        connection.close();

        // Remove knowledge of this connection.
        this.connections.remove(connection);
    }

    /**
     * 
     */
    public void closeConnections() {
        // Close any remaining open connections.
        for (Connection connection : this.connections) {
            connection.close();
        }
        this.connections.removeAll(this.connections);
    }
}
