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
package com.vangent.hieos.xtest.main;

import com.vangent.hieos.xtest.config.XTestConfig;
import com.vangent.hieos.xutil.exception.ExceptionUtil;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xtest.framework.TestConfig;
import com.vangent.hieos.xtest.framework.StringSub;
import com.vangent.hieos.xtest.framework.BasicTransaction;
import com.vangent.hieos.xtest.framework.PlanContext;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bernie Thuman (rewrote / cleanup ... still could be better).
 */
public class XTestDriver {

    static public XTestDriver driver;
    private String version = "xtest 1.0";
    private ArrayList only_steps = new ArrayList();
    private String args = "";
    private String logDir = null;
    private StringSub str_sub = new StringSub();

    /**
     * 
     * @param argv
     */
    static public void main(String[] argv) {
        XTestDriver.driver = new XTestDriver();
        driver.version = "xtest 1.0";
        for (int i = 0; i < argv.length; i++) {
            driver.args += argv[i] + " ";
        }
        String siteName = null;
        String testDir = null;
        String testCollectionName = null;
        boolean loop = false;
        boolean secure = false;
        try {
            for (int i = 0; i < argv.length; i++) {
                String cmd = argv[i];
                if (cmd.equals("-s")) {
                    i++;
                    if (i >= argv.length) {
                        System.out.println("-s missing value");
                        throw new Exception("");
                    }
                    siteName = argv[i];
                } else if (cmd.equals("-t")) {
                    i++;
                    if (i >= argv.length) {
                        System.out.println("-t missing value");
                        throw new Exception("");
                    }
                    testDir = argv[i];
                } else if (cmd.equals("-tc")) {
                    i++;
                    if (i >= argv.length) {
                        System.out.println("-tc missing value");
                        throw new Exception("");
                    }
                    testCollectionName = argv[i];
                } else if (cmd.equals("-secure")) {
                    secure = true;
                } else if (cmd.equals("-h")) {
                    driver.usage();
                    System.exit(0);
                } else if (cmd.equals("-loop")) {
                    loop = true;
                } else {
                    driver.usage();
                    System.exit(0);
                }
            }
        } catch (Exception e) {
            driver.usage();
            System.exit(-1);
        }
        try {
            TestConfig.secure = secure;
            driver.setConfig(siteName);
        } catch (XdsInternalException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
        List<TestSpec> testSpecs = null;
        if (testDir != null) {
            testSpecs = driver.findTestSpecs(testDir);
        } else if (testCollectionName != null) {
            testSpecs = driver.findTestSpecsForTestCollection(testCollectionName);
        } else {
            driver.usage();
            System.exit(0);
        }

        long testRunStartTime = System.currentTimeMillis();  // Start time of test run.
        for (;;) {
            for (TestSpec testSpec : testSpecs) {
                TestConfig.base_path = testSpec.getTestSpecDir();
                TestConfig.log_dir = driver.makeLogDir(testSpec);
                //System.out.println("test spec = " + TestConfig.base_path);
                boolean ok = driver.runTest(TestConfig.base_path);
                if (ok) {
                    System.out.println("...Pass");
                } else {
                    System.out.println("...Fail");
                }
            }
            if (!loop) {
                break;
            }
        }
        long testRunStopTime = System.currentTimeMillis();  // Stop time of test run.
        long testRunElapsedTime = testRunStopTime - testRunStartTime;
        System.out.println("\n\n---------------------------------  Test Summary  ------------------------------\n");
        System.out.println("\t Number of test specs: " + testSpecs.size());
        System.out.println("\t Elapsed time: " + testRunElapsedTime / 1000.0 + " seconds");
        System.exit(0);
    }

    /**
     * 
     * @return
     */
    public String getVersion() {
        return this.version;
    }

    /**
     *
     * @return
     */
    public String getArgs() {
        return this.args;
    }

    /**
     * 
     * @return
     */
    public ArrayList getOnlySteps() {
        return this.only_steps;
    }

    /**
     * 
     * @return
     */
    public String getLogDir() {
        return this.logDir;
    }

    /**
     *
     * @param siteName
     */
    private void setConfig(String siteName) throws XdsInternalException {
        // Test directory:
        String testDir = System.getenv("HIEOSxTestDir");
        if (testDir == null) {
            System.err.println("HIEOSxTestDir environment variable not specified!");
            System.exit(-1);
        }

        // FIXME: What if directory does not exist?
        TestConfig.testmgmt_dir = testDir + File.separatorChar;

        // Log directory:
        this.logDir = System.getenv("HIEOSxLogDir");
        if (this.logDir == null) {
            // Default
            this.logDir = TestConfig.testmgmt_dir + "logs" + File.separatorChar;
            System.out.println("HIEOSxLogDir environment variable not set ... default to " + this.logDir);
        }
  
        // Setup site configuration.
        this.setSiteConfig(siteName);
    }

    /**
     *
     * @param siteName
     * @throws XdsInternalException
     */
    private void setSiteConfig(String siteName) throws XdsInternalException {
        TestConfig.xtestConfig = XTestConfig.getInstance();
        if (siteName == null) {
            // get default from xtestconfig.xml
            siteName = TestConfig.xtestConfig.getDefaultSite();
        }
        TestConfig.pid_allocate_endpoint = TestConfig.xtestConfig.getPIDAllocateEndpoint(siteName);
        TestConfig.defaultRegistry = TestConfig.xtestConfig.getDefaultRegistry(siteName);
        TestConfig.defaultRepository = TestConfig.xtestConfig.getDefaultRepository(siteName);
        TestConfig.defaultInitiatingGateway = TestConfig.xtestConfig.getDefaultInitiatingGateway(siteName);
        TestConfig.target = siteName;
        System.out.println("Using site " + siteName);
    }

    /**
     *
     * @param testDir
     * @return
     */
    private List<TestSpec> findTestSpecs(String testDir) {
        String scriptsDir = TestConfig.testmgmt_dir + File.separatorChar + "scripts";
        // Loop through each test directory looking for directory.
        List<TestSpec> testSpecs = new ArrayList<TestSpec>();
        String[] sections = {"tests", "testdata"};
        for (int i = 0; i < sections.length; i++) {
            // See if the directory exists.
            File rootFolder = new File(scriptsDir + File.separatorChar + sections[i] + File.separatorChar + testDir);
            if (rootFolder.isDirectory()) {
                // Match.
                // Now see if the directory contains a testplan.xml file.
                File testPlanFile = new File(rootFolder.getAbsolutePath() + File.separatorChar + "testplan.xml");
                if (testPlanFile.exists()) {
                    TestSpec testSpec = new TestSpec();
                    testSpec.setTestSpecDir(rootFolder.getAbsolutePath());
                    String logDirSuffix = sections[i] + File.separatorChar + testDir;
                    testSpec.setLogDirSuffix(logDirSuffix);
                    testSpecs.add(testSpec);
                    //System.out.println("Found testplan.xml in " + rootFolder.getAbsolutePath());
                } else {
                    // See if the directory contains an index.idx file.
                    File indexFile = new File(rootFolder.getAbsolutePath() + File.separatorChar + "index.idx");
                    if (indexFile.exists()) {
                        //System.out.println("Found index.idx in " + rootFolder.getAbsolutePath());
                        BufferedReader input = null;
                        try {
                            input = new BufferedReader(new FileReader(indexFile));
                            String folderName = null;
                            while ((folderName = input.readLine()) != null) {
                                File subFolder = new File(rootFolder.getAbsolutePath() + File.separatorChar + folderName);
                                testPlanFile = new File(subFolder.getAbsolutePath() + File.separatorChar + "testplan.xml");
                                if (testPlanFile.exists()) {
                                    TestSpec testSpec = new TestSpec();
                                    testSpec.setTestSpecDir(subFolder.getAbsolutePath());
                                    String logDirSuffix = sections[i] + File.separatorChar + testDir + File.separatorChar + folderName;
                                    testSpec.setLogDirSuffix(logDirSuffix);
                                    testSpecs.add(testSpec);
                                    //System.out.println("Found testplan.xml in " + subFolder.getAbsolutePath());
                                }
                            }
                        } catch (IOException ex) {
                            // TBD
                        } finally {
                            try {
                                input.close();
                            } catch (IOException ex) {
                                // TBD
                            }
                        }
                    }
                }
            }
        }
        return testSpecs;
    }

    /**
     *
     * @param testCollectionName
     * @return
     */
    private List<TestSpec> findTestSpecsForTestCollection(String testCollectionName) {
        String scriptsDir = TestConfig.testmgmt_dir + File.separatorChar + "scripts";
        // Loop through each test directory looking for directory.
        List<TestSpec> testSpecs = new ArrayList<TestSpec>();
        String collectionsDir = scriptsDir + File.separatorChar + "collections";
        File collectionsFile = new File(collectionsDir + File.separatorChar + testCollectionName + ".tc");
        if (collectionsFile.exists()) {
            // Loop through the contents of the collections file.
            BufferedReader input = null;
            try {
                input = new BufferedReader(new FileReader(collectionsFile));
                String folderName;
                while ((folderName = input.readLine()) != null) {
                    List<TestSpec> subTestSpecs = this.findTestSpecs(folderName);
                    testSpecs.addAll(subTestSpecs);
                }
            } catch (IOException ex) {
                // TBD
            } finally {
                try {
                    input.close();
                } catch (IOException ex) {
                    // TBD
                }
            }

        }
        return testSpecs;
    }

    /**
     * 
     * @param testSpec
     * @return
     */
    private String makeLogDir(TestSpec testSpec) {
        //System.out.println("Making log dir from: " + testSpec.getLogDirSuffix());
        String logDirName = this.logDir + File.separatorChar + testSpec.getLogDirSuffix() + File.separatorChar;
        //System.out.println(" ... " + logDirName);
        new File(logDirName).mkdirs();
        return logDirName;
    }

    /**
     *
     */
    private void usage() {
        System.out.println(
                "\n"
                + "Usage: xtest [options] \n"
                + "     where options are:\n"
                + "   -h : Display this help message\n"
                + "   -s <sitename> : Selects site name to use from xtestconfig.xml\n"
                + "   -t <test name> : Selects test plan to run\n"
                + "   -tc <test collection name> : Selects test collection to run\n"
                + "   -secure : Run in secure mode\n"
                + "   -loop : Loops selected test forever\n");
    }

    /**
     *
     * @param testPathName
     * @return
     */
    private boolean runTest(String testPathName) {
        if (!testPathName.endsWith("/")) {
            testPathName = testPathName + "/";
        }
        TestConfig.base_path = testPathName;
        try {
            PlanContext plan = new PlanContext(BasicTransaction.xds_b);
            return plan.run(testPathName + "testplan.xml", str_sub);
        } catch (XdsException e) {
            System.out.println("XdsException thrown: " + exception_details(e));
        } catch (NullPointerException e) {
            System.out.println(ExceptionUtil.exception_details(e));
        }
        return false;
    }

    /**
     *
     * @param e
     * @return
     */
    private String exception_details(Exception e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        e.printStackTrace(ps);

        return "Exception thrown: " + e.getClass().getName() + "\n" + e.getMessage() + "\n" + new String(baos.toByteArray());
    }

    /**
     *
     */
    public class TestSpec {

        private String testSpecDir;
        private String logDirSuffix;

        /**
         *
         */
        public TestSpec() {
        }

        /**
         *
         * @return
         */
        public String getTestSpecDir() {
            return testSpecDir;
        }

        /**
         * 
         * @param testSpecDir
         */
        public void setTestSpecDir(String testSpecDir) {
            this.testSpecDir = testSpecDir;
        }

        /**
         *
         * @return
         */
        public String getLogDirSuffix() {
            return logDirSuffix;
        }

        /**
         * 
         * @param logDirSuffix
         */
        public void setLogDirSuffix(String logDirSuffix) {
            this.logDirSuffix = logDirSuffix;
        }
    }
}
