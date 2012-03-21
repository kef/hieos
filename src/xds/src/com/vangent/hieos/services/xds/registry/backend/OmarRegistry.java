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
package com.vangent.hieos.services.xds.registry.backend;

//java Imports
import java.io.StringWriter;

//freebxml imports (from omar)
import org.freebxml.omar.common.spi.LifeCycleManager;
import org.freebxml.omar.common.spi.LifeCycleManagerFactory;
import org.freebxml.omar.common.spi.QueryManager;
import org.freebxml.omar.common.spi.QueryManagerFactory;
import org.freebxml.omar.server.util.ServerResourceBundle;
import org.freebxml.omar.server.common.ServerRequestContext;

//ebxml bindings imports
import org.oasis.ebxml.registry.bindings.lcm.ApproveObjectsRequestType;
import org.oasis.ebxml.registry.bindings.lcm.DeprecateObjectsRequestType;
import org.oasis.ebxml.registry.bindings.lcm.RelocateObjectsRequestType;
import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequestType;
import org.oasis.ebxml.registry.bindings.lcm.SetStatusOnObjectsRequestType;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequestType;
import org.oasis.ebxml.registry.bindings.lcm.UndeprecateObjectsRequestType;
import org.oasis.ebxml.registry.bindings.lcm.UpdateObjectsRequestType;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequestType;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;
import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.xml.XMLParser;
import java.sql.Connection;
import java.util.Random;

/**
 *
 * @author nistra (Rewrote by Bernie Thuman)
 */
public class OmarRegistry {

    private final static Logger log = Logger.getLogger(OmarRegistry.class);
    //Instantiate required objects
    private org.freebxml.omar.common.BindingUtility bu = org.freebxml.omar.common.BindingUtility.getInstance();
    //This object is for adhocquery
    private QueryManager qm = QueryManagerFactory.getInstance().getQueryManager();
    //This object is for submitobjectrequest
    private LifeCycleManager lcm = LifeCycleManagerFactory.getInstance().getLifeCycleManager();
    private ServerRequestContext context = null;
    private OMElement request = null;
    private Connection connection = null;

    /**
     *
     * @param request
     */
    public OmarRegistry(OMElement request, Connection connection) {
        this.request = request;
        this.connection = connection;

    }

    /**
     * Processes the Request by dispatching it to a service in the registry.
     */
    public OMElement process()
            throws XdsInternalException {
        OMElement response = null;  // The final response.

        // Convert OMElement to String.
        String requestAsString = request.toString();

        boolean done = false;
        int retries = 0;
        int MAX_RETRIES = 5;
        int MAX_SLEEP_TIME_MILLIS = 1000;  // 1 sec.
        Random rand = new Random();
        while (!done) {
            try {
                // Submit the request to the ebXML Registry.
                RegistryResponseType rr = this.submitToEbXMLRegistry(requestAsString);
                // Now convert the ebXML Registry response to an OMElement.
                response = this.convertResponseToOMElement(rr);
                done = true;
                if (retries > 0) {
                    log.error("++++ ebXML: DEADLOCK RETRY #" + retries + " SUCCESS!! ++++");
                }
            } catch (RegistryDeadlockException e) {
                log.error("++++ ebXML: DEADLOCK DETECTED (# of retries so far = " + retries + ") ++++");
                if (retries == MAX_RETRIES) {
                    done = true;
                    log.error("++++ ebXML: ALL DEADLOCK RETRIES FAILED ++++");
                    throw new XdsInternalException("ebXML EXCEPTION: " + e.getMessage());
                } else {
                    try {
                        ++retries;
                        // Now sleep for a little period (<= MAX_SLEEP_TIME_MILLIS.
                        int delay = rand.nextInt(MAX_SLEEP_TIME_MILLIS);
                        log.error("++++ ebXML: DEADLOCK RETRY #" + retries + "(sleeping " + delay + " msecs)");
                        Thread.sleep(delay);
                    } catch (InterruptedException ex) {
                        // Do nothing here - just continue - still count as a retry.
                    }
                }
            }
        }
        return response;
    }

