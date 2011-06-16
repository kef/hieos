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

import java.io.InputStream;
import java.util.Map;
import com.vangent.hieos.services.xds.bridge.model.Document;
import com.vangent.hieos.services.xds.bridge.model.Identifier;
import com.vangent.hieos.services.xds.bridge.model.XDSPnR;
import com.vangent.hieos.services.xds.bridge.utils.DebugUtils;
import com.vangent.hieos.services.xds.bridge.utils.StringUtils;
import com.vangent.hieos.xutil.iosupport.Io;
import org.apache.log4j.Logger;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-13
 * @author         Jim Horner
 */
public class CDAToXDSMapperTest {

    private final static Logger logger =
            Logger.getLogger(CDAToXDSMapperTest.class);
    @Test
    public void mapTest() throws Exception {

        ContentParserConfig cfg =
            CDAToXDSContentParserConfigFactory.createConfig();
        ContentParser gen = new ContentParser();

        CDAToXDSMapper mapper = new CDAToXDSMapper(gen, cfg);

        ClassLoader cl = getClass().getClassLoader();
        InputStream xmlis =
            cl.getResourceAsStream("documents/exampleCDA-SHS-V1_0.xml");

        assertNotNull(xmlis);

        String xml = Io.getStringFromInputStream(xmlis);

        assertNotNull(xml);
        assertTrue(StringUtils.isNotBlank(xml));

        Document doc = new Document();

        doc.setContent(xml.getBytes());

        Identifier pid = new Identifier();
        XDSPnR pnr = mapper.map(pid, doc);
        assertNotNull(pnr);

        logger.debug(DebugUtils.toPrettyString(pnr.getNode()));
    }

    /**
     * This method will test the business logic w/in the mapper
     *
     *
     * @throws Exception
     */
    @Test
    public void createReplaceVariablesTest() throws Exception {

        ContentParserConfig cfg =
            CDAToXDSContentParserConfigFactory.createConfig();
        ContentParser gen = new ContentParser();

        CDAToXDSMapper mapper = new CDAToXDSMapper(gen, cfg);

        ClassLoader cl = getClass().getClassLoader();
        InputStream xmlis =
            cl.getResourceAsStream("documents/exampleCDA-SHS-V1_0.xml");

        assertNotNull(xmlis);

        String xml = Io.getStringFromInputStream(xmlis);

        assertNotNull(xml);
        assertTrue(StringUtils.isNotBlank(xml));

        Document doc = new Document();

        doc.setContent(xml.getBytes());

        Map<String, String> repl = mapper.createReplaceVariables(doc);

        logger.debug(DebugUtils.toPrettyString(repl));

        // 1.2.36.1.2001.1003.0.8003611234567890
        assertEquals("",
                repl.get(ContentVariableName.AuthorPersonRoot.toString()));
        assertEquals("1.2.36.1.2001.1003.0.8003611234567890",
                repl.get(ContentVariableName.AuthorPersonExtension.toString()));

        // "1.2.36.1.2001.1003.0.8003601234512345"
        assertEquals("&1.2.36.1.2001.1003.0&ISO",
                     repl.get(ContentVariableName.PatientRoot.toString()));
        assertEquals("8003601234512345",
                     repl.get(ContentVariableName.PatientExtension.toString()));
        
        // "1.2.36.1.2001.1003.0.8003601234512345"
        assertEquals("&1.2.36.1.2001.1003.0&ISO",
                     repl.get(ContentVariableName.SourcePatientRoot.toString()));
        assertEquals("8003601234512345",
                     repl.get(ContentVariableName.SourcePatientExtension.toString()));
    }
}
