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
DROP TABLE IF EXISTS subject_other_identifier;
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


--
-- TOC entry 1502 (class 1259 OID 2078869)
-- Dependencies: 3
-- Name: resource_lock; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE resource_lock (
    resource_id character varying(100) NOT NULL
);


ALTER TABLE public.resource_lock OWNER TO empi;

--
-- TOC entry 1490 (class 1259 OID 1337540)
-- Dependencies: 3
-- Name: subject; Type: TABLE; Schema: public; Owner: empi; Tablespace: 
--

CREATE TABLE subject (
    id character varying(64) NOT NULL,
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
-- TOC entry 1491 (class 1259 OID 1337546)
-- Dependencies: 3
-- Name: subject_address; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE subject_address (
    street_address_line1 character varying(100),
    street_address_line2 character varying(100),
    street_address_line3 character varying(100),
    city character varying(100),
    state character varying(100),
    postal_code character varying(50),
    subject_id character varying(64) NOT NULL,
    id character varying(64) NOT NULL,
    use character varying(50)
);


ALTER TABLE public.subject_address OWNER TO empi;

--
-- TOC entry 1501 (class 1259 OID 1352012)
-- Dependencies: 3
-- Name: subject_citizenship; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE subject_citizenship (
    id character varying(64) NOT NULL,
    subject_id character varying(64) NOT NULL,
    nation_code character varying(25),
    nation_name character varying(100)
);


ALTER TABLE public.subject_citizenship OWNER TO empi;

--
-- TOC entry 1493 (class 1259 OID 1337558)
-- Dependencies: 3
-- Name: subject_identifier; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE subject_identifier (
    subject_id character varying(64) NOT NULL,
    identifier character varying(100) NOT NULL,
    subject_identifier_domain_id integer NOT NULL,
    id character varying(64) NOT NULL
);


ALTER TABLE public.subject_identifier OWNER TO empi;

--
-- TOC entry 1494 (class 1259 OID 1337561)
-- Dependencies: 3
-- Name: subject_identifier_domain; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE subject_identifier_domain (
    namespace_id character varying(50),
    universal_id character varying(50) NOT NULL,
    universal_id_type character varying(25),
    id integer NOT NULL
);


ALTER TABLE public.subject_identifier_domain OWNER TO empi;

--
-- TOC entry 1500 (class 1259 OID 1350024)
-- Dependencies: 3
-- Name: subject_language; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE subject_language (
    id character varying(64) NOT NULL,
    subject_id character varying(64) NOT NULL,
    preference_indicator boolean,
    language_code character varying(25) NOT NULL
);


ALTER TABLE public.subject_language OWNER TO empi;

--
-- TOC entry 1495 (class 1259 OID 1337604)
-- Dependencies: 3
-- Name: subject_match_fields; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

--
-- NOTE: You can include additional "subject match fields" by 1) adding columns to the
-- "subject_match_field" table and 2) configuring "empiConfig.xml" accordingly (
-- no code changes required).  Also, be sure to address relevant indexes based upon configured
-- blocking passes (also specified in "empiConfig.xml).
--
CREATE TABLE subject_match_fields (
    subject_id character varying(64) NOT NULL,
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
-- TOC entry 1492 (class 1259 OID 1337551)
-- Dependencies: 3
-- Name: subject_name; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE subject_name (
    given_name character varying(100),
    family_name character varying(100),
    subject_id character varying(64) NOT NULL,
    prefix character varying(25),
    suffix character varying(25),
    middle_name character varying(100),
    id character varying(64) NOT NULL
);


ALTER TABLE public.subject_name OWNER TO empi;

--
-- TOC entry 1497 (class 1259 OID 1337977)
-- Dependencies: 3
-- Name: subject_other_identifier; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE subject_other_identifier (
    subject_id character varying(64) NOT NULL,
    identifier character varying(100) NOT NULL,
    subject_identifier_domain_id integer NOT NULL,
    id character varying(64) NOT NULL
);


ALTER TABLE public.subject_other_identifier OWNER TO empi;

--
-- TOC entry 1499 (class 1259 OID 1349830)
-- Dependencies: 3
-- Name: subject_personal_relationship; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE subject_personal_relationship (
    id character varying(64) NOT NULL,
    subject_id character varying(64) NOT NULL,
    personal_relationship_subject_id character varying(64),
    subject_personal_relationship_code character varying(25)
);


ALTER TABLE public.subject_personal_relationship OWNER TO empi;

--
-- TOC entry 1503 (class 1259 OID 2087397)
-- Dependencies: 3
-- Name: subject_review; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE subject_review (
    id character varying(64) NOT NULL,
    type character(1) NOT NULL,
    subject_id_left character varying(64) NOT NULL,
    subject_id_right character varying(64) NOT NULL
);


ALTER TABLE public.subject_review OWNER TO empi;

--
-- TOC entry 1498 (class 1259 OID 1338097)
-- Dependencies: 3
-- Name: subject_telecom_address; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE subject_telecom_address (
    id character varying(64) NOT NULL,
    subject_id character varying(64) NOT NULL,
    use character varying(50),
    value character varying(255)
);


ALTER TABLE public.subject_telecom_address OWNER TO empi;

--
-- TOC entry 1496 (class 1259 OID 1337661)
-- Dependencies: 3
-- Name: subject_xref; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE subject_xref (
    enterprise_subject_id character varying(64) NOT NULL,
    system_subject_id character varying(64) NOT NULL,
    match_score double precision
);


ALTER TABLE public.subject_xref OWNER TO empi;

--
-- TOC entry 1811 (class 2606 OID 2078873)
-- Dependencies: 1502 1502
-- Name: resource_lock_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY resource_lock
    ADD CONSTRAINT resource_lock_pkey PRIMARY KEY (resource_id);


--
-- TOC entry 1774 (class 2606 OID 1338010)
-- Dependencies: 1491 1491
-- Name: subject_address_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY subject_address
    ADD CONSTRAINT subject_address_pkey PRIMARY KEY (id);


--
-- TOC entry 1808 (class 2606 OID 1352016)
-- Dependencies: 1501 1501
-- Name: subject_citizenship_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY subject_citizenship
    ADD CONSTRAINT subject_citizenship_pkey PRIMARY KEY (id);


--
-- TOC entry 1784 (class 2606 OID 1338031)
-- Dependencies: 1494 1494
-- Name: subject_identifier_domain_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY subject_identifier_domain
    ADD CONSTRAINT subject_identifier_domain_pkey PRIMARY KEY (id);


--
-- TOC entry 1780 (class 2606 OID 1347823)
-- Dependencies: 1493 1493
-- Name: subject_identifier_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY subject_identifier
    ADD CONSTRAINT subject_identifier_pkey PRIMARY KEY (id);


--
-- TOC entry 1805 (class 2606 OID 1350028)
-- Dependencies: 1500 1500
-- Name: subject_language_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY subject_language
    ADD CONSTRAINT subject_language_pkey PRIMARY KEY (id);


--
-- TOC entry 1790 (class 2606 OID 1338038)
-- Dependencies: 1495 1495
-- Name: subject_match_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY subject_match_fields
    ADD CONSTRAINT subject_match_pkey PRIMARY KEY (subject_id);


--
-- TOC entry 1777 (class 2606 OID 1338040)
-- Dependencies: 1492 1492
-- Name: subject_name_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY subject_name
    ADD CONSTRAINT subject_name_pkey PRIMARY KEY (id);


--
-- TOC entry 1796 (class 2606 OID 1347825)
-- Dependencies: 1497 1497
-- Name: subject_other_identifier_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY subject_other_identifier
    ADD CONSTRAINT subject_other_identifier_pkey PRIMARY KEY (id);


--
-- TOC entry 1802 (class 2606 OID 1349834)
-- Dependencies: 1499 1499
-- Name: subject_personal_relationship_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY subject_personal_relationship
    ADD CONSTRAINT subject_personal_relationship_pkey PRIMARY KEY (id);


--
-- TOC entry 1772 (class 2606 OID 1337550)
-- Dependencies: 1490 1490
-- Name: subject_pkey; Type: CONSTRAINT; Schema: public; Owner: empi; Tablespace: 
--

ALTER TABLE ONLY subject
    ADD CONSTRAINT subject_pkey PRIMARY KEY (id);


--
-- TOC entry 1813 (class 2606 OID 2087401)
-- Dependencies: 1503 1503
-- Name: subject_review_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY subject_review
    ADD CONSTRAINT subject_review_pkey PRIMARY KEY (id);


--
-- TOC entry 1799 (class 2606 OID 1338101)
-- Dependencies: 1498 1498
-- Name: subject_telecom_address_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY subject_telecom_address
    ADD CONSTRAINT subject_telecom_address_pkey PRIMARY KEY (id);


--
-- TOC entry 1793 (class 2606 OID 1338069)
-- Dependencies: 1496 1496 1496
-- Name: subject_xref_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY subject_xref
    ADD CONSTRAINT subject_xref_pkey PRIMARY KEY (enterprise_subject_id, system_subject_id);


--
-- TOC entry 1775 (class 1259 OID 1350068)
-- Dependencies: 1491
-- Name: subject_address_subject_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX subject_address_subject_id_idx ON subject_address USING btree (subject_id);


--
-- TOC entry 1809 (class 1259 OID 1352023)
-- Dependencies: 1501
-- Name: subject_citizenship_subject_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX subject_citizenship_subject_id_idx ON subject_citizenship USING btree (subject_id);


--
-- TOC entry 1770 (class 1259 OID 1350067)
-- Dependencies: 1490
-- Name: subject_id_idx; Type: INDEX; Schema: public; Owner: empi; Tablespace: 
--

CREATE UNIQUE INDEX subject_id_idx ON subject USING btree (id);


--
-- TOC entry 1785 (class 1259 OID 2087370)
-- Dependencies: 1494
-- Name: subject_identifier_domain_universal_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX subject_identifier_domain_universal_id_idx ON subject_identifier_domain USING btree (universal_id);


--
-- TOC entry 1781 (class 1259 OID 1350071)
-- Dependencies: 1493 1493
-- Name: subject_identifier_search_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX subject_identifier_search_idx ON subject_identifier USING btree (identifier, subject_identifier_domain_id);


--
-- TOC entry 1782 (class 1259 OID 1350069)
-- Dependencies: 1493
-- Name: subject_identifier_subject_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX subject_identifier_subject_id_idx ON subject_identifier USING btree (subject_id);


--
-- TOC entry 1806 (class 1259 OID 1350072)
-- Dependencies: 1500
-- Name: subject_language_subject_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX subject_language_subject_id_idx ON subject_language USING btree (subject_id);


--
-- TOC entry 1786 (class 1259 OID 2087396)
-- Dependencies: 1495 1495 1495
-- Name: subject_match_fields_block_pass_1_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX subject_match_fields_block_pass_1_idx ON subject_match_fields USING btree (family_name_soundex, given_name_soundex, gender);


--
-- TOC entry 1787 (class 1259 OID 2087374)
-- Dependencies: 1495 1495 1495 1495
-- Name: subject_match_fields_block_pass_2_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX subject_match_fields_block_pass_2_idx ON subject_match_fields USING btree (street_address_line1, city, state, postal_code);


--
-- TOC entry 1788 (class 1259 OID 2087375)
-- Dependencies: 1495 1495
-- Name: subject_match_fields_block_pass_fuzzy_name_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX subject_match_fields_block_pass_fuzzy_name_idx ON subject_match_fields USING btree (family_name, given_name);


--
-- TOC entry 1778 (class 1259 OID 1350073)
-- Dependencies: 1492
-- Name: subject_name_subject_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX subject_name_subject_id_idx ON subject_name USING btree (subject_id);


--
-- TOC entry 1797 (class 1259 OID 1350074)
-- Dependencies: 1497
-- Name: subject_other_identifier_subject_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX subject_other_identifier_subject_id_idx ON subject_other_identifier USING btree (subject_id);


--
-- TOC entry 1803 (class 1259 OID 1350075)
-- Dependencies: 1499
-- Name: subject_personal_relationship_subject_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX subject_personal_relationship_subject_id_idx ON subject_personal_relationship USING btree (subject_id);


--
-- TOC entry 1800 (class 1259 OID 1350076)
-- Dependencies: 1498
-- Name: subject_telecom_address_subject_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX subject_telecom_address_subject_id_idx ON subject_telecom_address USING btree (subject_id);


--
-- TOC entry 1791 (class 1259 OID 1350077)
-- Dependencies: 1496
-- Name: subject_xref_enterprise_subject_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX subject_xref_enterprise_subject_id_idx ON subject_xref USING btree (enterprise_subject_id);


--
-- TOC entry 1794 (class 1259 OID 1350078)
-- Dependencies: 1496
-- Name: subject_xref_system_subject_id_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX subject_xref_system_subject_id_idx ON subject_xref USING btree (system_subject_id);


--
-- TOC entry 1819 (class 2606 OID 1338070)
-- Dependencies: 1771 1496 1490
-- Name: enterprise_subject_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY subject_xref
    ADD CONSTRAINT enterprise_subject_id_fkey FOREIGN KEY (enterprise_subject_id) REFERENCES subject(id);


--
-- TOC entry 1814 (class 2606 OID 1338011)
-- Dependencies: 1771 1491 1490
-- Name: subject_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY subject_address
    ADD CONSTRAINT subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id);


--
-- TOC entry 1816 (class 2606 OID 1338019)
-- Dependencies: 1771 1493 1490
-- Name: subject_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY subject_identifier
    ADD CONSTRAINT subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id);


