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
-- PostgreSQL DDL for ATNA ARR Schema
--


--
-- Drop statements for ATNA ARR Schema
-- Drop child tables first and parent last
--
DROP TABLE IF EXISTS amcodevalue;
DROP TABLE IF EXISTS ascodevalue;
DROP TABLE IF EXISTS apcodevalue;
DROP TABLE IF EXISTS pocodevalue;
DROP TABLE IF EXISTS potypevalue;

DROP TABLE IF EXISTS auditsource;
DROP TABLE IF EXISTS activeparticipant;
DROP TABLE IF EXISTS participantobject;

DROP TABLE IF EXISTS auditmessage;

DROP TABLE IF EXISTS auditlog;


--
-- Definition of table auditmessage and dependent tables
--

CREATE TABLE auditmessage (
  uniqueid CHARACTER VARYING(50) NOT NULL,
  eventactioncode CHAR(1), -- C,R,U,D,E
  eventdatetime TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  eventoutcomeindicator INTEGER NOT NULL, -- 0,4,8,12
  status CHAR(1) DEFAULT 'E' NOT NULL,       -- indicates if the message has an error, incomplete or complete   
  CONSTRAINT auditmessage_pkey PRIMARY KEY (uniqueid)
)
WITH (OIDS=FALSE);

ALTER TABLE auditmessage OWNER TO arr;

-- The following attributes are stored in the AuditMessageCodeValue table
-- eventid       - mandatory, one
-- eventtypecode - optional, zero or more
CREATE TABLE amcodevalue (
  parentid CHARACTER VARYING(50) NOT NULL,
  attributename CHAR(1) NOT NULL,
  seqno INTEGER NOT NULL,
  codevalue CHARACTER VARYING(50) NOT NULL,
  codesystem CHARACTER VARYING(100),
  displayname CHARACTER VARYING(100),
  codesystemname CHARACTER VARYING(100),
  originaltext CHARACTER VARYING(100),
  CONSTRAINT amcodevalue_pkey PRIMARY KEY (parentid, attributename, seqno),
  CONSTRAINT amcodevalue_am_fkey FOREIGN KEY (parentid)
      REFERENCES auditmessage (uniqueid) ON DELETE CASCADE
)
WITH (OIDS=FALSE);

ALTER TABLE amcodevalue OWNER TO arr;



--
-- Definition of table auditsource and dependent tables
--

CREATE TABLE auditsource (
  uniqueid CHARACTER VARYING(50) NOT NULL,
  amid CHARACTER VARYING(50) NOT NULL,
  id CHARACTER VARYING(100) NOT NULL,
  enterprisesiteid CHARACTER VARYING(100),
  CONSTRAINT auditsource_pkey PRIMARY KEY (uniqueid),
  CONSTRAINT auditsource_am_fkey FOREIGN KEY (amid)
      REFERENCES auditmessage (uniqueid) ON DELETE CASCADE
)
WITH (OIDS=FALSE);

ALTER TABLE auditsource OWNER TO arr;

--DROP INDEX "auditsource_am_index";
CREATE INDEX "auditsource_am_index"
  ON auditsource
  USING btree
  (amid);

-- The following attributes are stored in the AuditSourceIdCodeValue table
-- typecode - zero or more, values - 1,2,3,4,5,6,7,8,9
CREATE TABLE ascodevalue (
  parentid CHARACTER VARYING(50) NOT NULL,
  attributename CHAR(1) NOT NULL,
  seqno INTEGER NOT NULL,
  codevalue CHARACTER VARYING(50) NOT NULL,
  codesystem CHARACTER VARYING(100),
  displayname CHARACTER VARYING(100),
  codesystemname CHARACTER VARYING(100),
  originaltext CHARACTER VARYING(100),
  CONSTRAINT ascodevalue_pkey PRIMARY KEY (parentid, attributename, seqno),
  CONSTRAINT ascodevalue_as_fkey FOREIGN KEY (parentid)
      REFERENCES auditsource (uniqueid) ON DELETE CASCADE
)
WITH (OIDS=FALSE);

ALTER TABLE ascodevalue OWNER TO arr;


--
-- Definition of table activeparticipant and dependent tables
--

CREATE TABLE activeparticipant (
  uniqueid CHARACTER VARYING(50) NOT NULL,
  amid CHARACTER VARYING(50) NOT NULL,
  userid CHARACTER VARYING(200) NOT NULL,
  alternativeuserid CHARACTER VARYING(200),
  username CHARACTER VARYING(1024),
  userisrequestor CHAR(1),
  networkaccesspointtypecode INTEGER,  -- 1,2,3
  networkaccesspointid CHARACTER VARYING(100),
  CONSTRAINT activeparticipant_pkey PRIMARY KEY (uniqueid),
  CONSTRAINT activeparticipant_am_fkey FOREIGN KEY (amid)
      REFERENCES auditmessage (uniqueid) ON DELETE CASCADE
)
WITH (OIDS=FALSE);

