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
package com.vangent.hieos.xwebtools.servlets.xviewer;

import com.vangent.hieos.xutil.exception.XConfigException;
import com.vangent.hieos.xwebtools.servlets.framework.BasicServlet;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.registry.RegistryUtility;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;
import com.vangent.hieos.xutil.xconfig.XConfigTransaction;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

/**
 *
 * @author NIST (adapted)
 */
public class ViewerServlet extends BasicServlet implements OutputClass {

    boolean is_pid = false;
    String metadata_file = null;
    HttpSession session;
    String message = "";
    QueryControl query_control = null;
    String verb;
    Map<String, String[]> parms;
    private final static Logger logger = Logger.getLogger(ViewerServlet.class);

    void info(String msg) {
        logger.info("XDSViewer: " + msg);
        System.out.println(msg);
    }

    void message(String m) {
        if (message == null || message.equals("")) {
            message = m;
        } else {
            message = message + "<br />" + m;
        }
    }

    /**
     *
     */
    @Override
    public void init() {
        // This gets around a bug in Leopard (MacOS X 10.5) on Macs
        System.setProperty("http.nonProxyHosts", "");

        //config = new XViewerConfig();

        //(new SecurityProperties()).init();
    }

    void local_init() throws ServletException {
        message = "";
        query_control = get_query_control();
        loadRepositories();
        parms = null;
        verb = "";
    }

    void reset_session() {
        set_query_control(null);
    }

    void clear_session() {
        reset_session();
        for (Enumeration en = session.getAttributeNames(); en.hasMoreElements();) {
            String name = (String) en.nextElement();
            session.removeAttribute(name);
        }
        session.removeAttribute("query_control");  // just in case

    }

    void new_session(QueryControl qc) {
        reset_session();
        set_query_control(qc);
    }

    void new_session(HashMap<String, OMElement> data) {
        reset_session();
    }

    void loadRepositories() {
        if (query_control != null) {
            query_control.repositories = repositories();
        }
    }

    String currentId() {
        return (String) session.getAttribute("id");
    }

    String currentId(String value) {
        session.setAttribute("id", value);
        return value;
    }

    // AMS 05/29 - In the past, files would be input to the xviewer, possibly with metadata to be
    // uploaded to the registry. As that is no longer the case, all references to
    // file_input methods are being commented and should eventually be removed. (FIXME)
    /*
    String file_input() { return (String) session.getAttribute("file_input"); }
    String file_input(String contents) {
    session.setAttribute("file_input", contents);
    return contents;
    }
     */
    String currentEndpoint() {
        return (String) session.getAttribute("endpoint");
    }

    String currentEndpoint(String value) {
        session.setAttribute("endpoint", value);
        return value;
    }

    String currentSecureEndpoint() {
        return (String) session.getAttribute("secure_endpoint");
    }

    String currentSecureEndpoint(String value) {
        session.setAttribute("secure_endpoint", value);
        return value;
    }

    String currentRegistry() {
        return (String) session.getAttribute("registry");
    }

    String currentRegistry(String value) {
        session.setAttribute("registry", value);
        return value;
    }

    boolean isRG() {
        return ((String) session.getAttribute("isRG")).equals("true");
    }

    boolean isRG(boolean value) {
        session.setAttribute("isRG", (value) ? "true" : "false");
        return value;
    }

    String raw_id() {
        String id = (String) session.getAttribute("raw_id");
        if (id == null) {
            id = "";
        }
        return id;
    }

    String raw_id(String value) {
        session.setAttribute("raw_id", value);
        return value;
    }

