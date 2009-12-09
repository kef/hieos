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
-- Drop Table Statements
--
DROP TABLE error;
DROP TABLE http;
DROP TABLE other;
DROP TABLE soap;
DROP TABLE logdetail;
DROP TABLE main;
DROP TABLE ip;


-- Table: ip


CREATE TABLE ip
(
  ip character varying(100) NOT NULL,
  company_name character varying(255) NOT NULL DEFAULT 'Unknown'::character varying,
  email character varying,
  CONSTRAINT ip_pkey PRIMARY KEY (ip)
)
WITH (OIDS=FALSE);
ALTER TABLE ip OWNER TO log;

-- Index: "IP_INDEX"

-- DROP INDEX "IP_INDEX";

-- CREATE INDEX "IP_INDEX"
--  ON ip
--  USING btree
--  (ip);


-- Table: main

CREATE TABLE main
(
  messageid character varying(255) NOT NULL,
  is_secure char(1),
  ip character varying(100) NOT NULL,
  timereceived timestamp without time zone NOT NULL DEFAULT '2008-08-30 19:56:01.093'::timestamp without time zone,
  test text NOT NULL,
  pass char(1),
  CONSTRAINT main_pkey PRIMARY KEY (messageid),
  CONSTRAINT main_ip_fkey FOREIGN KEY (ip)
      REFERENCES ip (ip) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (OIDS=FALSE);
ALTER TABLE main OWNER TO log;

-- Index: "MAIN_IP_INDEX"

-- DROP INDEX "MAIN_IP_INDEX";

CREATE INDEX "MAIN_IP_INDEX"
  ON main
  USING btree
  (ip);

-- Index: "MAIN_MID_INDEX"

-- DROP INDEX "MAIN_MID_INDEX";

--CREATE INDEX "MAIN_MID_INDEX"
--  ON main
--  USING btree
--  (messageid);



-- Table: logdetail
--
-- logdetail replaces the following tables - error, soap, http and other.
--

CREATE TABLE logdetail
(
  type character varying(10) NOT NULL,
  messageid character varying(255) NOT NULL,
  "name" character varying(255) NOT NULL,
  "value" text,
  seqid integer NOT NULL DEFAULT 0,
  CONSTRAINT logdetail_messageid_fkey FOREIGN KEY (messageid)
      REFERENCES main (messageid) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (OIDS=FALSE);

ALTER TABLE logdetail OWNER TO log;

-- Index: "LOGDETAIL_MID_INDEX"

-- DROP INDEX "LOGDETAIL_MID_INDEX";

CREATE INDEX "LOGDETAIL_MID_INDEX"
  ON logdetail
  USING btree
  (messageid);

-- Index: "LOGDETAIL_TYPE_INDEX"

-- DROP INDEX "LOGDETAIL_TYPE_INDEX";

CREATE INDEX "LOGDETAIL_TYPE_INDEX"
  ON logdetail
  USING btree
  (type);
