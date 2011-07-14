/*
 * @(#)GetDocumentsSQRequestBuilder.java
 * Date 2011-07-06
 * Version 1.0
 * Author Jim Horner
 * Copyright (c)2011
 */

package com.vangent.hieos.services.xds.bridge.message;

import com.vangent.hieos.services.xds.bridge.support.URIConstants;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-07-06
 * @author         Vangent
 */
public class GetDocumentsSQRequestBuilder {

    /** Field description */
    public static final String GET_DOCUMENTS_UUID =
        "urn:uuid:5c4f972b-d56b-40ac-a5fc-c8ca9b40b9d4";

    /** Field description */
    private final OMFactory factory;

    /** Field description */
    private final OMNamespace queryNamespace;

    /** Field description */
    private final OMNamespace rimNamespace;

    /**
     * Constructs ...
     *
     */
    public GetDocumentsSQRequestBuilder() {

        super();

        this.factory = OMAbstractFactory.getOMFactory();
        this.queryNamespace =
            this.factory.createOMNamespace(URIConstants.QUERY_URI, "query");
        this.rimNamespace =
            this.factory.createOMNamespace(URIConstants.RIM_URI, "rim");
    }

    /**
     * Method description
     *
     *
     *
     * @param node
     * @param queryUUID
     *
     * @return
     */
    private OMElement addAdhocQuery(OMElement node, String queryUUID) {

        // /AdhocQueryRequest/AdhocQuery
        OMElement result = this.factory.createOMElement("AdhocQuery",
                               this.rimNamespace);

        result.addAttribute("id", queryUUID, null);

        node.addChild(result);

        return result;
    }

    /**
     * Method description
     *
     *
     * @param result
     * @param returnType
     */
    private void addResponseOption(OMElement result, String returnType) {

        // /AdhocQueryRequest/ResponseOption
        OMElement respOptElem = this.factory.createOMElement("ResponseOption",
                                    this.queryNamespace);

        respOptElem.addAttribute("returnComposedObjects", "true", null);
        respOptElem.addAttribute("returnType", returnType, null);

        result.addChild(respOptElem);
    }

    /**
     * Method description
     *
     *
     * @param queryElem
     * @param name
     * @param value
     *
     * @return
     */
    private OMElement addSlot(OMElement queryElem, String name, String value) {

        OMElement result = this.factory.createOMElement("Slot",
                               this.rimNamespace);

        result.addAttribute("name", name, null);

        OMElement valueListElem = this.factory.createOMElement("ValueList",
                                      this.rimNamespace);
        OMElement valueElem = this.factory.createOMElement("Value",
                                  this.rimNamespace);

        valueElem.setText(value);

        valueListElem.addChild(valueElem);
        result.addChild(valueListElem);
        queryElem.addChild(result);

        return result;
    }

    /**
     * Method description
     *
     *
     *
     *
     * @param uniqueId
     * @return
     */
    public GetDocumentsSQRequestMessage buildMessage(String uniqueId) {

        OMElement result = this.factory.createOMElement("AdhocQueryRequest",
                               this.queryNamespace);

        // /AdhocQueryRequest/ResponseOption
        addResponseOption(result, "ObjectRef");

        // /AdhocQueryRequest/AdhocQuery
        OMElement queryElem = addAdhocQuery(result, GET_DOCUMENTS_UUID);

        addSlot(queryElem, "$XDSDocumentEntryUniqueId",
                String.format("('%s')", uniqueId));

        return new GetDocumentsSQRequestMessage(result);
    }
}