    /*
    boolean showFileUpload() {
    //		String sfu = this.properties.getString("showFileUpload");
    String sfu = config.getConfigProperty("EnableFileUpload");
    return (sfu.toLowerCase().equals("true"));
    }*/
    HashMap<String, ArrayList<String>> repositories() {

        HashMap<String, ArrayList<String>> repositories = (HashMap<String, ArrayList<String>>) session.getAttribute("repositories");

        repositories = new HashMap<String, ArrayList<String>>();

        // If RG is target the 'repositories' loaded are really RGs

        if (isRG()) {   // XCA
            List<String> rgNames = this.getRgNames();
            for (String rgName : rgNames) {
                ArrayList<String> uids = new ArrayList<String>();
                uids.add(this.getRgSQEndpoint(rgName));
                /*String secureEndpoint = this.getRgAtt(rgName, "SecureEndpoint");
                if (!empty(secureEndpoint)) {
                uids.add(secureEndpoint);
                }*/
                String rgUniqueId = this.getRgHomeCommunityId(rgName);
                repositories.put(rgUniqueId, uids);
            }
        } else {   // XDS
            List<String> repositoryNames = this.getRepositoryNames();
            for (String repositoryName : repositoryNames) {
                ArrayList<String> uids = new ArrayList<String>();
                uids.add(this.getRepositoryEndpoint(repositoryName));
                /*String secureEndpoint = this.getRepositoryAtt(repositoryName, "SecureEndpoint");
                if (!empty(secureEndpoint)) {
                uids.add(secureEndpoint);
                }*/
                String repUniqueId = this.getRepositoryUniqueId(repositoryName);
                repositories.put(repUniqueId, uids);
            }
        }
        session.setAttribute("repositories", repositories);
        return repositories;
    }

    QueryControl get_query_control() {
        query_control = (QueryControl) session.getAttribute("query_control");
        return query_control;
    }

    void set_query_control(QueryControl qc) {
        query_control = qc;
        session.setAttribute("query_control", qc);
    }

    /**
     *
     * @param request
     * @param response
     * @throws ServletException
     */
    public void doPost(
            javax.servlet.http.HttpServletRequest request,
            javax.servlet.http.HttpServletResponse response) throws ServletException {

        this.session = request.getSession();
        this.response = response;
        this.request = request;
        message = "";
        local_init();

        System.out.println("POST");
        for (Enumeration<String> en = request.getParameterNames(); en.hasMoreElements();) {
            String p_name = en.nextElement();
            String p_value = request.getParameter(p_name);
            System.out.println("Parameter: " + p_name + " : " + p_value);
        }

        // Parse input

        String query_type = parse_query_input(request);

        info("POST id=" + currentId() + " raw_id=" + raw_id() + " query_type=" + query_type + " registry=" + currentRegistry());

        // AMS 05/29 - In the past, files would be input to the xviewer, possibly with metadata to be
        // uploaded to the registry. As that is no longer the case, all references to
        // file_input methods are being commented and should eventually be removed. (FIXME)
        //file_input(parse_file_input(request));
        //if (file_input() != null) {
        //	query_control = new QueryControl(file_input());
        //	set_query_control(query_control);
        //}
        //else

        if (query_type.equals("ReloadConf")) {
            System.out.println("Reloading configuration");
            clear_session();
            init();
            local_init();
            refresh();
        } else {
            // do request query
            run_query(query_type);
        }

        refresh();
    }

    /*
    private Metadata parse_metadata(OMElement ele) {
        Metadata m = null;
        try {
            m = MetadataParser.parseNonSubmission(ele);
        } catch (MetadataValidationException e) {
            message(e.getMessage());
            m = MetadataParser.noParse(ele);
        } catch (MetadataException e) {
            message(e.getMessage());
        }
        OMElement error = MetadataSupport.firstChildWithLocalName(ele, "Error");
        if (error != null) {
            message(error.getText());
        }
        return m;
    }*/

    private void run_query(String query_type) throws ServletException {
        if (!query_type.equals("")) {
            if (currentRegistry().equals("")) {
                this.query_control = null;
                refresh("No registry selected");
                return;
            }
            if (currentId().equals("")) {
                this.query_control = null;
                refresh("No ID");
                return;
            }
        }

        this.reset_session();
        query_control = new QueryControl();
        set_query_control(query_control);

        QueryContents qc = null;
        try {
            String secureEndpoint = currentSecureEndpoint();
            System.out.println("secureEndpoint = " + secureEndpoint);
            if (!empty(secureEndpoint)) {
                query_control.setEndpoint(secureEndpoint);
                qc = top_level_query(query_type, is_pid);
            }

            String nonSecureEndpoint = currentEndpoint();
            if ((qc == null || qc.hasExceptions()) && !empty(nonSecureEndpoint)) {
                query_control.setEndpoint(nonSecureEndpoint);
                top_level_query(query_type, is_pid);
            }
        } catch (Exception e) {
            message(e.getMessage());
        }
    }

