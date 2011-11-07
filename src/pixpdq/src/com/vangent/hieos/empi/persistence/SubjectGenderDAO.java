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
package com.vangent.hieos.empi.persistence;

import com.vangent.hieos.hl7v3util.model.subject.SubjectGender;
import com.vangent.hieos.services.pixpdq.empi.exception.EMPIException;
import java.sql.Connection;
import org.apache.log4j.Logger;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectGenderDAO extends CodeDAO {

    private static final Logger logger = Logger.getLogger(SubjectGenderDAO.class);

    /**
     *
     * @param connection
     */
    public SubjectGenderDAO(Connection connection) {
        super(connection);
    }

    /**
     * 
     * @param id
     * @return
     * @throws EMPIException
     */
    public SubjectGender load(int id) throws EMPIException {
        SubjectGender subjectGender = new SubjectGender();
        this.load(id, subjectGender);
        return subjectGender;
    }

    /**
     * 
     * @return
     */
    @Override
    public String getTableName() {
        return "gendercode";
    }
}
