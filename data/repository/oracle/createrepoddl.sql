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
-- Oracle DDL for REPO Schema
--


--
-- Definition of table `document`
--

DROP TABLE document;

CREATE TABLE document (
  uniqueid VARCHAR2(256) NOT NULL,
  hash VARCHAR2(256) NOT NULL,
  "SIZE_" number(10) NOT NULL,
  mimetype VARCHAR2(256) NOT NULL,
  repositoryid VARCHAR2(256) NOT NULL,
  bytes BLOB NOT NULL,
  documentid VARCHAR2(256) NOT NULL,
  CONSTRAINT doc_pkey PRIMARY KEY (uniqueid)
);


