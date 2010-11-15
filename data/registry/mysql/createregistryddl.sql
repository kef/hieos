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
-- Server version	5.1.34-community-log


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO,MYSQL323' */;


--
-- Create schema omar
--

CREATE DATABASE IF NOT EXISTS omar;
USE omar;


DROP TABLE IF EXISTS `adhocquery`;
DROP TABLE IF EXISTS `affectedobject`;
DROP TABLE IF EXISTS `auditableevent`;
DROP TABLE IF EXISTS `classificationnode`;
DROP TABLE IF EXISTS `classscheme`;
DROP TABLE IF EXISTS `emailaddress`;
DROP TABLE IF EXISTS `externallink`;
DROP TABLE IF EXISTS `federation`;
DROP TABLE IF EXISTS `notification`;
DROP TABLE IF EXISTS `notificationobject`;
DROP TABLE IF EXISTS `notifyaction`;
DROP TABLE IF EXISTS `objectref`;
DROP TABLE IF EXISTS `organization`;
DROP TABLE IF EXISTS `person`;
DROP TABLE IF EXISTS `postaladdress`;
DROP TABLE IF EXISTS `registry`;
DROP TABLE IF EXISTS `registryobject`;
DROP VIEW IF EXISTS `registryobject`;
DROP TABLE IF EXISTS `repositoryitem`;
DROP TABLE IF EXISTS `service`;
DROP TABLE IF EXISTS `servicebinding`;
DROP TABLE IF EXISTS `specificationlink`;
DROP TABLE IF EXISTS `subscription`;
DROP TABLE IF EXISTS `telephonenumber`;
DROP TABLE IF EXISTS `usagedescription`;
DROP TABLE IF EXISTS `usageparameter`;
DROP TABLE IF EXISTS `user_`;
DROP TABLE IF EXISTS `identifiable`;
DROP VIEW IF EXISTS `identifiable`;

--
-- Definition of table `association`
--

DROP TABLE IF EXISTS `association`;
CREATE TABLE `association` (
  `id` varchar(64) NOT NULL,
  `lid` varchar(64) NOT NULL,
  `objectType` char(2) DEFAULT NULL,
  `status` char(1) NOT NULL,
  `versionName` varchar(16) DEFAULT NULL,
  `associationType` char(2) NOT NULL,
  `sourceObject` varchar(64) NOT NULL,
  `targetObject` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `src_Ass_idx` (`sourceObject`),
  KEY `tgt_Ass_idx` (`targetObject`),
  KEY `type_Ass_idx` (`associationType`)
) TYPE=InnoDB;

--
-- Dumping data for table `association`
--

/*!40000 ALTER TABLE `association` DISABLE KEYS */;
/*!40000 ALTER TABLE `association` ENABLE KEYS */;


--
-- Definition of table `classification`
--

DROP TABLE IF EXISTS `classification`;
CREATE TABLE `classification` (
  `id` varchar(64) NOT NULL,
  `lid` varchar(64) NOT NULL,
  `objectType` char(2) DEFAULT NULL,
  `status` char(1) NOT NULL,
  `versionName` varchar(16) DEFAULT NULL,
  `classificationNode` varchar(64) DEFAULT NULL,
  `classificationScheme` varchar(64) DEFAULT NULL,
  `classifiedObject` varchar(64) NOT NULL,
  `nodeRepresentation` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `clsObj_Class_idx` (`classifiedObject`)
) TYPE=InnoDB;

--
-- Dumping data for table `classification`
--

/*!40000 ALTER TABLE `classification` DISABLE KEYS */;
/*!40000 ALTER TABLE `classification` ENABLE KEYS */;


--
-- Definition of table `description`
--

DROP TABLE IF EXISTS `description`;
CREATE TABLE `description` (
  `charset` varchar(32) DEFAULT NULL,
  `lang` varchar(32) NOT NULL,
  `value` varchar(256) NOT NULL,
  `parent` varchar(64) NOT NULL,
  PRIMARY KEY (`parent`)
) TYPE=InnoDB;

