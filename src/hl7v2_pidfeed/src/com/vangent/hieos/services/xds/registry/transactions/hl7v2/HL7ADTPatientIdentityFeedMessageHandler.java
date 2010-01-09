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
package com.vangent.hieos.services.xds.registry.transactions.hl7v2;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.app.DefaultApplication;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.model.Structure;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.soap.Soap;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.log4j.Logger;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author Bernie Thuman
 */
public class HL7ADTPatientIdentityFeedMessageHandler extends HL7Application {

    public static final String ADD_ACTION = "ADD";
    public static final String MERGE_ACTION = "MERGE";
    private static final Logger log = Logger.getLogger(HL7ADTPatientIdentityFeedMessageHandler.class);
    private static final String XDS_REGISTRY_ENDPOINT = "xds_registry_endpoint";

    /**
     * 
     * @param props
     */
    public HL7ADTPatientIdentityFeedMessageHandler(HL7ServerProperties props) {
        super(props);
    }

    /**
     *
     * @param inMessage
     * @param socket
     * @return
     * @throws ApplicationException
     * @throws HL7Exception
     */
    @Override
    protected Message handleMessage(Message inMessage, Socket socket) throws ApplicationException, HL7Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     *
     * @param request
     */
    protected OMElement sendRequestToRegistry(OMElement request) throws HL7Exception {
        log.info("Registry Request:\n");
        log.info(request.toString());
        String action = "urn:hieos:xds:PatientFeedRequest";
        String expectedReturnAction = "urn:hieos:xds:PatientFeedResponse";
        String endpoint = this.getProperties().getProperty(XDS_REGISTRY_ENDPOINT);
        Soap soap = new Soap();
        soap.setAsync(false);
        log.info("Making SOAP request to XDS registry using URL: " + endpoint);
        try {
            soap.soapCall(
                    request,
                    endpoint,
                    false /* MTOM */,
                    true /* Addressing */,
                    true /* SOAP12 */,
                    action,
                    expectedReturnAction);
        } catch (XdsException e) {
            log.error("EXCEPTION: " + e.getMessage());
            throw new HL7Exception(e);
        }
        OMElement result = soap.getResult();  // Get the result.
        log.info("Registry Response:\n");
        log.info(result.toString());
        return result;
    }

    /**
     *
     * @param action
     * @param sourceIP
     * @param inMessage
     * @return
     */
    private OMElement createRegistryRequest(String action, String sourceIP, Message inMessage, String messageControlId, String sourceIdentity) throws HL7Exception {
        OMFactory fact = OMAbstractFactory.getOMFactory();
        OMNamespace ns = this.getHIEOS_OMNamespace(fact);
        OMElement patientFeedRequestNode = fact.createOMElement("PatientFeedRequest", ns);

        // PatientFeedRequest/Action
        this.createTextNode(patientFeedRequestNode, "Action", action, fact, ns);

        // PatientFeedRequest/SourceIPAddress
        this.createTextNode(patientFeedRequestNode, "SourceIPAddress", sourceIP, fact, ns);

        // PatientFeedRequest/RawMessage
        // Convert to base 64 before sending:
        String encodedMessage = new PipeParser().encode(inMessage);
        byte[] base64 = Base64.encodeBase64(encodedMessage.getBytes());
        this.createTextNode(patientFeedRequestNode, "RawMessage", new String(base64), fact, ns);

        // PatientFeedRequest/MessageControlId
        this.createTextNode(patientFeedRequestNode, "MessageControlId", messageControlId, fact, ns);

        // PatientFeedRequest/SourceIdentity
        this.createTextNode(patientFeedRequestNode, "SourceIdentity", sourceIdentity, fact, ns);

        // Return the request node.
        return patientFeedRequestNode;
    }

    /**
     *
     * @param rootNode
     * @param name
     * @param text
     * @param fact
     * @param ns
     * @return
     */
    private OMElement createTextNode(OMElement rootNode, String name, String text, OMFactory fact, OMNamespace ns) {
        OMElement node = fact.createOMElement(name, ns);
        node.setText(text);
        rootNode.addChild(node);
        return node;
    }

    /**
     *
     * @param inMessage
     * @param registryResponse
     * @return
     */
    Message generateACK(Message inMessage, OMElement registryResponse) throws HL7Exception, IOException {
        String responseCode = this.getTextFromNode(registryResponse, "ResponseCode");
        String responseText = this.getTextFromNode(registryResponse, "ResponseText");
        String errorText = this.getTextFromNode(registryResponse, "ErrorText");
        Segment inHeader = (Segment) inMessage.get("MSH");
        Message retVal;
        try {
            retVal = DefaultApplication.makeACK(inHeader);
            Terser terser = new Terser(retVal);
            if (errorText == null) {
                terser.set("/.MSA-3-1", responseText);
            } else {
                log.error("ErrorText = " + errorText);
                terser.set("/.MSA-1", "AR");
                terser.set("/.MSA-3-1", errorText);
                // ERR|^^^207&Application Internal Error&HL70357 */
                terser.set("/.ERR-1-4-1", "207");
                terser.set("/.ERR-1-4-2", "Application Internal Error");
                terser.set("/.ERR-1-4-3", "HL70357");
            }
            DefaultXMLParser xmlParser = new DefaultXMLParser();
            String xmlEncodedMessage = xmlParser.encode(retVal);
            log.info("XML Encoded Response Message:\n" + xmlEncodedMessage);
        } catch (IOException e) {
            throw new HL7Exception(e);
        }
        return retVal;
    }

