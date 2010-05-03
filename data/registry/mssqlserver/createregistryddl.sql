--
--  This code is subject to the HIEOS License, Version 1.0
-- 
--  Copyright(c) 2008-2009 Vangent, Inc.  All rights reserved.
-- 
--  Unless required by applicable law or agreed to in writing, software
--  distributed under the License is distributed on an "AS IS" BASIS,
--  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- 
--  See the License for the specific language governing permissions and
--  limitations under the License.
--

--
-- MS SQL Server DDL for Registry - OMAR Schema
--

--
-- DROP Tables
--
DROP TABLE adhocquery;
DROP TABLE affectedobject;
DROP TABLE association;
DROP TABLE auditableevent;
DROP TABLE classification;
DROP TABLE classificationnode;
DROP TABLE classscheme;
DROP TABLE description;
DROP TABLE emailaddress;
DROP TABLE externalidentifier;
DROP TABLE externallink;
DROP TABLE extrinsicobject;
DROP TABLE federation;
DROP TABLE name_;
DROP TABLE notification;
DROP TABLE notificationobject;
DROP TABLE notifyaction;
DROP TABLE objectref;
DROP TABLE organization;
DROP TABLE person;
DROP TABLE postaladdress;
DROP TABLE registry;
--DROP TABLE registryobject;
DROP TABLE registrypackage;
DROP TABLE repositoryitem;
DROP TABLE service;
DROP TABLE servicebinding;
DROP TABLE slot;
DROP TABLE specificationlink;
DROP TABLE subscription;
DROP TABLE telephonenumber;
DROP TABLE usagedescription;
DROP TABLE usageparameter;
DROP TABLE user_;



--
-- TOC entry 1565 (class 1259 OID 78386)
-- Dependencies: 1867 6
-- Name: adhocquery; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE adhocquery (
    id VARCHAR(256) NOT NULL,
    home VARCHAR(256),
    lid VARCHAR(256) NOT NULL,
    objecttype VARCHAR(256),
    status VARCHAR(256) NOT NULL,
    versionname VARCHAR(16),
    comment_ VARCHAR(256),
    querylanguage VARCHAR(256) NOT NULL,
    query VARCHAR(4096) NOT NULL
    --CONSTRAINT adhocquery_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AdhocQuery'::text))
);


--
-- TOC entry 1566 (class 1259 OID 78394)
-- Dependencies: 6
-- Name: affectedobject; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE affectedobject (
    id VARCHAR(256) NOT NULL,
    home VARCHAR(256),
    eventid VARCHAR(256) NOT NULL
);


--
-- TOC entry 1567 (class 1259 OID 78400)
-- Dependencies: 1868 6
-- Name: association; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE association (
    id VARCHAR(64) NOT NULL,
    home VARCHAR(64),
    lid VARCHAR(64) NOT NULL,
    objecttype VARCHAR(128),
    status VARCHAR(128) NOT NULL,
    versionname VARCHAR(16),
    comment_ VARCHAR(256),
    associationtype VARCHAR(128) NOT NULL,
    sourceobject VARCHAR(64) NOT NULL,
    targetobject VARCHAR(64) NOT NULL
  --  CONSTRAINT association_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association'::text))
);



--
-- TOC entry 1568 (class 1259 OID 78407)
-- Dependencies: 1869 6
-- Name: auditableevent; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE auditableevent (
    id VARCHAR(256) NOT NULL,
    home VARCHAR(256),
    lid VARCHAR(256) NOT NULL,
    objecttype VARCHAR(256),
    status VARCHAR(256) NOT NULL,
    versionname VARCHAR(16),
    comment_ VARCHAR(256),
    requestid VARCHAR(256) NOT NULL,
    eventtype VARCHAR(256) NOT NULL,
    timestamp_ VARCHAR(30) NOT NULL,
    user_ VARCHAR(256) NOT NULL
    --CONSTRAINT auditableevent_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AuditableEvent'::text))
);


