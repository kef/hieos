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

import com.vangent.hieos.hl7v3util.model.subject.CodedValue;

import org.apache.axiom.om.OMElement;

import javax.xml.namespace.QName;

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
    public static boolean equals(CodedValue type1, CodedValue type2) {

        boolean result = false;

        if ((type1 != null) && (type2 != null)) {

            if (StringUtils.equals(type1.getCode(), type2.getCode())
                    && StringUtils.equals(type1.getCodeSystem(),
                                          type2.getCodeSystem())) {

                result = true;
            }

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

            result = new CodedValue();
            result.setCode(cvelem.getAttributeValue(new QName("code")));
            result.setCodeSystem(
                cvelem.getAttributeValue(new QName("codeSystem")));
            result.setCodeSystemName(
                cvelem.getAttributeValue(new QName("codeSystemName")));
            result.setDisplayName(
                cvelem.getAttributeValue(new QName("displayName")));
        }

        return result;

    }
}
