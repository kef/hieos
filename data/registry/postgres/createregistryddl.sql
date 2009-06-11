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
-- PostgreSQL database dump
--

-- Started on 2009-05-15 13:58:34

SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- TOC entry 393 (class 2612 OID 16386)
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: postgres
--

-- CREATE PROCEDURAL LANGUAGE plpgsql;


ALTER PROCEDURAL LANGUAGE plpgsql OWNER TO omar;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 1592 (class 1259 OID 50582)
-- Dependencies: 1885 3
-- Name: adhocquery; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE adhocquery (
    id character varying(256) NOT NULL,
    home character varying(256),
    lid character varying(256) NOT NULL,
    objecttype character varying(256),
    status character varying(256) NOT NULL,
    versionname character varying(16),
    comment_ character varying(256),
    querylanguage character varying(256) NOT NULL,
    query character varying(4096) NOT NULL,
    CONSTRAINT adhocquery_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AdhocQuery'::text))
);


ALTER TABLE public.adhocquery OWNER TO omar;

--
-- TOC entry 1567 (class 1259 OID 50372)
-- Dependencies: 3
-- Name: affectedobject; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE affectedobject (
    id character varying(256) NOT NULL,
    home character varying(256),
    eventid character varying(256) NOT NULL
);


ALTER TABLE public.affectedobject OWNER TO omar;

--
-- TOC entry 1565 (class 1259 OID 50354)
-- Dependencies: 1867 3
-- Name: association; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE association (
    id character varying(256) NOT NULL,
    home character varying(256),
    lid character varying(256) NOT NULL,
    objecttype character varying(256),
    status character varying(256) NOT NULL,
    versionname character varying(16),
    comment_ character varying(256),
    associationtype character varying(256) NOT NULL,
    sourceobject character varying(256) NOT NULL,
    targetobject character varying(256) NOT NULL,
    CONSTRAINT association_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association'::text))
);


ALTER TABLE public.association OWNER TO omar;

--
-- TOC entry 1566 (class 1259 OID 50363)
-- Dependencies: 1868 3
-- Name: auditableevent; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE auditableevent (
    id character varying(256) NOT NULL,
    home character varying(256),
    lid character varying(256) NOT NULL,
    objecttype character varying(256),
    status character varying(256) NOT NULL,
    versionname character varying(16),
    comment_ character varying(256),
    requestid character varying(256) NOT NULL,
    eventtype character varying(256) NOT NULL,
    timestamp_ character varying(30) NOT NULL,
    user_ character varying(256) NOT NULL,
    CONSTRAINT auditableevent_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AuditableEvent'::text))
);


ALTER TABLE public.auditableevent OWNER TO omar;

--
-- TOC entry 1568 (class 1259 OID 50380)
-- Dependencies: 1869 3
-- Name: classification; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE classification (
    id character varying(256) NOT NULL,
    home character varying(256),
    lid character varying(256) NOT NULL,
    objecttype character varying(256),
    status character varying(256) NOT NULL,
    versionname character varying(16),
    comment_ character varying(256),
    classificationnode character varying(256),
    classificationscheme character varying(256),
    classifiedobject character varying(256) NOT NULL,
    noderepresentation character varying(256),
    CONSTRAINT classification_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification'::text))
);


ALTER TABLE public.classification OWNER TO omar;

--
-- TOC entry 1569 (class 1259 OID 50389)
-- Dependencies: 1870 3
-- Name: classificationnode; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE classificationnode (
    id character varying(256) NOT NULL,
    home character varying(256),
    lid character varying(256) NOT NULL,
    objecttype character varying(256),
    status character varying(256) NOT NULL,
    versionname character varying(16),
    comment_ character varying(256),
    code character varying(256),
    parent character varying(256),
    path character varying(1024),
    CONSTRAINT classificationnode_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ClassificationNode'::text))
);


ALTER TABLE public.classificationnode OWNER TO omar;

--
-- TOC entry 1570 (class 1259 OID 50398)
-- Dependencies: 1871 3
-- Name: classscheme; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE classscheme (
    id character varying(256) NOT NULL,
    home character varying(256),
    lid character varying(256) NOT NULL,
    objecttype character varying(256),
    status character varying(256) NOT NULL,
    versionname character varying(16),
    comment_ character varying(256),
    isinternal character varying(1) NOT NULL,
    nodetype character varying(256) NOT NULL,
    CONSTRAINT classscheme_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ClassificationScheme'::text))
);


ALTER TABLE public.classscheme OWNER TO omar;

--
-- TOC entry 1576 (class 1259 OID 50449)
-- Dependencies: 3
-- Name: description; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE description (
    charset character varying(32),
    lang character varying(32) NOT NULL,
    value character varying(1024) NOT NULL,
    parent character varying(256) NOT NULL
);


ALTER TABLE public.description OWNER TO omar;

--
-- TOC entry 1582 (class 1259 OID 50497)
-- Dependencies: 3
-- Name: emailaddress; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE emailaddress (
    address character varying(64) NOT NULL,
    type character varying(256),
    parent character varying(256) NOT NULL
);


ALTER TABLE public.emailaddress OWNER TO omar;

