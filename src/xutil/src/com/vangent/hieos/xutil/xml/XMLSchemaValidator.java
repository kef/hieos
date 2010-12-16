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

import com.vangent.hieos.xutil.exception.XMLSchemaValidatorException;
import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.StringReader;
import org.xml.sax.SAXParseException;

/**
 * Perfoms validation of an XML document against specified XML schema definitions.
 *
 * @author Bernie Thuman
 */
public class XMLSchemaValidator {

    private final static Logger logger = Logger.getLogger(XMLSchemaValidator.class);
    private DOMParser parser = null;
    private XMLSchemaValidatorErrorHandler errorHandler = null;

    /**
     * Constructor.
     *
     * @param schemaLocation Location of schema file.
     * @throws XMLSchemaValidatorException
     */
    public XMLSchemaValidator(String schemaLocation) throws XMLSchemaValidatorException {
        try {
            this.parser = new DOMParser();
            parser.setFeature("http://xml.org/sax/features/validation", true);
            parser.setFeature("http://apache.org/xml/features/validation/schema", true);
            parser.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation",
                    schemaLocation);
            errorHandler = new XMLSchemaValidatorErrorHandler();
            errorHandler.setSchemaFile(schemaLocation);
            parser.setErrorHandler(errorHandler);
        } catch (Exception ex) {
            throw new XMLSchemaValidatorException("XMLSchemaValidator: Could not create XML parser: " + ex.getMessage());
        }
    }

    /**
     * Validate XML document against schema definitions.
     *
     * @param rootNode Starting point (OMElement)
     * @throws XMLSchemaValidatorException
     */
    public void validate(OMElement rootNode) throws XMLSchemaValidatorException {
        this.validate(rootNode.toString());
    }

    /**
     * Validate XML document (in String) against schema definitions.
     *
     * @param xml Starting point (OMElement)
     * @throws XMLSchemaValidatorException
     */
    public void validate(String xml) throws XMLSchemaValidatorException {
        try {
            InputSource is = new InputSource(new StringReader(xml));
            parser.parse(is);
        } catch (Exception ex) {
            throw new XMLSchemaValidatorException(ex.getMessage());
        }
        String errors = errorHandler.getErrors();
        if (errors.length() > 0) {
            throw new XMLSchemaValidatorException(errors);
        }
    }

    /**
     * Stores errors as they are emitted as part of the XML schema validation
     * process.
     */
    public class XMLSchemaValidatorErrorHandler implements ErrorHandler {

        StringBuffer errors;
        String schemaFile = "";

        public XMLSchemaValidatorErrorHandler() {
            errors = new StringBuffer();
        }

        public void setSchemaFile(String file) {
            schemaFile = file;
        }

        public String getErrors() {
            return errors.toString();
        }

        public void warning(SAXParseException e) throws SAXException {
            // Just EMIT warning in this case.
            logger.warn("\nWARNING (XMLSchemaValidator): " + e.getMessage());
        }

        public void error(SAXParseException e) throws SAXException {
            errors.append("\nERROR (XMLSchemaValidator): " + e.getMessage() + "\n" + "Schema location is " + schemaFile);
        }

        public void fatalError(SAXParseException e) throws SAXException {
            errors.append("\nFATAL ERROR (XMLSchemaValidator): " + e.getMessage() + "\n" + "Schema location is " + schemaFile);
        }
    }
}
