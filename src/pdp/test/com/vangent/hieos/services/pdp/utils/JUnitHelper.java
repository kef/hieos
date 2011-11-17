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

import java.io.File;
import java.net.URL;
import com.vangent.hieos.xutil.xml.XMLParser;
import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

import static org.junit.Assert.assertNotNull;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-22
 * @author         Vangent
 */
public class JUnitHelper {

    /** Field description */
    private static final Logger logger = Logger.getLogger(JUnitHelper.class);


    /**
     * Method description
     *
     *
     * @param file
     *
     * @return
     *
     * @throws Exception
     */
    public static OMElement fileToOMElement(String file) throws Exception {

        ClassLoader cl = JUnitHelper.class.getClassLoader();
        URL testfile = cl.getResource(file);

        assertNotNull(String.format("[%s] does not exist.", file), testfile);

        OMElement request = XMLParser.fileToOM(new File(testfile.getFile()));

        assertNotNull(request);

        logger.debug(DebugUtils.toPrettyString(request));

        return request;
    }
}
