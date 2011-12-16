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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.commons.lang.time.DateUtils;

/**
 * Class to produce dates in HL7 format.
 *
 */
public class Hl7Date {

    /**
     * Do not allow instances to be created.
     */
    private Hl7Date() {
    }

    /**
     * Return current time in HL7 format as: YYYYMMDDHHMMSS.
     *
     * @return HL7 date as string.
     */
    static public String now() {
        //StringBuilder sb = new StringBuilder();
        // Send all output to the Appendable object sb
        //Formatter formatter = new Formatter(sb, Locale.US);
       return Hl7Date.toDTM_DefaultTimeZone(new Date());
        /*        formatter.format("%s%02d%02d%02d%02d%02d",
        c.get(Calendar.YEAR),
        c.get(Calendar.MONTH) + 1,
        c.get(Calendar.DAY_OF_MONTH),
        c.get(Calendar.HOUR_OF_DAY),
        c.get(Calendar.MINUTE),
        c.get(Calendar.SECOND));
        return sb.toString();*/
    }

    /**
     * Return current time in HL7 format as: YYYYMMDDHHMMSS.
     *
     * @return HL7 date as string.
     */
    static public String nowUTC() {
        //StringBuilder sb = new StringBuilder();
        // Send all output to the Appendable object sb
        //Formatter formatter = new Formatter(sb, Locale.US);
        return Hl7Date.toDTM_UTCTimeZone(new Date());
        /*        formatter.format("%s%02d%02d%02d%02d%02d",
        c.get(Calendar.YEAR),
        c.get(Calendar.MONTH) + 1,
        c.get(Calendar.DAY_OF_MONTH),
        c.get(Calendar.HOUR_OF_DAY),
        c.get(Calendar.MINUTE),
        c.get(Calendar.SECOND));
        return sb.toString();*/
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
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        String hl7formattedDate = formatter.format(date);
        hl7formattedDate.replaceAll("UTC", "Z");
        return hl7formattedDate;
    }
    
    // DTM format: YYYY[MM[DD[HH[MM[SS[.S[S[S[S]]]]]]]]][+/-ZZZZ]
    static private String _dtmParsePatterns[] = {
        "yyyyMMddHHmmssZ",
        "yyyyMMddHHmmZ",
        "yyyyMMddHHZ",
        "yyyyMMddZ",
        "yyyyMMZ",
        "yyyyZ",
        "yyyyMMddHHmmss",
        "yyyyMMddHHmm",
        "yyyyMMddHH",
        "yyyyMMdd",
        "yyyyMM",
        "yyyy"};

    /**
     *
     * @param inputDTM
     * @return
     */
    static public String toDTM_UTCWithNoOffset(String inputDTM) {
        // Get a Java date instance from the DTM string.
        Date date = Hl7Date.toDate(inputDTM);

        // Convert the Java date to DTM in UTC but without the time zone component.
        String outputDTM = Hl7Date.toDTM_UTCTimeZone(date);

        // Now, trim string to original precision.
        int inputDTMLengthWithoutTZ;
        // See if the original DTM string had a time zone
        if (inputDTM.contains("-") || inputDTM.contains("+")) {
            String[] splits = inputDTM.split("[-+]");
            String inputDTMwithoutTZ = splits[0];  // input DTM (with no time zone).
            inputDTMLengthWithoutTZ = inputDTMwithoutTZ.length();  // Num chars before time zone.
        } else {
            inputDTMLengthWithoutTZ = inputDTM.length();
        }
        outputDTM = outputDTM.substring(0, inputDTMLengthWithoutTZ);
        return outputDTM;
    }

    /**
     * 
     * @param inputDTM
     * @return
     */
    static public Date toDate(String inputDTM) {
        try {
            Date date = DateUtils.parseDate(inputDTM, _dtmParsePatterns);
            return date;
        } catch (ParseException ex) {
            // FIXME: Do something?
        }
        return null;
    }

    /**
     *
     * @param date
     * @return
     */
    static public String toDTM_DefaultTimeZone(Date date) {
        Calendar c = new GregorianCalendar();
        c.setTime(date);
        return Hl7Date.formatDTM(c);
    }

    /**
     *
     * @param date
     * @return
     */
    static public String toDTM_UTCTimeZone(Date date) {
        Calendar c = new GregorianCalendar();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.setTime(date);
        return Hl7Date.formatDTM(c);
    }

    /**
     * 
     * @param c
     * @return
     */
    static private String formatDTM(Calendar c) {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.US);
        formatter.format("%s%02d%02d%02d%02d%02d",
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH) + 1,
                c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                c.get(Calendar.SECOND));
        String formattedTime = sb.toString();
        return formattedTime;
    }
}
