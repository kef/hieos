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
package com.vangent.hieos.services.atna.arr.support;

import com.vangent.hieos.services.atna.arr.storage.SQLPersistenceManagerImpl;
import com.vangent.hieos.xutil.atna.AuditMessage;
import com.vangent.hieos.xutil.atna.AuditMessage.ActiveParticipant;
import com.vangent.hieos.xutil.atna.AuditSourceIdentificationType;
import com.vangent.hieos.xutil.atna.CodedValueType;
import com.vangent.hieos.xutil.atna.EventIdentificationType;
import com.vangent.hieos.xutil.atna.ParticipantObjectIdentificationType;
import com.vangent.hieos.xutil.atna.TypeValuePairType;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.apache.log4j.Logger;

/**
 *
 * @author Adeola Odunlami
 */
public class AuditMessageHandler {

    private static final Logger log = Logger.getLogger(AuditMessageHandler.class);
    private ATNAMessage atnaMessage;
    private ATNALog atnaLog;
    private String uniqueID;

    /**
     *
     */
    public AuditMessageHandler() {
        // Use the same uniqueid for the ATNALog and ATNAMessage records
        // This will make it easy to match the ATNA DB records to the ATNA XML received
        uniqueID = getUniqueId();
    }

    /**
     *
     * @param atnaLog
     */
    public AuditMessageHandler(ATNALog atnaLog) {
        this.atnaLog = atnaLog;
        this.atnaLog.setUniqueID(getUniqueId());
    }

    /**
     *
     * @param ipAddress
     * @param port
     * @param protocol
     * @param xml
     * @param errorMessage
     * @throws AuditException
     */
    public void createATNALog(String ipAddress, int port, String protocol, String xml, String errorMessage) throws AuditException {
        atnaLog = new ATNALog();
        atnaLog.setUniqueID(uniqueID);
        atnaLog.setReceivedDateTime(new Date());
        atnaLog.setClientIPAddress(ipAddress);
        atnaLog.setClientPort(String.valueOf(port));
        atnaLog.setProtocol(protocol);
        atnaLog.setXml(xml);
        atnaLog.setErrorMessage(errorMessage);
    }

    /**
     * Create the ATNA Message Object from the XML message
     *
     * @param auditXML
     * @throws AuditException
     */
    public void createATNAMessage(String auditXML) throws AuditException {
        // Unmarshal XML into the JAXB Objects
        AuditMessage jbAuditMessage = parseAuditMessageXML(auditXML);

        // Copy the audit data from the JAXB objects to the POJO classes
        atnaMessage = initializeATNAMessage(jbAuditMessage);
    }

    /**
     * Save the ATNA Message in the database
     *
     * @throws AuditException
     */
    public void persistMessage() throws AuditException {
        // Save the ATNA XML received and parsed audit message in the database
        SQLPersistenceManagerImpl sql = new SQLPersistenceManagerImpl();
        sql.createAuditMessage(atnaMessage);

        // Update the status on the Audit record to indicate the complete message was stored
        atnaMessage.setStatus("C");
        sql.updateAuditMessage(atnaMessage);
    }

    /**
     * Save the ATNA Log in the database - This is always created
     *
     * @throws AuditException
     */
    public void persistLog() throws AuditException {
        // Save the ATNA XML received from the client
        SQLPersistenceManagerImpl sql = new SQLPersistenceManagerImpl();
        sql.createAuditLog(atnaLog);
    }

    /**
     * Retrieve all IP Addresses with audit messages in the atna repository
     *
     * @return
     * @throws AuditException
     */
    public List<String> retrieveIPAddresses() throws AuditException {
        //log.info("AuditMessageHandler - retrieveIPAddresses");
        List<String> ipAddresses = new ArrayList<String>();
        SQLPersistenceManagerImpl sql = new SQLPersistenceManagerImpl();
        ipAddresses = sql.retrieveIPAddresses();
        return ipAddresses;
    }

