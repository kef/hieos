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
-- Microsoft SQL Server DDL for HIEOS EMPI schema
--
-- Author: Bernie Thuman

-- Started on 2011-06-27 16:07:03

USE empi;

DROP TABLE subject_demographics;
DROP TABLE subject_match_fields;
DROP TABLE subject_identifier;
DROP TABLE subject_identifier_domain;
DROP TABLE subject_xref;
DROP TABLE subject_address;
DROP TABLE subject_citizenship;
DROP TABLE subject_name;
DROP TABLE subject_language;
DROP TABLE subject_telecom_address;
DROP TABLE subject_personal_relationship;
DROP TABLE subject_review_item;
DROP TABLE subject_review;  -- old
DROP TABLE resource_lock;
DROP TABLE subject;

DROP SEQUENCE subject_seq;

---------------------------
-- Sequences
---------------------------
CREATE SEQUENCE subject_seq
  START WITH 1
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  CACHE 5;

--ALTER TABLE subject_seq OWNER TO empi;

---------------------------
-- Tables
---------------------------

--
--
--
CREATE TABLE resource_lock (
    resource_id varchar(100) NOT NULL
);

--ALTER TABLE public.resource_lock OWNER TO empi;

--
--
--
CREATE TABLE subject (
    id int NOT NULL,
    type char(1),
    last_updated_time datetime
);

--ALTER TABLE public.resource_lock OWNER TO empi;

--
--
--
CREATE TABLE subject_demographics (
    subject_id int NOT NULL,
    birth_time date,
    deceased_indicator bit,
    deceased_time date,
    multiple_birth_indicator bit,
    multiple_birth_order_number smallint,
    gender_code varchar(2),
    marital_status_code varchar(25),
    ethnic_group_code varchar(25),
    race_code varchar(25),
    religious_affiliation_code varchar(25)
);

--ALTER TABLE public.subject OWNER TO empi;

--
--
--
CREATE TABLE subject_address (
    subject_id int NOT NULL,
    seq_no tinyint NOT NULL,
    street_address_line1 varchar(100),
    street_address_line2 varchar(100),
    street_address_line3 varchar(100),
    city varchar(100),
    state varchar(100),
    postal_code varchar(50),
    use_ varchar(50)
);

--ALTER TABLE public.subject_address OWNER TO empi;

--
--
--
CREATE TABLE subject_citizenship (
    subject_id int NOT NULL,
    seq_no tinyint NOT NULL,
    nation_code varchar(25),
    nation_name varchar(100)
);

--ALTER TABLE public.subject_citizenship OWNER TO empi;

--
--
--
CREATE TABLE subject_identifier (
    subject_id int NOT NULL,
    seq_no smallint NOT NULL,
    type char(1) NOT NULL,
    identifier varchar(100) NOT NULL,
    subject_identifier_domain_id int NOT NULL
);

--ALTER TABLE public.subject_identifier OWNER TO empi;

--
--
--
CREATE TABLE subject_identifier_domain (
    id int NOT NULL,
    namespace_id varchar(50),
    universal_id varchar(50) NOT NULL,
    universal_id_type varchar(25)
);

--ALTER TABLE public.subject_identifier_domain OWNER TO empi;

--
--
--
CREATE TABLE subject_language (
    subject_id int NOT NULL,
    seq_no smallint NOT NULL,
    preference_indicator bit,
    language_code varchar(25) NOT NULL
);

--ALTER TABLE public.subject_language OWNER TO empi;

