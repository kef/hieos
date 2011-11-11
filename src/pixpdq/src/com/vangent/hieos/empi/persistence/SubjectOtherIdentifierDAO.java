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

import java.sql.Connection;

/**
 *
 * @author Bernie Thuman
 */
public class SubjectOtherIdentifierDAO extends SubjectIdentifierDAO {

    /**
     *
     * @param connection
     */
    public SubjectOtherIdentifierDAO(Connection connection) {
        super(connection);
    }

    /**
     *
     * @return
     */
    @Override
    public String getTableName() {
        return "subject_other_identifier";
    }
}
