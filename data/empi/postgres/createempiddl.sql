--
--  This code is subject to the HIEOS License, Version 1.0
--
--  Copyright(c) 2012 Vangent, Inc.  All rights reserved.
--
--  Unless required by applicable law or agreed to in writing, software
--  distributed under the License is distributed on an "AS IS" BASIS,
--  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--
--  See the License for the specific language governing permissions and
--  limitations under the License.
--

--
-- PostgreSQL DDL for HIEOS EMPI schema
--
-- Author: Bernie Thuman

-- Started on 2011-06-27 16:07:03

SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- TOC entry 1829 (class 1262 OID 1337539)
-- Name: empi; Type: DATABASE; Schema: -; Owner: empi
--

--CREATE DATABASE empi WITH TEMPLATE = template0 ENCODING = 'WIN1252';


--ALTER DATABASE empi OWNER TO empi;

DROP TABLE IF EXISTS subject_match_fields;
DROP TABLE IF EXISTS subject_identifier;
DROP TABLE IF EXISTS subject_identifier_domain;
DROP TABLE IF EXISTS subject_xref;
DROP TABLE IF EXISTS subject_address;
DROP TABLE IF EXISTS subject_citizenship;
DROP TABLE IF EXISTS subject_name;
DROP TABLE IF EXISTS subject_language;
DROP TABLE IF EXISTS subject_telecom_address;
DROP TABLE IF EXISTS subject_personal_relationship;
DROP TABLE IF EXISTS subject_review;
DROP TABLE IF EXISTS resource_lock;
DROP TABLE IF EXISTS subject;

DROP SEQUENCE IF EXISTS subject_seq;

---------------------------
-- Sequences
---------------------------
CREATE SEQUENCE subject_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE subject_seq OWNER TO empi;

---------------------------
-- Tables
---------------------------

--
--
--
CREATE TABLE resource_lock (
    resource_id character varying(100) NOT NULL
);

ALTER TABLE public.resource_lock OWNER TO empi;

--
--
--
CREATE TABLE subject (
    id bigint NOT NULL,
    birth_time date,
    type character(1),
    deceased_indicator boolean,
    deceased_time date,
    multiple_birth_indicator boolean,
    multiple_birth_order_number integer,
    last_updated_time timestamp without time zone,
    gender_code character varying(2),
    marital_status_code character varying(25),
    ethnic_group_code character varying(25),
    race_code character varying(25),
    religious_affiliation_code character varying(25)
);

ALTER TABLE public.subject OWNER TO empi;

--
--
--
CREATE TABLE subject_address (
    subject_id bigint NOT NULL,
    seq_no smallint NOT NULL,
    street_address_line1 character varying(100),
    street_address_line2 character varying(100),
    street_address_line3 character varying(100),
    city character varying(100),
    state character varying(100),
    postal_code character varying(50),
    use character varying(50)
);

ALTER TABLE public.subject_address OWNER TO empi;

--
--
--
CREATE TABLE subject_citizenship (
    subject_id bigint NOT NULL,
    seq_no smallint NOT NULL,
    nation_code character varying(25),
    nation_name character varying(100)
);

ALTER TABLE public.subject_citizenship OWNER TO empi;

--
--
--
CREATE TABLE subject_identifier (
    subject_id bigint NOT NULL,
    seq_no smallint NOT NULL,
    type character(1) NOT NULL,
    identifier character varying(100) NOT NULL,
    subject_identifier_domain_id integer NOT NULL
);

ALTER TABLE public.subject_identifier OWNER TO empi;

--
--
--
CREATE TABLE subject_identifier_domain (
    id integer NOT NULL,
    namespace_id character varying(50),
    universal_id character varying(50) NOT NULL,
    universal_id_type character varying(25)
);

ALTER TABLE public.subject_identifier_domain OWNER TO empi;

--
--
--
CREATE TABLE subject_language (
    subject_id bigint NOT NULL,
    seq_no smallint NOT NULL,
    preference_indicator boolean,
    language_code character varying(25) NOT NULL
);