--
-- Dumping data for table `description`
--

/*!40000 ALTER TABLE `description` DISABLE KEYS */;
/*!40000 ALTER TABLE `description` ENABLE KEYS */;

--
-- Definition of table `externalidentifier`
--

DROP TABLE IF EXISTS `externalidentifier`;
CREATE TABLE `externalidentifier` (
  `id` varchar(64) NOT NULL,
  `lid` varchar(64) NOT NULL,
  `objectType` char(2) DEFAULT NULL,
  `status` char(1) NOT NULL,
  `versionName` varchar(16) DEFAULT NULL,
  `registryObject` varchar(64) NOT NULL,
  `identificationScheme` char(2) NOT NULL,
  `value` varchar(128) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `ro_EID_idx` (`registryObject`),
  KEY `idscheme_EID_idx` (`identificationScheme`),
  KEY `value_EID_idx` (`value`)
) TYPE=InnoDB;

--
-- Dumping data for table `externalidentifier`
--

/*!40000 ALTER TABLE `externalidentifier` DISABLE KEYS */;
/*!40000 ALTER TABLE `externalidentifier` ENABLE KEYS */;


--
-- Definition of table `extrinsicobject`
--

DROP TABLE IF EXISTS `extrinsicobject`;
CREATE TABLE `extrinsicobject` (
  `id` varchar(64) NOT NULL,
  `lid` varchar(64) NOT NULL,
  `objectType` char(2) DEFAULT NULL,
  `status` char(1) NOT NULL,
  `versionName` varchar(16) DEFAULT NULL,
  `isOpaque` char(1) NOT NULL,
  `mimeType` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`id`)
) TYPE=InnoDB;

--
-- Dumping data for table `extrinsicobject`
--

/*!40000 ALTER TABLE `extrinsicobject` DISABLE KEYS */;
/*!40000 ALTER TABLE `extrinsicobject` ENABLE KEYS */;


--
-- Definition of table `name_`
--

DROP TABLE IF EXISTS `name_`;
CREATE TABLE `name_` (
  `charset` varchar(32) DEFAULT NULL,
  `lang` varchar(32) NOT NULL,
  `value` varchar(256) NOT NULL,
  `parent` varchar(64) NOT NULL,
  PRIMARY KEY (`parent`)
) TYPE=InnoDB;

--
-- Dumping data for table `name_`
--

/*!40000 ALTER TABLE `name_` DISABLE KEYS */;
/*!40000 ALTER TABLE `name_` ENABLE KEYS */;

--
-- Definition of table `registrypackage`
--

DROP TABLE IF EXISTS `registrypackage`;
CREATE TABLE `registrypackage` (
  `id` varchar(64) NOT NULL,
  `lid` varchar(64) NOT NULL,
  `objectType` char(2) DEFAULT NULL,
  `status` char(1) NOT NULL,
  `versionName` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`id`)
) TYPE=InnoDB;

--
-- Dumping data for table `registrypackage`
--

/*!40000 ALTER TABLE `registrypackage` DISABLE KEYS */;
/*!40000 ALTER TABLE `registrypackage` ENABLE KEYS */;


--
-- Definition of table `slot`
--

DROP TABLE IF EXISTS `slot`;
CREATE TABLE `slot` (
  `sequenceId` int(11) NOT NULL,
  `name_` varchar(128) NOT NULL,
  `value` varchar(128) DEFAULT NULL,
  `parent` varchar(64) NOT NULL,
  PRIMARY KEY (`parent`,`name_`,`sequenceId`)
) TYPE=InnoDB;

--
-- Dumping data for table `slot`
--

/*!40000 ALTER TABLE `slot` DISABLE KEYS */;
/*!40000 ALTER TABLE `slot` ENABLE KEYS */;


/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;