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
package com.vangent.hieos.services.xds.repository.transactions;

import com.vangent.hieos.xutil.atna.XATNALogger;
import com.vangent.hieos.xutil.metadata.structure.MetadataTypes;
import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.exception.MetadataValidationException;
import com.vangent.hieos.xutil.exception.SchemaValidationException;
import com.vangent.hieos.xutil.exception.XDSMissingDocumentException;
import com.vangent.hieos.xutil.exception.XDSMissingDocumentMetadataException;
import com.vangent.hieos.xutil.exception.XdsConfigurationException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsFormatException;
import com.vangent.hieos.xutil.exception.XdsIOException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.iosupport.Sha1Bean;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.response.RegistryResponse;
import com.vangent.hieos.xutil.registry.RegistryUtility;
import com.vangent.hieos.xutil.services.framework.XBaseTransaction;
import com.vangent.hieos.xutil.soap.Soap;
import com.vangent.hieos.services.xds.repository.support.Repository;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import com.vangent.hieos.services.xds.repository.storage.XDSDocument;
import com.vangent.hieos.services.xds.repository.storage.XDSRepositoryStorage;
import com.vangent.hieos.xutil.exception.SOAPFaultException;

import com.vangent.hieos.xutil.exception.XDSRepositoryMetadataError;
import com.vangent.hieos.xutil.soap.SoapActionFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.axis2.context.MessageContext;
import org.apache.log4j.Logger;

//import sun.misc.BASE64Decoder;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author Bernie Thuman (plugged in new repository storage mechanism, removed XDS.a).
 */
public class ProvideAndRegisterDocumentSet extends XBaseTransaction {

    private final static Logger logger = Logger.getLogger(ProvideAndRegisterDocumentSet.class);

    private Repository repoConfig = null;

    /**
     *
     * @param log_message
     * @param messageContext
     */
    public ProvideAndRegisterDocumentSet(XLogMessage log_message, MessageContext messageContext) {
        this.log_message = log_message;
        //this.messageContext = messageContext;
        try {
            init(new RegistryResponse(), messageContext);
        } catch (XdsInternalException e) {
            logger.fatal("Internal Error creating RegistryResponse: " + e.getMessage());
        }
    }

    /**
     *
     * @param pnr
     * @return
     */
    public OMElement run(OMElement pnr) {
        repoConfig = new Repository(this.getConfigActor());
        try {
            pnr.build();
            handleProvideAndRegisterRequest(pnr);
        } catch (XdsFormatException e) {
            response.add_error(MetadataSupport.XDSRepositoryError, "SOAP Format Error: " + e.getMessage(), this.getClass().getName(), log_message);
        } catch (XDSMissingDocumentException e) {
            response.add_error(MetadataSupport.XDSMissingDocument, e.getMessage(), this.getClass().getName(), log_message);
            logger.warn(logger_exception_details(e));
        } catch (XDSMissingDocumentMetadataException e) {
            response.add_error(MetadataSupport.XDSMissingDocumentMetadata, e.getMessage(), this.getClass().getName(), log_message);
            logger.warn(logger_exception_details(e));
        } catch (XdsInternalException e) {
            response.add_error(MetadataSupport.XDSRepositoryError, "XDS Internal Error:\n " + e.getMessage(), this.getClass().getName(), log_message);
            logger.fatal(logger_exception_details(e));
        } catch (XdsIOException e) {
            response.add_error(MetadataSupport.XDSRepositoryError, "XDS IO Error:\n " + e.getMessage(), this.getClass().getName(), log_message);
            logger.fatal(logger_exception_details(e));
        } catch (XdsConfigurationException e) {
            response.add_error(MetadataSupport.XDSRepositoryError, "XDS Configuration Error:\n " + e.getMessage(), this.getClass().getName(), log_message);
            logger.fatal(logger_exception_details(e));
        } catch (MetadataValidationException e) {
            response.add_error(MetadataSupport.XDSRepositoryError, "Metadata Validation Errors:\n " + e.getMessage(), this.getClass().getName(), log_message);
        } catch (MetadataException e) {
            response.add_error(MetadataSupport.XDSRepositoryError, "Metadata Validation Errors:\n " + e.getMessage(), this.getClass().getName(), log_message);
        } catch (SchemaValidationException e) {
            response.add_error(MetadataSupport.XDSRepositoryError, "Schema Validation Errors:\n" + e.getMessage(), this.getClass().getName(), log_message);
        } catch (XDSRepositoryMetadataError e) {
            response.add_error(MetadataSupport.XDSRepositoryMetadataError, "Metadata Validation Errors:\n " + e.getMessage(), this.getClass().getName(), log_message);
        } catch (XdsException e) {
            response.add_error(MetadataSupport.XDSRepositoryError, "XDS Internal Error:\n " + e.getMessage(), this.getClass().getName(), log_message);
            logger.warn(logger_exception_details(e));
        } catch (Exception e) {
            response.add_error(MetadataSupport.XDSRepositoryError, "Input Error - no SOAP Body:\n " + e.getMessage(), this.getClass().getName(), log_message);
            logger.fatal(logger_exception_details(e));
        }

        this.log_response();
        OMElement res = null;
        try {
            res = response.getResponse();
        } catch (XdsInternalException e) {
            log_message.addErrorParam("Internal Error", "Error generating response from PnR");
        }
        return res;
    }

