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


package com.vangent.hieos.services.xds.bridge.mapper;

import com.vangent.hieos.hl7v3util.model.subject.CodedValue;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.services.xds.bridge.message.XDSPnRMessage;
import com.vangent.hieos.services.xds.bridge.model.Document;
import com.vangent.hieos.services.xds.bridge.utils.DebugUtils;
import com.vangent.hieos.services.xds.bridge.utils.JUnitHelper;
import com.vangent.hieos.services.xds.bridge.utils.SubjectIdentifierUtils;
import com.vangent.hieos.xutil.iosupport.Io;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-13
 * @author         Jim Horner
 */
public class CDAToXDSMapperTest {

    /** Field description */
    private static final Logger logger =
        Logger.getLogger(CDAToXDSMapperTest.class);

    /**
     * This method will test the business logic w/in the mapper
     *
     *
     * @throws Exception
     */
    @Test
    public void createReplaceVariablesTest() throws Exception {

        ContentParserConfig cfg =
            JUnitHelper.createCDAToXDSContentParserConfig();
        ContentParser gen = new ContentParser();

        CDAToXDSMapper mapper = new CDAToXDSMapper(gen, cfg);

        ClassLoader cl = getClass().getClassLoader();
        InputStream xmlis = cl.getResourceAsStream(JUnitHelper.SALLY_GRANT);

        assertNotNull(xmlis);

        byte[] xml = Io.getBytesFromInputStream(xmlis);

        assertNotNull(xml);
        assertTrue(xml.length > 0);

        Document doc = new Document();

        CodedValue type = new CodedValue();
        type.setCode("51855-5");
        type.setCodeSystem("2.16.840.1.113883.6.1");
        type.setCodeSystemName("LOINC");

        doc.setType(type);

        CodedValue format = new CodedValue();

        format.setCode("urn:ihe:pcc:xds-ms:2007");
        format.setCodeSystem("1.3.6.1.4.1.19376.1.2.3");
        format.setCodeSystemName("XDS");

        doc.setFormat(format);
        doc.setContent(xml);

        Map<String, String> repl = mapper.createReplaceVariables(doc);

        logger.debug(DebugUtils.toPrettyString(repl));

        // 1.2.36.1.2001.1003.0.8003611234567890
        assertEquals("",
                     repl.get(ContentVariableName.AuthorPersonRoot.toString()));
        assertEquals(
            "1.2.36.1.2001.1003.0.8003611234567890",
            repl.get(ContentVariableName.AuthorPersonExtension.toString()));

        // "1.3.6.1.4.1.21367.2005.3.7.6fa11e467880478"
        assertEquals("1.3.6.1.4.1.21367.2005.3.7.6fa11e467880478",
                     repl.get(ContentVariableName.PatientIdRoot.toString()));
        assertEquals("", repl.get(ContentVariableName.PatientIdExtension.toString()));
        assertEquals("6fa11e467880478^^^&1.3.6.1.4.1.21367.2005.3.7&ISO",
                     repl.get(ContentVariableName.PatientIdCX.toString()));

        // "1.3.6.1.4.1.21367.2005.3.7.6fa11e467880478"
        assertEquals(
            "1.3.6.1.4.1.21367.2005.3.7.6fa11e467880478",
            repl.get(ContentVariableName.SourcePatientIdRoot.toString()));
        assertEquals("",
            repl.get(ContentVariableName.SourcePatientIdExtension.toString()));
        assertEquals("6fa11e467880478^^^&1.3.6.1.4.1.21367.2005.3.7&ISO",
            repl.get(ContentVariableName.SourcePatientIdCX.toString()));
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void mapTest() throws Exception {

        ContentParserConfig cfg =
            JUnitHelper.createCDAToXDSContentParserConfig();
        ContentParser gen = new ContentParser();

        CDAToXDSMapper mapper = new CDAToXDSMapper(gen, cfg);

        ClassLoader cl = getClass().getClassLoader();
        InputStream xmlis = cl.getResourceAsStream(JUnitHelper.SALLY_GRANT);

        assertNotNull(xmlis);

        String xml = Io.getStringFromInputStream(xmlis);

        assertNotNull(xml);
        assertTrue(StringUtils.isNotBlank(xml));

        Document doc = new Document();

        CodedValue type = new CodedValue();
        type.setCode("51855-5");
        type.setCodeSystem("2.16.840.1.113883.6.1");
        type.setCodeSystemName("LOINC");

        doc.setType(type);

        CodedValue format = new CodedValue();

        format.setCode("urn:ihe:pcc:xds-ms:2007");
        format.setCodeSystem("1.3.6.1.4.1.19376.1.2.3");
        format.setCodeSystemName("XDS");

        doc.setFormat(format);
        doc.setContent(xml.getBytes());

        SubjectIdentifier patientId =
            SubjectIdentifierUtils.createSubjectIdentifier(
                "1.3.6.1.4.1.21367.2005.3.7.6fa11e467880478", null);

        XDSPnRMessage pnr = mapper.map(patientId, doc);

        assertNotNull(pnr);

        String pnrstr = DebugUtils.toPrettyString(pnr.getElement());

        logger.debug(pnrstr);

        Pattern pattern = Pattern.compile("\\{.*?\\}");
        Matcher matcher = pattern.matcher(pnrstr);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {

            sb.append(matcher.group());
            sb.append("\n");
        }

        // assertEquals("Found unset tokens: " + sb.toString(), 0, sb.length());

//      JUnitHelper.createXConfigInstance();
//
//      String schemaLocation =
//          String.format(
//              "%s/../../config/schema/v3/XDS.b_DocumentRepository.xsd",
//              System.getProperty("user.dir"));
//
//      assertTrue("File " + schemaLocation + " not exist.",
//              new File(schemaLocation).exists());
//
//      XMLSchemaValidator schemaValidator =
//          new XMLSchemaValidator(schemaLocation);
//
//      //schemaValidator.validate(pnrstr);
    }
}
