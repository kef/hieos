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
package com.vangent.hieos.xutil.xua.utils;

import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class XUAConfig {

    private final static Logger logger = Logger.getLogger(XUAConfig.class);
    static XUAConfig _instance = null;
    XConfigObject configObject = null;
    List<String> soapActionsList = new ArrayList<String>();
    List<String> constrainedIPList = new ArrayList<String>();

    /**
     * 
     * @return
     */
    static public synchronized XUAConfig getInstance() {
        if (_instance == null) {
            _instance = new XUAConfig();
        }
        return _instance;
    }

    /**
     *
     * @param config
     */
    private XUAConfig() {
        try {
            XConfig xconf = XConfig.getInstance();
            this.configObject = xconf.getXUAConfigProperties();
            init();
        } catch (XdsInternalException ex) {
            logger.fatal("UNABLE TO GET XConfig (for XUA): ", ex);
        }
    }

    /**
     * @param propKey
     * @return
     */
    public String getProperty(String propKey) {
        return configObject.getProperty(propKey);
    }

    /**
     *
     * @param propKey
     * @return
     */
    public boolean getPropertyAsBoolean(String propKey) {
        return configObject.getPropertyAsBoolean(propKey);
    }

    /**
     *
     * @param soapAction
     * @return
     */
    public boolean containsSOAPAction(String soapAction) {
        if (this.soapActionsList.isEmpty()) {
            // Always constrain if the SOAP action list is empty.
            return true;
        }
        if (this.soapActionsList.contains("all")) {
            // Constrain all SOAP actions.
            return true;
        }
        return this.soapActionsList.contains(soapAction.toLowerCase());
    }

    /**
     *
     * @param IPAddress
     * @return
     */
    public boolean IPAddressIsConstrained(String IPAddress) {
        // Check to see if all IP addresses should be constrained.
        if (this.constrainedIPList.isEmpty()) {
            // Always constrain if the IP list is empty.
            return true;
        }
        if (this.constrainedIPList.contains("all")) {
            // Constrain all IP addresses.
            return true;
        }
        // Only constrain if the IP address is on our list.
        return this.constrainedIPList.contains(IPAddress.toLowerCase());
    }

    /**
     *
     */
    private void init() {
        // Load SOAP actions to control.
        this.parseList("SOAPActions", this.soapActionsList);
        // Load IP addresses to control.
        this.parseList("ConstrainedIPAddresses", this.constrainedIPList);
    }

    /**
     *
     * @param propKey
     * @param targetList
     */
    private void parseList(String propKey, List targetList) {
        // Load IP addresses to control.
        String listUnParsed = this.getProperty(propKey);
        String[] listParsed = listUnParsed.split(";");
        for (int i = 0; i < listParsed.length; i++) {
            if (listParsed[i].length() != 0) {
                targetList.add(listParsed[i].toLowerCase());
            }
        }
    }

    /**
     *
     * @return
     */
    // FIXME: Rewrite / move to proper class
    static public String getCreatedTime() {
        return XUAConfig.getTimeUTCFormat(0);
        /*
        StringBuilder sb = new StringBuilder();
        // Send all output to the Appendable object sb
        Formatter formatter = new Formatter(sb, Locale.US);
        Calendar c = new GregorianCalendar();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));

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
        System.out.println("created time = " + formattedTime);
        return formattedTime;*/
    }

    /**
     *
     * @return
     */
    // FIXME: Rewrite / move to proper class
    static public String getExpireTime() {
        return XUAConfig.getTimeUTCFormat(1);
    }

    // FIXME: Move to reusable code location / consider rework.
    /**
     *
     * @param daysOffset
     * @return
     */
    // FIXME: Rewrite / move to proper class
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
        System.out.println("time = " + formattedTime);
        return formattedTime;
    }
}