--
-- TOC entry 1815 (class 2606 OID 1338046)
-- Dependencies: 1490 1492 1771
-- Name: subject_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY subject_name
    ADD CONSTRAINT subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id);


--
-- TOC entry 1821 (class 2606 OID 1338053)
-- Dependencies: 1490 1497 1771
-- Name: subject_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY subject_other_identifier
    ADD CONSTRAINT subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id);


--
-- TOC entry 1823 (class 2606 OID 1338107)
-- Dependencies: 1490 1771 1498
-- Name: subject_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY subject_telecom_address
    ADD CONSTRAINT subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id);


--
-- TOC entry 1818 (class 2606 OID 1347922)
-- Dependencies: 1771 1490 1495
-- Name: subject_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY subject_match_fields
    ADD CONSTRAINT subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id);


--
-- TOC entry 1824 (class 2606 OID 1349840)
-- Dependencies: 1771 1499 1490
-- Name: subject_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY subject_personal_relationship
    ADD CONSTRAINT subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id);


--
-- TOC entry 1825 (class 2606 OID 1350029)
-- Dependencies: 1771 1490 1500
-- Name: subject_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY subject_language
    ADD CONSTRAINT subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id);


--
-- TOC entry 1826 (class 2606 OID 1352017)
-- Dependencies: 1490 1771 1501
-- Name: subject_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY subject_citizenship
    ADD CONSTRAINT subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subject(id);


