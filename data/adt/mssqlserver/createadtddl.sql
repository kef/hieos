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
-- MS SQL Server DDL for ADT Schema
--


--
-- Drop Table Statements 
--
DROP TABLE patient;
DROP TABLE patientaddress;
DROP TABLE patientname;
DROP TABLE patientrace;

DROP TABLE mergedobjects;
DROP TABLE mergehistory; 
DROP TABLE patientcorrelation


--
-- Definition of table `patient`
--

CREATE TABLE patient (
  uuid VARCHAR(64) NOT NULL,
  id VARCHAR(100) NOT NULL,
  timestamp VARCHAR(40),
  status CHAR(1) NOT NULL DEFAULT 'A',
  CONSTRAINT patient_pkey PRIMARY KEY (id)
);

--
-- Create index on patient uuid col
--
CREATE INDEX patient_uuid_idx ON patient (uuid);

--
-- Definition of table mergehistory
--
CREATE TABLE mergehistory (
  uniqueid VARCHAR(64) NOT NULL,
  survivingpatientid VARCHAR(100) NOT NULL,
  subsumedpatientid VARCHAR(100) NOT NULL,
  action CHAR(1) NOT NULL,
  datetimeperformed DATETIME NOT NULL,
  CONSTRAINT mh_pkey PRIMARY KEY (uniqueid)
);

--
-- Create index on mergehistory
--
CREATE INDEX mh_patientids_idx ON mergehistory (survivingpatientid, subsumedpatientid);
CREATE INDEX mh_action_idx ON mergehistory (action);


--
-- Definition of table mergedobjects
--
CREATE TABLE mergedobjects (
  parentid VARCHAR(64) NOT NULL,
  externalidentifierid VARCHAR(64) NOT NULL,
  CONSTRAINT mo_pkey PRIMARY KEY (parentid, externalidentifierid),
  CONSTRAINT mo_mh_fkey FOREIGN KEY (parentid)
      REFERENCES mergehistory (uniqueid)
      ON DELETE CASCADE
);

--
-- Definition of table patientcorrelation
--
CREATE TABLE patientcorrelation (
  id VARCHAR(64) NOT NULL,
  localhome VARCHAR(100) NOT NULL,
  localpatientid VARCHAR(100) NOT NULL,
  remotehome VARCHAR(100) NOT NULL,
  remotepatientid VARCHAR(100),
  status CHAR(1) NOT NULL DEFAULT 'A',
  lastupdatetime DATETIME,
  expirationtime DATETIME
)

ALTER TABLE patientcorrelation
    ADD CONSTRAINT patientcorrelation_pkey PRIMARY KEY (id);
    
CREATE INDEX pc_localpatientid_idx ON patientcorrelation (localpatientid);