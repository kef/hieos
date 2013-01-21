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
package com.vangent.hieos.empi.config;

import com.vangent.hieos.empi.codes.CodeSystem;
import com.vangent.hieos.empi.codes.CodesConfig;
import com.vangent.hieos.empi.codes.CodesConfig.CodedType;
import com.vangent.hieos.empi.match.MatchAlgorithm;
import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.empi.match.CandidateFinder;
import com.vangent.hieos.empi.match.MatchAlgorithm.MatchType;
import com.vangent.hieos.subjectmodel.CodedValue;
import com.vangent.hieos.subjectmodel.DeviceInfo;
import com.vangent.hieos.xutil.xconfig.XConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class EMPIConfig {

    private final static Logger logger = Logger.getLogger(EMPIConfig.class);
    private static String EMPI_CONFIG_FILE_NAME = "empiConfig.xml";
    private static String EMPI_CODES_CONFIG_FILE_NAME = "codes.xml";
    private static String JNDI_RESOURCE_NAME = "jndi-resource-name";
    private static String EMPI_DEVICE_IDS = "empi-device-ids";
    private static String UPDATE_NOTIFICATION_ENABLED = "update-notification-enabled";
    private static String SUBJECT_SEQUENCE_GENERATOR_SQL = "subject-sequence-generator-sql";
    private static String VALIDATE_CODES_ENABLED = "validate-codes-enabled";
    private static String VALIDATE_IDENTITY_SOURCES_ENABLED = "validate-identity-sources-enabled";
    private static String MATCH_ALGORITHM = "match-algorithm";
    private static String CANDIDATE_FINDER = "candidate-finder";
    private static String DEFAULT_JNDI_RESOURCE_NAME = "jdbc/hieos-empi";
    private static String TRANSFORM_FUNCTIONS = "transform-functions.transform-function";
    private static String DISTANCE_FUNCTIONS = "distance-functions.distance-function";
    private static String FIELDS = "fields.field";
    private static String MATCH_CONFIG_FEED = "match-config-feed(0)";
    private static String MATCH_CONFIG_FIND = "match-config-find(0)";
    private static String EUID_CONFIG = "euid-config(0)";
    private static String IDENTITY_SOURCE_CONFIGS = "identity-sources.identity-source";
    private static EMPIConfig _instance = null;
    //private BlockingConfig blockingConfig;
    private MatchConfig matchConfigFeed;
    private MatchConfig matchConfigFind;
    private String jndiResourceName;
    private String[] empiDeviceIds;
    private MatchAlgorithm matchAlgorithm;
    private CandidateFinder candidateFinder;
    private EUIDConfig euidConfig;
    private boolean updateNotificationEnabled;
    private String subjectSequenceGeneratorSQL;
    private boolean validateCodesEnabled;
    private boolean validateIdentitySourcesEnabled;
    private Map<String, TransformFunctionConfig> transformFunctionConfigs = new HashMap<String, TransformFunctionConfig>();
    private Map<String, DistanceFunctionConfig> distanceFunctionConfigs = new HashMap<String, DistanceFunctionConfig>();
    private Map<String, FieldConfig> fieldConfigs = new HashMap<String, FieldConfig>();
    // Key = device id
    private Map<String, IdentitySourceConfig> identitySourceConfigs = new HashMap<String, IdentitySourceConfig>();
    private CodesConfig codesConfig;

    /**
     *
     */
    private EMPIConfig() {
        // Do not allow.
    }

    /**
     * 
     * @return
     * @throws EMPIException
     */
    static public synchronized EMPIConfig getInstance() throws EMPIException {
        if (_instance == null) {
            _instance = new EMPIConfig();
            _instance.loadConfiguration();
        }
        return _instance;
    }

    /**
     * 
     * @param deviceId
     * @return
     */
    public boolean isEMPIDeviceId(String deviceId) {
        for (int i = 0; i < empiDeviceIds.length; i++) {
            if (empiDeviceIds[i].equalsIgnoreCase(deviceId)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param matchType
     * @return
     */
    public MatchConfig getMatchConfig(MatchType matchType) {
        if (matchType == MatchType.SUBJECT_FEED) {
            return matchConfigFeed;
        } else {
            return matchConfigFind;
        }
    }

    /**
     *
     * @return
     */
    public String getJndiResourceName() {
        return jndiResourceName;
    }

    /**
     * 
     * @return
     */
    public String getSubjectSequenceGeneratorSQL() {
        return subjectSequenceGeneratorSQL;
    }

    /**
     *
     * @return
     */
    public MatchAlgorithm getMatchAlgorithm() {
        return matchAlgorithm;
    }

    /**
     *
     * @return
     */
    public CandidateFinder getCandidateFinder() {
        return candidateFinder;
    }

    /**
     *
     * @return
     */
    public EUIDConfig getEuidConfig() {
        return euidConfig;
    }

    /**
     *
     * @return
     */
    public Map<String, DistanceFunctionConfig> getDistanceFunctionConfigs() {
        return distanceFunctionConfigs;
    }

    /**
     *
     * @param name
     * @return
     */
    public DistanceFunctionConfig getDistanceFunctionConfig(String name) {
        return distanceFunctionConfigs.get(name);
    }

    /**
     *
     * @return
     */
    public Map<String, TransformFunctionConfig> getTransformFunctionConfigs() {
        return transformFunctionConfigs;
    }

    /**
     *
     * @param name
     * @return
     */
    public TransformFunctionConfig getTransformFunctionConfig(String name) {
        return transformFunctionConfigs.get(name);
    }

    /**
     * 
     * @return
     */
    public Map<String, FieldConfig> getFieldConfigs() {
        return fieldConfigs;
    }

    /**
     *
     * @param name
     * @return
     */
    public FieldConfig getFieldConfig(String name) {
        return fieldConfigs.get(name);
    }

    /**
     * 
     * @return
     */
    public List<FieldConfig> getFieldConfigList() {
        return new ArrayList<FieldConfig>(fieldConfigs.values());
    }

    /**
     *
     * @return
     */
    public boolean isUpdateNotificationEnabled() {
        return updateNotificationEnabled;
    }

    /**
     *
     * @return
     */
    public boolean isValidateCodesEnabled() {
        return validateCodesEnabled;
    }

    /**
     *
     * @return
     */
    public boolean isValidateIdentitySourcesEnabled() {
        return validateIdentitySourcesEnabled;
    }

    /**
     *
     * @param deviceInfo
     * @return
     */
    public IdentitySourceConfig getIdentitySourceConfig(DeviceInfo deviceInfo) {
        String deviceId = deviceInfo.getId();
        return this.identitySourceConfigs.get(deviceId);
    }

    /**
     *
     * @param code
     * @param codeSystem
     * @return
     */
    public boolean isValidCode(String code, String codeSystem) {
        return codesConfig.isValidCode(code, codeSystem);
    }

    /**
     *
     * @param code
     * @param codedType
     * @return
     */
    public boolean isValidCode(String code, CodedType codedType) {
        return codesConfig.isValidCode(code, codedType);
    }

    /**
     * 
     * @param codedValue
     * @param codedType
     * @throws EMPIException
     */
    public void validateCode(CodedValue codedValue, CodedType codedType) throws EMPIException {
        if (codedValue != null) {
            boolean validCodeByType = this.isValidCode(codedValue.getCode(), codedType);
            if (!validCodeByType) {
                String exText = "Coded value is not valid according to HIEOS configuration ("
                        + "code=" + codedValue.getCode()
                        + ",codeSystem=" + codedValue.getCodeSystem()
                        + ",codedType=" + codedType
                        + ")";
                if (this.validateCodesEnabled) {
                    logger.error(exText);
                    throw new EMPIException(exText);
                } else {
                    logger.info(exText);
                }
            }
        }
    }

    /**
     *
     * @param code
     * @param codeSystem
     * @return
     */
    public CodedValue getCodedValue(String code, String codeSystem) {
        return codesConfig.getCodedValue(code, codeSystem);
    }

    /**
     *
     * @param code
     * @param codedType
     * @return
     */
    public CodedValue getCodedValue(String code, CodedType codedType) {
        return codesConfig.getCodedValue(code, codedType);
    }

    /**
     *
     * @param codeSystemName 
     * @return
     */
    public CodeSystem getCodeSystemByName(String codeSystemName) {
        return codesConfig.getCodeSystemByName(codeSystemName);
    }

    /**
     *
     * @param codedType
     * @return
     */
    public CodeSystem getCodeSystemByType(CodedType codedType) {
        return codesConfig.getCodeSystemByType(codedType);
    }

    /**
     *
     * @return
     */
    public Map<String, IdentitySourceConfig> getIdentitySourceConfigs() {
        return identitySourceConfigs;
    }

    /**
     * 
     * @throws EMPIException
     */
    private void loadConfiguration() throws EMPIException {
        String empiConfigDir = XConfig.getConfigLocation(XConfig.ConfigItem.EMPI_DIR);
        String configLocation = empiConfigDir + "/" + EMPIConfig.EMPI_CONFIG_FILE_NAME;
        String codesConfigLocation = empiConfigDir + "/" + EMPIConfig.EMPI_CODES_CONFIG_FILE_NAME;
        try {
            XMLConfiguration xmlConfig = new XMLConfiguration(configLocation);
            jndiResourceName = xmlConfig.getString(JNDI_RESOURCE_NAME, DEFAULT_JNDI_RESOURCE_NAME);
            subjectSequenceGeneratorSQL = xmlConfig.getString(SUBJECT_SEQUENCE_GENERATOR_SQL, "UNKNOWN SUBJECT SEQUENCE GENERATOR SQL");
            updateNotificationEnabled = xmlConfig.getBoolean(UPDATE_NOTIFICATION_ENABLED, false);
            validateCodesEnabled = xmlConfig.getBoolean(VALIDATE_CODES_ENABLED, true);
            validateIdentitySourcesEnabled = xmlConfig.getBoolean(VALIDATE_IDENTITY_SOURCES_ENABLED, true);
            empiDeviceIds = xmlConfig.getStringArray(EMPI_DEVICE_IDS);

            // Load the candidate finder.
            this.loadCandidateFinder(xmlConfig);

            // Load the match algorithm.
            this.loadMatchAlgorithm(xmlConfig);

            // Load function configurations.
            this.loadFunctionConfigs(xmlConfig);

            // Load field configurations.
            this.loadFieldConfigs(xmlConfig);

            // Load matching configurations.

            // Configuration to support feeds.
            matchConfigFeed = new MatchConfig();
            matchConfigFeed.load(xmlConfig.configurationAt(MATCH_CONFIG_FEED), this);

            // Configuration to support finds.
            matchConfigFind = new MatchConfig();
            matchConfigFind.load(xmlConfig.configurationAt(MATCH_CONFIG_FIND), this);

            // Load EUID configuration.
            euidConfig = new EUIDConfig();
            euidConfig.load(xmlConfig.configurationAt(EUID_CONFIG), this);

            // Load identity sources.
            this.loadIdentitySources(xmlConfig);

            // Load codes configuration.
            codesConfig = new CodesConfig();
            codesConfig.loadConfiguration(codesConfigLocation);

        } catch (ConfigurationException ex) {
            throw new EMPIException(
                    "EMPIConfig: Could not load configuration from " + configLocation + " " + ex.getMessage());
        }
    }

    /**
     *
     * @param hc
     */
    private void loadFunctionConfigs(HierarchicalConfiguration hc) {

        // Get transform function configurations.
        List transformFunctions = hc.configurationsAt(TRANSFORM_FUNCTIONS);
        for (Iterator it = transformFunctions.iterator(); it.hasNext();) {
            HierarchicalConfiguration hcTransformFunction = (HierarchicalConfiguration) it.next();
            TransformFunctionConfig transformFunctionConfig = new TransformFunctionConfig();
            try {
                transformFunctionConfig.load(hcTransformFunction, this);
                // Keep track of transform function configurations (in hash map).
                transformFunctionConfigs.put(transformFunctionConfig.getName(), transformFunctionConfig);
            } catch (EMPIException ex) {
                // FIXME? Keep processing
            }
        }

        // Get distance function configurations.
        List distanceFunctions = hc.configurationsAt(DISTANCE_FUNCTIONS);
        for (Iterator it = distanceFunctions.iterator(); it.hasNext();) {
            HierarchicalConfiguration hcDistanceFunction = (HierarchicalConfiguration) it.next();
            DistanceFunctionConfig distanceFunctionConfig = new DistanceFunctionConfig();
            try {
                distanceFunctionConfig.load(hcDistanceFunction, this);
                // Keep track of distance function configurations (in hash map).
                distanceFunctionConfigs.put(distanceFunctionConfig.getName(), distanceFunctionConfig);
            } catch (EMPIException ex) {
                // FIXME? Keep processing
            }
        }
    }

    /**
     * 
     * @param hc
     * @throws EMPIException
     */
    private void loadFieldConfigs(HierarchicalConfiguration hc) throws EMPIException {
        // Get field configurations.
        List fields = hc.configurationsAt(FIELDS);
        for (Iterator it = fields.iterator(); it.hasNext();) {
            HierarchicalConfiguration hcField = (HierarchicalConfiguration) it.next();
            FieldConfig fieldConfig = new FieldConfig();
            fieldConfig.load(hcField, this);
            // Keep track of field configurations (in hash map).
            fieldConfigs.put(fieldConfig.getName(), fieldConfig);
        }
    }

    /**
     *
     * @param hc
     * @throws EMPIException
     */
    private void loadIdentitySources(HierarchicalConfiguration hc) throws EMPIException {

        // Get identity source(s) configurations.
        List identitySources = hc.configurationsAt(IDENTITY_SOURCE_CONFIGS);
        for (Iterator it = identitySources.iterator(); it.hasNext();) {
            HierarchicalConfiguration hcIdentitySource = (HierarchicalConfiguration) it.next();
            IdentitySourceConfig identitySourceConfig = new IdentitySourceConfig();
            identitySourceConfig.load(hcIdentitySource, this);
            identitySourceConfigs.put(identitySourceConfig.getDeviceId(), identitySourceConfig);
        }
    }

    /**
     *
     * @param hc
     * @throws EMPIException
     */
    private void loadMatchAlgorithm(HierarchicalConfiguration hc) throws EMPIException {
        String matchAlgorithmClassName = hc.getString(MATCH_ALGORITHM);
        // Get an instance of the match algorithm.
        this.matchAlgorithm = (MatchAlgorithm) ConfigHelper.loadClassInstance(matchAlgorithmClassName);
    }

    /**
     *
     * @param hc
     * @throws EMPIException
     */
    private void loadCandidateFinder(HierarchicalConfiguration hc) throws EMPIException {
        String candidateFinderClassName = hc.getString(CANDIDATE_FINDER);
        // Get an instance of the match algorithm.
        this.candidateFinder = (CandidateFinder) ConfigHelper.loadClassInstance(candidateFinderClassName);
    }
}
