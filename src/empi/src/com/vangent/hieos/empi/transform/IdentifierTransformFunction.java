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

import com.vangent.hieos.subjectmodel.Subject;
import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import com.vangent.hieos.subjectmodel.SubjectIdentifierDomain;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bernie Thuman
 */
public class IdentifierTransformFunction extends TransformFunction {
     private static String PARAM_UNIVERSAL_ID = "universal-id";


    /**
     * 
     * @param obj
     * @return
     */
    public Object transform(Object obj) {
        String identifier = null;
        Subject subject = (Subject) obj;
        String universalId = this.getFunctionConfig().getParameter(PARAM_UNIVERSAL_ID);
        if (universalId != null)
        {
            // Find an identifier that matches the universal id.
            List<SubjectIdentifier> subjectIdentifiersCopy = new ArrayList<SubjectIdentifier>();
            subjectIdentifiersCopy.addAll(subject.getSubjectIdentifiers());
            subjectIdentifiersCopy.addAll(subject.getSubjectOtherIdentifiers());
            for (SubjectIdentifier subjectIdentifier : subjectIdentifiersCopy)
            {
                SubjectIdentifierDomain subjectIdentifierDomain = subjectIdentifier.getIdentifierDomain();
                if (subjectIdentifierDomain.getUniversalId().equals(universalId))
                {
                    // Match.
                    identifier = subjectIdentifier.getIdentifier();
                    System.out.println("IdentifierTransform found identifier = " + subjectIdentifier.getCXFormatted());
                    break;
                }
            }
        }
        return identifier;
    }
}