--
-- NOTE: You can include additional "subject match fields" by 1) adding columns to the
-- "subject_match_field" table and 2) configuring "empiConfig.xml" accordingly (
-- no code changes required).  Also, be sure to address relevant indexes based upon configured
-- blocking passes (also specified in "empiConfig.xml).
--
CREATE TABLE subject_match_fields (
    subject_id int NOT NULL,
    family_name_double_metaphone varchar(255),
    given_name_double_metaphone varchar(255),
    family_name varchar(100),
    given_name varchar(100),
    birth_time char(8),
    family_name_soundex char(4),
    given_name_soundex char(4),
    street_address_line1 varchar(100),
    city varchar(100),
    state varchar(100),
    postal_code varchar(50),
    family_name_caverphone2 varchar(255),
    family_name_caverphone1 varchar(255),
    family_name_prefix varchar(2),
    gender char(1),
    ssn char(11)
);

--ALTER TABLE public.subject_match_fields OWNER TO empi;

--
--
--
CREATE TABLE subject_name (
    subject_id int NOT NULL,
    seq_no smallint NOT NULL,
    given_name varchar(100),
    family_name varchar(100),
    prefix varchar(25),
    suffix varchar(25),
    middle_name varchar(100)
);

--ALTER TABLE public.subject_name OWNER TO empi;

--
--
--
CREATE TABLE subject_personal_relationship (
    subject_id int NOT NULL,
    seq_no smallint NOT NULL,
    personal_relationship_subject_id int,
    subject_personal_relationship_code varchar(25)
);

--ALTER TABLE public.subject_personal_relationship OWNER TO empi;

--
--
--
CREATE TABLE subject_review_item (
    subject_id_left int NOT NULL,
    subject_id_right int NOT NULL,
    review_type char(2) NOT NULL
);

--ALTER TABLE public.subject_review_item OWNER TO empi;

--
--
--
CREATE TABLE subject_telecom_address (
    subject_id int NOT NULL,
    seq_no smallint NOT NULL,
    use_ varchar(50),
    value varchar(255)
);

--ALTER TABLE public.subject_telecom_address OWNER TO empi;

--
--
--
CREATE TABLE subject_xref (
    enterprise_subject_id int NOT NULL,
    system_subject_id int NOT NULL,
    match_score double precision
);

--ALTER TABLE public.subject_xref OWNER TO empi;

---------------------------
-- Primary key constraints.
---------------------------

ALTER TABLE resource_lock
    ADD CONSTRAINT resource_lock_pkey PRIMARY KEY (resource_id);

ALTER TABLE subject_demographics
    ADD CONSTRAINT subject_address_pkey PRIMARY KEY (subject_id, seq_no);

ALTER TABLE subject_address
    ADD CONSTRAINT subject_address_pkey PRIMARY KEY (subject_id, seq_no);

ALTER TABLE subject_citizenship
    ADD CONSTRAINT subject_citizenship_pkey PRIMARY KEY (subject_id, seq_no);

ALTER TABLE subject_identifier_domain
    ADD CONSTRAINT subject_identifier_domain_pkey PRIMARY KEY (id);

ALTER TABLE subject_identifier
    ADD CONSTRAINT subject_identifier_pkey PRIMARY KEY (subject_id, seq_no);

ALTER TABLE subject_language
    ADD CONSTRAINT subject_language_pkey PRIMARY KEY (subject_id, seq_no);

ALTER TABLE subject_match_fields
    ADD CONSTRAINT subject_match_fields_pkey PRIMARY KEY (subject_id);

ALTER TABLE subject_name
    ADD CONSTRAINT subject_name_pkey PRIMARY KEY (subject_id, seq_no);

ALTER TABLE subject_personal_relationship
    ADD CONSTRAINT subject_personal_relationship_pkey PRIMARY KEY (subject_id, seq_no);

ALTER TABLE subject
    ADD CONSTRAINT subject_pkey PRIMARY KEY (id);

ALTER TABLE subject_review_item
    ADD CONSTRAINT subject_review_item_pkey PRIMARY KEY (subject_id_left, subject_id_right, review_type);

ALTER TABLE subject_telecom_address
    ADD CONSTRAINT subject_telecom_address_pkey PRIMARY KEY (subject_id, seq_no);

ALTER TABLE subject_xref
    ADD CONSTRAINT subject_xref_pkey PRIMARY KEY (enterprise_subject_id, system_subject_id);

---------------------------
-- Indexes.
---------------------------
CREATE INDEX subject_demogrpahics_subject_id_idx ON subject_demographics (subject_id);

CREATE INDEX subject_address_subject_id_idx ON subject_address (subject_id);

CREATE INDEX subject_citizenship_subject_id_idx ON subject_citizenship (subject_id);

CREATE UNIQUE INDEX subject_id_idx ON subject (id);

CREATE UNIQUE INDEX subject_identifier_domain_universal_id_idx ON subject_identifier_domain (universal_id);
CREATE UNIQUE INDEX subject_identifier_domain_namespace_id_idx ON subject_identifier_domain (namespace_id);

CREATE INDEX subject_identifier_search_idx ON subject_identifier (identifier, subject_identifier_domain_id);

CREATE INDEX subject_identifier_subject_id_idx ON subject_identifier (subject_id);

CREATE INDEX subject_language_subject_id_idx ON subject_language (subject_id);

CREATE INDEX subject_match_fields_block_pass_1_idx ON subject_match_fields (family_name_soundex, given_name_soundex, gender);

CREATE INDEX subject_match_fields_block_pass_2_idx ON subject_match_fields (street_address_line1, city, state, postal_code);

CREATE INDEX subject_match_fields_block_pass_3_idx ON subject_match_fields (ssn);

CREATE INDEX subject_match_fields_block_pass_fuzzy_name_idx ON subject_match_fields (family_name, given_name);

CREATE INDEX subject_name_subject_id_idx ON subject_name (subject_id);

CREATE INDEX subject_personal_relationship_subject_id_idx ON subject_personal_relationship (subject_id);

CREATE INDEX subject_telecom_address_subject_id_idx ON subject_telecom_address (subject_id);

CREATE INDEX subject_xref_enterprise_subject_id_idx ON subject_xref (enterprise_subject_id);

CREATE INDEX subject_xref_system_subject_id_idx ON subject_xref (system_subject_id);

---------------------------
-- Foreign key references.
---------------------------
ALTER TABLE subject_identifier
    ADD CONSTRAINT subject_identifier_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id);

