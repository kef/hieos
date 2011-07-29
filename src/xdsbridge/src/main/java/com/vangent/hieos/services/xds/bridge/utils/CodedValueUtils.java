/*
 * @(#)CodedValueUtils.java   2011-06-24
 *
 * Copyright (c) 2011
 *
 *
 *
 *
 */

package com.vangent.hieos.services.xds.bridge.utils;

import javax.xml.namespace.QName;
import com.vangent.hieos.hl7v3util.model.subject.CodedValue;
import org.apache.axiom.om.OMElement;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-24
 * @author         Jim Horner
 */
public class CodedValueUtils {

    /**
     * Method description
     *
     *
     * @param type1
     * @param type2
     *
     * @return
     */
    public static boolean equals(CodedValue cv1, CodedValue cv2) {

        boolean result = false;

        if ((cv1 != null) && (cv2 != null)) {

            EqualsBuilder eqbuilder = new EqualsBuilder();
            eqbuilder.append(cv1.getCode(), cv2.getCode());
            eqbuilder.append(cv1.getCodeSystem(), cv2.getCodeSystem());
            
            result = eqbuilder.isEquals();
        }

        return result;
    }

    /**
     * Method description
     *
     *
     * @param cvelem
     *
     * @return
     */
    public static CodedValue parseCodedValue(OMElement cvelem) {

        CodedValue result = null;

        if (cvelem != null) {

            String code = cvelem.getAttributeValue(new QName("code"));
            if (StringUtils.isNotBlank(code)) {
                
                result = new CodedValue();
                result.setCode(code);
                result.setCodeSystem(
                    cvelem.getAttributeValue(new QName("codeSystem")));
                result.setCodeSystemName(
                    cvelem.getAttributeValue(new QName("codeSystemName")));
                result.setDisplayName(
                    cvelem.getAttributeValue(new QName("displayName")));
            }
        }

        return result;

    }
}