    boolean empty(String x) {
        return x == null || x.equals("");
    }

    QueryContents top_level_query(String query_type, boolean is_pid) throws Exception {
        System.out.println("\n\ntop_level_query = " + query_type + " \n\n");
        if (query_type.equals("SS")) {
            if (is_pid) {
                return query_control.queryFindSS(currentId());
            } else {
                return query_control.queryGetSSandContents(query_control.singleton(currentId()));
            }
        } else if (query_type.equals("Doc")) {
            if (is_pid) {
                if (this.isRegistry(currentRegistry())) {
                    query_control.leafClassQuery(false);  // for a Registry
                    System.out.println("Its a Registry");
                } else {
                    // This specialization is not reasonable but was needed for NHIN
                    query_control.leafClassQuery(true);   // for a RG
                    query_control.crossGateway();
                    //String soap11Str = this.getRgAtt(currentRegistry(), "Soap11");
                    //boolean soap11 = soap11Str.equals("true");
                    boolean soap11 = false;
                    query_control.setSoap11(soap11);
                    System.out.println("Its a RG");
                }

                return query_control.queryFindDoc(currentId());

            } else {
                return query_control.queryGetDocuments(query_control.singleton(currentId()));
            }
        } else if (query_type.equals("Fol")) {
            if (is_pid) {
                return query_control.queryFindFol(currentId());
            } else {
                return query_control.queryGetFolandContents(query_control.singleton(currentId()));
            }

        } else if (query_type.equals("clear")) {
            clear_session();
        } else if (query_type.equals("ReloadConf")) {
            System.out.println("\n\n******************* Reloading ******************\n\n");
            init();
            local_init();
            clear_session();
            refresh();
            query_control = null;
        }
        return null;

    }

    // AMS 05/29 - In the past, files would be input to the xviewer, possibly with metadata to be
    // uploaded to the registry. As that is no longer the case, all references to
    // file_input methods are being commented and should eventually be removed. (FIXME).
    // The following method was invoked by file_input(request) and it would create a RequestBean
    // which would eventually delegate to Multipart to handle multiple mime parts.
    // The classes RequestBean and Multipart have been deleted effective 05/29 and the following
    // method has been commented out to prevent compilation errors.
    //
	/*private String parse_file_input(
    javax.servlet.http.HttpServletRequest request) {
    try {
    RequestBean req_bean = new RequestBean();
    req_bean.setRequest(request);
    req_bean.setPartName("metadata_file");
    return req_bean.getPart();
    }
    catch (Exception e){
    message(e.getMessage());
    return null;
    }
    }
     */
    private String parse_query_input(
            javax.servlet.http.HttpServletRequest request) {
        String raw_id = raw_id(request.getParameter("id"));
        if (raw_id == null) {
            raw_id = raw_id("");
        }
        raw_id = raw_id(raw_id.replaceAll("&amp;", "&"));

        System.out.println("raw_id = " + raw_id);

        currentId(raw_id.trim());

        //String is_pid_str = request.getParameter("is_pid");
        is_pid = raw_id.contains("^^^");

        String query_type = request.getParameter("query_type");
        if (query_type == null) {
            query_type = "";
        }

        String registry;
        registry = request.getParameter("registry");
        if (registry == null) {
            registry = "";
        }
        currentRegistry(registry);

        if (this.isRegistry(registry)) {
            //currentSecureEndpoint(this.getRegistryAtt(registry, "SecureSQ"));
            currentEndpoint(this.getRegistrySQEndpoint(registry));
            isRG(false);
        } else {
            //currentSecureEndpoint(this.getRgAtt(registry, "SecureEndpoint"));
            currentEndpoint(this.getRgSQEndpoint(registry));
            isRG(true);
        }
        loadRepositories(); // config is different for XDS/XCA

        return query_type;
    }

