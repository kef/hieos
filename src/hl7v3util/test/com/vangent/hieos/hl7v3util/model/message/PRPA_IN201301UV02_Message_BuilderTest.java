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

package com.vangent.hieos.hl7v3util.model.message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import com.vangent.hieos.hl7v3util.model.builder.BuilderHelper;
import com.vangent.hieos.hl7v3util.model.subject.DeviceInfo;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifier;
import com.vangent.hieos.hl7v3util.model.subject.SubjectIdentifierDomain;
import com.vangent.hieos.xutil.xml.XPathHelper;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.util.XMLPrettyPrinter;
import org.apache.log4j.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-29
 * @author         Jim Horner
 */
public class PRPA_IN201301UV02_Message_BuilderTest {

    /** Field description */
    private static final Logger logger =
        Logger.getLogger(PRPA_IN201301UV02_Message_BuilderTest.class);

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void buildPRPA_IN201301UV02_MessageTest() throws Exception {

        DeviceInfo sndDeviceInfo = new DeviceInfo();

        sndDeviceInfo.setId("SNDTESTID");
        sndDeviceInfo.setName("SNDTESTNAME");

        DeviceInfo rcvDeviceInfo = new DeviceInfo();

        rcvDeviceInfo.setId("RCVTESTID");
        rcvDeviceInfo.setName("RCVTESTNAME");

        PRPA_IN201301UV02_Message_Builder builder =
            new PRPA_IN201301UV02_Message_Builder(sndDeviceInfo, rcvDeviceInfo);

        SubjectIdentifier subjectIdentifier = new SubjectIdentifier();

        String extension = "TEST007";

        subjectIdentifier.setIdentifier(extension);
        subjectIdentifier.setIdentifierDomain(new SubjectIdentifierDomain());
        subjectIdentifier.getIdentifierDomain().setUniversalId("1.2.3.4.5");

        PRPA_IN201301UV02_Message msg301 =
            builder.buildPRPA_IN201301UV02_Message(subjectIdentifier);

        assertNotNull(msg301);

        logger.debug(toPrettyString(msg301.getMessageNode()));

        String testid =
            XPathHelper.stringValueOf(
                msg301.getMessageNode(),
                "./ns:controlActProcess/ns:subject/ns:registrationEvent/ns:subject1/ns:patient[1]/ns:id/@extension",
                BuilderHelper.HL7V3_NAMESPACE);

        assertEquals(extension, testid);
    }

    /**
     * Method description
     *
     *
     * @param element
     *
     * @return
     */
    private String toPrettyString(OMElement element) {

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
}
