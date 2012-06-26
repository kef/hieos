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

import com.vangent.hieos.xutil.soap.Soap;
import com.vangent.hieos.xutil.template.TemplateUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.axiom.om.OMElement;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Bernie Thuman
 */
public class PIDFeedLoader {

    // Hack - change variables to vary test.
    private final static String ENDPOINT = "http://localhost:8080/axis2/services/pixmgr";
    private final static String TEMPLATE = "test\\PIDFeedTemplate.xml";
    private final static String DEMOGRAPHIC_FILES[] = {"test\\demographics-red.csv", "test\\demographics-green.csv", "test\\demographics-blue.csv"};

    // Positions in file to extract demographic elements.
    private final static int PATIENT_ID_TOKEN_POS = 2;
    private final static int PATIENT_ID_ASSIGNING_AUTHORITY_TOKEN_POS = 4;
    private final static int LOCAL_PATIENT_ID_TOKEN_POS = 5;
    private final static int LOCAL_PATIENT_ID_ASSIGNING_AUTHORITY_TOKEN_POS = 7;
    private final static int PATIENT_NAME_TOKEN_POS = 8;
    private final static int PATIENT_DOB_TOKEN_POS = 9;
    private final static int STREET_ADDRESS_LINE_TOKEN_POS = 10;
    private final static int CITY_TOKEN_POS = 11;
    private final static int STATE_TOKEN_POS = 12;
    private final static int POSTAL_CODE_TOKEN_POS = 13;
    private final static int GENDER_CODE_TOKEN_POS = 14;
    private final static int RACE_CODE_TOKEN_POS = 15;

    /**
     *
     */
    public class PIDFeedRunnable implements Runnable {

        private String templateFileName;
        private String csvFileName;
        private String endpoint;

        /**
         *
         * @param templateFileName
         * @param csvFileName
         * @param endpoint
         */
        public PIDFeedRunnable(String templateFileName, String csvFileName, String endpoint) {
            this.templateFileName = templateFileName;
            this.csvFileName = csvFileName;
            this.endpoint = endpoint;
        }

        /**
         * 
         */
        @Override
        public void run() {
            try {
                RandomString randomString = new RandomString();
                String templateText = FileUtils.readFileToString(new File(templateFileName));
                BufferedReader br = new BufferedReader(new FileReader(csvFileName));
                String strLine = "";
                StringTokenizer st = null;
                int lineNumber = 0, tokenNumber = 0;
                while ((strLine = br.readLine()) != null) {
                    lineNumber++;
                    //Thread.sleep(250);
                    System.out.print("PIDv3 Feed start ... threadid=" + Thread.currentThread().getId() + ", #" + lineNumber + " ...");
                    st = new StringTokenizer(strLine, ",");
                    Map<String, String> variables = new HashMap<String, String>();
                    while (st.hasMoreTokens()) {
                        tokenNumber++;
                        String token = st.nextToken();
                        if (tokenNumber == PATIENT_ID_TOKEN_POS) {
                            variables.put("PATIENT_ID", token);
                        } else if (tokenNumber == PATIENT_ID_ASSIGNING_AUTHORITY_TOKEN_POS) {
                            StringTokenizer st2 = new StringTokenizer(token, "&");
                            String assigningAuthority = st2.nextToken();
                            variables.put("ASSIGNING_AUTHORITY", assigningAuthority);
                        } else if (tokenNumber == LOCAL_PATIENT_ID_TOKEN_POS) {
                            variables.put("LOCAL_PATIENT_ID", token);
                        } else if (tokenNumber == LOCAL_PATIENT_ID_ASSIGNING_AUTHORITY_TOKEN_POS) {
                            StringTokenizer st2 = new StringTokenizer(token, "&");
                            String assigningAuthority = st2.nextToken();
                            variables.put("LOCAL_ASSIGNING_AUTHORITY", assigningAuthority);
                        } else {
                            switch (tokenNumber) {
                                case PATIENT_NAME_TOKEN_POS:
                                    StringTokenizer st4 = new StringTokenizer(token, "^");
                                    String familyName = st4.nextToken();
                                    String givenName = st4.nextToken();
                                    variables.put("FAMILY_NAME", familyName);
                                    variables.put("GIVEN_NAME", givenName);
                                    break;
                                case PATIENT_DOB_TOKEN_POS:
                                    variables.put("BIRTH_TIME", token);
                                    break;
                                case STREET_ADDRESS_LINE_TOKEN_POS:
                                    variables.put("STREET_ADDRESS_LINE", token);
                                    break;
                                case CITY_TOKEN_POS:
                                    variables.put("CITY", token);
                                    break;
                                case STATE_TOKEN_POS:
                                    variables.put("STATE", token);
                                    break;
                                case POSTAL_CODE_TOKEN_POS:
                                    variables.put("POSTAL_CODE", token);
                                    break;
                                case GENDER_CODE_TOKEN_POS:
                                    variables.put("GENDER_CODE", token);
                                    break;
                                case RACE_CODE_TOKEN_POS:
                                    variables.put("RACE_CODE", token);
                                    break;
                                default:
                                    //System.out.println("***** CANT DO ANYTHING HERE *****");
                                    break;
                            }
                        }
                        //System.out.println("Line # " + lineNumber
                        //        + ", Token # " + tokenNumber
                        //        + ", Token : " + token);
                    }
                    // Randomize SSN.
                    variables.put("SSN", randomString.get(3)
                            + "-" + randomString.get(2) + "-" + randomString.get(4));
                    tokenNumber = 0;
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
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        PIDFeedLoader pfl = new PIDFeedLoader();
        long start = System.currentTimeMillis();
        for (int i = 0; i < DEMOGRAPHIC_FILES.length; i++) {
            String demographicFile = DEMOGRAPHIC_FILES[i];
            PIDFeedRunnable pidFeedRunnable = pfl.getPIDFeedRunnable(
                    TEMPLATE,
                    demographicFile,
                    ENDPOINT);
            Thread thread = new Thread(pidFeedRunnable);
            thread.start();
        }
        System.out.println(" done ... TOTAL TIME - " + (System.currentTimeMillis() - start) + "ms.");
    }

    /**
     *
     * @param templateFileName
     * @param csvFileName
     * @param endpoint
     * @return
     */
    private PIDFeedRunnable getPIDFeedRunnable(String templateFileName, String csvFileName, String endpoint) {
        return new PIDFeedRunnable(templateFileName, csvFileName, endpoint);
    }
}
