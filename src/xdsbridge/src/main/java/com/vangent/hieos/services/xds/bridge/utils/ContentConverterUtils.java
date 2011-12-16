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
package com.vangent.hieos.services.xds.bridge.utils;

import com.vangent.hieos.xutil.hl7.date.Hl7Date;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class ContentConverterUtils {

    public static final Logger logger = Logger.getLogger(ContentConverterUtils.class);

    /**
     *
     * @param variable
     * @param value
     * @param contentConversion
     * @return
     */
    public static String convert(String variable, String value, String contentConversion) {
        String result = value;
        if (logger.isDebugEnabled()) {
            logger.debug("++++ variable converstion ++++");
            logger.debug("... variable = " + variable);
            logger.debug("... converter = " + contentConversion);
        }
        // FIXME: Should make converters configurable.
        if (contentConversion.equalsIgnoreCase("toDTM_UTCWithNoOffset")) {
            result = Hl7Date.toDTM_UTCWithNoOffset(value);
        }
        return result;
    }
}
