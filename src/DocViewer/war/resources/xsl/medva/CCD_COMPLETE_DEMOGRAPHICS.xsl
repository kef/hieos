<?xml version="1.0"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
 xmlns="http://www.w3.org/1999/xhtml"
 xmlns:n1="urn:hl7-org:v3" 
 xmlns:n2="urn:hl7-org:v3/meta/voc" 
 xmlns:voc="urn:hl7-org:v3/voc" 
 xmlns:sdtc="urn:hl7-org:sdtc"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 xmlns:xalan="http://xml.apache.org/xalan"
 xmlns:java="http://xml.apache.org/xslt/java"
 extension-element-prefixes="java">
 
<xsl:include href="./CCD_FUNCTIONS.xsl"/>
 
<xsl:output method="html" indent="yes" version="4.01" encoding="ISO-8859-1" doctype-public="-//W3C//DTD HTML 4.01//EN"/> 

<xsl:template name="COMPLETE_DEMOGRAPHICS">

	<!-- ***************************************************************  -->
	<!-- ************************* COMPLETE REPORT *********************  -->
	<!-- ***************************************************************  -->

	<h1 align='center' style='font-size:1.15in;'><b><xsl:text>COMPLETE</xsl:text></b></h1>
	<h4 align='center'><b><xsl:text>COMPLETE REPORT</xsl:text></b></h4>

	
	<table border="0" cellpadding="0" width="100%">

	<!--variables-->
	<xsl:variable name="patientRole2" select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole"/>


	<!-- *******************************************************  -->
	<!-- ****************** Complete - Patient Name ************  -->
	<!-- *******************************************************  -->

	<tr>
	<td width='50%' valign="top"><xsl:call-template name="getName">
	<xsl:with-param name="name" select="$patientRole2/n1:patient/n1:name"/>
	</xsl:call-template>
	<br />


	<!-- ** Address ** -->		 		 		 		 
	<xsl:if test="$patientRole2/n1:addr">
	<xsl:call-template name="getAddress"> 
	<xsl:with-param name="addr" select="$patientRole2/n1:addr"/>
	</xsl:call-template>
	</xsl:if>

	<!-- ** Telephone ** -->
	<xsl:if test="$patientRole2/n1:telecom">
	<br />
	<xsl:call-template name="getTelecom"> 
	<xsl:with-param name="telecom" select="$patientRole2/n1:telecom"/>
	</xsl:call-template>
	</xsl:if>
	
	
	<!-- ** Email Address // matchPattern is a Java function that will compare a string (or xpath that goes to a string) to a reuglar expression **  -->
	
	<xsl:for-each select="$patientRole2/n1:telecom">
		<xsl:call-template name="matchPattern">
			<xsl:with-param name="input" select="./@value" />
			<xsl:with-param name="expression" select="'mailto:[\w\-\.]+@([\w-]+\.)+[\w-]+'"/>
		</xsl:call-template>
	</xsl:for-each>
	
	<!-- ** URL ** -->
	<xsl:for-each select="$patientRole2/n1:telecom">
		<xsl:call-template name="matchPattern" >
			<xsl:with-param name="input" select="./@value" />
			<xsl:with-param name="expression" select="'(http|https|ftp)://([\w-]+\.)+[\w-]+(/[\w- ./?%=]*)?'" />
		</xsl:call-template>
	</xsl:for-each>
	</td>
	
	<!-- ** SSN ** -->		 		    
	<td width='50%' align='left' valign="top"><b><xsl:text>SSN: </xsl:text></b>
	<xsl:value-of select="$patientRole2/n1:id/@extension"/></td>		 		        
	</tr>

	<!-- ** DOB ** -->		 		    
	<tr><td width='50%' valign="top"><b><xsl:text>DOB: </xsl:text></b>
	<xsl:call-template name="formatDate">
	<xsl:with-param name="date" select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole/n1:patient/n1:birthTime/@value"/>
	</xsl:call-template></td>

	<!-- ** Sex ** -->					
	<td width='50%' align='left' valign="top"><b><xsl:text>Sex: </xsl:text></b>
	<xsl:variable name="sex2" select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole/n1:patient/n1:administrativeGenderCode/@code"/>
	<xsl:choose>
	<xsl:when test="$sex2='M'">Male</xsl:when>
	<xsl:when test="$sex2='F'">Female</xsl:when>
	<xsl:when test="$sex2='UN'">Undifferentiated</xsl:when>
	</xsl:choose></td>		 		        
	</tr>
	</table>

	<!-- ** Language ** -->					
	<table border="0" cellpadding="0" width="100%">
	<tr><td width='50%' align='left' valign="top"><b><xsl:text>Language: </xsl:text></b>
	<xsl:variable name="language2" select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole/n1:patient/n1:languageCommunication/n1:languageCode/@code"/>
	<xsl:choose>
	<xsl:when test="$language2='en-US'">English</xsl:when>
	<xsl:when test="$language2='es-US'">Spanish</xsl:when>
	</xsl:choose></td>

	<!-- ** Marital Status ** -->
	<td width='50%' align='left' valign="top"><b><xsl:text>Marital Status: </xsl:text></b>
	<xsl:variable name="marital2" select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole/n1:patient/n1:maritalStatusCode/@code"/>
	<xsl:choose>
	<xsl:when test="$marital2='S'">Single</xsl:when>
	<xsl:when test="$marital2='M'">Married</xsl:when>
	<xsl:when test="$marital2='L'">Legally Separated</xsl:when>
	<xsl:when test="$marital2='T'">Domestic partner</xsl:when>
	<xsl:when test="$marital2='D'">Divorced</xsl:when>
	<xsl:when test="$marital2='A'">Annulled</xsl:when>
	<xsl:when test="$marital2='W'">Widowed</xsl:when>
	<xsl:when test="$marital2='P'">Polygamous</xsl:when>
	</xsl:choose></td>
	</tr>
	<tr>

	<!-- ** Religion ** -->
	<td width='50%' align='left' valign="top"><b><xsl:text>Religion: </xsl:text></b>
	<xsl:variable name="religion" select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole/n1:patient/n1:religiousAffiliationCode/@code"/>
	<xsl:choose>
	<xsl:when test="$religion='1001'">Adventist</xsl:when>
	<xsl:when test="$religion='1002'">Africian Religions</xsl:when>
	<xsl:when test="$religion='1003'">Afro-Caribeean Religions</xsl:when>
	<xsl:when test="$religion='1004'">Agnosticism</xsl:when>
	<xsl:when test="$religion='1005'">Anglican</xsl:when>
	<xsl:when test="$religion='1006'">Animism</xsl:when>
	<xsl:when test="$religion='1007'">Atheism</xsl:when>
	<xsl:when test="$religion='1008'">BabiBahaI faiths</xsl:when>
	<xsl:when test="$religion='1009'">Baptist</xsl:when>
	<xsl:when test="$religion='1010'">Bon</xsl:when>
	<xsl:when test="$religion='1020'">Hinduism</xsl:when>
	<xsl:when test="$religion='1021'">Humanism</xsl:when>
	<xsl:when test="$religion='1023'">Islam</xsl:when>
	<xsl:when test="$religion='1026'">Judaism</xsl:when>
	<xsl:when test="$religion='1027'">Latter Day Saints</xsl:when>
	<xsl:when test="$religion='1028'">Lutheran</xsl:when>
	<xsl:when test="$religion='1036'">Orthodox</xsl:when>
	<xsl:when test="$religion='1037'">Paganism</xsl:when>
	<xsl:when test="$religion='1041'">Roman Catholic Church</xsl:when>
	<xsl:when test="$religion='1043'">Scientology</xsl:when>
	<xsl:when test="$religion='1057'">Wicca</xsl:when>
	<xsl:when test="$religion='1058'">Yaohushua</xsl:when>
	<xsl:when test="$religion='1059'">Zen Buddhism</xsl:when>
	<xsl:when test="$religion='1072'">Full Gospel</xsl:when>
	<xsl:when test="$religion='1074'">Native American</xsl:when>
	<xsl:when test="$religion='1077'">Protestant</xsl:when>
	</xsl:choose></td>
	<td width='50%' align='left' valign="top"><b><xsl:text>Race: </xsl:text></b><xsl:value-of select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole/n1:patient/n1:raceCode/@displayName"/></td>
	</tr>

	<!-- ** Occupation ** -->
	<tr>
	<td width='50%' align='left' valign="top"><b><xsl:text>Occupation: </xsl:text></b>
	<xsl:value-of select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.15']/n1:entry/n1:observation/n1:value/@displayName"/>
	</td>
	</tr>
	</table>
	
	<br />
	
	<!-- ****************************************************************  -->
	<!-- *************** Health Summary - Table of Contents *************  -->
	<!-- ****************************************************************  --> 
   <!-- Removing table of contents due to section not being picked up.  February 24, 2009 - Shane Rossman-->
       <!--<div>
        <h3><a name="toc">Table of Contents</a></h3>
        <ul>
        <xsl:for-each select="n1:documentationOf/n1:serviceEvent/n1:performer/n1:templateId/@root="2.16.840.1.113883.3.88.11.32.4"" | "n1:component/n1:structuredBody/n1:component/n1:section/n1:title">
		
		<li><a href="#{generate-id(.)}"><xsl:value-of select="."/></a></li>
        </xsl:for-each>
        </ul>
        </div>
	-->
<hr/>
	
 </xsl:template>	
 </xsl:stylesheet>