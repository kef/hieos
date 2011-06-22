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

import java.util.HashMap;
import java.util.Map;
import com.vangent.hieos.hl7v3util.model.subject.CodedValue;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-13
 * @author         Jim Horner
 */
public class MapperFactory {

    /** Field description */
    private final static Map<String, IXDSMapper> cache = new HashMap<String,
                                                             IXDSMapper>();

    /** Field description */
    private final ContentParser contentParser;

    /**
     * Constructs ...
     *
     *
     * @param tplGen
     */
    public MapperFactory(ContentParser tplGen) {

        super();
        this.contentParser = tplGen;
    }

    /**
     * Method description
     *
     *
     * @param tplGen
     * @param mycache
     *
     * @return
     */
    protected MapperFactory(ContentParser tplGen,
                            Map<String, IXDSMapper> mycache) {

        this(tplGen);

        cache.putAll(mycache);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public CDAToXDSMapper createCDAToXDSMapper() {

        ContentParserConfig cfg =
            CDAToXDSContentParserConfigFactory.createConfig();

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
    public IXDSMapper getMapper(CodedValue type) {

        IXDSMapper result = null;

        // if/else/then or a switch to create a key
        // only know about CDA
        String key = CDAToXDSMapper.class.getName();

        synchronized (cache) {

            result = cache.get(key);

            if (result == null) {

                ContentParserConfig cfg =
                    CDAToXDSContentParserConfigFactory.createConfig();

                result = new CDAToXDSMapper(this.contentParser, cfg);

                cache.put(key, result);
            }
        }

        return result;
    }
}
