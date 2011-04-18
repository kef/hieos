/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2010 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.atna.arr.support;

import java.util.ArrayList;
import java.util.List;


/**
 *
 *  @author Adeola Odunlami
 */
public class ATNAParticipantObject {
    private String uniqueID;
    private Integer typeCode;
    private Integer typeCodeRole;
    private Integer dataLifeCycle;
    private ATNACodedValue idTypeCode;
    private String sensitivity;
    private String id;
    private String name;
    private byte[] query;
    private List<ATNATypeValue> details; 

    /**
     * @return the typeCode
     */
    public Integer getTypeCode() {
        return typeCode;
    }

    /**
     * @param typeCode the typeCode to set
     */
    public void setTypeCode(Integer typeCode) {
        this.typeCode = typeCode;
    }

    /**
     * @return the typeCodeRole
     */
    public Integer getTypeCodeRole() {
        return typeCodeRole;
    }

    /**
     * @param typeCodeRole the typeCodeRole to set
     */
    public void setTypeCodeRole(Integer typeCodeRole) {
        this.typeCodeRole = typeCodeRole;
    }

    /**
     * @return the dataLifeCycle
     */
    public Integer getDataLifeCycle() {
        return dataLifeCycle;
    }

    /**
     * @param dataLifeCycle the dataLifeCycle to set
     */
    public void setDataLifeCycle(Integer dataLifeCycle) {
        this.dataLifeCycle = dataLifeCycle;
    }

    /**
     * @return the idTypeCode
     */
    public ATNACodedValue getIdTypeCode() {
        return idTypeCode;
    }

    /**
     * @param idTypeCode the idTypeCode to set
     */
    public void setIdTypeCode(ATNACodedValue idTypeCode) {
        this.idTypeCode = idTypeCode;
    }

    /**
     * @return the sensitivity
     */
    public String getSensitivity() {
        return sensitivity;
    }

    /**
     * @param sensitivity the sensitivity to set
     */
    public void setSensitivity(String sensitivity) {
        this.sensitivity = sensitivity;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the query
     */
    public byte[] getQuery() {
        return query;
    }

    /**
     * @param query the query to set
     */
    public void setQuery(byte[] query) {
        this.query = query;
    }

    /**
     * @return the details
     */
    public List<ATNATypeValue> getDetails() {
        return details;
    }

    /**
     * @param details the details to set
     */
    public void setDetails(List<ATNATypeValue> details) {
        this.details = details;
    }
  
    /**
     * Gets the value of the participantObject Details property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the participantObject Details property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDetailsList().add(newItem);
     * </pre>
     */
    public List<ATNATypeValue> getDetailsList() {
        if (getDetails() == null) {
            setDetails(new ArrayList<ATNATypeValue>());
        }
        return this.getDetails();
    }

    /**
     * @return the uniqueID
     */
    public String getUniqueID() {
        return uniqueID;
    }

    /**
     * @param uniqueID the uniqueID to set
     */
    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

}
