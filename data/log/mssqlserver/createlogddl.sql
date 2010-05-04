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
-- MS SQL Server DDL for LOG Schema
--

--
-- Drop Table Statements 
--

DROP TABLE logdetail;
DROP TABLE main;
DROP TABLE ip;


-- Table: ip


CREATE TABLE ip (
  ip VARCHAR(100) NOT NULL,
  company_name VARCHAR(255) NOT NULL DEFAULT 'Unknown',
  email VARCHAR(255),
  CONSTRAINT ip_pkey PRIMARY KEY (ip)
);



-- Table: main

CREATE TABLE main (
  messageid VARCHAR(255) NOT NULL,
  is_secure CHAR(1),
  ip VARCHAR(100) NOT NULL,
  timereceived DATETIME NOT NULL DEFAULT '08/30/2008 19:56:01:093',
  test VARCHAR(100) NOT NULL,
  pass CHAR(1),
  CONSTRAINT main_pkey PRIMARY KEY (messageid),
  CONSTRAINT main_ip_fkey FOREIGN KEY (ip)
      REFERENCES ip (ip) 
      ON UPDATE NO ACTION ON DELETE CASCADE
);

-- Index: MAIN_IP_INDEX

-- DROP INDEX MAIN_IP_INDEX;

CREATE INDEX MAIN_IP_INDEX
  ON main
  (ip);



-- Table: logdetail
--
-- logdetail replaces the following tables - error, soap, http and other.
--

CREATE TABLE logdetail (
  type VARCHAR(10) NOT NULL,
  messageid VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL,
  value TEXT,
  seqid INT NOT NULL DEFAULT 0,
  CONSTRAINT logdetail_messageid_fkey FOREIGN KEY (messageid)
      REFERENCES main (messageid) 
      ON UPDATE CASCADE ON DELETE CASCADE
);


-- Index: LOGDETAIL_MID_INDEX

-- DROP INDEX LOGDETAIL_MID_INDEX;

CREATE INDEX LOGDETAIL_MID_INDEX
  ON logdetail
  (messageid);

-- Index: LOGDETAIL_TYPE_INDEX

-- DROP INDEX LOGDETAIL_TYPE_INDEX;

CREATE INDEX LOGDETAIL_TYPE_INDEX
  ON logdetail
  (type);
