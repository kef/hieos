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

<!-- ** Date Range Requested (Java) ** 
<xsl:param name="requestedDateRangeText"/>

<xsl:template name="getRequestedDateRange"> 
   	<xsl:variable name="retrievedDate" select="$requestedDateRangeText"/>
	<xsl:value-of select="$retrievedDate" />
</xsl:template>-->

<!-- ** Received From: (Java) ** -->
<xsl:param name="hieName"/> <br />

<xsl:template name="getReceivedFrom"> 
   	<xsl:variable name="receivedFrom" select="$hieName"/>
	<xsl:value-of select="$receivedFrom" />
</xsl:template>

<xsl:template name="COVERPAGE">

	

	<!-- ** Patient Information Location Variable ** -->
	<xsl:variable name="patientRole" select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole"/> 


	<!-- ** HIT Document Title ** -->
	<h1 align='center' style='font-size:.25in;'><b><xsl:text>MedVirginia</xsl:text></b></h1>
	


<!-- ** HIT Disclaimer ** -->
	<xsl:text>NOTE: This C32 may not constitute a complete record.</xsl:text><br /><br />

	<hr /><hr />
	
	<h4 align='center'>
	<xsl:if test="/n1:ClinicalDocument/n1:title"><xsl:value-of select="/n1:ClinicalDocument/n1:title" /><br /></xsl:if>
	
	<!-- ** Received From: ** -->
	<xsl:text>Received From: </xsl:text>
	 <xsl:call-template name="getReceivedFrom" /><br />
	</h4>

	<!-- ** Date Range Requested ** 
	<b><xsl:text>Date Range Requested: </xsl:text></b> <xsl:call-template name="getRequestedDateRange" /><br /><br />-->
	
	<!-- ** Document Creation Date ** -->
	<b><xsl:text>Date source document created: </xsl:text></b>
		<xsl:call-template name="formatDate">
		<xsl:with-param name="date" select="/n1:ClinicalDocument/n1:effectiveTime/@value"/>
		</xsl:call-template><br /><br />




	<!-- ** Date Range of MER ** 
	<b><xsl:text>Date Range of MER: </xsl:text></b>
		<xsl:choose>
			<xsl:when test="(/n1:ClinicalDocument/n1:documentationOf/n1:serviceEvent/n1:effectiveTime/n1:low/@value != '')and(/n1:ClinicalDocument/n1:documentationOf/n1:serviceEvent/n1:effectiveTime/n1:high/@value != '')">
				<xsl:call-template name="formatDate">
					<xsl:with-param name="date" select="/n1:ClinicalDocument/n1:documentationOf/n1:serviceEvent/n1:effectiveTime/n1:low/@value" />
				</xsl:call-template>
				<xsl:text> - </xsl:text>
				<xsl:call-template name="formatDate">
					<xsl:with-param name="date" select="/n1:ClinicalDocument/n1:documentationOf/n1:serviceEvent/n1:effectiveTime/n1:high/@value" />
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="(not(/n1:ClinicalDocument/n1:documentationOf/n1:serviceEvent/n1:effectiveTime/n1:low/@value))and(/n1:ClinicalDocument/n1:documentationOf/n1:serviceEvent/n1:effectiveTime/n1:high/@value != '')">
				<xsl:text>No start date provided</xsl:text>
				<xsl:text> - </xsl:text>
				<xsl:call-template name="formatDate">
					<xsl:with-param name="date" select="/n1:ClinicalDocument/n1:documentationOf/n1:serviceEvent/n1:effectiveTime/n1:high/@value" />
				</xsl:call-template>	
			</xsl:when>
			<xsl:when test="(/n1:ClinicalDocument/n1:documentationOf/n1:serviceEvent/n1:effectiveTime/n1:low/@value != '')and(not(/n1:ClinicalDocument/n1:documentationOf/n1:serviceEvent/n1:effectiveTime/n1:high/@value))">
				<xsl:call-template name="formatDate">
					<xsl:with-param name="date" select="/n1:ClinicalDocument/n1:documentationOf/n1:serviceEvent/n1:effectiveTime/n1:low/@value" />
				</xsl:call-template>
				<xsl:text> - </xsl:text>
				<xsl:text>No end date provided</xsl:text>
			</xsl:when>			
			<xsl:when test="(not(/n1:ClinicalDocument/n1:documentationOf/n1:serviceEvent/n1:effectiveTime/n1:low/@value))and(not(/n1:ClinicalDocument/n1:documentationOf/n1:serviceEvent/n1:effectiveTime/n1:high/@value))">
				<xsl:text>No date range information provided</xsl:text>
			</xsl:when>
		</xsl:choose><br /><br />-->
		
	
        <!-- *******************************************  -->
	<!-- **************** Patient ******************  -->
	<!-- *******************************************  -->   

	<table border="1" cellpadding="0" width="100%">


	
	<!-- ** Patient Name variable ** -->
	<xsl:variable name="patientRole1" select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole"/>

	<!-- ** Name ** -->
	<tr>
	<td width='50%' valign="top"><b><xsl:call-template name="getName">
	<xsl:with-param name="name" select="$patientRole/n1:patient/n1:name"/>
	</xsl:call-template></b>
	<br />

	<!-- ** Address ** -->		 		 		 		 
	<xsl:if test="$patientRole/n1:addr !=''">
	<xsl:call-template name="getAddress"> 
	<xsl:with-param name="addr" select="$patientRole/n1:addr"/>
	</xsl:call-template>
	<br />
	</xsl:if>

	<!-- ** Telephone ** -->
	<xsl:if test="$patientRole/n1:telecom">
	<xsl:call-template name="getTelecom"> 
	<xsl:with-param name="telecom" select="$patientRole/n1:telecom"/>
	</xsl:call-template>
	</xsl:if>
	
	<!-- ** Email Address // This matches the string in the first argument with a regular expression. I'm waiting on Bharat to put in new Java code
				that will handle the matches() function. 
	<xsl:choose>
		<xsl:when test="matches ('$patientRole/n1:telecom/@value','[\w-]+@([\w-]+\.)+[\w-]+')" >
			<xsl:text>MAGIC!"</xsl:text>
		</xsl:when>
		<xsl:otherwise>
			<xsl:text>No Magic...</xsl:text>
		</xsl:otherwise>
	</xsl:choose> -->
	
	
	<!-- ** Vacation Address (if present) -->
	<xsl:if test="$patientRole/n1:addr[contains(@use,'HV')]">
	<br /><br /><b>Vacation Address</b><br />
	<xsl:call-template name="getAddress"> 
	<xsl:with-param name="addr" select="$patientRole/n1:addr[contains(@use,'HV')]"/>
	</xsl:call-template>
	</xsl:if></td>

	<!-- ** SSN ** -->		 		    
	<td width='50%' align='left' valign="top"><b><xsl:text>SSN: </xsl:text></b>
	<xsl:value-of select="$patientRole/n1:id/@extension"/></td>		 		        
	</tr>

	<!-- ** DOB ** -->		 		    
	<tr><td width='50%' valign="top"><b><xsl:text>DOB: </xsl:text></b>
	<xsl:call-template name="formatDate">
	<xsl:with-param name="date" select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole/n1:patient/n1:birthTime/@value"/>
	</xsl:call-template></td>

	<!-- ** Sex ** -->					
	<td width='50%' align='left' valign="top"><b><xsl:text>Sex: </xsl:text></b>
	<xsl:variable name="sex" select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole/n1:patient/n1:administrativeGenderCode/@code"/>
	<xsl:choose>
	<xsl:when test="$sex='M'">Male</xsl:when>
	<xsl:when test="$sex='F'">Female</xsl:when>
	<xsl:when test="$sex='UN'">Undifferentiated</xsl:when>
	</xsl:choose></td>		 		        
	</tr>
	</table>

	<!-- ** Language ** -->					
	<table border="thin solid" cellpadding="0" width="100%">
	<tr><td width='100%' align='left' valign="top"><b><xsl:text>Language: </xsl:text></b>
	<xsl:variable name="language" select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole/n1:patient/n1:languageCommunication/n1:languageCode/@code"/>
	<xsl:choose>
	<xsl:when test="$language='en-US'">English</xsl:when>
	<xsl:when test="$language='es-US'">Spanish</xsl:when>
	<xsl:when test="$language='sgn-US'">American Sign Language</xsl:when>
	</xsl:choose></td></tr>
	</table>
	
	<!-- <xsl:if test="/n1:ClinicalDocument//n1:value[contains (@code, '77386006')]">
	<br /><b><u><xsl:text>Note: This patient is currently pregnant</xsl:text></u></b>
	<br /><br /></xsl:if>	-->			
	

</xsl:template>	
</xsl:stylesheet>