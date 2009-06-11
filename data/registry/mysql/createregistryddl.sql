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

-- MySQL Administrator dump 1.4
--
-- ------------------------------------------------------
-- Server version	5.1.33-community


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


--
-- Create schema omar
--

CREATE DATABASE IF NOT EXISTS omar;
USE omar;

--
-- Temporary table structure for view `identifiable`
--
DROP TABLE IF EXISTS `identifiable`;
DROP VIEW IF EXISTS `identifiable`;
CREATE TABLE `identifiable` (
  `id` varchar(256),
  `home` varchar(256)
);

--
-- Definition of table `registryobject`
--
DROP TABLE IF EXISTS `registryobject`;
DROP VIEW IF EXISTS `registryobject`;
CREATE TABLE `registryobject` (
  `id` varchar(256) NOT NULL,
  `home` varchar(256) DEFAULT NULL,
  `lid` varchar(256) NOT NULL,
  `objectType` varchar(256) NOT NULL,
  `status` varchar(256) NOT NULL,
  `versionName` varchar(16) DEFAULT NULL,
  `comment_` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`, `objectType`),
  KEY `lid_RO_idx` (`lid`),
  KEY `id_RO_idx` (`id`),
  KEY `home_RO_idx` (`home`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


--
-- Definition of table `adhocquery`
--

DROP TABLE IF EXISTS `adhocquery`;
CREATE TABLE `adhocquery` (
  `id` varchar(256) NOT NULL,
  `home` varchar(256) DEFAULT NULL,
  `lid` varchar(256) NOT NULL,
  `objectType` varchar(256) DEFAULT NULL,
  `status` varchar(256) NOT NULL,
  `versionName` varchar(16) DEFAULT NULL,
  `comment_` varchar(256) DEFAULT NULL,
  `queryLanguage` varchar(256) NOT NULL,
  `query` varchar(4096) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `lid_AdhQuery_idx` (`lid`),
  KEY `id_AdhQuery_idx` (`id`),
  KEY `home_AdhQuery_idx` (`home`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `adhocquery`
--

/*!40000 ALTER TABLE `adhocquery` DISABLE KEYS */;
/*
INSERT INTO adhocquery (id,home,lid,objectType,status,versionName,comment_,queryLanguage,query) VALUES
 ('urn:freebxml:registry:demoDB:query:EpidemicAlertQuery',NULL,'urn:freebxml:registry:demoDB:query:EpidemicAlertQuery','urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AdhocQuery','urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted','1.1',NULL,'urn:oasis:names:tc:ebxml-regrep:QueryLanguage:SQL-92','\n      SELECT * FROM ExtrinsicObject eo WHERE eo.objectType = \'urn:freebxml:registry:demoDB:ObjectType:EpidemicAlert\'\n      '),
 ('urn:freebxml:registry:query:BusinessQuery',NULL,'urn:freebxml:registry:query:BusinessQuery','urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AdhocQuery','urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted','1.1',NULL,'urn:oasis:names:tc:ebxml-regrep:QueryLanguage:SQL-92','\nSELECT DISTINCT ro.* from RegistryObject ro, Name_ nm, Description d\nWHERE (1=1)\n AND (objecttype IN ( \n SELECT id FROM ClassificationNode WHERE path LIKE \'$objectTypePath\' OR path LIKE $objectTypePath || \'/%\' )\n )\n AND (nm.parent = ro.id AND UPPER ( nm.value ) LIKE UPPER ( \'$name\' ) )\n AND (d.parent = ro.id AND UPPER ( d.value ) LIKE UPPER ( \'$description\' ) ) \n AND (ro.status IN (  SELECT id\n FROM ClassificationNode WHERE path LIKE \'$status\' ) )\n AND (ro.id IN ( SELECT classifiedObject FROM Classification WHERE classificationNode IN (  SELECT id\n FROM ClassificationNode WHERE path LIKE $classificationPath1 OR\n path LIKE $classificationPath1 || \'/%\' ) ))\n AND (ro.id IN ( SELECT classifiedObject FROM Classification WHERE classificationNode IN (  SELECT id\n FROM ClassificationNode WHERE path LIKE $classificationPath2 OR\n path LIKE $classificationPath2 || \'/%\' ) ))\n AND (ro.id IN ( SELECT classifiedObject FROM Classification WHERE classificationNode IN (  SELECT id\n FROM ClassificationNode WHERE path LIKE $classificationPath3 OR\n path LIKE $classificationPath3 || \'/%\' ) ))\n AND (ro.id IN ( SELECT classifiedObject FROM Classification WHERE classificationNode IN (  SELECT id\n FROM ClassificationNode WHERE path LIKE $classificationPath4 OR\n path LIKE $classificationPath4 || \'/%\' ) )) \n ORDER BY ro.objecttype\n        '),
 ('urn:freebxml:registry:query:BusinessQueryCaseSensitive',NULL,'urn:freebxml:registry:query:BusinessQueryCaseSensitive','urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AdhocQuery','urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted','1.1',NULL,'urn:oasis:names:tc:ebxml-regrep:QueryLanguage:SQL-92','\nSELECT DISTINCT ro.* from RegistryObject ro, Name_ nm, Description d\nWHERE (1=1)\n AND (objecttype IN ( \n SELECT id FROM ClassificationNode WHERE path LIKE \'$objectTypePath\' OR path LIKE $objectTypePath || \'/%\' )\n )\n AND (nm.parent = ro.id AND nm.value LIKE \'$name\' )\n AND (d.parent = ro.id AND d.value LIKE \'$description\' )\n AND (ro.status IN (  SELECT id\n FROM ClassificationNode WHERE path LIKE \'$status\' ) )\n AND (ro.id IN ( SELECT classifiedObject FROM Classification WHERE classificationNode IN (  SELECT id\n FROM ClassificationNode WHERE path LIKE $classificationPath1 OR\n path LIKE $classificationPath1 || \'/%\' ) ))\n AND (ro.id IN ( SELECT classifiedObject FROM Classification WHERE classificationNode IN (  SELECT id\n FROM ClassificationNode WHERE path LIKE $classificationPath2 OR\n path LIKE $classificationPath2 || \'/%\' ) ))\n AND (ro.id IN ( SELECT classifiedObject FROM Classification WHERE classificationNode IN (  SELECT id\n FROM ClassificationNode WHERE path LIKE $classificationPath3 OR\n path LIKE $classificationPath3 || \'/%\' ) ))\n AND (ro.id IN ( SELECT classifiedObject FROM Classification WHERE classificationNode IN (  SELECT id\n FROM ClassificationNode WHERE path LIKE $classificationPath4 OR\n path LIKE $classificationPath4 || \'/%\' ) )) \n ORDER BY ro.objecttype\n        '),
 ('urn:oasis:names:tc:ebxml-regrep:profile:ws:query:BindingDiscoveryQuery',NULL,'urn:oasis:names:tc:ebxml-regrep:profile:ws:query:BindingDiscoveryQuery','urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AdhocQuery','urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted','1.1',NULL,'urn:oasis:names:tc:ebxml-regrep:QueryLanguage:SQL-92','\nSELECT DISTINCT binding.* FROM \n  ExtrinsicObject binding, Name_ bindingName, Description bindingDesc, Slot bindingTNS,\n  Association implements\nWHERE\n binding.objectType = \'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:WSDL:Binding\'\n AND (bindingName.parent = binding.id AND UPPER ( bindingName.value ) LIKE UPPER ( \'$binding.name\' ) ) \n AND (bindingDesc.parent = binding.id AND UPPER ( bindingDesc.value ) LIKE UPPER ( \'$binding.description\' ) ) \n AND (binding.status IN (  SELECT id\n FROM ClassificationNode WHERE path LIKE \'$binding.status\' ) )\n AND (binding.id = bindingTNS.parent \n  AND bindingTNS.name_ = \'urn:oasis:names:tc:ebxml-regrep:profile:ws:wsdl:targetNamespace\'\n  AND bindingTNS.value LIKE \'$binding.targetNamespace\')  \n AND (binding.id IN ( SELECT classifiedObject FROM Classification WHERE classificationNode IN (  SELECT id\n FROM ClassificationNode WHERE path LIKE \'$binding.protocolType\' ) ))        \n AND (binding.id IN ( SELECT classifiedObject FROM Classification WHERE classificationNode IN (  SELECT id\n FROM ClassificationNode WHERE path LIKE \'$binding.transportType\' OR path LIKE \'$binding.transportType\' || \'/%\' ) )) \n AND (binding.id IN ( SELECT classifiedObject FROM Classification WHERE classificationNode IN (  SELECT id\n FROM ClassificationNode WHERE path LIKE \'$binding.soapStyleType\' OR path LIKE \'$binding.soapStyleType\' || \'/%\' ) ))\n\n AND ($considerPortType = 0 OR (\n   implements.sourceObject=binding.id AND implements.associationType=\'urn:oasis:names:tc:ebxml-regrep:AssociationType:Implements\' AND implements.targetObject IN\n   (\n    SELECT DISTINCT portType.id from ExtrinsicObject portType, Name_ portTypeName, Description portTypeDesc, Slot portTypeTNS\n    WHERE\n     portType.objectType = \'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:WSDL:PortType\'\n     AND (portTypeName.parent = portType.id AND UPPER ( portTypeName.value ) LIKE UPPER ( \'$portType.name\' ) ) \n     AND (portTypeDesc.parent = portType.id AND UPPER ( portTypeDesc.value ) LIKE UPPER ( \'$portType.description\' ) ) \n     AND (portType.status IN (  SELECT id\n     FROM ClassificationNode WHERE path LIKE \'$portType.status\' ) )\n     AND (portType.id = portTypeTNS.parent \n      AND portTypeTNS.name_ = \'urn:oasis:names:tc:ebxml-regrep:profile:ws:wsdl:targetNamespace\'\n      AND portTypeTNS.value LIKE \'$portType.targetNamespace\')  \n   ))\n )\n        '),
 ('urn:oasis:names:tc:ebxml-regrep:profile:ws:query:GetPortsImplementingPortType',NULL,'urn:oasis:names:tc:ebxml-regrep:profile:ws:query:GetPortsImplementingPortType','urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AdhocQuery','urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted','1.1',NULL,'urn:oasis:names:tc:ebxml-regrep:QueryLanguage:SQL-92','\nSELECT DISTINCT port.* FROM \n  ServiceBinding port, ExtrinsicObject binding, ExtrinsicObject portType,\n  Association portImplementsBinding, Association bindingImplementsPortType\nWHERE\n portType.id = \'$portType.id\' AND\n (bindingImplementsPortType.sourceObject = binding.id AND \n  bindingImplementsPortType.targetObject = portType.id AND\n  bindingImplementsPortType.associationType = \'urn:oasis:names:tc:ebxml-regrep:AssociationType:Implements\') AND\n (portImplementsBinding.sourceObject = port.id AND \n  portImplementsBinding.targetObject = binding.id AND\n  portImplementsBinding.associationType = \'urn:oasis:names:tc:ebxml-regrep:AssociationType:Implements\') \n        '),
 ('urn:oasis:names:tc:ebxml-regrep:profile:ws:query:PortDiscoveryQuery',NULL,'urn:oasis:names:tc:ebxml-regrep:profile:ws:query:PortDiscoveryQuery','urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AdhocQuery','urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted','1.1',NULL,'urn:oasis:names:tc:ebxml-regrep:QueryLanguage:SQL-92','\nSELECT DISTINCT port.* FROM \n  ServiceBinding port, Name_ portName, Description portDesc, Slot portTNS,\n  Association implements\nWHERE\n (port.id IN ( SELECT classifiedObject FROM Classification WHERE classificationNode = \'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:WSDL:Port\' ) )        \n \n AND (portName.parent = port.id AND UPPER ( portName.value ) LIKE UPPER ( \'$port.name\' ) ) \n AND (portDesc.parent = port.id AND UPPER ( portDesc.value ) LIKE UPPER ( \'$port.description\' ) ) \n AND (port.status IN (  SELECT id\n FROM ClassificationNode WHERE path LIKE \'$port.status\' ) )\n AND (port.id = portTNS.parent \n  AND portTNS.name_ = \'urn:oasis:names:tc:ebxml-regrep:profile:ws:wsdl:targetNamespace\'\n  AND portTNS.value LIKE \'$port.targetNamespace\')\n AND (port.accessURI LIKE \'$port.accessURI\')\n\n AND ($considerBinding = 0 OR (\n   implements.sourceObject=port.id AND implements.associationType=\'urn:oasis:names:tc:ebxml-regrep:AssociationType:Implements\' AND implements.targetObject IN\n   (   \n    SELECT DISTINCT binding.id FROM \n      ExtrinsicObject binding, Name_ bindingName, Description bindingDesc, Slot bindingTNS,\n      Association implements\n    WHERE\n     binding.objectType = \'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:WSDL:Binding\'\n     AND (bindingName.parent = binding.id AND UPPER ( bindingName.value ) LIKE UPPER ( \'$binding.name\' ) ) \n     AND (bindingDesc.parent = binding.id AND UPPER ( bindingDesc.value ) LIKE UPPER ( \'$binding.description\' ) ) \n     AND (binding.status IN (  SELECT id\n     FROM ClassificationNode WHERE path LIKE \'$binding.status\' ) )\n     AND (binding.id = bindingTNS.parent \n      AND bindingTNS.name_ = \'urn:oasis:names:tc:ebxml-regrep:profile:ws:wsdl:targetNamespace\'\n      AND bindingTNS.value LIKE \'$binding.targetNamespace\')  \n     AND (binding.id IN ( SELECT classifiedObject FROM Classification WHERE classificationNode IN (  SELECT id\n     FROM ClassificationNode WHERE path LIKE \'$binding.protocolType\' ) ))        \n     AND (binding.id IN ( SELECT classifiedObject FROM Classification WHERE classificationNode IN (  SELECT id\n     FROM ClassificationNode WHERE path LIKE \'$binding.transportType\' OR path LIKE \'$binding.transportType\' || \'/%\') )) \n     AND (binding.id IN ( SELECT classifiedObject FROM Classification WHERE classificationNode IN (  SELECT id\n     FROM ClassificationNode WHERE path LIKE $binding.soapStyleType OR path LIKE $binding.soapStyleType || \'/%\') ))\n\n     AND ($considerPortType = 0 OR (\n       implements.sourceObject=binding.id AND implements.associationType=\'urn:oasis:names:tc:ebxml-regrep:AssociationType:Implements\' AND implements.targetObject IN\n       (\n        SELECT DISTINCT portType.id from ExtrinsicObject portType, Name_ portTypeName, Description portTypeDesc, Slot portTypeTNS\n        WHERE\n         portType.objectType = \'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:WSDL:PortType\'\n         AND (portTypeName.parent = portType.id AND UPPER ( portTypeName.value ) LIKE UPPER ( \'$portType.name\' ) ) \n         AND (portTypeDesc.parent = portType.id AND UPPER ( portTypeDesc.value ) LIKE UPPER ( \'$portType.description\' ) ) \n         AND (portType.status IN (  SELECT id\n         FROM ClassificationNode WHERE path LIKE \'$portType.status\' ) )\n         AND (portType.id = portTypeTNS.parent \n          AND portTypeTNS.name_ = \'urn:oasis:names:tc:ebxml-regrep:profile:ws:wsdl:targetNamespace\'\n          AND portTypeTNS.value LIKE \'$portType.targetNamespace\')  \n       )\n     ))      \n   ))\n )\n        '),
 ('urn:oasis:names:tc:ebxml-regrep:profile:ws:query:PortTypeDiscoveryQuery',NULL,'urn:oasis:names:tc:ebxml-regrep:profile:ws:query:PortTypeDiscoveryQuery','urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AdhocQuery','urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted','1.1',NULL,'urn:oasis:names:tc:ebxml-regrep:QueryLanguage:SQL-92','\nSELECT DISTINCT portType.* FROM ExtrinsicObject portType, Name_ portTypeName, Description portTypeDesc, Slot portTypeTNS\nWHERE\n portType.objectType = \'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:WSDL:PortType\'\n AND (portTypeName.parent = portType.id AND UPPER ( portTypeName.value ) LIKE UPPER ( \'$portType.name\' ) ) \n AND (portTypeDesc.parent = portType.id AND UPPER ( portTypeDesc.value ) LIKE UPPER ( \'$portType.description\' ) ) \n AND (portType.status IN (  SELECT id\n FROM ClassificationNode WHERE path LIKE \'$portType.status\' ) )\n AND (portType.id = portTypeTNS.parent \n  AND portTypeTNS.name_ = \'urn:oasis:names:tc:ebxml-regrep:profile:ws:wsdl:targetNamespace\'\n  AND portTypeTNS.value LIKE \'$portType.targetNamespace\')  \n        '),
 ('urn:oasis:names:tc:ebxml-regrep:profile:ws:query:ServiceDiscoveryQuery',NULL,'urn:oasis:names:tc:ebxml-regrep:profile:ws:query:ServiceDiscoveryQuery','urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AdhocQuery','urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted','1.1',NULL,'urn:oasis:names:tc:ebxml-regrep:QueryLanguage:SQL-92','urn:freebxml:registry:spillOverId:adhocQuery:query:urn:oasis:names:tc:ebxml-regrep:profile:ws:query:ServiceDiscoveryQuery'),
 ('urn:oasis:names:tc:ebxml-regrep:profile:ws:query:WSDLDiscoveryQuery',NULL,'urn:oasis:names:tc:ebxml-regrep:profile:ws:query:WSDLDiscoveryQuery','urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AdhocQuery','urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted','1.1',NULL,'urn:oasis:names:tc:ebxml-regrep:QueryLanguage:SQL-92','\nSELECT DISTINCT ro.* FROM RegistryObject ro, Name_ nm, Description d, Slot tns, Slot ins\nWHERE (1=1)\n AND (ro.objectType = \'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExtrinsicObject:WSDL\')\n AND (nm.parent = ro.id AND UPPER ( nm.value ) LIKE UPPER ( \'$name\' ) ) \n AND (d.parent = ro.id AND UPPER ( d.value ) LIKE UPPER ( \'$description\' ) )  \n AND (ro.status IN (  SELECT id\n FROM ClassificationNode WHERE path LIKE \'$status\' ) )\n AND (ro.id = tns.parent \n  AND tns.name_ = \'urn:oasis:names:tc:ebxml-regrep:profile:ws:wsdl:targetNamespace\'\n  AND tns.value LIKE \'$targetNamespace\')\n AND (ro.id = ins.parent \n  AND ins.name_ = \'urn:oasis:names:tc:ebxml-regrep:profile:ws:wsdl:importedNamespaces\'\n  AND ins.value LIKE \'$importedNamespaces\')\n \n        '),
 ('urn:oasis:names:tc:ebxml-regrep:query:ArbitraryQuery',NULL,'urn:oasis:names:tc:ebxml-regrep:query:ArbitraryQuery','urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AdhocQuery','urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted','1.1',NULL,'urn:oasis:names:tc:ebxml-regrep:QueryLanguage:SQL-92','\n$query;\n        '),
 ('urn:oasis:names:tc:ebxml-regrep:query:ExtrinsicObjectVersionQuery',NULL,'urn:oasis:names:tc:ebxml-regrep:query:ExtrinsicObjectVersionQuery','urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AdhocQuery','urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted','1.1',NULL,'urn:oasis:names:tc:ebxml-regrep:QueryLanguage:SQL-92','\nSELECT  * FROM ExtrinsicObject WHERE lid LIKE \'$lid\' AND versionName LIKE \'$versionName\' AND contentVersionName LIKE \'$contentVersionName\' ORDER BY versionName, contentVersionName;\n        '),
 ('urn:oasis:names:tc:ebxml-regrep:query:FindAllMyObjects',NULL,'urn:oasis:names:tc:ebxml-regrep:query:FindAllMyObjects','urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AdhocQuery','urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted','1.1',NULL,'urn:oasis:names:tc:ebxml-regrep:QueryLanguage:SQL-92','\nSELECT DISTINCT ro.* from RegistryObject ro, AffectedObject ao, AuditableEvent ae WHERE ae.user_ = $currentUser AND ao.id = ro.id AND ao.eventId = ae.id     \n      '),
 ('urn:oasis:names:tc:ebxml-regrep:query:FindObjectByIdAndType',NULL,'urn:oasis:names:tc:ebxml-regrep:query:FindObjectByIdAndType','urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AdhocQuery','urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted','1.1',NULL,'urn:oasis:names:tc:ebxml-regrep:QueryLanguage:SQL-92','\nSELECT ro.* from $tableName ro WHERE ro.id = \'$id\' \n      '),
 ('urn:oasis:names:tc:ebxml-regrep:query:GetAuditTrailForRegistryObject',NULL,'urn:oasis:names:tc:ebxml-regrep:query:GetAuditTrailForRegistryObject','urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AdhocQuery','urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted','1.1',NULL,'urn:oasis:names:tc:ebxml-regrep:QueryLanguage:SQL-92','\nSELECT ae.* FROM AuditableEvent ae, AffectedObject ao, RegistryObject ro WHERE ro.lid=\'$lid\' AND ro.id = ao.id AND ao.eventId = ae.id ORDER BY ae.timeStamp_ ASC\n      '),
 ('urn:oasis:names:tc:ebxml-regrep:query:GetCallersUser',NULL,'urn:oasis:names:tc:ebxml-regrep:query:GetCallersUser','urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AdhocQuery','urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted','1.1',NULL,'urn:oasis:names:tc:ebxml-regrep:QueryLanguage:SQL-92','\nSELECT u.* FROM User_ u WHERE u.id = $currentUser;\n        '),
 ('urn:oasis:names:tc:ebxml-regrep:query:GetClassificationNodesByParentId',NULL,'urn:oasis:names:tc:ebxml-regrep:query:GetClassificationNodesByParentId','urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AdhocQuery','urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted','1.1',NULL,'urn:oasis:names:tc:ebxml-regrep:QueryLanguage:SQL-92','\nSELECT n.* from ClassificationNode n WHERE n.parent = \'$parentId\'\n      '),
 ('urn:oasis:names:tc:ebxml-regrep:query:GetClassificationSchemesById',NULL,'urn:oasis:names:tc:ebxml-regrep:query:GetClassificationSchemesById','urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AdhocQuery','urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted','1.1',NULL,'urn:oasis:names:tc:ebxml-regrep:QueryLanguage:SQL-92','\nSELECT c.* from ClassScheme c WHERE c.id = \'$id\' \n      '),
 ('urn:oasis:names:tc:ebxml-regrep:query:GetMembersByRegistryPackageId',NULL,'urn:oasis:names:tc:ebxml-regrep:query:GetMembersByRegistryPackageId','urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AdhocQuery','urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted','1.1',NULL,'urn:oasis:names:tc:ebxml-regrep:QueryLanguage:SQL-92','\nSELECT ro.* FROM RegistryObject ro, Association ass WHERE ass.targetObject = ro.id AND ass.associationType=\'urn:oasis:names:tc:ebxml-regrep:AssociationType:HasMember\' AND ( ass.sourceObject = \'$packageId\' )\n      '),
 ('urn:oasis:names:tc:ebxml-regrep:query:GetRegistryPackagesByMemberId',NULL,'urn:oasis:names:tc:ebxml-regrep:query:GetRegistryPackagesByMemberId','urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AdhocQuery','urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted','1.1',NULL,'urn:oasis:names:tc:ebxml-regrep:QueryLanguage:SQL-92','\nSELECT rp.* FROM RegistryPackage rp, RegistryObject ro, Association ass WHERE ass.targetObject = \'$memberId\' AND ass.associationType=\'urn:oasis:names:tc:ebxml-regrep:AssociationType:HasMember\' AND ass.sourceObject = rp.id\n      '),
 ('urn:oasis:names:tc:ebxml-regrep:query:RegistryObjectVersionQuery',NULL,'urn:oasis:names:tc:ebxml-regrep:query:RegistryObjectVersionQuery','urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AdhocQuery','urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted','1.1',NULL,'urn:oasis:names:tc:ebxml-regrep:QueryLanguage:SQL-92','\nSELECT  * FROM ExtrinsicObject WHERE lid LIKE \'$lid\' AND versionName LIKE \'$versionName\' ORDER BY versionName;\n        ');
*/

/*!40000 ALTER TABLE `adhocquery` ENABLE KEYS */;


--
-- Definition of table `affectedobject`
--

DROP TABLE IF EXISTS `affectedobject`;
CREATE TABLE `affectedobject` (
  `id` varchar(256) NOT NULL,
  `home` varchar(256) DEFAULT NULL,
  `eventId` varchar(256) NOT NULL,
  PRIMARY KEY (`id`,`eventId`),
  KEY `id_evId_AFOBJ_idx` (`id`,`eventId`),
  KEY `id_AFOBJ_idx` (`id`),
  KEY `evid_AFOBJ_idx` (`eventId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `affectedobject`
--

/*!40000 ALTER TABLE `affectedobject` DISABLE KEYS */;
/*!40000 ALTER TABLE `affectedobject` ENABLE KEYS */;


--
-- Definition of table `association`
--

DROP TABLE IF EXISTS `association`;
CREATE TABLE `association` (
  `id` varchar(256) NOT NULL,
  `home` varchar(256) DEFAULT NULL,
  `lid` varchar(256) NOT NULL,
  `objectType` varchar(256) DEFAULT NULL,
  `status` varchar(256) NOT NULL,
  `versionName` varchar(16) DEFAULT NULL,
  `comment_` varchar(256) DEFAULT NULL,
  `associationType` varchar(256) NOT NULL,
  `sourceObject` varchar(256) NOT NULL,
  `targetObject` varchar(256) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `lid_Assoc_idx` (`lid`),
  KEY `id_Assoc_idx` (`id`),
  KEY `home_Assoc_idx` (`home`),
  KEY `src_Ass_idx` (`sourceObject`),
  KEY `tgt_Ass_idx` (`targetObject`),
  KEY `type_Ass_idx` (`associationType`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `association`
--

/*!40000 ALTER TABLE `association` DISABLE KEYS */;
/*!40000 ALTER TABLE `association` ENABLE KEYS */;


--
-- Definition of table `auditableevent`
--

DROP TABLE IF EXISTS `auditableevent`;
CREATE TABLE `auditableevent` (
  `id` varchar(256) NOT NULL,
  `home` varchar(256) DEFAULT NULL,
  `lid` varchar(256) NOT NULL,
  `objectType` varchar(256) DEFAULT NULL,
  `status` varchar(256) NOT NULL,
  `versionName` varchar(16) DEFAULT NULL,
  `comment_` varchar(256) DEFAULT NULL,
  `requestId` varchar(256) NOT NULL,
  `eventType` varchar(256) NOT NULL,
  `timeStamp_` varchar(30) NOT NULL,
  `user_` varchar(256) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `lid_AUEVENT_idx` (`lid`),
  KEY `id_AUEVENT_idx` (`id`),
  KEY `home_AUEVENT_idx` (`home`),
  KEY `lid_AUEVENT_evtTyp` (`eventType`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `auditableevent`
--

/*!40000 ALTER TABLE `auditableevent` DISABLE KEYS */;
/*!40000 ALTER TABLE `auditableevent` ENABLE KEYS */;


--
-- Definition of table `classification`
--

DROP TABLE IF EXISTS `classification`;
CREATE TABLE `classification` (
  `id` varchar(256) NOT NULL,
  `home` varchar(256) DEFAULT NULL,
  `lid` varchar(256) NOT NULL,
  `objectType` varchar(256) DEFAULT NULL,
  `status` varchar(256) NOT NULL,
  `versionName` varchar(16) DEFAULT NULL,
  `comment_` varchar(256) DEFAULT NULL,
  `classificationNode` varchar(256) DEFAULT NULL,
  `classificationScheme` varchar(256) DEFAULT NULL,
  `classifiedObject` varchar(256) NOT NULL,
  `nodeRepresentation` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `lid_Class_idx` (`lid`),
  KEY `id_Class_idx` (`id`),
  KEY `home_Class_idx` (`home`),
  KEY `clsObj_Class_idx` (`classifiedObject`),
  KEY `node_Class_idx` (`classificationNode`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `classification`
--

/*!40000 ALTER TABLE `classification` DISABLE KEYS */;
/*!40000 ALTER TABLE `classification` ENABLE KEYS */;


--
-- Definition of table `classificationnode`
--

DROP TABLE IF EXISTS `classificationnode`;
CREATE TABLE `classificationnode` (
  `id` varchar(256) NOT NULL,
  `home` varchar(256) DEFAULT NULL,
  `lid` varchar(256) NOT NULL,
  `objectType` varchar(256) DEFAULT NULL,
  `status` varchar(256) NOT NULL,
  `versionName` varchar(16) DEFAULT NULL,
  `comment_` varchar(256) DEFAULT NULL,
  `code` varchar(256) DEFAULT NULL,
  `parent` varchar(256) DEFAULT NULL,
  `path` varchar(1024) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `lid_Node_idx` (`lid`),
  KEY `id_Node_idx` (`id`),
  KEY `home_Node_idx` (`home`),
  KEY `parent_Node_idx` (`parent`),
  KEY `code_Node_idx` (`code`),
  KEY `path_Node_idx` (`path`(767))
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


--
-- Definition of table `classscheme`
--

DROP TABLE IF EXISTS `classscheme`;
CREATE TABLE `classscheme` (
  `id` varchar(256) NOT NULL,
  `home` varchar(256) DEFAULT NULL,
  `lid` varchar(256) NOT NULL,
  `objectType` varchar(256) DEFAULT NULL,
  `status` varchar(256) NOT NULL,
  `versionName` varchar(16) DEFAULT NULL,
  `comment_` varchar(256) DEFAULT NULL,
  `isInternal` varchar(1) NOT NULL,
  `nodeType` varchar(256) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `lid_SCHEME_idx` (`lid`),
  KEY `id_SCHEME_idx` (`id`),
  KEY `home_SCHEME_idx` (`home`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


--
-- Definition of table `description`
--

DROP TABLE IF EXISTS `description`;
CREATE TABLE `description` (
  `charset` varchar(32) DEFAULT NULL,
  `lang` varchar(32) NOT NULL,
  `value` varchar(1024) NOT NULL,
  `parent` varchar(256) NOT NULL,
  PRIMARY KEY (`parent`,`lang`),
  KEY `value_Desc_idx` (`value`(767))
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `description`
--

/*!40000 ALTER TABLE `description` DISABLE KEYS */;
/*!40000 ALTER TABLE `description` ENABLE KEYS */;


--
-- Definition of table `emailaddress`
--

DROP TABLE IF EXISTS `emailaddress`;
CREATE TABLE `emailaddress` (
  `address` varchar(64) NOT NULL,
  `type` varchar(256) DEFAULT NULL,
  `parent` varchar(256) NOT NULL,
  KEY `parent_EmlAdr_idx` (`parent`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `emailaddress`
--

/*!40000 ALTER TABLE `emailaddress` DISABLE KEYS */;
/*!40000 ALTER TABLE `emailaddress` ENABLE KEYS */;


--
-- Definition of table `externalidentifier`
--

DROP TABLE IF EXISTS `externalidentifier`;
CREATE TABLE `externalidentifier` (
  `id` varchar(256) NOT NULL,
  `home` varchar(256) DEFAULT NULL,
  `lid` varchar(256) NOT NULL,
  `objectType` varchar(256) DEFAULT NULL,
  `status` varchar(256) NOT NULL,
  `versionName` varchar(16) DEFAULT NULL,
  `comment_` varchar(256) DEFAULT NULL,
  `registryObject` varchar(256) NOT NULL,
  `identificationScheme` varchar(256) NOT NULL,
  `value` varchar(256) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `lid_EID_idx` (`lid`),
  KEY `id_EID_idx` (`id`),
  KEY `home_EID_idx` (`home`),
  KEY `ro_EID_idx` (`registryObject`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `externalidentifier`
--

/*!40000 ALTER TABLE `externalidentifier` DISABLE KEYS */;
/*!40000 ALTER TABLE `externalidentifier` ENABLE KEYS */;


--
-- Definition of table `externallink`
--

DROP TABLE IF EXISTS `externallink`;
CREATE TABLE `externallink` (
  `id` varchar(256) NOT NULL,
  `home` varchar(256) DEFAULT NULL,
  `lid` varchar(256) NOT NULL,
  `objectType` varchar(256) DEFAULT NULL,
  `status` varchar(256) NOT NULL,
  `versionName` varchar(16) DEFAULT NULL,
  `comment_` varchar(256) DEFAULT NULL,
  `externalURI` varchar(256) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `lid_ExLink_idx` (`lid`),
  KEY `id_ExLink_idx` (`id`),
  KEY `home_ExLink_idx` (`home`),
  KEY `uri_ExLink_idx` (`externalURI`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `externallink`
--

/*!40000 ALTER TABLE `externallink` DISABLE KEYS */;
/*!40000 ALTER TABLE `externallink` ENABLE KEYS */;


--
-- Definition of table `extrinsicobject`
--

DROP TABLE IF EXISTS `extrinsicobject`;
CREATE TABLE `extrinsicobject` (
  `id` varchar(256) NOT NULL,
  `home` varchar(256) DEFAULT NULL,
  `lid` varchar(256) NOT NULL,
  `objectType` varchar(256) DEFAULT NULL,
  `status` varchar(256) NOT NULL,
  `versionName` varchar(16) DEFAULT NULL,
  `comment_` varchar(256) DEFAULT NULL,
  `isOpaque` varchar(1) NOT NULL,
  `mimeType` varchar(256) DEFAULT NULL,
  `contentVersionName` varchar(16) DEFAULT NULL,
  `contentVersionComment` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `lid_EXTOBJ_idx` (`lid`),
  KEY `id_EXTOBJ_idx` (`id`),
  KEY `home_EXTOBJ_idx` (`home`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `extrinsicobject`
--

/*!40000 ALTER TABLE `extrinsicobject` DISABLE KEYS */;
/*!40000 ALTER TABLE `extrinsicobject` ENABLE KEYS */;


--
-- Definition of table `federation`
--

DROP TABLE IF EXISTS `federation`;
CREATE TABLE `federation` (
  `id` varchar(256) NOT NULL,
  `home` varchar(256) DEFAULT NULL,
  `lid` varchar(256) NOT NULL,
  `objectType` varchar(256) DEFAULT NULL,
  `status` varchar(256) NOT NULL,
  `versionName` varchar(16) DEFAULT NULL,
  `comment_` varchar(256) DEFAULT NULL,
  `replicationSyncLatency` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `lid_FED_idx` (`lid`),
  KEY `id_FED_idx` (`id`),
  KEY `home_FED_idx` (`home`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `federation`
--

/*!40000 ALTER TABLE `federation` DISABLE KEYS */;
/*!40000 ALTER TABLE `federation` ENABLE KEYS */;


--
-- Definition of table `name_`
--

DROP TABLE IF EXISTS `name_`;
CREATE TABLE `name_` (
  `charset` varchar(32) DEFAULT NULL,
  `lang` varchar(32) NOT NULL,
  `value` varchar(1024) NOT NULL,
  `parent` varchar(256) NOT NULL,
  PRIMARY KEY (`parent`,`lang`),
  KEY `value_Name_idx` (`value`(767))
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `name_`
--

/*!40000 ALTER TABLE `name_` DISABLE KEYS */;
/*!40000 ALTER TABLE `name_` ENABLE KEYS */;


--
-- Definition of table `notification`
--

DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id` varchar(256) NOT NULL,
  `home` varchar(256) DEFAULT NULL,
  `lid` varchar(256) NOT NULL,
  `objectType` varchar(256) DEFAULT NULL,
  `status` varchar(256) NOT NULL,
  `versionName` varchar(16) DEFAULT NULL,
  `comment_` varchar(256) DEFAULT NULL,
  `subscription` varchar(256) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `notification`
--

/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;


--
-- Definition of table `notificationobject`
--

DROP TABLE IF EXISTS `notificationobject`;
CREATE TABLE `notificationobject` (
  `notificationId` varchar(256) NOT NULL,
  `registryObjectId` varchar(256) NOT NULL,
  PRIMARY KEY (`notificationId`,`registryObjectId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `notificationobject`
--

/*!40000 ALTER TABLE `notificationobject` DISABLE KEYS */;
/*!40000 ALTER TABLE `notificationobject` ENABLE KEYS */;


--
-- Definition of table `notifyaction`
--

DROP TABLE IF EXISTS `notifyaction`;
CREATE TABLE `notifyaction` (
  `notificationOption` varchar(256) NOT NULL,
  `endPoint` varchar(256) NOT NULL,
  `parent` varchar(256) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `notifyaction`
--

/*!40000 ALTER TABLE `notifyaction` DISABLE KEYS */;
/*!40000 ALTER TABLE `notifyaction` ENABLE KEYS */;


--
-- Definition of table `objectref`
--

DROP TABLE IF EXISTS `objectref`;
CREATE TABLE `objectref` (
  `id` varchar(256) NOT NULL,
  `home` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id_ObjectRef_idx` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `objectref`
--

/*!40000 ALTER TABLE `objectref` DISABLE KEYS */;
/*!40000 ALTER TABLE `objectref` ENABLE KEYS */;


--
-- Definition of table `organization`
--

DROP TABLE IF EXISTS `organization`;
CREATE TABLE `organization` (
  `id` varchar(256) NOT NULL,
  `home` varchar(256) DEFAULT NULL,
  `lid` varchar(256) NOT NULL,
  `objectType` varchar(256) DEFAULT NULL,
  `status` varchar(256) NOT NULL,
  `versionName` varchar(16) DEFAULT NULL,
  `comment_` varchar(256) DEFAULT NULL,
  `parent` varchar(256) DEFAULT NULL,
  `primaryContact` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `lid_ORG_idx` (`lid`),
  KEY `id_ORG_idx` (`id`),
  KEY `home_ORG_idx` (`home`),
  KEY `parent_ORG_idx` (`parent`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `organization`
--

/*!40000 ALTER TABLE `organization` DISABLE KEYS */;
/*!40000 ALTER TABLE `organization` ENABLE KEYS */;


--
-- Definition of table `person`
--

DROP TABLE IF EXISTS `person`;
CREATE TABLE `person` (
  `id` varchar(256) NOT NULL,
  `home` varchar(256) DEFAULT NULL,
  `lid` varchar(256) NOT NULL,
  `objectType` varchar(256) DEFAULT NULL,
  `status` varchar(256) NOT NULL,
  `versionName` varchar(16) DEFAULT NULL,
  `comment_` varchar(256) DEFAULT NULL,
  `personName_firstName` varchar(64) DEFAULT NULL,
  `personName_middleName` varchar(64) DEFAULT NULL,
  `personName_lastName` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `lid_Person_idx` (`lid`),
  KEY `id_Person_idx` (`id`),
  KEY `home_Person_idx` (`home`),
  KEY `lastNm_Person_idx` (`personName_lastName`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `person`
--

/*!40000 ALTER TABLE `person` DISABLE KEYS */;
/*!40000 ALTER TABLE `person` ENABLE KEYS */;


--
-- Definition of table `postaladdress`
--

DROP TABLE IF EXISTS `postaladdress`;
CREATE TABLE `postaladdress` (
  `city` varchar(64) DEFAULT NULL,
  `country` varchar(64) DEFAULT NULL,
  `postalCode` varchar(64) DEFAULT NULL,
  `state` varchar(64) DEFAULT NULL,
  `street` varchar(64) DEFAULT NULL,
  `streetNumber` varchar(32) DEFAULT NULL,
  `parent` varchar(256) NOT NULL,
  KEY `parent_PstlAdr_idx` (`parent`),
  KEY `city_PstlAdr_idx` (`city`),
  KEY `cntry_PstlAdr_idx` (`country`),
  KEY `pCode_PstlAdr_idx` (`postalCode`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `postaladdress`
--

/*!40000 ALTER TABLE `postaladdress` DISABLE KEYS */;
/*!40000 ALTER TABLE `postaladdress` ENABLE KEYS */;


--
-- Definition of table `registry`
--

DROP TABLE IF EXISTS `registry`;
CREATE TABLE `registry` (
  `id` varchar(256) NOT NULL,
  `home` varchar(256) DEFAULT NULL,
  `lid` varchar(256) NOT NULL,
  `objectType` varchar(256) DEFAULT NULL,
  `status` varchar(256) NOT NULL,
  `versionName` varchar(16) DEFAULT NULL,
  `comment_` varchar(256) DEFAULT NULL,
  `catalogingSyncLatency` varchar(32) DEFAULT 'P1D',
  `conformanceProfile` varchar(16) DEFAULT NULL,
  `operator` varchar(256) NOT NULL,
  `replicationSyncLatency` varchar(32) DEFAULT 'P1D',
  `specificationVersion` varchar(8) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `lid_Registry_idx` (`lid`),
  KEY `id_Registry_idx` (`id`),
  KEY `home_Registry_idx` (`home`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `registry`
--

/*!40000 ALTER TABLE `registry` DISABLE KEYS */;
/*!40000 ALTER TABLE `registry` ENABLE KEYS */;


--
-- Definition of table `registrypackage`
--

DROP TABLE IF EXISTS `registrypackage`;
CREATE TABLE `registrypackage` (
  `id` varchar(256) NOT NULL,
  `home` varchar(256) DEFAULT NULL,
  `lid` varchar(256) NOT NULL,
  `objectType` varchar(256) DEFAULT NULL,
  `status` varchar(256) NOT NULL,
  `versionName` varchar(16) DEFAULT NULL,
  `comment_` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `lid_PKG_idx` (`lid`),
  KEY `id_PKG_idx` (`id`),
  KEY `home_PKG_idx` (`home`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `registrypackage`
--

/*!40000 ALTER TABLE `registrypackage` DISABLE KEYS */;
/*!40000 ALTER TABLE `registrypackage` ENABLE KEYS */;


--
-- Definition of table `repositoryitem`
--

DROP TABLE IF EXISTS `repositoryitem`;
CREATE TABLE `repositoryitem` (
  `lid` varchar(256) NOT NULL,
  `versionName` varchar(16) NOT NULL,
  `content` blob,
  PRIMARY KEY (`lid`,`versionName`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `repositoryitem`
--

/*!40000 ALTER TABLE `repositoryitem` DISABLE KEYS */;
/*!40000 ALTER TABLE `repositoryitem` ENABLE KEYS */;


--
-- Definition of table `service`
--

DROP TABLE IF EXISTS `service`;
CREATE TABLE `service` (
  `id` varchar(256) NOT NULL,
  `home` varchar(256) DEFAULT NULL,
  `lid` varchar(256) NOT NULL,
  `objectType` varchar(256) DEFAULT NULL,
  `status` varchar(256) NOT NULL,
  `versionName` varchar(16) DEFAULT NULL,
  `comment_` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `lid_Service_idx` (`lid`),
  KEY `id_Service_idx` (`id`),
  KEY `home_Service_idx` (`home`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `service`
--

/*!40000 ALTER TABLE `service` DISABLE KEYS */;
/*!40000 ALTER TABLE `service` ENABLE KEYS */;


--
-- Definition of table `servicebinding`
--

DROP TABLE IF EXISTS `servicebinding`;
CREATE TABLE `servicebinding` (
  `id` varchar(256) NOT NULL,
  `home` varchar(256) DEFAULT NULL,
  `lid` varchar(256) NOT NULL,
  `objectType` varchar(256) DEFAULT NULL,
  `status` varchar(256) NOT NULL,
  `versionName` varchar(16) DEFAULT NULL,
  `comment_` varchar(256) DEFAULT NULL,
  `service` varchar(256) NOT NULL,
  `accessURI` varchar(256) DEFAULT NULL,
  `targetBinding` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `lid_BIND_idx` (`lid`),
  KEY `id_BIND_idx` (`id`),
  KEY `home_BIND_idx` (`home`),
  KEY `service_BIND_idx` (`service`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `servicebinding`
--

/*!40000 ALTER TABLE `servicebinding` DISABLE KEYS */;
/*!40000 ALTER TABLE `servicebinding` ENABLE KEYS */;


--
-- Definition of table `slot`
--

DROP TABLE IF EXISTS `slot`;
CREATE TABLE `slot` (
  `sequenceId` int(11) NOT NULL,
  `name_` varchar(256) NOT NULL,
  `slotType` varchar(256) DEFAULT NULL,
  `value` varchar(256) DEFAULT NULL,
  `parent` varchar(256) NOT NULL,
  PRIMARY KEY (`parent`,`name_`,`sequenceId`),
  KEY `parent_Slot_idx` (`parent`),
  KEY `name_Slot_idx` (`name_`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `slot`
--

/*!40000 ALTER TABLE `slot` DISABLE KEYS */;
/*!40000 ALTER TABLE `slot` ENABLE KEYS */;


--
-- Definition of table `specificationlink`
--

DROP TABLE IF EXISTS `specificationlink`;
CREATE TABLE `specificationlink` (
  `id` varchar(256) NOT NULL,
  `home` varchar(256) DEFAULT NULL,
  `lid` varchar(256) NOT NULL,
  `objectType` varchar(256) DEFAULT NULL,
  `status` varchar(256) NOT NULL,
  `versionName` varchar(16) DEFAULT NULL,
  `comment_` varchar(256) DEFAULT NULL,
  `serviceBinding` varchar(256) NOT NULL,
  `specificationObject` varchar(256) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `lid_SLnk_idx` (`lid`),
  KEY `id_SLnk_idx` (`id`),
  KEY `home_SLnk_idx` (`home`),
  KEY `binding_SLnk_idx` (`serviceBinding`),
  KEY `spec_SLnk_idx` (`specificationObject`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `specificationlink`
--

/*!40000 ALTER TABLE `specificationlink` DISABLE KEYS */;
/*!40000 ALTER TABLE `specificationlink` ENABLE KEYS */;


--
-- Definition of table `subscription`
--

DROP TABLE IF EXISTS `subscription`;
CREATE TABLE `subscription` (
  `id` varchar(256) NOT NULL,
  `home` varchar(256) DEFAULT NULL,
  `lid` varchar(256) NOT NULL,
  `objectType` varchar(256) DEFAULT NULL,
  `status` varchar(256) NOT NULL,
  `versionName` varchar(16) DEFAULT NULL,
  `comment_` varchar(256) DEFAULT NULL,
  `selector` varchar(256) NOT NULL,
  `endTime` varchar(30) DEFAULT NULL,
  `notificationInterval` varchar(32) DEFAULT 'P1D',
  `startTime` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `lid_SUBS_idx` (`lid`),
  KEY `id_SUBS_idx` (`id`),
  KEY `home_SUBS_idx` (`home`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `subscription`
--

/*!40000 ALTER TABLE `subscription` DISABLE KEYS */;
/*!40000 ALTER TABLE `subscription` ENABLE KEYS */;


--
-- Definition of table `telephonenumber`
--

DROP TABLE IF EXISTS `telephonenumber`;
CREATE TABLE `telephonenumber` (
  `areaCode` varchar(8) DEFAULT NULL,
  `countryCode` varchar(8) DEFAULT NULL,
  `extension` varchar(8) DEFAULT NULL,
  `number_` varchar(16) DEFAULT NULL,
  `phoneType` varchar(256) DEFAULT NULL,
  `parent` varchar(256) NOT NULL,
  KEY `parent_Phone_idx` (`parent`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `telephonenumber`
--

/*!40000 ALTER TABLE `telephonenumber` DISABLE KEYS */;
/*!40000 ALTER TABLE `telephonenumber` ENABLE KEYS */;


--
-- Definition of table `usagedescription`
--

DROP TABLE IF EXISTS `usagedescription`;
CREATE TABLE `usagedescription` (
  `charset` varchar(32) DEFAULT NULL,
  `lang` varchar(32) NOT NULL,
  `value` varchar(1024) NOT NULL,
  `parent` varchar(256) NOT NULL,
  PRIMARY KEY (`parent`,`lang`),
  KEY `value_UsgDes_idx` (`value`(767))
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `usagedescription`
--

/*!40000 ALTER TABLE `usagedescription` DISABLE KEYS */;
/*!40000 ALTER TABLE `usagedescription` ENABLE KEYS */;


--
-- Definition of table `usageparameter`
--

DROP TABLE IF EXISTS `usageparameter`;
CREATE TABLE `usageparameter` (
  `value` varchar(1024) NOT NULL,
  `parent` varchar(256) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `usageparameter`
--

/*!40000 ALTER TABLE `usageparameter` DISABLE KEYS */;
/*!40000 ALTER TABLE `usageparameter` ENABLE KEYS */;


--
-- Definition of table `user_`
--

DROP TABLE IF EXISTS `user_`;
CREATE TABLE `user_` (
  `id` varchar(256) NOT NULL,
  `home` varchar(256) DEFAULT NULL,
  `lid` varchar(256) NOT NULL,
  `objectType` varchar(256) DEFAULT NULL,
  `status` varchar(256) NOT NULL,
  `versionName` varchar(16) DEFAULT NULL,
  `comment_` varchar(256) DEFAULT NULL,
  `personName_firstName` varchar(64) DEFAULT NULL,
  `personName_middleName` varchar(64) DEFAULT NULL,
  `personName_lastName` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `lid_User_idx` (`lid`),
  KEY `id_User_idx` (`id`),
  KEY `home_User_idx` (`home`),
  KEY `lastNm_User_idx` (`personName_lastName`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


--
-- Definition of view `identifiable`
--

DROP TABLE IF EXISTS `identifiable`;
DROP VIEW IF EXISTS `identifiable`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `identifiable` AS select `adhocquery`.`id` AS `id`,`adhocquery`.`home` AS `home` from `adhocquery` union all select `association`.`id` AS `id`,`association`.`home` AS `home` from `association` union all select `auditableevent`.`id` AS `id`,`auditableevent`.`home` AS `home` from `auditableevent` union all select `classification`.`id` AS `id`,`classification`.`home` AS `home` from `classification` union all select `classificationnode`.`id` AS `id`,`classificationnode`.`home` AS `home` from `classificationnode` union all select `classscheme`.`id` AS `id`,`classscheme`.`home` AS `home` from `classscheme` union all select `externalidentifier`.`id` AS `id`,`externalidentifier`.`home` AS `home` from `externalidentifier` union all select `externallink`.`id` AS `id`,`externallink`.`home` AS `home` from `externallink` union all select `extrinsicobject`.`id` AS `id`,`extrinsicobject`.`home` AS `home` from `extrinsicobject` union all select `federation`.`id` AS `id`,`federation`.`home` AS `home` from `federation` union all select `organization`.`id` AS `id`,`organization`.`home` AS `home` from `organization` union all select `registry`.`id` AS `id`,`registry`.`home` AS `home` from `registry` union all select `registrypackage`.`id` AS `id`,`registrypackage`.`home` AS `home` from `registrypackage` union all select `service`.`id` AS `id`,`service`.`home` AS `home` from `service` union all select `servicebinding`.`id` AS `id`,`servicebinding`.`home` AS `home` from `servicebinding` union all select `specificationlink`.`id` AS `id`,`specificationlink`.`home` AS `home` from `specificationlink` union all select `subscription`.`id` AS `id`,`subscription`.`home` AS `home` from `subscription` union all select `user_`.`id` AS `id`,`user_`.`home` AS `home` from `user_` union all select `person`.`id` AS `id`,`person`.`home` AS `home` from `person` union all select `objectref`.`id` AS `id`,`objectref`.`home` AS `home` from `objectref`;

--
-- Definition of view `registryobject`
--
/*
DROP TABLE IF EXISTS `registryobject`;
DROP VIEW IF EXISTS `registryobject`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `registryobject` AS select `adhocquery`.`id` AS `id`,`adhocquery`.`home` AS `home`,`adhocquery`.`lid` AS `lid`,`adhocquery`.`objectType` AS `objectType`,`adhocquery`.`status` AS `status`,`adhocquery`.`versionName` AS `versionName`,`adhocquery`.`comment_` AS `comment_` from `adhocquery` union all select `association`.`id` AS `id`,`association`.`home` AS `home`,`association`.`lid` AS `lid`,`association`.`objectType` AS `objectType`,`association`.`status` AS `status`,`association`.`versionName` AS `versionName`,`association`.`comment_` AS `comment_` from `association` union all select `auditableevent`.`id` AS `id`,`auditableevent`.`home` AS `home`,`auditableevent`.`lid` AS `lid`,`auditableevent`.`objectType` AS `objectType`,`auditableevent`.`status` AS `status`,`auditableevent`.`versionName` AS `versionName`,`auditableevent`.`comment_` AS `comment_` from `auditableevent` union all select `classification`.`id` AS `id`,`classification`.`home` AS `home`,`classification`.`lid` AS `lid`,`classification`.`objectType` AS `objectType`,`classification`.`status` AS `status`,`classification`.`versionName` AS `versionName`,`classification`.`comment_` AS `comment_` from `classification` union all select `classificationnode`.`id` AS `id`,`classificationnode`.`home` AS `home`,`classificationnode`.`lid` AS `lid`,`classificationnode`.`objectType` AS `objectType`,`classificationnode`.`status` AS `status`,`classificationnode`.`versionName` AS `versionName`,`classificationnode`.`comment_` AS `comment_` from `classificationnode` union all select `classscheme`.`id` AS `id`,`classscheme`.`home` AS `home`,`classscheme`.`lid` AS `lid`,`classscheme`.`objectType` AS `objectType`,`classscheme`.`status` AS `status`,`classscheme`.`versionName` AS `versionName`,`classscheme`.`comment_` AS `comment_` from `classscheme` union all select `externalidentifier`.`id` AS `id`,`externalidentifier`.`home` AS `home`,`externalidentifier`.`lid` AS `lid`,`externalidentifier`.`objectType` AS `objectType`,`externalidentifier`.`status` AS `status`,`externalidentifier`.`versionName` AS `versionName`,`externalidentifier`.`comment_` AS `comment_` from `externalidentifier` union all select `externallink`.`id` AS `id`,`externallink`.`home` AS `home`,`externallink`.`lid` AS `lid`,`externallink`.`objectType` AS `objectType`,`externallink`.`status` AS `status`,`externallink`.`versionName` AS `versionName`,`externallink`.`comment_` AS `comment_` from `externallink` union all select `extrinsicobject`.`id` AS `id`,`extrinsicobject`.`home` AS `home`,`extrinsicobject`.`lid` AS `lid`,`extrinsicobject`.`objectType` AS `objectType`,`extrinsicobject`.`status` AS `status`,`extrinsicobject`.`versionName` AS `versionName`,`extrinsicobject`.`comment_` AS `comment_` from `extrinsicobject` union all select `federation`.`id` AS `id`,`federation`.`home` AS `home`,`federation`.`lid` AS `lid`,`federation`.`objectType` AS `objectType`,`federation`.`status` AS `status`,`federation`.`versionName` AS `versionName`,`federation`.`comment_` AS `comment_` from `federation` union all select `organization`.`id` AS `id`,`organization`.`home` AS `home`,`organization`.`lid` AS `lid`,`organization`.`objectType` AS `objectType`,`organization`.`status` AS `status`,`organization`.`versionName` AS `versionName`,`organization`.`comment_` AS `comment_` from `organization` union all select `registry`.`id` AS `id`,`registry`.`home` AS `home`,`registry`.`lid` AS `lid`,`registry`.`objectType` AS `objectType`,`registry`.`status` AS `status`,`registry`.`versionName` AS `versionName`,`registry`.`comment_` AS `comment_` from `registry` union all select `registrypackage`.`id` AS `id`,`registrypackage`.`home` AS `home`,`registrypackage`.`lid` AS `lid`,`registrypackage`.`objectType` AS `objectType`,`registrypackage`.`status` AS `status`,`registrypackage`.`versionName` AS `versionName`,`registrypackage`.`comment_` AS `comment_` from `registrypackage` union all select `service`.`id` AS `id`,`service`.`home` AS `home`,`service`.`lid` AS `lid`,`service`.`objectType` AS `objectType`,`service`.`status` AS `status`,`service`.`versionName` AS `versionName`,`service`.`comment_` AS `comment_` from `service` union all select `servicebinding`.`id` AS `id`,`servicebinding`.`home` AS `home`,`servicebinding`.`lid` AS `lid`,`servicebinding`.`objectType` AS `objectType`,`servicebinding`.`status` AS `status`,`servicebinding`.`versionName` AS `versionName`,`servicebinding`.`comment_` AS `comment_` from `servicebinding` union all select `specificationlink`.`id` AS `id`,`specificationlink`.`home` AS `home`,`specificationlink`.`lid` AS `lid`,`specificationlink`.`objectType` AS `objectType`,`specificationlink`.`status` AS `status`,`specificationlink`.`versionName` AS `versionName`,`specificationlink`.`comment_` AS `comment_` from `specificationlink` union all select `subscription`.`id` AS `id`,`subscription`.`home` AS `home`,`subscription`.`lid` AS `lid`,`subscription`.`objectType` AS `objectType`,`subscription`.`status` AS `status`,`subscription`.`versionName` AS `versionName`,`subscription`.`comment_` AS `comment_` from `subscription` union all select `user_`.`id` AS `id`,`user_`.`home` AS `home`,`user_`.`lid` AS `lid`,`user_`.`objectType` AS `objectType`,`user_`.`status` AS `status`,`user_`.`versionName` AS `versionName`,`user_`.`comment_` AS `comment_` from `user_` union all select `person`.`id` AS `id`,`person`.`home` AS `home`,`person`.`lid` AS `lid`,`person`.`objectType` AS `objectType`,`person`.`status` AS `status`,`person`.`versionName` AS `versionName`,`person`.`comment_` AS `comment_` from `person`;
*/


/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;