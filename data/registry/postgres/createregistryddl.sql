--
-- PostgreSQL database dump
--

-- Started on 2009-06-18 22:56:22

SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- TOC entry 393 (class 2612 OID 16386)
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: omar
--

-- CREATE PROCEDURAL LANGUAGE plpgsql;


ALTER PROCEDURAL LANGUAGE plpgsql OWNER TO omar;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- DROP Tables
--
--DROP TABLE IF EXISTS adhocquery;
--DROP TABLE IF EXISTS affectedobject;
DROP TABLE IF EXISTS association;
--DROP TABLE IF EXISTS auditableevent;
DROP TABLE IF EXISTS classification;
--DROP TABLE IF EXISTS classificationnode;
--DROP TABLE IF EXISTS classscheme;
DROP TABLE IF EXISTS description;
--DROP TABLE IF EXISTS emailaddress;
DROP TABLE IF EXISTS externalidentifier;
--DROP TABLE IF EXISTS externallink;
DROP TABLE IF EXISTS extrinsicobject;
--DROP TABLE IF EXISTS federation;
DROP TABLE IF EXISTS name_;
--DROP TABLE IF EXISTS notification;
--DROP TABLE IF EXISTS notificationobject;
--DROP TABLE IF EXISTS notifyaction;
--DROP TABLE IF EXISTS objectref;
--DROP TABLE IF EXISTS organization;
--DROP TABLE IF EXISTS person;
--DROP TABLE IF EXISTS postaladdress;
--DROP TABLE IF EXISTS registry;
--DROP TABLE IF EXISTS registryobject;
DROP TABLE IF EXISTS registrypackage;
--DROP TABLE IF EXISTS repositoryitem;
--DROP TABLE IF EXISTS service;
--DROP TABLE IF EXISTS servicebinding;
DROP TABLE IF EXISTS slot;
--DROP TABLE IF EXISTS specificationlink;
--DROP TABLE IF EXISTS subscription;
--DROP TABLE IF EXISTS telephonenumber;
--DROP TABLE IF EXISTS usagedescription;
--DROP TABLE IF EXISTS usageparameter;
--DROP TABLE IF EXISTS user_;

--
-- TOC entry 1567 (class 1259 OID 78400)
-- Dependencies: 1868 6
-- Name: association; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE association (
    id character varying(64) NOT NULL,
    lid character varying(64) NOT NULL,
    objecttype char(2),
    status char(1) NOT NULL,
    versionname character varying(16),
    associationtype char(2) NOT NULL,
    sourceobject character varying(64) NOT NULL,
    targetobject character varying(64) NOT NULL,
    CONSTRAINT association_objecttype_check CHECK (((objecttype)::text = 'AS'::text))
);


ALTER TABLE public.association OWNER TO omar;


--
-- TOC entry 1569 (class 1259 OID 78414)
-- Dependencies: 1870 6
-- Name: classification; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE classification (
    id character varying(64) NOT NULL,
    lid character varying(64) NOT NULL,
    objecttype char(2),
    status char(1) NOT NULL,
    versionname character varying(16),
    classificationnode character varying(64),
    classificationscheme character varying(64),
    classifiedobject character varying(64) NOT NULL,
    noderepresentation character varying(128),
    CONSTRAINT classification_objecttype_check CHECK (((objecttype)::text = 'CL'::text))
);


ALTER TABLE public.classification OWNER TO omar;


--
-- TOC entry 1572 (class 1259 OID 78435)
-- Dependencies: 6
-- Name: description; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE description (
    charset character varying(32),
    lang character varying(32) NOT NULL,
    value character varying(256) NOT NULL,
    parent character varying(64) NOT NULL
);


ALTER TABLE public.description OWNER TO omar;

--
-- TOC entry 1574 (class 1259 OID 78447)
-- Dependencies: 1873 6
-- Name: externalidentifier; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE externalidentifier (
    id character varying(64) NOT NULL,
    lid character varying(64) NOT NULL,
    objecttype char(2),
    status char(1) NOT NULL,
    versionname character varying(16),
    registryobject character varying(64) NOT NULL,
    identificationscheme char(2) NOT NULL,
    value character varying(128) NOT NULL,
    CONSTRAINT externalidentifier_objecttype_check CHECK (((objecttype)::text = 'EI'::text))
);


ALTER TABLE public.externalidentifier OWNER TO omar;


--
-- TOC entry 1576 (class 1259 OID 78460)
-- Dependencies: 6
-- Name: extrinsicobject; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE extrinsicobject (
    id character varying(64) NOT NULL,
    lid character varying(64) NOT NULL,
    objecttype char(2),
    status char(1) NOT NULL,
    versionname character varying(16),
    isopaque char(1) NOT NULL,
    mimetype character varying(128)
);


ALTER TABLE public.extrinsicobject OWNER TO omar;