    String last_part(String parts) {
        String[] part_a = parts.split("/");
        if (part_a.length == 0) {
            return parts;
        }
        return part_a[part_a.length - 1];
    }

    /**
     *
     * @param request
     * @param response
     * @throws ServletException
     */
    public void doGet(
            javax.servlet.http.HttpServletRequest request,
            javax.servlet.http.HttpServletResponse response) throws ServletException {
        session = request.getSession();
        this.response = response;
        this.request = request;
        local_init();

        verb = last_part(request.getRequestURI());
        parms = request.getParameterMap();

        String[] id_array = request.getParameterValues("id");
        if (id_array != null && id_array.length > 0) {
            currentId(id_array[0]);
        }

        String[] cntl_array = request.getParameterValues("cntl");
        String cntl = null;
        int cntl_i = 0;
        if (cntl_array != null && cntl_array.length != 0) {
            cntl = cntl_array[0];
            cntl_i = Integer.parseInt(cntl);
        }

        info("GET id=" + currentId() + " raw_id=" + raw_id() + " verb=" + verb + " registry=" + currentRegistry());

        boolean refresh = true;
        if (verb == null) {
        } else if (query_control == null) {
        } else if (verb.equals("related") && currentId() != null) {
            query_control.queryGetRelated(query_control.singleton(currentId()));
        } else if (verb.equals("ssac") && currentId() != null) {
            query_control.queryGetSSandContents(query_control.singleton(currentId()));
        } else if (verb.equals("ss") && currentId() != null) {
            query_control.queryGetSubmissionSets(query_control.singleton(currentId()));
        } else if (verb.equals("nextpage") && cntl != null) {
            QueryContents qc = query_control.getQueryContents(cntl_i);
            if (qc instanceof PagedQueryContents) {
                PagedQueryContents paged = (PagedQueryContents) qc;
                paged.next();
            }
        } else if (verb.equals("ret_a") && currentId() != null) {
            // BHT: REMOVED XDS.a call.
            // query_control.retrieve_a(query_control.singleton(currentId()));
        } else if (verb.equals("ret_b") && currentId() != null) {
            QueryContents qc = query_control.retrieve_b(query_control.singleton(currentId()));
        } else if (verb.equals("display_headers") && currentId() != null) {
            QueryContents qc = query_control.getQueryContents(cntl_i);
            if (qc instanceof RetrieveAQueryContents) {
            } else {
                throw new ServletException("ViewerServlet.doGet(): http_display_headers request not defined for instance of ");
            }
        } else if (verb.equals("display_doc") && cntl != null) {
            QueryContents qc = query_control.getQueryContents(cntl_i);
            if (qc instanceof RetrieveAQueryContents) {
                RetrieveAQueryContents ret_a = (RetrieveAQueryContents) qc;
                response.setContentType(ret_a.get_content_type());
                byte[] data = ret_a.get_content();
                try {
                    response.getOutputStream().write(data, 0, data.length);
                } catch (Exception e) {
                    throw new ServletException(RegistryUtility.exception_details(e));
                }
                refresh = false;
            }
            if (qc instanceof RetrieveBQueryContents) {
                RetrieveBQueryContents ret_b = (RetrieveBQueryContents) qc;
                response.setContentType(ret_b.get_content_type());
                byte[] data = ret_b.get_content();
                if (data == null) {
                    throw new ServletException("ViewerServlet.doGet(): get_content() returned null");
                }
                OutputStream os;
                try {
                    os = response.getOutputStream();
                } catch (IOException e) {
                    throw new ServletException(RegistryUtility.exception_details(e));
                }
                if (os == null) {
                    throw new ServletException("ViewerServlet.doGet(): getOutputStream() return null");
                }
                try {
                    os.write(data, 0, data.length);
                } catch (Exception e) {
                    throw new ServletException(RegistryUtility.exception_details(e));
                }
                refresh = false;
            }
        } else if (verb.equals("delete") && cntl != null) {
            query_control.deleteQueryContents(cntl_i);
        }

        if (refresh) {
            refresh();
        }
        if (query_control != null) {
            System.out.println(query_control.structures());
        }
    }

