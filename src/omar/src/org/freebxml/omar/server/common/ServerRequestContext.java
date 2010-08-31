/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/common/ServerRequestContext.java,v 1.21 2006/12/12 20:52:48 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.common;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.bind.JAXBException;
import javax.xml.registry.InvalidRequestException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.common.BindingUtility;
import javax.xml.registry.JAXRException;
import org.freebxml.omar.common.CommonRequestContext;
import org.freebxml.omar.common.spi.QueryManager;
import org.freebxml.omar.common.spi.QueryManagerFactory;
import org.freebxml.omar.common.ReferenceInfo;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.common.spi.RequestContext;
import org.freebxml.omar.server.persistence.PersistenceManager;
import org.freebxml.omar.server.persistence.PersistenceManagerFactory;
import org.freebxml.omar.server.util.ServerResourceBundle;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponseType;
import org.oasis.ebxml.registry.bindings.query.ResponseOption;

import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.query.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.AdhocQueryType;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rs.RegistryErrorListType;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;

/*
 * HIEOS (CHANGE) - Removed use of interceptors, repository, replicas, versioning,
 *                  cache and events.
 */
/**
 * Keeps track of the state and context for a client request
 * as it makes its way through the server.
 *
 * @author  Farrukh S. Najmi
 */
public class ServerRequestContext extends CommonRequestContext {

    private Log log = LogFactory.getLog(this.getClass());
    private static BindingUtility bu = BindingUtility.getInstance();
    private static PersistenceManager pm = PersistenceManagerFactory.getInstance().getPersistenceManager();
    private static QueryManager qm = QueryManagerFactory.getInstance().getQueryManager();
    //Map of top level Identifiable objects within the request with id keys and IdentifiableType values
    private Map topLevelObjectsMap = new HashMap();
    //Ids of subset of submittedObjects that are new and not pre-existing in registry
    private Set newSubmittedObjectIds = null;
    //New versions of RegistryObjects that are a subset of topLevelObjects that were created by Versioning feature
    private Map newROVersionMap = new HashMap();
    //New versions of RepositoryItems that were created by Versioning feature
    private Map newRIVersionMap = new HashMap();
    //Map of composed RegistryObjects within the request with id keys and RegistryObjectType values
    private Map composedObjectsMap = new HashMap();
    //Map of all submitted RegistryObjects objects within the request with id keys and IdentifiableType values
    //includes composedObjects
    //Set of all RegistryObject ids referenced from submitted (top level + composed) objects
    private Set referencedInfos = null;
    //Set of solved id references for this request
    private SortedSet checkedRefs = new TreeSet();
    //Map of RegistryObject owners with RO id keys and ownerId string values
    private SortedMap fetchedOwners = new TreeMap();
    //Map of submitted RegistryObjects with RO id keys and RegistryObjectType values
    private Map submittedObjectsMap = new HashMap();
    //Map of ObjectRefs within the request with id keys and ObjectRef values
    private Map objectRefsMap = new HashMap();
    //Maps temporary id key to permanent id value
    private Map idMap = new HashMap();
    //Used only by QueryManagerImpl to pass results of a query for read access control check.
    private List queryResults = new ArrayList();
    //Short lived memory used only in handling stored query invocation
    private List storedQueryParams = new ArrayList();

    //The RegistryErrorList for this request
    private RegistryErrorListType errorList = null;
    //Begin former DAOContext members
    private Connection connection = null;
    private ResponseOptionType responseOption;
    private ArrayList objectRefs;
    //Consolidation of events of all types above into a single List. This is initialized in saveAuditableEVents()
    private List auditableEvents = new ArrayList();
    private Map affectedObjectsMap = new HashMap();
    //Map from id to lid for existing objects in registry that are either submitted or referenced in this request
    private Map idToLidMap = new HashMap();
    //The queryId for a request that is a parameterized query invocation
    private String queryId;
    private Map queryParamsMap = null;
    //true if user has RegistryAdministrator role