    /**
     *
     * @param requestAsString
     * @throws XdsInternalException
     */
    private RegistryResponseType submitToEbXMLRegistry(String requestAsString) throws XdsInternalException, RegistryDeadlockException {
        RegistryResponseType rr = null;
        try {
            // Create the ebXML request object to submit to ebXML Registry (OMAR).
            Object requestObject = bu.getRequestObject(request.getLocalName(), requestAsString);

            // Create the ServerRequestContext to use.
            context = new ServerRequestContext((RegistryRequestType) requestObject);
            context.setConnection(connection);
        } catch (Exception e) {
            log.error("**ebXML EXCEPTION**", e);
            throw new XdsInternalException("ebXML EXCEPTION: " + e.getMessage());
        }

        // Now determine what ebXML Registry action to invoke.
        try {
            RegistryRequestType message = context.getCurrentRegistryRequest();
            long startTime = System.currentTimeMillis();
            if (message instanceof AdhocQueryRequestType) {
                log.trace("OMAR: submitAdhocQuery ...");
                rr = qm.submitAdhocQuery(context);
                log.trace("OMAR: submitAdhocQuery ELAPSED TIME: " + new Long(System.currentTimeMillis() - startTime).toString());
            } else if (message instanceof SubmitObjectsRequestType) {
                log.trace("OMAR: submitObjects ...");
                rr = lcm.submitObjects(context);
                log.trace("OMAR: submitObjects ELAPSED TIME: " + new Long(System.currentTimeMillis() - startTime).toString());
            } else if (message instanceof ApproveObjectsRequestType) {
                log.trace("OMAR: approveObjects ...");
                rr = lcm.approveObjects(context);
                log.trace("OMAR: approveObjects ELAPSED TIME: " + new Long(System.currentTimeMillis() - startTime).toString());
            } else if (message instanceof DeprecateObjectsRequestType) {
                log.trace("OMAR: deprecateObjects ...");
                rr = lcm.deprecateObjects(context);
                log.trace("OMAR: deprecateObjects ELAPSED TIME: " + new Long(System.currentTimeMillis() - startTime).toString());
            } else if (message instanceof SetStatusOnObjectsRequestType) {
                //throw new XdsInternalException("SetStatusOnObjectsRequest - not supported by Registry");
                rr = lcm.setStatusOnObjects(context);
                log.trace("OMAR: SetStatusOnObjectsRequest ELAPSED TIME: " + new Long(System.currentTimeMillis() - startTime).toString());
            } else if (message instanceof UndeprecateObjectsRequestType) {
                throw new XdsInternalException("UndeprecateObjectsRequest - not supported by Registry");
            } else if (message instanceof RemoveObjectsRequestType) {
                throw new XdsInternalException("RemoveObjectsRequest - not supported by Registry");
            } else if (message instanceof UpdateObjectsRequestType) {
                throw new XdsInternalException("UpdateObjectsRequest - not supported by Registry");
            } else if (message instanceof RelocateObjectsRequestType) {
                throw new XdsInternalException("RelocateObjectsRequest - not supported by Registry");
            } else {
                throw new XdsInternalException(ServerResourceBundle.getInstance().
                        getString("message.unknownRequest")
                        + message.getClass().getName());
            }
        } catch (Exception e) {
            log.error("**ebXML EXCEPTION**", e);
            if (e.getMessage().contains("deadlock")) {
                log.error("+++++++++ ebXML: DEADLOCK DETECTED ++++++++++++", e);
                throw new RegistryDeadlockException(e.getMessage());
            }
            throw new XdsInternalException("ebXML EXCEPTION: " + e.getMessage());
        }
        return rr;
    }

    /**
     * Convert ebXML Registry response to OMElement.
     *
     * @param rr ebXML Registry response (RegistryResponseType)
     * @return OMElement
     * @throws XdsInternalException
     */
    private OMElement convertResponseToOMElement(RegistryResponseType rr) throws XdsInternalException {
        OMElement response = null;
        try {
            StringWriter sw = new StringWriter();
            javax.xml.bind.Marshaller marshaller = bu.rsFac.createMarshaller();
            /* NOTE (BHT): SHOULD NOT NEED TO FORMAT
            marshaller.setProperty(
            javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
            Boolean.TRUE); */
            marshaller.marshal(rr, sw);
            //Now get the RegistryResponse as a String
            String respStr = sw.toString();
            // Convert the response to OMElement to send the response
            response = XMLParser.stringToOM(respStr);
        } catch (Exception e) {
            log.error("**ebXML EXCEPTION**", e);
            throw new XdsInternalException("ebXML Internal Exception: " + e.getMessage());
        }
        return response;
    }

    /**
     * Used to signal a "deadlock" situation.
     */
    public class RegistryDeadlockException extends Exception {

        /**
         *
         * @param msg
         */
        public RegistryDeadlockException(String msg) {
            super(msg);
        }

        /**
         *
         * @param msg
         * @param cause
         */
        public RegistryDeadlockException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
}
