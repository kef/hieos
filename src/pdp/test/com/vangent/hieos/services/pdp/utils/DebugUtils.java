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

package com.vangent.hieos.services.pdp.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.util.XMLPrettyPrinter;
import org.apache.log4j.Logger;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-09
 * @author         Jim Horner
 */
public class DebugUtils {

    /** Field description */
    private static final Logger logger = Logger.getLogger(DebugUtils.class);

    /**
     * Method description
     *
     *
     * @param element
     *
     * @return
     */
    public static String toPrettyString(OMElement element) {

        String result = null;

        if (element != null) {

            ByteArrayOutputStream os = null;

            try {

                os = new ByteArrayOutputStream();
                XMLPrettyPrinter.prettify(element, os);
                result = os.toString();

            } catch (Exception e) {

                // nothing can be done to recover
                logger.fatal("###### Unable to PrettyPrint.", e);

            } finally {

                if (os != null) {

                    try {
                        os.close();
                    } catch (IOException e) {

                        // nothing can be done
                        logger.warn("Unable to close ByteStream.", e);
                    }
                }
            }
        }

        return result;
    }

    public static String toPrettyString(Map<String, String> map) {

        StringBuilder sb = new StringBuilder();

        if (map != null) {

            for (Map.Entry<String, String> entry : map.entrySet()) {

                sb.append("[");
                sb.append(entry.getKey());
                sb.append("]=[");
                sb.append(entry.getValue());
                sb.append("]");
                sb.append("\n");
            }

        } else {
            
            sb.append("[null]");
        }

        return sb.toString();
    }
}
