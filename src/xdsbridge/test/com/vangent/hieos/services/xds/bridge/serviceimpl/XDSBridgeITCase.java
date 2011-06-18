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

import com.vangent.hieos.services.xds.bridge.serviceimpl.support.MockXDSBridge;
import com.vangent.hieos.services.xds.bridge.serviceimpl.support
    .XDSBridgeTestConfigFactory;
import com.vangent.hieos.services.xds.bridge.utils.DebugUtils;
import com.vangent.hieos.xutil.xml.XMLParser;

import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import java.net.URL;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-13
 * @author         Jim Horner
 */
public class XDSBridgeITCase {

    /** Field description */
    private final static Logger logger =
        Logger.getLogger(XDSBridgeITCase.class);

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void submitDocumentRequestTest() throws Exception {

        // have to mock an xds brigde to override some axis2 methods
        XDSBridge xdsbridge =
            new MockXDSBridge(XDSBridgeTestConfigFactory.createConfig());

        ClassLoader cl = getClass().getClassLoader();
        URL testfile = cl.getResource("messages/test-sdr3-cda.xml");

        assertNotNull(testfile);

        OMElement test = XMLParser.fileToOM(new File(testfile.getFile()));

        assertNotNull(test);

        logger.debug(DebugUtils.toPrettyString(test));
        
        OMElement response = xdsbridge.SubmitDocumentRequest(test);

        assertNotNull(response);
    }
}