--
-- TOC entry 1569 (class 1259 OID 78414)
-- Dependencies: 1870 6
-- Name: classification; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE classification (
    id VARCHAR(64) NOT NULL,
    home VARCHAR(64),
    lid VARCHAR(64) NOT NULL,
    objecttype VARCHAR(128),
    status VARCHAR(128) NOT NULL,
    versionname VARCHAR(16),
    comment_ VARCHAR(256),
    classificationnode VARCHAR(64),
    classificationscheme VARCHAR(64),
    classifiedobject VARCHAR(64) NOT NULL,
    noderepresentation VARCHAR(128)
    --CONSTRAINT classification_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification'::text))
);


--
-- TOC entry 1570 (class 1259 OID 78421)
-- Dependencies: 1871 6
-- Name: classificationnode; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE classificationnode (
    id VARCHAR(64) NOT NULL,
    home VARCHAR(64),
    lid VARCHAR(64) NOT NULL,
    objecttype VARCHAR(128),
    status VARCHAR(128) NOT NULL,
    versionname VARCHAR(16),
    comment_ VARCHAR(256),
    code VARCHAR(64),
    parent VARCHAR(64),
    path VARCHAR(1024)
   --CONSTRAINT classificationnode_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ClassificationNode'::text))
);


--
-- TOC entry 1571 (class 1259 OID 78428)
-- Dependencies: 1872 6
-- Name: classscheme; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE classscheme (
    id VARCHAR(64) NOT NULL,
    home VARCHAR(64),
    lid VARCHAR(64) NOT NULL,
    objecttype VARCHAR(128),
    status VARCHAR(128) NOT NULL,
    versionname VARCHAR(16),
    comment_ VARCHAR(256),
    isinternal CHAR(1) NOT NULL,
    nodetype VARCHAR(128) NOT NULL
    --CONSTRAINT classscheme_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ClassificationScheme'::text))
);


--
-- TOC entry 1572 (class 1259 OID 78435)
-- Dependencies: 6
-- Name: description; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE description (
    charset VARCHAR(32),
    lang VARCHAR(32) NOT NULL,
    value VARCHAR(256) NOT NULL,
    parent VARCHAR(64) NOT NULL
);


--
-- TOC entry 1573 (class 1259 OID 78441)
-- Dependencies: 6
-- Name: emailaddress; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE emailaddress (
    address VARCHAR(64) NOT NULL,
    type VARCHAR(256),
    parent VARCHAR(256) NOT NULL
);


--
-- TOC entry 1574 (class 1259 OID 78447)
-- Dependencies: 1873 6
-- Name: externalidentifier; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE externalidentifier (
    id VARCHAR(64) NOT NULL,
    home VARCHAR(64),
    lid VARCHAR(64) NOT NULL,
    objecttype VARCHAR(128),
    status VARCHAR(128) NOT NULL,
    versionname VARCHAR(16),
    comment_ VARCHAR(256),
    registryobject VARCHAR(64) NOT NULL,
    identificationscheme VARCHAR(64) NOT NULL,
    value VARCHAR(128) NOT NULL
    --CONSTRAINT externalidentifier_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalIdentifier'::text))
);


--
-- TOC entry 1575 (class 1259 OID 78454)
-- Dependencies: 6
-- Name: externallink; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE externallink (
    id VARCHAR(256) NOT NULL,
    home VARCHAR(256),
    lid VARCHAR(256) NOT NULL,
    objecttype VARCHAR(256),
    status VARCHAR(256) NOT NULL,
    versionname VARCHAR(16),
    comment_ VARCHAR(256),
    externaluri VARCHAR(256) NOT NULL
);


--
-- TOC entry 1576 (class 1259 OID 78460)
-- Dependencies: 6
-- Name: extrinsicobject; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE extrinsicobject (
    id VARCHAR(64) NOT NULL,
    home VARCHAR(64),
    lid VARCHAR(64) NOT NULL,
    objecttype VARCHAR(128),
    status VARCHAR(128) NOT NULL,
    versionname VARCHAR(16),
    comment_ VARCHAR(256),
    isopaque CHAR(1) NOT NULL,
    mimetype VARCHAR(128),
    contentversionname VARCHAR(16),
    contentversioncomment VARCHAR(256)
);


