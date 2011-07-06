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

import com.vangent.hieos.hl7v3util.model.subject.CodedValue;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-24
 * @author         Jim Horner
 */
public class DocumentTypeMapping {

    /** Field description */
    private final ContentParserConfig contentParserConfig;

    /** Field description */
    private final CodedValue format;

    /** Field description */
    private final String mimeType;

    /** Field description */
    private final CodedValue type;

    /**
     * Constructs ...
     *
     *
     * @param type
     * @param format
     * @param mimeType
     * @param parserConfig
     */
    public DocumentTypeMapping(CodedValue type, CodedValue format,
                               String mimeType,
                               ContentParserConfig parserConfig) {

        super();
        this.type = type;
        this.format = format;
        this.mimeType = mimeType;
        this.contentParserConfig = parserConfig;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public ContentParserConfig getContentParserConfig() {
        return contentParserConfig;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public CodedValue getFormat() {
        return format;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public CodedValue getType() {
        return type;
    }
}