    /**
     * 
     * @param ack
     * @param errorText
     * @throws HL7Exception
     */
    /*
    private void fillAckErrorDetails(Message ack, String errorText) throws HL7Exception  {
            //populate MSA and ERR with  error ...
            Segment msa = (Segment) ack.get("MSA");
            Terser.set(msa, 1, 0, 1, 1, "AR");
            Terser.set(
                msa,
                3,
                0,
                1,
                1,
                errorText);
            //this is max length

            //populate ERR segment if it exists (may not depending on version)
            Structure s = ack.get("ERR");
            if (s != null) {
                Segment err = (Segment) s;
                Terser.set(err, 1, 0, 4, 1, "207");
                Terser.set(err, 1, 0, 4, 2, "Application Internal Error");
                Terser.set(err, 1, 0, 4, 3, "HL70357");
            }
    } */

    /**
     *
     * @param ele
     * @param localName
     * @return
     */
    private String getTextFromNode(OMElement ele, String localName) {
        for (Iterator it = ele.getChildElements(); it.hasNext();) {
            OMElement child = (OMElement) it.next();
            if (child.getLocalName().equals(localName)) {
                return child.getText();
            }
        }
        return null;
    }

    /**
     *
     * @param fact
     * @return
     */
    private OMNamespace getHIEOS_OMNamespace(OMFactory fact) {
        String hieos_uri = "urn:hieos:1.0";
        return fact.createOMNamespace(hieos_uri, "hieos");
    }

    /**
     *
     * @param sourceIP
     * @param inMessage
     * @param patientId
     * @return
     */
    protected OMElement createRegistryAddRequest(String sourceIP, Message inMessage, String messageControlId, String sourceIdentity, String patientId) throws HL7Exception {
        /*
        <PatientFeedRequest>
        <Action>ADD</Action>
        <SourceIPAddress>10.23.1.102</SourceIPAddress>
        <PatientId>&amp;17.22.30.40.50&amp;ISO</PatientId>
        <RawMessage>....</MergePatientId>
        <MessageControlId>...</MessageControlId>
        <SourceIdentity>...</SourceIdentity>
        </PatientFeedRequest>
         */
        OMFactory fact = OMAbstractFactory.getOMFactory();
        OMNamespace ns = this.getHIEOS_OMNamespace(fact);
        OMElement patientFeedRequestNode = this.createRegistryRequest("ADD", sourceIP, inMessage, messageControlId, sourceIdentity);

        // PatientFeedRequest/PatientId
        this.createTextNode(patientFeedRequestNode, "PatientId", patientId, fact, ns);

        // Return the node.
        return patientFeedRequestNode;
    }

    /**
     *
     * @param sourceIP
     * @param inMessage
     * @param patientId
     * @param mergePatientId
     * @return
     */
    protected OMElement createRegistryMergeRequest(String sourceIP, Message inMessage, String messageControlId, String sourceIdentity, String patientId, String mergePatientId) throws HL7Exception {
        /*
        <PatientFeedRequest>
        <Action>MERGE</Action>
        <SourceIPAddress>10.23.1.102</SourceIPAddress>
        <PatientId>&amp;17.22.30.40.50&amp;ISO</PatientId>
        <MergePatientId>&amp;17.22.30.40.50&amp;ISO</MergePatientId>
        <RawMessage>....</MergePatientId>
        <MessageControlId>...</MessageControlId>
        <SourceIdentity>...</SourceIdentity>
        </PatientFeedRequest>
         */
        OMFactory fact = OMAbstractFactory.getOMFactory();
        OMNamespace ns = this.getHIEOS_OMNamespace(fact);
        OMElement patientFeedRequestNode = this.createRegistryRequest("MERGE", sourceIP, inMessage, messageControlId, sourceIdentity);

        // PatientFeedRequest/PatientId
        this.createTextNode(patientFeedRequestNode, "PatientId", patientId, fact, ns);

        // PatientFeedRequest/MergePatientId
        this.createTextNode(patientFeedRequestNode, "MergePatientId", mergePatientId, fact, ns);

        // Return the node.
        return patientFeedRequestNode;
    }

    /**
     *
     * @param pid
     * @param assigningAuthority
     * @return
     */
    protected String formatPatientId(String pid, String assigningAuthority) {
        return pid + "^^^&" + assigningAuthority + "&ISO";
    }

    /**
     *
     * @param sendingApplication
     * @param sendingFacility
     * @return
     */
    protected String formatPatientIdentitySource(String sendingFacility, String sendingApplication) {
        if (sendingApplication == null) {
            sendingApplication = "";
        }
        if (sendingFacility == null) {
            sendingFacility = "";
        }
        return sendingFacility + "|" + sendingApplication;
    }
}
