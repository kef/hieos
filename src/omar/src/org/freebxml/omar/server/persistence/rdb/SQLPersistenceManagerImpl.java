/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/persistence/rdb/SQLPersistenceManagerImpl.java,v 1.88 2007/06/06 14:40:15 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.persistence.rdb;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.registry.RegistryException;
import javax.xml.registry.JAXRException;
import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.CommonResourceBundle;
import org.freebxml.omar.server.common.RegistryProperties;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.util.ServerResourceBundle;
import org.freebxml.omar.common.IterativeQueryParams;
import org.oasis.ebxml.registry.bindings.query.ResponseOption;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.query.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefListType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.UserType;

/*
 * HIEOS (CHANGE): Removed unused XDS.b object types.  These include:
 *
 * AffectedObject
 * AuditableEvent
 * ClassificationNode
 * ClassificationScheme
 * EmailAddress
 * ExternalLink
 * Federation
 * ObjectRef
 * Organization
 * Person
 * PostalAddress
 * Registry
 * Service
 * ServiceBinding
 * SpecificationLink
 * Subscription
 * TelephoneNumber
 * User
 */

/* HIEOS (CHANGE): Removed redundant ConnectionPool scheme.
 */
/**
 * Class Declaration for SQLPersistenceManagerImpl.
 * @see
 * @author Farrukh S. Najmi
 * @author Adrian Chong
 */
public class SQLPersistenceManagerImpl
        implements org.freebxml.omar.server.persistence.PersistenceManager {

    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */

    /*# private SQLPersistenceManagerImpl _sqlPersistenceManagerImpl; */
    private static SQLPersistenceManagerImpl instance = null;
    private static final Log log = LogFactory.getLog(SQLPersistenceManagerImpl.class);
    private BindingUtility bu = BindingUtility.getInstance();
    private int numConnectionsOpen = 0;
    /**
     *
     * @associates <{org.freebxml.omar.server.persistence.rdb.ExtrinsicObjectDAO}>
     */
    private boolean dumpStackOnQuery;
    private int transactionIsolation;
    private DataSource ds = null;

    private SQLPersistenceManagerImpl() {
        // define transaction isolation
        if ("TRANSACTION_READ_COMMITTED".equalsIgnoreCase(RegistryProperties.getInstance().getProperty("omar.persistence.rdb.transactionIsolation"))) {
            transactionIsolation = Connection.TRANSACTION_READ_COMMITTED;
        } else {
            transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED;
        }

        dumpStackOnQuery = Boolean.valueOf(RegistryProperties.getInstance().getProperty("omar.persistence.rdb.dumpStackOnQuery", "false")).booleanValue();

        //Create JNDI context
        // Use Container's connection pooling
        String omarName = RegistryProperties.getInstance().getProperty("omar.name", "omar");
        String envName = "java:comp/env";
        String dataSourceName = "jdbc/" + omarName + "-registry";
        Context ctx = null;

        try {
            ctx = new InitialContext();
            if (null == ctx) {
                log.info(ServerResourceBundle.getInstance().
                        getString("message.UnableToGetInitialContext"));
            }
        } catch (NamingException e) {
            log.info(ServerResourceBundle.getInstance().
                    getString("message.UnableToGetInitialContext"), e);
            ctx = null;
        }

        if (null != ctx) {
            try {
                ds = (DataSource) ctx.lookup(dataSourceName);
                if (null == ds) {
                    log.fatal(ServerResourceBundle.getInstance().
                            getString("message.UnableToGetJNDIContextForDataSource",
                            new Object[]{envName + "/" + dataSourceName}));
                }
            } catch (NamingException e) {
                log.fatal(ServerResourceBundle.getInstance().
                        getString("message.UnableToGetJNDIContextForDataSource",
                        new Object[]{envName + "/" + dataSourceName}),
                        e);
                ds = null;
            }
        }

        if (ds != null) {
            // Create a test connection to make sure all is well with DataSource
            Connection connection = null;
            try {
                connection = ds.getConnection();
            } catch (Exception e) {
                log.fatal(ServerResourceBundle.getInstance().
                        getString("message.UnableToCreateTestConnectionForDataSource",
                        new Object[]{envName + "/" + dataSourceName}), e);
                ds = null;
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Exception e1) {
                        //Do nothing.
                    }
                }
            }
        }

        if (ds == null) {
            String errorString = ServerResourceBundle.getInstance().getString("message.ErrorUnableToOpenDbConnectionForDataSource=", new Object[]{ds});
            log.fatal(errorString);
        }
    }

    /**
     * Get a database connection. The connection is of autocommit off and with
     * transaction isolation level "transaction read committed"
     */
    public Connection getConnection() throws RegistryException {
        Connection connection = null;
        if (log.isDebugEnabled()) {
            log.debug("SQLPersistenceManagerImpl.getConnection");
            numConnectionsOpen++;
        }
        try {
            if (ds != null) {
                connection = ds.getConnection();
                if (connection == null) {
                    String errorString = ServerResourceBundle.getInstance().getString("message.ErrorUnableToOpenDbConnectionForDataSource=", new Object[]{ds});
                    log.fatal(errorString);
                    throw new RegistryException(errorString);
                }
            }
            connection.setTransactionIsolation(transactionIsolation);
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.connectToDatabaseFailed"), e);
        }
        return connection;
    }

    /**
     * 
     * @param connection
     * @throws RegistryException
     */
    public void releaseConnection(Connection connection) throws RegistryException {
        if (log.isDebugEnabled()) {
            log.debug("SQLPersistenceManagerImpl.releaseConnection");
            numConnectionsOpen--;
            log.debug("Number of connections open:" + numConnectionsOpen);
        }
        try {
            if ((connection != null)
                    && (!connection.isClosed()) && (ds != null)) {
                connection.close();
            }
        } catch (Exception e) {
            throw new RegistryException(e);
        }
    }

    /**
     *
     * @return
     */
    public synchronized static SQLPersistenceManagerImpl getInstance() {
        if (instance == null) {
            instance = new SQLPersistenceManagerImpl();
        }
        return instance;
    }

