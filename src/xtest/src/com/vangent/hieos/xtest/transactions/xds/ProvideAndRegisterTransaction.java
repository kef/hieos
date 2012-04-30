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
package com.vangent.hieos.xtest.transactions.xds;

import com.vangent.hieos.xtest.framework.BasicTransaction;
import com.vangent.hieos.xtest.framework.StepContext;
import com.vangent.hieos.xutil.metadata.structure.MetadataTypes;
import com.vangent.hieos.xutil.exception.ExceptionUtil;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.soap.SoapActionFactory;
import com.vangent.hieos.xtest.framework.TestConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.activation.FileDataSource;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;

/**
 *
 * @author thumbe
 */
public class ProvideAndRegisterTransaction extends RegisterTransaction {

    boolean use_xop = true;
    HashMap<String, String> document_id_filenames = new HashMap<String, String>();

    /**
     *
     * @param s_ctx
     * @param instruction
     * @param instruction_output
     */
    public ProvideAndRegisterTransaction(StepContext s_ctx, OMElement instruction, OMElement instruction_output) {
        super(s_ctx, instruction, instruction_output);
    }

    /**
     *
     * @throws XdsException
     */
    @Override
    public void run()
            throws XdsException {

        Iterator elements = instruction.getChildElements();
        while (elements.hasNext()) {
            OMElement part = (OMElement) elements.next();
            parse_instruction2(part);
        }

        // PnR always go to default repository
        String repositoryUniqueId = TestConfig.defaultRepository;
        parseRepEndpoint(repositoryUniqueId, "ProvideAndRegisterDocumentSet-b");

        validate_xds_version();

        prepare_metadata();

        if (metadata_filename == null) {
            throw new XdsInternalException("No MetadataFile element found for RegisterTransaction instruction within step " + this.s_ctx.get("step_id"));
        }


        OMElement body = null;
        if (xds_version == BasicTransaction.xds_b) {
            OMElement pnr = metadata.om_factory().createOMElement("ProvideAndRegisterDocumentSetRequest", MetadataSupport.xdsB);
            pnr.addChild(this.getV3SubmitObjectsRequest(metadata));
            for (String id : document_id_filenames.keySet()) {
                String filename = (String) document_id_filenames.get(id);

                if (nameUuidMap != null) {
                    String newId = nameUuidMap.get(id);
                    if (newId != null && !id.equals("")) {
                        id = newId;
                    }
                }

                javax.activation.DataHandler dataHandler = new javax.activation.DataHandler(new FileDataSource(filename));
                OMText t = metadata.om_factory().createOMText(dataHandler, this.use_xop);
                t.setOptimize(this.use_xop);
                OMElement document = metadata.om_factory().createOMElement("Document", MetadataSupport.xdsB);
                document.addAttribute("id", id, null);
                document.addChild(t);
                pnr.addChild(document);
            }

            log_metadata(pnr);

            /*if (XTestDriver.prepair_only) {
                return;
            }*/

            body = pnr;
            try {
                OMElement result = null;

                setMetadata(body);
                useMtom = true;
                useAddressing = true;

                soapCall();
                result = getSoapResult();


                if (result == null) {
                    this.s_ctx.add_name_value(instruction_output, "Result", "None");
                    s_ctx.set_error("Result was null");
                } else {
                    this.s_ctx.add_name_value(instruction_output, "Result", result);

                    validate_registry_response(
                            result,
                            MetadataTypes.METADATA_TYPE_SQ);
                }

            } catch (Exception e) {
                throw new XdsInternalException(ExceptionUtil.exception_details(e));
            }
        }
    }

    ArrayList<String> singleton(String value) {
        ArrayList<String> al = new ArrayList<String>();
        al.add(value);
        return al;
    }

    String htmlize(String xml) {
        return xml.replaceAll("<", "&lt;");
    }

    String file_extension(String path) {
        String[] parts = path.split("/");
        String filename;
        if (parts.length < 2) {
            filename = path;
        } else {
            filename = parts[parts.length - 1];
        }
        int dot = filename.indexOf(".");
        if (dot == -1) {
            return "";
        }
        return filename.substring(dot + 1);
    }

    OMElement getHeader(OMElement envelope) {
        return MetadataSupport.firstChildWithLocalName(envelope, "Header");
    }

    OMElement getBody(OMElement envelope) {
        return MetadataSupport.firstChildWithLocalName(envelope, "Body");
    }

    void parse_instruction2(OMElement part) throws XdsException {
        String part_name = part.getLocalName();
        if (part_name.equals("Document")) {
            String id = part.getAttributeValue(MetadataSupport.id_qname);
            if (id == null || id.equals("")) {
                throw new XdsException("ProvideAndRegisterTransaction: empty id attribute on Document element");
            }
            String filename = part.getText();
            if (filename == null || filename.equals("")) {
                throw new XdsException("ProvideAndRegisterTransaction: Document with id " + id + " has no filename specified");
            }
            document_id_filenames.put(id, TestConfig.base_path + filename);
        } else if (part_name.equals("XDSb")) {
            xds_version = BasicTransaction.xds_b;
        /*} else if (part_name.equals("XDSa")) {
            xds_version = BasicTransaction.xds_a;*/
        } else if (part_name.equals("NoXOP")) {
            this.use_xop = false;
        } else {
            BasicTransaction rt = this;
            rt.parse_instruction(part);
        }
    }

    /**
     *
     * @return
     */
    @Override
    protected String getRequestAction() {
        if (xds_version == BasicTransaction.xds_b) {
            return SoapActionFactory.XDSB_REPOSITORY_PNR_ACTION;
            /*
            if (async) {
                return SoapActionFactory.pnr_b_async_action;
            } else {
                return SoapActionFactory.pnr_b_action;
            }*/
        } else {
            return SoapActionFactory.ANON_ACTION;
        }
    }
}
