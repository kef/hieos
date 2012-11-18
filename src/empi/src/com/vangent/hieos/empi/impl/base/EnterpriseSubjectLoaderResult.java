/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2012 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.empi.impl.base;

import com.vangent.hieos.subjectmodel.Subject;

/**
 *
 * @author Bernie Thuman
 */
public class EnterpriseSubjectLoaderResult {

    private Subject enterpriseSubject;
    private boolean alreadyExists;

    /**
     *
     * @return
     */
    public boolean isAlreadyExists() {
        return alreadyExists;
    }

    /**
     * 
     * @param alreadyExists
     */
    public void setAlreadyExists(boolean alreadyExists) {
        this.alreadyExists = alreadyExists;
    }

    /**
     *
     * @return
     */
    public Subject getEnterpriseSubject() {
        return enterpriseSubject;
    }

    /**
     *
     * @param enterpriseSubject
     */
    public void setEnterpriseSubject(Subject enterpriseSubject) {
        this.enterpriseSubject = enterpriseSubject;
    }
}
