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
import com.vangent.hieos.xutil.exception.MetadataValidationException;
import com.vangent.hieos.xutil.exception.NoMetadataException;
import com.vangent.hieos.xutil.exception.NoSubmissionSetException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.xml.Util;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;
import com.vangent.hieos.xutil.hl7.date.Hl7Date;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.log4j.Logger;

/**
 *
 * @author thumbe
 */
public class Metadata {

    /**
     *
     */
    protected OMFactory fac;
    boolean grok_metadata = true;
    private final static Logger logger = Logger.getLogger(Metadata.class);
    /**
     * List of IHE association types:
     */
    public static final List<String> iheAssocTypes = new ArrayList<String>() {

        {
            add("APND");
            add("XFRM");
            add("RPLC");
            add("XFRM_RPLC");
            add("signs");
        }
    };
    OMElement metadata;
    // wrapper
    OMElement wrapper;   // current metadata document being parsed
    ArrayList<OMElement> wrappers;  // the collection of all metadata documents included in current tables
    ArrayList<OMElement> extrinsicObjects = null;
    ArrayList<OMElement> folders = null;
    OMElement submissionSet = null;
    ArrayList<OMElement> submissionSets = null;
    ArrayList<OMElement> registryPackages = null;
    ArrayList<OMElement> associations = null;
    ArrayList<OMElement> objectRefs = null;
    ArrayList<OMElement> classifications = null;
    ArrayList<OMElement> allObjects = null;
    ArrayList objectsToDeprecate = null;
    ArrayList objectsReferenced = null;
    HashMap<String, ArrayList> classificationsOfId = null;
    boolean version2;
    OMElement metadataDup = null;  // both of these are set by dup_wrapper which is used by metadata_copy
    OMElement wrapperDup = null;
    boolean mustDup = false;
    int idAllocation = 0;
    IdIndex idIndex = null;

    /**
     *
     * @param metadata
     * @throws MetadataException
     * @throws MetadataValidationException
     */
    public Metadata(OMElement metadata) throws MetadataException, MetadataValidationException {
        this.metadata = metadata;
        runParser();
    }

    /**
     *
     * @param metadata_file
     * @param parse
     * @throws XdsInternalException
     * @throws MetadataException
     * @throws MetadataValidationException
     */
    public Metadata(File metadata_file, boolean parse) throws XdsInternalException, MetadataException, MetadataValidationException {
        metadata = Util.parse_xml(metadata_file);
        wrapper = null;
        wrappers = new ArrayList();
        if (parse) {
            wrapper = find_metadata_wrapper();
            wrappers.add(wrapper);
            parse(false);
        } else {
            init();
        }
    }

    /**
     *
     */
    public Metadata() {
        init();
        this.setGrokMetadata(false);
    }

    /**
     * 
     * @return
     */
    private String allocate_id() {
        idAllocation += 1;
        return ("ID_" + String.valueOf(this.hashCode()) + "_" + idAllocation);
    }

    /**
     *
     */
    public void removeDuplicates() {
        removeDuplicates(extrinsicObjects);
        removeDuplicates(folders);
        removeDuplicates(submissionSets);
        removeDuplicates(registryPackages);
        removeDuplicates(associations);
        removeDuplicates(objectRefs);
        removeDuplicates(classifications);

        removeFromObjectRefs(extrinsicObjects);
        removeFromObjectRefs(registryPackages);
        removeFromObjectRefs(associations);
        removeFromObjectRefs(classifications);

        allObjects = new ArrayList<OMElement>();
        allObjects.addAll(extrinsicObjects);
        allObjects.addAll(registryPackages);
        allObjects.addAll(associations);
        allObjects.addAll(classifications);
        allObjects.addAll(objectRefs);
    }

    /**
     *
     * @param set
     */
    private void removeDuplicates(ArrayList<OMElement> set) {
        boolean running = true;
        while (running) {
            running = false;
            for (int targetI = 0; targetI < set.size(); targetI++) {
                OMElement target = set.get(targetI);
                String targetId = id(target);
                for (int i = targetI + 1; i < set.size(); i++) {
                    OMElement it = set.get(i);
                    if (targetId.equals(id(it))) {
                        set.remove(i);
                        running = true;
                        break;
                    }
                }
            }
        }
    }

    /**
     *
     * @param set
     */
    private void removeFromObjectRefs(ArrayList<OMElement> set) {
        for (int i = 0; i < set.size(); i++) {
            String id = id(set.get(i));
            boolean restart = true;
            while (restart) {
                restart = false;
                for (int j = 0; j < objectRefs.size(); j++) {
                    if (id.equals(id(objectRefs.get(j)))) {
                        objectRefs.remove(j);
                        restart = true;
                        break;
                    }
                }
            }
        }
    }

    /**
     * Return true if the metadata only includes object references.  Otherwise, return false.
     *
     * @return Boolean result.
     */
    public boolean isObjectRefsOnly() {
        return submissionSets.size() == 0 && extrinsicObjects.size() == 0 && folders.size() == 0 && associations.size() == 0 && classifications.size() == 0 && objectRefs.size() != 0;
    }

    /**
     * Return list of approvable object (extrinsicObjects and registryPackages) identifiers.
     *
     * @return The list of approvable objects.
     */
    public ArrayList getApprovableObjectIds() {
        ArrayList o = new ArrayList();
        o.addAll(this.extrinsicObjects);
        o.addAll(this.registryPackages);
        return this.getObjectIds(o);
    }

    /**
     * Go through all objects in metadata and only maintain those objects that contain
     * an id in the passed in list.
     *
     * @param ids The list of ids to maintain in the metadata.
     */
    public void filter(ArrayList<String> ids) {
        submissionSets = filter(submissionSets, ids);
        extrinsicObjects = filter(extrinsicObjects, ids);
        folders = filter(folders, ids);
        associations = filter(associations, ids);
        allObjects = filter(allObjects, ids);
    }

    /**
     *
     * @param objects
     * @param ids
     * @return
     */
    private ArrayList<OMElement> filter(ArrayList<OMElement> objects, ArrayList<String> ids) {
        ArrayList<OMElement> out = new ArrayList<OMElement>();
        for (OMElement object : objects) {
            String id = id(object);
            if (ids.contains(id)) {
                out.add(object);
            }
        }
        return out;
    }

    /**
     *
     * @return
     */
    public ArrayList<OMElement> getNonObjectRefs() {
        ArrayList<OMElement> objs = new ArrayList<OMElement>();
        objs.addAll(this.submissionSets);
        objs.addAll(this.folders);
        objs.addAll(this.extrinsicObjects);
        objs.addAll(this.associations);
        objs.addAll(this.classifications);
        return objs;

    }

    /**
     *
     * @return
     */
    private ArrayList<ArrayList<OMElement>> getMetadataContainers() {
        ArrayList<ArrayList<OMElement>> containers = new ArrayList<ArrayList<OMElement>>();
        containers.add(extrinsicObjects);
        containers.add(folders);
        containers.add(submissionSets);
        containers.add(associations);
        containers.add(objectRefs);
        containers.add(classifications);
        containers.add(allObjects);
        containers.add(registryPackages);
        return containers;
    }

    /**
     *
     * @return
     */
    private OMNamespace getCurrentNamespace() {
        if (version2) {
            return MetadataSupport.ebRIMns2;
        }
        return MetadataSupport.ebRIMns3;
    }

    /**
     *
     * @param x
     */
    public void setGrokMetadata(boolean x) {
        grok_metadata = x;
    }

    /**
     *
     * @param metadata
     */
    public void setMetadata(OMElement metadata) {
        this.metadata = metadata;
        init();
    }

    /**
     *
     * @throws MetadataException
     * @throws MetadataValidationException
     */
    public void runParser() throws MetadataException, MetadataValidationException {
        wrapper = find_metadata_wrapper();
        if (wrappers == null) {
            wrappers = new ArrayList<OMElement>();
        }
        wrappers.add(wrapper);
        parse(false);
    }

    /**
     *
     * @param m
     * @throws MetadataException
     * @throws MetadataValidationException
     */
    public void addMetadata(Metadata m) throws MetadataException, MetadataValidationException {
        if (m.getRoot() != null) {
            addMetadata(m.getRoot(), false);
        }
    }

    /**
     *
     * @param m
     * @param discard_duplicates
     * @throws MetadataException
     * @throws MetadataValidationException
     */
    public void addMetadata(Metadata m, boolean discard_duplicates) throws MetadataException, MetadataValidationException {
        addMetadata(m.getRoot(), discard_duplicates);
    }

    /**
     *
     * @param metadata
     * @throws MetadataException
     * @throws MetadataValidationException
     */
    public void addMetadata(OMElement metadata) throws MetadataException, MetadataValidationException {
        addMetadata(metadata, false);
    }

    /**
     *
     * @param metadata
     * @param discard_duplicates
     * @throws MetadataException
     * @throws MetadataValidationException
     */
    public void addMetadata(OMElement metadata, boolean discard_duplicates) throws MetadataException, MetadataValidationException {
        init();
        if (wrappers == null) {
            wrappers = new ArrayList();
        }
        this.metadata = metadata;
        wrapper = find_metadata_wrapper();
        wrappers.add(wrapper);
        reindex();

        parse(discard_duplicates);
    }

