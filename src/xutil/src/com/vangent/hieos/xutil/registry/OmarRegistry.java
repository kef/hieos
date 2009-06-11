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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vangent.hieos.xutil.registry;


//java Imports
import java.io.StringWriter;
import java.util.HashMap;

//freebxml imports (from omar)
import org.freebxml.omar.common.RegistryResponseHolder;
import org.freebxml.omar.common.spi.LifeCycleManager;
import org.freebxml.omar.common.spi.LifeCycleManagerFactory;
import org.freebxml.omar.common.spi.QueryManager;
import org.freebxml.omar.common.spi.QueryManagerFactory;
import org.freebxml.omar.server.util.ServerResourceBundle;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
//import org.freebxml.omar.server.interfaces.Response;
//ebxml bindings imports
import org.oasis.ebxml.registry.bindings.lcm.ApproveObjectsRequestType;
import org.oasis.ebxml.registry.bindings.lcm.DeprecateObjectsRequestType;
import org.oasis.ebxml.registry.bindings.lcm.RelocateObjectsRequestType;
import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequestType;
import org.oasis.ebxml.registry.bindings.lcm.SetStatusOnObjectsRequestType;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequestType;
import org.oasis.ebxml.registry.bindings.lcm.UndeprecateObjectsRequestType;
import org.oasis.ebxml.registry.bindings.lcm.UpdateObjectsRequestType;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequestType;
import org.oasis.ebxml.registry.bindings.query.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;
//axis 2 imports
import org.apache.axiom.om.OMElement;
//NIST Imports
import com.vangent.hieos.xutil.exception.XdsInternalException;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;

/**
 *
 * @author nistra
 */
public class OmarRegistry
{

    //Instantiate required objects
    private org.freebxml.omar.common.BindingUtility bu = org.freebxml.omar.common.BindingUtility.
        getInstance();
    private AuthenticationServiceImpl authc = AuthenticationServiceImpl.
        getInstance();
    private ServerRequestContext context = null;
    //This object is for adhocquery
    private QueryManager qm = QueryManagerFactory.getInstance().getQueryManager();
    //This object is for submitobjectrequest
    private LifeCycleManager lcm = LifeCycleManagerFactory.getInstance().
        getLifeCycleManager();
    public static final String ALIAS_REGISTRY_OPERATOR = "urn:freebxml:registry:predefinedusers:registryoperator";

    public OmarRegistry(OMElement request)
    {
        //System.out.println("*** OMAR REQUEST: " + request);
        try
        {
            //Create Variables which are required to call to the Request class.
            //creating the context object
            Object requestObject = bu.getRequestObject(request.getLocalName(),
                request.toString());
            // FIXME (BHT): Should use UUID as context ID.
            String contextId = "Request." + requestObject.getClass().getName();
            context = new ServerRequestContext(contextId,
                (RegistryRequestType) requestObject);

            //instantiate User object with registryObject
            //registryObject has the authority to submit object requests as well as query the registry objects
            //In future this have to be changed to handle real user configured and created in in the omar DB
            UserType user = authc.registryOperator;
            context.setUser(user);
        }
        catch (Exception e)
        {
            // FIXME (BHT): Can not just eat exceptions.
            e.printStackTrace();
            System.out.println("   OmarRegistry()     " + e.getMessage());
        }
    }

    /**
     * Processes the Request by dispatching it to a service in the registry.
     */
    public OMElement process()
        throws XdsInternalException
    {
        RegistryResponseType rr = null;
        try
        {
            RegistryRequestType message = context.getCurrentRegistryRequest();
            long startTime = System.currentTimeMillis();
            if (message instanceof AdhocQueryRequestType)
            {
                System.out.println("submitAdhocQuery ...");
                rr = qm.submitAdhocQuery(context);
                System.out.println("submitAdhocQuery ELAPSED TIME: " + new Long(System.currentTimeMillis() - startTime).toString());
            }
            else if (message instanceof SubmitObjectsRequestType)
            {
                System.out.println("submitObjects ...");
                rr = lcm.submitObjects(context);
                System.out.println("submitObjects ELAPSED TIME: " + new Long(System.currentTimeMillis() - startTime).toString());
            }
            else if (message instanceof ApproveObjectsRequestType)
            {
                System.out.println("approveObjects ...");
                rr = lcm.approveObjects(context);
                System.out.println("approveObjects ELAPSED TIME: " + new Long(System.currentTimeMillis() - startTime).toString());
            }
            else if (message instanceof DeprecateObjectsRequestType)
            {
                System.out.println("deprecateObjects ...");
                rr = lcm.deprecateObjects(context);
                System.out.println("deprecateObjects ELAPSED TIME: " + new Long(System.currentTimeMillis() - startTime).toString());
            }
            else if (message instanceof SetStatusOnObjectsRequestType)
            {
                System.out.println("setStatusOnObjects ...");
                rr = lcm.setStatusOnObjects(context);
                System.out.println("setStatusOnObjects ELAPSED TIME: " + new Long(System.currentTimeMillis() - startTime).toString());
            }
            else if (message instanceof UndeprecateObjectsRequestType)
            {
                System.out.println("unDeprecateObjects ...");
                rr = lcm.unDeprecateObjects(context);
                System.out.println("unDeprecateObjects ELAPSED TIME: " + new Long(System.currentTimeMillis() - startTime).toString());
            }
            else if (message instanceof RemoveObjectsRequestType)
            {
                System.out.println("removeObjects ...");
                rr = lcm.removeObjects(context);
                System.out.println("removeObjects ELAPSED TIME: " + new Long(System.currentTimeMillis() - startTime).toString());
            }

            else if (message instanceof UpdateObjectsRequestType)
            {
                System.out.println("updateObjects ...");
                rr = lcm.updateObjects(context);
                System.out.println("updateObjects ELAPSED TIME: " + new Long(System.currentTimeMillis() - startTime).toString());
            }
            else if (message instanceof RelocateObjectsRequestType)
            {
                System.out.println("relocateObjects ...");
                rr = lcm.relocateObjects(context);
                System.out.println("relocateObjects ELAPSED TIME: " + new Long(System.currentTimeMillis() - startTime).toString());
            }
            else
            {
                throw new XdsInternalException(ServerResourceBundle.getInstance().
                    getString("message.unknownRequest") +
                        message.getClass().getName());
            }
        }
        catch (Exception e)
        {
            // FIXME (BHT): Can not just eat exceptions.
            e.printStackTrace();
            System.out.println("   OmarRegistry.process()     " + e.getMessage());
        }
        OMElement response = null;
        try
        {
            StringWriter sw = new StringWriter();
            javax.xml.bind.Marshaller marshaller = bu.rsFac.createMarshaller();
            marshaller.setProperty(
                javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            marshaller.marshal(rr, sw);

            //Now get the RegistryResponse as a String
            String respStr = sw.toString();
            //convert the response to OMElement to send the response
            response = AXIOMUtil.stringToOM(respStr);
        }
        catch (Exception e)
        {
            // FIXME (BHT): Do not eat exceptions.
            e.printStackTrace();
        }
        return response;
    }
}
