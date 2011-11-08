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
package com.vangent.hieos.xutil.response;

import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import java.util.ArrayList;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;

public abstract class Response extends ErrorLogger {

    //String status = "Success";
    protected OMNamespace ebRSns;
    protected OMNamespace ebRIMns;
    protected OMNamespace ebQns;
    ArrayList query_results = null;
    boolean errors_and_warnings_included = false;

    //String errors_and_warnings = "";
    //boolean has_errors = false;
    public abstract OMElement getRoot() throws XdsInternalException;
    OMElement response = null;
    OMElement content = null;
    public RegistryErrorList registryErrorList;

    public Response() throws XdsInternalException {
        init(new RegistryErrorList(true /* log */));
    }

    public Response(RegistryErrorList rel) throws XdsInternalException {
        init(rel);
    }

    void init(RegistryErrorList rel) throws XdsInternalException {
        ebRSns = MetadataSupport.ebRSns3;
        ebRIMns = MetadataSupport.ebRIMns3;
        ebQns = MetadataSupport.ebQns3;
        registryErrorList = rel;
    }

    abstract public void addQueryResults(OMElement metadata) throws XdsInternalException;

    public OMElement getResponse() throws XdsInternalException {
        if (registryErrorList.hasContent()) {
            OMElement error_list = registryErrorList.getRegistryErrorList();
            if (error_list != null) {
                response.addChild(error_list);
            }
        }
        response.addAttribute("status", MetadataSupport.response_status_type_namespace + registryErrorList.getStatus(), null);
        if (this instanceof RetrieveMultipleResponse) {
            return ((RetrieveMultipleResponse) this).rdsr;
        } else if (this instanceof RegistryResponse) {
        } else if (this instanceof AdhocQueryResponse) {
            AdhocQueryResponse a = (AdhocQueryResponse) this;
            OMElement query_result = a.getQueryResult();
            if (query_result != null) {
                response.addChild(query_result);
            }
        } else if (this instanceof XCAAdhocQueryResponse) {
            XCAAdhocQueryResponse a = (XCAAdhocQueryResponse) this;
            OMElement queryResult = a.getQueryResult();
            if (queryResult != null) {
                response.addChild(queryResult);
            }
        } else {
            throw new XdsInternalException("Response.getResponse(): unknown extending class: " + getClass().getName());
        }
        return response;
    }

    /**
     * This is a minor hack ... in some cases, when consolidating results (e.g XCA) from multiple sources, it
     * may be ok to have a partial success status even when errors exist.
     */
    public void forcePartialSuccessStatus() {
        registryErrorList.forcePartialSuccessStatus();
    }

    public void add_error(String code, String msg, String location, XLogMessage log_message) {
        registryErrorList.add_error(code, msg, location, log_message);
    }

    public void addRegistryErrorList(OMElement rel, XLogMessage log_message) throws XdsInternalException {
        registryErrorList.addRegistryErrorList(rel, log_message);
    }

    public void add_warning(String code, String msg, String location, XLogMessage log_message) {
        registryErrorList.add_warning(code, msg, location, log_message);
    }

    public String getErrorsAndWarnings() {
        return registryErrorList.getErrorsAndWarnings();
    }

    public void error(String msg) {
        registryErrorList.error(msg);
    }

    public void warning(String msg) {
        registryErrorList.warning(msg);
    }

    public boolean has_errors() {
        return registryErrorList.has_errors();
    }
}