    /**
     *
     * @param pnr
     * @param m
     * @throws com.vangent.hieos.xutil.exception.XDSMissingDocumentException
     * @throws com.vangent.hieos.xutil.exception.XDSMissingDocumentMetadataException
     */
    private void validatePNR(OMElement pnr, Metadata m) throws XDSMissingDocumentException, XDSMissingDocumentMetadataException {
        ArrayList<OMElement> docs = MetadataSupport.childrenWithLocalName(pnr, "Document");
        ArrayList<String> doc_ids = new ArrayList<String>();

        for (OMElement doc : docs) {
            String id = doc.getAttributeValue(MetadataSupport.id_qname);
            // if id == null or id ==""
            doc_ids.add(id);
        }

        ArrayList<String> eo_ids = m.getExtrinsicObjectIds();

        for (String id : eo_ids) {
            if (!doc_ids.contains(id)) {
                throw new XDSMissingDocumentException("Document with id " + id + " is missing");
            }
        }

        for (String id : doc_ids) {
            if (!eo_ids.contains(id)) {
                throw new XDSMissingDocumentMetadataException("XDSDocumentEntry with id " + id + " is missing");
            }
        }

    }

    /**
     *
     * @param pnr
     * @throws com.vangent.hieos.xutil.exception.MetadataValidationException
     * @throws com.vangent.hieos.xutil.exception.SchemaValidationException
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     * @throws com.vangent.hieos.xutil.exception.MetadataException
     * @throws com.vangent.hieos.xutil.exception.XdsConfigurationException
     * @throws com.vangent.hieos.xutil.exception.XdsIOException
     * @throws com.vangent.hieos.xutil.exception.XdsException
     * @throws java.io.IOException
     */
    private void handleProvideAndRegisterRequest(OMElement pnr)
            throws MetadataValidationException, SchemaValidationException,
            XdsInternalException, MetadataException, XdsConfigurationException,
            XdsIOException, XdsException, IOException {

        RegistryUtility.schema_validate_local(
                pnr,
                MetadataTypes.METADATA_TYPE_RET);

        OMElement sor = findSubjectObjectsRequest(pnr);
        Metadata m = new Metadata(sor);

        //AUDIT:POINT
        //call to audit message for document repository
        //for Transaction id = ITI-41. (Provide & Register Document set-b)
        //Here document consumer is treated as document repository
        performAudit(
                XATNALogger.TXN_ITI41,
                sor,
                null,
                XATNALogger.ActorType.REPOSITORY,
                XATNALogger.OutcomeIndicator.SUCCESS);

        log_message.addOtherParam("SSuid", m.getSubmissionSetUniqueId());
        log_message.addOtherParam("Structure", m.structure());

        this.validatePNR(pnr, m);

        int eo_count = m.getExtrinsicObjectIds().size();
        int doc_count = 0;
        for (OMElement document : MetadataSupport.childrenWithLocalName(pnr, "Document")) {
            doc_count++;
            String id = document.getAttributeValue(MetadataSupport.id_qname);
            OMText binaryNode = (OMText) document.getFirstOMChild();
            boolean optimized = false;
            javax.activation.DataHandler datahandler = null;
            try {
                datahandler = (javax.activation.DataHandler) binaryNode.getDataHandler();
                optimized = true;
            } catch (Exception e) {
                // Message is not optimized.
            }

            // Create the XDSDocument to hold relevant storage parameters.
            XDSDocument doc = new XDSDocument(repoConfig.getRepositoryUniqueId());
            doc.setDocumentId(id);
            if (optimized) {
                InputStream is = null;
                try {
                    is = datahandler.getInputStream();
                } catch (IOException e) {
                    throw new XdsIOException("Error accessing document content from message");
                }
                this.storeXOPDocument(m, doc, is);
            } else {
                String base64 = document.getText();
                //String base64 = binaryNode.getText();
                /* DEBUG:
                System.out.println("+++ NOT XOP OPTIMIZED +++");
                System.out.println("++++ BEGIN BASE64 CONTENT ++++");
                System.out.println(base64);
                System.out.println("++++ END BASE64 CONTENT ++++");
                System.out.println("-> base64.length = " + base64.length());
                System.out.println("-> base64(bytes).length = " + base64.getBytes().length);
                 */
                byte[] ba = Base64.decodeBase64(base64.getBytes());
                /* DEBUG:
                System.out.println("-> base64(decoded).length = " + ba.length);
                 */
                /* BHT: Replaced code (with above line) to get rid of sun.misc dependency.
                BASE64Decoder d = new BASE64Decoder();
                byte[] ba = d.decodeBuffer(base64);
                 */
                storeDocument(m, doc, ba);
            }
        }

        if (eo_count != doc_count) {
            throw new XDSMissingDocumentMetadataException("Submission contained " + doc_count + " documents but " + eo_count +
                    " ExtrinsicObjects in metadata - they must match");
        }

        setRepositoryUniqueId(m);
        OMElement register_transaction = m.getV3SubmitObjectsRequest();
        String epr = getRegistryEndpoint();

        log_message.addOtherParam("Register transaction endpoint", epr);
        log_message.addOtherParam("Register transaction", register_transaction);
        Soap soap = new Soap();
        boolean isAsyncTxn = repoConfig.isRegisterTransactionAsync();
        String action = getRegistrySOAPAction();
        String expectedReturnAction = getRegistryExpectedReturnSOAPAction();
        soap.setAsync(isAsyncTxn);
        try {
            OMElement result;
            try {
                boolean soap12 = this.isRegisterTransactionSOAP12();
                soap.soapCall(
                        register_transaction,
                        epr,
                        false,    /* mtom. */
                        soap12,   /* addressing - only if SOAP 1.2 */
                        soap12,
                        action,
                        expectedReturnAction);
                //AUDIT:POINT
                //call to audit message for document repository
                //for Transaction id = ITI-42. (Register Document set-b)
                //Here document consumer is treated as document repository
                performAudit(
                        XATNALogger.TXN_ITI42,
                        register_transaction,
                        epr,
                        XATNALogger.ActorType.REPOSITORY,
                        XATNALogger.OutcomeIndicator.SUCCESS);
            } catch (SOAPFaultException e) {
                response.add_error(MetadataSupport.XDSRegistryNotAvailable, e.getMessage(), this.getClass().getName(), log_message);
                return;  // Early exit!!
            }
            result = soap.getResult();
            logSOAPHeaders(soap);
            if (result == null) {
                response.add_error(MetadataSupport.XDSRepositoryError, "Null response message from Registry", this.getClass().getName(), log_message);
                log_message.addOtherParam("Register transaction response", "null");

            } else {
                log_message.addOtherParam("Register transaction response", result);
                String status = result.getAttributeValue(MetadataSupport.status_qname);
                if (status == null) {
                    response.add_error(MetadataSupport.XDSRepositoryError, "Null status from Registry", this.getClass().getName(), log_message);
                } else {
                    status = m.stripNamespace(status);
                    if (!status.equals("Success")) {
                        OMElement registry_error_list = MetadataSupport.firstChildWithLocalName(result, "RegistryErrorList");
                        if (registry_error_list != null) {
                            response.addRegistryErrorList(registry_error_list, log_message);
                        } else {
                            response.add_error(MetadataSupport.XDSRepositoryError, "Registry returned Failure but no error list", this.getClass().getName(), log_message);
                        }
                    }
                }
            }
        } catch (Exception e) {
            response.add_error(MetadataSupport.XDSRepositoryError, e.getMessage(), this.getClass().getName(), log_message);
        }
    }