--
-- TOC entry 1577 (class 1259 OID 78466)
-- Dependencies: 1874 6
-- Name: federation; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE federation (
    id VARCHAR(256) NOT NULL,
    home VARCHAR(256),
    lid VARCHAR(256) NOT NULL,
    objecttype VARCHAR(256),
    status VARCHAR(256) NOT NULL,
    versionname VARCHAR(16),
    comment_ VARCHAR(256),
    replicationsynclatency VARCHAR(32)
    --CONSTRAINT federation_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Federation'::text))
);


--
-- TOC entry 1578 (class 1259 OID 78474)
-- Dependencies: 6
-- Name: objectref; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE objectref (
    id VARCHAR(64) NOT NULL,
    home VARCHAR(64)
);


--
-- TOC entry 1579 (class 1259 OID 78481)
-- Dependencies: 1875 6
-- Name: organization; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE organization (
    id VARCHAR(256) NOT NULL,
    home VARCHAR(256),
    lid VARCHAR(256) NOT NULL,
    objecttype VARCHAR(256),
    status VARCHAR(256) NOT NULL,
    versionname VARCHAR(16),
    comment_ VARCHAR(256),
    parent VARCHAR(256),
    primarycontact VARCHAR(256)
    --CONSTRAINT organization_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Organization'::text))
);


--
-- TOC entry 1580 (class 1259 OID 78488)
-- Dependencies: 1876 6
-- Name: person; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE person (
    id VARCHAR(256) NOT NULL,
    home VARCHAR(256),
    lid VARCHAR(256) NOT NULL,
    objecttype VARCHAR(256),
    status VARCHAR(256) NOT NULL,
    versionname VARCHAR(16),
    comment_ VARCHAR(256),
    personname_firstname VARCHAR(64),
    personname_middlename VARCHAR(64),
    personname_lastname VARCHAR(64)
    --CONSTRAINT person_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Person'::text))
);


--
-- TOC entry 1581 (class 1259 OID 78495)
-- Dependencies: 1877 1878 1879 6
-- Name: registry; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE registry (
    id VARCHAR(256) NOT NULL,
    home VARCHAR(256),
    lid VARCHAR(256) NOT NULL,
    objecttype VARCHAR(256),
    status VARCHAR(256) NOT NULL,
    versionname VARCHAR(16),
    comment_ VARCHAR(256),
    catalogingsynclatency VARCHAR(32) DEFAULT 'P1D',
    conformanceprofile VARCHAR(16),
    operator VARCHAR(256) NOT NULL,
    replicationsynclatency VARCHAR(32) DEFAULT 'P1D',
    specificationversion VARCHAR(8) NOT NULL
    --CONSTRAINT registry_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Registry'::text))
);


--
-- TOC entry 1582 (class 1259 OID 78504)
-- Dependencies: 1880 6
-- Name: registrypackage; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE registrypackage (
    id VARCHAR(64) NOT NULL,
    home VARCHAR(64),
    lid VARCHAR(64) NOT NULL,
    objecttype VARCHAR(128),
    status VARCHAR(128) NOT NULL,
    versionname VARCHAR(16),
    comment_ VARCHAR(256)
    --CONSTRAINT registrypackage_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:RegistryPackage'::text))
);


--
-- TOC entry 1583 (class 1259 OID 78511)
-- Dependencies: 1881 6
-- Name: service; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE service (
    id VARCHAR(256) NOT NULL,
    home VARCHAR(256),
    lid VARCHAR(256) NOT NULL,
    objecttype VARCHAR(256),
    status VARCHAR(256) NOT NULL,
    versionname VARCHAR(16),
    comment_ VARCHAR(256)
    -- CONSTRAINT service_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Service'::text))
);


