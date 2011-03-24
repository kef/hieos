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

<xsl:template name="COMPLETE_FAMILY_HISTORY">
	<xsl:if test="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component[n1:section/n1:templateId/@root='2.16.840.1.113883.10.20.1.4']">
	<!-- *******************************************************  -->
	<!-- ************* Complete - Family History ***************  -->
	<!-- *******************************************************  -->

	<h4 align='center'><b><xsl:text>Family History</xsl:text></b></h4>
	
	<xsl:variable name="familyXpath_CR" select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component[n1:section/n1:templateId/@root='2.16.840.1.113883.10.20.1.4']/n1:section/n1:entry" />
	<xsl:for-each select="$familyXpath_CR">

	<table border="0" cellpadding="0" width="100%">
	<!-- ** Header ** -->
	<tr>
	<td width='25%' align='left' valign="top"><b><xsl:text>Name</xsl:text></b></td>
	<td width='25%' align='left' valign="top"><b><xsl:text>Relationship</xsl:text></b></td>
	<td width='50%' align='left' valign="top"><b><xsl:text>Medical History</xsl:text></b></td>
	</tr>
	
	
	
	<xsl:choose>
		<xsl:when test="./n1:organizer">
	<tr>
	
	<!-- ** Organizer - Name ** -->
	<td width='25%' align='left' valign="top">
	<xsl:value-of select="./n1:organizer/n1:subject/n1:relatedSubject/n1:subject/n1:name"/> 
	<br />
	<xsl:call-template name="getAddress"> 
	<xsl:with-param name="addr" select="./n1:organizer/n1:subject/n1:relatedSubject/n1:addr"/>
	</xsl:call-template>

	</td>
	<!-- ** Organizer - Relationship ** -->
	<td width='25%' align='left' valign="top"><xsl:value-of select="./n1:organizer/n1:subject/n1:relatedSubject/n1:code/@displayName"/></td>
	
	<!-- ** Organizer - Medical History ** -->
	<td width='50%' align='left' valign="top">
	<xsl:for-each select="./n1:organizer/n1:component/n1:observation/n1:value">
	<xsl:value-of select=".//@displayName"/><br />
	</xsl:for-each></td>
	
	</tr>
	
	<!-- ** Organizer - Sex ** -->
	<tr><td width='25%' align='left' valign="top"><b><xsl:text>Sex: </xsl:text></b><xsl:variable name="sex4" select="./n1:organizer/n1:subject/n1:relatedSubject/n1:subject/n1:administrativeGenderCode/@code"/>
	<xsl:choose>
	<xsl:when test="$sex4='M'">Male</xsl:when>
	<xsl:when test="$sex4='F'">Female</xsl:when>
	<xsl:when test="$sex4='UN'">Undifferentiated</xsl:when>
	</xsl:choose></td>
		
	<!-- ** Organizer -DOB ** -->
	<td width='25%' valign="top"><b><xsl:text>DOB: </xsl:text></b>
	<xsl:call-template name="formatDate">
	<xsl:with-param name="date" select="./n1:organizer/n1:subject/n1:relatedSubject/n1:subject/n1:birthTime/@value"/>
	</xsl:call-template></td>
	</tr>
	
	<!-- ** Search for Family History Death Observation OID ** -->
   <xsl:if test="./n1:organizer/n1:component/n1:observation/n1:templateId/@root ='2.16.840.1.113883.10.20.1.42'">
	<tr>

	<!-- ** Organizer -Deceased ** -->
	<xsl:if test="(not(./n1:organizer/n1:subject/n1:relatedSubject/n1:subject/sdtc:deceasedInd/@value) = '')">
	<td width='25%' valign="top"><b><xsl:text>Deceased: </xsl:text></b>
	<xsl:variable name="deceased2" select="./n1:organizer/n1:subject/n1:relatedSubject/n1:subject/sdtc:deceasedInd/@value"/>
	<xsl:choose>
	<xsl:when test="$deceased2='1'">Yes</xsl:when>
	<xsl:when test="$deceased2='2'">No</xsl:when>
	</xsl:choose>
	</td>
	</xsl:if>
	
	<!-- ** Deceased Date ** -->
	<xsl:if test="(not(./n1:organizer/n1:subject/n1:relatedSubject/n1:subject/sdtc:deceasedTime/@value) = '')">
	<td width='25%' align='left' valign="top"><b><xsl:text>Deceased Date: </xsl:text></b>
	<xsl:call-template name="formatDate">
	<xsl:with-param name="date" select="./n1:organizer/n1:subject/n1:relatedSubject/n1:subject/sdtc:deceasedTime/@value"/>
	</xsl:call-template></td>
	</xsl:if>
	
	</tr>
	
	<td width='25%' valign="top"></td>
	
	<!-- ** Organizer -Cause of Death ** -->
	<xsl:if test="(not(./n1:organizer/n1:component/n1:observation/n1:value/@displayName) = '')">
	<tr>
	<td width='25%' align='left' valign="top"><b><xsl:text>Cause of Death: </xsl:text></b>
	<xsl:value-of select="./n1:organizer/n1:component/n1:observation[n1:templateId/@root='2.16.840.1.113883.10.20.1.42']/n1:value/@displayName"/>
	</td>
	</tr>
	</xsl:if>
	
   </xsl:if>
		</xsl:when>
		




		<xsl:when test="./n1:observation">
	<tr>	
	
	<!-- ** Observation - Name ** -->

	<td width='25%' align='left' valign="top">
	<xsl:value-of select="./n1:observation/n1:subject/n1:relatedSubject/n1:subject/n1:name"/> 
	<br />
	<xsl:call-template name="getAddress"> 
	<xsl:with-param name="addr" select="./n1:observation/n1:subject/n1:relatedSubject/n1:addr"/>
	</xsl:call-template>
	
	</td>
	<!-- ** Observation - Relationship ** -->
	<td width='25%' align='left' valign="top"><xsl:value-of select="./n1:observation/n1:subject/n1:relatedSubject/n1:code/@displayName"/></td>
		
	<!-- ** Observation - Medical History ** -->
	<td width='50%' align='left' valign="top">
	<xsl:value-of select="./n1:observation/n1:value/@displayName"/><br /></td>
	</tr>
	<!-- ** Observation - Sex ** -->
	<tr><td width='25%' align='left' valign="top"><b><xsl:text>Sex: </xsl:text></b><xsl:variable name="sex3" select="./n1:observation/n1:subject/n1:relatedSubject/n1:subject/n1:administrativeGenderCode/@code"/>
	<xsl:choose>
	<xsl:when test="$sex3='M'">Male</xsl:when>
	<xsl:when test="$sex3='F'">Female</xsl:when>
	<xsl:when test="$sex3='UN'">Undifferentiated</xsl:when>
	</xsl:choose></td>
		
	<!-- ** Observation - DOB ** -->
	<td width='25%' valign="top"><b><xsl:text>DOB: </xsl:text></b>
	<xsl:call-template name="formatDate">
	<xsl:with-param name="date" select="./n1:observation/n1:subject/n1:relatedSubject/n1:subject/n1:birthTime/@value"/>
	</xsl:call-template></td>
	</tr>
	
	<!-- ** Search for Family History Death Observation OID ** -->