--
-- TOC entry 1571 (class 1259 OID 50407)
-- Dependencies: 1872 3
-- Name: externalidentifier; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE externalidentifier (
    id character varying(256) NOT NULL,
    home character varying(256),
    lid character varying(256) NOT NULL,
    objecttype character varying(256),
    status character varying(256) NOT NULL,
    versionname character varying(16),
    comment_ character varying(256),
    registryobject character varying(256) NOT NULL,
    identificationscheme character varying(256) NOT NULL,
    value character varying(256) NOT NULL,
    CONSTRAINT externalidentifier_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalIdentifier'::text))
);


ALTER TABLE public.externalidentifier OWNER TO omar;

--
-- TOC entry 1572 (class 1259 OID 50416)
-- Dependencies: 3
-- Name: externallink; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE externallink (
    id character varying(256) NOT NULL,
    home character varying(256),
    lid character varying(256) NOT NULL,
    objecttype character varying(256),
    status character varying(256) NOT NULL,
    versionname character varying(16),
    comment_ character varying(256),
    externaluri character varying(256) NOT NULL
);


ALTER TABLE public.externallink OWNER TO omar;

--
-- TOC entry 1573 (class 1259 OID 50424)
-- Dependencies: 3
-- Name: extrinsicobject; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE extrinsicobject (
    id character varying(256) NOT NULL,
    home character varying(256),
    lid character varying(256) NOT NULL,
    objecttype character varying(256),
    status character varying(256) NOT NULL,
    versionname character varying(16),
    comment_ character varying(256),
    isopaque character varying(1) NOT NULL,
    mimetype character varying(256),
    contentversionname character varying(16),
    contentversioncomment character varying(256)
);


ALTER TABLE public.extrinsicobject OWNER TO omar;

--
-- TOC entry 1574 (class 1259 OID 50432)
-- Dependencies: 1873 3
-- Name: federation; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE federation (
    id character varying(256) NOT NULL,
    home character varying(256),
    lid character varying(256) NOT NULL,
    objecttype character varying(256),
    status character varying(256) NOT NULL,
    versionname character varying(16),
    comment_ character varying(256),
    replicationsynclatency character varying(32),
    CONSTRAINT federation_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Federation'::text))
);


ALTER TABLE public.federation OWNER TO omar;

--
-- TOC entry 1578 (class 1259 OID 50465)
-- Dependencies: 3
-- Name: objectref; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE objectref (
    id character varying(256) NOT NULL,
    home character varying(256)
);


ALTER TABLE public.objectref OWNER TO omar;

--
-- TOC entry 1579 (class 1259 OID 50473)
-- Dependencies: 1874 3
-- Name: organization; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE organization (
    id character varying(256) NOT NULL,
    home character varying(256),
    lid character varying(256) NOT NULL,
    objecttype character varying(256),
    status character varying(256) NOT NULL,
    versionname character varying(16),
    comment_ character varying(256),
    parent character varying(256),
    primarycontact character varying(256),
    CONSTRAINT organization_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Organization'::text))
);


ALTER TABLE public.organization OWNER TO omar;

--
-- TOC entry 1596 (class 1259 OID 50612)
-- Dependencies: 1887 3
-- Name: person; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE person (
    id character varying(256) NOT NULL,
    home character varying(256),
    lid character varying(256) NOT NULL,
    objecttype character varying(256),
    status character varying(256) NOT NULL,
    versionname character varying(16),
    comment_ character varying(256),
    personname_firstname character varying(64),
    personname_middlename character varying(64),
    personname_lastname character varying(64),
    CONSTRAINT person_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Person'::text))
);


ALTER TABLE public.person OWNER TO omar;

--
-- TOC entry 1583 (class 1259 OID 50503)
-- Dependencies: 1876 1877 1878 3
-- Name: registry; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE registry (
    id character varying(256) NOT NULL,
    home character varying(256),
    lid character varying(256) NOT NULL,
    objecttype character varying(256),
    status character varying(256) NOT NULL,
    versionname character varying(16),
    comment_ character varying(256),
    catalogingsynclatency character varying(32) DEFAULT 'P1D'::character varying,
    conformanceprofile character varying(16),
    operator character varying(256) NOT NULL,
    replicationsynclatency character varying(32) DEFAULT 'P1D'::character varying,
    specificationversion character varying(8) NOT NULL,
    CONSTRAINT registry_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Registry'::text))
);


ALTER TABLE public.registry OWNER TO omar;

--
-- TOC entry 1580 (class 1259 OID 50482)
-- Dependencies: 1875 3
-- Name: registrypackage; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE registrypackage (
    id character varying(256) NOT NULL,
    home character varying(256),
    lid character varying(256) NOT NULL,
    objecttype character varying(256),
    status character varying(256) NOT NULL,
    versionname character varying(16),
    comment_ character varying(256),
    CONSTRAINT registrypackage_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:RegistryPackage'::text))
);


ALTER TABLE public.registrypackage OWNER TO omar;

--
-- TOC entry 1584 (class 1259 OID 50514)
-- Dependencies: 1879 3
-- Name: service; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE service (
    id character varying(256) NOT NULL,
    home character varying(256),
    lid character varying(256) NOT NULL,
    objecttype character varying(256),
    status character varying(256) NOT NULL,
    versionname character varying(16),
    comment_ character varying(256),
    CONSTRAINT service_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Service'::text))
);


ALTER TABLE public.service OWNER TO omar;

