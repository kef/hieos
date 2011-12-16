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

/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */

package com.vangent.hieos.xutil.hl7.date;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author hornja
 */
public class Hl7DateTest {

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void getDateFromHL7FormatTest() throws Exception {

        DateFormat df = new SimpleDateFormat("yyyyMMdd");

        // Test 1
        String dstr = "201105271150";
        Date date = Hl7Date.toDate(dstr);

        assertNotNull(date);

        String dteststr = df.format(date);

        assertEquals("20110527", dteststr);

        // Test 2
        dstr = "20110527";
        date = Hl7Date.toDate(dstr);

        assertNotNull(date);
        dteststr = df.format(date);

        assertEquals("20110527", dteststr);

        // Test 3
        dstr = "2011052712";
        date = Hl7Date.toDate(dstr);

        assertNotNull(date);
        dteststr = df.format(date);

        assertEquals("20110527", dteststr);

        // Test 4
        dstr = "201105";
        date = Hl7Date.toDate(dstr);

        assertNotNull(date);
        dteststr = df.format(date);

        assertEquals("20110501", dteststr);

         // Some examples:
        String dtmExamples[] = {
            "2011",
            "201112",
            "20111216",
            "2011121615",
            "201112161513",
            "20111216151330",
            "2011-0500",
            "201112-0500",
            "20111216-0500",
            "2011121615-0500",
            "201112161513-0500",
            "20111216151330-0500",
            Hl7Date.now()
        };
        for (String dtmExample : dtmExamples) {
            System.out.println("DTM = " + dtmExample);
            Date dateExample = Hl7Date.toDate(dtmExample);
            System.out.println(" .. DTM -> Java Date [default TZ] = " + dateExample);
            System.out.println(" .. Java Date -> DTM[default TZ] = " + Hl7Date.toDTM_DefaultTimeZone(dateExample));
            System.out.println(" .. Java Date -> DTM[UTC TZ] = " + Hl7Date.toDTM_UTCTimeZone(dateExample));
            System.out.println(" .. DTM->DTM(UTC) = " + Hl7Date.toDTM_UTCWithNoOffset(dtmExample));
        }

    }
}