    /**
     *
     * @return
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    private String getRegistryEndpoint() throws XdsInternalException {
        return repoConfig.getRegisterTransactionEndpoint();
    }

    /**
     *
     * @return
     */
    private boolean isRegisterTransactionSOAP12() throws XdsInternalException {
        return repoConfig.isRegisterTransactionSOAP12();
    }

    /**
     *
     * @param soap
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    private void logSOAPHeaders(Soap soap) throws XdsInternalException {
        if (log_message.isLogEnabled()) {
            OMElement in_hdr = soap.getInHeader();
            OMElement out_hdr = soap.getOutHeader();
            log_message.addSOAPParam("Header sent to Registry", (out_hdr == null) ? "Null" : out_hdr);
            log_message.addSOAPParam("Header received from Registry", (in_hdr == null) ? "Null" : in_hdr);
        }
    }

    /**
     *
     * @param pnr
     * @return
     * @throws com.vangent.hieos.xutil.exception.MetadataValidationException
     */
    private OMElement findSubjectObjectsRequest(OMElement pnr) throws MetadataValidationException {
        OMElement sor;
        sor = pnr.getFirstElement();
        if (sor == null || !sor.getLocalName().equals("SubmitObjectsRequest")) {
            throw new MetadataValidationException("Cannot find SubmitObjectsRequest element in submission - top level element is " +
                    pnr.getLocalName());
        }
        return sor;
    }

