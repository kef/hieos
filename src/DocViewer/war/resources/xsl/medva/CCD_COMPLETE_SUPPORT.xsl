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
 xmlns:fo="http://www.w3.org/1999/XSL/Format"
 extension-element-prefixes="java">
 
<xsl:include href="./CCD_FUNCTIONS.xsl"/>
 
<xsl:output method="html" indent="yes" version="4.01" encoding="ISO-8859-1" doctype-public="-//W3C//DTD HTML 4.01//EN"/> 

<xsl:template name="COMPLETE_SUPPORT">

	<!-- ********************************************************************  -->
	<!-- ********************** Health Summary - Support ********************  -->
	<!-- ********************************************************************  -->
	<xsl:choose>
	<xsl:when test="/n1:ClinicalDocument/n1:participant/n1:associatedEntity/n1:associatedPerson/n1:name != ''">
	<h4 align='center'><b><xsl:text>Contacts</xsl:text></b></h4>
	<table border="0" cellpadding="0" width="100%">

	<!-- ** Header ** -->
	<tr>
	<td width='20%' align='left' valign="top"><b><xsl:text>Support Date</xsl:text></b></td>
	<td width='20%' align='left' valign="top"><b><xsl:text>Contact Name</xsl:text></b></td>
	<td width='20%' align='left' valign="top"><b><xsl:text>Contact Type</xsl:text></b></td>
	<td width='20%' align='left' valign="top"><b><xsl:text>Relationship</xsl:text></b></td>
	<td width='20%' align='left' valign="top"><b><xsl:text>Contact Address</xsl:text></b></td>
	</tr>
	
	<!-- ************************************************** -->
	<!-- **********	This section is for Guardians ********* -->
	<!-- ************************************************** -->
	<xsl:for-each select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole/n1:patient/n1:guardian">
	<tr>
	
	<!-- ** Date ** -->		 		    
	<td></td>

		
	<td>
	<!-- ** Support Name ** -->
	<xsl:call-template name="getName">
	<xsl:with-param name="name" select="./n1:guardianPerson/n1:name"/>
	</xsl:call-template>
	</td>
	
	<!-- ** Contact Type ** -->
	<td>
	<xsl:text>Guardian</xsl:text>
	</td>
	

	<!-- ** Relationship variable ** -->
	<td>
	<xsl:call-template name="famLookup">
	<xsl:with-param name="relationship" select="../n1:guardian/n1:code/@code" />
	</xsl:call-template>
	</td>

	<!-- ** Support Address ** -->
	<td>
	<xsl:if test="../n1:guardian/n1:addr !=''">
	<xsl:call-template name="getAddress"> 
	<xsl:with-param name="addr" select="../n1:guardian/n1:addr"/>
	</xsl:call-template>
	<br />
	</xsl:if>

	<!-- ** Support Telephone ** -->
	<xsl:if test="../n1:guardian/n1:telecom">
	<xsl:call-template name="getTelecom"> 
	<xsl:with-param name="telecom" select="../n1:guardian/n1:telecom"/>
	</xsl:call-template>
	</xsl:if>
	
	<!-- ** Support Email Address ** -->
					
	<xsl:for-each select="../n1:guardian/n1:telecom">
		<xsl:call-template name="matchPattern">
			<xsl:with-param name="input" select="./@value" />
			<xsl:with-param name="expression" select="'mailto:[\w\-\.]+@([\w-]+\.)+[\w-]+'"/>
		</xsl:call-template>
	</xsl:for-each>
	
	<!-- ** URL ** -->
			
	<xsl:for-each select="../n1:guardian/n1:telecom">
		<xsl:call-template name="matchPattern">
			<xsl:with-param name="input" select="./@value" />
			<xsl:with-param name="expression" select="'(http|https|ftp)://([\w-]+\.)+[\w-]+(/[\w- ./?%=]*)?'" />
		</xsl:call-template>
	</xsl:for-each>
	
	</td>
	</tr>
	</xsl:for-each>
	
	<!-- ************************************************** -->
	<!-- ** This section is for Regular Support Contacts ** -->
	<!-- ************************************************** -->
	<xsl:for-each select="/n1:ClinicalDocument/n1:participant">
	<tr>
	
	<!-- ** Support Date -->
	<td>
	<xsl:call-template name="formatDate">
	<xsl:with-param name="date" select="(/n1:ClinicalDocument/n1:participant/n1:time/n1:low/@value)|(/n1:ClinicalDocument/n1:participant/n1:time/@value)"/>
	</xsl:call-template>
	</td>
	
	<td>	
	<!-- ** Support Name ** -->
	<xsl:call-template name="getName">
	<xsl:with-param name="name" select="./n1:associatedEntity/n1:associatedPerson/n1:name"/>
	</xsl:call-template>
	</td>

	<!-- ** Contact Type ** -->
	<td>
	<xsl:call-template name="contactTypeLookup">
	<xsl:with-param name="contactType" select="./n1:associatedEntity/@classCode" />
	</xsl:call-template>
	</td>

	<!-- ** Relationship variable ** -->
	<td>
	<xsl:call-template name="famLookup">
	<xsl:with-param name="relationship" select="./n1:associatedEntity/n1:code/@code" />
	</xsl:call-template>
	</td>

	<!-- ** Support Address ** -->
	<td>
	<xsl:if test="./n1:associatedEntity/n1:addr !=''">
	<xsl:call-template name="getAddress"> 
	<xsl:with-param name="addr" select="./n1:associatedEntity/n1:addr"/>
	</xsl:call-template>
	<br />
	</xsl:if>

	<!-- ** Support Telephone ** -->
	<xsl:if test="./n1:associatedEntity/n1:telecom">
	<xsl:call-template name="getTelecom"> 
	<xsl:with-param name="telecom" select="./n1:associatedEntity/n1:telecom"/>
	</xsl:call-template>
	</xsl:if>
		
	<!-- ** Support Email Address ** -->
	<xsl:for-each select="./n1:associatedEntity/n1:telecom">
		<xsl:call-template name="matchPattern">
			<xsl:with-param name="input" select="./@value" />
			<xsl:with-param name="expression" select="'mailto:[\w\-\.]+@([\w-]+\.)+[\w-]+'"/>
		</xsl:call-template>
		<br />
	</xsl:for-each>
	
	<!-- ** URL ** -->
			
	<xsl:for-each select="./n1:associatedEntity/n1:telecom">
		<xsl:call-template name="matchPattern">
			<xsl:with-param name="input" select="./@value" />
			<xsl:with-param name="expression" select="'(http|https|ftp)://([\w-]+\.)+[\w-]+(/[\w- ./?%=]*)?'" />
		</xsl:call-template>
		<br />
	</xsl:for-each>
	
	</td>
	</tr>
	</xsl:for-each>
	</table>
	
	<hr />
	</xsl:when>
	
	</xsl:choose>

 </xsl:template>	
 </xsl:stylesheet>