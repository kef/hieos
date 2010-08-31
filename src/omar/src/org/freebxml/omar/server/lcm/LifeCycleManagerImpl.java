/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/lcm/LifeCycleManagerImpl.java,v 1.78 2007/11/20 13:32:42 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.lcm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBException;
import javax.xml.registry.InvalidRequestException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.CanonicalSchemes;
//import org.freebxml.omar.common.CommonProperties;
import org.freebxml.omar.common.spi.LifeCycleManager;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.common.RegistryResponseHolder;
/* HIEOS/BHT - Removed:
import org.freebxml.omar.common.RepositoryItem;
 */
import org.freebxml.omar.common.UUIDFactory;
import org.freebxml.omar.common.exceptions.ObjectsNotFoundException;
import org.freebxml.omar.common.exceptions.QuotaExceededException;
import org.freebxml.omar.common.exceptions.UnauthorizedRequestException;
import org.freebxml.omar.common.spi.RequestContext;
/* HIEOS/BHT (REMOVED):
import org.freebxml.omar.server.cms.CMSManager;
import org.freebxml.omar.server.cms.CMSManagerImpl;
 */
import org.freebxml.omar.server.common.RegistryProperties;
import org.freebxml.omar.server.common.ServerRequestContext;
/* HIEOS/BHT (REMOVED):
import org.freebxml.omar.server.lcm.quota.QuotaServiceImpl;
 */
import org.freebxml.omar.server.lcm.relocation.RelocationProcessor;
/* HIEOS/BHT - Removed:
import org.freebxml.omar.server.repository.RepositoryItemKey;
import org.freebxml.omar.server.repository.RepositoryManager;
import org.freebxml.omar.server.repository.RepositoryManagerFactory;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
 */
/* HIEOS/BHT (REMOVED):
import org.freebxml.omar.server.security.authentication.CertificateAuthority;
import org.freebxml.omar.server.security.authorization.AuthorizationResult;
import org.freebxml.omar.server.security.authorization.AuthorizationServiceImpl;
 */
import org.freebxml.omar.server.util.ServerResourceBundle;
import org.oasis.ebxml.registry.bindings.lcm.ApproveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.DeprecateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.RelocateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SetStatusOnObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UndeprecateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UpdateObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.Association;
import org.oasis.ebxml.registry.bindings.rim.AssociationType1;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryPackage;
import org.oasis.ebxml.registry.bindings.rim.RegistryPackageType;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponse;

/*
 * HIEOS (CHANGE): Removed use of repository, CMS, quotas and authentication.
 */
/**
 * Implementation of the LifeCycleManager interface
 * @see
 * @author Farrukh S. Najmi
 * @author Adrian Chong
 *
 * TODO: Replace exception-handling code with calls to
 * util.createRegistryResponseFromThrowable() where appropriate.
 */
public class LifeCycleManagerImpl implements LifeCycleManager {

    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */

    /*# private LifeCycleManagerImpl _objectManagerImpl; */
    private static LifeCycleManagerImpl instance = null;
    private static BindingUtility bu = BindingUtility.getInstance();
    /**
     *
     * @associates <{org.freebxml.omar.server.persistence.PersistenceManagerImpl}>
     */
    org.freebxml.omar.server.persistence.PersistenceManager pm = org.freebxml.omar.server.persistence.PersistenceManagerFactory.getInstance().getPersistenceManager();
    /**
     *
     * @associates <{org.freebxml.omar.common.QueryManagerImpl}>
     */
    org.freebxml.omar.common.spi.QueryManager qm = org.freebxml.omar.common.spi.QueryManagerFactory.getInstance().getQueryManager();
    org.freebxml.omar.server.common.Utility util = org.freebxml.omar.server.common.Utility.getInstance();
    UUIDFactory uf = UUIDFactory.getInstance();
    private static final Log log = LogFactory.getLog(LifeCycleManagerImpl.class);

    /**
     *
     */
    protected LifeCycleManagerImpl() {
    }

    /**
     *
     * @return
     */
    public synchronized static LifeCycleManagerImpl getInstance() {
        if (instance == null) {
            instance = new LifeCycleManagerImpl();
        }
        return instance;
    }