    /**
     *
     * @param m
     * @param doc
     * @param is
     * @throws com.vangent.hieos.xutil.exception.MetadataException
     * @throws com.vangent.hieos.xutil.exception.XdsIOException
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     * @throws com.vangent.hieos.xutil.exception.XdsConfigurationException
     * @throws com.vangent.hieos.xutil.exception.XdsException
     */
    private void storeXOPDocument(Metadata m, XDSDocument doc, InputStream is)
            throws MetadataException, XdsIOException, XdsInternalException, XdsConfigurationException, XdsException {
        this.validateDocumentMetadata(doc, m); // Validate that all is present.

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(os);
        int length = 8192;  // 8K chunks.
        byte[] buf = new byte[length];
        int size = 0;
        byte[] bytes = null;
        try {
            do {
                size = is.read(buf, 0, length);
                if (size > 0) {
                    bos.write(buf, 0, size);
                }
            } while (size > 0);
            bos.flush();
            bytes = os.toByteArray();
        } catch (IOException e) {
            throw new XdsIOException("Error reading from input stream: " + e.getMessage());
        } finally {
            try {
                is.close();  // A bit of a side effect, but OK for now.
                os.close();
                bos.close();
            } catch (IOException e) {
                // Eat exceptions.
                logger.error("Problem closing a stream", e);
            }
        }

        // Set document vitals and store.
        this.setDocumentVitals(bytes, doc, m);
        this.storeDocument(doc);
    }

    /**
     *
     * @param m
     * @param doc
     * @param bytes
     * @throws com.vangent.hieos.xutil.exception.MetadataException
     * @throws com.vangent.hieos.xutil.exception.XdsIOException
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     * @throws com.vangent.hieos.xutil.exception.XdsConfigurationException
     * @throws com.vangent.hieos.xutil.exception.XdsException
     */
    private void storeDocument(Metadata m, XDSDocument doc, byte[] bytes)
            throws MetadataException, XdsIOException, XdsInternalException, XdsConfigurationException, XdsException {

        // Validate metadata, set document vitals and store.
        this.validateDocumentMetadata(doc, m);
        this.setDocumentVitals(bytes, doc, m);
        this.storeDocument(doc);
    }

