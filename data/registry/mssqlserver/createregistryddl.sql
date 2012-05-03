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
-- TOC entry 1567 (class 1259 OID 78400)
-- Dependencies: 1868 6
-- Name: association; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE association (
    id VARCHAR(64) NOT NULL,
    lid VARCHAR(64) NOT NULL,
    objecttype CHAR(2),
    status CHAR(1) NOT NULL,
    versionname VARCHAR(16),
    associationtype CHAR(2) NOT NULL,
    sourceobject VARCHAR(64) NOT NULL,
    targetobject VARCHAR(64) NOT NULL
  --  CONSTRAINT association_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association'::text))
);

--
-- TOC entry 1569 (class 1259 OID 78414)
-- Dependencies: 1870 6
-- Name: classification; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE classification (
    id VARCHAR(64) NOT NULL,
    lid VARCHAR(64) NOT NULL,
    objecttype CHAR(2),
    status CHAR(1) NOT NULL,
    versionname VARCHAR(16),
    classificationnode VARCHAR(64),
    classificationscheme VARCHAR(64),
    classifiedobject VARCHAR(64) NOT NULL,
    noderepresentation VARCHAR(128)
    --CONSTRAINT classification_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification'::text))
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
-- TOC entry 1574 (class 1259 OID 78447)
-- Dependencies: 1873 6
-- Name: externalidentifier; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE externalidentifier (
    id VARCHAR(64) NOT NULL,
    lid VARCHAR(64) NOT NULL,
    objecttype CHAR(2),
    status CHAR(1) NOT NULL,
    versionname VARCHAR(16),
    registryobject VARCHAR(64) NOT NULL,
    identificationscheme CHAR(2) NOT NULL,
    value VARCHAR(128) NOT NULL
    --CONSTRAINT externalidentifier_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalIdentifier'::text))
);

--
-- TOC entry 1576 (class 1259 OID 78460)
-- Dependencies: 6
-- Name: extrinsicobject; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE extrinsicobject (
    id VARCHAR(64) NOT NULL,
    lid VARCHAR(64) NOT NULL,
    objecttype CHAR(2),
    status CHAR(1) NOT NULL,
    versionname VARCHAR(16),
    isopaque CHAR(1) NOT NULL,
    mimetype VARCHAR(128)
);

--
-- TOC entry 1582 (class 1259 OID 78504)
-- Dependencies: 1880 6
-- Name: registrypackage; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE registrypackage (
    id VARCHAR(64) NOT NULL,
    lid VARCHAR(64) NOT NULL,
    objecttype CHAR(2),
    status CHAR(1) NOT NULL,
    versionname VARCHAR(16),
    --CONSTRAINT registrypackage_objecttype_check CHECK (((objecttype)::text = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:RegistryPackage'::text))
);

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
-- TOC entry 1596 (class 1259 OID 78597)
-- Dependencies: 6
-- Name: slot; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE slot (
    sequenceid INT NOT NULL,
    name_ VARCHAR(128) NOT NULL,
    value VARCHAR(128),
    parent VARCHAR(64) NOT NULL
);

--
-- TOC entry 1895 (class 2606 OID 78626)
-- Dependencies: 1567 1567
-- Name: association_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE association
    ADD CONSTRAINT association_pkey PRIMARY KEY (id);

--
-- TOC entry 1903 (class 2606 OID 78630)
-- Dependencies: 1569 1569
-- Name: classification_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE classification
    ADD CONSTRAINT classification_pkey PRIMARY KEY (id);

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
-- TOC entry 1924 (class 2606 OID 78644)
-- Dependencies: 1576 1576
-- Name: extrinsicobject_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE extrinsicobject
    ADD CONSTRAINT extrinsicobject_pkey PRIMARY KEY (id);

--
-- TOC entry 1953 (class 2606 OID 80294)
-- Dependencies: 1589 1589
-- Name: name__pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE name_
    ADD CONSTRAINT name__pkey PRIMARY KEY (parent);

--
-- TOC entry 1938 (class 2606 OID 78666)
-- Dependencies: 1582 1582
-- Name: registrypackage_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE registrypackage
    ADD CONSTRAINT registrypackage_pkey PRIMARY KEY (id);

--
-- TOC entry 1967 (class 2606 OID 78674)
-- Dependencies: 1596 1596 1596 1596
-- Name: slot_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE slot
    ADD CONSTRAINT slot_pkey PRIMARY KEY (parent, name_, sequenceid);

--
-- TOC entry 1904 (class 1259 OID 78686)
-- Dependencies: 1569
-- Name: clsobj_class_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX clsobj_class_idx ON classification (classifiedobject);

--
-- TOC entry 1917 (class 1259 OID 80024)
-- Dependencies: 1574
-- Name: idscheme_eid_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX idscheme_eid_idx ON externalidentifier (identificationscheme);

--
-- TOC entry 1918 (class 1259 OID 78778)
-- Dependencies: 1574
-- Name: ro_eid_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX ro_eid_idx ON externalidentifier (registryobject);

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
-- TOC entry 1919 (class 1259 OID 80074)
-- Dependencies: 1574
-- Name: value_eid_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX value_eid_idx ON externalidentifier (value);

-- Additional indexes (on LID).
CREATE INDEX lid_class_idx ON classification (lid);
CREATE INDEX lid_eo_idx ON extrinsicobject (lid);
CREATE INDEX lid_rp_idx ON registrypackage (lid);