ALTER TABLE subject_identifier
    ADD CONSTRAINT subject_identifier_subject_identifier_domain_id_fkey FOREIGN KEY (subject_identifier_domain_id) REFERENCES subject_identifier_domain(id);

ALTER TABLE subject_xref
    ADD CONSTRAINT subject_xref_enterprise_subject_id_fkey FOREIGN KEY (enterprise_subject_id) REFERENCES subject(id);

ALTER TABLE subject_xref
    ADD CONSTRAINT subject_xref_system_subject_id_fkey FOREIGN KEY (system_subject_id) REFERENCES subject(id);

ALTER TABLE subject_demographics
    ADD CONSTRAINT subject_demographics_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id);

ALTER TABLE subject_address
    ADD CONSTRAINT subject_address_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id);

ALTER TABLE subject_name
    ADD CONSTRAINT subject_name_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id);

ALTER TABLE subject_telecom_address
    ADD CONSTRAINT subject_telecom_address_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id);

ALTER TABLE subject_match_fields
    ADD CONSTRAINT subject_match_fields_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id);

ALTER TABLE subject_personal_relationship
    ADD CONSTRAINT subject_personal_relationship_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id);

ALTER TABLE subject_language
    ADD CONSTRAINT subject_language_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id);

ALTER TABLE subject_citizenship
    ADD CONSTRAINT subject_citizenship_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id);

ALTER TABLE subject_review_item
    ADD CONSTRAINT subject_review_item_subject_id_left_fkey FOREIGN KEY (subject_id_left) REFERENCES subject(id);

ALTER TABLE subject_review_item
    ADD CONSTRAINT subject_review_item_subject_id_right_fkey FOREIGN KEY (subject_id_right) REFERENCES subject(id);

--REVOKE ALL ON SCHEMA public FROM PUBLIC;
--REVOKE ALL ON SCHEMA public FROM postgres;
--GRANT ALL ON SCHEMA public TO postgres;
--GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2012-06-27 16:07:03

--
-- PostgreSQL database dump complete
--

