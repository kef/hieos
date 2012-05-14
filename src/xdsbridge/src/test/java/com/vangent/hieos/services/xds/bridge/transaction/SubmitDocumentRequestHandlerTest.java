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


package com.vangent.hieos.services.xds.bridge.transaction;

import com.vangent.hieos.services.xds.bridge.client.MockRegistryClient;
import com.vangent.hieos.services.xds.bridge.client.MockRepositoryClient;
import com.vangent.hieos.services.xds.bridge.client.XDSDocumentRegistryClient;
import com.vangent.hieos.services.xds.bridge.client.XDSDocumentRepositoryClient;
import com.vangent.hieos.services.xds.bridge.model.SubmitDocumentResponse
    .Status;
import com.vangent.hieos.services.xds.bridge.support.URIConstants;
import com.vangent.hieos.services.xds.bridge.support.XDSBridgeConfig;
import com.vangent.hieos.services.xds.bridge.support
    .XDSBridgeServiceContext;
import com.vangent.hieos.services.xds.bridge.transactions
    .SubmitDocumentRequestHandler;
import com.vangent.hieos.services.xds.bridge.utils.DebugUtils;
import com.vangent.hieos.services.xds.bridge.utils.JUnitHelper;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import com.vangent.hieos.xutil.xml.XPathHelper;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.util.XMLUtils;
import org.apache.log4j.Logger;

import org.junit.Test;

import org.w3c.dom.Element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;


/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-13
 * @author         Vangent
 */
public class SubmitDocumentRequestHandlerTest {

