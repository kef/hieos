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


package com.vangent.hieos.services.xds.bridge.model;

import com.vangent.hieos.hl7v3util.model.exception.ModelBuilderException;
import com.vangent.hieos.hl7v3util.model.subject.CodedValue;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifierDomain;
import com.vangent.hieos.services.xds.bridge.message
    .SubmitDocumentRequestBuilder;
import com.vangent.hieos.services.xds.bridge.support.XDSBridgeConfig;
import com.vangent.hieos.services.xds.bridge.utils.JUnitHelper;

import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;


/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-09
 * @author         Jim Horner
 */
public class SubmitDocumentRequestBuilderTest {

    /** Field description */
    private static final Logger logger =
        Logger.getLogger(SubmitDocumentRequestBuilderTest.class);

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void buildSDR0Test() throws Exception {

        OMElement test =
            JUnitHelper.fileToOMElement("messages/test-sdr0-no-optionals.xml");

        XDSBridgeConfig cfg = JUnitHelper.createXDSBridgeConfig();

        SubmitDocumentRequestBuilder builder =
            new SubmitDocumentRequestBuilder(cfg);

        SubmitDocumentRequest sdr = builder.buildSubmitDocumentRequest(test);

        assertNotNull(sdr);

        SubjectIdentifier pid = sdr.getPatientId();

        assertNotNull(pid);

        SubjectIdentifierDomain domain = pid.getIdentifierDomain();

        assertNotNull(domain);

        assertEquals("2.16.840.1.113883.3.454.1", domain.getUniversalId());
        assertEquals("1000387002", pid.getIdentifier());

        List<Document> docs = sdr.getDocuments();

        assertNotNull(docs);
        assertEquals(1, docs.size());

        for (Document doc : docs) {

            assertNull(doc.getId());

            CodedValue type = doc.getType();

            assertNotNull(type);

            assertEquals("51855-5", type.getCode());
            assertEquals("2.16.840.1.113883.6.1", type.getCodeSystem());
            assertNull(type.getCodeSystemName());
            assertNull(type.getDisplayName());
        }
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void buildSDR1Test() throws Exception {

        OMElement test =
            JUnitHelper.fileToOMElement("messages/test-sdr1-optionals.xml");

        XDSBridgeConfig cfg = JUnitHelper.createXDSBridgeConfig();

        SubmitDocumentRequestBuilder builder =
            new SubmitDocumentRequestBuilder(cfg);

        SubmitDocumentRequest sdr = builder.buildSubmitDocumentRequest(test);

        assertNotNull(sdr);

        SubjectIdentifier pid = sdr.getPatientId();

        assertNotNull(pid);

        SubjectIdentifierDomain domain = pid.getIdentifierDomain();

        assertNotNull(domain);

        assertEquals("2.16.840.1.113883.3.454.1", domain.getUniversalId());
        assertEquals("1000387002", pid.getIdentifier());

        Identifier orgid = sdr.getOrganizationId();

        assertNotNull(orgid);

        assertEquals("2.16.840.1.113883.3.454.1.8991", orgid.getRoot());
        assertEquals("50", orgid.getExtension());

        List<Document> docs = sdr.getDocuments();

        assertNotNull(docs);
        assertEquals(1, docs.size());

        for (Document doc : docs) {

            assertEquals("2.16.840.1.113883.10.20.1234", doc.getId());

            // assertEquals("2.16.840.1.113883.10.20.1235", doc.getReplaceId());

            CodedValue type = doc.getType();

            assertNotNull(type);

            assertEquals("51855-5", type.getCode());
            assertEquals("2.16.840.1.113883.6.1", type.getCodeSystem());
            assertEquals("LOINC", type.getCodeSystemName());
            assertEquals("Shared Health Summary", type.getDisplayName());
        }
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void buildSDR2Test() throws Exception {

        OMElement test =
            JUnitHelper.fileToOMElement("messages/test-sdr2-multidocs.xml");

        XDSBridgeConfig cfg = JUnitHelper.createXDSBridgeConfig();

        SubmitDocumentRequestBuilder builder =
            new SubmitDocumentRequestBuilder(cfg);

        SubmitDocumentRequest sdr = builder.buildSubmitDocumentRequest(test);

        assertNotNull(sdr);

        SubjectIdentifier pid = sdr.getPatientId();

        assertNotNull(pid);

        SubjectIdentifierDomain domain = pid.getIdentifierDomain();

        assertNotNull(domain);
        assertEquals("2.16.840.1.113883.3.454.1", domain.getUniversalId());
        assertEquals("1000387002", pid.getIdentifier());

        List<Document> docs = sdr.getDocuments();

        assertNotNull(docs);
        assertEquals(3, docs.size());

        String[] knownIds = new String[] { "2.16.840.1.113883.10.20.1231",
                                           "2.16.840.1.113883.10.20.1232",
                                           "2.16.840.1.113883.10.20.1233" };

        for (int i = 0; i < docs.size(); ++i) {

            Document doc = docs.get(i);

            assertEquals(knownIds[i], doc.getId());

            // assertEquals("2.16.840.1.113883.10.20.1235", doc.getReplaceId());

            CodedValue type = doc.getType();

            assertNotNull(type);

            assertEquals("51855-5", type.getCode());
            assertEquals("2.16.840.1.113883.6.1", type.getCodeSystem());
            assertNull(type.getCodeSystemName());
            assertNull(type.getDisplayName());
        }
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void buildSDR4Test() throws Exception {

        OMElement test =
            JUnitHelper.fileToOMElement("messages/test-sdr4-missing-stuff.xml");

        XDSBridgeConfig cfg = JUnitHelper.createXDSBridgeConfig();

        SubmitDocumentRequestBuilder builder =
            new SubmitDocumentRequestBuilder(cfg);

        SubmitDocumentRequest sdr = null;
        boolean exception = false;
        try {
            builder.buildSubmitDocumentRequest(test);
        } catch (ModelBuilderException e) {
            exception = true;
        }

        assertNull(sdr);
        assertTrue(exception);
    }
}
