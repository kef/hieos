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
package com.vangent.hieos.hl7v3util.model.subject;

/**
 *
 * @author Bernie Thuman
 */
public class Address {

    private String internalId = null;
    private String streetAddressLine1;
    private String streetAddressLine2;
    private String streetAddressLine3;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    /**
     * 
     * @return
     */
    public String getInternalId() {
        return internalId;
    }

    /**
     *
     * @param internalId
     */
    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    /**
     *
     * @return
     */
    public String getCity() {
        return city;
    }

    /**
     *
     * @param city
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     *
     * @return
     */
    public String getCountry() {
        return country;
    }

    /**
     *
     * @param country
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     *
     * @return
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     *
     * @param postalCode
     */
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    /**
     * 
     * @return
     */
    public String getState() {
        return state;
    }

    /**
     *
     * @param state
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     *
     * @return
     */
    public String getStreetAddressLine1() {
        return streetAddressLine1;
    }

    /**
     *
     * @param streetAddressLine1
     */
    public void setStreetAddressLine1(String streetAddressLine1) {
        this.streetAddressLine1 = streetAddressLine1;
    }

    /**
     *
     * @return
     */
    public String getStreetAddressLine2() {
        return streetAddressLine2;
    }

    /**
     *
     * @param streetAddressLine2
     */
    public void setStreetAddressLine2(String streetAddressLine2) {
        this.streetAddressLine2 = streetAddressLine2;
    }

    /**
     *
     * @return
     */
    public String getStreetAddressLine3() {
        return streetAddressLine3;
    }

    /**
     *
     * @param streetAddressLine3
     */
    public void setStreetAddressLine3(String streetAddressLine3) {
        this.streetAddressLine3 = streetAddressLine3;
    }
}
