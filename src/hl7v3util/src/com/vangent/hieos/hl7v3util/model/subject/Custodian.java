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
package com.vangent.hieos.hl7v3util.model.subject;

/**
 *
 * @author Bernie Thuman
 */
public class Custodian implements Cloneable {

    private boolean supportsHealthDataLocator;
    private String custodianId;

    /**
     *
     * @return
     */
    public String getCustodianId() {
        return custodianId;
    }

    /**
     * 
     * @param custodianId
     */
    public void setCustodianId(String custodianId) {
        this.custodianId = custodianId;
    }

    /**
     *
     * @return
     */
    public boolean isSupportsHealthDataLocator() {
        return supportsHealthDataLocator;
    }

    /**
     *
     * @param supportsHealthDataLocator
     */
    public void setSupportsHealthDataLocator(boolean supportsHealthDataLocator) {
        this.supportsHealthDataLocator = supportsHealthDataLocator;
    }

    /**
     *
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