    /**
     * Add to metadata collection. If collection empty then initialize it.
     * @param metadata - a collection of metadata objects.  Will be wrapped internally (made into
     * single XML document)
     * @param discard_duplicates
     * @throws XdsInternalException
     * @throws MetadataValidationException
     * @throws MetadataException
     */
    public Metadata addToMetadata(List<OMElement> metadata, boolean discard_duplicates) throws XdsInternalException, MetadataException, MetadataValidationException {
        for (OMElement ele : metadata) {
            addToMetadata(ele, discard_duplicates, false);
        }
        parse(discard_duplicates);
        return this;
    }

    /**
     * 
     * @param new_metadata
     * @param discard_duplicates
     * @param run_parse
     * @return
     * @throws XdsInternalException
     * @throws MetadataException
     * @throws MetadataValidationException
     */
    private Metadata addToMetadata(OMElement new_metadata,
            boolean discard_duplicates, boolean run_parse)
            throws XdsInternalException, MetadataException,
            MetadataValidationException {
        boolean hasExistingData = false;
        if (wrapper == null) {
            wrapper = makeWrapper();
            metadata = new_metadata;
            if (wrappers == null) {
                wrappers = new ArrayList<OMElement>();
            }
            wrappers.add(wrapper);
        } else {
            hasExistingData = true;
        }
        wrapper.addChild(Util.deep_copy(new_metadata));
        if (run_parse) {
            if (hasExistingData) {
                reparse(discard_duplicates);
            } else {
                parse(discard_duplicates);
            }
        }
        return this;
    }

    /**
     *
     * @return
     */
    private OMElement makeWrapper() {
        return MetadataSupport.om_factory.createOMElement("root", MetadataSupport.ebRIMns3);
    }

    /**
     *
     */
    public void clearLeafClassObjects() {
        registryPackages.clear();
        submissionSet = null;
        submissionSets.clear();
        extrinsicObjects.clear();
        associations.clear();
        reindex();
    }

    /**
     *
     */
    public void clearObjectRefs() {
        objectRefs = new ArrayList<OMElement>();
    }

    /**
     *
     */
    private void reindex() {
        this.idIndex = null;  // lazy
    }

    /**
     *
     * @param eos
     */
    public void addExtrinsicObjects(List<OMElement> eos) {
        extrinsicObjects.addAll(eos);
        allObjects.addAll(eos);
    }

    /**
     * 
     * @param object_refs_or_ids
     * @throws MetadataException
     */
    @SuppressWarnings("unchecked")
    public void addObjectRefs(List<?> object_refs_or_ids) throws MetadataException {
        if (object_refs_or_ids.size() == 0) {
            return;
        }
        Object ele = object_refs_or_ids.get(0);
        if (ele instanceof OMElement) {
            objectRefs.addAll((List<OMElement>) object_refs_or_ids);
        } else if (ele instanceof String) {
            this.makeObjectRefs((List<String>) object_refs_or_ids);
        } else {
            throw new MetadataException("Don't understand format " + ele.getClass().getName());
        }
    }

    /**
     *
     * @param ids
     */
    public void makeObjectRefs(List<String> ids) {
        for (String id : ids) {
            makeObjectRef(id);
        }
    }

    /**
     *
     * @param id
     */
    private void makeObjectRef(String id) {
        OMElement objRef = MetadataSupport.om_factory.createOMElement(MetadataSupport.object_ref_qname);
        objRef.addAttribute("id", id, null);
        objectRefs.add(objRef);
    }

    /**
     * 
     * @return
     * @throws XdsInternalException
     * @throws MetadataException
     * @throws MetadataValidationException
     */
    public Metadata makeClone() throws XdsInternalException, MetadataException, MetadataValidationException {
        Metadata m = new Metadata();
        if (wrappers != null) {
            if (m.wrappers == null) {
                m.wrappers = new ArrayList<OMElement>();
            }
            for (OMElement ele : wrappers) {
                m.wrappers.add(ele);
            }
        }
        m.extrinsicObjects.addAll(extrinsicObjects);
        m.folders.addAll(folders);
        m.submissionSets.addAll(submissionSets);
        m.registryPackages.addAll(registryPackages);
        m.associations.addAll(associations);
        m.objectRefs.addAll(objectRefs);
        m.classifications.addAll(classifications);
        m.allObjects.addAll(allObjects);
        m.submissionSet = submissionSet;
        return m;
    }

    /**
     * 
     * @return
     */
    public List<OMElement> getLeafClassObjects() {
        List<OMElement> objs = new ArrayList<OMElement>();
        objs.addAll(registryPackages);
        objs.addAll(extrinsicObjects);
        objs.addAll(associations);
        return objs;
    }

    /**
     *
     * @return
     */
    public boolean isVersion2() {
        return version2;
    }

    /**
     *
     * @return
     */
    public OMFactory om_factory() {
        if (fac == null) {
            fac = OMAbstractFactory.getOMFactory();
        }
        return fac;
    }

    /**
     *
     * @return
     */
    public OMElement getRoot() {
        return metadata;
    }

    /**
     * Return a string that can be used for debugging purposes.  It lists the size
     * of each metadata element.
     *
     * @return String representing structure of the metadata.
     */
    public String structure() {
        return this.getSubmissionSetIds().size() + " SS + " + this.extrinsicObjects.size() + " EO + " + this.folders.size() + " Fol + " + this.associations.size() + " A + " + this.objectRefs.size() + " OR";
    }

    /**
     *
     * @param registryObject
     * @param idScheme
     * @return
     */
    public ArrayList<OMElement> getExternalIdentifiers(OMElement registryObject, String idScheme) {
        ArrayList<OMElement> results = new ArrayList<OMElement>();
        QName idSchemeQName = MetadataSupport.identificationscheme_qname;
        for (Iterator it = registryObject.getChildElements(); it.hasNext();) {
            OMElement ele = (OMElement) it.next();
            if (!ele.getLocalName().equals("ExternalIdentifier")) {
                continue;
            }
            String elementIdScheme = ele.getAttributeValue(idSchemeQName);
            if (idScheme == null || idScheme.equals(elementIdScheme)) {
                results.add(ele);
            }
        }
        return results;
    }

    /**
     *
     * @param registryObject
     * @param idScheme
     * @return
     */
    private boolean hasExternalIdentifier(OMElement registryObject, String idScheme) {
        QName idSchemeQName = new QName("identificationScheme");
        for (Iterator it = registryObject.getChildElements(); it.hasNext();) {
            OMElement ele = (OMElement) it.next();
            if (!ele.getLocalName().equals("ExternalIdentifier")) {
                continue;
            }
            String elementIdScheme = ele.getAttributeValue(idSchemeQName);
            if (idScheme.equals(elementIdScheme)) {
                return true;
            }
        }
        return false;

    }

