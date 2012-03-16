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
package com.vangent.hieos.xutil.metadata.structure;

import com.vangent.hieos.xutil.exception.MetadataException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author thumbe
 */
public class SqParams {

    HashMap<String, Object> params;

    /**
     *
     * @param Bernie Thuman (Adapted from NIST).
     */
    public SqParams(HashMap<String, Object> params) {
        this.params = params;
    }

    /**
     *
     */
    public SqParams() {
        params = new HashMap<String, Object>();
    }

    /**
     * 
     * @return
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append("SqParms\n");
        for (Iterator<String> it = params.keySet().iterator(); it.hasNext();) {
            String name = it.next();
            Object value = params.get(name);
            buf.append("\t");
            buf.append(name);
            buf.append(" => ");
            buf.append(value);
            buf.append("\n");
        }

        return buf.toString();
    }

    /**
     *
     * @return
     */
    public int size() {
        return params.size();
    }

    /**
     *
     * @param name
     * @param value
     */
    public void addParm(String name, Object value) {
        params.put(name, value);
    }

    /**
     *
     * @param parmName
     * @return
     */
    public boolean hasParm(String parmName) {
        return params.containsKey(parmName);
    }

    /**
     *
     * @param parmName
     * @return
     */
    public Object getParm(String parmName) {
        return params.get(parmName);
    }

    /**
     *
     * @param name
     * @param value
     */
    public void addStringParm(String name, String value) {
        addParm(name, value);
    }

    /**
     *
     * @param name
     * @param value
     */
    public void addIntParm(String name, Integer value) {
        addParm(name, value);
    }

    /**
     *
     * @param name
     * @param value
     */
    public void addIntParm(String name, BigInteger value) {
        addParm(name, value);
    }

    /**
     *
     * @param name
     * @param values
     */
    public void addListParm(String name, List<String> values) {
        addParm(name, values);
    }

    /**
     *
     * @param name
     * @param onlyValue
     */
    public void addListParm(String name, String onlyValue) {
        List<String> values = new ArrayList<String>();
        values.add(onlyValue);
        addParm(name, values);
    }

    /**
     *
     * @param name
     * @return
     */
    public String getStringParm(String name) {
        Object o = params.get(name);
        if (o instanceof String) {
            return (String) o;
        }
        return null;
    }

    /**
     *
     * @param name
     * @return
     * @throws MetadataException
     */
    public String getIntParm(String name) throws MetadataException {
        Object o = params.get(name);
        if (o == null) {
            return null;
        }
        if (o instanceof Integer) {
            Integer i = (Integer) o;
            return i.toString();
        }
        if (o instanceof BigInteger) {
            BigInteger i = (BigInteger) o;
            return i.toString();
        } else {
            throw new MetadataException("Parameter " + name + " - expecting a number but got " + o.getClass().getName() + " instead");
        }
    }

    /**
     *
     * @param name
     * @return
     * @throws XdsInternalException
     * @throws MetadataException
     */
    public List<String> getListParm(String name) throws XdsInternalException, MetadataException {
        Object o = params.get(name);
        if (o == null) {
            return null;
        }
        if (o instanceof List) {
            List<String> a = (List<String>) o;
            if (a.isEmpty()) {
                throw new MetadataException("Parameter " + name + " is an empty list");
            }
            return a;
        }
        throw new XdsInternalException("getListParm(): bad type = " + o.getClass().getName());
    }

    /**
     *
     * @param name
     * @return
     * @throws XdsInternalException
     * @throws MetadataException
     */
    public List<Object> getAndorParm(String name) throws XdsInternalException, MetadataException {
        Object o = params.get(name);
        if (o == null) {
            return null;
        }
        if (o instanceof ArrayList) {
            List<Object> a = (List<Object>) o;
            if (a.isEmpty()) {
                throw new MetadataException("Parameter " + name + " is an empty list");
            }
            return a;
        }
        throw new XdsInternalException("getAndorParm(): bad type = " + o.getClass().getName());
    }

    /**
     *
     * @param name
     * @return
     * @throws MetadataException
     * @throws XdsInternalException
     */
    public SQCodedTerm getCodedParm(String name) throws MetadataException, XdsInternalException {
        Object o = params.get(name);
        if (o == null) {
            return null;
        }
        if (o instanceof SQCodedTerm) {
            SQCodedTerm term = (SQCodedTerm) o;
            if (term.isEmpty()) {
                throw new MetadataException("Parameter " + name + " is empty");
            }
            return term;
        }

        throw new XdsInternalException("getCodedParm(): bad type = " + o.getClass().getName());
    }
}