//Sort objects by their type.
    private void sortRegistryObjects(List registryObjects, List associations,
            List classifications, List externalIds,
            List extrinsicObjects, List packages)
            throws RegistryException {
        associations.clear();
        classifications.clear();
        externalIds.clear();
        extrinsicObjects.clear();
        packages.clear();

        java.util.Iterator objIter = registryObjects.iterator();

        while (objIter.hasNext()) {
            IdentifiableType obj = (IdentifiableType) objIter.next();

            if (obj instanceof org.oasis.ebxml.registry.bindings.rim.AssociationType1) {
                associations.add(obj);
            } else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.ClassificationType) {
                classifications.add(obj);
            } else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.ExternalIdentifierType) {
                externalIds.add(obj);
            } else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType) {
                extrinsicObjects.add(obj);
            } else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.RegistryPackageType) {
                packages.add(obj);
            } else {
                throw new RegistryException(CommonResourceBundle.getInstance().getString("message.unexpectedObjectType",
                        new Object[]{obj.getClass().getName(), "org.oasis.ebxml.registry.bindings.rim.IdentifiableType"}));
            }

        }
    }

    /**
     * Does a bulk insert of a heterogeneous Collection of RegistrObjects.
     *
     */
    public void insert(ServerRequestContext context, List registryObjects)
            throws RegistryException {

        List associations = new java.util.ArrayList();
        List classifications = new java.util.ArrayList();
        List externalIds = new java.util.ArrayList();
        List extrinsicObjects = new java.util.ArrayList();
        List packages = new java.util.ArrayList();

        sortRegistryObjects(registryObjects, associations,
                classifications, externalIds, extrinsicObjects, packages);

        if (associations.size() > 0) {
            AssociationDAO associationDAO = new AssociationDAO(context);
            associationDAO.insert(associations);
        }

        if (classifications.size() > 0) {
            ClassificationDAO classificationDAO = new ClassificationDAO(context);
            classificationDAO.insert(classifications);
        }

        if (externalIds.size() > 0) {
            ExternalIdentifierDAO externalIdentifierDAO = new ExternalIdentifierDAO(context);
            externalIdentifierDAO.insert(externalIds);
        }

        if (extrinsicObjects.size() > 0) {
            ExtrinsicObjectDAO extrinsicObjectDAO = new ExtrinsicObjectDAO(context);
            extrinsicObjectDAO.insert(extrinsicObjects);
        }

        if (packages.size() > 0) {
            RegistryPackageDAO registryPackageDAO = new RegistryPackageDAO(context);
            registryPackageDAO.insert(packages);
        }

    }

    /**
     * Does a bulk update of a heterogeneous Collection of RegistrObjects.
     *
     */
    public void update(ServerRequestContext context, List registryObjects)
            throws RegistryException {

        List associations = new java.util.ArrayList();
        List classifications = new java.util.ArrayList();
        List externalIds = new java.util.ArrayList();
        List extrinsicObjects = new java.util.ArrayList();
        List packages = new java.util.ArrayList();

        sortRegistryObjects(registryObjects, associations,
                classifications, externalIds, extrinsicObjects, packages);

        if (associations.size() > 0) {
            AssociationDAO associationDAO = new AssociationDAO(context);
            associationDAO.update(associations);
        }

        if (classifications.size() > 0) {
            ClassificationDAO classificationDAO = new ClassificationDAO(context);
            classificationDAO.update(classifications);
        }

        if (externalIds.size() > 0) {
            // ExternalId is no longer the first level, right?
            ExternalIdentifierDAO externalIdentifierDAO = new ExternalIdentifierDAO(context);
            externalIdentifierDAO.update(externalIds);
        }

        if (extrinsicObjects.size() > 0) {
            ExtrinsicObjectDAO extrinsicObjectDAO = new ExtrinsicObjectDAO(context);
            extrinsicObjectDAO.update(extrinsicObjects);
        }

        if (packages.size() > 0) {
            RegistryPackageDAO registryPackageDAO = new RegistryPackageDAO(context);
            registryPackageDAO.update(packages);
        }

    }

    /**
     * Update the status of specified objects to the specified status.
     *
     */
    @SuppressWarnings("static-access")
    public void updateStatus(ServerRequestContext context, List registryObjectsIds,
            String status)
            throws RegistryException {
        try {
            //Make sure that status is a ref to a StatusType ClassificationNode
            /* HIEOS (REMOVED):
            context.checkClassificationNodeRefConstraint(status, bu.CANONICAL_CLASSIFICATION_SCHEME_ID_StatusType, "status");
             */
            ObjectRefListType orefList = bu.rimFac.createObjectRefList();

            List refs = bu.getObjectRefsFromRegistryObjectIds(registryObjectsIds);
            Iterator iter = refs.iterator();
            while (iter.hasNext()) {
                ObjectRefType ref = (ObjectRefType) iter.next();
                // HIEOS/AMS/BHT: Removed next line of code (to speed up process).
                // RegistryObjectType ro = getRegistryObject(context, ref);
                // HIEOS/AMS/BHT: Now, calling new method (again, to speed up process).
                RegistryObjectType ro = getRegistryObjectForStatusUpdate(context, ref);
                RegistryObjectDAO roDAO = (RegistryObjectDAO) getDAOForObject(ro, context);
                roDAO.updateStatus(ro, status);
                orefList.getObjectRef().add(ref);
            }

        } catch (JAXBException e) {
            throw new RegistryException(e);
        } catch (JAXRException e) {
            throw new RegistryException(e);
        }

    }

    /**
     * Given a Binding object returns the OMARDAO for that object.
     *
     */
    private OMARDAO getDAOForObject(RegistryObjectType ro, ServerRequestContext context) throws RegistryException {
        OMARDAO dao = null;

        try {
            String bindingClassName = ro.getClass().getName();
            String daoClassName = bindingClassName.substring(bindingClassName.lastIndexOf('.') + 1, bindingClassName.length() - 4);

            //Construct the corresonding DAO instance using reflections
            Class daoClazz = Class.forName("org.freebxml.omar.server.persistence.rdb." + daoClassName + "DAO");

            Class[] conParameterTypes = new Class[1];
            conParameterTypes[0] = context.getClass();
            Object[] conParameterValues = new Object[1];
            conParameterValues[0] = context;
            Constructor[] cons = daoClazz.getDeclaredConstructors();

            //Find the constructor that takes RequestContext as its only arg
            Constructor con = null;
            for (int i = 0; i
                    < cons.length; i++) {
                con = cons[i];
                if ((con.getParameterTypes().length == 1) && (con.getParameterTypes()[0] == conParameterTypes[0])) {
                    dao = (OMARDAO) con.newInstance(conParameterValues);
                    break;

                }


            }

        } catch (Exception e) {
            throw new RegistryException(e);
        }

        return dao;

    }

    /**
     * Does a bulk delete of a heterogeneous Collection of RegistrObjects. If
     * any RegistryObject cannot be found, it will make no change to the
     * database and throw RegistryException
     *
     */
    public void delete(ServerRequestContext context, List orefs)
            throws RegistryException {
        //Return if nothing specified to delete
        if (orefs.isEmpty()) {
            return;
        }

        List idList = new ArrayList();
        idList.addAll(context.getObjectRefsMap().keySet());

        //First fetch the objects and then delete them
        String query = "SELECT * FROM RegistryObject ro WHERE ro.id IN ( "
                + bu.getIdListFromIds(idList)
                + " ) ";
        List objs = getRegistryObjectsMatchingQuery(context, query, null, "RegistryObject");
        List userAliases = null;
        Iterator iter = objs.iterator();
        while (iter.hasNext()) {
            RegistryObjectType ro = (RegistryObjectType) iter.next();
            if (ro instanceof UserType) {
                if (userAliases == null) {
                    userAliases = new ArrayList();
                }

                userAliases.add(((UserType) ro).getId());
            }

            OMARDAO dao = getDAOForObject(ro, context);

            //Now call delete method
            List objectsToDelete = new ArrayList();
            objectsToDelete.add(ro);
            dao.delete(objectsToDelete);
        }

        /* HIEOS (REMOVED):
        //Now delete from ObjectRef table
        ObjectRefDAO dao = new ObjectRefDAO(context);
        dao.delete(orefs);
         */
//Now, if any of the deleted ROs were of UserType, delete the credentials
//from the server keystore
        if (userAliases != null) {
            Iterator aliasItr = userAliases.iterator();
            String alias = null;
            while (aliasItr.hasNext()) {
                try {
                    alias = (String) aliasItr.next();
//                    AuthenticationServiceImpl.getInstance().deleteUserCertificate(alias);
                } catch (Throwable t) {
                    ServerResourceBundle.getInstance().getString("message.couldNotDeleteCredentials",
                            new Object[]{alias});
                }

            }
        }
    }

    /**
     * Executes an SQL Query with default values for IterativeQueryParamHolder.
     */
    /* HIEOS (REMOVED):
    public List executeSQLQuery(
    ServerRequestContext context, String sqlQuery,
    ResponseOptionType responseOption, String tableName, List objectRefs)
    throws RegistryException {
    IterativeQueryParams paramHolder = new IterativeQueryParams(0, -1);
    return executeSQLQuery(context, sqlQuery, responseOption, tableName, objectRefs, paramHolder);
    }*/
    /**
     * Executes and SQL query using specified parameters.
     * This variant is used to invoke fixed queries without PreparedStatements.
     *
     * @return An List of RegistryObjectType instances
     */
    /* HIEOS (REMOVED):
    public List executeSQLQuery(
    ServerRequestContext context, String sqlQuery,
    ResponseOptionType responseOption, String tableName, List objectRefs,
    IterativeQueryParams paramHolder)
    throws RegistryException {
    return executeSQLQuery(context, sqlQuery, null, responseOption, tableName, objectRefs, paramHolder);
    }*/
    /**
     * Executes an SQL Query.
     */
    /* HIEOS (REMOVED):
    public List executeSQLQuery(
    ServerRequestContext context, String sqlQuery, List queryParams,
    ResponseOptionType responseOption, String tableName, List objectRefs)
    throws RegistryException {
    IterativeQueryParams paramHolder = new IterativeQueryParams(0, -1);
    return executeSQLQuery(context, sqlQuery, queryParams, responseOption, tableName, objectRefs, paramHolder);
    } */
    /**
     * Executes an SQL Query.
     */
    @SuppressWarnings("static-access")
    public List executeSQLQuery(
            ServerRequestContext context, String sqlQuery, List queryParams,
            ResponseOptionType responseOption, String tableName, List objectRefs,
            IterativeQueryParams paramHolder)
            throws RegistryException {
        List res = null;
        Connection connection = null;
        int startIndex = paramHolder.startIndex;
        int maxResults = paramHolder.maxResults;
        int totalResultCount = -1;
        Statement stmt = null;

        try {
            connection = context.getConnection();
            java.sql.ResultSet rs = null;
            tableName = org.freebxml.omar.common.Utility.getInstance().mapTableName(tableName);

            ReturnType returnType = responseOption.getReturnType();
            // HIEOS (REMOVED) - Not used
            // boolean returnComposedObjects = responseOption.isReturnComposedObjects();

            if (maxResults < 0) {
                if (queryParams == null) {
                    stmt = connection.createStatement();
                } else {
                    stmt = connection.prepareStatement(sqlQuery);
                }
            } else {
                if (queryParams == null) {
                    stmt = connection.createStatement(java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY);
                } else {
                    stmt = connection.prepareStatement(sqlQuery, java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Executing query: '" + sqlQuery + "'");
                if (dumpStackOnQuery) {
                    Thread.currentThread().dumpStack();
                }
            }
            log.trace("SQL = " + sqlQuery);  // HIEOS/BHT: (DEBUG)
            if (queryParams == null) {
                rs = stmt.executeQuery(sqlQuery);
            } else {
                Iterator iter = queryParams.iterator();
                int paramCount = 0;
                while (iter.hasNext()) {
                    Object param = iter.next();
                    ((PreparedStatement) stmt).setObject(++paramCount, param);
                    // HIEOS/BHT (DEBUG):
                    log.trace("  -> param(" + new Integer(paramCount).toString() + "): " + (String) param);
                }

                rs = ((PreparedStatement) stmt).executeQuery();
            }

            if (maxResults >= 0) {
                rs.last();
                totalResultCount =
                        rs.getRow();
                // Reset back to before first row so that DAO can correctly scroll
                // through the result set
                rs.beforeFirst();
            }
            if (returnType == ReturnType.OBJECT_REF) {
                res = new java.util.ArrayList();

                if (startIndex > 0) {
                    rs.last();
                    totalResultCount = rs.getRow();
                    rs.beforeFirst();
                    // calling rs.next() is a workaround for some drivers, such
                    // as Derby's, that do not set the cursor during call to 
                    // rs.relative(...)
                    rs.next();
                    boolean onRow = rs.relative(startIndex - 1);
                    // HIEOS/BHT (DEBUG):
                    log.trace(" -> Total Result Count: " + totalResultCount);
                }

                int cnt = 0;
                while (rs.next()) {
                    org.oasis.ebxml.registry.bindings.rim.ObjectRef or = bu.rimFac.createObjectRef();
                    String id = rs.getString(1);
                    or.setId(id);
                    res.add(or);
                    if (++cnt == maxResults) {
                        break;
                    }

                }
                // HIEOS/BHT (DEBUG):
                log.trace(" -> cnt: " + totalResultCount);
            } else if (returnType == ReturnType.REGISTRY_OBJECT) {
                context.setResponseOption(responseOption);
                RegistryObjectDAO roDAO = new RegistryObjectDAO(context);
                res = roDAO.getObjects(rs, startIndex, maxResults);
                // HIEOS/BHT (DEBUG):
                log.trace(" -> Object Size: " + res.size());
            } else if ((returnType == ReturnType.LEAF_CLASS)
                    || (returnType == ReturnType.LEAF_CLASS_WITH_REPOSITORY_ITEM)) {
                res = getObjects(context, connection, rs, tableName, responseOption,
                        objectRefs, startIndex, maxResults);
                // HIEOS/BHT (DEBUG):
                log.trace(" -> Object Size: " + res.size());
            } else {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.invalidReturnType",
                        new Object[]{returnType}));
            }
        } catch (SQLException e) {
            throw new RegistryException(e);
        } catch (javax.xml.bind.JAXBException e) {
            throw new RegistryException(e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
                log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), sqle);
            }
        }
        paramHolder.totalResultCount = totalResultCount;
        return res;
    }

    /**
     *
     * @param context
     * @param connection
     * @param rs
     * @param tableName
     * @param responseOption
     * @param objectRefs
     * @param startIndex
     * @param maxResults
     * @return
     * @throws RegistryException
     */
    private List getObjects(ServerRequestContext context, Connection connection, java.sql.ResultSet rs,
            String tableName, ResponseOptionType responseOption, List objectRefs,
            int startIndex, int maxResults)
            throws RegistryException {
        List res = null;

        context.setResponseOption(responseOption);

        if (tableName.equalsIgnoreCase(AssociationDAO.getTableNameStatic())) {
            AssociationDAO associationDAO = new AssociationDAO(context);
            res =
                    associationDAO.getObjects(rs, startIndex, maxResults);
        } else if (tableName.equalsIgnoreCase(
                ClassificationDAO.getTableNameStatic())) {
            ClassificationDAO classificationDAO = new ClassificationDAO(context);
            res =
                    classificationDAO.getObjects(rs, startIndex, maxResults);
        } else if (tableName.equalsIgnoreCase(
                ExternalIdentifierDAO.getTableNameStatic())) {
            ExternalIdentifierDAO externalIdentifierDAO = new ExternalIdentifierDAO(context);
            res =
                    externalIdentifierDAO.getObjects(rs, startIndex, maxResults);
        } else if (tableName.equalsIgnoreCase(
                ExtrinsicObjectDAO.getTableNameStatic())) {
            ExtrinsicObjectDAO extrinsicObjectDAO = new ExtrinsicObjectDAO(context);
            res =
                    extrinsicObjectDAO.getObjects(rs, startIndex, maxResults);
        } else if (tableName.equalsIgnoreCase(
                RegistryPackageDAO.getTableNameStatic())) {
            RegistryPackageDAO registryPackageDAO = new RegistryPackageDAO(context);
            res =
                    registryPackageDAO.getObjects(rs, startIndex, maxResults);
        }

        return res;
    }

    /**
     * Gets the specified objects using specified query and className
     *
     */
    public List getRegistryObjectsMatchingQuery(
            ServerRequestContext context, String query, List queryParams, String tableName)
            throws RegistryException {
        List objects = null;

        try {
            ResponseOption responseOption = bu.queryFac.createResponseOption();
            responseOption.setReturnType(ReturnType.LEAF_CLASS);
            responseOption.setReturnComposedObjects(true);
            objects =
                    getIdentifiablesMatchingQuery(context, query, queryParams, tableName, responseOption);
        } catch (javax.xml.bind.JAXBException e) {
            throw new RegistryException(e);
        }

        return objects;
    }

    /**
     * Gets the specified objects using specified query and className
     *
     */
    public List getIdentifiablesMatchingQuery(
            ServerRequestContext context, String query, List queryParams, String tableName, ResponseOption responseOption)
            throws RegistryException {
        List objects = null;

        List objectRefs = new java.util.ArrayList();
        IterativeQueryParams paramHolder = new IterativeQueryParams(0, -1);
        objects =
                executeSQLQuery(context, query, queryParams, responseOption, tableName,
                objectRefs, paramHolder);
        return objects;
    }

    /**
     * Gets the first object matching specified query.
     * TODO: This is a dangerous query to use and it should eventually be
     *   eliminated.
     */
    public RegistryObjectType getRegistryObjectMatchingQuery(
            ServerRequestContext context, String query, List queryParams, String tableName)
            throws RegistryException {
        RegistryObjectType ro = null;

        List al = getRegistryObjectsMatchingQuery(context, query, queryParams, tableName);
        if (al.size() >= 1) {
            ro = (RegistryObjectType) al.get(0);
        }

        return ro;
    }

    /**
     * Gets the specified object using specified id and className
     *
     */
    public IdentifiableType getIdentifiableMatchingQuery(
            ServerRequestContext context, String query, List queryParams, String tableName, ResponseOption responseOption)
            throws RegistryException {
        IdentifiableType obj = null;

        List al = getIdentifiablesMatchingQuery(context, query, queryParams, tableName, responseOption);

        if (al.size() == 1) {
            obj = (IdentifiableType) al.get(0);
        }

        return obj;
    }