ALTER TABLE public.subject_language OWNER TO empi;

--
-- NOTE: You can include additional "subject match fields" by 1) adding columns to the
-- "subject_match_field" table and 2) configuring "empiConfig.xml" accordingly (
-- no code changes required).  Also, be sure to address relevant indexes based upon configured
-- blocking passes (also specified in "empiConfig.xml).
--
CREATE TABLE subject_match_fields (
    subject_id bigint NOT NULL,
    family_name_double_metaphone character varying(255),
    given_name_double_metaphone character varying(255),
    family_name character varying(255),
    given_name character varying(255),
    birth_time character varying(255),
    family_name_soundex character varying(255),
    given_name_soundex character varying(255),
    street_address_line1 character varying(255),
    city character varying(255),
    state character varying(255),
    postal_code character varying(255),
    family_name_caverphone2 character varying(255),
    family_name_caverphone1 character varying(255),
    family_name_prefix character varying(2),
    gender character(1)
);

ALTER TABLE public.subject_match_fields OWNER TO empi;

--
--
--
CREATE TABLE subject_name (
    subject_id bigint NOT NULL,
    seq_no smallint NOT NULL,
    given_name character varying(100),
    family_name character varying(100),
    prefix character varying(25),
    suffix character varying(25),
    middle_name character varying(100)
);

ALTER TABLE public.subject_name OWNER TO empi;

--
--
--
CREATE TABLE subject_personal_relationship (
    subject_id bigint NOT NULL,
    seq_no smallint NOT NULL,
    personal_relationship_subject_id bigint,
    subject_personal_relationship_code character varying(25)
);

ALTER TABLE public.subject_personal_relationship OWNER TO empi;

--
--
--
CREATE TABLE subject_review (
    subject_id_left bigint NOT NULL,
    subject_id_right bigint NOT NULL,
    type character(1) NOT NULL
);

ALTER TABLE public.subject_review OWNER TO empi;

--
--
--
CREATE TABLE subject_telecom_address (
    subject_id bigint NOT NULL,
    seq_no smallint NOT NULL,
    use character varying(50),
    value character varying(255)
);

ALTER TABLE public.subject_telecom_address OWNER TO empi;

--
--
--
CREATE TABLE subject_xref (
    enterprise_subject_id bigint NOT NULL,
    system_subject_id bigint NOT NULL,
    match_score double precision
);

ALTER TABLE public.subject_xref OWNER TO empi;

---------------------------
-- Primary key constraints.
---------------------------

ALTER TABLE ONLY resource_lock
    ADD CONSTRAINT resource_lock_pkey PRIMARY KEY (resource_id);

ALTER TABLE ONLY subject_address
    ADD CONSTRAINT subject_address_pkey PRIMARY KEY (subject_id, seq_no);

ALTER TABLE ONLY subject_citizenship
    ADD CONSTRAINT subject_citizenship_pkey PRIMARY KEY (subject_id, seq_no);

ALTER TABLE ONLY subject_identifier_domain
    ADD CONSTRAINT subject_identifier_domain_pkey PRIMARY KEY (id);

ALTER TABLE ONLY subject_identifier
    ADD CONSTRAINT subject_identifier_pkey PRIMARY KEY (subject_id, seq_no);

ALTER TABLE ONLY subject_language
    ADD CONSTRAINT subject_language_pkey PRIMARY KEY (subject_id, seq_no);

ALTER TABLE ONLY subject_match_fields
    ADD CONSTRAINT subject_match_pkey PRIMARY KEY (subject_id);

ALTER TABLE ONLY subject_name
    ADD CONSTRAINT subject_name_pkey PRIMARY KEY (subject_id, seq_no);

ALTER TABLE ONLY subject_personal_relationship
    ADD CONSTRAINT subject_personal_relationship_pkey PRIMARY KEY (subject_id, seq_no);

ALTER TABLE ONLY subject
    ADD CONSTRAINT subject_pkey PRIMARY KEY (id);

ALTER TABLE ONLY subject_review
    ADD CONSTRAINT subject_review_pkey PRIMARY KEY (subject_id_left, subject_id_right, type);

ALTER TABLE ONLY subject_telecom_address
    ADD CONSTRAINT subject_telecom_address_pkey PRIMARY KEY (subject_id, seq_no);

ALTER TABLE ONLY subject_xref
    ADD CONSTRAINT subject_xref_pkey PRIMARY KEY (enterprise_subject_id, system_subject_id);

---------------------------
-- Indexes.
---------------------------
CREATE INDEX subject_address_subject_id_idx ON subject_address USING btree (subject_id);

CREATE INDEX subject_citizenship_subject_id_idx ON subject_citizenship USING btree (subject_id);

CREATE UNIQUE INDEX subject_id_idx ON subject USING btree (id);

CREATE INDEX subject_identifier_domain_universal_id_idx ON subject_identifier_domain USING btree (universal_id);

CREATE INDEX subject_identifier_search_idx ON subject_identifier USING btree (identifier, subject_identifier_domain_id);

CREATE INDEX subject_identifier_subject_id_idx ON subject_identifier USING btree (subject_id);

CREATE INDEX subject_language_subject_id_idx ON subject_language USING btree (subject_id);

CREATE INDEX subject_match_fields_block_pass_1_idx ON subject_match_fields USING btree (family_name_soundex, given_name_soundex, gender);

CREATE INDEX subject_match_fields_block_pass_2_idx ON subject_match_fields USING btree (street_address_line1, city, state, postal_code);

CREATE INDEX subject_match_fields_block_pass_fuzzy_name_idx ON subject_match_fields USING btree (family_name, given_name);

CREATE INDEX subject_name_subject_id_idx ON subject_name USING btree (subject_id);

CREATE INDEX subject_personal_relationship_subject_id_idx ON subject_personal_relationship USING btree (subject_id);

CREATE INDEX subject_telecom_address_subject_id_idx ON subject_telecom_address USING btree (subject_id);

CREATE INDEX subject_xref_enterprise_subject_id_idx ON subject_xref USING btree (enterprise_subject_id);

CREATE INDEX subject_xref_system_subject_id_idx ON subject_xref USING btree (system_subject_id);

---------------------------
-- Foreign key references.
---------------------------
ALTER TABLE ONLY subject_identifier
    ADD CONSTRAINT subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id);

