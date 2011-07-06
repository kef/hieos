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


package com.vangent.hieos.services.xds.bridge.utils;

import com.vangent.hieos.schemas.xdsbridge.CodeType;
import com.vangent.hieos.schemas.xdsbridge.DocumentType;
import com.vangent.hieos.schemas.xdsbridge.DocumentsType;
import com.vangent.hieos.schemas.xdsbridge.IdType;
import com.vangent.hieos.schemas.xdsbridge.ObjectFactory;
import com.vangent.hieos.schemas.xdsbridge.SubmitDocumentRequest;
import com.vangent.hieos.services.xds.bridge.mapper.ContentParserConfig;
import com.vangent.hieos.services.xds.bridge.mapper.ContentParserConfig
    .ContentParserConfigName;
import com.vangent.hieos.services.xds.bridge.message
    .SubmitDocumentRequestBuilder;
import com.vangent.hieos.services.xds.bridge.support.XDSBridgeConfig;
import com.vangent.hieos.services.xds.bridge.mock
    .MockXConfigActor;
import com.vangent.hieos.services.xds.bridge.transaction
    .SubmitDocumentRequestHandlerTest;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xml.XMLParser;
import com.vangent.hieos.xutil.xml.XPathHelper;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;

import java.net.URL;

import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;


/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-22
 * @author         Jim Horner
 */
public class JUnitHelper {

    /** Field description */
    public static final String SALLY_GRANT = "documents/shs_sally_grant.xml";

    /** Field description */
    private static final Logger logger =
        Logger.getLogger(SubmitDocumentRequestHandlerTest.class);

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws Exception
     */
    public static ContentParserConfig createCDAToXDSContentParserConfig()
            throws Exception {

        XDSBridgeConfig cfg = createXDSBridgeConfig();

        return cfg.findContentParserConfig(
            ContentParserConfigName.SharedHealthSummaryMapper);
    }

    /**
     * Method description
     *
     *
     *
     * @return
     */
    public static OMElement createOMRequest(String file, int count,
            String[] documentIds)
            throws Exception {

        ObjectFactory factory = new ObjectFactory();
        SubmitDocumentRequest sdr = factory.createSubmitDocumentRequest();

        IdType pid = factory.createIdType();

        pid.setRoot("1.3.6.1.4.1.21367.2005.3.7.6fa11e467880478");
        sdr.setPatientId(pid);

        ClassLoader classLoader = JUnitHelper.class.getClassLoader();

        DocumentsType documents = factory.createDocumentsType();

        for (int i = 0; i < count; ++i) {

            DocumentType document = factory.createDocumentType();

            if ((documentIds != null) && (documentIds.length > i)) {
                document.setId(documentIds[i]);
            }

            CodeType type = factory.createCodeType();

            type.setCode("51855-5");
            type.setCodeSystem("2.16.840.1.113883.6.1");

            document.setType(type);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            InputStream is = classLoader.getResourceAsStream(file);

            assertNotNull(is);

            IOUtils.copy(is, bos);

            document.setContent(bos.toByteArray());

            documents.getDocument().add(document);
        }

        sdr.setDocuments(documents);

        QName qname = new QName(SubmitDocumentRequestBuilder.XDSBRIDGE_URI,
                                "SubmitDocumentRequest");
        JAXBContext jc = JAXBContext.newInstance(SubmitDocumentRequest.class);
        Marshaller marshaller = jc.createMarshaller();

        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        JAXBElement element = new JAXBElement(qname, sdr.getClass(), sdr);

        StringWriter sw = new StringWriter();

        marshaller.marshal(element, sw);

        String xml = sw.toString();

        logger.debug(xml);

        OMElement result =
            AXIOMUtil.stringToOM(OMAbstractFactory.getOMFactory(), xml);

        List<OMElement> list = XPathHelper.selectNodes(result,
                                   "./ns:Documents/ns:Document/ns:Content",
                                   SubmitDocumentRequestBuilder.XDSBRIDGE_URI);

        for (OMElement contentNode : list) {

            OMText binaryNode = (OMText) contentNode.getFirstOMChild();

            if (binaryNode != null) {
                binaryNode.setOptimize(true);
            }
        }
        
        return result;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public static XConfigActor createXDSBridgeActor() {

        System.setProperty(
            XConfig.SYSPROP_HIEOS_XDSBRIDGE_DIR,
            String.format(
                "%s/src/test/resources/config/xdsbridge",
                System.getProperty("user.dir")));

        Properties props = new Properties();

        props.setProperty(XDSBridgeConfig.CONFIG_FILE_PROP,
                          "TESTxdsbridgeconfig.xml");

        props.setProperty(XDSBridgeConfig.TEMPLATE_METADATA_PROP,
                          "TESTProvideAndRegisterMetadata.xml");

        return new MockXConfigActor(props);
    }

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws Exception
     */
    public static XDSBridgeConfig createXDSBridgeConfig() throws Exception {

        XConfigActor bridgeActor = createXDSBridgeActor();

        String cfgfile =
            bridgeActor.getProperty(XDSBridgeConfig.CONFIG_FILE_PROP);

        assertNotNull(cfgfile);

        String tplfile =
            bridgeActor.getProperty(XDSBridgeConfig.TEMPLATE_METADATA_PROP);

        assertNotNull(tplfile);

        return XDSBridgeConfig.newInstance(bridgeActor);
    }

    /**
     * Method description
     *
     *
     * @param file
     *
     * @return
     *
     * @throws Exception
     */
    public static OMElement fileToOMElement(String file) throws Exception {

        ClassLoader cl = JUnitHelper.class.getClassLoader();
        URL testfile = cl.getResource(file);

        assertNotNull(String.format("[%s] does not exist.", file), testfile);

        OMElement request = XMLParser.fileToOM(new File(testfile.getFile()));

        assertNotNull(request);

        logger.debug(DebugUtils.toPrettyString(request));

        return request;
    }
}
