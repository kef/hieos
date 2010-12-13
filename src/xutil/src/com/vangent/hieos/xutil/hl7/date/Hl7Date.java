/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2008-2009 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.xutil.hl7.date;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Class to produce dates in HL7 format.
 *
 */
public class Hl7Date {

    /**
     * Do not allow instances to be created.
     */
    private Hl7Date() {}

    /**
     * Return current time in HL7 format as: YYYYMMDDHHMMSS.
     *
     * @return HL7 date as string.
     */
    static public String now() {
        StringBuilder sb = new StringBuilder();
        // Send all output to the Appendable object sb
        Formatter formatter = new Formatter(sb, Locale.US);
        Calendar c = new GregorianCalendar();
        formatter.format("%s%02d%02d%02d%02d%02d",
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH) + 1,
                c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                c.get(Calendar.SECOND));
        return sb.toString();
    }

    /**
     * Return current time (minus 1 year) in HL7 format as YYYYMMDDHHMMSS.  This method
     * has no practical purpose beyond for test support.
     *
     * @return Current time (minus 1 year) in HL7 format.
     */
    static public String lastyear() {
        StringBuilder sb = new StringBuilder();
        // Send all output to the Appendable object sb
        Formatter formatter = new Formatter(sb, Locale.US);
        Calendar c = new GregorianCalendar();
        formatter.format("%s%02d%02d%02d%02d%02d",
                c.get(Calendar.YEAR) - 1,
                c.get(Calendar.MONTH) + 1,
                c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                c.get(Calendar.SECOND));
        return sb.toString();
    }

    /**
     * Convert a Java date to HL7 format.
     *
     * @param date Java date.
     * @return String In HL7 format.
     */
    static public String toHL7format(Date date) {
        String hl7DateFormat = "yyyyMMdd";
        SimpleDateFormat formatter = new SimpleDateFormat(hl7DateFormat);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        formatter.setTimeZone(timeZone);

        String hl7formattedDate = formatter.format(date);
        hl7formattedDate.replaceAll("UTC","Z");
        return hl7formattedDate;
    }

}
