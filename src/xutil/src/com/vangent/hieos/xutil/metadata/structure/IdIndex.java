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
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;

/**
 *
 * @author NIST (adapted).
 */
public class IdIndex {

    private Metadata m;
    private Map<String, OMElement> _object_by_id = null;   // id => OMElement
    private Map<String, Map<String, List<OMElement>>> _object_parts_by_id = null;  // id => HashMap(type => ArrayList(OMElement))   type is Slot, Description, ...
    private XLogMessage log_message = null;

    /**
     *
     * @param log_message
     */
    public void setLogMessage(XLogMessage log_message) {
        this.log_message = log_message;
    }

    /**
     *
     */
    public IdIndex() {
    }

    /**
     *
     * @param m
     * @throws MetadataException
     */
    public void setMetadata(Metadata m) throws MetadataException {
        this.m = m;
        this.parse_objects_by_id(m.getNonObjectRefs());
    }

    /**
     *
     * @param m
     * @throws MetadataException
     */
    public IdIndex(Metadata m) throws MetadataException {
        this.setMetadata(m);
    }

    /**
     *
     * @param id
     * @param identifier_scheme
     * @return
     */
    public String getExternalIdentifierValue(String id, String identifier_scheme) {
        Map<String, List<OMElement>> part_map = object_parts_by_id().get(id);
        if (part_map == null) {
            return null;
        }
        try {
            OMElement obj = m.getObjectById(id);
        } catch (Exception e) {
        }
        List<OMElement> external_identifiers = part_map.get("ExternalIdentifier");
        for (int i = 0; i < external_identifiers.size(); i++) {
            OMElement ei = external_identifiers.get(i);
            OMAttribute id_scheme_att = ei.getAttribute(MetadataSupport.identificationscheme_qname);
            String scheme = id_scheme_att.getAttributeValue();
            if (id_scheme_att != null && id_scheme_att.getAttributeValue().equals(identifier_scheme)) {
                OMAttribute value_att = ei.getAttribute(MetadataSupport.value_qname);
                if (value_att != null) {
                    return value_att.getAttributeValue();
                }
            }
        }
        return null;
    }

    /**
     *
     * @param id
     * @return
     */
    public List<OMElement> getSlots(String id) {
        Map<String, List<OMElement>> part_map = object_parts_by_id().get(id);
        if (part_map == null) {
            return new ArrayList<OMElement>();
        }
        return part_map.get("Slot");
    }

    /**
     *
     * @param id
     * @param name
     * @return
     */
    public OMElement getSlot(String id, String name) {
        List<OMElement> slots = getSlots(id);
        for (OMElement ele : slots) {
            if (ele.getAttributeValue(MetadataSupport.slot_name_qname).equals(name)) {
                return ele;
            }
        }
        return null;
    }

    /**
     *
     * @param id
     * @return
     */
    public List<OMElement> getClassifications(String id) {
        Map<String, List<OMElement>> part_map = object_parts_by_id().get(id);
        if (part_map == null) {
            return new ArrayList<OMElement>();
        }
        return part_map.get("Classification");
    }

    /**
     *
     * @param id
     * @return
     */
    public OMElement getName(String id) {
        Map<String, List<OMElement>> part_map = object_parts_by_id().get(id);
        if (part_map == null) {
            return null;
        }
        List<OMElement> name_list = part_map.get("Name");
        if (name_list.isEmpty()) {
            return null;
        }
        return name_list.get(0);
    }

    /**
     * 
     * @param id
     * @return
     */
    public OMElement getDescription(String id) {
        Map<String, List<OMElement>> part_map = object_parts_by_id().get(id);
        if (part_map == null) {
            return null;
        }
        List<OMElement> name_list = part_map.get("Description");
        if (name_list.isEmpty()) {
            return null;
        }
        return name_list.get(0);
    }

    /**
     *
     * @param id
     * @return
     */
    public String getNameValue(String id) {
        OMElement name_ele = getName(id);
        if (name_ele == null) {
            return null;
        }
        OMElement loc_st = MetadataSupport.firstChildWithLocalName(name_ele, "LocalizedString");
        if (loc_st == null) {
            return null;
        }
        return loc_st.getAttributeValue(MetadataSupport.value_qname);
    }

