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
package com.vangent.hieos.subjectmodel;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectAbstractEntity implements Cloneable, Serializable {

    private InternalId internalId = null;
    private Date lastUpdatedTime = new Date();

    /**
     *
     * @return
     */
    public Date getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    /**
     *
     * @param lastUpdatedTime
     */
    public void setLastUpdatedTime(Date lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    /**
     * 
     * @return
     */
    public InternalId getInternalId() {
        return internalId;
    }

    /**
     *
     * @param internalId
     */
    public void setInternalId(InternalId internalId) {
        this.internalId = internalId;
    }

    /**
     *
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        SubjectAbstractEntity copy = (SubjectAbstractEntity) super.clone();
        if (lastUpdatedTime != null) {
            copy.lastUpdatedTime = (Date) lastUpdatedTime.clone();
        }
        return copy;
    }
}