--
-- TOC entry 1817 (class 2606 OID 1338032)
-- Dependencies: 1494 1493 1783
-- Name: subject_identifier_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY subject_identifier
    ADD CONSTRAINT subject_identifier_domain_id_fkey FOREIGN KEY (subject_identifier_domain_id) REFERENCES subject_identifier_domain(id);


--
-- TOC entry 1822 (class 2606 OID 1338063)
-- Dependencies: 1494 1497 1783
-- Name: subject_identifier_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY subject_other_identifier
    ADD CONSTRAINT subject_identifier_domain_id_fkey FOREIGN KEY (subject_identifier_domain_id) REFERENCES subject_identifier_domain(id);


--
-- TOC entry 1820 (class 2606 OID 1338075)
-- Dependencies: 1771 1490 1496
-- Name: system_subject_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY subject_xref
    ADD CONSTRAINT system_subject_id_fkey FOREIGN KEY (system_subject_id) REFERENCES subject(id);


--
-- TOC entry 1831 (class 0 OID 0)
-- Dependencies: 3
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

--REVOKE ALL ON SCHEMA public FROM PUBLIC;
--REVOKE ALL ON SCHEMA public FROM postgres;
--GRANT ALL ON SCHEMA public TO postgres;
--GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2012-06-27 16:07:03

--
-- PostgreSQL database dump complete
--

