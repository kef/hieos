--
--  This code is subject to the HIEOS License, Version 1.0
-- 
--  Copyright(c) 2012 Vangent, Inc.  All rights reserved.
-- 
--  Unless required by applicable law or agreed to in writing, software
--  distributed under the License is distributed on an "AS IS" BASIS,
--  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- 
--  See the License for the specific language governing permissions and
--  limitations under the License.
--

-- Database: omar

-- DROP DATABASE omar;
-- use omar;
-- Additional indexes (on LID).
--CREATE INDEX lid_class_idx ON classification (lid);
CREATE INDEX lid_eo_idx ON extrinsicobject (lid);
CREATE INDEX lid_rp_idx ON registrypackage (lid);

-- Change all "submitted" objects to "approved".
UPDATE association SET status='A' where status='S';
UPDATE extrinsicobject SET status='A' where status='S';
UPDATE registrypackage SET status='A' where status='S';

-- Change all versions from default "1.1" to "1"
UPDATE extrinsicobject SET versionname='1' where versionname='1.1';
UPDATE registrypackage SET versionname='1' where versionname='1.1';
--commit;