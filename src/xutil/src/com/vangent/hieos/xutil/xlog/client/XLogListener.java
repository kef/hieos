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
package com.vangent.hieos.xutil.xlog.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class XLogListener implements Runnable {

    private final static Logger logger = Logger.getLogger(XLogListener.class);
    private LinkedBlockingQueue<XLogMessage> logMessageQueue;
    private ExecutorService executorService;

    /**
     *
     */
    public XLogListener() {
    }

    /**
     * 
     */
    public void startup() {
        // Establish queue.
        logMessageQueue = new LinkedBlockingQueue<XLogMessage>();   // Start thread

        // Create log listener and spawn thread.
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(this);
    }

    /**
     * 
     * @return
     */
    public LinkedBlockingQueue<XLogMessage> getLogMessageQueue() {
        return logMessageQueue;
    }

    /**
     *
     */
    public void shutdownAndAwaitTermination() {
        // Try to shutdown main listener now.
        logger.info("Shutting down XLogListener thread ...");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
                // Wait a while for tasks to respond to being cancelled
                if (!executorService.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                    logger.error("XLogListener thread did not terminate");
                } else {
                    logger.warn("XLogListener thread terminated after forced shutdown");
                }
            } else {
                logger.info("XLogListener thread terminated gracefully!");
            }
        } catch (InterruptedException ex) {// (Re-)Cancel if current thread also interrupted
            executorService.shutdownNow();
            logger.warn("XLogListener thread terminated after forced shutdown!");
            // Preserve interrupt status
            // FIXME?
            //Thread.currentThread().interrupt();
        }
    }

    /**
     *
     */
    public void run() {
        logger.info("Starting XLogListener thread (Thread ID = " + Thread.currentThread().getId() + ")");
        boolean running = true;
        while (running) {
            try {
                logger.info("XLogListener .. waiting for log message (Thread ID = " + Thread.currentThread().getId() + ")");
                XLogMessage messageData = logMessageQueue.take();
                logger.info("XLogListener .. pulled message from queue (Thread ID = " + Thread.currentThread().getId() + ")");
                XLogMessageDAO dao = new XLogMessageDAO();
                dao.persist(messageData);
            } catch (InterruptedException ex) {
                running = false;
            } catch (Exception ex) {
                logger.error("XLogListener .. exception .. continuing (Thread ID = " + Thread.currentThread().getId() + ")", ex);
            }
        }
        logger.info("Stopped XLogListener thread (Thread ID = " + Thread.currentThread().getId() + ")");
    }
}
