/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2008-2009 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.xutil.metadata.validation;

import com.vangent.hieos.xutil.metadata.structure.Structure;
import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.exception.MetadataValidationException;
import com.vangent.hieos.xutil.exception.XdsException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import com.vangent.hieos.xutil.response.RegistryErrorList;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

/**
 *
 * @author NIST (Adapted by Bernie Thuman)
 */
public class Validator {

    private RegistryErrorList rel;
    private Metadata m;
    private Structure s;
    private Attribute a;
    private CodeValidation cv;
    private PatientId pid;
    private UniqueId uid;
    private XLogMessage logMessage;

    /**
     * 
     * @param m
     * @param rel
     * @param isSubmit
     * @param logMessage
     * @throws XdsException
     */
    public Validator(Metadata m, RegistryErrorList rel, boolean isSubmit, XLogMessage logMessage) throws XdsException {
        this.rel = rel;
        this.m = m;
        this.logMessage = logMessage;
        // Prepare all validation structures:
        s = new Structure(m, isSubmit, rel, logMessage);
        a = new Attribute(m, rel, logMessage);
        try {
            cv = new CodeValidation(m, rel, logMessage);
        } catch (XdsInternalException e) {
            rel.add_error(MetadataSupport.XDSRegistryError, e.getMessage(), this.getClass().getName(), null);
            throw new XdsInternalException(e.getLocalizedMessage(), e);
        }
        //assigning_authorities = cv.getAssigningAuthorities();
        pid = new PatientId(m, rel);
        uid = new UniqueId(m, isSubmit, rel);
    }

    /**
     *
     * @throws XdsInternalException
     * @throws MetadataValidationException
     * @throws XdsException
     */
    public void run() throws XdsInternalException, MetadataValidationException, XdsException {
        try {
            // Run series of validations (Structure/Attribute/Codes).
            s.run();
            a.run();
            cv.run();
        } catch (XdsInternalException e) {
            rel.add_error(MetadataSupport.XDSRegistryError, e.getMessage(), this.getClass().getName(), null);
        } catch (MetadataException e) {
            rel.add_error(MetadataSupport.XDSRegistryError, e.getMessage(), this.getClass().getName(), null);
        }
        pid.run();  // PID validation.
        /*for (OMElement ele : m.getRegistryPackages()) {
            validateInternalClassifications(ele);
        }
        for (OMElement ele : m.getExtrinsicObjects()) {
            validateInternalClassifications(ele);
        }*/
        uid.run();  // UID validations.
        rel.getRegistryErrorList(); // forces output of validation report
    }
}
