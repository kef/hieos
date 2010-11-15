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
-- Create schema adt
--

CREATE DATABASE IF NOT EXISTS adt;
USE adt;

--
-- Definition of table `patient`
--

DROP TABLE IF EXISTS `patient`;
CREATE TABLE `patient` (
  `uuid` varchar(64) NOT NULL,
  `id` text NOT NULL,
  `timestamp` text,
  `status` CHAR(1) NOT NULL DEFAULT 'A',
  PRIMARY KEY (`id`(100)),
  KEY `patient_uuid_idx` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


--
-- Dumping data for table `patient`
--

/*!40000 ALTER TABLE `patient` DISABLE KEYS */;
/*!40000 ALTER TABLE `patient` ENABLE KEYS */;


DROP TABLE IF EXISTS `patientaddress`;
DROP TABLE IF EXISTS `patientname`;
DROP TABLE IF EXISTS `patientrace`;


--
-- Definition of table mergehistory
--
DROP TABLE IF EXISTS `mergedobjects`;
DROP TABLE IF EXISTS `mergehistory`;

CREATE TABLE `mergehistory` (
  `uniqueid` VARCHAR(64) NOT NULL,
  `survivingpatientid` VARCHAR(100) NOT NULL,
  `subsumedpatientid` VARCHAR(100) NOT NULL,
  `action` CHAR(1) NOT NULL,
  `datetimeperformed` DATETIME NOT NULL,
  CONSTRAINT mh_pkey PRIMARY KEY (uniqueid)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Create index on mergehistory
--
CREATE INDEX mh_patientids_idx ON mergehistory (survivingpatientid, subsumedpatientid);
CREATE INDEX mh_action_idx ON mergehistory (action);


--
-- Definition of table mergedobjects
--
CREATE TABLE `mergedobjects` (
  `parentid` VARCHAR(64) NOT NULL,
  `externalidentifierid` VARCHAR(64) NOT NULL,
  CONSTRAINT mo_pkey PRIMARY KEY (parentid, externalidentifierid),
  CONSTRAINT mo_mh_fkey FOREIGN KEY (parentid)
      REFERENCES mergehistory (uniqueid)
      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
