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

package com.vangent.hieos.services.sts.transactions;

import com.vangent.hieos.services.sts.model.STSRequestData;
import com.vangent.hieos.services.sts.config.STSConfig;
import com.vangent.hieos.services.sts.exception.STSException;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMElement;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilderFactory;

/**
 *
 * @author Bernie Thuman
 */
public abstract class SAML2TokenHandler {
    private STSConfig stsConfig;

    /**
     *
     */
    private SAML2TokenHandler()
    {
        // Do nothing -- must use constructor below.
    }

    /**
     *
     * @param stsConfig
     */
    protected SAML2TokenHandler(STSConfig stsConfig)
    {
        this.stsConfig = stsConfig;
    }

    /**
     * 
     * @return
     */
    protected STSConfig getSTSConfig()
    {
        return this.stsConfig;
    }

    abstract protected OMElement handle(STSRequestData requestData) throws STSException;

     /**
     *
     * @param qname
     * @return
     */
    protected XMLObject createSamlObject(QName qname) throws STSException {
        //return Configuration.getBuilderFactory().getBuilder(qname).buildObject(qname);
        return SAML2TokenHandler.getXMLObjectBuilderFactory().getBuilder(qname).buildObject(qname);
    }

    private static XMLObjectBuilderFactory builderFactory;

    /**
     * 
     * @return
     * @throws ConfigurationException
     */
    protected synchronized static XMLObjectBuilderFactory getXMLObjectBuilderFactory() throws STSException {

        if (builderFactory == null) {
            try {
                // OpenSAML 2.3
                DefaultBootstrap.bootstrap();
            } catch (ConfigurationException ex) {
               throw new STSException("Failure initializing OpenSAML: " + ex.getMessage());
            }
            builderFactory = Configuration.getBuilderFactory();
        }

        return builderFactory;
    }

}