    /**
     * Submits one or more RegistryObjects and one or more repository items.
     * <br>
     * <br>
     * Note: One more special feature that is not in the RS spec. version 2.
     * The SubmitObjectsRequest allows updating objects.If a object of a particular
     * id already exist, it is updated instead of trying to be inserted.
     * @param idToRepositoryItemMap is a HashMap with key that is id of a RegistryObject and value that is a RepositoryItem instance.
     */
    public RegistryResponse submitObjects(RequestContext context) throws RegistryException {
        context = ServerRequestContext.convert(context);
        SubmitObjectsRequest req = (SubmitObjectsRequest) context.getCurrentRegistryRequest();

        RegistryResponse resp = null;
        try {
            RegistryObjectListType objs = req.getRegistryObjectList();

            // insert member objects of RegistryPackages
            int objsSize = objs.getIdentifiable().size();
            for (int i = 0; i < objsSize; i++) {
                Object identObj = objs.getIdentifiable().get(i);

                if (identObj instanceof RegistryPackage) {
                    insertPackageMembers(objs, (RegistryPackage) identObj);
                }
            }

            //Split Identifiables by RegistryObjects and ObjectRefs
            bu.getObjectRefsAndRegistryObjects(objs, ((ServerRequestContext) context).getTopLevelObjectsMap(), ((ServerRequestContext) context).getObjectRefsMap());

            processConfirmationAssociations(((ServerRequestContext) context));

            ((ServerRequestContext) context).checkObjects();

            /*
             * For RegistryObjects, the DAO will take care which objects already
             * exist and update them instead
             */
            log.trace(ServerResourceBundle.getInstance().getString("message.CallingPminsertAt", new Object[]{new Long(System.currentTimeMillis())}));
            ArrayList list = new ArrayList();
            list.addAll(((ServerRequestContext) context).getTopLevelObjectsMap().values());
            pm.insert(((ServerRequestContext) context), list);

            log.trace(ServerResourceBundle.getInstance().getString("message.DoneCallingPminsertAt", new Object[]{new Long(System.currentTimeMillis())}));

            resp = bu.rsFac.createRegistryResponse();
            resp.setStatus(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);

            if (((ServerRequestContext) context).getErrorList().getRegistryError().size() > 0) {
                // warning exists
                resp.setRegistryErrorList(((ServerRequestContext) context).getErrorList());
            }
        } catch (RegistryException e) {
            ((ServerRequestContext) context).rollback();
            throw e;
        } catch (IllegalStateException e) {
            //?? This is a JAXR spec bug that we do not send an UnauthorizedRequestException
            ((ServerRequestContext) context).rollback();
            throw e;
        } catch (Exception e) {
            ((ServerRequestContext) context).rollback();
            throw new RegistryException(e);
        }
        ((ServerRequestContext) context).commit();
        return resp;
    }

    /**
     * Iterates through the members of the RegistryPackage <code>regPkg</code>,
     * creates and adds a 'HasMember' association for each member 
     * of the RegistryPackages to the RegistryObjectList <code>regObjs</code> together with the member.
     * 
     * @param regObjs RegistryObjectList to append RegistryPackage members and their associations.
     * @param regPkg Package to get members from.
     * @throws javax.xml.bind.JAXBException 
     */
    private void insertPackageMembers(RegistryObjectListType regObjs, RegistryPackageType regPkg) throws JAXBException {
        if (regPkg.getRegistryObjectList() != null && regPkg.getRegistryObjectList().getIdentifiable().size() > 0) {

            for (int j = 0; j < regPkg.getRegistryObjectList().getIdentifiable().size(); j++) {
                Object obj = regPkg.getRegistryObjectList().getIdentifiable().get(j);

                if (obj instanceof RegistryPackageType) {
                    insertPackageMembers(regObjs, (RegistryPackageType) obj);
                }

                if (obj instanceof RegistryObjectType) {
                    RegistryObjectType ro = (RegistryObjectType) obj;
                    String assId = org.freebxml.omar.common.Utility.getInstance().createId();
                    Association asso = BindingUtility.getInstance().rimFac.createAssociation();
                    asso.setId(assId);
                    asso.setLid(assId);
                    asso.setAssociationType(CanonicalSchemes.CANONICAL_ASSOCIATION_TYPE_ID_HasMember);
                    asso.setSourceObject(regPkg.getId());
                    asso.setTargetObject(ro.getId());
                    regObjs.getIdentifiable().add(ro);
                    regObjs.getIdentifiable().add(asso);
                }
            }
            regPkg.getRegistryObjectList().getIdentifiable().clear();
        }
    }

