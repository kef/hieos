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

package com.vangent.hieos.xutil.xua.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 *
 * @author Bernie Thuman
 */
public class XUAUtil {
    /**
     *
     * @return
     */
    static public String getCreatedTime() {
        return XUAUtil.getTimeUTCFormat(0);
    }

    /**
     *
     * @return
     */
    static public String getExpireTime() {
        return XUAUtil.getTimeUTCFormat(1);
    }

    // FIXME: Move to reusable code location / consider rework.
    /**
     *
     * @param daysOffset
     * @return
     */
    static public String getTimeUTCFormat(int daysOffset) {
        StringBuilder sb = new StringBuilder();
        Calendar c = new GregorianCalendar();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.setTime(new Date());  // Now.
        c.add(Calendar.DATE, daysOffset);

        // Send all output to the Appendable object sb
        Formatter formatter = new Formatter(sb, Locale.US);

        //"2011-01-20T17:23:33.011Z";
        formatter.format("%s-%02d-%02dT%02d:%02d:%02d.%03dZ",
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH) + 1,
                c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                c.get(Calendar.SECOND),
                c.get(Calendar.MILLISECOND));
        String formattedTime = sb.toString();
        return formattedTime;
    }
}
