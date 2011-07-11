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
package com.vangent.hieos.services.xca.gateway.serviceimpl;

import com.vangent.hieos.xutil.exception.SchemaValidationException;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.services.framework.XAbstractService;
import com.vangent.hieos.services.xca.gateway.transactions.XCAAdhocQueryRequest;
import com.vangent.hieos.services.xca.gateway.transactions.XCARetrieveDocumentSet;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.exception.XdsValidationException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public abstract class XCAGateway extends XAbstractService {

    private final static Logger logger = Logger.getLogger(XCAGateway.class);

    /**
     * 
     * @return
     * @throws XdsInternalException
     */
    abstract XCAAdhocQueryRequest getAdHocQueryTransaction() throws XdsInternalException;

    /**
     *
     * @return
     * @throws XdsInternalException
     */
    abstract XCARetrieveDocumentSet getRetrieveDocumentSet() throws XdsInternalException;

    /**
     *
     * @return
     */
    abstract String getQueryTransactionName();

    /**
     *
     * @return
     */
    abstract String getRetTransactionName();

    /**
     *
     * @param request
     * @return
     * @throws AxisFault
     */
    public OMElement AdhocQueryRequest(OMElement request) throws AxisFault {
        beginTransaction(getQueryTransactionName(), request);
        try {
            OMElement ahq = MetadataSupport.firstChildWithLocalName(request, "AdhocQuery");
            if (ahq == null) {
                endTransaction(false);
                return this.start_up_error(request, null, XAbstractService.ActorType.REGISTRY, "XCA" + " only accepts Stored Query - AdhocQuery element not found");
            }
            validateWS();
            validateNoMTOM();
            validateQueryTransaction(request);
            // Delegate all the hard work to the XCAAdhocQueryRequest class (follows same NIST patterns).
            XCAAdhocQueryRequest transaction = this.getAdHocQueryTransaction();
            // Now, do some work!
            OMElement result = transaction.run(request);
            endTransaction(transaction.getStatus());
            return result;
        } catch (SchemaValidationException ex) {
            return endTransaction(request, ex, XAbstractService.ActorType.REGISTRY, "");
        } catch (XdsInternalException ex) {
            return endTransaction(request, ex, XAbstractService.ActorType.REGISTRY, "");
        } catch (XdsValidationException ex) {
            return endTransaction(request, ex, XAbstractService.ActorType.REGISTRY, "");
        }
    }

    /**
     *
     * @param request
     * @return
     * @throws AxisFault 
     */
    public OMElement RetrieveDocumentSetRequest(OMElement request) throws AxisFault {
        beginTransaction(getRetTransactionName(), request);
        validateWS();
        validateMTOM();
        try {
            validateRetrieveTransaction(request);
            // Delegate all the hard work to the XCARetrieveDocumentSet class (follows same NIST patterns).
            XCARetrieveDocumentSet transaction = this.getRetrieveDocumentSet();
            OMElement result = transaction.run(request);
            endTransaction(transaction.getStatus());
            return result;
        } catch (SchemaValidationException ex) {
            return endTransaction(request, ex, XAbstractService.ActorType.REPOSITORY, "");
        } catch (XdsInternalException ex) {
            return endTransaction(request, ex, XAbstractService.ActorType.REPOSITORY, "");
        } catch (XdsValidationException ex) {
            return endTransaction(request, ex, XAbstractService.ActorType.REPOSITORY, "");
        }
    }

    /**
     *
     * @param request
     * @throws XdsValidationException
     */
    protected void validateQueryTransaction(OMElement request)
            throws XdsValidationException {
        OMNamespace ns = request.getNamespace();
        String ns_uri = ns.getNamespaceURI();
        if (ns_uri == null || !ns_uri.equals(MetadataSupport.ebQns3.getNamespaceURI())) {
            throw new XdsValidationException("Invalid namespace on " + request.getLocalName() + " (" + ns_uri + ")");
        }
        if (!this.isSQ(request)) {
            throw new XdsValidationException("Only StoredQuery is acceptable on this endpoint");
        }
    }

    /**
     *
     * @param request
     * @return
     */
    private boolean isSQ(OMElement request) {
        return MetadataSupport.firstChildWithLocalName(request, "AdhocQuery") != null;
    }

    /**
     * 
     * @param request
     * @throws XdsValidationException
     */
    protected void validateRetrieveTransaction(OMElement request) throws XdsValidationException {
        OMNamespace ns = request.getNamespace();
        String ns_uri = ns.getNamespaceURI();
        if (ns_uri == null || !ns_uri.equals(MetadataSupport.xdsB.getNamespaceURI())) {
            throw new XdsValidationException("Invalid namespace on " + request.getLocalName() + " (" + ns_uri + ")");
        }
    }
}
