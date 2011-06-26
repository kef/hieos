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
import com.vangent.hieos.services.xds.bridge.mapper.ContentParser;
import com.vangent.hieos.services.xds.bridge.mapper.MapperFactory;
import com.vangent.hieos.services.xds.bridge.model.SubmitDocumentRequestBuilder;
import com.vangent.hieos.services.xds.bridge.model.SubmitDocumentResponse
    .Status;
import com.vangent.hieos.services.xds.bridge.serviceimpl.XDSBridgeConfig;
import com.vangent.hieos.services.xds.bridge.transactions
    .SubmitDocumentRequestHandler;
import com.vangent.hieos.services.xds.bridge.utils.DebugUtils;
import com.vangent.hieos.services.xds.bridge.utils.JUnitHelper;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import com.vangent.hieos.xutil.xml.XPathHelper;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.log4j.Logger;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-13
 * @author         Jim Horner
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
    public void submitDocumentRequestAllFailureResponsesTest()
            throws Exception {

        XDSBridgeConfig cfg = JUnitHelper.createXDSBridgeConfig();

        XLogMessage logMessage = null;
        SubmitDocumentRequestBuilder builder =
            new SubmitDocumentRequestBuilder(cfg);
        
        MapperFactory mapFactory = new MapperFactory(cfg, new ContentParser());

        XDSDocumentRegistryClient regClient = new MockRegistryClient(false,
                                                  false, null);

        OMElement reporesp = JUnitHelper.fileToOMElement(
                                 "messages/PnRResponse-Sample-Failure.xml");

        XDSDocumentRepositoryClient repoClient =
            new MockRepositoryClient(false, false, reporesp);

        SubmitDocumentRequestHandler handler =
            new SubmitDocumentRequestHandler(logMessage, builder, mapFactory,
                regClient, repoClient);

        OMElement request =
            JUnitHelper.fileToOMElement("messages/test-sdr5-multi-cda.xml");

        boolean exception = false;
        OMElement response = null;

        try {

            response = handler.run(null, request);
            logger.debug(DebugUtils.toPrettyString(response));

        } catch (Exception e) {

            logger.error(e, e);
            exception = true;
        }

        assertFalse(exception);
        assertNotNull(response);

        OMNamespace ns = response.getNamespace();

        assertEquals(SubmitDocumentRequestBuilder.URI, ns.getNamespaceURI());
        assertEquals("SubmitDocumentResponse", response.getLocalName());
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
        SubmitDocumentRequestBuilder builder =
            new SubmitDocumentRequestBuilder(cfg);

        MapperFactory mapFactory = new MapperFactory(cfg, new ContentParser());

        XDSDocumentRegistryClient regClient = new MockRegistryClient(false,
                                                  false, null);

        OMElement reporesp = JUnitHelper.fileToOMElement(
                                 "messages/PnRResponse-Sample-Success.xml");

        XDSDocumentRepositoryClient repoClient =
            new MockRepositoryClient(false, false, reporesp);

        SubmitDocumentRequestHandler handler =
            new SubmitDocumentRequestHandler(logMessage, builder, mapFactory,
                regClient, repoClient);

        OMElement request =
            JUnitHelper.fileToOMElement("messages/test-sdr5-multi-cda.xml");

        boolean exception = false;
        OMElement response = null;

        try {

            response = handler.run(null, request);
            logger.debug(DebugUtils.toPrettyString(response));

        } catch (Exception e) {

            logger.error(e, e);
            exception = true;
        }

        assertFalse(exception);
        assertNotNull(response);

        OMNamespace ns = response.getNamespace();

        assertEquals(SubmitDocumentRequestBuilder.URI, ns.getNamespaceURI());
        assertEquals("SubmitDocumentResponse", response.getLocalName());
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
        SubmitDocumentRequestBuilder builder =
            new SubmitDocumentRequestBuilder(cfg);

        MapperFactory mapFactory = new MapperFactory(cfg, new ContentParser());

        XDSDocumentRegistryClient regClient = new MockRegistryClient(false,
                                                  false, null);

        OMElement reporesp = JUnitHelper.fileToOMElement(
                                 "messages/PnRResponse-Sample-Success.xml");

        XDSDocumentRepositoryClient repoClient = new MockRepositoryClient(true,
                                                     false, reporesp);

        SubmitDocumentRequestHandler handler =
            new SubmitDocumentRequestHandler(logMessage, builder, mapFactory,
                regClient, repoClient);

        OMElement request =
            JUnitHelper.fileToOMElement("messages/test-sdr5-multi-cda.xml");

        boolean exception = false;
        OMElement response = null;

        try {

            response = handler.run(null, request);
            logger.debug(DebugUtils.toPrettyString(response));

        } catch (Exception e) {

            logger.error(e, e);
            exception = true;
        }

        assertFalse(exception);
        assertNotNull(response);

        OMNamespace ns = response.getNamespace();

        assertEquals(SubmitDocumentRequestBuilder.URI, ns.getNamespaceURI());
        assertEquals("SubmitDocumentResponse", response.getLocalName());
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
        SubmitDocumentRequestBuilder builder =
            new SubmitDocumentRequestBuilder(cfg);


        SubmitDocumentRequestHandler handler =
            new SubmitDocumentRequestHandler(logMessage, builder, null, null,
                null);

        boolean exception = false;
        OMElement response = null;

        try {

            response = handler.run(null, request);
            logger.debug(DebugUtils.toPrettyString(response));

        } catch (Exception e) {

            logger.error(e, e);
            exception = true;
        }

        assertFalse(exception);
        assertNotNull(response);

        OMNamespace ns = response.getNamespace();

        assertEquals(SubmitDocumentRequestBuilder.URI, ns.getNamespaceURI());
        assertEquals("SubmitDocumentResponse", response.getLocalName());
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
        SubmitDocumentRequestBuilder builder =
            new SubmitDocumentRequestBuilder(cfg);

        MapperFactory mapFactory = new MapperFactory(cfg, new ContentParser());

        XDSDocumentRegistryClient regClient = new MockRegistryClient(false,
                                                  false, null);

        OMElement reporesp = JUnitHelper.fileToOMElement(
                                 "messages/PnRResponse-Sample-Success.xml");

        XDSDocumentRepositoryClient repoClient =
            new MockRepositoryClient(false, true, reporesp);

        SubmitDocumentRequestHandler handler =
            new SubmitDocumentRequestHandler(logMessage, builder, mapFactory,
                regClient, repoClient);

        OMElement request =
            JUnitHelper.fileToOMElement("messages/test-sdr5-multi-cda.xml");

        boolean exception = false;
        OMElement response = null;

        try {

            response = handler.run(null, request);
            logger.debug(DebugUtils.toPrettyString(response));

        } catch (Exception e) {

            logger.error(e, e);
            exception = true;
        }

        assertFalse(exception);
        assertNotNull(response);

        OMNamespace ns = response.getNamespace();

        assertEquals(SubmitDocumentRequestBuilder.URI, ns.getNamespaceURI());
        assertEquals("SubmitDocumentResponse", response.getLocalName());
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
        SubmitDocumentRequestBuilder builder =
            new SubmitDocumentRequestBuilder(cfg);

        MapperFactory mapFactory = new MapperFactory(cfg, new ContentParser());

        XDSDocumentRegistryClient regClient = new MockRegistryClient(false,
                                                  false, null);

        OMElement reporesp = JUnitHelper.fileToOMElement(
                                 "messages/PnRResponse-Sample-Success.xml");

        XDSDocumentRepositoryClient repoClient =
            new MockRepositoryClient(false, false, reporesp);

        SubmitDocumentRequestHandler handler =
            new SubmitDocumentRequestHandler(logMessage, builder, mapFactory,
                regClient, repoClient);

        OMElement request =
            JUnitHelper.fileToOMElement("messages/test-sdr3-cda.xml");

        boolean exception = false;
        OMElement response = null;

        try {

            response = handler.run(null, request);
            logger.debug(DebugUtils.toPrettyString(response));

        } catch (Exception e) {

            logger.error(e, e);
            exception = true;
        }

        assertFalse(exception);
        assertNotNull(response);

        OMNamespace ns = response.getNamespace();

        assertEquals(SubmitDocumentRequestBuilder.URI, ns.getNamespaceURI());
        assertEquals("SubmitDocumentResponse", response.getLocalName());
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

        String uri = SubmitDocumentRequestBuilder.URI;
        String expr = "/@status";
        String status = XPathHelper.stringValueOf(response, expr, uri);

        assertEquals(validStatus.toString(), status);
    }
}