--
-- TOC entry 1582 (class 1259 OID 78504)
-- Dependencies: 1880 6
-- Name: registrypackage; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE registrypackage (
    id character varying(64) NOT NULL,
    lid character varying(64) NOT NULL,
    objecttype char(2),
    status char(1) NOT NULL,
    versionname character varying(16),
    CONSTRAINT registrypackage_objecttype_check CHECK (((objecttype)::text = 'RP'::text))
);


ALTER TABLE public.registrypackage OWNER TO omar;

--
-- TOC entry 1589 (class 1259 OID 78552)
-- Dependencies: 6
-- Name: name_; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE name_ (
    charset character varying(32),
    lang character varying(32) NOT NULL,
    value character varying(256) NOT NULL,
    parent character varying(64) NOT NULL
);


ALTER TABLE public.name_ OWNER TO omar;

--
-- TOC entry 1596 (class 1259 OID 78597)
-- Dependencies: 6
-- Name: slot; Type: TABLE; Schema: public; Owner: omar; Tablespace: 
--

CREATE TABLE slot (
    sequenceid integer NOT NULL,
    name_ character varying(128) NOT NULL,
    value character varying(128),
    parent character varying(64) NOT NULL
);


ALTER TABLE public.slot OWNER TO omar;


--
-- TOC entry 1895 (class 2606 OID 78626)
-- Dependencies: 1567 1567
-- Name: association_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE ONLY association
    ADD CONSTRAINT association_pkey PRIMARY KEY (id);

--
-- TOC entry 1903 (class 2606 OID 78630)
-- Dependencies: 1569 1569
-- Name: classification_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE ONLY classification
    ADD CONSTRAINT classification_pkey PRIMARY KEY (id);

--
-- TOC entry 1913 (class 2606 OID 79837)
-- Dependencies: 1572 1572
-- Name: description_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE ONLY description
    ADD CONSTRAINT description_pkey PRIMARY KEY (parent);


--
-- TOC entry 1916 (class 2606 OID 78640)
-- Dependencies: 1574 1574
-- Name: externalidentifier_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE ONLY externalidentifier
    ADD CONSTRAINT externalidentifier_pkey PRIMARY KEY (id);

--
-- TOC entry 1924 (class 2606 OID 78644)
-- Dependencies: 1576 1576
-- Name: extrinsicobject_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE ONLY extrinsicobject
    ADD CONSTRAINT extrinsicobject_pkey PRIMARY KEY (id);

--
-- TOC entry 1953 (class 2606 OID 80294)
-- Dependencies: 1589 1589
-- Name: name__pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE ONLY name_
    ADD CONSTRAINT name__pkey PRIMARY KEY (parent);

--
-- TOC entry 1938 (class 2606 OID 78666)
-- Dependencies: 1582 1582
-- Name: registrypackage_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE ONLY registrypackage
    ADD CONSTRAINT registrypackage_pkey PRIMARY KEY (id);

--
-- TOC entry 1967 (class 2606 OID 78674)
-- Dependencies: 1596 1596 1596 1596
-- Name: slot_pkey; Type: CONSTRAINT; Schema: public; Owner: omar; Tablespace: 
--

ALTER TABLE ONLY slot
    ADD CONSTRAINT slot_pkey PRIMARY KEY (parent, name_, sequenceid);

--
-- TOC entry 1904 (class 1259 OID 78686)
-- Dependencies: 1569
-- Name: clsobj_class_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX clsobj_class_idx ON classification USING btree (classifiedobject);

--
-- TOC entry 1917 (class 1259 OID 80024)
-- Dependencies: 1574
-- Name: idscheme_eid_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX idscheme_eid_idx ON externalidentifier USING btree (identificationscheme);

--
-- TOC entry 1918 (class 1259 OID 78778)
-- Dependencies: 1574
-- Name: ro_eid_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX ro_eid_idx ON externalidentifier USING btree (registryobject);

--
-- TOC entry 1896 (class 1259 OID 78781)
-- Dependencies: 1567
-- Name: src_ass_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX src_ass_idx ON association USING btree (sourceobject);


--
-- TOC entry 1897 (class 1259 OID 78783)
-- Dependencies: 1567
-- Name: tgt_ass_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX tgt_ass_idx ON association USING btree (targetobject);


--
-- TOC entry 1898 (class 1259 OID 78785)
-- Dependencies: 1567
-- Name: type_ass_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX type_ass_idx ON association USING btree (associationtype);

--
-- TOC entry 1919 (class 1259 OID 80074)
-- Dependencies: 1574
-- Name: value_eid_idx; Type: INDEX; Schema: public; Owner: omar; Tablespace: 
--

CREATE INDEX value_eid_idx ON externalidentifier USING btree (value);

-- Additional indexes (on LID).
CREATE INDEX lid_eo_idx ON extrinsicobject USING btree(lid);
CREATE INDEX lid_rp_idx ON registrypackage USING btree(lid);

--
-- TOC entry 1976 (class 0 OID 0)
-- Dependencies: 6
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2009-06-18 22:56:23

--
-- PostgreSQL database dump complete
--

