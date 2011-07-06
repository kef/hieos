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

import java.util.EnumMap;
import java.util.Map;
import com.vangent.hieos.hl7v3util.model.subject.CodedValue;
import com.vangent.hieos.services.xds.bridge.mapper.ContentParserConfig
    .ContentParserConfigName;
import com.vangent.hieos.services.xds.bridge.support.XDSBridgeConfig;
import org.apache.log4j.Logger;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-13
 * @author         Jim Horner
 */
public class MapperFactory {

    /** Field description */
    private static final Logger logger = Logger.getLogger(MapperFactory.class);

    /** Field description */
    private final ContentParser contentParser;

    /** Field description */
    private final Map<ContentParserConfigName, IXDSMapper> xdsMappers;

    /** Field description */
    private final XDSBridgeConfig xdsbridgeConfig;

    /**
     * Constructs ...
     *
     *
     *
     *
     * @param bridgeConfig
     * @param tplGen
     */
    public MapperFactory(XDSBridgeConfig bridgeConfig, ContentParser tplGen) {

        super();
        this.xdsbridgeConfig = bridgeConfig;
        this.contentParser = tplGen;

        this.xdsMappers = createXDSMappers();
    }

    /**
     * Method description
     *
     *
     *
     * @param cfg
     * @return
     */
    public CDAToXDSMapper createCDAToXDSMapper(ContentParserConfig cfg) {

        return new CDAToXDSMapper(this.contentParser, cfg);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    private Map<ContentParserConfigName, IXDSMapper> createXDSMappers() {

        Map<ContentParserConfigName, IXDSMapper> result =
            new EnumMap<ContentParserConfigName,
                        IXDSMapper>(ContentParserConfigName.class);

        for (DocumentTypeMapping mapping :
                this.xdsbridgeConfig.getDocumentTypeMappings()) {

            ContentParserConfig config = mapping.getContentParserConfig();
            ContentParserConfigName name = config.getName();

            if (result.containsKey(name) == false) {

                switch (name) {

                    case SharedHealthSummaryMapper :
                    case DischargeSummaryMapper :
                        logger.debug(String.format("Creating %s mapper.",
                                                   name.toString()));
                        result.put(name, createCDAToXDSMapper(config));
                        break;
                    default:
                        logger.warn(String.format("Unknown mapper %s.",
                                                   name.toString()));
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
    private ContentParserConfig findContentParserConfig(CodedValue type) {

        ContentParserConfig result = null;

        DocumentTypeMapping mapping =
            this.xdsbridgeConfig.findDocumentTypeMapping(type);

        if (mapping != null) {
            result = mapping.getContentParserConfig();
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
    public IXDSMapper getMapper(CodedValue type) {

        IXDSMapper result = null;

        ContentParserConfig parserConfig = findContentParserConfig(type);

        if (parserConfig != null) {

            result = this.xdsMappers.get(parserConfig.getName());
        }

        return result;
    }
}
