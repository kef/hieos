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

package com.vangent.hieos.services.xds.bridge.serviceimpl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import com.vangent.hieos.hl7v3util.model.subject.CodedValue;
import com.vangent.hieos.services.xds.bridge.mapper.ContentParserConfig;
import com.vangent.hieos.services.xds.bridge.mapper.ContentParserConfig
    .ContentParserConfigName;
import com.vangent.hieos.services.xds.bridge.mapper.ContentVariableName;
import com.vangent.hieos.services.xds.bridge.mapper.DocumentTypeMapping;
import com.vangent.hieos.services.xds.bridge.utils.CodedValueUtils;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfig.ConfigItem;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xml.XMLParser;
import org.apache.axiom.om.OMElement;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-24
 * @author         Jim Horner
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
    public static XDSBridgeConfig parseConfigFile(XConfigActor actor)
            throws Exception {

        String cfgfile = prefixFullPath(actor.getProperty(CONFIG_FILE_PROP));
        OMElement configElem = XMLParser.fileToOM(cfgfile);

        String tplfile =
            prefixFullPath(actor.getProperty(TEMPLATE_METADATA_PROP));

        Map<String, ContentParserConfig> parserConfigs =
            parseContentConfigs(configElem, tplfile);

        List<DocumentTypeMapping> mappings =
            parseDocmentTypeMappings(configElem, parserConfigs);

        return new XDSBridgeConfig(actor, mappings);
    }

    /**
     * Method description
     *
     *
     * @param configElem
     * @param defaultTemplateFilename
     *
     * @return
     *
     */
    private static Map<String, ContentParserConfig> parseContentConfigs(
            OMElement configElem, String defaultTemplateFilename) {

        Map<String, ContentParserConfig> result = new HashMap<String,
                                                      ContentParserConfig>();

        QName nameQName = new QName("name");
        QName templateQName = new QName("template");
        QName namespacesQName = new QName("Namespaces");
        QName docContentQName = new QName("DocumentContentVariables");
        QName staticValuesQName = new QName("StaticContentVariables");

        Iterator<OMElement> iterator =
            configElem.getChildrenWithName(new QName("ContentParserConfig"));

        while (iterator.hasNext()) {

            OMElement mapperConfigElem = iterator.next();

            // pull name
            String nameAttribute =
                mapperConfigElem.getAttributeValue(nameQName);

            ContentParserConfigName name =
                ContentParserConfigName.valueOf(nameAttribute);

            // pull template or use default
            String templateFilename =
                mapperConfigElem.getAttributeValue(templateQName);

            if (StringUtils.isBlank(templateFilename)) {

                templateFilename = defaultTemplateFilename;
            }

            OMElement namespacesElem =
                mapperConfigElem.getFirstChildWithName(namespacesQName);

            // pull namespaces
            Map<String, String> namespaces = parseNamespaces(namespacesElem);

            OMElement dynamicElem =
                mapperConfigElem.getFirstChildWithName(docContentQName);

            // pull expressions
            Map<String, List<String>> expressions =
                parseExpressions(dynamicElem);

            parseNameValuePairs(dynamicElem, "name", "expression", true);

            OMElement staticElem =
                mapperConfigElem.getFirstChildWithName(staticValuesQName);

            // pull static values
            Map<String, String> staticValues = parseStaticValues(staticElem);

            ContentParserConfig parserConfig = new ContentParserConfig(name,
                                                   namespaces, expressions,
                                                   staticValues,
                                                   templateFilename);

            result.put(name.toString(), parserConfig);
        }

        return result;
    }

    /**
     * Method description
     *
     *
     * @param configElem
     * @param parserConfigs
     *
     * @return
     */
    private static List<DocumentTypeMapping> parseDocmentTypeMappings(
            OMElement configElem,
            Map<String, ContentParserConfig> parserConfigs) {

        List<DocumentTypeMapping> result = new ArrayList<DocumentTypeMapping>();

        OMElement docMappingsElem =
            configElem.getFirstChildWithName(new QName("DocumentTypeMappings"));

        Iterator<OMElement> iterator = docMappingsElem.getChildElements();

        QName nameQName = new QName("name");
        QName parserConfigQName = new QName("ContentParserConfig");
        QName typeQName = new QName("Type");
        QName formatQName = new QName("Format");

        while (iterator.hasNext()) {

            OMElement mapping = iterator.next();

            OMElement xdsMapperElem =
                mapping.getFirstChildWithName(parserConfigQName);
            String parserConfigName =
                xdsMapperElem.getAttributeValue(nameQName);

            ContentParserConfig parserConfig =
                parserConfigs.get(parserConfigName);

            if (parserConfig == null) {

                throw new IllegalStateException(
                    String.format(
                        "ContentParserConfig name=%s does not exist.",
                        parserConfigName));
            }

            OMElement typeElem = mapping.getFirstChildWithName(typeQName);

            CodedValue type = CodedValueUtils.parseCodedValue(typeElem);

            OMElement formatElem = mapping.getFirstChildWithName(formatQName);

            CodedValue format = CodedValueUtils.parseCodedValue(formatElem);

            result.add(new DocumentTypeMapping(type, format, parserConfig));
        }

        return result;
    }

    /**
     * Method description
     *
     *
     * @param dynamicElem
     *
     * @return
     */
    private static Map<String,
                       List<String>> parseExpressions(OMElement dynamicElem) {

        NameValueList result = parseNameValuePairs(dynamicElem, "name",
                                   "expression", true);

        return result.toMultiValueMap();
    }

    /**
     * Method description
     *
     *
     * @param node
     * @param nameAttribute
     * @param valueAttribute
     * @param checkKey
     *
     * @return
     */
    private static NameValueList parseNameValuePairs(OMElement node,
            String nameAttribute, String valueAttribute, boolean checkKey) {

        NameValueList result = new NameValueList();

        QName nameQName = new QName(nameAttribute);
        QName valueQName = new QName(valueAttribute);

        Iterator<OMElement> iterator = node.getChildElements();

        while (iterator.hasNext()) {

            OMElement childNode = iterator.next();
            String key = childNode.getAttributeValue(nameQName);

            if (checkKey) {

                try {

                    ContentVariableName varName =
                        ContentVariableName.valueOf(key);

                    key = varName.toString();

                } catch (IllegalArgumentException e) {

                    logger.warn(String.format("%s is not valid.", key));
                    key = null;
                }
            }

            // checkKey will null out a bad key
            if (StringUtils.isNotBlank(key)) {

                String value = childNode.getAttributeValue(valueQName);

                result.addNameValue(key, value);
            }
        }

        return result;
    }

    /**
     * Method description
     *
     *
     * @param node
     *
     * @return
     */
    private static Map<String, String> parseNamespaces(OMElement node) {

        NameValueList list = parseNameValuePairs(node, "prefix", "uri", false);

        return list.toSingleValueMap();
    }

    /**
     * Method description
     *
     *
     * @param staticElem
     *
     * @return
     */
    private static Map<String, String> parseStaticValues(OMElement staticElem) {

        NameValueList result = parseNameValuePairs(staticElem, "name", "value",
                                   true);

        return result.toSingleValueMap();
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
    public ContentParserConfig findContentParserConfig(
            ContentParserConfigName name) {

        ContentParserConfig result = null;

        if (name != null) {

            for (DocumentTypeMapping mapping : getDocumentTypeMappings()) {

                ContentParserConfigName configName =
                    mapping.getContentParserConfig().getName();

                if (configName == name) {

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

    /**
     * Class description
     *
     *
     * @version        v1.0, 2011-06-29
     * @author         Jim Horner
     */
    private static class NameValueList {

        /** Field description */
        private final Map<String, List<String>> pairs;

        /**
         * Constructs ...
         *
         */
        public NameValueList() {

            super();
            this.pairs = new LinkedHashMap<String, List<String>>();
        }

        /**
         * Method description
         *
         *
         * @param name
         * @param value
         */
        public void addNameValue(String name, String value) {

            if (this.pairs.containsKey(name) == false) {

                this.pairs.put(name, new ArrayList<String>());
            }

            this.pairs.get(name).add(value);
        }

        /**
         * Method description
         *
         *
         * @return
         */
        public Map<String, List<String>> toMultiValueMap() {

            return this.pairs;
        }

        /**
         * Method description
         *
         *
         * @return
         */
        public Map<String, String> toSingleValueMap() {

            Map<String, String> result = new LinkedHashMap<String, String>();

            for (Map.Entry<String, List<String>> entry :
                    this.pairs.entrySet()) {

                result.put(entry.getKey(), entry.getValue().get(0));
            }

            return result;
        }
    }
}

