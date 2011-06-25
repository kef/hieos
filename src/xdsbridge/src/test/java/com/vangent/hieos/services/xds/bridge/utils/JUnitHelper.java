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

import com.vangent.hieos.services.xds.bridge.mapper.ContentParserConfig;
import com.vangent.hieos.services.xds.bridge.mapper.ContentParserConfig
    .ContentParserConfigName;
import com.vangent.hieos.services.xds.bridge.serviceimpl.XDSBridgeConfig;
import com.vangent.hieos.services.xds.bridge.serviceimpl.support
    .MockXConfigActor;
import com.vangent.hieos.services.xds.bridge.transaction
    .SubmitDocumentRequestHandlerTest;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xml.XMLParser;

import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import java.net.URL;

import java.util.Properties;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-22
 * @author         Jim Horner
 */
public class JUnitHelper {

    /** Field description */
    private final static Logger logger =
        Logger.getLogger(SubmitDocumentRequestHandlerTest.class);

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws Exception
     */
    public static ContentParserConfig createCDAToXDSContentParserConfig()
            throws Exception {

        XDSBridgeConfig cfg = createXDSBridgeConfig();

        return cfg.findContentParserConfig(
            ContentParserConfigName.CDAToXDSMapper);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public static XConfigActor createXDSBridgeActor() {

        System.setProperty(
            XConfig.SYSPROP_HIEOS_XDSBRIDGE_DIR,
            String.format(
                "%s/src/test/resources/config/xdsbridge",
                System.getProperty("user.dir")));

        Properties props = new Properties();

        props.setProperty(XDSBridgeConfig.CONFIG_FILE_PROP,
                          "TESTxdsbridgeconfig.xml");

        props.setProperty(XDSBridgeConfig.TEMPLATE_PROP,
                          "TESTProvideAndRegisterMetadata.xml");

        return new MockXConfigActor(props);
    }

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws Exception
     */
    public static XDSBridgeConfig createXDSBridgeConfig() throws Exception {

        XConfigActor bridgeActor = createXDSBridgeActor();

        String cfgfile =
            bridgeActor.getProperty(XDSBridgeConfig.CONFIG_FILE_PROP);

        assertNotNull(cfgfile);

        String tplfile = bridgeActor.getProperty(XDSBridgeConfig.TEMPLATE_PROP);

        assertNotNull(tplfile);

        return XDSBridgeConfig.parseConfigFile(bridgeActor);
    }

//  /**
//   * Method description
//   *
//   *
//   * @return
//   *
//   * @throws Exception
//   */
//  public static XConfig createXConfigInstance() throws Exception {
//
//      XConfig xconfig = null;
//
//      try {
//
//          String configLoc = String.format("%s/src/test/resources/config",
//                                           System.getProperty("user.dir"));
//
//          System.setProperty(XConfig.SYSPROP_HIEOS_CONFIG_DIR, configLoc);
//
//          String xconfigFile =
//              String.format("%s/src/test/resources/config/TESTxconfig.xml",
//                            System.getProperty("user.dir"));
//
//          System.setProperty(XConfig.SYSPROP_HIEOS_XCONFIG_FILE, xconfigFile);
//          
//          xconfig = XConfig.getInstance();
//
//      } catch (XConfigException e) {
//
//          // refuse to deploy
//          throw new IllegalStateException(e);
//      }
//
//      return xconfig;
//  }

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