    /**
     * Processes Associations looking for Associations that already exist and are being
     * submitted by source or target owner and are identical in state to existing
     * Association in registry.    
     *
     * ebXML Registry 3.0 hasber discarded association confirmation in favour of 
     * Role Based access control. However, freebXML Registry supports it in an 
     * impl specific manner as this is required by JAXR 1.0 API.
     * This SHOULD be removed once JAXR 2.0 no longer requires it for ebXML Registry
     * in future.
     *
     * The processing updates the Association to add a special Impl specific slot to 
     * remember the confirmation state change.
     * 
     * TODO: Need to do unconfirm when src or target owner removes an Association they had
     * previously confirmed.
     */
    private void processConfirmationAssociations(ServerRequestContext context) throws RegistryException {

        try {
            //Make a copy to avoid ConcurrentModificationException
            ArrayList topLevelObjects = new ArrayList(((ServerRequestContext) context).getTopLevelObjectsMap().values());
            Iterator iter = topLevelObjects.iterator();
            while (iter.hasNext()) {
                Object obj = iter.next();
                if (obj instanceof AssociationType1) {
                    AssociationType1 ass = (AssociationType1) obj;
                    HashMap slotsMap = bu.getSlotsFromRegistryObject(ass);
                    String beingConfirmed = (String) slotsMap.remove(bu.IMPL_SLOT_ASSOCIATION_IS_BEING_CONFIRMED);
                    if ((beingConfirmed != null) && (beingConfirmed.equalsIgnoreCase("true"))) {
                        //Need to set slotMap again
                        ass.getSlot().clear();
                        bu.addSlotsToRegistryObject(ass, slotsMap);

                        ((ServerRequestContext) context).getConfirmationAssociations().put(ass.getId(), ass);
                    }
                }
            }
        } catch (javax.xml.bind.JAXBException e) {
            throw new RegistryException(e);
        }
    }

    /** Approves one or more previously submitted objects */
    public RegistryResponse approveObjects(RequestContext context) throws RegistryException {
        context = ServerRequestContext.convert(context);
        ApproveObjectsRequest req = (ApproveObjectsRequest) context.getCurrentRegistryRequest();
        UserType user = context.getUser();
        RegistryResponse resp = null;
        try {
            context = new ServerRequestContext("LifeCycleManagerImpl.approveObjects", req);
            ((ServerRequestContext) context).setUser(user);

            List idList = new java.util.ArrayList();
            //Add explicitly specified oref params
            List orefs = bu.getObjectRefsFromObjectRefList(req.getObjectRefList());

            //Append those orefs specified via ad hoc query param
            orefs.addAll(((ServerRequestContext) context).getObjectsRefsFromQueryResults(req.getAdhocQuery()));

            Iterator orefsIter = orefs.iterator();

            while (orefsIter.hasNext()) {
                ObjectRefType oref = (ObjectRefType) orefsIter.next();
                idList.add(oref.getId());
            }

            pm.updateStatus(((ServerRequestContext) context), idList,
                    bu.CANONICAL_STATUS_TYPE_ID_Approved);
            resp = bu.rsFac.createRegistryResponse();
            resp.setStatus(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);

            if (((ServerRequestContext) context).getErrorList().getRegistryError().size() > 0) {
                // warning exists
                resp.setRegistryErrorList(((ServerRequestContext) context).getErrorList());
            }
        } catch (RegistryException e) {
            ((ServerRequestContext) context).rollback();
            throw e;
        } catch (Exception e) {
            ((ServerRequestContext) context).rollback();
            throw new RegistryException(e);
        }

        ((ServerRequestContext) context).commit();
        return resp;
    }

