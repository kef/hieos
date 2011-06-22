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

package com.vangent.hieos.services.xds.bridge.transactions;

import com.vangent.hieos.hl7v3util.model.subject.CodedValue;
import com.vangent.hieos.services.xds.bridge.client.XDSDocumentRegistryClient;
import com.vangent.hieos.services.xds.bridge.client.XDSDocumentRepositoryClient;
import com.vangent.hieos.services.xds.bridge.mapper.IXDSMapper;
import com.vangent.hieos.services.xds.bridge.mapper.MapperFactory;
import com.vangent.hieos.services.xds.bridge.model.Document;
import com.vangent.hieos.services.xds.bridge.model.SubmitDocumentRequest;
import com.vangent.hieos.services.xds.bridge.model.SubmitDocumentRequestBuilder;
import com.vangent.hieos.services.xds.bridge.model.XDSPnR;
import com.vangent.hieos.services.xds.bridge.support.IMessageHandler;
import com.vangent.hieos.services.xds.bridge.utils.DebugUtils;
import com.vangent.hieos.xutil.services.framework.XBaseTransaction;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.context.MessageContext;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-09
 * @author         Jim Horner
 */
public class SubmitDocumentRequestHandler extends XBaseTransaction
        implements IMessageHandler {

    /** Field description */
    private final static Logger logger =
        Logger.getLogger(SubmitDocumentRequestHandler.class);

    /** Field description */
    private final MapperFactory mapperFactory;

    /** Field description */
    private final XDSDocumentRegistryClient registryClient;

    /** Field description */
    private final XDSDocumentRepositoryClient repositoryClient;

    /** Field description */
    private final SubmitDocumentRequestBuilder submitDocumentRequestBuilder;

    /**
     * Constructs ...
     *
     *
     * @param logMessage
     * @param builder
     * @param mapFactory
     * @param regClient
     * @param repoClient
     */
    public SubmitDocumentRequestHandler(XLogMessage logMessage,
            SubmitDocumentRequestBuilder builder, MapperFactory mapFactory,
            XDSDocumentRegistryClient regClient,
            XDSDocumentRepositoryClient repoClient) {

        super();

        // super(logMessage); ??
        this.log_message = logMessage;

        this.submitDocumentRequestBuilder = builder;
        this.mapperFactory = mapFactory;
        this.registryClient = regClient;
        this.repositoryClient = repoClient;
    }

    /**
     * Method description
     *
     *
     * @param request
     *
     * @return
     */
    private OMElement createResponse(OMElement request) {

        OMFactory fac = request.getOMFactory();
        OMNamespace ns = request.getNamespace();
        OMElement result = fac.createOMElement("SubmitDocumentResponse", ns);

        result.addAttribute("status", "Success", ns);

        return result;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    protected MapperFactory getMapperFactory() {
        return mapperFactory;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    protected XDSDocumentRegistryClient getRegistryClient() {
        return registryClient;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    protected XDSDocumentRepositoryClient getRepositoryClient() {
        return repositoryClient;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    protected SubmitDocumentRequestBuilder getSubmitDocumentRequestBuilder() {
        return submitDocumentRequestBuilder;
    }

    /**
     * Method description
     *
     *
     * @param messageContext
     * @param request
     *
     * @return
     *
     * @throws Exception
     */
    @Override
    public OMElement run(MessageContext messageContext, OMElement request)
            throws Exception {

        // TODO move lower ... move to try/catch paradigm
        // create proper response with errors
        OMElement result = createResponse(request);

        // unmarshal xml
        SubmitDocumentRequestBuilder builder =
            getSubmitDocumentRequestBuilder();

        SubmitDocumentRequest sdr = builder.buildSubmitDocumentRequest(request);

        List<Document> documents = sdr.getDocuments();

        XDSDocumentRegistryClient regclient = getRegistryClient();
        XDSDocumentRepositoryClient repoclient = getRepositoryClient();

        // TODO
        // from here we need to start tracking exceptions per document
        // to send back a proper response of success, partial, failure

        for (Document document : documents) {

            CodedValue type = document.getType();
            IXDSMapper mapper = getMapperFactory().getMapper(type);

            XDSPnR pnr = mapper.map(sdr.getPatientId(), document);

            logger.debug(DebugUtils.toPrettyString(pnr.getNode()));

            // call registry client to add patientId if it doesn't exist
            // regclient.blah(pnr);

            // send PNR
            OMElement pnrResponse =
                repoclient.submitProvideAndRegisterDocumentSet(pnr);
            
            logger.debug(DebugUtils.toPrettyString(pnrResponse));
        }

        return result;
    }
}
