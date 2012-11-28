/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2012 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.empi.pixpdq.loader;

import com.vangent.hieos.xutil.hl7.date.Hl7Date;
import com.vangent.hieos.xutil.soap.Soap;
import com.vangent.hieos.xutil.template.TemplateUtil;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.axiom.om.OMElement;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Bernie Thuman
 */
public class PIDFeedRandomLoader {

    // Hack - change variables to vary test.
    private final static String ENDPOINT = "http://localhost:8080/axis2/services/pixmgr";
    private final static String TEMPLATE = "test\\PIDFeedTemplate.xml";
    private final static int MAX_THREADS = 16;
    private final static int RUN_COUNT_PER_THREAD = 250000;
    private final static String ENTERPRISE_ASSIGNING_AUTHORITY = "1.3.6.1.4.1.21367.13.20.1000";
    private final static String LOCAL_ASSIGNING_AUTHORITY = "1.3.6.1.4.1.21367.1000.1.6";

    /**
     *
     */
    public class PIDFeedRunnable implements Runnable {

        private String templateFileName;
        private String assigningAuthority;
        private String localAssigningAuthority;
        private String endpoint;
        private long maxRuns;

        /**
         * 
         * @param templateFileName
         * @param assigningAuthority
         * @param localAssigningAuthority
         * @param maxRuns
         * @param endpoint
         */
        public PIDFeedRunnable(String templateFileName, String assigningAuthority, String localAssigningAuthority, long maxRuns, String endpoint) {
            this.templateFileName = templateFileName;
            this.assigningAuthority = assigningAuthority;
            this.localAssigningAuthority = localAssigningAuthority;
            this.maxRuns = maxRuns;
            this.endpoint = endpoint;
        }

        /**
         *
         */
        @Override
        public void run() {
            try {
                String templateText = FileUtils.readFileToString(new File(templateFileName));
                RandomString randomString = new RandomString();
                for (int run = 0; run < maxRuns; run++) {
                    System.out.print("PIDv3 Feed start ... threadid=" + Thread.currentThread().getId() + ", #" + run + " ...");
                    Map<String, String> variables = new HashMap<String, String>();
                    variables.put("PATIENT_ID", this.getUniqueId());
                    variables.put("ASSIGNING_AUTHORITY", assigningAuthority);
                    variables.put("LOCAL_PATIENT_ID", this.getUniqueId());
                    variables.put("LOCAL_ASSIGNING_AUTHORITY", localAssigningAuthority);
                    variables.put("FAMILY_NAME", randomString.get(20));
                    variables.put("GIVEN_NAME", randomString.get(15));
                    variables.put("BIRTH_TIME", Hl7Date.now());
                    variables.put("STREET_ADDRESS_LINE", randomString.get(20));
                    variables.put("CITY", randomString.get(15));
                    variables.put("STATE", randomString.get(2));
                    variables.put("POSTAL_CODE", randomString.get(5));
                    variables.put("GENDER_CODE", (run % 2) == 0 ? "M" : "F");
                    variables.put("RACE_CODE", "W");
                    variables.put("SSN", randomString.get(3)
                            + "-" + randomString.get(2) + "-" + randomString.get(4));

                    OMElement pidFeedNode = TemplateUtil.getOMElementFromTemplate(templateText, variables);
                    //System.out.println("+++++++++++++++++++");
                    //System.out.println(pidFeedNode.toString());
                    Soap soap = new Soap();
                    long start = System.currentTimeMillis();
                    OMElement response = soap.soapCall(pidFeedNode, endpoint, false, true, true, "urn:hl7-org:v3:PRPA_IN201301UV02", "urn:hl7-org:v3:MCCI_IN000002UV01");
                    //System.out.println("+++++++++++++++++++");
                    //System.out.println(response.toString());
                    System.out.println(" done ... TOTAL TIME - " + (System.currentTimeMillis() - start) + "ms.");
                }
            } catch (Exception ex) {
                // Do something ...
                System.out.println("EXCEPTION: " + ex.getMessage());
            }
        }

        /**
         * 
         * @return
         */
        private String getUniqueId() {

            // Generate UUID.
            UUID uuid = UUID.randomUUID();
            // Get rid of dashes before returning unique identifier.
            return uuid.toString().replaceAll("-", "");
        }
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        PIDFeedRandomLoader pfl = new PIDFeedRandomLoader();
        long start = System.currentTimeMillis();
        Thread[] threads = new Thread[MAX_THREADS];
        for (int i = 0; i < MAX_THREADS; i++) {
            PIDFeedRunnable pidFeedRunnable = pfl.getPIDFeedRunnable(TEMPLATE,
                    ENTERPRISE_ASSIGNING_AUTHORITY, LOCAL_ASSIGNING_AUTHORITY,
                    RUN_COUNT_PER_THREAD, ENDPOINT);
            threads[i] = new Thread(pidFeedRunnable);
            threads[i].start();
        }
        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ignore) {
            }
        }
        long elapsedTime = System.currentTimeMillis() - start;
        double txnSec = ((double) MAX_THREADS * (double) RUN_COUNT_PER_THREAD) / 
                ((double)elapsedTime / 1000.0);
        System.out.println(" done ... TOTAL TIME = " + elapsedTime + "ms" + ", txn/sec = "
                + txnSec);
    }

    /**
     *
     * @param templateFileName
     * @param assigningAuthority
     * @param maxRuns
     * @param endpoint
     * @return
     */
    private PIDFeedRunnable getPIDFeedRunnable(String templateFileName, String assigningAuthority, String localAssigningAuthority, int maxRuns, String endpoint) {
        return new PIDFeedRunnable(templateFileName, assigningAuthority, localAssigningAuthority, maxRuns, endpoint);
    }
}
