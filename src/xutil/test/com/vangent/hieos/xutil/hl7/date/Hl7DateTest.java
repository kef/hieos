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
        Date date = Hl7Date.getDateFromHL7Format(dstr);

        assertNotNull(date);

        String dteststr = df.format(date);

        assertEquals("20110527", dteststr);

        // Test 2
        dstr = "20110527";
        date = Hl7Date.getDateFromHL7Format(dstr);

        assertNotNull(date);
        dteststr = df.format(date);

        assertEquals("20110527", dteststr);

        // Test 3
        dstr = "2011052712";
        date = Hl7Date.getDateFromHL7Format(dstr);

        assertNotNull(date);
        dteststr = df.format(date);

        assertEquals("20110527", dteststr);

        // Test 4
        dstr = "201105";
        date = Hl7Date.getDateFromHL7Format(dstr);

        assertNotNull(date);
        dteststr = df.format(date);

        assertEquals("00010101", dteststr);

    }
}
