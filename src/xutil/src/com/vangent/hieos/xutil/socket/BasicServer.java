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
package com.vangent.hieos.xutil.socket;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;

/**
 *
 * @author Adeola Odunlami
 */
public abstract class BasicServer {

    private static final Logger log = Logger.getLogger(BasicServer.class);
    public final ExecutorService pool;
    public final ServerProperties props;

    /**
     *
     * @param poolSize
     * @param props
     * @throws IOException
     */
    public BasicServer(int poolSize, ServerProperties props) throws IOException {
        this.props = props;
        this.pool = Executors.newFixedThreadPool(poolSize);
    }

    /**
     *
     */
    public abstract void start();

    /**
     *
     * @throws IOException
     */
    public void listen() throws IOException {
    }

    /**
     *
     * @throws IOException
     */
    public void serve() throws IOException {
    }

    /**
     *  Close the Socket
     */
    public void close() {
        log.info("Closing Server ThreadPool");
        pool.shutdown();
    }
}
