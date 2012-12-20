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

DROP TABLE subject_match_fields;
DROP TABLE subject_identifier;
DROP TABLE subject_identifier_domain;
DROP TABLE subject_xref;
DROP TABLE subject_demographics;
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
    resource_id varchar(100) NOT NULL,
    CONSTRAINT resource_lock_pkey PRIMARY KEY (resource_id)
);

--ALTER TABLE public.resource_lock OWNER TO empi;

--
--
--
CREATE TABLE subject (
    id int NOT NULL,
    type char(1),
    last_updated_time datetime,
    CONSTRAINT subject_pkey PRIMARY KEY (id)
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
    religious_affiliation_code varchar(25),
    CONSTRAINT subject_demographics_pkey PRIMARY KEY (subject_id),
    CONSTRAINT subject_demographics_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id)
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
    use_ varchar(50),
    CONSTRAINT subject_address_pkey PRIMARY KEY (subject_id, seq_no),
    CONSTRAINT subject_address_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id)
);

--ALTER TABLE public.subject_address OWNER TO empi;

--
--
--
CREATE TABLE subject_citizenship (
    subject_id int NOT NULL,
    seq_no tinyint NOT NULL,
    nation_code varchar(25),
    nation_name varchar(100),
    CONSTRAINT subject_citizenship_pkey PRIMARY KEY (subject_id, seq_no),
    CONSTRAINT subject_citizenship_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id)
);

--ALTER TABLE public.subject_citizenship OWNER TO empi;

--
--
--
CREATE TABLE subject_identifier_domain (
    id int NOT NULL,
    namespace_id varchar(50),
    universal_id varchar(50) NOT NULL,
    universal_id_type varchar(25),
    CONSTRAINT subject_identifier_domain_pkey PRIMARY KEY (id)
);

--ALTER TABLE public.subject_identifier_domain OWNER TO empi;

--
--
--
CREATE TABLE subject_identifier (
    subject_id int NOT NULL,
    seq_no smallint NOT NULL,
    type char(1) NOT NULL,
    identifier varchar(100) NOT NULL,
    subject_identifier_domain_id int NOT NULL,
    CONSTRAINT subject_identifier_pkey PRIMARY KEY (subject_id, seq_no),
    CONSTRAINT subject_identifier_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id),
    CONSTRAINT subject_identifier_subject_identifier_domain_id_fkey FOREIGN KEY (subject_identifier_domain_id) REFERENCES subject_identifier_domain(id)
);

--ALTER TABLE public.subject_identifier OWNER TO empi;


--
--
--
CREATE TABLE subject_language (
    subject_id int NOT NULL,
    seq_no smallint NOT NULL,
    preference_indicator bit,
    language_code varchar(25) NOT NULL,
    CONSTRAINT subject_language_pkey PRIMARY KEY (subject_id, seq_no),
    CONSTRAINT subject_language_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id)
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
    ssn char(11),
    CONSTRAINT subject_match_fields_pkey PRIMARY KEY (subject_id),
    CONSTRAINT subject_match_fields_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id)
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
    middle_name varchar(100),
    CONSTRAINT subject_name_pkey PRIMARY KEY (subject_id, seq_no),
    CONSTRAINT subject_name_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id)
);

--ALTER TABLE public.subject_name OWNER TO empi;

--
--
--
CREATE TABLE subject_personal_relationship (
    subject_id int NOT NULL,
    seq_no smallint NOT NULL,
    personal_relationship_subject_id int,
    subject_personal_relationship_code varchar(25),
    CONSTRAINT subject_personal_relationship_pkey PRIMARY KEY (subject_id, seq_no),
    CONSTRAINT subject_personal_relationship_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id)
-- FIXME: Add constraint on personal_relationship_subject_id?
);

--ALTER TABLE public.subject_personal_relationship OWNER TO empi;

--
--
--
CREATE TABLE subject_review_item (
    subject_id_left int NOT NULL,
    subject_id_right int NOT NULL,
    review_type char(2) NOT NULL,
    CONSTRAINT subject_review_item_pkey PRIMARY KEY (subject_id_left, subject_id_right, review_type),
    CONSTRAINT subject_review_item_subject_id_left_fkey FOREIGN KEY (subject_id_left) REFERENCES subject(id),
    CONSTRAINT subject_review_item_subject_id_right_fkey FOREIGN KEY (subject_id_right) REFERENCES subject(id)
);

--ALTER TABLE public.subject_review_item OWNER TO empi;

--
--
--
CREATE TABLE subject_telecom_address (
    subject_id int NOT NULL,
    seq_no smallint NOT NULL,
    use_ varchar(50),
    value varchar(255),
    CONSTRAINT subject_telecom_address_pkey PRIMARY KEY (subject_id, seq_no),
    CONSTRAINT subject_telecom_address_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id)
);

--ALTER TABLE public.subject_telecom_address OWNER TO empi;

--
--
--
CREATE TABLE subject_xref (
    enterprise_subject_id int NOT NULL,
    system_subject_id int NOT NULL,
    match_score double precision,
    CONSTRAINT subject_xref_pkey PRIMARY KEY (enterprise_subject_id, system_subject_id),
    CONSTRAINT subject_xref_enterprise_subject_id_fkey FOREIGN KEY (enterprise_subject_id) REFERENCES subject(id),
    CONSTRAINT subject_xref_system_subject_id_fkey FOREIGN KEY (system_subject_id) REFERENCES subject(id)
);

--ALTER TABLE public.subject_xref OWNER TO empi;

---------------------------
-- Indexes.
---------------------------

CREATE UNIQUE INDEX subject_identifier_domain_universal_id_idx ON subject_identifier_domain (universal_id);
CREATE UNIQUE INDEX subject_identifier_domain_namespace_id_idx ON subject_identifier_domain (namespace_id);

CREATE INDEX subject_identifier_search_idx ON subject_identifier (identifier, subject_identifier_domain_id);

CREATE INDEX subject_match_fields_block_pass_1_idx ON subject_match_fields (family_name_soundex, given_name_soundex, gender);
CREATE INDEX subject_match_fields_block_pass_2_idx ON subject_match_fields (street_address_line1, city, state, postal_code);
CREATE INDEX subject_match_fields_block_pass_3_idx ON subject_match_fields (ssn);
CREATE INDEX subject_match_fields_block_pass_fuzzy_name_idx ON subject_match_fields (family_name, given_name);


--REVOKE ALL ON SCHEMA public FROM PUBLIC;
--REVOKE ALL ON SCHEMA public FROM postgres;
--GRANT ALL ON SCHEMA public TO postgres;
--GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2012-06-27 16:07:03

--
-- PostgreSQL database dump complete
--