    /**
     * Retrieves a List of ATNA Records for an IP Address
     *
     * @param searchCriteria
     * @return
     * @throws AuditException
     */
    public List<ATNARecord> retrieveATNARecords(ATNARecord searchCriteria) throws AuditException {
        //log.info("AuditMessageHandler - retrieveATNARecords");
        List<ATNARecord> records = new ArrayList<ATNARecord>();
        SQLPersistenceManagerImpl sql = new SQLPersistenceManagerImpl();

        // Search for ATNA Logs
        ATNALog atnaLogSearchCriteria = new ATNALog();
        atnaLogSearchCriteria.setClientIPAddress(searchCriteria.getClientIPAddress());
        List<ATNALog> logs = sql.retrieveATNALogs(atnaLogSearchCriteria);

        // Populate the ATNA Record and Get Transaction Name for each ATNA Log
        for (ATNALog log : logs) {
            ATNARecord atnaRecord = new ATNARecord();
            atnaRecord.setClientIPAddress(log.getClientIPAddress());
            atnaRecord.setReceivedDateTime(log.getReceivedDateTime());
            atnaRecord.setUniqueID(log.getUniqueID());
            atnaRecord.setProtocol(log.getProtocol());

            // TODO - delete XML & ErrorMessage from the auditLog List
            // TODO - only retrieve from the detail record
            atnaRecord.setXml(log.getXml());
            atnaRecord.setErrorMessage(log.getErrorMessage());

            // Get data from AuditMessage
            ATNAMessage message = sql.retrieveATNAMessage(log.getUniqueID());
            ATNACodedValue codedValue = sql.retrieveATNACodedValue(log.getUniqueID(), "T");
            atnaRecord.setTransactionShortName(codedValue.getCode());
            atnaRecord.setTransactionLongName(codedValue.getDisplayName());
            atnaRecord.setStatus(message.getStatus());

            records.add(atnaRecord);
        }
        return records;
    }

    /**
     * Retrieve a single ATNA Record
     *
     * @param uniqueID
     * @return
     * @throws AuditException
     */
    public ATNARecord retrieveATNARecord(String uniqueID) throws AuditException {
        //log.info("AuditMessageHandler - retrieveATNARecord");
        ATNARecord atnaRecord = new ATNARecord();

        SQLPersistenceManagerImpl sql = new SQLPersistenceManagerImpl();
        ATNALog log = sql.retrieveATNALog(uniqueID);
        ATNAMessage message = sql.retrieveATNAMessage(uniqueID);
        ATNACodedValue codedValue = sql.retrieveATNACodedValue(uniqueID, "T");

        atnaRecord.setClientIPAddress(log.getClientIPAddress());
        atnaRecord.setClientPort(log.getClientPort());
        atnaRecord.setReceivedDateTime(log.getReceivedDateTime());
        atnaRecord.setUniqueID(log.getUniqueID());
        atnaRecord.setProtocol(log.getProtocol());
        atnaRecord.setXml(log.getXml());
        atnaRecord.setErrorMessage(log.getErrorMessage());

        atnaRecord.setTransactionShortName(codedValue.getCode());
        atnaRecord.setTransactionLongName(codedValue.getDisplayName());
        atnaRecord.setStatus(message.getStatus());

        return atnaRecord;
    }

    /**
     *
     * @param searchCriteria
     * @return
     */
    public ATNALog getATNALog(ATNALog searchCriteria) {
        return atnaLog;
    }

    /**
     *
     * @param searchCriteria
     * @return
     */
    public ATNAMessage getATNAMessage(ATNAMessage searchCriteria) {
        return atnaMessage;
    }

    /**
     *Create Java Object from XML
     *
     * @param auditXML
     * @return
     * @throws AuditException
     */
    private AuditMessage parseAuditMessageXML(String auditXML) throws AuditException {

        if (log.isTraceEnabled()) {
            log.trace("AUDIT MESSAGE: " + auditXML);
        }

        if (!auditXML.startsWith("<AuditMessage")) {
            log.error("Message does not start with <AUDIT MESSAGE>: " + auditXML);
            throw new AuditException("Message does not start with <AUDIT MESSAGE>");
        }

        // marshal the XML into Java Objects
        String packageName = "com.vangent.hieos.xutil.atna";
        try {
            // Load the Schema to validate XML received
            // TODO - Use Schema validator in xutil library instead
            
            /*SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Source source = new StreamSource(AuditMessageHandler.class.getClassLoader().getResourceAsStream("com/vangent/hieos/xutil/AuditMessage.xsd"));
            Source[] sources = new Source[]{source,};
            Schema schema = sf.newSchema(sources); */

            // Create JAXB UnMarshaller
            JAXBContext jc = JAXBContext.newInstance(packageName);
            Unmarshaller um = jc.createUnmarshaller();
            //um.setSchema(schema);

            // Unmarshall the XML Message to the AuditMessage Java Object
            log.trace("Unmarshall XML Message");
            StringReader auditString = new StringReader(auditXML);
            AuditMessage auditMessage = (AuditMessage) um.unmarshal(auditString);

            //Return the AuditMessage class
            return auditMessage;

        } catch (MarshalException mex) {
            log.error(mex);
            throw new AuditException(mex);
        } catch (JAXBException jbex) {
            log.error(jbex);
            throw new AuditException(jbex);
        } catch (Exception ex) {
            log.error(ex);
            throw new AuditException(ex);
        }
    }

