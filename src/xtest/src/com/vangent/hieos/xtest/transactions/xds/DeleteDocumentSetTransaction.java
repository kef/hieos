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
package com.vangent.hieos.xtest.transactions.xds;

import com.vangent.hieos.xtest.framework.BasicTransaction;
import com.vangent.hieos.xtest.framework.StepContext;
import com.vangent.hieos.xtest.framework.TestConfig;
import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.exception.MetadataValidationException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataParser;
import com.vangent.hieos.xutil.metadata.structure.MetadataTypes;
import com.vangent.hieos.xutil.soap.SoapActionFactory;
import com.vangent.hieos.xutil.xml.XMLParser;
import java.io.File;
import java.util.Iterator;

import org.apache.axiom.om.OMElement;

/**
 *
 * @author thumbe
 */
public class DeleteDocumentSetTransaction extends BasicTransaction {

    /**
     *
     * @param s_ctx
     * @param instruction
     * @param instruction_output
     */
    public DeleteDocumentSetTransaction(StepContext s_ctx, OMElement instruction, OMElement instruction_output) {
        super(s_ctx, instruction, instruction_output);
    }

    /**
     *
     * @return
     */
    protected String getTransactionName() {
        return "DeleteDocumentSet";
    }

    /**
     *
     * @return
     */
    @Override
    protected String getRequestAction() {
        if (xds_version == BasicTransaction.xds_b) {
            return SoapActionFactory.XDSB_REGISTRY_DELETE_ACTION;
        } else {
            return SoapActionFactory.ANON_ACTION;
        }
    }

    @Override
    public void run() throws XdsException {
        Iterator elements = instruction.getChildElements();
        while (elements.hasNext()) {
            OMElement part = (OMElement) elements.next();
            parse_instruction(part);
        }

        // Endpoint for default registry
        parseRegistryEndpoint(TestConfig.defaultRegistry, this.getTransactionName());
        validate_xds_version();
       if (metadata_filename == null) {
            throw new XdsInternalException("No MetadataFile element found for "
                    + this.getTransactionName()
                    + " instruction within step "
                    + this.s_ctx.get("step_id"));
        }

        OMElement metadata_ele = XMLParser.fileToOM(metadata_filename);
        Metadata m = MetadataParser.noParse(metadata_ele);

        // compile in results of previous steps
        if (use_id.size() > 0) {
            compileUseIdLinkage(m, use_id);
        }
        if (use_xpath.size() > 0) {
            compileUseXPathLinkage(m, use_xpath);
        }

        useMtom = false;
        useAddressing = true;
        soap_1_2 = true;

        log_metadata(metadata_ele);
        setMetadata(metadata_ele);
        try {
            soapCall();
            OMElement result = getSoapResult();
            if (result != null) {
                this.s_ctx.add_name_value(instruction_output, "Result", result);
                validate_registry_response(
                        result,
                        MetadataTypes.METADATA_TYPE_SQ);
            } else {
                this.s_ctx.add_name_value(instruction_output, "Result", "None");
                s_ctx.set_error("Result was null");
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
