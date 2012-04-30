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
package com.vangent.hieos.xutil.metadata.structure;

import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.exception.MetadataValidationException;
import com.vangent.hieos.xutil.exception.XdsInternalException;

import com.vangent.hieos.xutil.xml.XMLParser;
import java.io.File;

import org.apache.axiom.om.OMElement;

/**
 *
 * @author NIST (Adapted for HIEOS by Bernie Thuman).
 */
public class MetadataParser {

    /**
     *
     */
    public MetadataParser() {
    }

    /**
     *
     * @param e
     * @return
     * @throws MetadataException
     * @throws MetadataValidationException
     */
    static public Metadata parseNonSubmission(OMElement e) throws MetadataException, MetadataValidationException {
        Metadata m = new Metadata();
        m.setGrokMetadata(false);
        if (e != null) {
            m.setMetadata(e);
            m.runParser();
        }
        return m;
    }

    /**
     *
     * @param metadata_file
     * @return
     * @throws MetadataException
     * @throws MetadataValidationException
     * @throws XdsInternalException
     */
    static public Metadata parseNonSubmission(File metadata_file) throws MetadataException, MetadataValidationException, XdsInternalException {
        OMElement rootNode = XMLParser.fileToOM(metadata_file);
        return parseNonSubmission(rootNode);
    }

    /**
     *
     * @param e
     * @return
     */
    static public Metadata noParse(OMElement e) {
        Metadata m = new Metadata();
        m.setGrokMetadata(false);
        if (e != null) {
            m.setMetadata(e);
        }
        return m;
    }

    /**
     *
     * @param metadata_file
     * @return
     * @throws MetadataException
     * @throws XdsInternalException
     */
    static public Metadata noParse(File metadata_file) throws MetadataException, XdsInternalException {
        OMElement rootNode = XMLParser.fileToOM(metadata_file);
        return noParse(rootNode);
    }

    /**
     *
     * @param e
     * @return
     * @throws MetadataException
     * @throws XdsInternalException
     * @throws MetadataValidationException
     */
    static public Metadata parse(OMElement e) throws MetadataException, XdsInternalException, MetadataValidationException {
        return new Metadata(e);
    }
}