    private void refresh(String message) throws ServletException {
        message(message);
        refresh();
    }

    void refresh(String verb, Map parms) {
    }

    private void refresh() throws ServletException {
        info("refresh: " + message);
        h().head("XDS Viewer");
        h().open("table width=\"100%\"");
        h().open("tr");
        h().o("<td colspan=\"2\" bgcolor=\"lightgrey\">");

        if (query_control == null) {
            System.out.println("refresh: query_control is null");
        }
        Metadata m = (query_control != null) ? query_control.getMetadata() : new Metadata();
        sq_panel(m);

        h().clos("td");
        h().clos("tr");
        if (message != null && !message.equals("")) {
            h().o("<tr span=\"2\">");
            h().o("<td>");
            h().o(message);
            h().o("</td>");
        }

        h().open("tr");

        displayStructure();

        h().o("<td  valign=\"top\">");
        //detail
        if (parms != null && query_control != null) {
            query_control.displayDetail(verb, parms, h());
        }

        h().o("</td>");
        h().o("</tr>");
        h().clos("table");
        h().tail();
        close();
    }

    private void displayStructure() throws ServletException {
        System.out.println("displayStructure");
        // structure
        h().open("td width=\"35%\" bgcolor=\"lightgrey\" valign=\"top\"");
        try {
            if (query_control != null) {
                int i = 0;
                XView xv = new XView(h());
                for (QueryContents qc : query_control.getAllQueryContents()) {
                    System.out.println("displayStructure: " + qc.getClass().getName());
                    xv.displayOutline(query_control, i);
                    i++;
                }
            } else {
                System.out.println("No Query Control");
            }
        } catch (Exception e) {
            h().alert(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        h().clos("td");
    }

    void sq_panel(Metadata m) throws ServletException {
        h().post_form("/xwebtools/xviewer", null);

        h().open("table");

        h().open("tr");
        h().open("td");
        o("Registry: ");
        h().clos("td");
        System.out.println("Registries: " + this.getRegistryNames());
        h().open("td");
        for (String name : this.getRegistryNames()) {
            String checked = "";
            if (name.equals(currentRegistry())) {
                checked = "checked=\"checked\"";
            }
            h().input("radio", "registry", name, name, checked);
        }
        o("  ______ GATE: ");
        System.out.println("Rg: " + this.getRgNames());
        for (String name : this.getRgNames()) {
            String checked = "";
            if (name.equals(currentRegistry())) {
                checked = "checked=\"checked\"";
            }
            h().input("radio", "registry", name, name, checked);
        }
        h().clos("td");
        h().clos("tr");

        h().open("tr");
        h().open("td");
        o("id: ");
        h().clos("td");
        h().open("td");
        h().input("text", "id", raw_id(), null, "size=\"65\"");

//		o("Patient ID?");
//		h().input("checkbox", "is_pid", "", null, (is_pid)?"checked=\"true\"" : "");
        o("PatientId or UniqueId or UUID");
        h().clos("td");
        h().clos("tr");


        h().open("tr");
        h().open("td");
        o("Query For:");
        h().clos("td");
        h().open("td");
        h().input("submit", "query_type", "SS", null, "");
        h().input("submit", "query_type", "Doc", null, "");
        h().input("submit", "query_type", "Fol", null, "");
        o("________");
        h().input("submit", "query_type", "Clear", null, "");
        h().input("submit", "query_type", "ReloadConf", null, "");
        h().clos("td");
        h().clos("tr");

        h().clos("table");



        h().end_form();
        h().hr();

        /*if (showFileUpload()) {
        h().post_form("/xwebtools/xviewer", "multipart/form-data");
        h().o("File: <input type=\"file\" name=\"metadata_file\" size=\"50\"/>");
        h().o("<input type=submit name=\"submit\" value=\"Load\"/>");
        h().end_form();
        }*/
    }

    /**
     *
     * @param o
     */
    public void o(Object o) {
        try {
            h().o(o);
        } catch (Exception e) {
        }
    }

    /**
     *
     * @return
     */
    private List<String> getRgNames() {
        return this.getConfigNames(XConfig.XCA_RESPONDING_GATEWAY_TYPE);
    }

    /**
     *
     * @return
     */
    private List<String> getRegistryNames() {
        List<String> registryNames = this.getConfigNames(XConfig.XDSB_DOCUMENT_REGISTRY_TYPE);
        // Combine initiating gateways here also.
        List<String> igNames = this.getConfigNames(XConfig.XCA_INITIATING_GATEWAY_TYPE);
        List<String> names = new ArrayList<String>();
        names.addAll(registryNames);
        names.addAll(igNames);
        return names;
    }

    /**
     * 
     * @return
     */
    private List<String> getRepositoryNames() {
        return this.getConfigNames(XConfig.XDSB_DOCUMENT_REPOSITORY_TYPE);
    }

    /**
     * 
     * @param type
     * @return
     */
    private List<String> getConfigNames(String type) {
        List<String> names = new ArrayList<String>();
        try {
            XConfig config = XConfig.getInstance();
            List<XConfigObject> configObjects = config.getXConfigObjectsOfType(type);
            for (XConfigObject configObject : configObjects) {
                names.add(configObject.getName());
            }
        } catch (XConfigException ex) {
            //
        }
        return names;
    }

    /**
     * 
     * @param registryName
     * @return
     */
    private boolean isRegistry(String registryName) {
        XConfigObject configObject = this.getConfigActor(registryName, XConfig.XDSB_DOCUMENT_REGISTRY_TYPE);
        if (configObject != null)
        {
            return true;
        }
        // Now, see if it is an Initiating Gateway.
        configObject = this.getConfigActor(registryName, XConfig.XCA_INITIATING_GATEWAY_TYPE);
        return configObject != null;
    }

    /**
     *
     * @param name
     * @param type
     * @return
     */
    private XConfigActor getConfigActor(String name, String type) {

        try {
            XConfig config = XConfig.getInstance();
            return config.getXConfigActorByName(name, type);
        } catch (XConfigException ex) {
            //
        }
        return null;
    }

    /**
     *
     * @param registryName
     * @return
     */
    private String getRegistrySQEndpoint(String registryName) {
        XConfigActor config = this.getConfigActor(registryName, XConfig.XDSB_DOCUMENT_REGISTRY_TYPE);
        if (config == null)
        {
            // Must be an Initiating Gateway.
            config = this.getConfigActor(registryName, XConfig.XCA_INITIATING_GATEWAY_TYPE);
        }
        XConfigTransaction txn = config.getTransaction("RegistryStoredQuery");
        return txn.getEndpointURL();
    }

    /**
     *
     * @param repoName
     * @return
     */
    private String getRepositoryEndpoint(String repoName) {
        XConfigActor config = this.getConfigActor(repoName, XConfig.XDSB_DOCUMENT_REPOSITORY_TYPE);
        XConfigTransaction txn = config.getTransaction("RetrieveDocumentSet");
        return txn.getEndpointURL();
    }

    /**
     *
     * @param rgName
     * @return
     */
    private String getRgSQEndpoint(String rgName) {
        XConfigActor config = this.getConfigActor(rgName, XConfig.XCA_RESPONDING_GATEWAY_TYPE);
        XConfigTransaction txn = config.getTransaction("CrossGatewayQuery");
        return txn.getEndpointURL();
    }

    /**
     * 
     * @param rgName
     * @return
     */
    private String getRgHomeCommunityId(String rgName) {
        XConfigActor config = this.getConfigActor(rgName, XConfig.XCA_RESPONDING_GATEWAY_TYPE);
        return config.getUniqueId();
    }

    /**
     * 
     * @param repoName
     * @return
     */
    private String getRepositoryUniqueId(String repoName) {
         XConfigActor config = this.getConfigActor(repoName, XConfig.XDSB_DOCUMENT_REPOSITORY_TYPE);
         return config.getUniqueId();
    }
}
