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