    /**
     *
     * @param context
     * @return
     * @throws RegistryException
     */
    public RegistryResponse updateObjects(RequestContext context) throws RegistryException {
        context = ServerRequestContext.convert(context);
        RegistryResponse resp = null;
        UpdateObjectsRequest req = (UpdateObjectsRequest) ((ServerRequestContext) context).getCurrentRegistryRequest();
        try {
            RegistryObjectListType objs = req.getRegistryObjectList();

            //Split Identifiables by RegistryObjects and ObjectRefs
            bu.getObjectRefsAndRegistryObjects(objs, ((ServerRequestContext) context).getTopLevelObjectsMap(), ((ServerRequestContext) context).getObjectRefsMap());

            ((ServerRequestContext) context).checkObjects();

            ArrayList list = new ArrayList();
            list.addAll(((ServerRequestContext) context).getTopLevelObjectsMap().values());
            pm.update(((ServerRequestContext) context), list);

            resp = bu.rsFac.createRegistryResponse();
            resp.setStatus(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);

            if (((ServerRequestContext) context).getErrorList().getRegistryError().size() > 0) {
                // warning exists
                resp.setRegistryErrorList(((ServerRequestContext) context).getErrorList());
            }
        } catch (RegistryException e) {
            ((ServerRequestContext) context).rollback();
            throw e;
        } catch (IllegalStateException e) {
            //?? This is a JAXR spec bug that we do not send an UnauthorizedRequestException
            ((ServerRequestContext) context).rollback();
            throw e;
        } catch (Exception e) {
            ((ServerRequestContext) context).rollback();
            throw new RegistryException(e);
        }

        ((ServerRequestContext) context).commit();
        return resp;
    }

    /** Sets the status of specified objects. This is an extension request that will be adde to ebRR 3.1?? */
    public RegistryResponse setStatusOnObjects(RequestContext context) throws RegistryException {
        context = ServerRequestContext.convert(context);
        RegistryResponse resp = null;
        SetStatusOnObjectsRequest req = (SetStatusOnObjectsRequest) ((ServerRequestContext) context).getCurrentRegistryRequest();
        try {
            List idList = new java.util.ArrayList();
            //Add explicitly specified oref params
            List orefs = bu.getObjectRefsFromObjectRefList(req.getObjectRefList());

            //Append those orefs specified via ad hoc query param
            orefs.addAll(((ServerRequestContext) context).getObjectsRefsFromQueryResults(req.getAdhocQuery()));

            Iterator orefsIter = orefs.iterator();

            while (orefsIter.hasNext()) {
                ObjectRefType oref = (ObjectRefType) orefsIter.next();
                idList.add(oref.getId());
            }

            String statusId = req.getStatus();
            pm.updateStatus(((ServerRequestContext) context), idList, statusId);
            resp = bu.rsFac.createRegistryResponse();
            resp.setStatus(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);

            if (((ServerRequestContext) context).getErrorList().getRegistryError().size() > 0) {
                // warning exists
                resp.setRegistryErrorList(((ServerRequestContext) context).getErrorList());
            }
        } catch (RegistryException e) {
            ((ServerRequestContext) context).rollback();
            throw e;
        } catch (Exception e) {
            ((ServerRequestContext) context).rollback();
            throw new RegistryException(e);
        }

        ((ServerRequestContext) context).commit();
        return resp;
    }

