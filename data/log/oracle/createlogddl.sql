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
-- Oracle DDL for LOG Schema
--

--
-- Drop Tables and Indices in the proper order
--
DROP TABLE logdetail;
DROP TABLE main;
DROP TABLE ip ;



-- Table: ip

CREATE TABLE ip (
  ip VARCHAR2(100) NOT NULL,
  company_name VARCHAR2(255) DEFAULT 'Unknown' NOT NULL,
  email VARCHAR2(255),
  CONSTRAINT ip_pkey PRIMARY KEY (ip)
);



-- Table: main

CREATE TABLE main (
  messageid VARCHAR2(255) NOT NULL,
  is_secure CHAR(1),
  ip VARCHAR2(100) NOT NULL,
  timereceived TIMESTAMP(3) DEFAULT '2008-08-30 19:56:01.093',
  test VARCHAR2(100) NOT NULL,		
  pass CHAR(1),
  CONSTRAINT main_pkey PRIMARY KEY (messageid),
  CONSTRAINT main_ip_fkey FOREIGN KEY (ip)
      REFERENCES ip (ip)
);

-- Index: "MAIN_IP_INDEX"

--DROP INDEX "MAIN_IP_INDEX";

CREATE INDEX "MAIN_IP_INDEX"
  ON main (ip);



--
-- Table: logdetail
--
-- logdetail replaces the following tables - error, soap, http and other.
--

CREATE TABLE logdetail (
  type VARCHAR2(10) NOT NULL,
  messageid VARCHAR2(255) NOT NULL,
  name VARCHAR2(255) NOT NULL,
  value CLOB,
  seqid NUMBER(11) DEFAULT 0  NOT NULL ,
  CONSTRAINT logdetail_messageid_fkey FOREIGN KEY (messageid)
      REFERENCES main (messageid)
      ON DELETE CASCADE
);

-- Index: "LOGDETAIL_MID_INDEX"

--DROP INDEX "LOGDETAIL_MID_INDEX";

CREATE INDEX "LOGDETAIL_MID_INDEX"
  ON logdetail (messageid);
  
-- Index: "LOGDETAIL_TYPE_INDEX"

--DROP INDEX "LOGDETAIL_TYPE_INDEX";

CREATE INDEX "LOGDETAIL_TYPE_INDEX"
  ON logdetail (type);

