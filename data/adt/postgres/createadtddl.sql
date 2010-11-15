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
  uuid character varying(64) NOT NULL,
  id character varying(100) NOT NULL,
  "timestamp" character varying(40),
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


-- Table: patientname

DROP TABLE IF EXISTS patientname;


-- Table: patientrace

DROP TABLE IF EXISTS patientrace;


--
-- Definition of table mergehistory
--
DROP TABLE IF EXISTS mergehistory CASCADE;

CREATE TABLE mergehistory (
  uniqueid character varying(64) NOT NULL,
  survivingpatientid character varying(100) NOT NULL,
  subsumedpatientid character varying(100) NOT NULL,
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