    /**
     *
     * @param id
     * @return
     */
    public String getDescriptionValue(String id) {
        OMElement desc_ele = getDescription(id);
        if (desc_ele == null) {
            return null;
        }
        OMElement loc_st = MetadataSupport.firstChildWithLocalName(desc_ele, "LocalizedString");
        if (loc_st == null) {
            return null;
        }
        return loc_st.getAttributeValue(MetadataSupport.value_qname);
    }

    /**
     *
     * @param id
     * @return
     */
    public List<OMElement> getExternalIdentifiers(String id) {
        Map<String, List<OMElement>> part_map = object_parts_by_id().get(id);
        if (part_map == null) {
            return new ArrayList<OMElement>();
        }
        return part_map.get("ExternalIdentifier");
    }

    /**
     *
     * @return
     */
    public String getSubmissionSetUniqueId() {
        OMElement ss = m.getSubmissionSet();
        String ss_id = ss.getAttributeValue(MetadataSupport.id_qname);
        return getExternalIdentifierValue(ss_id, MetadataSupport.XDSSubmissionSet_uniqueid_uuid);
    }

    /**
     *
     * @return
     */
    public String getSubmissionSetPatientId() {
        OMElement ss = m.getSubmissionSet();
        String ss_id = ss.getAttributeValue(MetadataSupport.id_qname);
        return getExternalIdentifierValue(ss_id, MetadataSupport.XDSSubmissionSet_patientid_uuid);
    }

    /**
     *
     * @return
     */
    public String getSubmissionSetSourceId() {
        OMElement ss = m.getSubmissionSet();
        String ss_id = ss.getAttributeValue(MetadataSupport.id_qname);
        Map<String, List<OMElement>> by_att_type = object_parts_by_id().get(ss_id);
        //HashMap by_att_type = (HashMap) object_parts_by_id().get(ss_id);
        List<OMElement> slots = by_att_type.get("Slot");
        String source_id = get_slot_value(slots, "sourceId");
        return source_id;
    }

    /**
     * 
     * @param slots
     * @param slotName
     * @return
     */
    String get_slot_value(List<OMElement> slots, String slotName) {
        for (int i = 0; i < slots.size(); i++) {
            OMElement slot = slots.get(i);
            String name = slot.getAttributeValue(MetadataSupport.name_qname);
            if (name == null) {
                continue;
            }
            if (!name.equals(slotName)) {
                continue;
            }
            OMElement valueList = MetadataSupport.firstChildWithLocalName(slot, "ValueList");
            if (valueList == null) {
                continue;
            }
            OMElement value = MetadataSupport.firstChildWithLocalName(valueList, "Value");
            if (value == null) {
                continue;
            }
            return value.getText();
        }
        return null;
    }

    /**
     *
     * @param id
     * @return
     */
    public OMElement getObjectById(String id) {
        return object_by_id().get(id);
    }

    /**
     *
     * @param id
     * @return
     */
    public String getIdentifyingString(String id) {
        Object obj = object_by_id().get(id);
        if (obj == null) {
            return "<Unknown object " + id + ">";
        }

        OMElement ele;
        if (obj instanceof OMElement) {
            ele = (OMElement) obj;
        } else {
            return "Unknown object type for id " + id + ">";
        }

        return m.getIdentifyingString(ele);
    }

    /**
     *
     * @param id
     * @return
     */
    public String getObjectTypeById(String id) {
        OMElement submission_set = m.getSubmissionSet();
        OMElement obj = getObjectById(id);
        if (obj == null) {
            return null;
        }
        String name = obj.getLocalName();
        if (name.equals("RegistryPackage")) {
            if (obj == submission_set) {
                return "SubmissionSet";
            }
            return "Folder";
        }
        return name;
    }

    /**
     * 
     * @param objects
     * @throws MetadataException
     */
    private void parse_objects_by_id(List<OMElement> objects) throws MetadataException {
        for (int i = 0; i < objects.size(); i++) {
            OMElement obj = (OMElement) objects.get(i);
            parse_object_by_id(obj);
        }
    }

    /**
     *
     * @param it
     * @return
     */
    private int count_iterator(Iterator it) {
        int i = 0;
        for (; it.hasNext();) {
            it.next();
            i++;
        }
        return i;
    }

    /**
     *
     * @return
     */
    private Map<String, OMElement> object_by_id() {
        if (_object_by_id == null) {
            _object_by_id = new HashMap<String, OMElement>();   // id => OMElement
        }
        return _object_by_id;
    }