--
-- TOC entry 1585 (class 1259 OID 50523)
-- Dependencies: 1880 3
-- Name: servicebinding; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE servicebinding (
    id character varying(256) NOT NULL,
    home character varying(256),
    lid character varying(256) NOT NULL,
    objecttype character varying(256),
    status character varying(256) NOT NULL,
    versionname character varying(16),
    comment_ character varying(256),
    service character varying(256) NOT NULL,
    accessuri character varying(256),
    targetbinding character varying(256),
    CONSTRAINT servicebinding_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ServiceBinding'::text))
);


ALTER TABLE public.servicebinding OWNER TO omar;

--
-- TOC entry 1587 (class 1259 OID 50540)
-- Dependencies: 1881 3
-- Name: specificationlink; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE specificationlink (
    id character varying(256) NOT NULL,
    home character varying(256),
    lid character varying(256) NOT NULL,
    objecttype character varying(256),
    status character varying(256) NOT NULL,
    versionname character varying(16),
    comment_ character varying(256),
    servicebinding character varying(256) NOT NULL,
    specificationobject character varying(256) NOT NULL,
    CONSTRAINT specificationlink_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:SpecificationLink'::text))
);


ALTER TABLE public.specificationlink OWNER TO omar;

--
-- TOC entry 1588 (class 1259 OID 50549)
-- Dependencies: 1882 1883 3
-- Name: subscription; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE subscription (
    id character varying(256) NOT NULL,
    home character varying(256),
    lid character varying(256) NOT NULL,
    objecttype character varying(256),
    status character varying(256) NOT NULL,
    versionname character varying(16),
    comment_ character varying(256),
    selector character varying(256) NOT NULL,
    endtime character varying(30),
    notificationinterval character varying(32) DEFAULT 'P1D'::character varying,
    starttime character varying(30),
    CONSTRAINT subscription_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Subscription'::text))
);


ALTER TABLE public.subscription OWNER TO omar;

--
-- TOC entry 1595 (class 1259 OID 50603)
-- Dependencies: 1886 3
-- Name: user_; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE user_ (
    id character varying(256) NOT NULL,
    home character varying(256),
    lid character varying(256) NOT NULL,
    objecttype character varying(256),
    status character varying(256) NOT NULL,
    versionname character varying(16),
    comment_ character varying(256),
    personname_firstname character varying(64),
    personname_middlename character varying(64),
    personname_lastname character varying(64),
    CONSTRAINT user__objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Person:User'::text))
);


ALTER TABLE public.user_ OWNER TO omar;

--
-- TOC entry 1597 (class 1259 OID 50621)
-- Dependencies: 1675 3
-- Name: identifiable; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW identifiable AS
    ((((((((((((((((((SELECT adhocquery.id, adhocquery.home FROM adhocquery UNION ALL SELECT association.id, association.home FROM association) UNION ALL SELECT auditableevent.id, auditableevent.home FROM auditableevent) UNION ALL SELECT classification.id, classification.home FROM classification) UNION ALL SELECT classificationnode.id, classificationnode.home FROM classificationnode) UNION ALL SELECT classscheme.id, classscheme.home FROM classscheme) UNION ALL SELECT externalidentifier.id, externalidentifier.home FROM externalidentifier) UNION ALL SELECT externallink.id, externallink.home FROM externallink) UNION ALL SELECT extrinsicobject.id, extrinsicobject.home FROM extrinsicobject) UNION ALL SELECT federation.id, federation.home FROM federation) UNION ALL SELECT organization.id, organization.home FROM organization) UNION ALL SELECT registry.id, registry.home FROM registry) UNION ALL SELECT registrypackage.id, registrypackage.home FROM registrypackage) UNION ALL SELECT service.id, service.home FROM service) UNION ALL SELECT servicebinding.id, servicebinding.home FROM servicebinding) UNION ALL SELECT specificationlink.id, specificationlink.home FROM specificationlink) UNION ALL SELECT subscription.id, subscription.home FROM subscription) UNION ALL SELECT user_.id, user_.home FROM user_) UNION ALL SELECT person.id, person.home FROM person) UNION ALL SELECT objectref.id, objectref.home FROM objectref;


ALTER TABLE public.identifiable OWNER TO omar;

--
-- TOC entry 1575 (class 1259 OID 50441)
-- Dependencies: 3
-- Name: name_; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE name_ (
    charset character varying(32),
    lang character varying(32) NOT NULL,
    value character varying(1024) NOT NULL,
    parent character varying(256) NOT NULL
);


ALTER TABLE public.name_ OWNER TO omar;

--
-- TOC entry 1590 (class 1259 OID 50565)
-- Dependencies: 1884 3
-- Name: notification; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE notification (
    id character varying(256) NOT NULL,
    home character varying(256),
    lid character varying(256) NOT NULL,
    objecttype character varying(256),
    status character varying(256) NOT NULL,
    versionname character varying(16),
    comment_ character varying(256),
    subscription character varying(256) NOT NULL,
    CONSTRAINT notification_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Notification'::text))
);


ALTER TABLE public.notification OWNER TO omar;

--
-- TOC entry 1591 (class 1259 OID 50574)
-- Dependencies: 3
-- Name: notificationobject; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE notificationobject (
    notificationid character varying(256) NOT NULL,
    registryobjectid character varying(256) NOT NULL
);


ALTER TABLE public.notificationobject OWNER TO omar;

--
-- TOC entry 1589 (class 1259 OID 50559)
-- Dependencies: 3
-- Name: notifyaction; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE notifyaction (
    notificationoption character varying(256) NOT NULL,
    endpoint character varying(256) NOT NULL,
    parent character varying(256) NOT NULL
);