--
-- TOC entry 1584 (class 1259 OID 78518)
-- Dependencies: 1882 6
-- Name: servicebinding; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE servicebinding (
    id VARCHAR(256) NOT NULL,
    home VARCHAR(256),
    lid VARCHAR(256) NOT NULL,
    objecttype VARCHAR(256),
    status VARCHAR(256) NOT NULL,
    versionname VARCHAR(16),
    comment_ VARCHAR(256),
    service VARCHAR(256) NOT NULL,
    accessuri VARCHAR(256),
    targetbinding VARCHAR(256)
    --CONSTRAINT servicebinding_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ServiceBinding'::text))
);


--
-- TOC entry 1585 (class 1259 OID 78525)
-- Dependencies: 1883 6
-- Name: specificationlink; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE specificationlink (
    id VARCHAR(256) NOT NULL,
    home VARCHAR(256),
    lid VARCHAR(256) NOT NULL,
    objecttype VARCHAR(256),
    status VARCHAR(256) NOT NULL,
    versionname VARCHAR(16),
    comment_ VARCHAR(256),
    servicebinding VARCHAR(256) NOT NULL,
    specificationobject VARCHAR(256) NOT NULL
    --CONSTRAINT specificationlink_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:SpecificationLink'::text))
);


--
-- TOC entry 1586 (class 1259 OID 78532)
-- Dependencies: 1884 1885 6
-- Name: subscription; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE subscription (
    id VARCHAR(256) NOT NULL,
    home VARCHAR(256),
    lid VARCHAR(256) NOT NULL,
    objecttype VARCHAR(256),
    status VARCHAR(256) NOT NULL,
    versionname VARCHAR(16),
    comment_ VARCHAR(256),
    selector VARCHAR(256) NOT NULL,
    endtime VARCHAR(30),
    notificationinterval VARCHAR(32) DEFAULT 'P1D',
    starttime VARCHAR(30)
    --CONSTRAINT subscription_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Subscription'::text))
);


--
-- TOC entry 1587 (class 1259 OID 78540)
-- Dependencies: 1886 6
-- Name: user_; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE user_ (
    id VARCHAR(64) NOT NULL,
    home VARCHAR(64),
    lid VARCHAR(64) NOT NULL,
    objecttype VARCHAR(128),
    status VARCHAR(128) NOT NULL,
    versionname VARCHAR(16),
    comment_ VARCHAR(256),
    personname_firstname VARCHAR(64),
    personname_middlename VARCHAR(64),
    personname_lastname VARCHAR(64)
    --CONSTRAINT user__objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Person:User'::text))
);


--
-- TOC entry 1588 (class 1259 OID 78547)
-- Dependencies: 1675 6
-- Name: identifiable; Type: VIEW; Schema: public; Owner: omar
--

--CREATE VIEW identifiable AS
--    ((((((((((((((((((SELECT adhocquery.id, adhocquery.home FROM adhocquery UNION ALL SELECT association.id, association.home --FROM association) UNION ALL SELECT auditableevent.id, auditableevent.home FROM auditableevent) UNION ALL SELECT --classification.id, classification.home FROM classification) UNION ALL SELECT classificationnode.id, classificationnode.home FROM --classificationnode) UNION ALL SELECT classscheme.id, classscheme.home FROM classscheme) UNION ALL SELECT externalidentifier.id, --externalidentifier.home FROM externalidentifier) UNION ALL SELECT externallink.id, externallink.home FROM externallink) UNION ALL --SELECT extrinsicobject.id, extrinsicobject.home FROM extrinsicobject) UNION ALL SELECT federation.id, federation.home FROM --federation) UNION ALL SELECT organization.id, organization.home FROM organization) UNION ALL SELECT registry.id, registry.home --FROM registry) UNION ALL SELECT registrypackage.id, registrypackage.home FROM registrypackage) UNION ALL SELECT service.id, --service.home FROM service) UNION ALL SELECT servicebinding.id, servicebinding.home FROM servicebinding) UNION ALL SELECT --specificationlink.id, specificationlink.home FROM specificationlink) UNION ALL SELECT subscription.id, subscription.home FROM --subscription) UNION ALL SELECT user_.id, user_.home FROM user_) UNION ALL SELECT person.id, person.home FROM person) UNION ALL --SELECT objectref.id, objectref.home FROM objectref;