    /**
     *
     * @param bytes
     * @param doc
     * @param m
     * @throws XdsInternalException
     * @throws MetadataException
     * @throws XDSRepositoryMetadataError
     */
    private void setDocumentVitals(byte[] bytes, XDSDocument doc, Metadata m) throws XdsInternalException, MetadataException, XDSRepositoryMetadataError {
        // Get a reference to the extrinsic object.
        OMElement extrinsic_object = m.getObjectById(doc.getDocumentId());

        // Set unique id.
        String uid = m.getExternalIdentifierValue(doc.getDocumentId(), MetadataSupport.XDSDocumentEntry_uniqueid_uuid);  // doc uniqueid
        doc.setUniqueId(uid);

        // Set mime type.
        String mime_type = extrinsic_object.getAttributeValue(MetadataSupport.mime_type_qname);
        doc.setMimeType(mime_type);

        // Set bytes, document size and hash (after computation).
        doc.setBytes(bytes);
        doc.setLength(bytes.length);
        Sha1Bean sha1 = new Sha1Bean();
        sha1.setByteStream(bytes);
        try {
            doc.setHash(sha1.getSha1String());
        } catch (Exception e) {
            throw new XdsInternalException("Error calculating hash on repository file");
        }

        // If the submitted metadata has a "hash", it must validate against the submitted document's
        // computed hash value.
        String submittedDocumentHash = m.getSlotValue(extrinsic_object, "hash", 0);
        if (submittedDocumentHash != null) {
            if (!submittedDocumentHash.equalsIgnoreCase(doc.getHash())) {
                throw new XDSRepositoryMetadataError(
                        "Submitted hash(" + submittedDocumentHash + ")" +
                        " does not match computed hash(" +
                        doc.getHash() + ")");
            }
        }

        // If the submitted metadata has a "size", it must validate against the submitted document's
        // computed size value.
        String computedDocumentedSize = new Integer(doc.getLength()).toString();
        String submittedDocumentSize = m.getSlotValue(extrinsic_object, "size", 0);
        if (submittedDocumentSize != null) {
            if (!submittedDocumentSize.equalsIgnoreCase(computedDocumentedSize)) {
                throw new XDSRepositoryMetadataError(
                        "Submitted size(" + submittedDocumentSize + ")" +
                        " does not match computed size(" +
                        computedDocumentedSize + ")");
            }
        }

        // set size, hash into metadata
        m.setSlot(extrinsic_object, "size", computedDocumentedSize);
        m.setSlot(extrinsic_object, "hash", doc.getHash());
        /* BHT: REMOVED
        m.setSlot(extrinsic_object, "URI",  document_uri (uid, mime_type));
        m.setURIAttribute(extrinsic_object, document_uri(uid, mime_type));
         */
    }

    /**
     *
     * @param doc
     */
    private void storeDocument(XDSDocument doc) throws XdsInternalException {
        // Now store the document.
        XDSRepositoryStorage repoStorage = XDSRepositoryStorage.getInstance();
        repoStorage.store(doc);
    }

    /**
     * 
     * @param doc
     * @param m
     * @throws com.vangent.hieos.xutil.exception.MetadataException
     */
    private void validateDocumentMetadata(XDSDocument doc, Metadata m) throws MetadataException {
        String id = doc.getDocumentId();

        // Do some metadata validation.
        OMElement extrinsic_object = m.getObjectById(id);
        if (extrinsic_object == null) {
            throw new MetadataException("Document submitted with id of " + id + " but no ExtrinsicObject exists in metadata with same id");
        }

        // Unique Id better exist.
        String uid = m.getExternalIdentifierValue(id, MetadataSupport.XDSDocumentEntry_uniqueid_uuid);  // doc uniqueid
        if (uid == null) {
            throw new MetadataException("Document " + id + " does not have a Unique ID");
        }

        // Mime type better exist.
        String mime_type = extrinsic_object.getAttributeValue(MetadataSupport.mime_type_qname);
        if (mime_type == null || mime_type.equals("")) {
            throw new MetadataException("ExtrinsicObject " + id + " does not have a mimeType");
        }
    }

    /**
     *
     * @param m
     * @throws com.vangent.hieos.xutil.exception.MetadataException
     * @throws com.vangent.hieos.xutil.exception.XdsInternalException
     */
    private void setRepositoryUniqueId(Metadata m) throws MetadataException, XdsInternalException {
        for (OMElement eo : m.getExtrinsicObjects()) {
            m.setSlot(eo, "repositoryUniqueId", repoConfig.getRepositoryUniqueId());
        }
    }

    /**
     * 
     * @return
     */
    private String getRegistrySOAPAction() {
        return SoapActionFactory.XDSB_REGISTRY_REGISTER_ACTION;
    }

    /**
     *
     * @return
     */
    private String getRegistryExpectedReturnSOAPAction() {
        return SoapActionFactory.XDSB_REGISTRY_REGISTER_ACTION_RESPONSE;
    }
}
