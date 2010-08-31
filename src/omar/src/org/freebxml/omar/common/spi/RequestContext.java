/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/spi/RequestContext.java,v 1.2 2005/11/28 20:17:33 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.common.spi;

import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;

/* HIOES (CHANGE) - Removed stack. */

/**
 * The interface that carries Request specific context information.
 * Implemented differently by client and server to serve their 
 * specific needs for providing state and context during the 
 * processing of a request.
 * 
 * @author Farrukh Najmi
 * @author Diego Ballve
 * @author Bernie Thuman (rewrote for HIEOS).
 *
 */
public interface RequestContext {
    /**
     * Gets the current RegistryRequest being processed by the context.
     *
     * @return the current RegistryRequestType.
     */
    public RegistryRequestType getCurrentRegistryRequest();
}