--ALTER TABLE public.identifiable OWNER TO omar;

--
-- TOC entry 1589 (class 1259 OID 78552)
-- Dependencies: 6
-- Name: name_; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE name_ (
    charset VARCHAR(32),
    lang VARCHAR(32) NOT NULL,
    value VARCHAR(256) NOT NULL,
    parent VARCHAR(64) NOT NULL
);


--
-- TOC entry 1590 (class 1259 OID 78558)
-- Dependencies: 1887 6
-- Name: notification; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE notification (
    id VARCHAR(256) NOT NULL,
    home VARCHAR(256),
    lid VARCHAR(256) NOT NULL,
    objecttype VARCHAR(256),
    status VARCHAR(256) NOT NULL,
    versionname VARCHAR(16),
    comment_ VARCHAR(256),
    subscription VARCHAR(256) NOT NULL
    --CONSTRAINT notification_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Notification'::text))
);


--
-- TOC entry 1591 (class 1259 OID 78567)
-- Dependencies: 6
-- Name: notificationobject; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE notificationobject (
    notificationid VARCHAR(256) NOT NULL,
    registryobjectid VARCHAR(256) NOT NULL
);


--
-- TOC entry 1592 (class 1259 OID 78573)
-- Dependencies: 6
-- Name: notifyaction; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE notifyaction (
    notificationoption VARCHAR(256) NOT NULL,
    endpoint VARCHAR(256) NOT NULL,
    parent VARCHAR(256) NOT NULL
);


--
-- TOC entry 1593 (class 1259 OID 78579)
-- Dependencies: 6
-- Name: postaladdress; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE postaladdress (
    city VARCHAR(64),
    country VARCHAR(64),
    postalcode VARCHAR(64),
    state VARCHAR(64),
    street VARCHAR(64),
    streetnumber VARCHAR(32),
    parent VARCHAR(256) NOT NULL
);


--
-- TOC entry 1594 (class 1259 OID 78585)
-- Dependencies: 6
-- Name: registryobject; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

--CREATE TABLE registryobject (
--    id VARCHAR(256) NOT NULL,
--    home VARCHAR(256),
--    lid VARCHAR(256) NOT NULL,
--    objecttype VARCHAR(256) NOT NULL,
--    status VARCHAR(256) NOT NULL,
--    versionname VARCHAR(16),
--    comment_ VARCHAR(256)
--);


--ALTER TABLE public.registryobject OWNER TO omar;

--
-- TOC entry 1595 (class 1259 OID 78591)
-- Dependencies: 6
-- Name: repositoryitem; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE repositoryitem (
    lid VARCHAR(256) NOT NULL,
    versionname VARCHAR(16) NOT NULL,
    content IMAGE
);


--
-- TOC entry 1596 (class 1259 OID 78597)
-- Dependencies: 6
-- Name: slot; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE slot (
    sequenceid INT NOT NULL,
    name_ VARCHAR(128) NOT NULL,
    slottype VARCHAR(128),
    value VARCHAR(128),
    parent VARCHAR(64) NOT NULL
);


--
-- TOC entry 1597 (class 1259 OID 78603)
-- Dependencies: 6
-- Name: telephonenumber; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE telephonenumber (
    areacode VARCHAR(8),
    countrycode VARCHAR(8),
    extension VARCHAR(8),
    number_ VARCHAR(16),
    phonetype VARCHAR(256),
    parent VARCHAR(256) NOT NULL
);


--
-- TOC entry 1598 (class 1259 OID 78609)
-- Dependencies: 6
-- Name: usagedescription; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE usagedescription (
    charset VARCHAR(32),
    lang VARCHAR(32) NOT NULL,
    value VARCHAR(1024) NOT NULL,
    parent VARCHAR(256) NOT NULL
);


