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
package com.vangent.hieos.policyutil.client;

import com.vangent.hieos.policyutil.util.PolicyConstants;
import com.vangent.hieos.xutil.soap.Soap;
import com.vangent.hieos.xutil.xml.XMLParser;
import java.io.StringWriter;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.apache.axiom.om.OMElement;

import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.factories.SAMLAssertionFactory;
import org.picketlink.identity.federation.core.saml.v2.util.SOAPSAMLXACMLUtil;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.org.xmlsoap.schemas.soap.envelope.Fault;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.profiles.xacml.assertion.XACMLAuthzDecisionStatementType;
import org.picketlink.identity.federation.saml.v2.profiles.xacml.protocol.XACMLAuthzDecisionQueryType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.jboss.security.xacml.core.model.context.RequestType;
import org.jboss.security.xacml.core.model.context.ResultType;

/**
 *
 * @author Bernie Thuman
 */
public class PDPSOAPClient {

    /**
     */
    public ResultType send(String endpointURL, boolean soap12, String issuer, RequestType xacmlRequest) throws ProcessingException {
        try {
            XACMLAuthzDecisionQueryType queryType = SOAPSAMLXACMLUtil.createXACMLAuthzDecisionQueryType();
            queryType.setRequest(xacmlRequest);

            //Create Issue Instant
            queryType.setIssueInstant(XMLTimeUtil.getIssueInstant());

            //Create Issuer
            NameIDType nameIDType = SAMLAssertionFactory.getObjectFactory().createNameIDType();
            nameIDType.setValue(issuer);
            queryType.setIssuer(nameIDType);

            JAXBElement<?> jaxbQueryType = SOAPSAMLXACMLUtil.getJAXB(queryType);

            // Convert JAXBElement to OMElement.
            OMElement pdpRequest = this.convertJAXBElementToOMElement(jaxbQueryType);

            // Issue SOAP call to PDP.
            Soap soap = new Soap();
            OMElement pdpResponse;
            try {
                pdpResponse = soap.soapCall(
                        pdpRequest,
                        endpointURL,
                        false /* MTOM */,
                        soap12 /* Addressing - Only if SOAP 1.2 */,
                        soap12 /* SOAP 1.2 */,
                        PolicyConstants.PDP_SOAP_ACTION, null);
            } catch (Exception ex) {
                throw new ProcessingException(ex.getMessage());
            }
            if (pdpResponse == null) {
                throw new ProcessingException("No SOAP Response!");
            }

            // Convert OMElement to JAXBElement.
            Unmarshaller unmarshaller = SOAPSAMLXACMLUtil.getUnmarshaller();
            JAXBElement<?> samlResponse = (JAXBElement<?>) unmarshaller.unmarshal(pdpResponse.getXMLStreamReader());
            Object response = samlResponse.getValue();
            if (response instanceof Fault) {
                Fault fault = (Fault) response;
                throw new ProcessingException("Fault: " + fault.getFaultstring());
                //return new Result(null,fault);
            }

            // Get Result.
            ResponseType responseType = (ResponseType) response;
            AssertionType at = (AssertionType) responseType.getAssertionOrEncryptedAssertion().get(0);
            XACMLAuthzDecisionStatementType xst = (XACMLAuthzDecisionStatementType) at.getStatementOrAuthnStatementOrAuthzDecisionStatement().get(0);
            ResultType rt = xst.getResponse().getResult().get(0);
            return rt;
        } catch (JAXBException e) {
            throw new ProcessingException(e);
        } catch (ConfigurationException e) {
            throw new ProcessingException(e);
        }
    }

    /**
     *
     * @param jaxbElement
     * @return
     * @throws ProcessingException
     */
    private OMElement convertJAXBElementToOMElement(JAXBElement<?> jaxbElement) throws ProcessingException {
        OMElement response = null;
        try {
            StringWriter sw = new StringWriter();
            Marshaller marshaller = SOAPSAMLXACMLUtil.getMarshaller();
            marshaller.marshal(jaxbElement, sw);
            String xml = sw.toString();
            response = XMLParser.stringToOM(xml);
        } catch (Exception e) {
            throw new ProcessingException(e.getMessage());
        }
        return response;
    }
}