ALTER TABLE activeparticipant OWNER TO arr;

--DROP INDEX "activeparticipant_am_index";
CREATE INDEX "activeparticipant_am_index"
  ON activeparticipant
  USING btree
  (amid);

-- The following attributes are stored in the ActiveParticipantCodeValue table
--  roleidcode - optional, zero or more
CREATE TABLE apcodevalue (
  parentid CHARACTER VARYING(50) NOT NULL,
  attributename CHAR(1) NOT NULL,
  seqno INTEGER NOT NULL,
  codevalue CHARACTER VARYING(50) NOT NULL,
  codesystem CHARACTER VARYING(100),
  displayname CHARACTER VARYING(100),
  codesystemname CHARACTER VARYING(100),
  originaltext CHARACTER VARYING(100),
  CONSTRAINT apcodevalue_pkey PRIMARY KEY (parentid, attributename, seqno),
  CONSTRAINT apcodevalue_ap_fkey FOREIGN KEY (parentid)
      REFERENCES activeparticipant (uniqueid) ON DELETE CASCADE
)
WITH (OIDS=FALSE);

ALTER TABLE apcodevalue OWNER TO arr;


--
-- Definition of table participantobject and dependent tables
--

CREATE TABLE participantobject (
  uniqueid CHARACTER VARYING(50) NOT NULL,
  amid CHARACTER VARYING(50) NOT NULL,
  typecode INTEGER,
  typecoderole INTEGER,           -- values 1 through 24
  datalifecycle INTEGER,          -- values 1 through 15
  sensitivity CHARACTER VARYING(50),
  id BYTEA NOT NULL,		  -- can contain multiple IDs
  poname CHARACTER VARYING(100),
  query BYTEA,                    -- binary data
  CONSTRAINT participantobject_pkey PRIMARY KEY (uniqueid),
  CONSTRAINT participantobject_am_fkey FOREIGN KEY (amid)
      REFERENCES auditmessage (uniqueid) ON DELETE CASCADE
)
WITH (OIDS=FALSE);

ALTER TABLE participantobject OWNER TO arr;

--DROP INDEX "participantobject_am_index";
CREATE INDEX "participantobject_am_index"
  ON participantobject
  USING btree
  (amid);

-- The following attributes are stored in the ParticipantObjectCodeValue table
--  typecode - optional, one, values - 1,2,3,4
CREATE TABLE pocodevalue (
  parentid CHARACTER VARYING(50) NOT NULL,
  attributename CHAR(1) NOT NULL,
  seqno INTEGER NOT NULL,
  codevalue CHARACTER VARYING(50) NOT NULL,
  codesystem CHARACTER VARYING(100),
  displayname CHARACTER VARYING(100),
  codesystemname CHARACTER VARYING(100),
  originaltext CHARACTER VARYING(100),
  CONSTRAINT pocodevalue_pkey PRIMARY KEY (parentid, attributename, seqno),
  CONSTRAINT pocodevalue_po_fkey FOREIGN KEY (parentid)
      REFERENCES participantobject (uniqueid) ON DELETE CASCADE
)
WITH (OIDS=FALSE);

ALTER TABLE pocodevalue OWNER TO arr;

-- The following attributes are stored in the ObjectParticipantTypeValue table
--  detail - optional, zero or more
--  description - optional, zero or more
CREATE TABLE potypevalue (
  parentid CHARACTER VARYING(50) NOT NULL,
  attributename CHAR(1) NOT NULL,
  seqno INTEGER NOT NULL,
  codetype CHARACTER VARYING(100) NOT NULL,
  codevalue BYTEA NOT NULL,
  CONSTRAINT potypevalue_pkey PRIMARY KEY (parentid, attributename, seqno),
  CONSTRAINT potypevalue_po_fkey FOREIGN KEY (parentid)
      REFERENCES participantobject (uniqueid) ON DELETE CASCADE
)
WITH (OIDS=FALSE);

ALTER TABLE potypevalue OWNER TO arr;


--
-- Definition of table auditlog and dependent tables
-- This table stores the raw XML received from the client
--

CREATE TABLE auditlog (
  uniqueid CHARACTER VARYING(50) NOT NULL,
  clientipaddress CHARACTER VARYING(100),
  clientport CHARACTER VARYING(100),
  receiveddatetime TIMESTAMP(3) NOT NULL,
  xml BYTEA,
  protocol CHARACTER VARYING(10),
  errormessage CHARACTER VARYING(500),
  CONSTRAINT auditlog_pkey PRIMARY KEY (uniqueid)
)
WITH (OIDS=FALSE);

ALTER TABLE auditlog OWNER TO arr;