ALTER TABLE public.notifyaction OWNER TO omar;

--
-- TOC entry 1581 (class 1259 OID 50491)
-- Dependencies: 3
-- Name: postaladdress; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE postaladdress (
    city character varying(64),
    country character varying(64),
    postalcode character varying(64),
    state character varying(64),
    street character varying(64),
    streetnumber character varying(32),
    parent character varying(256) NOT NULL
);


ALTER TABLE public.postaladdress OWNER TO omar;

--
-- TOC entry 1599 (class 1259 OID 50738)
-- Dependencies: 3
-- Name: registryobject; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE registryobject (
    id character varying(256) NOT NULL,
    home character varying(256),
    lid character varying(256) NOT NULL,
    objecttype character varying(256) NOT NULL,
    status character varying(256) NOT NULL,
    versionname character varying(16),
    comment_ character varying(256)
);


ALTER TABLE public.registryobject OWNER TO omar;

--
-- TOC entry 1598 (class 1259 OID 50723)
-- Dependencies: 3
-- Name: repositoryitem; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE repositoryitem (
    lid character varying(256) NOT NULL,
    versionname character varying(16) NOT NULL,
    content bytea
);


ALTER TABLE public.repositoryitem OWNER TO omar;

--
-- TOC entry 1586 (class 1259 OID 50532)
-- Dependencies: 3
-- Name: slot; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE slot (
    sequenceid integer NOT NULL,
    name_ character varying(256) NOT NULL,
    slottype character varying(256),
    value character varying(256),
    parent character varying(256) NOT NULL
);


ALTER TABLE public.slot OWNER TO omar;

--
-- TOC entry 1594 (class 1259 OID 50597)
-- Dependencies: 3
-- Name: telephonenumber; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE telephonenumber (
    areacode character varying(8),
    countrycode character varying(8),
    extension character varying(8),
    number_ character varying(16),
    phonetype character varying(256),
    parent character varying(256) NOT NULL
);


ALTER TABLE public.telephonenumber OWNER TO omar;

--
-- TOC entry 1577 (class 1259 OID 50457)
-- Dependencies: 3
-- Name: usagedescription; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE usagedescription (
    charset character varying(32),
    lang character varying(32) NOT NULL,
    value character varying(1024) NOT NULL,
    parent character varying(256) NOT NULL
);


ALTER TABLE public.usagedescription OWNER TO omar;

--
-- TOC entry 1593 (class 1259 OID 50591)
-- Dependencies: 3
-- Name: usageparameter; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE usageparameter (
    value character varying(1024) NOT NULL,
    parent character varying(256) NOT NULL
);


ALTER TABLE public.usageparameter OWNER TO omar;

--
-- TOC entry 2017 (class 2606 OID 50590)
-- Dependencies: 1592 1592
-- Name: adhocquery_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY adhocquery
    ADD CONSTRAINT adhocquery_pkey PRIMARY KEY (id);


--
-- TOC entry 1903 (class 2606 OID 50379)
-- Dependencies: 1567 1567 1567
-- Name: affectedobject_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY affectedobject
    ADD CONSTRAINT affectedobject_pkey PRIMARY KEY (id, eventid);


--
-- TOC entry 1889 (class 2606 OID 50362)
-- Dependencies: 1565 1565
-- Name: association_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY association
    ADD CONSTRAINT association_pkey PRIMARY KEY (id);


--
-- TOC entry 1897 (class 2606 OID 50371)
-- Dependencies: 1566 1566
-- Name: auditableevent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY auditableevent
    ADD CONSTRAINT auditableevent_pkey PRIMARY KEY (id);


--
-- TOC entry 1908 (class 2606 OID 50388)
-- Dependencies: 1568 1568
-- Name: classification_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY classification
    ADD CONSTRAINT classification_pkey PRIMARY KEY (id);


--
-- TOC entry 1915 (class 2606 OID 50397)
-- Dependencies: 1569 1569
-- Name: classificationnode_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY classificationnode
    ADD CONSTRAINT classificationnode_pkey PRIMARY KEY (id);


--
-- TOC entry 1923 (class 2606 OID 50406)
-- Dependencies: 1570 1570
-- Name: classscheme_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY classscheme
    ADD CONSTRAINT classscheme_pkey PRIMARY KEY (id);


--
-- TOC entry 1954 (class 2606 OID 50456)
-- Dependencies: 1576 1576 1576
-- Name: description_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY description
    ADD CONSTRAINT description_pkey PRIMARY KEY (parent, lang);


--
-- TOC entry 1928 (class 2606 OID 50415)
-- Dependencies: 1571 1571
-- Name: externalidentifier_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY externalidentifier
    ADD CONSTRAINT externalidentifier_pkey PRIMARY KEY (id);


--
-- TOC entry 1934 (class 2606 OID 50423)
-- Dependencies: 1572 1572
-- Name: externallink_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY externallink
    ADD CONSTRAINT externallink_pkey PRIMARY KEY (id);


--
-- TOC entry 1940 (class 2606 OID 50431)
-- Dependencies: 1573 1573
-- Name: extrinsicobject_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY extrinsicobject
    ADD CONSTRAINT extrinsicobject_pkey PRIMARY KEY (id);


--
-- TOC entry 1945 (class 2606 OID 50440)
-- Dependencies: 1574 1574
-- Name: federation_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY federation
    ADD CONSTRAINT federation_pkey PRIMARY KEY (id);