ALTER TABLE ONLY subject_identifier
    ADD CONSTRAINT subject_identifier_domain_id_fkey FOREIGN KEY (subject_identifier_domain_id) REFERENCES subject_identifier_domain(id);

ALTER TABLE ONLY subject_xref
    ADD CONSTRAINT enterprise_subject_id_fkey FOREIGN KEY (enterprise_subject_id) REFERENCES subject(id);

ALTER TABLE ONLY subject_xref
    ADD CONSTRAINT system_subject_id_fkey FOREIGN KEY (system_subject_id) REFERENCES subject(id);

ALTER TABLE ONLY subject_address
    ADD CONSTRAINT subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id);

ALTER TABLE ONLY subject_name
    ADD CONSTRAINT subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id);

ALTER TABLE ONLY subject_telecom_address
    ADD CONSTRAINT subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id);

ALTER TABLE ONLY subject_match_fields
    ADD CONSTRAINT subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id);

ALTER TABLE ONLY subject_personal_relationship
    ADD CONSTRAINT subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id);

ALTER TABLE ONLY subject_language
    ADD CONSTRAINT subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id);

ALTER TABLE ONLY subject_citizenship
    ADD CONSTRAINT subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id);

--REVOKE ALL ON SCHEMA public FROM PUBLIC;
--REVOKE ALL ON SCHEMA public FROM postgres;
--GRANT ALL ON SCHEMA public TO postgres;
--GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2012-06-27 16:07:03

--
-- PostgreSQL database dump complete
--

