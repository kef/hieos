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

import com.vangent.hieos.hl7v3util.model.subject.CodedValue;
import com.vangent.hieos.services.xds.bridge.mapper.ContentParserConfig;
import com.vangent.hieos.services.xds.bridge.mapper.DocumentTypeMapping;
import com.vangent.hieos.services.xds.bridge.utils.JUnitHelper;
import com.vangent.hieos.xutil.xconfig.XConfigActor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;

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

        String tplfile = bridgeActor.getProperty(XDSBridgeConfig.TEMPLATE_PROP);

        assertNotNull(tplfile);

        XDSBridgeConfig config = XDSBridgeConfig.parseConfigFile(bridgeActor);

        assertNotNull(config);

        assertNotNull(config.getXdsbridgeActor());
        assertNotNull(config.getDocumentTypeMappings());

        List<DocumentTypeMapping> mappings = config.getDocumentTypeMappings();

        assertEquals(1, mappings.size());

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

        Map<String, String> staticValues = parserConfig.getStaticValues();

        assertEquals("WORK", staticValues.get("HealthcareFacilityTypeCode"));
        assertEquals("N", staticValues.get("DocumentConfidentialityCode"));
        assertEquals("Not applicable",
                     staticValues.get("PracticeSettingDisplayName"));

        Map<String, String> expressions = parserConfig.getExpressionMap();

        assertEquals("/hl7:ClinicalDocument/hl7:effectiveTime/@value",
                     expressions.get("ServiceStopTime"));
        assertEquals(
            "/hl7:ClinicalDocument/hl7:author/hl7:assignedAuthor/hl7:representedOrganization/hl7:asOrganizationPartOf/hl7:wholeOrganization/hl7:name",
            expressions.get("AuthorInstitutionName"));
        assertEquals("/hl7:ClinicalDocument/hl7:title",
                     expressions.get("DocumentTitle"));

    }
}
