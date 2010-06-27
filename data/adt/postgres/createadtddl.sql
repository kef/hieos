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
-- Postgres DDL for ADT Schema
--


-- Table: patient

DROP TABLE IF EXISTS patient;

CREATE TABLE patient (
  uuid text NOT NULL,
  id text NOT NULL,
  "timestamp" text,
  birthdatetime text,
  adminsex text,
  accountnumber text,
  bedid text,
  status char(1) NOT NULL DEFAULT 'A',
  CONSTRAINT patient_pkey PRIMARY KEY (id)
)
WITH (OIDS=TRUE);
ALTER TABLE patient OWNER TO adt;

--
-- Create index on patient 'uuid' col
--
CREATE INDEX patient_uuid_idx ON patient USING btree (uuid);


-- Table: patientaddress

DROP TABLE IF EXISTS patientaddress;

CREATE TABLE patientaddress (
  parent text NOT NULL,
  streetaddress text NOT NULL,
  otherdesignation text NOT NULL,
  city text NOT NULL,
  stateorprovince text NOT NULL,
  zipcode text NOT NULL,
  country text NOT NULL,
  countyorparish text NOT NULL,
  CONSTRAINT patientaddress_pkey PRIMARY KEY (parent)
)
WITH (OIDS=TRUE);
ALTER TABLE patientaddress OWNER TO adt;

-- Table: patientname

DROP TABLE IF EXISTS patientname;

CREATE TABLE patientname (
  parent text NOT NULL,
  familyname text NOT NULL,
  givenname text NOT NULL,
  secondandfurthername text NOT NULL,
  suffix text NOT NULL,
  prefix text NOT NULL,
  degree text NOT NULL,
  CONSTRAINT patientname_pkey PRIMARY KEY (parent)
)
WITH (OIDS=TRUE);
ALTER TABLE patientname OWNER TO adt;


-- Table: patientrace

DROP TABLE IF EXISTS patientrace;

CREATE TABLE patientrace (
  parent text NOT NULL,
  race text NOT NULL,
  CONSTRAINT patientrace_pkey PRIMARY KEY (parent)
)
WITH (OIDS=TRUE);
ALTER TABLE patientrace OWNER TO adt;



--
-- Definition of table mergehistory
--
DROP TABLE IF EXISTS mergehistory;

CREATE TABLE mergehistory (
  uniqueid character varying(64) NOT NULL,
  survivingpatientid character varying(64) NOT NULL,
  subsumedpatientid character varying(64) NOT NULL,
  action char(1) NOT NULL,
  datetimeperformed timestamp without time zone NOT NULL,
  CONSTRAINT mh_pkey PRIMARY KEY (uniqueid)
)
WITH (OIDS=FALSE);
ALTER TABLE mergehistory OWNER TO adt;

--
-- Create index on mergehistory
--
CREATE INDEX mh_patientids_idx ON mergehistory USING btree (survivingpatientid, subsumedpatientid);
CREATE INDEX mh_action_idx ON mergehistory USING btree (action);

--
-- Definition of table mergedobjects
--
DROP TABLE IF EXISTS mergedobjects;

CREATE TABLE mergedobjects (
  parentid character varying(64) NOT NULL,
  externalidentifierid character varying(64) NOT NULL,
  CONSTRAINT mo_pkey PRIMARY KEY (parentid, externalidentifierid),
  CONSTRAINT mo_mh_fkey FOREIGN KEY (parentid)
      REFERENCES mergehistory (uniqueid) MATCH SIMPLE
      ON DELETE CASCADE
)
WITH (OIDS=FALSE);
ALTER TABLE mergedobjects OWNER TO adt;