    /**
     * Return true if the slot name exists for the given object.  Otherwise, return false.
     *
     * @param registryObject The registry object in question.
     * @param slotName The name of the slot.
     * @return true if the slot name exists for the object, otherwise false.
     */
    public boolean hasSlot(OMElement registryObject, String slotName) {
        if (registryObject == null) {
            return false;
        }
        for (OMElement slot : MetadataSupport.childrenWithLocalName(registryObject, "Slot")) {
            String name = slot.getAttributeValue(MetadataSupport.slot_name_qname);
            if (name.equals(slotName)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param registryObject
     * @param slotName
     * @param valueIndex
     * @return
     */
    public String getSlotValue(OMElement registryObject, String slotName, int valueIndex) {
        if (registryObject == null) {
            return null;
        }
        for (OMElement slot : MetadataSupport.childrenWithLocalName(registryObject, "Slot")) {
            String name = slot.getAttributeValue(MetadataSupport.slot_name_qname);
            if (!name.equals(slotName)) {
                continue;
            }
            OMElement valueList = MetadataSupport.firstChildWithLocalName(slot, "ValueList");
            if (valueList == null) {
                continue;
            }
            int valueCount = 0;
            for (OMElement valueElement : MetadataSupport.childrenWithLocalName(valueList, "Value")) {
                if (valueCount != valueIndex) {
                    valueCount++;
                    continue;
                }
                return valueElement.getText();
            }
        }
        return null;
    }

    /**
     *
     * @param id
     * @param slot_name
     * @param value_index
     * @return
     * @throws MetadataException
     */
    public String getSlotValue(String id, String slot_name, int value_index) throws MetadataException {
        return getSlotValue(getObjectById(id), slot_name, value_index);
    }

    /**
     *
     * @param obj
     * @param slot_name
     * @param value_index
     * @param value
     */
    public void setSlotValue(OMElement obj, String slot_name, int value_index, String value) {
        if (obj == null) {
            return;
        }
        for (OMElement slot : MetadataSupport.childrenWithLocalName(obj, "Slot")) {
            String name = slot.getAttributeValue(MetadataSupport.slot_name_qname);
            if (!name.equals(slot_name)) {
                continue;
            }
            OMElement value_list = MetadataSupport.firstChildWithLocalName(slot, "ValueList");
            if (value_list == null) {
                continue;
            }
            int value_count = 0;
            for (OMElement value_ele : MetadataSupport.childrenWithLocalName(value_list, "Value")) {
                if (value_count != value_index) {
                    value_count++;
                    continue;
                }
                value_ele.setText(value);
            }
        }
    }

    /**
     *
     * @param ele
     * @return
     */
    public String getStatus(OMElement ele) {
        return ele.getAttributeValue(MetadataSupport.status_qname);
    }

    /**
     *
     * @return
     */
    public ArrayList<OMElement> getMajorObjects() {
        //return getMajorObjects(null);
        return this.allObjects;
    }

    /**
     *
     * @param type
     * @return
     */
    public ArrayList<OMElement> getMajorObjects(String type) {
        ArrayList<OMElement> objs = new ArrayList<OMElement>();

        if (wrapper != null) {
            for (Iterator it = wrapper.getChildElements(); it.hasNext();) {
                OMElement obj = (OMElement) it.next();
                if (type == null || type.equals(obj.getLocalName())) {
                    objs.add(obj);
                }
            }
        }
        return objs;
    }

    /**
     *
     * @param ele
     * @return
     */
    public String getId(OMElement ele) {
        return ele.getAttributeValue(MetadataSupport.id_qname);
    }

    /**
     *
     * @param assoc
     * @return
     */
    public String getSourceObject(OMElement assoc) {
        return assoc.getAttributeValue(MetadataSupport.source_object_qname);
    }

    /**
     *
     * @param assoc
     * @return
     */
    public String getTargetObject(OMElement assoc) {
        return assoc.getAttributeValue(MetadataSupport.target_object_qname);
    }

    /**
     *
     * @param assoc
     * @return
     */
    public String getAssocType(OMElement assoc) {
        return assoc.getAttributeValue(MetadataSupport.association_type_qname);
    }

    /**
     *
     * @param assoc
     * @return
     */
    public String getAssocSource(OMElement assoc) {
        return assoc.getAttributeValue(MetadataSupport.source_object_qname);
    }

    /**
     *
     * @param assoc
     * @return
     */
    public String getAssocTarget(OMElement assoc) {
        return assoc.getAttributeValue(MetadataSupport.target_object_qname);
    }

    /**
     *
     * @return
     */
    public ArrayList<OMElement> getAllObjects() {   // probably the same as Major Objects
        return allObjects;
    }

    /**
     *
     * @param objects
     * @return
     */
    public ArrayList<String> getIdsForObjects(List<OMElement> objects) {
        ArrayList<String> ids = new ArrayList<String>();
        for (int i = 0; i < objects.size(); i++) {
            OMElement object = (OMElement) objects.get(i);
            String id = object.getAttributeValue(MetadataSupport.id_qname);
            ids.add(id);
        }
        return ids;
    }

    /**
     *
     * @param id
     * @return
     * @throws MetadataException
     */
    public String type(String id) throws MetadataException {
        OMElement ele = this.getObjectById(id);
        if (ele == null) {
            return null;
        }
        return ele.getLocalName();
    }

    /**
     *
     * @param ids
     * @param ele
     */
    private void addIds(ArrayList<String> ids, OMElement ele) {
        if (!ele.getLocalName().equals("ObjectRef")) {
            String id = ele.getAttributeValue(MetadataSupport.id_qname);
            if (id != null && !ids.contains(id)) {
                ids.add(id);
            }
        }
        for (Iterator it = ele.getChildElements(); it.hasNext();) {
            OMElement ele2 = (OMElement) it.next();
            addIds(ids, ele2);  // Recurse.
        }
    }

    /**
     *
     * @return
     */
    public ArrayList<String> getAllDefinedIds() {
        ArrayList<String> ids = new ArrayList<String>();
        ArrayList<OMElement> objects = getAllObjects();
        for (int i = 0; i < objects.size(); i++) {
            OMElement object = (OMElement) objects.get(i);
            addIds(ids, object);
        }
        return ids;
    }

    /**
     *
     * @return
     */
    public ArrayList<OMElement> getRegistryPackages() {
        return this.registryPackages;
    }

    /**
     *
     * @return
     */
    public ArrayList<OMElement> getSubmissionSets() {
        return this.submissionSets;
    }

    /**
     *
     * @return
     */
    public ArrayList<OMElement> getExtrinsicObjects() {
        return extrinsicObjects;
    }

    /**
     *
     * @return
     */
    public ArrayList<OMElement> getObjectRefs() {
        return objectRefs;
    }

    /**
     *
     * @return
     */
    public List<String> getObjectRefIds() {
        List<String> ids = new ArrayList<String>();

        for (Iterator<OMElement> it = getObjectRefs().iterator(); it.hasNext();) {
            OMElement ele = it.next();
            ids.add(ele.getAttributeValue(MetadataSupport.id_qname));
        }

        return ids;
    }

    /**
     *
     * @param i
     * @return
     */
    public OMElement getExtrinsicObject(int i) {
        return (OMElement) getExtrinsicObjects().get(i);
    }

    /**
     *
     * @return
     */
    public ArrayList<String> getExtrinsicObjectIds() {
        ArrayList<String> ids = new ArrayList<String>();
        for (Iterator<OMElement> it = getExtrinsicObjects().iterator(); it.hasNext();) {
            OMElement ele = it.next();
            ids.add(ele.getAttributeValue(MetadataSupport.id_qname));
        }
        return ids;
    }

    /**
     *
     * @return
     * @throws MetadataException
     */
    public HashMap<String, OMElement> getDocumentUidMap() throws MetadataException {
        HashMap<String, OMElement> map = new HashMap<String, OMElement>();

        for (Iterator<String> it = getExtrinsicObjectIds().iterator(); it.hasNext();) {
            String id = it.next();
            String uid = this.getUniqueIdValue(id);
            map.put(uid, this.getObjectById(id));
        }
        return map;
    }

    /**
     * Return the "mime type" for the extrinsic object.
     *
     * @param eo An Extrinsic Object.
     * @return A string representing the mime type for the extrinsic object.
     */
    public String getMimeType(OMElement eo) {
        return eo.getAttributeValue(MetadataSupport.mime_type_qname);
    }

    /**
     *
     * @param ro
     * @return
     */
    public String getHome(OMElement ro) {
        return ro.getAttributeValue(MetadataSupport.home_qname);
    }

    /**
     *
     * @return
     */
    public ArrayList<OMElement> getAssociations() {
        return associations;
    }

    /**
     * 
     * @return
     */
    public List<String> getAssocReferences() {
        List<String> ids = new ArrayList<String>();
        for (OMElement assoc : associations) {
            String obj1_id = assoc.getAttributeValue(MetadataSupport.source_object_qname);
            String obj2_id = assoc.getAttributeValue(MetadataSupport.target_object_qname);
            if (!listContains(ids, obj1_id)) {
                ids.add(obj1_id);
            }
            if (!listContains(ids, obj2_id)) {
                ids.add(obj2_id);
            }
        }
        return ids;
    }

    /**
     *
     * @param list
     * @param value
     * @return
     */
    private boolean listContains(List<String> list, String value) {
        for (Iterator<String> it = list.iterator(); it.hasNext();) {
            String val = it.next();
            if (value.equals(val)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param ele
     * @param slot_name
     * @param slot_value
     */
    private void addSlot(OMElement ele, String slot_name, String slot_value) {
        OMElement slot = this.om_factory().createOMElement("Slot", getCurrentNamespace());
        slot.addAttribute("name", slot_name, null);
        OMElement value_list = this.om_factory().createOMElement("ValueList", getCurrentNamespace());
        slot.addChild(value_list);
        OMElement value = this.om_factory().createOMElement("Value", getCurrentNamespace());
        value_list.addChild(value);
        value.setText(slot_value);
        OMElement firstChild = ele.getFirstElement();
        firstChild.insertSiblingBefore(slot);
        //ele.addChild(slot);
        mustDup = true;
    }

    /**
     *
     * @param ele
     * @param slot_name
     * @return
     */
    private OMElement addSlot(OMElement ele, String slot_name) {
        OMElement slot = this.om_factory().createOMElement("Slot", null);
        slot.addAttribute("name", slot_name, null);
        OMElement value_list = this.om_factory().createOMElement("ValueList", null);
        slot.addChild(value_list);

        OMElement firstChild = ele.getFirstElement();
        firstChild.insertSiblingBefore(slot);
        //ele.addChild(slot);

        mustDup = true;
        return slot;
    }

    /**
     *
     * @param slot
     * @param value
     * @return
     */
    private OMElement addSlotValue(OMElement slot, String value) {
        OMElement value_list = MetadataSupport.firstChildWithLocalName(slot, "ValueList");
        OMElement valueEle = this.om_factory().createOMElement("Value", null);
        valueEle.setText(value);
        value_list.addChild(valueEle);
        mustDup = true;
        return slot;
    }

    /**
     *
     * @param ele
     * @return
     */
    private String id(OMElement ele) {
        return ele.getAttributeValue(MetadataSupport.id_qname);
    }

    /**
     *
     * @param ele
     * @param slot_name
     * @param slot_value
     * @throws MetadataException
     */
    public void setSlot(OMElement ele, String slot_name, String slot_value) throws MetadataException {
        OMElement slot = getSlot(id(ele), slot_name);
        if (slot == null) {
            addSlot(ele, slot_name, slot_value);
        } else {
            OMElement value_list = MetadataSupport.firstChildWithLocalName(slot, "ValueList");
            if (value_list == null) {
                throw new MetadataException("Slot without ValueList - slot name is " + slot_name + " of object " + id(ele));
            }
            for (Iterator it = value_list.getChildElements(); it.hasNext();) {
                OMElement v = (OMElement) it.next();
                v.detach();
            }
            OMElement value = MetadataSupport.om_factory.createOMElement("Value", getCurrentNamespace());
            value_list.addChild(value);
            value.addChild(MetadataSupport.om_factory.createOMText(slot_value));
        }
    }

    /**
     *
     * @param rm_dups
     * @throws MetadataException
     * @throws MetadataValidationException
     */
    private void reparse(boolean rm_dups) throws MetadataException, MetadataValidationException {
        reinit();
        reindex();
        parse(rm_dups);
    }

    /**
     * 
     */
    private void reinit() {
        registryPackages = null;
        objectsReferenced = null;
        objectsToDeprecate = null;
        wrappers = null;
        init();
    }

    /**
     *
     */
    private void init() {
        if (registryPackages == null) {
            associations = new ArrayList();
            extrinsicObjects = new ArrayList();
            registryPackages = new ArrayList();
            submissionSets = new ArrayList();
            objectRefs = new ArrayList();
            folders = new ArrayList();
            submissionSet = null;
            classifications = new ArrayList();
            allObjects = new ArrayList();
            classificationsOfId = new HashMap<String, ArrayList>();
            //objects_to_deprecate = new ArrayList();
            //objects_referenced = new ArrayList();
        }
    }

    // referencedObjects are:
    // RegistryObjects referenced by Associations (sourceObject or targetObject)
    // That are not contained in the package that was parsed to create this instance of Metadata
    /**
     *
     * @return
     * @throws MetadataValidationException
     * @throws MetadataException
     */
    public ArrayList getReferencedObjects() throws MetadataValidationException, MetadataException {
        if (objectsReferenced == null) {
            this.objectsReferenced = new ArrayList();
            this.objectsToDeprecate = new ArrayList();
            for (Iterator it = associations.iterator(); it.hasNext();) {
                OMElement association = (OMElement) it.next();
                String association_type = association.getAttributeValue(MetadataSupport.association_type_qname);
                String target_object = association.getAttributeValue(MetadataSupport.target_object_qname);
                String source_object = association.getAttributeValue(MetadataSupport.source_object_qname);
                if (association_type == null) {
                    throw new MetadataValidationException("Association has no associationType attribute");
                }
                if (source_object == null) {
                    throw new MetadataValidationException(association_type + " Association has no sourceObject attribute");
                }
                if (target_object == null) {
                    throw new MetadataValidationException(association_type + " Association has no targetObject attribute");
                }
                if (isUUID(source_object) && !containsObject(source_object)) {
                    objectsReferenced.add(source_object);
                }
                if (isUUID(target_object) && !containsObject(target_object)) {
                    objectsReferenced.add(target_object);
                }
                if (MetadataSupport.xdsB_ihe_assoc_type_rplc.equals(association_type) ||
                        MetadataSupport.xdsB_ihe_assoc_type_xfrm_rplc.equals(association_type)) {
                    if (!target_object.startsWith("urn:uuid:")) {
                        throw new MetadataValidationException("RPLC association has targetObject attribute which is not a UUID: " + target_object);
                    }
                    this.objectsToDeprecate.add(target_object);
                }
            }
        }
        return objectsReferenced;
    }

    /**
     * Return the patient identifier for the given OMElement.
     *
     * @param ele
     * @return
     * @throws MetadataException
     */
    private String getPatientId(OMElement ele) throws MetadataException {
        if (ele == null) {
            return null;
        }
        String id = getId(ele);
        if (isDocument(id)) {
            return getExternalIdentifierValue(id, MetadataSupport.XDSDocumentEntry_patientid_uuid);
        }
        if (isSubmissionSet(id)) {
            return getExternalIdentifierValue(id, MetadataSupport.XDSSubmissionSet_patientid_uuid);
        }
        if (isFolder(id)) {
            return getExternalIdentifierValue(id, MetadataSupport.XDSFolder_patientid_uuid);
        }
        return null;
    }

    /**
     *
     * @param id
     * @return
     * @throws MetadataException
     */
    public boolean containsObject(String id) throws MetadataException {
        OMElement ele = id_index().getObjectById(id);
        if (ele == null) {
            return false;
        }
        if (ele.getLocalName().equals("ObjectRef")) {
            return false;
        }
        return true;
    }

    /**
     *
     * @param objects
     * @return
     */
    public ArrayList<String> idsForObjects(ArrayList<OMElement> objects) {
        ArrayList<String> ids = new ArrayList<String>();
        for (OMElement ele : objects) {
            ids.add(ele.getAttributeValue(MetadataSupport.id_qname));
        }
        return ids;
    }

    /**
     * 
     * @param id
     * @return
     */
    private boolean isUUID(String id) {
        return id.startsWith("urn:uuid:");
    }

    /**
     *
     * @return
     * @throws MetadataValidationException
     * @throws MetadataException
     */
    public ArrayList getReferencedObjectsThatMustHaveSamePatientId() throws MetadataValidationException, MetadataException {
        ArrayList<String> objects = new ArrayList<String>();
        for (Iterator it = associations.iterator(); it.hasNext();) {
            OMElement association = (OMElement) it.next();
            String association_type = association.getAttributeValue(MetadataSupport.association_type_qname);
            String target_object = association.getAttributeValue(MetadataSupport.target_object_qname);
            String source_object = association.getAttributeValue(MetadataSupport.source_object_qname);
            if (association_type == null) {
                throw new MetadataValidationException("Association has no associationType attribute");
            }
            if (source_object == null) {
                throw new MetadataValidationException(association_type + " Association has no sourceObject attribute");
            }
            if (target_object == null) {
                throw new MetadataValidationException(association_type + " Association has no targetObject attribute");
            }
            if (MetadataSupport.xdsB_eb_assoc_type_has_member.equals(association_type) && "Reference".equals(getSlotValue(association, "SubmissionSetStatus", 0))) {
                continue;
            }
            if (source_object.startsWith("urn:uuid:") && id_index().getObjectById(source_object) == null) {
                objects.add(source_object);
            }
            if (target_object.startsWith("urn:uuid:")) {
                OMElement o = id_index().getObjectById(target_object);
                if (o == null) {
                    objects.add(target_object);
                } else if (o.getLocalName().equals("ObjectRef")) {
                    objects.add(target_object);
                }
            }
        }
        return objects;
    }

    /**
     *
     * @param id
     * @return
     * @throws MetadataValidationException
     * @throws MetadataException
     */
    public boolean isReferencedObject(String id) throws MetadataValidationException, MetadataException {
        if (!id.startsWith("urn:uuid:")) {
            return false;
        }
        return getReferencedObjects().contains(id);
    }

    /**
     *
     * @return
     * @throws MetadataValidationException
     * @throws MetadataException
     */
    public ArrayList getDeprecatableObjectIds() throws MetadataValidationException, MetadataException {
        this.getReferencedObjects();
        return objectsToDeprecate;
    }

    /**
     *
     * @param classifications
     */
    private void add_to_classifications_of_id(ArrayList<OMElement> classifications) {
        for (OMElement classification : classifications) {
            add_to_classifications_of_id(classification);
        }
    }

    /**
     *
     * @param classification
     */
    private void add_to_classifications_of_id(OMElement classification) {
        String id = classification.getAttributeValue(MetadataSupport.id_qname);
        ArrayList old = this.classificationsOfId.get(id);
        if (old == null) {
            old = new ArrayList();
            classificationsOfId.put(id, old);
        }
        old.add(classification);
    }

    /**
     *
     * @param a
     * @return
     */
    public OMElement addAssociation(OMElement a) {
        this.associations.add(a);
        this.allObjects.add(a);
        return a;
    }

    /**
     *
     * @param type
     * @param sourceUuid
     * @param targetUuid
     * @return
     */
    public OMElement makeAssociation(String type, String sourceUuid, String targetUuid) {
        OMElement assoc = MetadataSupport.om_factory.createOMElement("Association", MetadataSupport.ebRIMns3);
        assoc.addAttribute(MetadataSupport.om_factory.createOMAttribute("associationType", null, type));
        assoc.addAttribute(MetadataSupport.om_factory.createOMAttribute("sourceObject", null, sourceUuid));
        assoc.addAttribute(MetadataSupport.om_factory.createOMAttribute("targetObject", null, targetUuid));
        assoc.addAttribute(MetadataSupport.om_factory.createOMAttribute("id", null, allocate_id()));
        return assoc;
    }

    /**
     *
     * @param discard_duplicates
     * @throws MetadataException
     * @throws MetadataValidationException
     */
    private void parse(boolean discard_duplicates) throws MetadataException, MetadataValidationException {
        init();
        OMNamespace namespace = wrapper.getNamespace();
        String namespace_uri = (namespace != null) ? namespace.getNamespaceURI() : "";
        detect_metadata_version(namespace_uri);
        for (Iterator it = wrapper.getChildElements(); it.hasNext();) {
            OMElement obj = (OMElement) it.next();
            String type = obj.getLocalName();
            OMAttribute id_att = obj.getAttribute(MetadataSupport.id_qname);
            // obj has no id attribute - assign it one
            if (id_att == null) {
                String id = allocate_id();
                id_att = obj.addAttribute("id", id, null);
            } else {
                String id = id_att.getAttributeValue();
                if (id == null || id.equals("")) {
                    id_att.setAttributeValue(allocate_id());
                }
            }
            if (!discard_duplicates || !getIds(allObjects).contains(id(obj))) {
                allObjects.add(obj);
            }

            add_to_classifications_of_id(findClassifications(obj));

            if (type.equals("RegistryPackage")) {
                if (hasExternalIdentifier(obj, MetadataSupport.XDSSubmissionSet_uniqueid_uuid)) {
                    if (!discard_duplicates || !getIds(submissionSets).contains(id(obj))) {
                        submissionSets.add(obj);
                    }

                    if (submissionSet != null && this.grok_metadata == true) {
                        throw new MetadataException("Metadata: Submission has multiple SubmissionSets");
                    }
                    submissionSet = obj;

                } else if (hasExternalIdentifier(obj, MetadataSupport.XDSFolder_uniqueid_uuid)) {
                    if (!discard_duplicates || !getIds(folders).contains(id(obj))) {
                        folders.add(obj);
                    }
                }
                if (!discard_duplicates || !getIds(registryPackages).contains(id(obj))) {
                    registryPackages.add(obj);
                }

            } else if (type.equals("ExtrinsicObject")) {
                if (!discard_duplicates || !getIds(extrinsicObjects).contains(id(obj))) {
                    extrinsicObjects.add(obj);
                }

            } else if (type.equals("ObjectRef")) {
                if (!discard_duplicates || !getIds(objectRefs).contains(id(obj))) {
                    objectRefs.add(obj);
                }

            } else if (type.equals("Classification")) {
                if (!discard_duplicates || !getIds(classifications).contains(id(obj))) {
                    classifications.add(obj);
                }
                add_to_classifications_of_id(obj);

            } else if (type.equals("Association")) {
                if (!discard_duplicates || !getIds(associations).contains(id(obj))) {
                    associations.add(obj);
                }
            } else {
                throw new MetadataException("Metadata: parse(): did not expect a " + type + " object at the top level");
            }

            for (Iterator it1 = obj.getChildElements(); it1.hasNext();) {
                OMElement obj_i = (OMElement) it1.next();
                String type_i = obj_i.getLocalName();
                if (type_i.equals("Classification")) {
                    if (!discard_duplicates || !getIds(classifications).contains(id(obj_i))) {
                        classifications.add(obj_i);
                    }
                }
            }
        }
        if (grok_metadata && submissionSet == null) {
            throw new NoSubmissionSetException("Metadata: No Submission Set found");
        }
    }

    /**
     *
     * @param namespace_uri
     * @throws MetadataException
     */
    private void detect_metadata_version(String namespace_uri) throws MetadataException {
        // if this class later accepts v3 metadata as well we may have to worry about intermixing v2 and v3
        if (namespace_uri.equals("urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1")) {
            version2 = true;
        } else if (namespace_uri.equals("urn:oasis:names:tc:ebxml-regrep:query:xsd:2.1")) {
            version2 = true;
        } else if (namespace_uri.equals("urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0")) {
            version2 = false;
        } else if (namespace_uri.equals("urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0")) {
            version2 = false;
        } else {
            throw new MetadataException("Metadata.parse(): Cannot identify version of metadata from namespace " + namespace_uri);
        }
    }

    /**
     *
     * @param ele
     * @return
     */
    public String getNameValue(OMElement ele) {
        OMElement name_ele = MetadataSupport.firstChildWithLocalName(ele, "Name");
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
     * @return
     */
    public OMElement getSubmissionSet() {
        return submissionSet;
    }

    /**
     *
     * @return
     */
    public String getSubmissionSetId() {
        OMElement ss = getSubmissionSet();
        if (ss == null) {
            return "";
        }
        return ss.getAttributeValue(MetadataSupport.id_qname);
    }

    /**
     *
     * @param ids
     * @return
     */
    public ArrayList<OMElement> getAssociationsInclusive(ArrayList<String> ids) {
        ArrayList<OMElement> assocs = new ArrayList<OMElement>();
        for (OMElement a : this.getAssociations()) {
            if (ids.contains(getAssocSource(a)) &&
                    ids.contains(getAssocTarget(a))) {
                assocs.add(a);
            }
        }
        return assocs;
    }

    /**
     *
     * @return
     */
    public ArrayList<String> getSubmissionSetIds() {
        ArrayList ids = new ArrayList();
        ArrayList sss = getSubmissionSets();
        for (int i = 0; i < sss.size(); i++) {
            OMElement ss = (OMElement) sss.get(i);
            String f_id = ss.getAttributeValue(MetadataSupport.id_qname);
            ids.add(f_id);
        }
        return ids;
    }

    /**
     *
     * @param id
     * @return
     */
    public boolean isSubmissionSet(String id) {
        return getSubmissionSetIds().contains(id);
    }

    /**
     *
     * @param id
     * @return
     */
    public boolean isFolder(String id) {
        return getFolderIds().contains(id);
    }

    /**
     *
     * @param id
     * @return
     */
    public boolean isDocument(String id) {
        return this.getExtrinsicObjectIds().contains(id);
    }

    /**
     * Return true if all patient ids in metadata are equivalent.  Otherwise return false.
     * @return Boolean value indicating result of verification.
     * @throws MetadataException
     */
    public boolean isPatientIdConsistent() throws MetadataException {
        String patientID = null;
        for (OMElement ele : allObjects) {
            String pid = getPatientId(ele);
            if (patientID == null) {
                patientID = pid;
                continue;
            }
            if (pid == null) {
                continue;
            }
            if (!patientID.equals(pid)) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @return
     */
    public ArrayList<OMElement> getFolders() {
        return folders;
    }

    /**
     *
     * @param parts
     * @return
     */
    public ArrayList<String> getIds(ArrayList<OMElement> parts) {
        ArrayList<String> ids = new ArrayList<String>();
        for (int i = 0; i < parts.size(); i++) {
            OMElement part = (OMElement) parts.get(i);
            String f_id = part.getAttributeValue(MetadataSupport.id_qname);
            ids.add(f_id);
        }
        return ids;
    }

    /**
     *
     * @return
     */
    public ArrayList<String> getFolderIds() {
        ArrayList ids = new ArrayList();
        ArrayList folders = getFolders();
        for (int i = 0; i < folders.size(); i++) {
            OMElement folder = (OMElement) folders.get(i);
            String f_id = folder.getAttributeValue(MetadataSupport.id_qname);
            ids.add(f_id);
        }
        return ids;
    }

    /**
     *
     * @param i
     * @return
     */
    public OMElement getFolder(int i) {
        return (OMElement) getFolders().get(i);
    }

    /**
     *
     * @return
     */
    public ArrayList getRegistryPackageIds() {
        ArrayList ids = new ArrayList();
        ArrayList rps = this.getRegistryPackages();
        for (int i = 0; i < rps.size(); i++) {
            OMElement rp = (OMElement) rps.get(i);
            String f_id = rp.getAttributeValue(MetadataSupport.id_qname);
            ids.add(f_id);
        }
        return ids;
    }

    /**
     *
     * @param ele
     * @return
     */
    public String getIdentifyingString(OMElement ele) {
        StringBuffer b = new StringBuffer();
        b.append(ele.getLocalName());
        OMElement name_ele = MetadataSupport.firstChildWithLocalName(ele, "Name");
        if (name_ele != null) {
            OMElement loc = MetadataSupport.firstChildWithLocalName(name_ele, "LocalizedString");
            if (loc != null) {
                String name = loc.getAttributeValue(new QName("value"));
                b.append(" Name=\"" + name + "\"");
            }
        }
        b.append(" id=\"" + ele.getAttributeValue(MetadataSupport.id_qname) + "\"");
        return "<" + b.toString() + ">";
    }

    /**
     *
     * @param objects
     * @return
     */
    public ArrayList getObjectNames(ArrayList objects) {
        ArrayList names = new ArrayList();
        for (int i = 0; i < objects.size(); i++) {
            OMElement obj = (OMElement) objects.get(i);
            names.add(obj.getLocalName());
        }
        return names;
    }

    /**
     *
     * @return
     * @throws MetadataException
     */
    private OMElement find_metadata_wrapper() throws MetadataException {
        if (metadata == null || metadata.getLocalName() == null) {
            throw new NoMetadataException("find_metadata_wrapper: Cannot find a wrapper element, top element is NULL" + ". A wrapper is one of the XML elements that holds metadata (ExtrinsicObject, RegistryPackage, Association etc.)");
        }
        if (metadata.getLocalName().equals("TestResults")) {
            OMElement test_step = MetadataSupport.firstChildWithLocalName(metadata, "TestStep");
            if (test_step != null) {
                OMElement sqt = MetadataSupport.firstChildWithLocalName(test_step, "StoredQueryTransaction");
                if (sqt != null) {
                    OMElement result = MetadataSupport.firstChildWithLocalName(sqt, "Result");
                    if (result != null) {
                        OMElement ahqr = MetadataSupport.firstChildWithLocalName(result, "AdhocQueryResponse");
                        if (ahqr != null) {
                            OMElement rol = MetadataSupport.firstChildWithLocalName(ahqr, "RegistryObjectList");
                            if (rol != null) {
                                return rol;
                            }
                        }
                    }
                }
            }
        }
        if (metadata.getLocalName().equals("LeafRegistryObjectList")) {
            return metadata;
        }
        if (metadata.getLocalName().equals("ProvideAndRegisterDocumentSetRequest")) {
            OMElement sor = MetadataSupport.firstChildWithLocalName(metadata, "SubmitObjectsRequest");
            if (sor != null) {
                return MetadataSupport.firstChildWithLocalName(sor, "RegistryObjectList");
            }
        }
        for (Iterator it = metadata.getChildElements(); it.hasNext();) {
            OMElement child = (OMElement) it.next();
            if (child.getLocalName().equals("RegistryObjectList")) {
                return child;
            }
            if (child.getLocalName().equals("AdhocQueryResponse")) {
                OMElement achild = MetadataSupport.firstChildWithLocalName(child, "SQLQueryResult");
                if (achild != null) {
                    return achild;
                }
            }
            if (child.getLocalName().equals("LeafRegistryObjectList")) {
                return child;
            }
        }
        OMElement ele2 = find_metadata_wrapper2(metadata);
        if (ele2 != null) {
            return ele2;
        }
        throw new NoMetadataException("find_metadata_wrapper: Cannot find a wrapper element, top element is " + metadata.getLocalName() + ". A wrapper is one of the XML elements that holds metadata (ExtrinsicObject, RegistryPackage, Association etc.)");
    }

    /**
     *
     * @param ele
     * @return
     * @throws MetadataException
     */
    private OMElement find_metadata_wrapper2(OMElement ele) throws MetadataException {
        for (Iterator<OMElement> it = ele.getChildElements(); it.hasNext();) {
            OMElement e = it.next();
            String name = e.getLocalName();
            if (name == null) {
                continue;
            }
            if (name.equals("ObjectRef") ||
                    name.equals("ExtrinsicObject") ||
                    name.equals("RegistryPackage") ||
                    name.equals("Association") ||
                    name.equals("Classification")) {
                return ele;
            }
            OMElement e2 = find_metadata_wrapper2(e);
            if (e2 != null) {
                return e2;
            }
        }
        return null;
    }

    /**
     *
     * @return
     */
    public OMElement getWrapper() {
        return wrapper;
    }

    /**
     *
     * @param registry_object
     * @param slot_name
     * @return
     */
    public OMElement findSlot(OMElement registry_object, String slot_name) {
        for (Iterator it = registry_object.getChildElements(); it.hasNext();) {
            OMElement s = (OMElement) it.next();
            if (!s.getLocalName().equals("Slot")) {
                continue;
            }
            String val = s.getAttributeValue(MetadataSupport.slot_name_qname);
            if (val != null && val.equals(slot_name)) {
                return s;
            }
        }
        return null;
    }

    /**
     *
     * @param registry_object
     * @param classificationScheme
     * @return
     */
    public ArrayList findClassifications(OMElement registry_object, String classificationScheme) {
        ArrayList cl = new ArrayList();
        for (Iterator it = registry_object.getChildElements(); it.hasNext();) {
            OMElement s = (OMElement) it.next();
            if (!s.getLocalName().equals("Classification")) {
                continue;
            }
            String val = s.getAttributeValue(MetadataSupport.classificationscheme_qname);
            if (val != null && val.equals(classificationScheme)) {
                cl.add(s);
            }
        }
        return cl;
    }

    /**
     *
     * @param registry_object
     * @return
     */
    public ArrayList findClassifications(OMElement registry_object) {
        ArrayList cl = new ArrayList();
        for (Iterator it = registry_object.getChildElements(); it.hasNext();) {
            OMElement s = (OMElement) it.next();
            if (!s.getLocalName().equals("Classification")) {
                continue;
            }
            cl.add(s);
        }
        return cl;
    }

    /**
     *
     * @param registry_object
     * @param element_name
     * @return
     */
    public ArrayList findChildElements(OMElement registry_object, String element_name) {
        ArrayList al = new ArrayList();
        for (Iterator it = registry_object.getChildElements(); it.hasNext();) {
            OMElement s = (OMElement) it.next();
            if (s.getLocalName().equals(element_name)) {
                al.add(s);
            }
        }
        return al;
    }

    /**
     *
     * @param registry_objects
     * @return
     */
    public ArrayList<String> getObjectIds(ArrayList<OMElement> registry_objects) {
        ArrayList ids = new ArrayList();
        for (int i = 0; i < registry_objects.size(); i++) {
            OMElement ele = registry_objects.get(i);
            String id = ele.getAttributeValue(MetadataSupport.id_qname);
            ids.add(id);
        }
        return ids;
    }

    /**
     *
     * @param registry_objects
     * @param version2
     * @return
     */
    public ArrayList<OMElement> getObjectRefs(ArrayList registry_objects, boolean version2) {
        ArrayList<OMElement> ors = new ArrayList<OMElement>();
        for (int i = 0; i < registry_objects.size(); i++) {
            OMElement ele = (OMElement) registry_objects.get(i);
            String id = ele.getAttributeValue(MetadataSupport.id_qname);
            OMElement or = MetadataSupport.om_factory.createOMElement("ObjectRef",
                    (version2) ? MetadataSupport.ebRIMns2 : MetadataSupport.ebRIMns3);
            or.addAttribute("id", id, null);
            or.addAttribute("home", "", null);
            ors.add(or);
        }
        return ors;
    }

    /**
     *
     * @return
     */
    public ArrayList<OMElement> getClassifications() {
        return classifications;
    }

    /*
     * by ID
     */
    private IdIndex id_index() throws MetadataException {
        if (idIndex == null) {
            idIndex = new IdIndex(this);
            //			System.out.println("Metadata indexed: \n" + id_index.toString());
        }
        return idIndex;
    }

    /**
     *
     * @param log_message
     * @return
     * @throws MetadataException
     */
    private IdIndex id_index(XLogMessage log_message) throws MetadataException {
        if (idIndex == null) {
            idIndex = new IdIndex();
            idIndex.setLogMessage(log_message);
            idIndex.setMetadata(this);
        }
        return idIndex;
    }

    /**
     *
     * @param object_id
     * @return
     * @throws MetadataException
     */
    public String getNameValue(String object_id) throws MetadataException {
        return id_index().getNameValue(object_id);
    }

    /**
     *
     * @param object_id
     * @return
     * @throws MetadataException
     */
    public String getDescriptionValue(String object_id) throws MetadataException {
        return id_index().getDescriptionValue(object_id);
    }

    /**
     *
     * @param object_id
     * @return
     * @throws MetadataException
     */
    public ArrayList getSlots(String object_id) throws MetadataException {
        return id_index().getSlots(object_id);
    }

    /**
     *
     * @param object_id
     * @param name
     * @return
     * @throws MetadataException
     */
    public OMElement getSlot(String object_id, String name) throws MetadataException {
        return id_index().getSlot(object_id, name);
    }

    /**
     *
     * @param object_id
     * @param name
     * @throws MetadataException
     */
    public void removeSlot(String object_id, String name) throws MetadataException {
        OMElement slot = getSlot(object_id, name);
        if (slot != null) {
            slot.detach();
            reindex();
        }
    }

    /**
     *
     * @return
     */
    public String getMetadataDescription() {
        StringBuffer buf = new StringBuffer();
        buf.append(this.getSubmissionSets().size() + " SubmissionSets\n");
        buf.append(this.getExtrinsicObjects().size() + " DocumentEntries\n");
        buf.append(this.getFolders().size() + " Folders\n");
        buf.append(this.getAssociations().size() + " Associations\n");
        buf.append(this.getObjectRefs().size() + " ObjectRefs\n");
        return buf.toString();
    }

    /**
     *
     * @throws MetadataException
     */
    public void fixClassifications() throws MetadataException {
        ArrayList<String> rpIds = getRegistryPackageIds();
        for (int i = 0; i < rpIds.size(); i++) {
            String id = rpIds.get(i);
            ArrayList<OMElement> classifications = getClassifications(id);
            for (OMElement classification : classifications) {
                if (classification.getAttribute(MetadataSupport.noderepresentation_qname) != null) {
                    continue;
                }

                // not a code - must be classification of RP as SS or Fol, make sure
                // classificationNode is present

                if (classification.getAttribute(MetadataSupport.classificationnode_qname) == null) {
                    // add classification, first figure out if this is SS or Fol
                    if (isSubmissionSet(id)) {
                        classification.addAttribute("classificationNode", MetadataSupport.XDSSubmissionSet_classification_uuid, null);
                    } else {
                        classification.addAttribute("classificationNode", MetadataSupport.XDSFolder_classification_uuid, null);
                    }
                }
            }
        }
    }

    /**
     *
     * @param object_id
     * @return
     * @throws MetadataException
     */
    public ArrayList<OMElement> getClassifications(String object_id) throws MetadataException {
        return id_index().getClassifications(object_id);
    }

    /**
     *
     * @param object
     * @return
     * @throws MetadataException
     */
    public ArrayList<OMElement> getClassifications(OMElement object) throws MetadataException {
        return id_index().getClassifications(this.getId(object));
    }

    /**
     *
     * @param object
     * @param classification_scheme
     * @return
     * @throws MetadataException
     */
    public ArrayList<OMElement> getClassifications(OMElement object, String classification_scheme) throws MetadataException {
        return getClassifications(this.getId(object), classification_scheme);
    }

    /**
     *
     * @param classification
     * @return
     */
    public String getClassificationValue(OMElement classification) {
        return classification.getAttributeValue(MetadataSupport.noderepresentation_qname);
    }

    /**
     *
     * @param classification
     * @return
     */
    public String getClassificationScheme(OMElement classification) {
        return getSlotValue(classification, "codingScheme", 0);
    }

    /**
     *
     * @param object
     * @param classification_scheme
     * @return
     * @throws MetadataException
     */
    public ArrayList<String> getClassificationsValues(OMElement object, String classification_scheme) throws MetadataException {
        ArrayList<OMElement> classes = getClassifications(object, classification_scheme);
        ArrayList<String> values = new ArrayList<String>();
        for (OMElement e : classes) {
            values.add(e.getAttributeValue(MetadataSupport.noderepresentation_qname));
        }
        return values;
    }

    /**
     *
     * @param id
     * @param classification_scheme
     * @return
     * @throws MetadataException
     */
    public ArrayList<String> getClassificationsValues(String id, String classification_scheme) throws MetadataException {
        return getClassificationsValues(this.getObjectById(id), classification_scheme);
    }

    /**
     *
     * @param id
     * @param classification_scheme
     * @return
     * @throws MetadataException
     */
    public ArrayList<OMElement> getClassifications(String id, String classification_scheme) throws MetadataException {
        ArrayList<OMElement> cls = getClassifications(id);
        ArrayList<OMElement> cls_2 = new ArrayList<OMElement>();
        for (OMElement cl : cls) {
            String cl_scheme = cl.getAttributeValue(MetadataSupport.classificationscheme_qname);
            if (cl_scheme != null && cl_scheme.equals(classification_scheme)) {
                cls_2.add(cl);
            }
        }
        return cls_2;
    }

    /**
     *
     * @param object_id
     * @return
     * @throws MetadataException
     */
    public ArrayList getExternalIdentifiers(String object_id) throws MetadataException {
        return id_index().getExternalIdentifiers(object_id);
    }

    /**
     *
     * @param object_id
     * @param identifier_scheme
     * @return
     * @throws MetadataException
     */
    public String getExternalIdentifierValue(String object_id, String identifier_scheme) throws MetadataException {
        return id_index().getExternalIdentifierValue(object_id, identifier_scheme);
    }

    /**
     * Updates all folders "lastUpdateTime" slot with the current time.
     *
     * @throws MetadataException
     */
    public void updateFoldersLastUpdateTimeSlot() throws MetadataException {
        ArrayList<OMElement> folderList = this.folders;
        // Set XDSFolder.lastUpdateTime
        if ((folderList != null) && (folderList.size() != 0)) {
            String timestamp = Hl7Date.now();
            for (OMElement fol : folderList) {
                this.setSlot(fol, "lastUpdateTime", timestamp);
            }
        }
    }

    /**
     *
     * @param id
     * @return
     * @throws MetadataException
     */
    public String getUniqueIdValue(String id) throws MetadataException {
        String uid;
        uid = id_index().getExternalIdentifierValue(id, MetadataSupport.XDSDocumentEntry_uniqueid_uuid);
        if (uid != null && !uid.equals("")) {
            return uid;
        }
        uid = id_index().getExternalIdentifierValue(id, MetadataSupport.XDSSubmissionSet_uniqueid_uuid);
        if (uid != null && !uid.equals("")) {
            return uid;
        }
        uid = id_index().getExternalIdentifierValue(id, MetadataSupport.XDSFolder_uniqueid_uuid);
        if (uid != null && !uid.equals("")) {
            return uid;
        }
        return null;
    }

    /**
     *
     * @return
     * @throws MetadataException
     */
    public ArrayList<String> getAllUids() throws MetadataException {
        ArrayList<String> all_ids = this.getAllDefinedIds();
        ArrayList<String> all_uids = new ArrayList<String>();
        for (String id : all_ids) {
            String uid = getUniqueIdValue(id);
            if (uid != null) {
                all_uids.add(uid);
            }
        }
        return all_uids;
    }

    /**
     * 
     * @return
     */
    public List<OMElement> getAllLeafClasses() {
        List<OMElement> lc = new ArrayList<OMElement>();
        lc.addAll(extrinsicObjects);
        lc.addAll(registryPackages);
        lc.addAll(associations);
        return lc;
    }

    /**
     *
     * @param map
     * @param uid
     * @param hash
     */
    private void addToUidHashMap(HashMap<String, ArrayList<String>> map, String uid, String hash) {
        if (uid == null) {
            return;
        }
        ArrayList<String> hash_list = map.get(uid);
        if (hash_list == null) {
            hash_list = new ArrayList<String>();
            map.put(uid, hash_list);
        }
        hash_list.add(hash);
    }

    // get map of uid ==> ArrayList of hashes
    // for folder and ss, hash is null
    // Some docs may not have a hash either, depending on where this use used
    /**
     *
     * @return
     * @throws MetadataException
     */
    public HashMap<String, ArrayList<String>> getUidHashMap() throws MetadataException {
        HashMap<String, ArrayList<String>> hm = new HashMap<String, ArrayList<String>>();
        ArrayList<String> ids;
        ids = this.getExtrinsicObjectIds();
        for (String id : ids) {
            OMElement registry_object = this.getObjectById(id);
            String uid;
            ArrayList<OMElement> eis = this.getExternalIdentifiers(id);
            ArrayList<OMElement> eid_eles = this.getExternalIdentifiers(registry_object, MetadataSupport.XDSDocumentEntry_uniqueid_uuid);
            if (eid_eles.size() > 0) {
                uid = eid_eles.get(0).getAttributeValue(MetadataSupport.value_qname);
            } else {
                throw new MetadataException("Metadata.getUidHashMap(): Doc " + id + " has no uniqueId\nfound " + eis.size() + " external identifiers");
            }
            String hash = this.getSlotValue(id, "hash", 0);
            if (hash != null && hash.equals("")) {
                hash = null;
            }
            addToUidHashMap(hm, uid, hash);
        }

        ids = this.getSubmissionSetIds();
        for (String id : ids) {
            String uid;
            uid = id_index().getExternalIdentifierValue(id, MetadataSupport.XDSSubmissionSet_uniqueid_uuid);
            if (uid == null || uid.equals("")) {
                throw new MetadataException("Metadata.getUidHashMap(): SS " + id + " has no uniqueId");
            }
            addToUidHashMap(hm, uid, null);
        }

        ids = this.getFolderIds();
        for (String id : ids) {
            String uid;
            uid = id_index().getExternalIdentifierValue(id, MetadataSupport.XDSFolder_uniqueid_uuid);
            if (uid == null || uid.equals("")) {
                throw new MetadataException("Metadata.getUidHashMap(): Fol " + id + " has no uniqueId");
            }
            addToUidHashMap(hm, uid, null);
        }
        return hm;
    }

    /**
     *
     * @return
     * @throws MetadataException
     */
    public String getSubmissionSetUniqueId() throws MetadataException {
        return id_index().getSubmissionSetUniqueId();
    }

    /**
     *
     * @return
     * @throws MetadataException
     */
    public String getSubmissionSetPatientId() throws MetadataException {
        return id_index().getSubmissionSetPatientId();
    }

    /**
     *
     * @param id
     * @return
     * @throws MetadataException
     */
    public OMElement getObjectById(String id) throws MetadataException {
        return id_index().getObjectById(id);
    }

    /**
     *
     * @param id
     * @return
     * @throws MetadataException
     */
    public String getIdentifyingString(String id) throws MetadataException {
        return id_index().getIdentifyingString(id);
    }

    /**
     *
     * @param id
     * @return
     * @throws MetadataException
     */
    public String getObjectTypeById(String id) throws MetadataException {
        return id_index().getObjectTypeById(id);
    }

    /**
     *
     * @return
     * @throws MetadataException
     */
    public String getSubmissionSetSourceId() throws MetadataException {
        return id_index().getSubmissionSetSourceId();
    }

    /**
     *
     * @return
     * @throws XdsInternalException
     */
    public ArrayList getOriginal() throws XdsInternalException {
        if (!mustDup) {
            return allObjects;
        }
        if (version2) {
            return getV2();
        } else {
            return getV3();
        }
    }

    // return ArrayList of OMElements
    // returns a copy of the original
    /**
     *
     * @return
     * @throws XdsInternalException
     */
    public ArrayList getV2() throws XdsInternalException {
        ArrayList al;
        IdParser ip = new IdParser(this);
        ArrayList undefinedIds = ip.getUndefinedIds();
        TranslateToV2 v = new TranslateToV2();
        al = v.translate(allObjects, mustDup);
        for (Iterator it = undefinedIds.iterator(); it.hasNext();) {
            String id = (String) it.next();
            if (!id.startsWith("urn:uuid:")) {
                continue;
            }
            OMElement or;
            al.add(or = (OMElement) MetadataSupport.om_factory.createOMElement("ObjectRef", MetadataSupport.ebRIMns2));
            or.addAttribute("id", id, null);
        }
        // BHT: BUG FIX TEST (add missing classification references).
        OMElement or;
        or = (OMElement) MetadataSupport.om_factory.createOMElement("ObjectRef", MetadataSupport.ebRIMns2);
        al.add(or);
        or.addAttribute("id", MetadataSupport.XDSFolder_classification_uuid, null);
        or = (OMElement) MetadataSupport.om_factory.createOMElement("ObjectRef", MetadataSupport.ebRIMns2);
        al.add(or);
        or.addAttribute("id", MetadataSupport.XDSSubmissionSet_classification_uuid, null);
        // BHT: END BUG FIX HACK

        return al;
    }

    // returns a copy of the original
    /**
     *
     * @return
     * @throws XdsInternalException
     */
    public ArrayList<OMElement> getV3() throws XdsInternalException {
        ArrayList al;
        if (version2) {
            TranslateToV3 v = new TranslateToV3();
            al = v.translate(allObjects, mustDup);
        } else {
            TranslateToV3 v = new TranslateToV3();
            al = v.translate(allObjects, mustDup);
        }
        return al;
    }

    /**
     *
     * @param root
     * @param localname
     * @return
     */
    // breadth first search for element with localname
    private OMElement find_element(OMElement root, String localname) {
        if (root.getLocalName().equals(localname)) {
            return root;
        }
        for (Iterator it = root.getChildElements(); it.hasNext();) {
            OMElement child = (OMElement) it.next();
            if (child.getLocalName().equals(localname)) {
                return child;
            }
        }
        for (Iterator it = root.getChildElements(); it.hasNext();) {
            OMElement child = (OMElement) it.next();
            OMElement ele = find_element(child, localname);
            if (ele != null) {
                return ele;
            }
        }
        return null;
    }

    /**
     *
     * @return
     * @throws XdsInternalException
     */
    public OMElement getV3SubmitObjectsRequest() throws XdsInternalException {
        //OMNamespace rs = MetadataSupport.ebRSns3;
        OMNamespace lcm = MetadataSupport.ebLcm3;
        OMNamespace rim = MetadataSupport.ebRIMns3;
        OMElement sor = this.om_factory().createOMElement("SubmitObjectsRequest", lcm);
        OMElement lrol = this.om_factory().createOMElement("RegistryObjectList", rim);
        sor.addChild(lrol);
        ArrayList objects = this.getV3();
        for (int i = 0; i < objects.size(); i++) {
            OMElement ele = (OMElement) objects.get(i);
            lrol.addChild(ele);
        }
        return sor;
    }

    /**
     *
     * @param value
     * @return
     */
    public String stripNamespace(String value) {
        if (value == null) {
            return null;
        }
        if (value.indexOf(":") == -1) {
            return value;
        }
        String[] parts = value.split(":");
        return parts[parts.length - 1];
    }

    /**
     *
     * @param value
     * @return
     */
    public boolean hasNamespace(String value) {
        if (value.indexOf(":") == -1) {
            return false;
        }
        return true;
    }

    /**
     *
     * @param value
     * @param namespace
     * @return
     */
    public String addNamespace(String value, String namespace) {
        if (hasNamespace(value)) {
            return value;
        }
        if (namespace.endsWith(":")) {
            return namespace + value;
        }
        return namespace + ":" + value;
    }

    /**
     *
     * @param objects
     * @return
     * @throws MetadataException
     */
    public HashMap<String, OMElement> getUidMap(ArrayList<OMElement> objects) throws MetadataException {
        HashMap<String, OMElement> map = new HashMap<String, OMElement>();  // uid -> OMElement
        for (OMElement non_ref : objects) {
            String non_ref_id = this.getId(non_ref);
            String a_uid = this.getUniqueIdValue(non_ref_id);
            if (a_uid != null) {
                map.put(a_uid, non_ref);
            }
        }
        return map;
    }

    /**
     *
     * @return
     * @throws MetadataException
     */
    public HashMap<String, OMElement> getUidMap() throws MetadataException {
        return getUidMap(this.getNonObjectRefs());
    }

    /**
     *
     * @param id
     * @return
     * @throws MetadataException
     */
    public boolean isRetrievable_a(String id) throws MetadataException {
        String uri = this.getSlotValue(id, "URI", 0);
        return uri != null;
    }

    /**
     *
     * @param id
     * @return
     * @throws MetadataException
     */
    public boolean isRetrievable_b(String id) throws MetadataException {
        String uid = this.getSlotValue(id, "repositoryUniqueId", 0);
        return uid != null;
    }

    /**
     *
     * @param simpleAssociationType
     * @return
     */
    public String v3AssociationNamespace(String simpleAssociationType) {
        if (Metadata.iheAssocTypes.contains(simpleAssociationType)) {
            return MetadataSupport.xdsB_ihe_assoc_namespace_uri;
        } else {
            return MetadataSupport.xdsB_eb_assoc_namespace_uri;
        }
    }

    /**
     *
     * @param eo
     * @return
     */
    private boolean isURIExtendedFormat(OMElement eo) {
        String uri = getSlotValue(eo, "URI", 0);
        String uri2 = getSlotValue(eo, "URI", 1);

        if (uri == null) {
            return false;
        }

        if (uri2 != null) {
            return true;
        }

        String[] parts = uri.split("\\|");
        return (parts.length >= 2);

    }

    /**
     *
     * @param eo
     * @return
     * @throws MetadataException
     */
    public String getURIAttribute(OMElement eo) throws MetadataException {
        String eoId = getId(eo);
        String value = null;
        if (!isURIExtendedFormat(eo)) {
            value = getSlotValue(eo, "URI", 0);
        } else {
            HashMap<String, String> map = new HashMap<String, String>();
            for (int i = 0; i < 1000; i++) {
                String slotValue = getSlotValue(eoId, "URI", i);
                if (slotValue == null) {
                    break;
                }
                String[] parts = slotValue.split("\\|");
                if (parts.length != 2 ||
                        parts[0].length() == 0) {
                    throw new MetadataException("URI value does not parse: " + slotValue + " must be num|string format");
                }
                map.put(parts[0], parts[1]);
            }
            StringBuffer buf = new StringBuffer();
            int i = 1;
            for (;; i++) {
                String iStr = String.valueOf(i);
                String part = map.get(iStr);
                if (part == null) {
                    break;
                }
                buf.append(part);
            }
            if (map.size() != i - 1) {
                throw new MetadataException("URI value does not parse: index " + i + "  not found but Slot has " + map.size() + " values. Slot is\n" +
                        getSlot(eoId, "URI").toString());
            }
            value = buf.toString();
        }
        if (value == null) {
            return null;
        }
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            throw new MetadataException("URI must have http:// or https:// prefix. URI was calculated to be\n" + value +
                    "\nand original slot is\n" + getSlot(eoId, "URI").toString());
        }
        return value;
    }
    
    int uriChunkSize = 100;

    /**
     *
     * @param size
     */
    /**
    public void setUriChunkSize(int size) {
        uriChunkSize = size;
    }*/

    /**
     *
     * @param a
     * @param b
     * @return
     */
    private int min(int a, int b) {
        if (a < b) {
            return a;
        }
        return b;
    }

    /**
     *
     * @param eo
     * @param uri
     */
    public void setURIAttribute(OMElement eo, String uri) {
        try {
            removeSlot(this.getId(eo), "URI");
        } catch (MetadataException e) {
        }
        if (uri.length() < uriChunkSize) {
            addSlot(eo, "URI", uri);
            return;
        }
        OMElement slot = addSlot(eo, "URI");
        StringBuffer buf = new StringBuffer();
        int chunkIndex = 1;
        int uriSize = uri.length();
        int strStart = 0;
        int strEnd = min(uriChunkSize, uriSize);
        while (true) {
            buf.setLength(0);
            buf.append(String.valueOf(chunkIndex++)).append("|").append(uri.substring(strStart, strEnd));
            addSlotValue(slot, buf.toString());
            if (strEnd == uriSize) {
                break;
            }
            strStart = strEnd;
            strEnd = min(strStart + uriChunkSize, uriSize);
        }
    }
}