    /** Deprecates one or more previously submitted objects */
    public RegistryResponse deprecateObjects(RequestContext context) throws RegistryException {
        context = ServerRequestContext.convert(context);
        RegistryResponse resp = null;
        DeprecateObjectsRequest req = (DeprecateObjectsRequest) ((ServerRequestContext) context).getCurrentRegistryRequest();
        try {
            List idList = new java.util.ArrayList();
            //Add explicitly specified oref params
            List orefs = bu.getObjectRefsFromObjectRefList(req.getObjectRefList());

            //Append those orefs specified via ad hoc query param
            orefs.addAll(((ServerRequestContext) context).getObjectsRefsFromQueryResults(req.getAdhocQuery()));

            Iterator orefsIter = orefs.iterator();

            while (orefsIter.hasNext()) {
                ObjectRefType oref = (ObjectRefType) orefsIter.next();
                idList.add(oref.getId());
            }

            pm.updateStatus(((ServerRequestContext) context), idList,
                    bu.CANONICAL_STATUS_TYPE_ID_Deprecated);
            resp = bu.rsFac.createRegistryResponse();
            resp.setStatus(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);

            if (((ServerRequestContext) context).getErrorList().getRegistryError().size() > 0) {
                // warning exists
                resp.setRegistryErrorList(((ServerRequestContext) context).getErrorList());
            }
        } catch (RegistryException e) {
            ((ServerRequestContext) context).rollback();
            throw e;
        } catch (Exception e) {
            ((ServerRequestContext) context).rollback();
            throw new RegistryException(e);
        }

        ((ServerRequestContext) context).commit();
        return resp;
    }

    /**
     *
     * @param context
     * @return
     * @throws RegistryException
     */
    public RegistryResponse unDeprecateObjects(RequestContext context) throws RegistryException {
        context = ServerRequestContext.convert(context);
        RegistryResponse resp = null;
        UndeprecateObjectsRequest req = (UndeprecateObjectsRequest) ((ServerRequestContext) context).getCurrentRegistryRequest();
        try {
            List idList = new java.util.ArrayList();
            //Add explicitly specified oref params
            List orefs = bu.getObjectRefsFromObjectRefList(req.getObjectRefList());

            //Append those orefs specified via ad hoc query param
            orefs.addAll(((ServerRequestContext) context).getObjectsRefsFromQueryResults(req.getAdhocQuery()));

            Iterator orefsIter = orefs.iterator();

            while (orefsIter.hasNext()) {
                ObjectRefType oref = (ObjectRefType) orefsIter.next();
                idList.add(oref.getId());
            }

            pm.updateStatus(((ServerRequestContext) context), idList,
                    bu.CANONICAL_STATUS_TYPE_ID_Submitted);
            resp = bu.rsFac.createRegistryResponse();
            resp.setStatus(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);

            if (((ServerRequestContext) context).getErrorList().getRegistryError().size() > 0) {
                // warning exists
                resp.setRegistryErrorList(((ServerRequestContext) context).getErrorList());
            }
        } catch (RegistryException e) {
            ((ServerRequestContext) context).rollback();
            throw e;
        } catch (Exception e) {
            ((ServerRequestContext) context).rollback();
            throw new RegistryException(e);
        }

        ((ServerRequestContext) context).commit();
        return resp;
    }

