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
-- MS SQLServer DDL for REPO database
--

--
-- Definition of table document
--

DROP TABLE document;

CREATE TABLE document (
  uniqueid varchar(256) NOT NULL,
  hash varchar(256) NOT NULL,
  size_ BIGINT NOT NULL,
  mimetype varchar(256) NOT NULL,
  repositoryid varchar(256) NOT NULL,
  bytes image NOT NULL,
  documentid varchar(256) NOT NULL,
  CONSTRAINT doc_pkey PRIMARY KEY (uniqueid)
);

