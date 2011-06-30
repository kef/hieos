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

package com.vangent.hieos.services.xds.bridge.model;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-09
 * @author         Jim Horner
 */
public class Identifier implements Serializable {

    /** Assigning Authority responsible for the Identifier */
    private String assigningAuthorityName = null;

    /** Identifier Value */
    private String extension = null;

    /** OID responsible for the Identifier */
    private String root = null;

    /**
     * Constructs ...
     *
     */
    public Identifier() {

        super();
    }

    /**
     * This method tests that root and extension are equal.
     *
     * @param obj other object
     *
     * @return true if they are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {

        boolean result = false;

        if (obj == this) {

            result = true;

        } else if ((obj != null) && (getClass().equals(obj.getClass()))) {

            Identifier other = (Identifier) obj;

            EqualsBuilder eqbuilder = new EqualsBuilder();

            eqbuilder.append(getRoot(), other.getRoot());
            eqbuilder.append(getExtension(), other.getExtension());

            result = eqbuilder.isEquals();
        }

        return result;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getAssigningAuthorityName() {
        return assigningAuthorityName;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getRoot() {
        return root;
    }

    /**
     * This method uses the root and extension to generate a
     * hashcode. equals() override uses the same.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {

        HashCodeBuilder hb = new HashCodeBuilder(5, 51);

        hb.append(getRoot());
        hb.append(getExtension());

        return hb.toHashCode();
    }

    /**
     * Method description
     *
     *
     * @param assigningAuthority
     */
    public void setAssigningAuthorityName(String assigningAuthority) {
        this.assigningAuthorityName = assigningAuthority;
    }

    /**
     * Method description
     *
     *
     * @param extension
     */
    public void setExtension(String extension) {
        this.extension = extension;
    }

    /**
     * Method description
     *
     *
     * @param root
     */
    public void setRoot(String root) {
        this.root = root;
    }
}