    /**
     * Removes one or more previously submitted objects from the registry. If the
     * deletionScope is "DeleteRepositoryItemOnly", it will assume all the
     * ObjectRef under ObjectRefList is referencing repository items. If the
     * deletionScope is "DeleteAll", the reference may be either RegistryObject
     * or repository item. In both case, if the referenced object cannot be found,
     * RegistryResponse with errors list will be returned.
     */
    public RegistryResponse removeObjects(RequestContext context) throws RegistryException {
        context = ServerRequestContext.convert(context);
        ServerRequestContext _context = (ServerRequestContext) context;
        RegistryResponse resp = null;
        RemoveObjectsRequest req = (RemoveObjectsRequest) _context.getCurrentRegistryRequest();

        //This request option instructs the server to delete objects even if references exist to them
        boolean forceDelete = false;

        //This request option instructs the server to also delete the network of objects reachable by
        //reference from the objects being deleted. This option is not implemented yet.
        boolean cascadeDelete = false;

        try {
            //Get relevant request slots if any
            HashMap requestSlots = bu.getSlotsFromRequest(req);

            if (requestSlots.containsKey(bu.CANONICAL_SLOT_DELETE_MODE_FORCE)) {
                String val = (String) requestSlots.get(bu.CANONICAL_SLOT_DELETE_MODE_FORCE);
                if (val.trim().equalsIgnoreCase("true")) {
                    forceDelete = true;
                }
            }

            if (requestSlots.containsKey(bu.CANONICAL_SLOT_DELETE_MODE_CASCADE)) {
                String val = (String) requestSlots.get(bu.CANONICAL_SLOT_DELETE_MODE_CASCADE);
                if (val.trim().equalsIgnoreCase("true")) {
                    cascadeDelete = true;
                }
            }

            List orefs = null;
            if (req.getObjectRefList() == null) {
                org.oasis.ebxml.registry.bindings.rim.ObjectRefList orList = bu.rimFac.createObjectRefList();
                req.setObjectRefList(orList);
            }

            //Add explicitly specified oref params
            orefs = bu.getObjectRefsFromObjectRefList(req.getObjectRefList());

            //Append those orefs specified via ad hoc query param
            orefs.addAll(_context.getObjectsRefsFromQueryResults(req.getAdhocQuery()));

            Iterator orefsIter = orefs.iterator();

            while (orefsIter.hasNext()) {
                ObjectRefType oref = (ObjectRefType) orefsIter.next();
                _context.getObjectRefsMap().put(oref.getId(), oref);
            }

            List idList = new ArrayList(_context.getObjectRefsMap().keySet());
            pm.updateIdToLidMap(_context, _context.getObjectRefsMap().keySet(), "RegistryObject");
            Set idsNotInRegistry = _context.getIdsNotInRegistry(_context.getObjectRefsMap().keySet());
            if (idsNotInRegistry.size() > 0) {
                throw new ObjectsNotFoundException(new ArrayList(idsNotInRegistry));
            }
            String deletionScope = BindingUtility.CANONICAL_DELETION_SCOPE_TYPE_ID_DeleteAll;

            if (req.getDeletionScope() != null) {
                deletionScope = req.getDeletionScope();
            }

            //DeletionScope=DeleteRepositoryItemOnly. If any repository item
            //does not exist, it will stop
            if (deletionScope.equals(BindingUtility.CANONICAL_DELETION_SCOPE_TYPE_ID_DeleteAll)) {
                if (!forceDelete) {
                    pm.checkIfReferencesExist((ServerRequestContext) context, idList);
                }

                //Delete all ROs with the ids
                pm.delete(_context, orefs);
            } else {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.undefinedDeletionScope"));
            }

            resp = bu.rsFac.createRegistryResponse();
            resp.setStatus(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);
        } catch (RegistryException e) {
            ((ServerRequestContext) context).rollback();
            throw e;
        } catch (Exception e) {
            ((ServerRequestContext) context).rollback();
            throw new RegistryException(e);
        }
        ((ServerRequestContext) context).commit();
        return resp;
    }

    /**
     * Checks that the user in the current context is authorized to do
     * everything necessary to process the current request.
     *
     * @param context a <code>RequestContext</code> value
     * @exception UnauthorizedRequestException if an error occurs
     * @exception RegistryException if an error occurs
     */
    private void checkAuthorizedAll(ServerRequestContext context)
            throws UnauthorizedRequestException, RegistryException {

        /* HIEOS/BHT (NOOP):
        boolean noRegRequired = Boolean.valueOf(CommonProperties.getInstance().getProperty("omar.common.noUserRegistrationRequired", "false")).booleanValue();

        if (!noRegRequired) {
        checkAuthorized(((ServerRequestContext) context),
        AuthorizationResult.PERMIT_NONE | AuthorizationResult.PERMIT_SOME);
        }
         */
    }

    /**
     * Checks that the user in the current context is authorized to do
     * everything except the specified authorization levels.
     *
     * @param context a <code>RequestContext</code> value
     * @param throwExceptionOn Flags specifying when to throw exceptions
     * @exception UnauthorizedRequestException if an error occurs
     * @exception RegistryException if an error occurs
     */
    private void checkAuthorized(ServerRequestContext context,
            int throwExceptionOn)
            throws UnauthorizedRequestException, RegistryException {
        /* HIEOS/BHT (NOOP):
        AuthorizationResult authRes =
        AuthorizationServiceImpl.getInstance().checkAuthorization(((ServerRequestContext) context));
        authRes.throwExceptionOn(throwExceptionOn);
         */
    }
}
