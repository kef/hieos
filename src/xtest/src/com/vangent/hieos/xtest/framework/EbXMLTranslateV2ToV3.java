/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vangent.hieos.xtest.framework;

import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.metadata.structure.Metadata;
import com.vangent.hieos.xutil.metadata.structure.MetadataSupport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;

/**
 *
 * @author NIST (moved out of xutil library).
 */
public class EbXMLTranslateV2ToV3 {
  int id_count = 1;
    Metadata m;

    /**
     *
     * @param in
     * @return
     * @throws XdsInternalException
     */
    public ArrayList<OMElement> translate(List<OMElement> in) throws XdsInternalException {
        ArrayList<OMElement> out = new ArrayList<OMElement>();
        for (int i = 0; i < in.size(); i++) {
            OMElement e = in.get(i);
            OMElement output = translate(e);
            if (output != null) {
                out.add(output);
            }
        }
        return out;
    }

    /**
     *
     * @param ro2
     * @return
     * @throws XdsInternalException
     */
    public OMElement translate(OMElement ro2) throws XdsInternalException {

        m = new Metadata();
        return deep_copy(ro2, MetadataSupport.ebRIMns3);
    }

    String new_id() {
        return "id_" + id_count++;
    }

    OMElement add_id(OMElement ele) {
        String att_value = ele.getAttributeValue(MetadataSupport.id_qname);
        if (att_value == null || att_value.equals("")) {
            ele.addAttribute("id", new_id(), null);
        }
        return ele;
    }

    enum Att {

        Slot, Name, Description, VersionInfo, Classification, ExternalIdentifier
    };

    OMElement deep_copy(OMElement from, OMNamespace new_namespace) {
        String to_id = from.getAttributeValue(MetadataSupport.id_qname);
        String to_name = from.getLocalName();

        if (to_name.equals("ObjectRef")) {
            return null;
        }


        OMElement to = MetadataSupport.om_factory.createOMElement(from.getLocalName(), new_namespace);

        copy_attributes(from, to);

        if (to_name.equals("Association")) {
            add_id(to);
        }

        if (to_name.equals("Classification")) {
            add_id(to);
        }

        for (Att att : Att.values()) {
            String att_name = att.name();
            for (Iterator it = from.getChildElements(); it.hasNext();) {
                OMElement child = (OMElement) it.next();
                if (child.getLocalName().equals(att_name)) {
                    OMElement newx = deep_copy(child, new_namespace);
                    if (att_name.equals("ExternalIdentifier")) {
                        add_id(newx);
                        newx.addAttribute("registryObject", to_id, null);
                    }
                    to.addChild(newx);
                }
            }
        }


        OMElement x;

        for (Iterator it = from.getChildElements(); it.hasNext();) {
            x = (OMElement) it.next();

            if (x.getLocalName().equals("Name")) {
                continue;
            }
            if (x.getLocalName().equals("Description")) {
                continue;
            }
            if (x.getLocalName().equals("Slot")) {
                continue;
            }
            if (x.getLocalName().equals("VersionInfo")) {
                continue;
            }
            if (x.getLocalName().equals("Classification")) {
                continue;
            }
            if (x.getLocalName().equals("ExternalIdentifier")) {
                continue;
            }
            if (x.getLocalName().equals("ObjectRef")) {
                continue;
            }

            to.addChild(deep_copy(x, new_namespace));
        }

        String text = from.getText();
        to.setText(text);

        return to;
    }

    /**
     *
     * @param from
     * @param to
     */
    protected void copy_attributes(OMElement from, OMElement to) {
        String element_name = from.getLocalName();
        for (Iterator it = from.getAllAttributes(); it.hasNext();) {
            OMAttribute from_a = (OMAttribute) it.next();
            String name = from_a.getLocalName();
            String value = from_a.getAttributeValue();
            OMNamespace xml_namespace = MetadataSupport.xml_namespace;
            OMNamespace namespace = null;
            if (name.equals("status")) {
                value = m.addNamespace(value, MetadataSupport.status_type_namespace);
            } else if (name.equals("associationType")) {
                value = m.addNamespace(value, this.v3AssociationNamespace(value));
            } else if (name.equals("minorVersion")) {
                continue;
            } else if (name.equals("majorVersion")) {
                continue;
            } else if (name.equals("lang")) {
                namespace = xml_namespace;
            }
//			else if (name.equals("objectType") && ! value.startsWith("urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:"))
//			value = "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:" + value;
            OMAttribute to_a = MetadataSupport.om_factory.createOMAttribute(name, namespace, value);
            to.addAttribute(to_a);
        }
        if (element_name.equals("RegistryPackage")) {
            OMAttribute object_type_att = to.getAttribute(MetadataSupport.object_type_qname);
            if (object_type_att == null) {
                object_type_att = MetadataSupport.om_factory.createOMAttribute("objectType", null, "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:RegistryPackage");
                to.addAttribute(object_type_att);
            } else {
                object_type_att.setAttributeValue("urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:RegistryPackage");
            }
        } else if (element_name.equals("Association")) {
            OMAttribute object_type_att = to.getAttribute(MetadataSupport.object_type_qname);
            if (object_type_att == null) {
                object_type_att = MetadataSupport.om_factory.createOMAttribute("objectType", null, "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association");
                to.addAttribute(object_type_att);
            } else {
                object_type_att.setAttributeValue("urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association");
            }
        } else if (element_name.equals("Classification")) {
            OMAttribute object_type_att = to.getAttribute(MetadataSupport.object_type_qname);
            if (object_type_att == null) {
                object_type_att = MetadataSupport.om_factory.createOMAttribute("objectType", null, "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification");
                to.addAttribute(object_type_att);
            } else {
                object_type_att.setAttributeValue("urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification");
            }
        } else if (element_name.equals("ExternalIdentifier")) {
            OMAttribute object_type_att = to.getAttribute(MetadataSupport.object_type_qname);
            if (object_type_att == null) {
                object_type_att = MetadataSupport.om_factory.createOMAttribute("objectType", null, "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalIdentifier");
                to.addAttribute(object_type_att);
            } else {
                object_type_att.setAttributeValue("urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalIdentifier");
            }
        } else if (element_name.equals("ObjectRef")) {
//			OMAttribute object_type_att = to.getAttribute(MetadataSupport.object_type_qname);
//			if (object_type_att == null) {
//				object_type_att = MetadataSupport.om_factory.createOMAttribute("objectType", null, "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ObjectRef");
//				to.addAttribute(object_type_att);
//			} else {
//				object_type_att.setAttributeValue("urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ObjectRef");
//			}
        }
    }

    /**
     * 
     */
    private static final List<String> iheAssocTypes = new ArrayList<String>() {
        {
            add("APND");
            add("XFRM");
            add("RPLC");
            add("XFRM_RPLC");
            add("signs");
        }
    };
     /**
     *
     * @param simpleAssociationType
     * @return
     */
    private String v3AssociationNamespace(String simpleAssociationType) {
        if (iheAssocTypes.contains(simpleAssociationType)) {
            return MetadataSupport.xdsB_ihe_assoc_namespace_uri;
        } else {
            return MetadataSupport.xdsB_eb_assoc_namespace_uri;
        }
    }
}