--
-- TOC entry 1951 (class 2606 OID 50448)
-- Dependencies: 1575 1575 1575
-- Name: name__pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY name_
    ADD CONSTRAINT name__pkey PRIMARY KEY (parent, lang);


--
-- TOC entry 2013 (class 2606 OID 50573)
-- Dependencies: 1590 1590
-- Name: notification_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY notification
    ADD CONSTRAINT notification_pkey PRIMARY KEY (id);


--
-- TOC entry 2015 (class 2606 OID 50581)
-- Dependencies: 1591 1591 1591
-- Name: notificationobject_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY notificationobject
    ADD CONSTRAINT notificationobject_pkey PRIMARY KEY (notificationid, registryobjectid);


--
-- TOC entry 1963 (class 2606 OID 50472)
-- Dependencies: 1578 1578
-- Name: objectref_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY objectref
    ADD CONSTRAINT objectref_pkey PRIMARY KEY (id);


--
-- TOC entry 1968 (class 2606 OID 50481)
-- Dependencies: 1579 1579
-- Name: organization_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY organization
    ADD CONSTRAINT organization_pkey PRIMARY KEY (id);


--
-- TOC entry 2033 (class 2606 OID 50620)
-- Dependencies: 1596 1596
-- Name: person_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY person
    ADD CONSTRAINT person_pkey PRIMARY KEY (id);


--
-- TOC entry 1984 (class 2606 OID 50513)
-- Dependencies: 1583 1583
-- Name: registry_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY registry
    ADD CONSTRAINT registry_pkey PRIMARY KEY (id);


--
-- TOC entry 2040 (class 2606 OID 50745)
-- Dependencies: 1599 1599 1599
-- Name: registryobject_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY registryobject
    ADD CONSTRAINT registryobject_pkey PRIMARY KEY (id, objecttype);


--
-- TOC entry 1974 (class 2606 OID 50490)
-- Dependencies: 1580 1580
-- Name: registrypackage_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY registrypackage
    ADD CONSTRAINT registrypackage_pkey PRIMARY KEY (id);


--
-- TOC entry 2035 (class 2606 OID 50730)
-- Dependencies: 1598 1598 1598
-- Name: repositoryitem_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY repositoryitem
    ADD CONSTRAINT repositoryitem_pkey PRIMARY KEY (lid, versionname);


--
-- TOC entry 1989 (class 2606 OID 50522)
-- Dependencies: 1584 1584
-- Name: service_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY service
    ADD CONSTRAINT service_pkey PRIMARY KEY (id);


--
-- TOC entry 1995 (class 2606 OID 50531)
-- Dependencies: 1585 1585
-- Name: servicebinding_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY servicebinding
    ADD CONSTRAINT servicebinding_pkey PRIMARY KEY (id);


--
-- TOC entry 1999 (class 2606 OID 50539)
-- Dependencies: 1586 1586 1586 1586
-- Name: slot_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY slot
    ADD CONSTRAINT slot_pkey PRIMARY KEY (parent, name_, sequenceid);


--
-- TOC entry 2006 (class 2606 OID 50548)
-- Dependencies: 1587 1587
-- Name: specificationlink_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY specificationlink
    ADD CONSTRAINT specificationlink_pkey PRIMARY KEY (id);


--
-- TOC entry 2011 (class 2606 OID 50558)
-- Dependencies: 1588 1588
-- Name: subscription_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY subscription
    ADD CONSTRAINT subscription_pkey PRIMARY KEY (id);


--
-- TOC entry 1959 (class 2606 OID 50464)
-- Dependencies: 1577 1577 1577
-- Name: usagedescription_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY usagedescription
    ADD CONSTRAINT usagedescription_pkey PRIMARY KEY (parent, lang);


--
-- TOC entry 2027 (class 2606 OID 50611)
-- Dependencies: 1595 1595
-- Name: user__pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY user_
    ADD CONSTRAINT user__pkey PRIMARY KEY (id);


--
-- TOC entry 2000 (class 1259 OID 50718)
-- Dependencies: 1587
-- Name: binding_slnk_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX binding_slnk_idx ON specificationlink USING btree (servicebinding);


--
-- TOC entry 1975 (class 1259 OID 50711)
-- Dependencies: 1581
-- Name: city_pstladr_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX city_pstladr_idx ON postaladdress USING btree (city);


--
-- TOC entry 1909 (class 1259 OID 50702)
-- Dependencies: 1568
-- Name: clsobj_class_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX clsobj_class_idx ON classification USING btree (classifiedobject);


--
-- TOC entry 1976 (class 1259 OID 50712)
-- Dependencies: 1581
-- Name: cntry_pstladr_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX cntry_pstladr_idx ON postaladdress USING btree (country);


--
-- TOC entry 1916 (class 1259 OID 50705)
-- Dependencies: 1569
-- Name: code_node_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX code_node_idx ON classificationnode USING btree (code);


--
-- TOC entry 1904 (class 1259 OID 50691)
-- Dependencies: 1567
-- Name: evid_afobj_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX evid_afobj_idx ON affectedobject USING btree (eventid);


--
-- TOC entry 2018 (class 1259 OID 50670)
-- Dependencies: 1592
-- Name: home_adhquery_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX home_adhquery_idx ON adhocquery USING btree (home);


