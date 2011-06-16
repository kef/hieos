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
package com.vangent.hieos.xutil.xml;

import com.vangent.hieos.xutil.exception.XMLParserException;
import com.vangent.hieos.xutil.iosupport.Io;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;

/**
 *
 * @author Bernie Thuman
 */
public class XMLParser {

    /**
     *
     * @param fileName
     * @return
     * @throws XMLParserException
     */
    static public OMElement fileToOM(String fileName) throws XMLParserException {
        File file = new File(fileName);
        return XMLParser.fileToOM(file);
    }

    /**
     * 
     * @param file
     * @return
     * @throws XMLParserException
     */
    static public OMElement fileToOM(File file) throws XMLParserException {
        try {
            String xml = Io.getStringFromInputStream(new FileInputStream(file));
            OMElement rootNode = XMLParser.stringToOM(xml);
            return rootNode;
        } catch (IOException ex) {
            throw new XMLParserException(ex.getMessage());
        }
    }

    /**
     *
     * @param xml
     * @return
     * @throws XMLStreamException
     */
    public static OMElement stringToOM(String xml) throws XMLParserException {
        try {
            return AXIOMUtil.stringToOM(xml);
        } catch (XMLStreamException ex) {
            throw new XMLParserException(ex.getMessage());
        }
    }

    /**
     *
     * @param xml
     * @return
     * @throws XMLStreamException
     */
    public static OMElement bytesToOM(byte[] xml) throws XMLParserException {
        StAXOMBuilder builder = null;
        try {
            builder = new StAXOMBuilder(new ByteArrayInputStream(xml));
        } catch (final XMLStreamException ex) {
            throw new XMLParserException(ex.getMessage());
        }
        return builder.getDocumentElement();
    }
}
