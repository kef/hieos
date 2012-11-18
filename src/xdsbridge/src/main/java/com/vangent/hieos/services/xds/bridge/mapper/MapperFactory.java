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

import java.util.Map;
import com.vangent.hieos.subjectmodel.CodedValue;
import com.vangent.hieos.services.xds.bridge.support.XDSBridgeConfig;
import java.util.HashMap;
import org.apache.log4j.Logger;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-13
 * @author         Vangent
 */
public class MapperFactory {

    /** Field description */
    private static final Logger logger = Logger.getLogger(MapperFactory.class);
    /** Field description */
    private final ContentParser contentParser;
    /** Field description */
    private final Map<String, IXDSMapper> xdsMappers;
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
        this.xdsMappers = initializeXDSMappers();
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
     * Method to convert from a document type to a document mapper.
     *
     * @param type document type
     *
     * @return a document mapper
     */
    public IXDSMapper getMapper(CodedValue type) {
        IXDSMapper result = null;
        ContentParserConfig parserConfig = findContentParserConfig(type);
        if (parserConfig != null) {
            result = this.xdsMappers.get(parserConfig.getName());
        }
        return result;
    }

    /**
     * Initializes all mappers and stores them in a map.
     *
     * @return a map containing all known mappers
     */
    private Map<String, IXDSMapper> initializeXDSMappers() {
        Map<String, IXDSMapper> result = new HashMap<String, IXDSMapper>();
        for (DocumentTypeMapping mapping : this.xdsbridgeConfig.getDocumentTypeMappings()) {
            ContentParserConfig config = mapping.getContentParserConfig();
            String name = config.getName();
            if (result.containsKey(name) == false) {
                logger.debug(String.format("Creating %s mapper.", name.toString()));
                result.put(name, createCDAToXDSMapper(config));
                /*
                switch (name) {

                case SharedHealthSummaryMapper :
                case DischargeSummaryMapper :
                logger.debug(String.format("Creating %s mapper.",
                name.toString()));
                result.put(name, createCDAToXDSMapper(config));

                break;

                default :
                logger.warn(String.format("Unknown mapper %s.",
                name.toString()));
                }*/
            }
        }
        return result;
    }
}
