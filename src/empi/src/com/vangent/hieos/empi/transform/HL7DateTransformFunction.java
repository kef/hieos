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
package com.vangent.hieos.empi.transform;

import com.vangent.hieos.xutil.hl7.date.Hl7Date;
import java.util.Date;

/**
 *
 * @author Bernie Thuman
 */
public class HL7DateTransformFunction extends TransformFunction {

    /**
     * 
     * @param obj
     * @return
     */
    public Object transform(Object obj) {
        return Hl7Date.toHL7format((Date) obj);
    }
}
