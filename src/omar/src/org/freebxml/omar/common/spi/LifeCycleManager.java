/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/spi/LifeCycleManager.java,v 1.2 2006/03/02 15:21:31 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.common.spi;

import javax.xml.registry.RegistryException;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponse;

public interface LifeCycleManager {
    
    /**
     * Submits one or more RegistryObjects and one or more repository items.
     *
     * @param idToRepositoryItemMap is a HashMap with key that is id of a RegistryObject and value that is a repository item in form of a javax.activation.DataHandler instance.
     */
    public RegistryResponse submitObjects(RequestContext context)  throws RegistryException;

    /** Approves one or more previously submitted objects */
    public RegistryResponse approveObjects(RequestContext context) throws RegistryException;

    /** Sets the status of specified objects. This is an extension request that will be adde to ebRR 3.1?? */
    public RegistryResponse setStatusOnObjects(RequestContext context) throws RegistryException;
    
    /** Deprecates one or more previously submitted objects */
    public RegistryResponse deprecateObjects(RequestContext context) throws RegistryException;

    /** Deprecates one or more previously submitted objects */
    public RegistryResponse unDeprecateObjects(RequestContext context) throws RegistryException;
    
    public RegistryResponse updateObjects(RequestContext context)
        throws RegistryException;

    /** Removes one or more previously submitted objects from the registry */
    public RegistryResponse removeObjects(RequestContext context) throws RegistryException;
    
    /** Approves one or more previously submitted objects */
    /* HIEOS (REMOVED):
    public RegistryResponse relocateObjects(RequestContext context) throws RegistryException;
    */
    
    /** Sends an impl specific protocol extension request. */
    /* HIEOS (REMOVED):
     public RegistryResponseHolder extensionRequest(RequestContext context) throws RegistryException;
    */
}