    /**
     *  This method parses the JAXB objects and writes the data into Java Objects used by the
     *  DAO and other classes.
     *
     */
    private ATNAMessage initializeATNAMessage(AuditMessage jbAuditMessage) {

        log.trace("Create ATNAMessage Object from JAXB Object ");
        ATNAMessage message = new ATNAMessage();
        // Use the same uniqueid fro the ATNALog and ATNAMessage records
        // This will make it easy to match the ATNA DB records to the ATNA XML received
        message.setUniqueID(uniqueID);

        // Parse the JAXB Audit Event Identification data into the Java Object
        if (jbAuditMessage.getEventIdentification() != null) {
            EventIdentificationType jbEvent = jbAuditMessage.getEventIdentification();
            message.setEventActionCode(jbEvent.getEventActionCode());
            message.setEventDateTime(getDateFromXMLDate(jbEvent.getEventDateTime()));
            message.setEventID(createCodedValueObj(message.getUniqueID(), "I", jbEvent.getEventID()));
            message.setEventOutcomeIndicator(getIntegerFromBigInteger(jbEvent.getEventOutcomeIndicator()));
            message.setEventTypeCodes(createCodedValueList(message.getUniqueID(), "T", jbEvent.getEventTypeCode()));
        }

        // Parse the JAXB Audit Source Identification Data into the Java Object
        if (jbAuditMessage.getAuditSourceIdentification() != null) {
            List<ATNAAuditSource> auditSources = new ArrayList<ATNAAuditSource>();
            for (AuditSourceIdentificationType jbAuditSource : jbAuditMessage.getAuditSourceIdentification()) {
                ATNAAuditSource auditSource = new ATNAAuditSource();
                auditSource.setUniqueID(getUniqueId());
                auditSource.setId(jbAuditSource.getAuditSourceID());
                auditSource.setEnterpriseSiteID(jbAuditSource.getAuditEnterpriseSiteID());
                auditSource.setTypeCodes(createCodedValueList(auditSource.getUniqueID(), "T", jbAuditSource.getAuditSourceTypeCode()));
                auditSources.add(auditSource);
            }
            message.setAuditSources(auditSources);
        }

        // Parse the JAXB Active Participant data into the Java Object
        if (jbAuditMessage.getActiveParticipant() != null) {
            List<ATNAActiveParticipant> activeParticipants = new ArrayList<ATNAActiveParticipant>();
            for (ActiveParticipant jbActiveParticipant : jbAuditMessage.getActiveParticipant()) {
                ATNAActiveParticipant activeParticipant = new ATNAActiveParticipant();
                activeParticipant.setUniqueID(getUniqueId());
                activeParticipant.setUserID(jbActiveParticipant.getUserID());
                activeParticipant.setAlternativeUserID(jbActiveParticipant.getAlternativeUserID());
                activeParticipant.setUserName(jbActiveParticipant.getUserName());
                activeParticipant.setNetworkAccessPointID(jbActiveParticipant.getNetworkAccessPointID());
                activeParticipant.setNetworkAccessPointTypeCode(getIntegerFromShort(jbActiveParticipant.getNetworkAccessPointTypeCode()));
                activeParticipant.setRoleIDCodes(createCodedValueList(activeParticipant.getUniqueID(), "R", jbActiveParticipant.getRoleIDCode()));
                activeParticipant.setUserIsRequestor(jbActiveParticipant.isUserIsRequestor());
                activeParticipants.add(activeParticipant);
            }
            message.setActiveParticipants(activeParticipants);
        }

        // Parse the JAXB Participant Object data into the Java Object
        if (jbAuditMessage.getParticipantObjectIdentification() != null) {
            List<ATNAParticipantObject> participantObjects = new ArrayList<ATNAParticipantObject>();
            for (ParticipantObjectIdentificationType jbParticipantObject : jbAuditMessage.getParticipantObjectIdentification()) {
                ATNAParticipantObject participantObject = new ATNAParticipantObject();
                participantObject.setUniqueID(getUniqueId());
                participantObject.setId(jbParticipantObject.getParticipantObjectID());
                participantObject.setIdTypeCode(createCodedValueObj(participantObject.getUniqueID(), "T", jbParticipantObject.getParticipantObjectIDTypeCode()));
                participantObject.setName(jbParticipantObject.getParticipantObjectName());
                participantObject.setQuery(jbParticipantObject.getParticipantObjectQuery());
                participantObject.setSensitivity(jbParticipantObject.getParticipantObjectSensitivity());
                participantObject.setTypeCode(getIntegerFromShort(jbParticipantObject.getParticipantObjectTypeCode()));
                participantObject.setTypeCodeRole(getIntegerFromShort(jbParticipantObject.getParticipantObjectTypeCodeRole()));
                participantObject.setDataLifeCycle(getIntegerFromShort(jbParticipantObject.getParticipantObjectDataLifeCycle()));
                participantObject.setDetails(createTypeValueList(participantObject.getUniqueID(), "D", jbParticipantObject.getParticipantObjectDetail()));
                participantObjects.add(participantObject);
            }
            message.setParticipantObjects(participantObjects);
        }

        return message;
    }