--
-- TOC entry 1890 (class 1259 OID 50671)
-- Dependencies: 1565
-- Name: home_assoc_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX home_assoc_idx ON association USING btree (home);


--
-- TOC entry 1898 (class 1259 OID 50672)
-- Dependencies: 1566
-- Name: home_auevent_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX home_auevent_idx ON auditableevent USING btree (home);


--
-- TOC entry 1990 (class 1259 OID 50684)
-- Dependencies: 1585
-- Name: home_bind_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX home_bind_idx ON servicebinding USING btree (home);


--
-- TOC entry 1910 (class 1259 OID 50673)
-- Dependencies: 1568
-- Name: home_class_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX home_class_idx ON classification USING btree (home);


--
-- TOC entry 1929 (class 1259 OID 50676)
-- Dependencies: 1571
-- Name: home_eid_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX home_eid_idx ON externalidentifier USING btree (home);


--
-- TOC entry 1935 (class 1259 OID 50677)
-- Dependencies: 1572
-- Name: home_exlink_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX home_exlink_idx ON externallink USING btree (home);


--
-- TOC entry 1941 (class 1259 OID 50678)
-- Dependencies: 1573
-- Name: home_extobj_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX home_extobj_idx ON extrinsicobject USING btree (home);


--
-- TOC entry 1946 (class 1259 OID 50679)
-- Dependencies: 1574
-- Name: home_fed_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX home_fed_idx ON federation USING btree (home);


--
-- TOC entry 1917 (class 1259 OID 50674)
-- Dependencies: 1569
-- Name: home_node_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX home_node_idx ON classificationnode USING btree (home);


--
-- TOC entry 1964 (class 1259 OID 50680)
-- Dependencies: 1579
-- Name: home_org_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX home_org_idx ON organization USING btree (home);


--
-- TOC entry 2028 (class 1259 OID 50688)
-- Dependencies: 1596
-- Name: home_person_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX home_person_idx ON person USING btree (home);


--
-- TOC entry 1970 (class 1259 OID 50682)
-- Dependencies: 1580
-- Name: home_pkg_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX home_pkg_idx ON registrypackage USING btree (home);


--
-- TOC entry 1980 (class 1259 OID 50681)
-- Dependencies: 1583
-- Name: home_registry_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX home_registry_idx ON registry USING btree (home);


--
-- TOC entry 2036 (class 1259 OID 50748)
-- Dependencies: 1599
-- Name: home_regobj_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX home_regobj_idx ON registryobject USING btree (home);


--
-- TOC entry 1924 (class 1259 OID 50675)
-- Dependencies: 1570
-- Name: home_scheme_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX home_scheme_idx ON classscheme USING btree (home);


--
-- TOC entry 1985 (class 1259 OID 50683)
-- Dependencies: 1584
-- Name: home_service_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX home_service_idx ON service USING btree (home);


--
-- TOC entry 2001 (class 1259 OID 50685)
-- Dependencies: 1587
-- Name: home_slnk_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX home_slnk_idx ON specificationlink USING btree (home);


--
-- TOC entry 2007 (class 1259 OID 50686)
-- Dependencies: 1588
-- Name: home_subs_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX home_subs_idx ON subscription USING btree (home);


--
-- TOC entry 2022 (class 1259 OID 50687)
-- Dependencies: 1595
-- Name: home_user_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX home_user_idx ON user_ USING btree (home);


--
-- TOC entry 2019 (class 1259 OID 50650)
-- Dependencies: 1592
-- Name: id_adhquery_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX id_adhquery_idx ON adhocquery USING btree (id);


--
-- TOC entry 1905 (class 1259 OID 50690)
-- Dependencies: 1567
-- Name: id_afobj_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX id_afobj_idx ON affectedobject USING btree (id);


--
-- TOC entry 1891 (class 1259 OID 50651)
-- Dependencies: 1565
-- Name: id_assoc_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX id_assoc_idx ON association USING btree (id);


--
-- TOC entry 1899 (class 1259 OID 50652)
-- Dependencies: 1566
-- Name: id_auevent_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX id_auevent_idx ON auditableevent USING btree (id);


--
-- TOC entry 1991 (class 1259 OID 50665)
-- Dependencies: 1585
-- Name: id_bind_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX id_bind_idx ON servicebinding USING btree (id);


--
-- TOC entry 1911 (class 1259 OID 50653)
-- Dependencies: 1568
-- Name: id_class_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX id_class_idx ON classification USING btree (id);


--
-- TOC entry 1930 (class 1259 OID 50656)
-- Dependencies: 1571
-- Name: id_eid_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX id_eid_idx ON externalidentifier USING btree (id);


--
-- TOC entry 1906 (class 1259 OID 50689)
-- Dependencies: 1567 1567
-- Name: id_evid_afobj_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX id_evid_afobj_idx ON affectedobject USING btree (id, eventid);


--
-- TOC entry 1936 (class 1259 OID 50657)
-- Dependencies: 1572
-- Name: id_exlink_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX id_exlink_idx ON externallink USING btree (id);


--
-- TOC entry 1942 (class 1259 OID 50658)
-- Dependencies: 1573
-- Name: id_extobj_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX id_extobj_idx ON extrinsicobject USING btree (id);


--
-- TOC entry 1947 (class 1259 OID 50659)
-- Dependencies: 1574
-- Name: id_fed_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX id_fed_idx ON federation USING btree (id);


