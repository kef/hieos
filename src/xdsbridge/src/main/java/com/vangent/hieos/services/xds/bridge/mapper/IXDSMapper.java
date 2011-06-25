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

package com.vangent.hieos.services.xds.bridge.mapper;

import com.vangent.hieos.services.xds.bridge.model.Document;
import com.vangent.hieos.services.xds.bridge.model.Identifier;
import com.vangent.hieos.services.xds.bridge.model.XDSPnR;

/**
 * Interface description
 *
 *
 * @version        v1.0, 2011-06-13
 * @author         Jim Horner
 */
public interface IXDSMapper {

    
    /**
     * Method description
     *
     *
     *
     *
     * @param patientId
     * @param document
     *
     * @return
     *
     * @throws Exception
     */
    public abstract XDSPnR map(Identifier patientId, Document document)
            throws Exception;
}
