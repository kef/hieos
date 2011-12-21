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
-- MS SQLServer DDL for ATNA ARR Schema
--


--
-- Drop statements for ATNA ARR Schema
-- Drop child tables first and parent last
--

DROP TABLE amcodevalue;
DROP TABLE ascodevalue;
DROP TABLE apcodevalue;
DROP TABLE pocodevalue;
DROP TABLE potypevalue;

DROP TABLE auditsource;
DROP TABLE activeparticipant;
DROP TABLE participantobject;

DROP TABLE auditmessage;

DROP TABLE auditlog;



--
-- Definition of table auditmessage and dependent tables
--

CREATE TABLE auditmessage (
  uniqueid VARCHAR(50) NOT NULL,
  eventactioncode CHAR(1), -- C,R,U,D,E
  eventdatetime DATETIME NOT NULL,
  eventoutcomeindicator INT NOT NULL, -- 0,4,8,12
  status CHAR(1) DEFAULT 'E' NOT NULL,       -- indicates if the message has an error, incomplete or complete   
  CONSTRAINT auditmessage_pkey PRIMARY KEY (uniqueid)
);

-- The following attributes are stored in the AuditMessageCodeValue table
-- eventid       - mandatory, one
-- eventtypecode - optional, zero or more
CREATE TABLE amcodevalue (
  parentid VARCHAR(50) NOT NULL,
  attributename CHAR(1) NOT NULL,
  seqno INT NOT NULL,
  codevalue VARCHAR(50) NOT NULL,
  codesystem VARCHAR(100),
  displayname VARCHAR(100),
  codesystemname VARCHAR(100),
  originaltext VARCHAR(100),
  CONSTRAINT amcodevalue_pkey PRIMARY KEY (parentid, attributename, seqno),
  CONSTRAINT amcodevalue_am_fkey FOREIGN KEY (parentid)
      REFERENCES auditmessage (uniqueid) ON DELETE CASCADE
);


--
-- Definition of table auditsource and dependent tables
--

CREATE TABLE auditsource (
  uniqueid VARCHAR(50) NOT NULL,
  amid VARCHAR(50) NOT NULL,
  id VARCHAR(100) NOT NULL,
  enterprisesiteid VARCHAR(100),
  CONSTRAINT auditsource_pkey PRIMARY KEY (uniqueid),
  CONSTRAINT auditsource_am_fkey FOREIGN KEY (amid)
      REFERENCES auditmessage (uniqueid) ON DELETE CASCADE
);

--DROP INDEX "auditsource_am_index";
CREATE INDEX "auditsource_am_index"
  ON auditsource (amid);


-- The following attributes are stored in the AuditSourceIdCodeValue table
-- typecode - zero or more, values - 1,2,3,4,5,6,7,8,9
CREATE TABLE ascodevalue (
  parentid VARCHAR(50) NOT NULL,
  attributename CHAR(1) NOT NULL,
  seqno INT NOT NULL,
  codevalue VARCHAR(50) NOT NULL,
  codesystem VARCHAR(100),
  displayname VARCHAR(100),
  codesystemname VARCHAR(100),
  originaltext VARCHAR(100),
  CONSTRAINT ascodevalue_pkey PRIMARY KEY (parentid, attributename, seqno),
  CONSTRAINT ascodevalue_as_fkey FOREIGN KEY (parentid)
      REFERENCES auditsource (uniqueid) ON DELETE CASCADE
);


--
-- Definition of table activeparticipant and dependent tables
--

CREATE TABLE activeparticipant (
  uniqueid VARCHAR(50) NOT NULL,
  amid VARCHAR(50) NOT NULL,
  userid VARCHAR(200) NOT NULL,
  alternativeuserid VARCHAR(200),
  username VARCHAR(1024),
  userisrequestor CHAR(1),
  networkaccesspointtypecode INT,  -- 1,2,3
  networkaccesspointid VARCHAR(100),
  CONSTRAINT activeparticipant_pkey PRIMARY KEY (uniqueid),
  CONSTRAINT activeparticipant_am_fkey FOREIGN KEY (amid)
      REFERENCES auditmessage (uniqueid) ON DELETE CASCADE
);