--
-- TOC entry 1918 (class 1259 OID 50654)
-- Dependencies: 1569
-- Name: id_node_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX id_node_idx ON classificationnode USING btree (id);


--
-- TOC entry 1961 (class 1259 OID 50660)
-- Dependencies: 1578
-- Name: id_objectref_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX id_objectref_idx ON objectref USING btree (id);


--
-- TOC entry 1965 (class 1259 OID 50661)
-- Dependencies: 1579
-- Name: id_org_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX id_org_idx ON organization USING btree (id);


--
-- TOC entry 2029 (class 1259 OID 50669)
-- Dependencies: 1596
-- Name: id_person_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX id_person_idx ON person USING btree (id);


--
-- TOC entry 1971 (class 1259 OID 50663)
-- Dependencies: 1580
-- Name: id_pkg_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX id_pkg_idx ON registrypackage USING btree (id);


--
-- TOC entry 1981 (class 1259 OID 50662)
-- Dependencies: 1583
-- Name: id_registry_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX id_registry_idx ON registry USING btree (id);


--
-- TOC entry 2037 (class 1259 OID 50747)
-- Dependencies: 1599
-- Name: id_regobj_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX id_regobj_idx ON registryobject USING btree (id);


--
-- TOC entry 1925 (class 1259 OID 50655)
-- Dependencies: 1570
-- Name: id_scheme_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX id_scheme_idx ON classscheme USING btree (id);


--
-- TOC entry 1986 (class 1259 OID 50664)
-- Dependencies: 1584
-- Name: id_service_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX id_service_idx ON service USING btree (id);


--
-- TOC entry 2002 (class 1259 OID 50666)
-- Dependencies: 1587
-- Name: id_slnk_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX id_slnk_idx ON specificationlink USING btree (id);


--
-- TOC entry 2008 (class 1259 OID 50667)
-- Dependencies: 1588
-- Name: id_subs_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX id_subs_idx ON subscription USING btree (id);


--
-- TOC entry 2023 (class 1259 OID 50668)
-- Dependencies: 1595
-- Name: id_user_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX id_user_idx ON user_ USING btree (id);


--
-- TOC entry 2030 (class 1259 OID 50722)
-- Dependencies: 1596
-- Name: lastnm_person_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX lastnm_person_idx ON person USING btree (personname_lastname);


--
-- TOC entry 2024 (class 1259 OID 50721)
-- Dependencies: 1595
-- Name: lastnm_user_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX lastnm_user_idx ON user_ USING btree (personname_lastname);


--
-- TOC entry 2020 (class 1259 OID 50631)
-- Dependencies: 1592
-- Name: lid_adhquery_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX lid_adhquery_idx ON adhocquery USING btree (lid);


--
-- TOC entry 1892 (class 1259 OID 50632)
-- Dependencies: 1565
-- Name: lid_assoc_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX lid_assoc_idx ON association USING btree (lid);


--
-- TOC entry 1900 (class 1259 OID 50692)
-- Dependencies: 1566
-- Name: lid_auevent_evttyp; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX lid_auevent_evttyp ON auditableevent USING btree (eventtype);


--
-- TOC entry 1901 (class 1259 OID 50633)
-- Dependencies: 1566
-- Name: lid_auevent_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX lid_auevent_idx ON auditableevent USING btree (lid);


--
-- TOC entry 1992 (class 1259 OID 50645)
-- Dependencies: 1585
-- Name: lid_bind_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX lid_bind_idx ON servicebinding USING btree (lid);


--
-- TOC entry 1912 (class 1259 OID 50634)
-- Dependencies: 1568
-- Name: lid_class_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX lid_class_idx ON classification USING btree (lid);


--
-- TOC entry 1931 (class 1259 OID 50637)
-- Dependencies: 1571
-- Name: lid_eid_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX lid_eid_idx ON externalidentifier USING btree (lid);


--
-- TOC entry 1937 (class 1259 OID 50638)
-- Dependencies: 1572
-- Name: lid_exlink_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX lid_exlink_idx ON externallink USING btree (lid);


--
-- TOC entry 1943 (class 1259 OID 50639)
-- Dependencies: 1573
-- Name: lid_extobj_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX lid_extobj_idx ON extrinsicobject USING btree (lid);


--
-- TOC entry 1948 (class 1259 OID 50640)
-- Dependencies: 1574
-- Name: lid_fed_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX lid_fed_idx ON federation USING btree (lid);


--
-- TOC entry 1919 (class 1259 OID 50635)
-- Dependencies: 1569
-- Name: lid_node_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX lid_node_idx ON classificationnode USING btree (lid);


--
-- TOC entry 1966 (class 1259 OID 50641)
-- Dependencies: 1579
-- Name: lid_org_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX lid_org_idx ON organization USING btree (lid);


--
-- TOC entry 2031 (class 1259 OID 50649)
-- Dependencies: 1596
-- Name: lid_person_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX lid_person_idx ON person USING btree (lid);


--
-- TOC entry 1972 (class 1259 OID 50643)
-- Dependencies: 1580
-- Name: lid_pkg_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX lid_pkg_idx ON registrypackage USING btree (lid);


--
-- TOC entry 1982 (class 1259 OID 50642)
-- Dependencies: 1583
-- Name: lid_registry_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX lid_registry_idx ON registry USING btree (lid);


--
-- TOC entry 2038 (class 1259 OID 50746)
-- Dependencies: 1599
-- Name: lid_regobj_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX lid_regobj_idx ON registryobject USING btree (lid);


