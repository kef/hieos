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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bernie Thuman
 */
public class TelecomAddress extends SubjectAbstractEntity implements Cloneable {

    private String use;
    private String value;

    /**
     *
     * @return
     */
    public String getUse() {
        return use;
    }

    /**
     *
     * @param use
     */
    public void setUse(String use) {
        this.use = use;
    }

    /**
     *
     * @return
     */
    public String getValue() {
        return value;
    }

    /**
     *
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
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

    /**
     *
     * @param listToClone
     * @return
     * @throws CloneNotSupportedException
     */
    public static List<TelecomAddress> clone(List<TelecomAddress> listToClone) throws CloneNotSupportedException {
        List<TelecomAddress> copy = null;
        if (listToClone != null) {
            copy = new ArrayList<TelecomAddress>();
            for (TelecomAddress elementToClone : listToClone) {
                TelecomAddress clonedElement = (TelecomAddress) elementToClone.clone();
                copy.add(clonedElement);
            }
        }
        return copy;
    }
}
