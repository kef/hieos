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
package com.vangent.hieos.services.sts.model;

import com.vangent.hieos.services.sts.exception.STSException;
import org.opensaml.saml2.core.Attribute;

/**
 * Abstract class representing a XACML Claim.
 *
 * @author Bernie Thuman
 */
public abstract class Claim {

    private String name;

    /**
     * 
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     * @throws STSException
     */
    abstract public Attribute getAttribute() throws STSException;

    /**
     *
     * @return
     */
    abstract public String getStringValue();
}