--DROP INDEX "activeparticipant_am_index";
CREATE INDEX "activeparticipant_am_index"
  ON activeparticipant (amid);


-- The following attributes are stored in the ActiveParticipantCodeValue table
--  roleidcode - optional, zero or more
CREATE TABLE apcodevalue (
  parentid VARCHAR(50) NOT NULL,
  attributename CHAR(1) NOT NULL,
  seqno INT NOT NULL,
  codevalue VARCHAR(50) NOT NULL,
  codesystem VARCHAR(100),
  displayname VARCHAR(100),
  codesystemname VARCHAR(100),
  originaltext VARCHAR(100),
  CONSTRAINT apcodevalue_pkey PRIMARY KEY (parentid, attributename, seqno),
  CONSTRAINT apcodevalue_ap_fkey FOREIGN KEY (parentid)
      REFERENCES activeparticipant (uniqueid) ON DELETE CASCADE
);


--
-- Definition of table participantobject and dependent tables
--

CREATE TABLE participantobject (
  uniqueid VARCHAR(50) NOT NULL,
  amid VARCHAR(50) NOT NULL,
  typecode INT,
  typecoderole INT,           -- values 1 through 24
  datalifecycle INT,          -- values 1 through 15
  sensitivity VARCHAR(50),
  id image NOT NULL,		-- can contain multiple IDs
  poname VARCHAR(100),
  query image,                   -- binary data
  CONSTRAINT participantobject_pkey PRIMARY KEY (uniqueid),
  CONSTRAINT participantobject_am_fkey FOREIGN KEY (amid)
      REFERENCES auditmessage (uniqueid) ON DELETE CASCADE
);

--DROP INDEX "participantobject_am_index";
CREATE INDEX "participantobject_am_index"
  ON participantobject (amid);

-- The following attributes are stored in the ParticipantObjectCodeValue table
--  typecode - optional, one, values - 1,2,3,4
CREATE TABLE pocodevalue (
  parentid VARCHAR(50) NOT NULL,
  attributename CHAR(1) NOT NULL,
  seqno INT NOT NULL,
  codevalue VARCHAR(50) NOT NULL,
  codesystem VARCHAR(100),
  displayname VARCHAR(100),
  codesystemname VARCHAR(100),
  originaltext VARCHAR(100),
  CONSTRAINT pocodevalue_pkey PRIMARY KEY (parentid, attributename, seqno),
  CONSTRAINT pocodevalue_po_fkey FOREIGN KEY (parentid)
      REFERENCES participantobject (uniqueid) ON DELETE CASCADE
);

-- The following attributes are stored in the ObjectParticipantTypeValue table
--  detail - optional, zero or more
--  description - optional, zero or more
CREATE TABLE potypevalue (
  parentid VARCHAR(50) NOT NULL,
  seqno INT NOT NULL,
  codetype VARCHAR(100) NOT NULL,
  codevalue image NOT NULL,
  CONSTRAINT potypevalue_pkey PRIMARY KEY (parentid, seqno),
  CONSTRAINT potypevalue_po_fkey FOREIGN KEY (parentid)
      REFERENCES participantobject (uniqueid) ON DELETE CASCADE
);


--
-- Definition of table auditlog and dependent tables
-- This table stores the raw XML received from the client
--

CREATE TABLE auditlog (
  uniqueid VARCHAR(50) NOT NULL,
  clientipaddress VARCHAR(100),
  clientport VARCHAR(100),
  receiveddatetime DATETIME NOT NULL,
  xml image,
  protocol VARCHAR(10),
  errormessage VARCHAR(500),
  CONSTRAINT auditlog_pkey PRIMARY KEY (uniqueid)
);
