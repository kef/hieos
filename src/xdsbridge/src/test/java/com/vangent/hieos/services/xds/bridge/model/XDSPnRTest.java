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

package com.vangent.hieos.services.xds.bridge.model;

import java.io.IOException;
import java.io.InputStream;
import com.vangent.hieos.xutil.iosupport.Io;
import org.apache.log4j.Logger;

import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-14
 * @author         Jim Horner
 */
public class XDSPnRTest {

    /** Field description */
    private static final String PNRFILE =
        "META-INF/templates/ProvideAndRegisterMetadata.xml";

    /** Field description */
    private static String pnrTemplate = null;

    /** Field description */
    private static final Logger logger = Logger.getLogger(XDSPnRTest.class);

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @BeforeClass
    public static void beforeClass() throws Exception {

        // we're going to cheat and load the template
        pnrTemplate = readPNRTemplate();
    }

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws IOException
     */
    private static String readPNRTemplate() throws IOException {

        String result = null;

        ClassLoader cl = XDSPnRTest.class.getClassLoader();
        InputStream is = null;

        try {

            is = cl.getResourceAsStream(PNRFILE);
            assertNotNull(is);
            result = Io.getStringFromInputStream(is);

        } catch (IOException e) {

            logger.error(String.format("Unable to read %s.", PNRFILE), e);

            throw e;

        } finally {

            if (is != null) {

                try {
                    is.close();
                } catch (IOException e) {

                    logger.warn(String.format("Unable to close %s.", PNRFILE),
                                e);
                }
            }
        }

        return result;
    }

}