--
-- TOC entry 1599 (class 1259 OID 78615)
-- Dependencies: 6
-- Name: usageparameter; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE usageparameter (
    value VARCHAR(1024) NOT NULL,
    parent VARCHAR(256) NOT NULL
);


--
-- TOC entry 1889 (class 2606 OID 78622)
-- Dependencies: 1565 1565
-- Name: adhocquery_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE adhocquery
    ADD CONSTRAINT adhocquery_pkey PRIMARY KEY (id);


--
-- TOC entry 1891 (class 2606 OID 78624)
-- Dependencies: 1566 1566 1566
-- Name: affectedobject_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE affectedobject
    ADD CONSTRAINT affectedobject_pkey PRIMARY KEY (id, eventid);


--
-- TOC entry 1895 (class 2606 OID 78626)
-- Dependencies: 1567 1567
-- Name: association_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE association
    ADD CONSTRAINT association_pkey PRIMARY KEY (id);


--
-- TOC entry 1900 (class 2606 OID 78628)
-- Dependencies: 1568 1568
-- Name: auditableevent_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE auditableevent
    ADD CONSTRAINT auditableevent_pkey PRIMARY KEY (id);


--
-- TOC entry 1903 (class 2606 OID 78630)
-- Dependencies: 1569 1569
-- Name: classification_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE classification
    ADD CONSTRAINT classification_pkey PRIMARY KEY (id);


--
-- TOC entry 1906 (class 2606 OID 78632)
-- Dependencies: 1570 1570
-- Name: classificationnode_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE classificationnode
    ADD CONSTRAINT classificationnode_pkey PRIMARY KEY (id);


--
-- TOC entry 1911 (class 2606 OID 78635)
-- Dependencies: 1571 1571
-- Name: classscheme_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE classscheme
    ADD CONSTRAINT classscheme_pkey PRIMARY KEY (id);


--
-- TOC entry 1913 (class 2606 OID 79837)
-- Dependencies: 1572 1572
-- Name: description_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE description
    ADD CONSTRAINT description_pkey PRIMARY KEY (parent);


--
-- TOC entry 1916 (class 2606 OID 78640)
-- Dependencies: 1574 1574
-- Name: externalidentifier_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE externalidentifier
    ADD CONSTRAINT externalidentifier_pkey PRIMARY KEY (id);


--
-- TOC entry 1921 (class 2606 OID 78642)
-- Dependencies: 1575 1575
-- Name: externallink_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE externallink
    ADD CONSTRAINT externallink_pkey PRIMARY KEY (id);


--
-- TOC entry 1924 (class 2606 OID 78644)
-- Dependencies: 1576 1576
-- Name: extrinsicobject_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE extrinsicobject
    ADD CONSTRAINT extrinsicobject_pkey PRIMARY KEY (id);


--
-- TOC entry 1926 (class 2606 OID 78646)
-- Dependencies: 1577 1577
-- Name: federation_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE federation
    ADD CONSTRAINT federation_pkey PRIMARY KEY (id);


--
-- TOC entry 1953 (class 2606 OID 80294)
-- Dependencies: 1589 1589
-- Name: name__pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE name_
    ADD CONSTRAINT name__pkey PRIMARY KEY (parent);


--
-- TOC entry 1955 (class 2606 OID 78650)
-- Dependencies: 1590 1590
-- Name: notification_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE notification
    ADD CONSTRAINT notification_pkey PRIMARY KEY (id);


--
-- TOC entry 1957 (class 2606 OID 78652)
-- Dependencies: 1591 1591 1591
-- Name: notificationobject_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE notificationobject
    ADD CONSTRAINT notificationobject_pkey PRIMARY KEY (notificationid, registryobjectid);


--
-- TOC entry 1928 (class 2606 OID 78655)
-- Dependencies: 1578 1578
-- Name: objectref_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE objectref
    ADD CONSTRAINT objectref_pkey PRIMARY KEY (id);


