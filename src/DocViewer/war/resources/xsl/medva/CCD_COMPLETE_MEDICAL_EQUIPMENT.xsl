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

<xsl:template name="COMPLETE_MEDICAL_EQUIPMENT">
	<xsl:if test="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.7']">
	<!-- *******************************************************  -->
	<!-- ********** Complete - Medical Equipment ***************  -->
	<!-- *******************************************************  -->
	
	<h4 align='center'><b><xsl:text>Medical Equipment</xsl:text></b></h4>
	

	<xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.7']/n1:entry/n1:supply">
	<xsl:sort select="(./n1:effectiveTime/n1:low/@value)|(./n1:effectiveTime/@value)" order="descending"/>
	
	<xsl:if test="position() > 1" >
		<tr>
		<td><hr width="75%"  noshade="false" /></td>
		</tr>
	</xsl:if>

	<table border="0" cellpadding="0" width="100%">

	<tr>
	
	<!-- ** Date Issued ** -->
	<td width='25%' align='left' valign="top">
	<b><xsl:text>Date Issued: </xsl:text></b>
	<xsl:choose>
		<xsl:when test="./n1:effectiveTime/@value">
			<xsl:call-template name="formatDate">
				<xsl:with-param name="date" select="./n1:effectiveTime/@value" />
			</xsl:call-template>
		</xsl:when>
		<xsl:when test="./n1:effectiveTime/n1:low/@value">
			<xsl:call-template name="formatDate">
				<xsl:with-param name="date" select="./n1:effectiveTime/n1:low/@value" />
			</xsl:call-template>
		</xsl:when>
		<xsl:otherwise>
			<xsl:text>No date available</xsl:text>
		</xsl:otherwise>
	</xsl:choose>
	</td>


	<!-- ** Device Equipment ** -->
	<td width='25%' align='left' valign="top">
	<b><xsl:text>Device Equipment: </xsl:text></b>
	<xsl:value-of select="./n1:product/n1:manufacturedProduct/n1:manufacturedMaterial/n1:name"/>
	</td>
	
	
	<!-- ** Facility ** -->
	<td width='25%' align='left' valign="top">
	<b><xsl:text>Facility: </xsl:text></b>
	<xsl:value-of select="./n1:performer/n1:assignedEntity/n1:representedOrganization/n1:name"/>
	</td>
	
	<!-- ** Provider ** -->
	<td width='25%' align='left' valign="top">
	<b><xsl:text>Provider: </xsl:text></b>
	<xsl:value-of select="./n1:performer/n1:assignedEntity/n1:assignedPerson/n1:name/n1:prefix"/>
	<xsl:text> </xsl:text>
	<xsl:value-of select="./n1:performer/n1:assignedEntity/n1:assignedPerson/n1:name/n1:given"/>
	<xsl:text> </xsl:text>
	<xsl:value-of select="./n1:performer/n1:assignedEntity/n1:assignedPerson/n1:name/n1:family"/>
	</td>
	
	</tr>

	<tr>
	<!-- ** Quantity ** -->
	<td width='25%' align='left' valign="top">
	<b><xsl:text>Quantity: </xsl:text></b>
	<xsl:value-of select="./n1:quantity/@value"/>
	</td>
	
	<!-- ** Time Range ** -->
	<td width='25%' align='left' valign="top">
	<b><xsl:text>Time Range: </xsl:text></b>
	<xsl:if test= "not(./n1:expectedUseTime/n1:low/@value) =''">
		<xsl:call-template name="formatDate">
			<xsl:with-param name="date" select="./n1:expectedUseTime/n1:low/@value"/>
		</xsl:call-template>
	
		<xsl:if test= "not(./n1:expectedUseTime/n1:high/@value) =''">
			<xsl:text> - </xsl:text>
			<xsl:call-template name="formatDate">
				<xsl:with-param name="date" select="./n1:expectedUseTime/n1:high/@value"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:if>
	</td>

	<!-- ** Status ** -->	
	<td width='25%' align='left' valign="top">
	<b><xsl:text>Status: </xsl:text></b>
	<xsl:value-of select="./n1:entryRelationship/n1:observation/n1:value/@displayName"/>
	</td>

	</tr>
	

	</table>
	
	</xsl:for-each>

	<!-- *********************************************************************  -->
	<!-- ********************* Narrative Section *****************************  -->
	<!-- *********************************************************************  -->
		
	<!-- Call HTML table formatting -->
	<xsl:if test="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.7']/n1:text/n1:table" >
	<xsl:apply-templates select="n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.7']/n1:text/n1:table"/>
	</xsl:if>
	
		
	<!-- *********************************************************************  -->
	<!-- ********************* Comments Section ******************************  -->
	<!-- *********************************************************************  -->
	<xsl:choose>
		<xsl:when test="(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.7']//n1:code[contains (@code, '48767-8')])and(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.7']/n1:text !='')">
			<h4 align='center'><b><xsl:text>Comments</xsl:text></b></h4>
			<xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.7']//n1:code[contains (@code, '48767-8')]" >	
				<xsl:variable name="medEquipComment" select="substring(../n1:text/n1:reference/@value, 2)" />
				<xsl:if test="$medEquipComment = (/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.7']/n1:text/n1:content/@ID)">
					<xsl:value-of select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.7']/n1:text/n1:content[contains (@ID, $medEquipComment)]"/>
				<br/>
				</xsl:if>
			</xsl:for-each>
		</xsl:when>
	</xsl:choose>
	
	
<hr />
	</xsl:if>
 </xsl:template>	
 </xsl:stylesheet>