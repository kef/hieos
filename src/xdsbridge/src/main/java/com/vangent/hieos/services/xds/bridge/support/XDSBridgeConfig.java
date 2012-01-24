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
package com.vangent.hieos.services.xds.bridge.support;

import com.vangent.hieos.hl7v3util.model.subject.CodedValue;
import com.vangent.hieos.services.xds.bridge.mapper.ContentParserConfig;
import com.vangent.hieos.services.xds.bridge.mapper.DocumentTypeMapping;
import com.vangent.hieos.services.xds.bridge.utils.CodedValueUtils;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfig.ConfigItem;
import com.vangent.hieos.xutil.xconfig.XConfigActor;

import org.apache.log4j.Logger;

import java.io.File;

import java.util.Collections;
import java.util.List;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-24
 * @author         Vangent
 */
public class XDSBridgeConfig {

    /** Field description */
    public static final String CONFIG_FILE_PROP = "ConfigFile";
    /** Field description */
    public static final String TEMPLATE_METADATA_PROP =
            "ProvideAndRegisterMetadataTemplate";
    /** Field description */
    private static final Logger logger =
            Logger.getLogger(XDSBridgeConfig.class);
    /** Field description */
    private final List<DocumentTypeMapping> documentTypeMappings;
    /** Field description */
    private final XConfigActor xdsBridgeActor;

    /**
     * Constructs ...
     *
     *
     *
     *
     * @param xdsBridgeActor
     * @param mappings
     */
    private XDSBridgeConfig(XConfigActor xdsBridgeActor,
            List<DocumentTypeMapping> mappings) {

        super();
        this.xdsBridgeActor = xdsBridgeActor;
        this.documentTypeMappings = mappings;
    }

    /**
     * Method description
     *
     *
     *
     * @param actor
     *
     * @return
     *
     * @throws Exception
     */
    public static XDSBridgeConfig newInstance(XConfigActor actor)
            throws Exception {

        String cfgFileName =
                prefixFullPath(actor.getProperty(CONFIG_FILE_PROP));
        File cfgFile = new File(cfgFileName);

        if (cfgFile.exists() == false) {

            throw new IllegalStateException(String.format("%s does not exist.",
                    cfgFileName));
        }

        String tplFileName =
                prefixFullPath(actor.getProperty(TEMPLATE_METADATA_PROP));

        File tplFile = new File(tplFileName);

        if (tplFile.exists() == false) {

            throw new IllegalStateException(String.format("%s does not exist.",
                    tplFileName));
        }

        XDSBridgeConfigXmlParser parser =
                new XDSBridgeConfigXmlParser(cfgFileName, tplFileName);
        List<DocumentTypeMapping> mappings = parser.parse();

        return new XDSBridgeConfig(actor, mappings);
    }

    /**
     * Method description
     *
     *
     * @param filename
     *
     * @return
     */
    private static String prefixFullPath(String filename) {

        return String.format(
                "%s%s%s", XConfig.getConfigLocation(ConfigItem.XDSBRIDGE_DIR),
                File.separator, filename);
    }

    /**
     * Method description
     *
     *
     * @param name
     *
     * @return
     */
    public ContentParserConfig findContentParserConfig(String name) {
        ContentParserConfig result = null;
        if (name != null) {
            for (DocumentTypeMapping mapping : getDocumentTypeMappings()) {
                String configName = mapping.getContentParserConfig().getName();
                if (configName.equalsIgnoreCase(name)) {
                    result = mapping.getContentParserConfig();
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Method description
     *
     *
     * @param type
     *
     * @return
     */
    public DocumentTypeMapping findDocumentTypeMapping(CodedValue type) {

        DocumentTypeMapping result = null;

        if (type != null) {

            for (DocumentTypeMapping mapping : getDocumentTypeMappings()) {

                if (CodedValueUtils.equals(type, mapping.getType())) {

                    result = mapping;

                    break;
                }
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
    public List<DocumentTypeMapping> getDocumentTypeMappings() {
        return Collections.unmodifiableList(documentTypeMappings);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public XConfigActor getXdsBridgeActor() {
        return xdsBridgeActor;
    }
}
