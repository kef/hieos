/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/query/QueryManagerImpl.java,v 1.78 2008/02/28 17:11:40 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.query;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Calendar;
import javax.xml.registry.UnsupportedCapabilityException;
import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.spi.QueryManager;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.common.spi.RequestContext;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.common.IterativeQueryParams;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequestType;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponseType;
import org.oasis.ebxml.registry.bindings.rim.QueryExpressionType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.UserType;

/*
 * HIEOS (CHANGE) - removed code related to repository manager, query filters,
 *                  plugins, CMS, federated queries, and authentication.
 */
/**
 * Implements the QueryManager interface for ebXML Registry as defined by ebRS spec.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 * @author Modified extensively by Bernie Thuman
 */
public class QueryManagerImpl implements QueryManager {

    private static QueryManagerImpl instance = null;

    /**
     * @directed
     */
    private org.freebxml.omar.server.query.sql.SQLQueryProcessor sqlQueryProcessor =
            org.freebxml.omar.server.query.sql.SQLQueryProcessor.getInstance();

    /**
     * 
     */
    protected QueryManagerImpl() {
    }

    /**
     *
     * @return
     */
    public synchronized static QueryManagerImpl getInstance() {
        if (instance == null) {
            instance = new QueryManagerImpl();
        }
        return instance;
    }

    /**
     * submitAdhocQuery
     */
    public AdhocQueryResponseType submitAdhocQuery(RequestContext context)
            throws RegistryException {
        org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse ahqr = null;
        context = ServerRequestContext.convert(context);
        AdhocQueryRequestType req = (AdhocQueryRequestType) ((ServerRequestContext) context).getCurrentRegistryRequest();
        org.oasis.ebxml.registry.bindings.query.ResponseOptionType responseOption = req.getResponseOption();

        UserType user = ((ServerRequestContext) context).getUser();

        //The result of the query
        RegistryObjectListType rolt = null;
        try {
            ahqr = null;
            int startIndex = req.getStartIndex().intValue();
            int maxResults = req.getMaxResults().intValue();
            IterativeQueryParams paramHolder = new IterativeQueryParams(startIndex, maxResults);
            org.oasis.ebxml.registry.bindings.rim.AdhocQueryType adhocQuery = req.getAdhocQuery();
            QueryExpressionType queryExp = adhocQuery.getQueryExpression();
            String queryLang = queryExp.getQueryLanguage();
            if (queryLang.equals(BindingUtility.CANONICAL_QUERY_LANGUAGE_ID_SQL_92)) {
                String queryStr = (String) queryExp.getContent().get(0);
                queryStr = replaceSpecialVariables(user, queryStr);
                rolt = sqlQueryProcessor.executeQuery((ServerRequestContext) context, user,
                        queryStr, responseOption, paramHolder);
                ahqr = BindingUtility.getInstance().queryFac.createAdhocQueryResponse();
                ahqr.setRegistryObjectList(rolt);
                ahqr.setStatus(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);
                ahqr.setStartIndex(BigInteger.valueOf(paramHolder.startIndex));
                ahqr.setTotalResultCount(BigInteger.valueOf(paramHolder.totalResultCount));
            } else {
                throw new UnsupportedCapabilityException(
                        "Unsupported Query Language: ClassificationNode id: " + queryLang);
            }
        } catch (RegistryException e) {
            ((ServerRequestContext) context).rollback();
            throw e;
        } catch (Exception e) {
            ((ServerRequestContext) context).rollback();
            throw new RegistryException(e);
        }
        ((ServerRequestContext) context).setQueryResults(ahqr.getRegistryObjectList().getIdentifiable());
        ((ServerRequestContext) context).commit();
        ahqr.setRequestId(req.getId());
        return ahqr;
    }

    /**
     * Replaces special environment variables within specified query string.
     */
    private String replaceSpecialVariables(UserType user, String query) {
        String newQuery = query;

        //Replace $currentUser 
        if (user != null) {
            newQuery = newQuery.replaceAll("\\$currentUser", "'" + user.getId() + "'");
        }

        //Replace $currentTime       
        Timestamp currentTime = new Timestamp(Calendar.getInstance().getTimeInMillis());

        //??The timestamp is being truncated to work around a bug in PostgreSQL 7.2.2 JDBC driver
        String currentTimeStr = currentTime.toString().substring(0, 19);
        newQuery = newQuery.replaceAll("\\$currentTime", currentTimeStr);
        return newQuery;
    }
}