--
-- TOC entry 1930 (class 2606 OID 78658)
-- Dependencies: 1579 1579
-- Name: organization_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE organization
    ADD CONSTRAINT organization_pkey PRIMARY KEY (id);


--
-- TOC entry 1934 (class 2606 OID 78660)
-- Dependencies: 1580 1580
-- Name: person_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE person
    ADD CONSTRAINT person_pkey PRIMARY KEY (id);


--
-- TOC entry 1936 (class 2606 OID 78662)
-- Dependencies: 1581 1581
-- Name: registry_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE registry
    ADD CONSTRAINT registry_pkey PRIMARY KEY (id);


--
-- TOC entry 1963 (class 2606 OID 78664)
-- Dependencies: 1594 1594 1594
-- Name: registryobject_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

--ALTER TABLE registryobject
--    ADD CONSTRAINT registryobject_pkey PRIMARY KEY (id, objecttype);


--
-- TOC entry 1938 (class 2606 OID 78666)
-- Dependencies: 1582 1582
-- Name: registrypackage_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE registrypackage
    ADD CONSTRAINT registrypackage_pkey PRIMARY KEY (id);


--
-- TOC entry 1965 (class 2606 OID 78668)
-- Dependencies: 1595 1595 1595
-- Name: repositoryitem_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE repositoryitem
    ADD CONSTRAINT repositoryitem_pkey PRIMARY KEY (lid, versionname);


--
-- TOC entry 1940 (class 2606 OID 78670)
-- Dependencies: 1583 1583
-- Name: service_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE service
    ADD CONSTRAINT service_pkey PRIMARY KEY (id);


--
-- TOC entry 1943 (class 2606 OID 78672)
-- Dependencies: 1584 1584
-- Name: servicebinding_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE servicebinding
    ADD CONSTRAINT servicebinding_pkey PRIMARY KEY (id);


--
-- TOC entry 1967 (class 2606 OID 78674)
-- Dependencies: 1596 1596 1596 1596
-- Name: slot_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE slot
    ADD CONSTRAINT slot_pkey PRIMARY KEY (parent, name_, sequenceid);


--
-- TOC entry 1947 (class 2606 OID 78676)
-- Dependencies: 1585 1585
-- Name: specificationlink_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE specificationlink
    ADD CONSTRAINT specificationlink_pkey PRIMARY KEY (id);


--
-- TOC entry 1949 (class 2606 OID 78678)
-- Dependencies: 1586 1586
-- Name: subscription_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE subscription
    ADD CONSTRAINT subscription_pkey PRIMARY KEY (id);


--
-- TOC entry 1970 (class 2606 OID 78680)
-- Dependencies: 1598 1598 1598
-- Name: usagedescription_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE usagedescription
    ADD CONSTRAINT usagedescription_pkey PRIMARY KEY (parent, lang);


--
-- TOC entry 1951 (class 2606 OID 78682)
-- Dependencies: 1587 1587
-- Name: user__pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE user_
    ADD CONSTRAINT user__pkey PRIMARY KEY (id);


--
-- TOC entry 1944 (class 1259 OID 78683)
-- Dependencies: 1585
-- Name: binding_slnk_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX binding_slnk_idx ON specificationlink (servicebinding);


--
-- TOC entry 1958 (class 1259 OID 78684)
-- Dependencies: 1593
-- Name: city_pstladr_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX city_pstladr_idx ON postaladdress (city);


--
-- TOC entry 1904 (class 1259 OID 78686)
-- Dependencies: 1569
-- Name: clsobj_class_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX clsobj_class_idx ON classification (classifiedobject);


--
-- TOC entry 1959 (class 1259 OID 78687)
-- Dependencies: 1593
-- Name: cntry_pstladr_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX cntry_pstladr_idx ON postaladdress (country);


--
-- TOC entry 1907 (class 1259 OID 78689)
-- Dependencies: 1570
-- Name: code_node_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX code_node_idx ON classificationnode (code);


--
-- TOC entry 1892 (class 1259 OID 78690)
-- Dependencies: 1566
-- Name: evid_afobj_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX evid_afobj_idx ON affectedobject (eventid);


