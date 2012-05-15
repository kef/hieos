/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2010 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.freebxml.omar.server.persistence.rdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Bernie Thuman
 */
public class RegistryCodedValueMapper {

    static RegistryCodedValueMapper _instance;
    private HashMap status_ValueToCodeMap = new HashMap();
    private HashMap status_CodeToValueMap = new HashMap();
    private HashMap objectType_ValueToCodeMap = new HashMap();
    private HashMap objectType_CodeToValueMap = new HashMap();
    private HashMap idScheme_ValueToCodeMap = new HashMap();
    private HashMap idScheme_CodeToValueMap = new HashMap();
    private HashMap assocType_ValueToCodeMap = new HashMap();
    private HashMap assocType_CodeToValueMap = new HashMap();

    /**
     *
     * @return
     */
    private static synchronized RegistryCodedValueMapper getInstance() {
        if (_instance == null) {
            _instance = new RegistryCodedValueMapper();
            _instance.loadMaps();
        }
        return _instance;
    }

    /**
     *
     */
    private RegistryCodedValueMapper() {
    }

    /**
     * 
     */
    private void loadMaps() {
        // Build "status" maps:
        status_ValueToCodeMap.put("urn:oasis:names:tc:ebxml-regrep:StatusType:Approved", "A");
        status_ValueToCodeMap.put("urn:oasis:names:tc:ebxml-regrep:StatusType:Deprecated", "D");
        status_ValueToCodeMap.put("urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted", "S");
        this.buildCodeValueMap(status_ValueToCodeMap, status_CodeToValueMap);

        // Build "objecttype" maps:
        objectType_ValueToCodeMap.put("urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:RegistryPackage", "RP");
        objectType_ValueToCodeMap.put("urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association", "AS");
        objectType_ValueToCodeMap.put("urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalIdentifier", "EI");
        objectType_ValueToCodeMap.put("urn:uuid:7edca82f-054d-47f2-a032-9b2a5b5186c1", "DO"); // XDSb.Document (Stable).
        objectType_ValueToCodeMap.put("urn:uuid:34268e47-fdf5-41a6-ba33-82133c465248", "OD"); // XDSb.Document (On-Demand).
        objectType_ValueToCodeMap.put("urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification", "CL");
        this.buildCodeValueMap(objectType_ValueToCodeMap, objectType_CodeToValueMap);

        // Build "identificationscheme" maps:
        // XDSDocumentEntry_uniqueid_uuid
        idScheme_ValueToCodeMap.put("urn:uuid:2e82c1f6-a085-4c72-9da3-8640a32e42ab", "DU");
        // XDSDocumentEntry_patientid_uuid
        idScheme_ValueToCodeMap.put("urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427", "DP");
        // XDSSubmissionSet_sourceid_uuid
        idScheme_ValueToCodeMap.put("urn:uuid:554ac39e-e3fe-47fe-b233-965d2a147832", "SS");
        // XDSSubmissionSet_uniqueid_uuid
        idScheme_ValueToCodeMap.put("urn:uuid:96fdda7c-d067-4183-912e-bf5ee74998a8", "SU");
        // XDSSubmissionSet_patientid_uuid
        idScheme_ValueToCodeMap.put("urn:uuid:6b5aea1a-874d-4603-a4bc-96a0a7b38446", "SP");
        // XDSFolder_patientid_uuid
        idScheme_ValueToCodeMap.put("urn:uuid:f64ffdf0-4b97-4e06-b79f-a52b38ec2f8a", "FP");
        // XDSFolder_uniqueid_uuid
        idScheme_ValueToCodeMap.put("urn:uuid:75df8f67-9973-4fbe-a900-df66cefecc5a", "FU");
        this.buildCodeValueMap(idScheme_ValueToCodeMap, idScheme_CodeToValueMap);

        // Build "associationtype" maps:
        assocType_ValueToCodeMap.put("urn:ihe:iti:2007:AssociationType:APND", "AP");
        assocType_ValueToCodeMap.put("urn:ihe:iti:2007:AssociationType:RPLC", "RP");
        assocType_ValueToCodeMap.put("urn:ihe:iti:2007:AssociationType:XFRM", "XF");
        assocType_ValueToCodeMap.put("urn:ihe:iti:2007:AssociationType:XFRM_RPLC", "XR");
        assocType_ValueToCodeMap.put("urn:ihe:iti:2007:AssociationType:signs", "SI");
        assocType_ValueToCodeMap.put("urn:ihe:iti:2010:AssociationType:IsSnapshotOf", "SN");
        assocType_ValueToCodeMap.put("urn:oasis:names:tc:ebxml-regrep:AssociationType:HasMember", "HM");
        this.buildCodeValueMap(assocType_ValueToCodeMap, assocType_CodeToValueMap);
    }

