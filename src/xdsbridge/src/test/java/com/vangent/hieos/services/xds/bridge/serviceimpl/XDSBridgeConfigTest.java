/*
 * @(#)XDSBridgeConfigTest.java   2011-06-24
 *
 * Copyright (c) 2011
 *
 *
 *
 *
 */

package com.vangent.hieos.services.xds.bridge.serviceimpl;

import java.util.List;
import java.util.Map;
import com.vangent.hieos.subjectmodel.CodedValue;
import com.vangent.hieos.services.xds.bridge.mapper.ContentParserConfig;
import com.vangent.hieos.services.xds.bridge.mapper.DocumentTypeMapping;
import com.vangent.hieos.services.xds.bridge.support.XDSBridgeConfig;
import com.vangent.hieos.services.xds.bridge.utils.JUnitHelper;
import com.vangent.hieos.xutil.xconfig.XConfigActor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-24
 * @author         Jim Horner
 */
public class XDSBridgeConfigTest {

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void parseConfigTest() throws Exception {

        XConfigActor bridgeActor = JUnitHelper.createXDSBridgeActor();

        String cfgfile =
            bridgeActor.getProperty(XDSBridgeConfig.CONFIG_FILE_PROP);

        assertNotNull(cfgfile);

        String tplfile =
            bridgeActor.getProperty(XDSBridgeConfig.TEMPLATE_METADATA_PROP);

        assertNotNull(tplfile);

        XDSBridgeConfig config = XDSBridgeConfig.newInstance(bridgeActor);

        assertNotNull(config);

        assertNotNull(config.getXdsBridgeActor());
        assertNotNull(config.getDocumentTypeMappings());

        List<DocumentTypeMapping> mappings = config.getDocumentTypeMappings();

        assertEquals(2, mappings.size());

        CodedValue findtype = new CodedValue();

        findtype.setCode("51855-5");
        findtype.setCodeSystem("2.16.840.1.113883.6.1");

        DocumentTypeMapping mapping = config.findDocumentTypeMapping(findtype);

        assertNotNull(mapping);

        CodedValue format = mapping.getFormat();

        assertNotNull(format);
        assertEquals("urn:ihe:pcc:xds-ms:2007", format.getCode());

        CodedValue type = mapping.getType();

        assertNotNull(type);
        assertEquals("2.16.840.1.113883.6.1", type.getCodeSystem());

        ContentParserConfig parserConfig = mapping.getContentParserConfig();

        Map<String, String> namespaces = parserConfig.getNamespaces();

        assertEquals("http://ns.electronichealth.net.au/Ci/Cda/Extensions/3.0",
                     namespaces.get("ext"));
        assertEquals("urn:hl7-org:v3", namespaces.get("hl7"));

        Map<String, Map<String, String>> staticValues =
            parserConfig.getStaticValues();

        Map<String, String> hcfcodes =
            staticValues.get("HealthcareFacilityTypeCode");

        assertNotNull(hcfcodes);
        assertFalse(hcfcodes.isEmpty());

        assertEquals("WORK", hcfcodes.get("HealthcareFacilityTypeCode"));
        assertEquals("2.16.840.1.113883.5.11",
                     hcfcodes.get("HealthcareFacilityTypeCodeSystem"));
        assertEquals("work site",
                     hcfcodes.get("HealthcareFacilityTypeDisplayName"));

        Map<String, String> oneval = staticValues.get("DocumentTitle");

        assertNotNull(oneval);
        assertEquals(1, oneval.size());

        assertEquals("BooFar", oneval.get("DocumentTitle"));

        Map<String, String> expressions = parserConfig.getExpressions();

        String expression = expressions.get("ServiceStopTime");

        assertNotNull(expression);
        assertEquals("/hl7:ClinicalDocument/hl7:effectiveTime/@value",
                     expression);

        expression = expressions.get("AuthorInstitutionName");
        assertNotNull(expression);
        assertEquals(
            "/hl7:ClinicalDocument/hl7:author/hl7:assignedAuthor/hl7:representedOrganization/hl7:asOrganizationPartOf/hl7:wholeOrganization/hl7:name",
            expression);

        expression = expressions.get("DocumentTitle");
        assertNotNull(expression);
        assertEquals("/hl7:ClinicalDocument/hl7:title", expression);

        expression = expressions.get("PatientIdRoot");
        assertNotNull(expression);
        assertEquals(
            "/hl7:ClinicalDocument/hl7:recordTarget/hl7:patientRole/hl7:patient/ext:asEntityIdentifier/ext:id/@root",
            expression);

    }
}
