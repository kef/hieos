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
-- Create schema `log`
--

CREATE DATABASE IF NOT EXISTS `log`;
USE `log`;


--
-- Drop Tables
--

DROP TABLE IF EXISTS `error`;
DROP TABLE IF EXISTS `http`;
DROP TABLE IF EXISTS `other`;
DROP TABLE IF EXISTS `soap`;
DROP TABLE IF EXISTS `logdetail`;
DROP TABLE IF EXISTS `main`;
DROP TABLE IF EXISTS `ip`;


--
-- Definition of table `ip`
--

CREATE TABLE `ip` (
  `ip` varchar(100) NOT NULL,
  `company_name` varchar(255) NOT NULL DEFAULT 'Unknown',
  `email` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ip`),
  KEY `IP_INDEX` (`ip`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ip`
--

/*!40000 ALTER TABLE `ip` DISABLE KEYS */;
/*!40000 ALTER TABLE `ip` ENABLE KEYS */;


--
-- Definition of table `main`
--

CREATE TABLE `main` (
  `messageid` varchar(255) NOT NULL,
  `is_secure` char(1) DEFAULT NULL,
  `ip` varchar(100) NOT NULL,
  `timereceived` timestamp NOT NULL DEFAULT '2008-08-30 19:56:01',
  `test` varchar(100) NOT NULL,
  `pass` char(1) DEFAULT NULL,
  PRIMARY KEY (`messageid`),
  KEY `MAIN_IP_INDEX` (`ip`) USING BTREE,
  KEY `MAIN_MID_INDEX` (`messageid`) USING BTREE,
  CONSTRAINT `main_ip_fkey` FOREIGN KEY (`ip`) REFERENCES `ip` (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `main`
--

/*!40000 ALTER TABLE `main` DISABLE KEYS */;
/*!40000 ALTER TABLE `main` ENABLE KEYS */;

--
-- Definition of table `logdetail`
--
-- logdetail replaces the following tables - error, soap, http and other.
--

CREATE TABLE `logdetail` (
  `type` varchar(10) NOT NULL,
  `messageid` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `value` longtext,
  `seqid` int(11) NOT NULL DEFAULT '0',
  KEY `LOGDETAIL_MID_INDEX` (`messageid`) USING BTREE,
  KEY `LOGDETAIL_TYPE_INDEX` (`type`) USING BTREE,
  CONSTRAINT `logdetail_messageid_fkey` FOREIGN KEY (`messageid`) REFERENCES `main` (`messageid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `logdetail`
--

/*!40000 ALTER TABLE `logdetail` DISABLE KEYS */;
/*!40000 ALTER TABLE `logdetail` ENABLE KEYS */;




/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;