// HEIOS/AMS/BHT Added new method to optimize status update operations.
    /**
     * Return a concrete RegistryObjectType (ExtrinsicObjectType, RegistryPackageType or AssociationType)
     * depending on the "objectType" found in the "RegistryObject" table.
     *
     * @param context Holds the context for request processing.
     * @param ref Holds the object reference that we are interested in.
     * @return A concrete RegistryObjectType (ExtrinsicObjectType, RegistryPackageType or AssociationType).
     * @throws javax.xml.registry.RegistryException
     */
    public RegistryObjectType getRegistryObjectForStatusUpdate(
            ServerRequestContext context, ObjectRefType ref)
            throws RegistryException {
        Connection connection = context.getConnection();
        try {
            RegistryObjectType concreteRegistryObject = null;
            // Look to see if object is in ExtrinisicObject table first ... hedge bets (this is the
            // most probable case.
            boolean exists = this.doesRegistryObjectExist(connection, ref, ExtrinsicObjectDAO.getTableNameStatic());
            if (exists) {
                concreteRegistryObject = bu.rimFac.createExtrinsicObject();
            } else {
                // Did not find in ExtrinsicObject table ... look in RegistryPackage table now.
                exists = this.doesRegistryObjectExist(connection, ref, RegistryPackageDAO.getTableNameStatic());
                if (exists) {
                    concreteRegistryObject = bu.rimFac.createRegistryPackage();
                } else {
                    // Finally, try the Association table.
                    exists = this.doesRegistryObjectExist(connection, ref, AssociationDAO.getTableNameStatic());
                    if (exists) {
                        concreteRegistryObject = bu.rimFac.createAssociation();
                    }
                }
            }
            if (!exists) {
                // BAD -- none found!!!
                throw new RegistryException(
                        "Can not find ExtrinsicObject, RegistryPackage or Association for id = " + ref.getId());
            }
            concreteRegistryObject.setId(ref.getId());  // Just to be in sync.
            return concreteRegistryObject;
        } catch (JAXBException e) {
            throw new RegistryException(e);
        } finally {
            // Nothing to do now.
        }
    }

    // HIEOS/BHT -- added
    /**
     * Return the RegistryObjectType for the given tableName and referenced object.  If not found,
     * return null.
     *
     * @param connection Database connection.
     * @param ref Object reference in question.
     * @param tableName The table name to query.
     * @return The object type if the record is found.  Otherwise, null.
     * @throws RegistryException
     */
    /* REMOVED (BHT) - NO LONGER USED:
    private String getRegistryObjectType(Connection connection, ObjectRefType ref, String tableName) throws RegistryException {
        String objectType = null;
        PreparedStatement stmt = null;
        try {
            String sql = "SELECT objecttype FROM " + tableName + " WHERE id = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, ref.getId());
            log.trace("SQL = " + sql.toString());
            ResultSet rs = stmt.executeQuery();
            boolean exists = rs.next();
            if (exists == true) {
                objectType = rs.getString(1);
            }

        } catch (SQLException e) {
            throw new RegistryException(e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqle) {
                    log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), sqle);
                }

            }
        }
        return RegistryCodedValueMapper.convertObjectType_CodeToValue(objectType);
    }*/

    /**
     * Return true if the registry object exists for the given "tableName".
     *
     * @param connection Database connection.
     * @param ref Object reference in question.
     * @param tableName The table name to query.
     * @return true if found.  Otherwise, false.
     * @throws RegistryException
     */
    // HIEOS (ADDED):
    private boolean doesRegistryObjectExist(Connection connection, ObjectRefType ref, String tableName) throws RegistryException {
        boolean exists = false;
        PreparedStatement stmt = null;
        try {
            String sql = "SELECT id FROM " + tableName + " WHERE id = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, ref.getId());
            log.trace("SQL = " + sql.toString());
            ResultSet rs = stmt.executeQuery();
            exists = rs.next();

        } catch (SQLException e) {
            throw new RegistryException(e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqle) {
                    log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), sqle);
                }
            }
        }
        return exists;
    }

    /**
     * Gets the specified object using specified id and className
     *
     */
    public RegistryObjectType getRegistryObject(
            ServerRequestContext context, String id, String className)
            throws RegistryException {
        RegistryObjectType ro = null;

        try {
            ResponseOption responseOption = bu.queryFac.createResponseOption();
            responseOption.setReturnType(ReturnType.LEAF_CLASS);
            responseOption.setReturnComposedObjects(true);
            ro = (RegistryObjectType) getIdentifiable(context, id, className, responseOption);
        } catch (JAXBException e) {
            throw new RegistryException(e);
        }

        return ro;
    }

    /**
     * Gets the specified object using specified id and className
     *
     */
    public IdentifiableType getIdentifiable(
            ServerRequestContext context, String id, String className, ResponseOption responseOption)
            throws RegistryException {
        IdentifiableType obj = null;

        String tableName = org.freebxml.omar.common.Utility.getInstance().mapTableName(className);
        String sqlQuery = "SELECT * FROM " + tableName + " WHERE id = ?";
        ArrayList queryParams = new ArrayList();
        queryParams.add(id);

        obj =
                getIdentifiableMatchingQuery(context, sqlQuery, queryParams, tableName, responseOption);

        return obj;
    }

    /**
     * Gets the specified object using specified ObjectRef
     *
     */
    public RegistryObjectType getRegistryObject(
            ServerRequestContext context, ObjectRefType ref)
            throws RegistryException {

        return getRegistryObject(context, ref.getId(), "RegistryObject");
    }

    /**
     * Get a HashMap with registry object id as key and owner id as value
     */
    public HashMap getOwnersMap(
            ServerRequestContext context, List ids) throws RegistryException {
        RegistryObjectDAO roDAO = new RegistryObjectDAO(context);

        HashMap ownersMap = roDAO.getOwnersMap(ids);

        return ownersMap;
    }
    /**
     * Updates the idToLidMap in context entries with RegistryObject id as Key and RegistryObject lid as value
     * for each object that matches specified id.
     *
     */
    /* HIEOS (REMOVED):
    public void updateIdToLidMap(ServerRequestContext context, Set ids, String tableName) throws RegistryException {
    if ((ids != null) && (ids.size() >= 0)) {

    Iterator iter = ids.iterator();
    Statement stmt = null;

    try {
    stmt = context.getConnection().createStatement();

    StringBuffer sql = new StringBuffer("SELECT id, lid FROM " + tableName + " WHERE id IN (");
    List existingIdList = new ArrayList();

    // We need to count the number of item in "IN" list.
    // We need to split the a single SQL Strings if it is too long. Some database such as Oracle,
    // does not allow the IN list is too long
    //
    int listCounter = 0;

    while (iter.hasNext()) {
    String id = (String) iter.next();

    if (iter.hasNext() && (listCounter < IdentifiableDAO.identifiableExistsBatchCount)) {
    sql.append("'" + id + "',");
    } else {
    sql.append("'" + id + "')");

    //log.info("sql string=" + sql.toString());
    log.trace("SQL = " + sql.toString());
    ResultSet rs = stmt.executeQuery(sql.toString());

    while (rs.next()) {
    String _id = rs.getString(1);
    String lid = rs.getString(2);
    context.getIdToLidMap().put(_id, lid);
    }

    sql = new StringBuffer("SELECT id, lid FROM " + tableName + " WHERE id IN (");
    listCounter =
    0;
    }

    listCounter++;
    }

    } catch (SQLException e) {
    throw new RegistryException(e);
    } finally {
    try {
    if (stmt != null) {
    stmt.close();
    }

    } catch (SQLException sqle) {
    log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), sqle);
    }

    }
    }
    }*/
    /**
     * Checks each object being deleted to make sure that it does not have any currently existing references.
     * Objects must be fetched from the Cache or Server and not from the RequestContext??
     *
     * @throws ReferencesExistException if references exist to any of the RegistryObject ids specified in roIds
     *
     */
    /* HIEOS (REMOVED):
    public void checkIfReferencesExist(ServerRequestContext context, List roIds) throws RegistryException {
    if (skipReferenceCheckOnRemove) {
    return;
    }

    Iterator iter = roIds.iterator();

    HashMap idToReferenceSourceMap = new HashMap();
    while (iter.hasNext()) {
    String id = (String) iter.next();


    StringBuffer query = new StringBuffer();
    query.append("SELECT id FROM RegistryObject WHERE objectType = ? UNION ");
    query.append("SELECT id FROM ClassificationNode WHERE parent = ? UNION ");
    query.append("SELECT id FROM Classification WHERE classificationNode = ? OR classificationScheme = ? OR classifiedObject = ? UNION ");
    query.append("SELECT id FROM ExternalIdentifier WHERE identificationScheme = ? OR registryObject = ? UNION ");
    query.append("SELECT id FROM Association WHERE associationType = ? OR sourceObject = ? OR targetObject= ?  UNION ");
    query.append("SELECT id FROM AuditableEvent WHERE user_ = ? OR requestId = ? UNION ");
    query.append("SELECT id FROM Organization WHERE parent = ? UNION ");
    query.append("SELECT id FROM Registry where operator = ? UNION ");
    query.append("SELECT id FROM ServiceBinding WHERE service = ? OR targetBinding = ? UNION ");
    query.append("SELECT id FROM SpecificationLink WHERE serviceBinding = ? OR specificationObject = ? UNION ");
    query.append("SELECT id FROM Subscription WHERE selector = ?  UNION ");
    query.append("SELECT s.parent FROM Slot s WHERE s.slotType = '" + BindingUtility.CANONICAL_DATA_TYPE_ID_ObjectRef + "' AND s.value = ?");

    PreparedStatement stmt = null;
    try {
    stmt = context.getConnection().prepareStatement(query.toString());
    stmt.setString(1, id);
    stmt.setString(2, id);
    stmt.setString(3, id);
    stmt.setString(4, id);
    stmt.setString(5, id);
    stmt.setString(6, id);
    stmt.setString(7, id);
    stmt.setString(8, id);
    stmt.setString(9, id);
    stmt.setString(10, id);
    stmt.setString(11, id);
    stmt.setString(12, id);
    stmt.setString(13, id);
    stmt.setString(14, id);
    stmt.setString(15, id);
    stmt.setString(16, id);
    stmt.setString(17, id);
    stmt.setString(18, id);
    stmt.setString(19, id);
    stmt.setString(20, id);
    log.trace("SQL = " + query.toString());  // HIEOS/BHT: (DEBUG)
    ResultSet rs = stmt.executeQuery();
    boolean result = false;

    ArrayList referenceSourceIds = new ArrayList();
    while (rs.next()) {
    String referenceSourceId = rs.getString(1);
    if (!roIds.contains(referenceSourceId)) {
    referenceSourceIds.add(referenceSourceId);
    }

    }

    if (!referenceSourceIds.isEmpty()) {
    idToReferenceSourceMap.put(id, referenceSourceIds);
    }

    } catch (SQLException e) {
    throw new RegistryException(e);
    } finally {
    try {
    if (stmt != null) {
    stmt.close();
    }

    } catch (SQLException sqle) {
    log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), sqle);
    }

    }
    }

    if (!idToReferenceSourceMap.isEmpty()) {
    //At least one ref exists to at least one object so throw exception
    String msg = ServerResourceBundle.getInstance().getString("message.referencesExist");
    msg +=
    "\n" + idToReferenceSourceMap.toString();

    throw new ReferencesExistException(msg);
    }
    }*/
}