    /**
     *
     * @return
     */
    private Map<String, Map<String, List<OMElement>>> object_parts_by_id() {
        if (_object_parts_by_id == null) {
            _object_parts_by_id = new HashMap<String, Map<String, List<OMElement>>>();   // id => HashMap(type => ArrayList(OMElement))   type is Slot, Description, ...
        }
        return _object_parts_by_id;
    }

    /**
     *
     * @param obj
     * @throws MetadataException
     */
    private void parse_object_by_id(OMElement obj) throws MetadataException {
        String id = obj.getAttributeValue(MetadataSupport.id_qname);
        if (id == null) {
            return;
        }
        if (id.length() == 0) {
            return;
        }
        if (log_message != null) {
            try {
                log_message.addOtherParam("ii object to parse", obj.getLocalName() + " " + "id=" + id + " " + count_iterator(obj.getChildElements()) + " minor elements");
            } catch (Exception e) {
            }
        }

        // ebxmlrr gens ObjectRefs even when real object is returned
        OMElement existing = object_by_id().get(id);
        if (existing != null) {
            String existing_type = existing.getLocalName();
            if (existing_type.equals("ObjectRef")) {
                object_by_id().put(id, obj);
            }
        } else {
            object_by_id().put(id, obj);
        }
        Map<String, List<OMElement>> parts = new HashMap<String, List<OMElement>>();
        List<OMElement> name = new ArrayList<OMElement>();
        List<OMElement> description = new ArrayList<OMElement>();
        List<OMElement> slots = new ArrayList<OMElement>();
        List<OMElement> external_identifiers = new ArrayList<OMElement>();
        List<OMElement> classifications = new ArrayList<OMElement>();
        for (Iterator<OMNode> it = obj.getChildren(); it.hasNext();) {
            OMNode part_n = it.next();
            if (!(part_n instanceof OMElement)) {
                continue;
            }
            OMElement part = (OMElement)part_n;
            String part_type = part.getLocalName();
            if (log_message != null) {
                try {
                    log_message.addOtherParam("part", part.toString());
                } catch (Exception e) {
                }
            }
            if (part_type.equals("Name")) {
                name.add(part);
            } else if (part_type.equals("Description")) {
                description.add(part);
            } else if (part_type.equals("Slot")) {
                slots.add(part);
            } else if (part_type.equals("ExternalIdentifier")) {
                external_identifiers.add(part);
                if (log_message != null) {
                    try {
                        log_message.addOtherParam("adding", external_identifiers.size() + " eis so far");
                    } catch (Exception e) {
                    }
                }
            } else if (part_type.equals("Classification")) {
                classifications.add(part);
            } else if (part_type.equals("ObjectRef")); else;
        }
        parts.put("Name", name);
        parts.put("Description", description);
        parts.put("Slot", slots);
        parts.put("ExternalIdentifier", external_identifiers);
        parts.put("Classification", classifications);
        parts.put("Element", singleton(obj));
        object_parts_by_id().put(id, parts);
    }

    /**
     * 
     * @return
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("Metadata Index:\n");
        for (String id : object_parts_by_id().keySet()) {
            OMElement obj = null;
            try {
                obj = m.getObjectById(id);
            } catch (Exception e) {
                break;
            }
            buf.append(id).append("(").append(obj.getLocalName()).append(")\n");
            Map<String, List<OMElement>> type_map = object_parts_by_id().get(id);
            for (String type : type_map.keySet()) {
                buf.append("\t").append(type).append("\n");
                List<OMElement> elements = type_map.get(type);
                for (OMElement element : elements) {
                    buf.append("\t\t").append(element.getLocalName());
                    if (element.getLocalName().equals("ExternalIdentifier")) {
                        buf.append(" idscheme=").append(element.getAttributeValue(MetadataSupport.identificationscheme_qname));
                        buf.append(" value=").append(element.getAttributeValue(MetadataSupport.value_qname));
                    }
                    buf.append("\n");
                }
            }
        }
        return buf.toString();
    }

    /**
     *
     * @param o
     * @return
     */
    private List<OMElement> singleton(OMElement o) {
        List<OMElement> al = new ArrayList<OMElement>();
        al.add(o);
        return al;
    }
}