    /** Field description */
    private static final Logger logger =
        Logger.getLogger(SubmitDocumentRequestHandlerTest.class);

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void addPatientIdentifierExceptionTest() throws Exception {

        XDSBridgeConfig cfg = JUnitHelper.createXDSBridgeConfig();

        XLogMessage logMessage = null;

        OMElement existSample =
            JUnitHelper.fileToOMElement(
                "messages/AddPIDResponse-Sample-Failure.xml");

        XDSDocumentRegistryClient regClient = new MockRegistryClient(true,
                                                  false, existSample);

        OMElement reporesp = JUnitHelper.fileToOMElement(
                                 "messages/PnRResponse-Sample-Success.xml");

        XDSDocumentRepositoryClient repoClient =
            new MockRepositoryClient(false, false, reporesp);

        XDSBridgeServiceContext context = new XDSBridgeServiceContext(cfg,
                                              regClient, repoClient);

        SubmitDocumentRequestHandler handler =
            new SubmitDocumentRequestHandler(logMessage, context);

        String[] documentIds = new String[] { "1.2.3.4.5.1000" };

        OMElement request =
            JUnitHelper.createOMRequest(JUnitHelper.SALLY_GRANT, 1,
                                        documentIds);

        boolean exception = false;
        OMElement response = null;

        try {

            response = handler.run(request);
            logger.debug(DebugUtils.toPrettyString(response));

        } catch (Exception e) {

            logger.error(e, e);
            exception = true;
        }

        assertFalse(exception);
        assertNotNull(response);

        validateStatus(response, Status.Failure);
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void addPatientIdentifierFailureExistsTest() throws Exception {

        XDSBridgeConfig cfg = JUnitHelper.createXDSBridgeConfig();

        XLogMessage logMessage = null;

        OMElement existSample =
            JUnitHelper.fileToOMElement(
                "messages/AddPIDResponse-Sample-Failure-Exists.xml");

        XDSDocumentRegistryClient regClient = new MockRegistryClient(false,
                                                  false, existSample);

        OMElement reporesp = JUnitHelper.fileToOMElement(
                                 "messages/PnRResponse-Sample-Success.xml");

        XDSDocumentRepositoryClient repoClient =
            new MockRepositoryClient(false, false, reporesp);

        XDSBridgeServiceContext context = new XDSBridgeServiceContext(cfg,
                                              regClient, repoClient);

        SubmitDocumentRequestHandler handler =
            new SubmitDocumentRequestHandler(logMessage, context);

        String[] documentIds = new String[] { "1.2.3.4.5.1000" };

        OMElement request =
            JUnitHelper.createOMRequest(JUnitHelper.SALLY_GRANT, 1,
                                        documentIds);

        boolean exception = false;
        OMElement response = null;

        try {

            response = handler.run(request);
            logger.debug(DebugUtils.toPrettyString(response));

        } catch (Exception e) {

            logger.error(e, e);
            exception = true;
        }

        assertFalse(exception);
        assertNotNull(response);

        validateStatus(response, Status.Success);
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void addPatientIdentifierFailureTest() throws Exception {

        XDSBridgeConfig cfg = JUnitHelper.createXDSBridgeConfig();

        XLogMessage logMessage = null;

        OMElement existSample =
            JUnitHelper.fileToOMElement(
                "messages/AddPIDResponse-Sample-Failure.xml");

        XDSDocumentRegistryClient regClient = new MockRegistryClient(false,
                                                  false, existSample);

        OMElement reporesp = JUnitHelper.fileToOMElement(
                                 "messages/PnRResponse-Sample-Success.xml");

        XDSDocumentRepositoryClient repoClient =
            new MockRepositoryClient(false, false, reporesp);

        XDSBridgeServiceContext context = new XDSBridgeServiceContext(cfg,
                                              regClient, repoClient);

        SubmitDocumentRequestHandler handler =
            new SubmitDocumentRequestHandler(logMessage, context);

        String[] documentIds = new String[] { "1.2.3.4.5.1000" };

        OMElement request =
            JUnitHelper.createOMRequest(JUnitHelper.SALLY_GRANT, 1,
                                        documentIds);

        boolean exception = false;
        OMElement response = null;

        try {

            response = handler.run(request);
            logger.debug(DebugUtils.toPrettyString(response));

        } catch (Exception e) {

            logger.error(e, e);
            exception = true;
        }

        assertFalse(exception);
        assertNotNull(response);

        // a PID failure no longer causes a full failure
        // validateStatus(response, Status.Failure);
        validateStatus(response, Status.Success);
    }

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws Exception
     */
    private XDSDocumentRegistryClient createSuccessRegistryClient()
            throws Exception {

        OMElement success = JUnitHelper.fileToOMElement(
                                "messages/AddPIDResponse-Sample-Success.xml");

        return new MockRegistryClient(false, false, success);
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void submitDocumentRequestAllFailureResponsesTest()
            throws Exception {

        XDSBridgeConfig cfg = JUnitHelper.createXDSBridgeConfig();

        XLogMessage logMessage = null;

        XDSDocumentRegistryClient regClient = createSuccessRegistryClient();

        OMElement reporesp = JUnitHelper.fileToOMElement(
                                 "messages/PnRResponse-Sample-Failure.xml");

        XDSDocumentRepositoryClient repoClient =
            new MockRepositoryClient(false, false, reporesp);

        XDSBridgeServiceContext context = new XDSBridgeServiceContext(cfg,
                                              regClient, repoClient);

        SubmitDocumentRequestHandler handler =
            new SubmitDocumentRequestHandler(logMessage, context);

        String[] documentIds = new String[] { "1.2.3.4.5.1000",
                "1.2.3.4.5.2000", "1.2.3.4.5.3000", "1.2.3.4.5.4000",
                "1.2.3.4.5.5000" };

        OMElement request =
            JUnitHelper.createOMRequest(JUnitHelper.SALLY_GRANT, 5,
                                        documentIds);

        boolean exception = false;
        OMElement response = null;

        try {

            response = handler.run(request);
            logger.debug(DebugUtils.toPrettyString(response));

        } catch (Exception e) {

            logger.error(e, e);
            exception = true;
        }

        assertFalse(exception);
        assertNotNull(response);

        validateStatus(response, Status.Failure);
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void submitDocumentRequestAllSuccessResponsesTest()
            throws Exception {

        XDSBridgeConfig cfg = JUnitHelper.createXDSBridgeConfig();

        XLogMessage logMessage = null;

        XDSDocumentRegistryClient regClient = createSuccessRegistryClient();

        OMElement reporesp = JUnitHelper.fileToOMElement(
                                 "messages/PnRResponse-Sample-Success.xml");

        XDSDocumentRepositoryClient repoClient =
            new MockRepositoryClient(false, false, reporesp);

        XDSBridgeServiceContext context = new XDSBridgeServiceContext(cfg,
                                              regClient, repoClient);

        SubmitDocumentRequestHandler handler =
            new SubmitDocumentRequestHandler(logMessage, context);

        String[] documentIds = new String[] { "1.2.3.4.5.1000",
                "1.2.3.4.5.2000", "1.2.3.4.5.3000", "1.2.3.4.5.4000",
                "1.2.3.4.5.5000" };

        OMElement request =
            JUnitHelper.createOMRequest(JUnitHelper.SALLY_GRANT, 5,
                                        documentIds);

        boolean exception = false;
        OMElement response = null;

        try {

            response = handler.run(request);
            logger.debug(DebugUtils.toPrettyString(response));

        } catch (Exception e) {

            logger.error(e, e);
            exception = true;
        }

        assertFalse(exception);
        assertNotNull(response);

        validateStatus(response, Status.Success);
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void submitDocumentRequestAlwaysExceptionTest() throws Exception {

        XDSBridgeConfig cfg = JUnitHelper.createXDSBridgeConfig();

        XLogMessage logMessage = null;

        XDSDocumentRegistryClient regClient = createSuccessRegistryClient();

        OMElement reporesp = JUnitHelper.fileToOMElement(
                                 "messages/PnRResponse-Sample-Success.xml");

        XDSDocumentRepositoryClient repoClient = new MockRepositoryClient(true,
                                                     false, reporesp);

        XDSBridgeServiceContext context = new XDSBridgeServiceContext(cfg,
                                              regClient, repoClient);

        SubmitDocumentRequestHandler handler =
            new SubmitDocumentRequestHandler(logMessage, context);

        String[] documentIds = new String[] { "1.2.3.4.5.1000",
                "1.2.3.4.5.2000", "1.2.3.4.5.3000", "1.2.3.4.5.4000",
                "1.2.3.4.5.5000" };

        OMElement request =
            JUnitHelper.createOMRequest(JUnitHelper.SALLY_GRANT, 5,
                                        documentIds);

        boolean exception = false;
        OMElement response = null;

        try {

            response = handler.run(request);
            logger.debug(DebugUtils.toPrettyString(response));

        } catch (Exception e) {

            logger.error(e, e);
            exception = true;
        }

        assertFalse(exception);
        assertNotNull(response);

        validateStatus(response, Status.Failure);
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void submitDocumentRequestBuilderFailureTest() throws Exception {

        OMElement request =
            JUnitHelper.fileToOMElement("messages/test-sdr4-missing-stuff.xml");

        XDSBridgeConfig cfg = JUnitHelper.createXDSBridgeConfig();

        XLogMessage logMessage = null;

        XDSBridgeServiceContext context = new XDSBridgeServiceContext(cfg,
                                              null, null);

        SubmitDocumentRequestHandler handler =
            new SubmitDocumentRequestHandler(logMessage, context);

        boolean exception = false;
        OMElement response = null;

        try {

            response = handler.run(request);
            logger.debug(DebugUtils.toPrettyString(response));

        } catch (Exception e) {

            logger.error(e, e);
            exception = true;
        }

        assertFalse(exception);
        assertNotNull(response);

        validateStatus(response, Status.Failure);
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void submitDocumentRequestPartialExceptionTest() throws Exception {

        XDSBridgeConfig cfg = JUnitHelper.createXDSBridgeConfig();

        XLogMessage logMessage = null;

        XDSDocumentRegistryClient regClient = createSuccessRegistryClient();

        OMElement reporesp = JUnitHelper.fileToOMElement(
                                 "messages/PnRResponse-Sample-Success.xml");

        XDSDocumentRepositoryClient repoClient =
            new MockRepositoryClient(false, true, reporesp);

        XDSBridgeServiceContext context = new XDSBridgeServiceContext(cfg,
                                              regClient, repoClient);

        SubmitDocumentRequestHandler handler =
            new SubmitDocumentRequestHandler(logMessage, context);

        String[] documentIds = new String[] { "1.2.3.4.5.1000",
                "1.2.3.4.5.2000", "1.2.3.4.5.3000", "1.2.3.4.5.4000",
                "1.2.3.4.5.5000" };

        OMElement request =
            JUnitHelper.createOMRequest(JUnitHelper.SALLY_GRANT, 5,
                                        documentIds);

        boolean exception = false;
        OMElement response = null;

        try {

            response = handler.run(request);
            logger.debug(DebugUtils.toPrettyString(response));

        } catch (Exception e) {

            logger.error(e, e);
            exception = true;
        }

        assertFalse(exception);
        assertNotNull(response);

        validateStatus(response, Status.PartialSuccess);
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void submitDocumentRequestSuccessTest() throws Exception {

        XDSBridgeConfig cfg = JUnitHelper.createXDSBridgeConfig();

        XLogMessage logMessage = null;

        XDSDocumentRegistryClient regClient = createSuccessRegistryClient();

        OMElement reporesp = JUnitHelper.fileToOMElement(
                                 "messages/PnRResponse-Sample-Success.xml");

        XDSDocumentRepositoryClient repoClient =
            new MockRepositoryClient(false, false, reporesp);

        XDSBridgeServiceContext context = new XDSBridgeServiceContext(cfg,
                                              regClient, repoClient);

        SubmitDocumentRequestHandler handler =
            new SubmitDocumentRequestHandler(logMessage, context);

        OMElement request =
            JUnitHelper.createOMRequest(JUnitHelper.SALLY_GRANT, 1,
                                        new String[] { "1.2.3.4.5.1000" });

        boolean exception = false;
        OMElement response = null;

        try {

            response = handler.run(request);
            logger.debug(DebugUtils.toPrettyString(response));

        } catch (Exception e) {

            logger.error(e, e);
            exception = true;
        }

        assertFalse(exception);
        assertNotNull(response);

        validateStatus(response, Status.Success);
    }

    /**
     * Method description
     *
     *
     * @param response
     * @param validStatus
     *
     *
     * @throws Exception
     */
    private void validateStatus(OMElement response, Status validStatus)
            throws Exception {

        // validate against the schema
        // Create a SchemaFactory capable of understanding WXS schemas.
        SchemaFactory factory =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        // Load a WXS schema, represented by a Schema instance.
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream xsdStream =
            classLoader.getResourceAsStream("META-INF/XDSBridge.xsd");

        assertNotNull(xsdStream);

        Source schemaFile = new StreamSource(xsdStream);
        Schema schema = factory.newSchema(schemaFile);

        // validate
        Validator validator = schema.newValidator();

        Element w3element = XMLUtils.toDOM(response);

        validator.validate(new DOMSource(w3element));

        // validate the status
        String uri = URIConstants.XDSBRIDGE_URI;
        String expr = "/@status";
        String status = XPathHelper.stringValueOf(response, expr, uri);

        assertEquals(validStatus.toString(), status);
    }
}