    /**
     * 
     * @param xmlDate
     * @return
     */
    private static Date getDateFromXMLDate(XMLGregorianCalendar xmlDate) {
        if (xmlDate == null) {
            return null;
        } else {
            return xmlDate.toGregorianCalendar().getTime();
        }
    }

    /**
     *
     * @param value
     * @return
     */
    private static Integer getIntegerFromBigInteger(BigInteger value) {
        if (value == null) {
            return 0;
        } else {
            return value.intValue();
        }
    }

    /**
     *
     * @param value
     * @return
     */
    private static Integer getIntegerFromShort(Short value) {
        if (value == null) {
            return 0;
        } else {
            return value.intValue();
        }
    }

    /**
     *
     * @param parentId
     * @param attributeName
     * @param jb
     * @return
     */
    private static ATNACodedValue createCodedValueObj(String parentId, String attributeName, CodedValueType jb) {
        if (jb == null) {
            return null;
        }

        ATNACodedValue obj = new ATNACodedValue();
        obj.setParent(parentId);
        obj.setAttributeName(attributeName);
        obj.setSeqNo(1);
        obj.setCode(jb.getCode());
        obj.setCodeSystem(jb.getCodeSystem());
        obj.setCodeSystemName(jb.getCodeSystemName());
        obj.setDisplayName(jb.getDisplayName());
        obj.setOriginalText(jb.getOriginalText());
        return obj;
    }

    /**
     *
     * @param parentId
     * @param attributeName
     * @param jbList
     * @return
     */
    private static List<ATNACodedValue> createCodedValueList(String parentId, String attributeName, List<CodedValueType> jbList) {
        if (jbList == null) {
            return null;
        } else {
            List<ATNACodedValue> objList = new ArrayList<ATNACodedValue>();
            int seqNo = 0;
            for (CodedValueType jbValue : jbList) {
                ATNACodedValue obj = createCodedValueObj(parentId, attributeName, jbValue);
                obj.setSeqNo(seqNo + 1);
                objList.add(obj);
            }
            return objList;
        }
    }

    /**
     *
     * @param parentId
     * @param attributeName
     * @param jbList
     * @return
     */
    private static List<ATNATypeValue> createTypeValueList(String parentId, String attributeName, List<TypeValuePairType> jbList) {
        if (jbList == null) {
            return null;
        } else {
            List<ATNATypeValue> objList = new ArrayList<ATNATypeValue>();
            int seqNo = 0;
            for (TypeValuePairType jbValue : jbList) {
                ATNATypeValue obj = createTypeValueObj(parentId, attributeName, jbValue);
                obj.setSeqNo(seqNo + 1);
                objList.add(obj);
            }
            return objList;
        }
    }

    /**
     *
     * @param parentId
     * @param attributeName
     * @param jbValue
     * @return
     */
    private static ATNATypeValue createTypeValueObj(String parentId, String attributeName, TypeValuePairType jbValue) {
        if (jbValue == null) {
            return null;
        } else {
            ATNATypeValue obj = new ATNATypeValue();
            obj.setParent(parentId);
            obj.setAttributeName(attributeName);
            obj.setSeqNo(1);
            obj.setType(jbValue.getType());
            obj.setValue(jbValue.getValue());
            log.debug("ATNATypeValue Type: " + obj.getType());
            return obj;
        }
    }

    /**
     *
     * @return String - a system generated unique id
     */
    public static String getUniqueId() {
        return UUID.randomUUID().toString();
    }
}