    /**
     * 
     * @param valueToCodeMap
     * @param codeToValueMap
     */
    private void buildCodeValueMap(HashMap valueToCodeMap, HashMap codeToValueMap) {
        Set keys = valueToCodeMap.keySet();
        Iterator keyIterator = keys.iterator();
        while (keyIterator.hasNext()) {
            String value = (String) keyIterator.next();
            String code = (String) valueToCodeMap.get(value);
            codeToValueMap.put(code, value);
        }
    }

    /**
     *
     * @param code
     * @return
     */
    public static String convertStatus_CodeToValue(String code) {
        RegistryCodedValueMapper mapper = RegistryCodedValueMapper.getInstance();
        return (String) mapper.status_CodeToValueMap.get(code);
    }

    /**
     *
     * @param value
     * @return
     */
    public static String convertStatus_ValueToCode(String value) {
        RegistryCodedValueMapper mapper = RegistryCodedValueMapper.getInstance();
        return (String) mapper.status_ValueToCodeMap.get(value);
    }

    /**
     *
     * @param values
     * @return
     */
    public static List<String> convertStatus_ValueToCode(List<String> values) {
        RegistryCodedValueMapper mapper = RegistryCodedValueMapper.getInstance();
        List<String> result = new ArrayList<String>();
        Iterator iter = values.iterator();
        while (iter.hasNext()) {
            String value = (String) iter.next();
            String code = (String) mapper.status_ValueToCodeMap.get(value);
            // Guard against adding null values to the list.
            if (code != null) {
                result.add(code);
            }
        }
        return result;
    }

    /**
     *
     * @param code
     * @return
     */
    public static String convertObjectType_CodeToValue(String code) {
        RegistryCodedValueMapper mapper = RegistryCodedValueMapper.getInstance();
        return (String) mapper.objectType_CodeToValueMap.get(code);
    }

    /**
     *
     * @param value
     * @return
     */
    public static String convertObjectType_ValueToCode(String value) {
        RegistryCodedValueMapper mapper = RegistryCodedValueMapper.getInstance();
        return (String) mapper.objectType_ValueToCodeMap.get(value);
    }

    /**
     *
     * @param code
     * @return
     */
    public static String convertIdScheme_CodeToValue(String code) {
        RegistryCodedValueMapper mapper = RegistryCodedValueMapper.getInstance();
        return (String) mapper.idScheme_CodeToValueMap.get(code);
    }

    /**
     *
     * @param value
     * @return
     */
    public static String convertIdScheme_ValueToCode(String value) {
        RegistryCodedValueMapper mapper = RegistryCodedValueMapper.getInstance();
        return (String) mapper.idScheme_ValueToCodeMap.get(value);
    }

    /**
     *
     * @param code
     * @return
     */
    public static String convertAssocType_CodeToValue(String code) {
        RegistryCodedValueMapper mapper = RegistryCodedValueMapper.getInstance();
        return (String) mapper.assocType_CodeToValueMap.get(code);
    }

    /**
     *
     * @param value
     * @return
     */
    public static String convertAssocType_ValueToCode(String value) {
        RegistryCodedValueMapper mapper = RegistryCodedValueMapper.getInstance();
        return (String) mapper.assocType_ValueToCodeMap.get(value);
    }

    /**
     *
     * @param values
     * @return
     */
    public static List<String> convertAssocType_ValueToCode(List<String> values) {
        RegistryCodedValueMapper mapper = RegistryCodedValueMapper.getInstance();
        List<String> result = new ArrayList<String>();
        Iterator iter = values.iterator();
        while (iter.hasNext()) {
            String value = (String) iter.next();
            result.add((String) mapper.assocType_ValueToCodeMap.get(value));
        }
        return result;
    }
}