    /** Creates a new instance of RequestContext
     * @param contextId
     * @param request
     * @throws RegistryException
     */
    public ServerRequestContext(String contextId, RegistryRequestType request) throws RegistryException {
        super(contextId, request);

        //Call RequestInterceptors
        //Only intercept top level requests.
        /* HIEOS/BHT (REMOVED):
        if (request != null) {
        RequestInterceptorManager.getInstance().preProcessRequest(this);
        }
         */

        try {
            setErrorList(BindingUtility.getInstance().rsFac.createRegistryErrorList());

            objectRefs = new ArrayList();

        } catch (JAXBException e) {
            throw new RegistryException(e);
        }
    }
    
    /**
     *
     * Removes object matching specified id from all the various maps.
     * @param id
     */
    public void remove(String id) {
        getTopLevelObjectsMap().remove(id);
        getSubmittedObjectsMap().remove(id);
        getComposedObjectsMap().remove(id);
        getIdMap().remove(id);
    }

    /**
     * Checks each object including composed objects.
     * @throws RegistryException
     */
    public void checkObjects() throws RegistryException {
        try {
            //Process ObjectRefs and create local replicas of any remote ObjectRefs
            //createReplicasOfRemoteObjectRefs();

            //Get all submitted objects including composed objects that are part of the submission
            //so that they can be used to resolve references
            getSubmittedObjectsMap().putAll(getTopLevelObjectsMap());

            Set composedObjects = bu.getComposedRegistryObjects(getTopLevelObjectsMap().values(), -1);
            getSubmittedObjectsMap().putAll(bu.getRegistryObjectMap(composedObjects));

            // HIEOS(START CHANGE) - force objects into idmap (optimization):
            //pm.updateIdToLidMap(this, getSubmittedObjectsMap().keySet(), "RegistryObject");
            for (Object id : getSubmittedObjectsMap().keySet()) {
                String idString = (String) id;
                this.getIdToLidMap().put(id, id);
            }
            // HIEOS(END CHANGE)

            getNewSubmittedObjectIds();

            //Check id of each object (top level or composed)
            Iterator iter = getSubmittedObjectsMap().values().iterator();
            while (iter.hasNext()) {
                RegistryObjectType ro = (RegistryObjectType) iter.next();

                //AuditableEvents are not allowed to be submitted by clients
                if (ro instanceof AuditableEventType) {
                    throw new InvalidRequestException(ServerResourceBundle.getInstance().getString("message.auditableEventsNotAllowed"));
                }
                checkId(ro);
            }

            //Get RegistryObjects referenced by submittedObjects.
            this.getReferenceInfos();

            //Append the references to the IdToLidMap

            iter = this.referencedInfos.iterator();
            Set referencedIds = new HashSet();
            while (iter.hasNext()) {
                ReferenceInfo refInfo = (ReferenceInfo) iter.next();
                referencedIds.add(refInfo.targetObject);
            }

// HIEOS(START CHANGE) - force objects into idmap (optimization):
            //pm.updateIdToLidMap(this, referencedIds, "RegistryObject");
            for (Object id : referencedIds) {
                String idString = (String) id;
                this.getIdToLidMap().put(id, id);
            }
            // HIEOS(END CHANGE)

            //Iterate over idMap and replace keys in various structures that use id as key
            //that are based on temporary ids with their permanent id.
            iter = getIdMap().keySet().iterator();
            while (iter.hasNext()) {
                String idOld = (String) iter.next();
                String idNew = (String) getIdMap().get(idOld);

                //replace in all RequestContext data structures
                Object obj = getTopLevelObjectsMap().remove(idOld);
                if (obj != null) {
                    getTopLevelObjectsMap().put(idNew, obj);
                }
                obj = getSubmittedObjectsMap().remove(idOld);
                if (obj != null) {
                    getSubmittedObjectsMap().put(idNew, obj);
                }
                if (getNewSubmittedObjectIds().remove(idOld)) {
                    getNewSubmittedObjectIds().add(idNew);
                }
            }

            //Now replace any old versions of RegistryObjects with new versions
            iter = getNewROVersionMap().keySet().iterator();
            while (iter.hasNext()) {
                RegistryObjectType roOld = (RegistryObjectType) iter.next();
                RegistryObjectType roNew = (RegistryObjectType) getNewROVersionMap().get(roOld);

                //replace in all data structures
                getSubmittedObjectsMap().remove(roOld.getId());
                getSubmittedObjectsMap().put(roNew.getId(), roNew);
                getTopLevelObjectsMap().remove(roOld.getId());
                getTopLevelObjectsMap().put(roNew.getId(), roNew);
            }

            //resolve references from each object
            resolveObjectReferences();
        } catch (JAXRException e) {
            throw new RegistryException(e);
        }
    }

