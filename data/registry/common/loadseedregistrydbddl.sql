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

-- loadseedregistrydb.sql
--
-- Note: Had to change syntax to separate INSERT statements due to an Oracle SQL-92 limitation.
-- -----------------------------------------------------


--
-- Dumping data for table user_
--

INSERT INTO user_ (id,home,lid,objectType,status,versionName,comment_,personName_firstName,personName_middleName,personName_lastName) VALUES ('urn:freebxml:registry:predefinedusers:registryguest',NULL,'urn:freebxml:registry:predefinedusers:registryguest','urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Person:User','urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted','1.0',NULL,'Registry',NULL,'Guest');
INSERT INTO user_ (id,home,lid,objectType,status,versionName,comment_,personName_firstName,personName_middleName,personName_lastName) VALUES ('urn:freebxml:registry:predefinedusers:registryoperator',NULL,'urn:freebxml:registry:predefinedUser:RegistryOperator','urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Person:User','urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted','1.0',NULL,'Registry',NULL,'Operator');