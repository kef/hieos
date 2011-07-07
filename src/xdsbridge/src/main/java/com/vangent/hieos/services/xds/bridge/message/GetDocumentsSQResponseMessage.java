/*
 * @(#)GetDocumentsSQResponseMessage.java
 * Date 2011-07-06
 * Version 1.0
 * Author Jim Horner
 * Copyright (c)2011
 */


package com.vangent.hieos.services.xds.bridge.message;

import org.apache.axiom.om.OMElement;


/**
 * Class description
 *
 *
 * @version        v1.0, 2011-07-06
 * @author         Jim Horner    
 */
public class GetDocumentsSQResponseMessage extends AbstractXdsBridgeMessage {

    /**
     * Constructs ...
     *
     *
     * @param elem
     */
    public GetDocumentsSQResponseMessage(OMElement elem) {
        super(elem, "AdhocQueryResponse");
    }
}