    /**
     * Check if id is a proper UUID. If not make a proper UUID based URN and add
     * a mapping in idMap between old and new Id.
     *
     * @param submittedIds The ArrayList holding ids of all objects (including composed objects) submitted.
     *
     * @param idMap The HashMap with old temporary id to new permanent id mapping.
     *
     * @throws UUIDNotUniqueException if any UUID is not unique within a
     * SubmitObjectsRequest
     */
    private void checkId(RegistryObjectType ro)
            throws RegistryException {
        String id = ro.getId();

        org.freebxml.omar.common.Utility util = org.freebxml.omar.common.Utility.getInstance();
        if (!util.isValidRegistryId(id)) {
            // Generate permanent id for this temporary id
            String newId = util.createId();
            ro.setId(newId);
            getIdMap().put(id, newId);
        }
    }

    /*
     * Resolves each ObjectRef within the specified objects.
     *
     * @param obj the object whose reference attribute are being checked for being resolvable.
     *
     *
     */
    private void resolveObjectReferences()
            throws RegistryException {

        try {
            //Get Set of ids for objects referenced from obj
            Set refInfos = this.referencedInfos;

            //Check that each ref is resolvable
            Iterator iter = refInfos.iterator();
            Set unresolvedRefIds = new HashSet();
            while (iter.hasNext()) {
                ReferenceInfo refInfo = (ReferenceInfo) iter.next();
                String refId = refInfo.targetObject;

                // only check referenced id once per request
                if (getCheckedRefs().contains(refId)) {
                    continue;
                } else {
                    getCheckedRefs().add(refId);
                }

                ObjectRefType ref = (ObjectRefType) getObjectRefsMap().get(refId);

                //Remote references already resolved by creating local replica by now
                //First check if resolved within submittedIds
                if (!(getSubmittedObjectsMap().containsKey(refId))) {
                    //ref not resolved within submitted objects

                    //See if exists in the registry
                    if (!(getIdToLidMap().keySet().contains(refId))) {
                        unresolvedRefIds.add(refId);
                    }

                }
            }

            if (unresolvedRefIds.size() > 0) {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.unresolvedReferences",
                        new Object[]{unresolvedRefIds}));
            }
        } catch (JAXRException e) {
            throw new RegistryException(e);
        }
    }

    /**
     *
     * @return
     * @throws RegistryException
     */
    public ResponseOptionType getResponseOption() throws RegistryException {
        try {
            if (responseOption == null) {
                responseOption = bu.queryFac.createResponseOption();
                responseOption.setReturnType(ReturnType.LEAF_CLASS);
                responseOption.setReturnComposedObjects(true);
            }
        } catch (JAXBException e) {
            throw new RegistryException(e);
        }
        return responseOption;
    }

    /**
     *
     * @param responseOption
     */
    public void setResponseOption(ResponseOptionType responseOption) {
        this.responseOption = responseOption;
    }

    /**
     * Gets ObjectRefs from result of the AdhocQuery specified (if any).
     *
     * @param query 
     * @return
     * @throws RegistryException
     * @throws JAXBException
     */
    public List getObjectsRefsFromQueryResults(AdhocQueryType query) throws RegistryException, JAXBException {
        List orefs = new ArrayList();

        try {
            if (query != null) {
                AdhocQueryRequest req = bu.queryFac.createAdhocQueryRequest();
                req.setId(org.freebxml.omar.common.Utility.getInstance().createId());
                req.setAdhocQuery(query);

                ResponseOption ro = bu.queryFac.createResponseOption();
                ro.setReturnComposedObjects(false);
                ro.setReturnType(ReturnType.OBJECT_REF);
                req.setResponseOption(ro);
                this.pushRegistryRequest(req);
                AdhocQueryResponseType resp = qm.submitAdhocQuery(this);
                orefs.addAll(resp.getRegistryObjectList().getIdentifiable());
            }
        } finally {
            if (query != null) {
                this.popRegistryRequest();
            }
        }

        return orefs;
    }

    /**
     *
     * @return
     */
    public List getObjectRefs() {
        return objectRefs;
    }

    /**
     *
     * @return
     * @throws RegistryException
     */
    public Connection getConnection() throws RegistryException {
        if (connection == null) {
            connection = pm.getConnection(this);
        }
        return connection;
    }

    /*
     * Called to commit the transaction
     * Saves auditable events for this transaction prior to commit.
     * Notifies EventManager after commit.
     */
    /**
     *
     * @throws RegistryException
     */
    public void commit() throws RegistryException {
        //Dont commit unless this is the last request in stack.
        if ((connection != null) && (getRegistryRequestStack().size() <= 1)) {
            try {
                //Only commit if LCM_DO_NOT_COMMIT is unspecified or false
                String dontCommit = null;
                if (getRegistryRequestStack().size() > 0) {
                    HashMap slotsMap = bu.getSlotsFromRequest(this.getCurrentRegistryRequest());
                    dontCommit = (String) slotsMap.get(BindingUtility.CANONICAL_SLOT_LCM_DO_NOT_COMMIT);
                }
                if ((dontCommit == null) || (dontCommit.equalsIgnoreCase("false"))) {
                    connection.commit();
                    pm.releaseConnection(this, connection);
                    connection = null;
                } else {
                    rollback();
                }
            } catch (RegistryException e) {
                rollback();
                throw e;
            } catch (JAXBException e) {
                rollback();
                throw new RegistryException(e);
            } catch (SQLException e) {
                rollback();
                throw new RegistryException(e);
            }

            //Call RequestInterceptors
            if (getRegistryRequestStack().size() == 1) {
                //Only intercept top level requests.
                //Still causing infinite recursion and StackOverflow
                //RequestInterceptorManager.getInstance().postProcessRequest(this);
            }
        }
    }

    /**
     *
     * @throws RegistryException
     */
    public void rollback() throws RegistryException {
        try {
            //Dont rollback if there are multiple requests on the requestStack
            if ((connection != null) && (getRegistryRequestStack().size() <= 1)) {
                connection.rollback();
                pm.releaseConnection(this, connection);
                connection = null;
            }
        } catch (SQLException e) {
        }
    }

    /**
     *
     * @return
     */
    public Map getTopLevelObjectsMap() {
        return topLevelObjectsMap;
    }

    /**
     *
     * @return
     */
    public Set getNewSubmittedObjectIds() {
        if (newSubmittedObjectIds == null) {
            newSubmittedObjectIds = new HashSet();
            newSubmittedObjectIds = getIdsNotInRegistry(getSubmittedObjectsMap().keySet());
        }
        return newSubmittedObjectIds;
    }

    /*
     * Gets the subset of ids that do not match ids of objects that are already in registry
     */
    /**
     *
     * @param ids
     * @return
     */
    public Set getIdsNotInRegistry(Set ids) {
        Set idsNotInRegistry = new HashSet();

        Iterator iter = ids.iterator();
        while (iter.hasNext()) {
            Object id = iter.next();
            if (!(getIdToLidMap().keySet().contains(id))) {
                idsNotInRegistry.add(id);
            }
        }

        return idsNotInRegistry;
    }

    /**
     *
     * @return
     */
    public Map getNewROVersionMap() {
        return newROVersionMap;
    }

    /**
     *
     * @return
     */
    public Map getNewRIVersionMap() {
        return newRIVersionMap;
    }

    /**
     *
     * @return
     */
    public Map getComposedObjectsMap() {
        return composedObjectsMap;
    }

    /**
     *
     * @return
     */
    public SortedSet getCheckedRefs() {
        return checkedRefs;
    }

    /**
     *
     * @return
     */
    public SortedMap getFetchedOwners() {
        return fetchedOwners;
    }

    /**
     *
     * @return
     */
    public Map getSubmittedObjectsMap() {
        return submittedObjectsMap;
    }

    /**
     *
     * @return
     */
    public Map getObjectRefsMap() {
        return objectRefsMap;
    }

    /**
     * 
     * @return
     */
    public Map getIdMap() {
        return idMap;
    }

    /**
     *
     * @return
     */
    public RegistryErrorListType getErrorList() {
        return errorList;
    }

    private void setErrorList(RegistryErrorListType errorList) {
        this.errorList = errorList;
    }

    /**
     *
     * @param queryResults
     */
    public void setQueryResults(List queryResults) {
        //Need to create a copy because if the List param is a SingletonList
        //then remove() method does not work on it during filtering of objects
        //that are not authorized to be seen by requestor in QueryManager.
        this.queryResults = new ArrayList();
        this.queryResults.addAll(queryResults);
    }

    /**
     * If context is not a ServerRequestContext then convert it to ServerRequestContext.
     * This is used in XXManagerLocalProxy classes to convert a ClientRequestContext to a ServerRequestContext.
     *
     * @param context 
     * @return the ServerRequestContext
     * @throws RegistryException
     */
    public static ServerRequestContext convert(RequestContext context) throws RegistryException {
        ServerRequestContext serverContext = null;

        if (context instanceof ServerRequestContext) {
            serverContext = (ServerRequestContext) context;
        } else {
            RegistryRequestType req = null;
            if (context.getRegistryRequestStack().size() > 0) {
                req = context.getCurrentRegistryRequest();
            }
            serverContext = new ServerRequestContext(context.getId(), req);
            serverContext.setUser(context.getUser());
        }
        return serverContext;
    }

    /**
     *
     * @return
     */
    public Map getAffectedObjectsMap() {
        return affectedObjectsMap;
    }

    /**
     *
     * @return
     */
    public List getStoredQueryParams() {
        return storedQueryParams;
    }

    /**
     *
     * @return
     */
    public Map getIdToLidMap() {
        return idToLidMap;
    }

    /**
     *
     * @return
     * @throws RegistryException
     */
    public Set getReferenceInfos() throws RegistryException {
        if (referencedInfos == null) {
            try {
                referencedInfos = new HashSet();

                Iterator iter = getSubmittedObjectsMap().entrySet().iterator();
                while (iter.hasNext()) {
                    Object o = ((java.util.Map.Entry) iter.next()).getValue();

                    if (o instanceof RegistryObjectType) {
                        RegistryObjectType ro = (RegistryObjectType) o;
                        //Get Set of ids for objects referenced from obj
                        Set refInfos = bu.getObjectRefsInRegistryObject(ro, getIdMap(), new HashSet(), -1);

                        Iterator refInfosIter = refInfos.iterator();
                        while (refInfosIter.hasNext()) {
                            referencedInfos.add(refInfosIter.next());
                        }
                    }
                }
            } catch (JAXRException e) {
                throw new RegistryException(e);
            }
        }

        return referencedInfos;
    }

    /**
     *
     * @return
     */
    public String getQueryId() {
        return queryId;
    }

    /**
     *
     * @param queryId
     */
    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    /**
     *
     * @return
     */
    public Map getQueryParamsMap() {
        return queryParamsMap;
    }

    /**
     *
     * @param queryParamsMap
     */
    public void setQueryParamsMap(Map queryParamsMap) {
        this.queryParamsMap = queryParamsMap;
    }

    /* HIEOS (REMOVED):
    public boolean isRegistryAdministrator() throws RegistryException {
        if (isAdmin == null) {
            UserType user = getUser();
            if (user != null) {
                isAdmin = Boolean.valueOf(AuthenticationServiceImpl.getInstance().hasRegistryAdministratorRole(user));
            }
        }

        return isAdmin.booleanValue();
    }*/
}
