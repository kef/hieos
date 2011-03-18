/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2008-2009 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.xcpd.correlationcache.model;

import java.util.Date;

/**
 *
 * @author Bernie Thuman
 */
public class CorrelationCacheEntry {

    String id;
    String localPatientId;
    String localHomeCommunityId;
    String remotePatientId;
    String remoteHomeCommunityId;
    Date lastUpdatedTime;
    Date expirationTime;
    char status;

    /**
     *
     * @return
     */
    public Date getExpirationTime() {
        return expirationTime;
    }

    /**
     *
     * @param expirationTime
     */
    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }

    /**
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return
     */
    public Date getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    /**
     *
     * @param lastUpdatedTime
     */
    public void setLastUpdatedTime(Date lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    /**
     *
     * @return
     */
    public String getLocalHomeCommunityId() {
        return localHomeCommunityId;
    }

    /**
     *
     * @param localHomeCommunityId
     */
    public void setLocalHomeCommunityId(String localHomeCommunityId) {
        this.localHomeCommunityId = localHomeCommunityId;
    }

    /**
     *
     * @return
     */
    public String getLocalPatientId() {
        return localPatientId;
    }

    /**
     *
     * @param localPatientId
     */
    public void setLocalPatientId(String localPatientId) {
        this.localPatientId = localPatientId;
    }

    /**
     *
     * @return
     */
    public String getRemoteHomeCommunityId() {
        return remoteHomeCommunityId;
    }

    /**
     *
     * @param remoteHomeCommunityId
     */
    public void setRemoteHomeCommunityId(String remoteHomeCommunityId) {
        this.remoteHomeCommunityId = remoteHomeCommunityId;
    }

    /**
     *
     * @return
     */
    public String getRemotePatientId() {
        return remotePatientId;
    }

    /**
     *
     * @param remotePatientId
     */
    public void setRemotePatientId(String remotePatientId) {
        this.remotePatientId = remotePatientId;
    }

    /**
     *
     * @return
     */
    public char getStatus() {
        return status;
    }

    /**
     *
     * @param status
     */
    public void setStatus(char status) {
        this.status = status;
    }

    /**
     * 
     * @return
     */
    public String getVitals() {
        String uniqueId = this.getId();
        String result = "";
        result = "(id=" + (uniqueId == null ? "NULL" : uniqueId) +
                ",lpid=" + this.getLocalPatientId() +
                ",lhid=" + this.getLocalHomeCommunityId() +
                ",rpid=" + this.getRemotePatientId() +
                ",rhid=" + this.getRemoteHomeCommunityId() + ")";
        return result;
    }
}