--
-- TOC entry 1926 (class 1259 OID 50636)
-- Dependencies: 1570
-- Name: lid_scheme_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX lid_scheme_idx ON classscheme USING btree (lid);


--
-- TOC entry 1987 (class 1259 OID 50644)
-- Dependencies: 1584
-- Name: lid_service_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX lid_service_idx ON service USING btree (lid);


--
-- TOC entry 2003 (class 1259 OID 50646)
-- Dependencies: 1587
-- Name: lid_slnk_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX lid_slnk_idx ON specificationlink USING btree (lid);


--
-- TOC entry 2009 (class 1259 OID 50647)
-- Dependencies: 1588
-- Name: lid_subs_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX lid_subs_idx ON subscription USING btree (lid);


--
-- TOC entry 2025 (class 1259 OID 50648)
-- Dependencies: 1595
-- Name: lid_user_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX lid_user_idx ON user_ USING btree (lid);


--
-- TOC entry 1955 (class 1259 OID 50696)
-- Dependencies: 1576 1576
-- Name: lngval_desc_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX lngval_desc_idx ON description USING btree (lang, value);


--
-- TOC entry 1949 (class 1259 OID 50694)
-- Dependencies: 1575 1575
-- Name: lngval_name_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX lngval_name_idx ON name_ USING btree (lang, value);


--
-- TOC entry 1957 (class 1259 OID 50698)
-- Dependencies: 1577 1577
-- Name: lngval_usgdes_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX lngval_usgdes_idx ON usagedescription USING btree (lang, value);


--
-- TOC entry 1996 (class 1259 OID 50717)
-- Dependencies: 1586
-- Name: name_slot_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX name_slot_idx ON slot USING btree (name_);


--
-- TOC entry 1913 (class 1259 OID 50703)
-- Dependencies: 1568
-- Name: node_class_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX node_class_idx ON classification USING btree (classificationnode);


--
-- TOC entry 1979 (class 1259 OID 50714)
-- Dependencies: 1582
-- Name: parent_emladr_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX parent_emladr_idx ON emailaddress USING btree (parent);


--
-- TOC entry 1920 (class 1259 OID 50704)
-- Dependencies: 1569
-- Name: parent_node_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX parent_node_idx ON classificationnode USING btree (parent);


--
-- TOC entry 1969 (class 1259 OID 50709)
-- Dependencies: 1579
-- Name: parent_org_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX parent_org_idx ON organization USING btree (parent);


--
-- TOC entry 2021 (class 1259 OID 50720)
-- Dependencies: 1594
-- Name: parent_phone_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX parent_phone_idx ON telephonenumber USING btree (parent);


--
-- TOC entry 1977 (class 1259 OID 50710)
-- Dependencies: 1581
-- Name: parent_pstladr_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX parent_pstladr_idx ON postaladdress USING btree (parent);


--
-- TOC entry 1997 (class 1259 OID 50716)
-- Dependencies: 1586
-- Name: parent_slot_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX parent_slot_idx ON slot USING btree (parent);


--
-- TOC entry 1921 (class 1259 OID 50706)
-- Dependencies: 1569
-- Name: path_node_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX path_node_idx ON classificationnode USING btree (path);


--
-- TOC entry 1978 (class 1259 OID 50713)
-- Dependencies: 1581
-- Name: pcode_pstladr_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX pcode_pstladr_idx ON postaladdress USING btree (postalcode);


--
-- TOC entry 1932 (class 1259 OID 50707)
-- Dependencies: 1571
-- Name: ro_eid_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX ro_eid_idx ON externalidentifier USING btree (registryobject);


--
-- TOC entry 1993 (class 1259 OID 50715)
-- Dependencies: 1585
-- Name: service_bind_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX service_bind_idx ON servicebinding USING btree (service);


--
-- TOC entry 2004 (class 1259 OID 50719)
-- Dependencies: 1587
-- Name: spec_slnk_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX spec_slnk_idx ON specificationlink USING btree (specificationobject);


--
-- TOC entry 1893 (class 1259 OID 50699)
-- Dependencies: 1565
-- Name: src_ass_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX src_ass_idx ON association USING btree (sourceobject);


--
-- TOC entry 1894 (class 1259 OID 50700)
-- Dependencies: 1565
-- Name: tgt_ass_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX tgt_ass_idx ON association USING btree (targetobject);


--
-- TOC entry 1895 (class 1259 OID 50701)
-- Dependencies: 1565
-- Name: type_ass_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX type_ass_idx ON association USING btree (associationtype);


--
-- TOC entry 1938 (class 1259 OID 50708)
-- Dependencies: 1572
-- Name: uri_exlink_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX uri_exlink_idx ON externallink USING btree (externaluri);


--
-- TOC entry 1956 (class 1259 OID 50695)
-- Dependencies: 1576
-- Name: value_desc_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX value_desc_idx ON description USING btree (value);


--
-- TOC entry 1952 (class 1259 OID 50693)
-- Dependencies: 1575
-- Name: value_name_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX value_name_idx ON name_ USING btree (value);


--
-- TOC entry 1960 (class 1259 OID 50697)
-- Dependencies: 1577
-- Name: value_usgdes_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX value_usgdes_idx ON usagedescription USING btree (value);


--
-- TOC entry 2045 (class 0 OID 0)
-- Dependencies: 3
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2009-05-15 13:58:34

--
-- PostgreSQL database dump complete
--