--
-- TOC entry 1893 (class 1259 OID 78714)
-- Dependencies: 1566
-- Name: id_afobj_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX id_afobj_idx ON affectedobject (id);


--
-- TOC entry 1917 (class 1259 OID 80024)
-- Dependencies: 1574
-- Name: idscheme_eid_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX idscheme_eid_idx ON externalidentifier (identificationscheme);


--
-- TOC entry 1932 (class 1259 OID 78738)
-- Dependencies: 1580
-- Name: lastnm_person_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX lastnm_person_idx ON person (personname_lastname);


--
-- TOC entry 1901 (class 1259 OID 78742)
-- Dependencies: 1568
-- Name: lid_auevent_evttyp; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX lid_auevent_evttyp ON auditableevent (eventtype);


--
-- TOC entry 1914 (class 1259 OID 78770)
-- Dependencies: 1573
-- Name: parent_emladr_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX parent_emladr_idx ON emailaddress (parent);


--
-- TOC entry 1908 (class 1259 OID 78771)
-- Dependencies: 1570
-- Name: parent_node_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX parent_node_idx ON classificationnode (parent);


--
-- TOC entry 1931 (class 1259 OID 78772)
-- Dependencies: 1579
-- Name: parent_org_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX parent_org_idx ON organization (parent);


--
-- TOC entry 1968 (class 1259 OID 78773)
-- Dependencies: 1597
-- Name: parent_phone_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX parent_phone_idx ON telephonenumber (parent);


--
-- TOC entry 1960 (class 1259 OID 78774)
-- Dependencies: 1593
-- Name: parent_pstladr_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX parent_pstladr_idx ON postaladdress (parent);


--
-- TOC entry 1909 (class 1259 OID 78776)
-- Dependencies: 1570
-- Name: path_node_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX path_node_idx ON classificationnode (path);


--
-- TOC entry 1961 (class 1259 OID 78777)
-- Dependencies: 1593
-- Name: pcode_pstladr_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX pcode_pstladr_idx ON postaladdress (postalcode);


--
-- TOC entry 1918 (class 1259 OID 78778)
-- Dependencies: 1574
-- Name: ro_eid_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX ro_eid_idx ON externalidentifier (registryobject);


--
-- TOC entry 1941 (class 1259 OID 78779)
-- Dependencies: 1584
-- Name: service_bind_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX service_bind_idx ON servicebinding (service);


--
-- TOC entry 1945 (class 1259 OID 78780)
-- Dependencies: 1585
-- Name: spec_slnk_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX spec_slnk_idx ON specificationlink (specificationobject);


--
-- TOC entry 1896 (class 1259 OID 78781)
-- Dependencies: 1567
-- Name: src_ass_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX src_ass_idx ON association (sourceobject);


--
-- TOC entry 1897 (class 1259 OID 78783)
-- Dependencies: 1567
-- Name: tgt_ass_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX tgt_ass_idx ON association (targetobject);


--
-- TOC entry 1898 (class 1259 OID 78785)
-- Dependencies: 1567
-- Name: type_ass_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX type_ass_idx ON association (associationtype);


--
-- TOC entry 1922 (class 1259 OID 78786)
-- Dependencies: 1575
-- Name: uri_exlink_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX uri_exlink_idx ON externallink (externaluri);


--
-- TOC entry 1919 (class 1259 OID 80074)
-- Dependencies: 1574
-- Name: value_eid_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX value_eid_idx ON externalidentifier (value);


--
-- TOC entry 1971 (class 1259 OID 78789)
-- Dependencies: 1598
-- Name: value_usgdes_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX value_usgdes_idx ON usagedescription (value);

-- View: registryobject

-- DROP VIEW registryobject;

--CREATE OR REPLACE VIEW registryobject AS 
--(SELECT extrinsicobject.id, extrinsicobject.objecttype
--   FROM extrinsicobject)
--UNION 
-- SELECT registrypackage.id, registrypackage.objecttype
--   FROM registrypackage;
