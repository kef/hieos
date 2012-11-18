package com.vangent.hieos.services.xds.bridge.utils;

import com.vangent.hieos.subjectmodel.SubjectIdentifier;
import com.vangent.hieos.subjectmodel.SubjectIdentifierDomain;
import org.apache.commons.lang.StringUtils;

public class SubjectIdentifierUtils {
    
    public static SubjectIdentifier createSubjectIdentifier(
            String root, String extension) {
        
        SubjectIdentifier result = new SubjectIdentifier();
        SubjectIdentifierDomain domain = new SubjectIdentifierDomain();
        result.setIdentifierDomain(domain);
        
        
        if (StringUtils.isBlank(extension)) {

            // "Case 1 - root only"
            // extension is everything before last period
            result.setIdentifier(StringUtils.substringAfterLast(root, "."));
            domain.setUniversalId(StringUtils.substringBeforeLast(root, "."));
            
        } else {
            
            result.setIdentifier(extension);
            domain.setUniversalId(root);
        }
        
        domain.setUniversalIdType("ISO");

        return result;
    }
}
