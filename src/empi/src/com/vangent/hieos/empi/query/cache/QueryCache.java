/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2013 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.empi.query.cache;

import com.vangent.hieos.empi.exception.EMPIException;
import com.vangent.hieos.subjectmodel.Subject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author Bernie Thuman
 */
// FIXME: THIS IS A TEMPORARY IMPLEMENTATION (IN MEMORY CACHE).
public class QueryCache {

    private static QueryCache _instance = null;
    private Map<String, QueryCacheItem> cache = new HashMap<String, QueryCacheItem>();
    private Map<String, List<QueryCacheItem>> cacheByQueryId = new HashMap<String, List<QueryCacheItem>>();

    /**
     *
     * @return
     */
    public synchronized static QueryCache getInstance() {
        if (_instance == null) {
            _instance = new QueryCache();
        }
        return _instance;
    }

    /**
     *
     */
    private QueryCache() {
        // Do not allow calling from the outside.
    }

    /**
     * Add subjects to cache and return unique cache key (continuation pointer id).
     *
     * @param queryId
     * @param subjects
     * @param incrementQuantity
     * @return Continuation pointer ID
     */
    public synchronized String addSubjectsToCache(String queryId, List<Subject> subjects, int incrementQuantity) throws EMPIException {
        // Cancel any existing query that may be running under the same query id.
        this.cancelQuery(queryId);

        // Generate unique id for cache entry.
        String cacheItemKey = UUID.randomUUID().toString();

        // Create cache item.
        QueryCacheItem cacheItem = new QueryCacheItem();
        cacheItem.setKey(cacheItemKey);
        cacheItem.setSubjects(subjects);
        cacheItem.setIncrementQuantity(incrementQuantity);

        // Add to cache
        cache.put(cacheItemKey, cacheItem);

        // Add to cache (by query id).
        List<QueryCacheItem> cacheItemsByQueryId = cacheByQueryId.get(queryId);
        if (cacheItemsByQueryId == null) {
            cacheItemsByQueryId = new ArrayList<QueryCacheItem>();
        }
        cacheItemsByQueryId.add(cacheItem);
        cacheByQueryId.put(queryId, cacheItemsByQueryId);
        return cacheItemKey;
    }

    /**
     *
     * @param cacheItemKey
     * @return
     * @throws EMPIException
     */
    public synchronized List<Subject> getNextIncrement(String cacheItemKey) throws EMPIException {
        QueryCacheItem cacheItem = cache.get(cacheItemKey);
        if (cacheItem == null) {
            throw new EMPIException("Continuation pointer " + cacheItemKey + " not known to the EMPI");
        } else {
            return cacheItem.getNextIncrement();
        }
    }

    /**
     * 
     * @param queryId
     * @throws EMPIException
     */
    public synchronized void cancelQuery(String queryId) throws EMPIException {
        // Add to cache (by query id).
        List<QueryCacheItem> cacheItemsByQueryId = cacheByQueryId.get(queryId);
        if (cacheItemsByQueryId == null) {
            //throw new EMPIException("Query id" + queryId + " not known to the EMPI");
            return;  // Go silent - ignore errononeous cancelations.
        }
        // Go through the list and remove items from the cache.
        for (QueryCacheItem cacheItem : cacheItemsByQueryId) {
            cache.remove(cacheItem.getKey());
        }
        cacheByQueryId.remove(queryId);
    }

    /**
     * 
     */
    public synchronized void flushExpiredCacheItems() {
        // TODO: Implement
    }
}
