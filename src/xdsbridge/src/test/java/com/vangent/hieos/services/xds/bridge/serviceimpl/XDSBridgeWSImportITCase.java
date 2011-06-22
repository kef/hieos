/*
 * @(#)XDSBridgeWSImportITCase.java   2011-06-18
 *
 * Copyright (c) 2011
 *
 *
 *
 *
 */

package com.vangent.hieos.services.xds.bridge.serviceimpl;

import com.vangent.hieos.schemas.xdsbridge.CodeType;
import com.vangent.hieos.schemas.xdsbridge.DocumentType;
import com.vangent.hieos.schemas.xdsbridge.IdType;
import com.vangent.hieos.schemas.xdsbridge.ObjectFactory;
import com.vangent.hieos.schemas.xdsbridge.SubmitDocumentRequest;
import com.vangent.hieos.schemas.xdsbridge.XDSBridgePortType;
import com.vangent.hieos.schemas.xdsbridge.Xdsbridge;
import com.vangent.hieos.xutil.iosupport.Io;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import java.net.URL;

import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.MTOMFeature;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-18
 * @author         Jim Horner
 */
public class XDSBridgeWSImportITCase {

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws Exception
     */
    private byte[] retrieveDocument() throws Exception {

        ClassLoader cl = getClass().getClassLoader();
        InputStream xmlis =
            cl.getResourceAsStream("documents/exampleCDA-SHS-V1_0.xml");

        assertNotNull(xmlis);

        return Io.getBytesFromInputStream(xmlis);
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void submitDocumentRequestTest() throws Exception {

        URL url =
            new URL("http://localhost:9090/axis2/services/xdsbridge?wsdl");

        QName qname = new QName("http://schemas.hieos.vangent.com/xdsbridge",
                                "xdsbridge");
        Xdsbridge client = new Xdsbridge(url, qname);

        XDSBridgePortType port =
            client.getXDSBridgeEndpoint(new AddressingFeature(true, true),
                                        new MTOMFeature(true));

        BindingProvider bprovider = (BindingProvider) port;
        Map<String, Object> context = bprovider.getRequestContext();

        context.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url.toString());

        context.put("com.sun.xml.ws.request.timeout", 60);
        context.put("com.sun.xml.ws.connect.timeout", 30);

        ObjectFactory factory = new ObjectFactory();
        SubmitDocumentRequest sdr = factory.createSubmitDocumentRequest();

        IdType patientId = factory.createIdType();

        patientId.setRoot("1.2.3.4.89008234");
        sdr.setPatientId(patientId);
        
        DocumentType document = factory.createDocumentType();
        CodeType type = factory.createCodeType();
        type.setCode("C32");
        type.setCodeSystem("1.2.3.4.6789.992");
        document.setType(type);
        document.setContent(retrieveDocument());

        sdr.setDocuments(factory.createDocumentsType());
        sdr.getDocuments().getDocument().add(document);

        port.submitDocumentRequest(sdr);

    }
}
