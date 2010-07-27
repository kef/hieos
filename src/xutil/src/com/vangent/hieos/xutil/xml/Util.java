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
package com.vangent.hieos.xutil.xml;

import com.vangent.hieos.xutil.exception.XdsInternalException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
//import java.io.InputStream;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;

/**
 *
 * @author NIST (adapted by Bernie Thuman)
 */
public class Util {

    /**
     *
     * @param infile
     * @return
     * @throws FactoryConfigurationError
     * @throws XdsInternalException
     */
    public static OMElement parse_xml(File infile) throws FactoryConfigurationError, XdsInternalException {
        // Create the parser
        XMLStreamReader parser;
        try {
            parser = XMLInputFactory.newInstance().createXMLStreamReader(new FileInputStream(infile));
        } catch (XMLStreamException e) {
            throw new XdsInternalException("Could not create XMLStreamReader from " + infile.getName());
        } catch (FileNotFoundException e) {
            throw new XdsInternalException("Could not find input file " + infile.getAbsolutePath());
        }
        return Util.getRootElement(parser);
    }

    /**
     *
     * @param input
     * @return
     * @throws FactoryConfigurationError
     * @throws XdsInternalException
     */
    public static OMElement parse_xml(String input) throws FactoryConfigurationError, XdsInternalException {
        // Create the parser
        XMLStreamReader parser;
        try {
            parser = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(input.getBytes()));
        } catch (Exception e) {
            throw new XdsInternalException("Could not create XMLStreamReader from string: " + input.substring(0, 100) + "...");
        }
        return Util.getRootElement(parser);
    }

    /**
     *
     * @param parser
     * @return
     * @throws FactoryConfigurationError
     * @throws XdsInternalException
     */
    private static OMElement getRootElement(XMLStreamReader parser) throws FactoryConfigurationError, XdsInternalException {
        // create the builder
        StAXOMBuilder builder = null;
        try {
            builder = new StAXOMBuilder(parser);
        } catch (Exception e) {
            throw new XdsInternalException("Could not create StAXOMBuilder: " + e.getMessage());
        }

        OMElement documentElement = null;
        try {
            // get the root element (in this case the envelope)
            documentElement = builder.getDocumentElement();
            if (documentElement == null) {
                throw new XdsInternalException("No document element");
            }
        } catch (Exception e) {
            throw new XdsInternalException(
                    "Could not create XMLStreamReader in Util.getRootElement():" + e.getMessage());
        }
        return documentElement;
    }

    /**
     *
     * @param in
     * @return
     * @throws XdsInternalException
     */
    public static OMElement deep_copy(OMElement in) throws XdsInternalException {
        if (in == null) {
            return null;
        }
        OMElement clonedElement = in.cloneOMElement();
        clonedElement.build();
        return clonedElement;
        //return parse_xml(in.toString());
    }
}