<xsl:if test="./n1:observation/n1:templateId/@root ='2.16.840.1.113883.10.20.1.42'">
	
	<tr>
	<!-- ** Observation -Deceased ** -->
	<xsl:if test="(not(./n1:observation/n1:subject/n1:relatedSubject/n1:subject/sdtc:deceasedInd/@value) = '')">
	<td width='25%' valign="top"><b><xsl:text>Deceased: </xsl:text></b>
	<xsl:variable name="deceased" select="./n1:observation/n1:subject/n1:relatedSubject/n1:subject/sdtc:deceasedInd/@value"/>
	<xsl:choose>
	<xsl:when test="$deceased='1'">Yes</xsl:when>
	<xsl:when test="$deceased='2'">No</xsl:when>
	</xsl:choose>
	</td>
	</xsl:if>
	

	<!-- ** Deceased Date ** -->
	<xsl:if test="(not(./n1:observation/n1:subject/n1:relatedSubject/n1:subject/sdtc:deceasedTime/@value) = '')">
	<td width='25%' align='left' valign="top"><b><xsl:text>Deceased Date: </xsl:text></b>
	<xsl:call-template name="formatDate">
	<xsl:with-param name="date" select="./n1:observation/n1:subject/n1:relatedSubject/n1:subject/sdtc:deceasedTime/@value"/>
	</xsl:call-template>
	</td>
	</xsl:if>
	</tr>
	
	<!-- ** Observation -Cause of Death ** -->
	<xsl:if test="(not(./n1:observation/n1:value/@displayName) = '')">
	<tr>
	<td width='25%' align='left' valign="top"><b><xsl:text>Cause of Death: </xsl:text></b>
	<xsl:value-of select="./n1:observation/n1:value/@displayName"/>
	</td>
	</tr>
	</xsl:if>	
	
	</xsl:if>
	
		</xsl:when>
	</xsl:choose>
	</table>

	<xsl:if test="position() != last()" >
		<tr>
		<td><hr width="75%"  noshade="false" /></td>

		</tr>
	</xsl:if>
	</xsl:for-each>


<hr/>


	</xsl:if>
 </xsl:template>	
 </xsl:stylesheet>