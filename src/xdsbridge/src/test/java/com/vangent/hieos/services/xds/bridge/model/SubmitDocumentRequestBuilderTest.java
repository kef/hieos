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

import java.io.File;
import java.net.URL;
import java.util.List;
import com.vangent.hieos.hl7v3util.model.subject.CodedValue;
import com.vangent.hieos.services.xds.bridge.utils.DebugUtils;
import com.vangent.hieos.xutil.xml.XMLParser;
import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-09
 * @author         Jim Horner
 */
public class SubmitDocumentRequestBuilderTest {

    /** Field description */
    private final static Logger logger =
        Logger.getLogger(SubmitDocumentRequestBuilderTest.class);

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void buildSDR0Test() throws Exception {

        ClassLoader cl = getClass().getClassLoader();
        URL testfile = cl.getResource("messages/test-sdr0-no-optionals.xml");

        assertNotNull(testfile);

        OMElement test = XMLParser.fileToOM(new File(testfile.getFile()));

        assertNotNull(test);

        logger.debug(DebugUtils.toPrettyString(test));

        SubmitDocumentRequestBuilder uut = new SubmitDocumentRequestBuilder();
        SubmitDocumentRequest sdr = uut.buildSubmitDocumentRequest(test);

        assertNotNull(sdr);

        Identifier pid = sdr.getPatientId();

        assertNotNull(pid);
        assertEquals("2.16.840.1.113883.3.454.1.1000387002", pid.getRoot());
        assertNull(pid.getExtension());

        List<Document> docs = sdr.getDocuments();

        assertNotNull(docs);
        assertEquals(1, docs.size());

        for (Document doc : docs) {

            assertNull(doc.getId());

            CodedValue type = doc.getType();

            assertNotNull(type);

            assertEquals("C32", type.getCode());
            assertEquals("2.16.840.1.113883.3.88.11.32.1",
                         type.getCodeSystem());
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

        ClassLoader cl = getClass().getClassLoader();
        URL testfile = cl.getResource("messages/test-sdr1-optionals.xml");

        assertNotNull(testfile);

        OMElement test = XMLParser.fileToOM(new File(testfile.getFile()));

        assertNotNull(test);

        logger.debug(DebugUtils.toPrettyString(test));

        SubmitDocumentRequestBuilder uut = new SubmitDocumentRequestBuilder();
        SubmitDocumentRequest sdr = uut.buildSubmitDocumentRequest(test);

        assertNotNull(sdr);

        Identifier pid = sdr.getPatientId();

        assertNotNull(pid);
        assertEquals("2.16.840.1.113883.3.454.1", pid.getRoot());
        assertEquals("1000387002", pid.getExtension());

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

            assertEquals("C32", type.getCode());
            assertEquals("2.16.840.1.113883.3.88.11.32.1",
                         type.getCodeSystem());
            assertEquals("CodeSystemName", type.getCodeSystemName());
            assertEquals("DisplayName", type.getDisplayName());
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

        ClassLoader cl = getClass().getClassLoader();
        URL testfile = cl.getResource("messages/test-sdr2-multidocs.xml");

        assertNotNull(testfile);

        OMElement test = XMLParser.fileToOM(new File(testfile.getFile()));

        assertNotNull(test);

        logger.debug(DebugUtils.toPrettyString(test));

        SubmitDocumentRequestBuilder uut = new SubmitDocumentRequestBuilder();
        SubmitDocumentRequest sdr = uut.buildSubmitDocumentRequest(test);

        assertNotNull(sdr);

        Identifier pid = sdr.getPatientId();

        assertNotNull(pid);
        assertEquals("2.16.840.1.113883.3.454.1.1000387002", pid.getRoot());
        assertNull(pid.getExtension());

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

            assertEquals("C32", type.getCode());
            assertEquals("2.16.840.1.113883.3.88.11.32.1",
                         type.getCodeSystem());
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
    public void buildSDR3Test() throws Exception {

        ClassLoader cl = getClass().getClassLoader();
        URL testfile = cl.getResource("messages/test-sdr3-cda.xml");

        assertNotNull(testfile);

        OMElement test = XMLParser.fileToOM(new File(testfile.getFile()));

        assertNotNull(test);

        logger.debug(DebugUtils.toPrettyString(test));

        SubmitDocumentRequestBuilder uut = new SubmitDocumentRequestBuilder();
        SubmitDocumentRequest sdr = uut.buildSubmitDocumentRequest(test);

        assertNotNull(sdr);

        Identifier pid = sdr.getPatientId();

        assertNotNull(pid);
        assertEquals("2.16.840.1.113883.3.454.1.1000387002", pid.getRoot());
        assertNull(pid.getExtension());

        List<Document> docs = sdr.getDocuments();

        assertNotNull(docs);
        assertEquals(1, docs.size());

        for (Document doc : docs) {

            assertNull(doc.getId());

            // assertNull(doc.getReplaceId());

            CodedValue type = doc.getType();

            assertNotNull(type);

            assertEquals("C32", type.getCode());
            assertEquals("2.16.840.1.113883.3.88.11.32.1",
                         type.getCodeSystem());
            assertNull(type.getCodeSystemName());
            assertNull(type.getDisplayName());

            byte[] content = doc.getContent();

            assertNotNull(content);
            assertTrue(content.length > 0);
            logger.debug(new String(doc.getContent()));
        }
    }
}
