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

<xsl:template name="COMPLETE_IMMUNIZATION">
	<xsl:if test="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.6']">
	<!-- *******************************************************  -->
	<!-- ************* Complete - Immunizations ****************  -->
	<!-- *******************************************************  -->

	<h4 align='center'><b><xsl:text>Immunization</xsl:text></b></h4>
	

	<xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.6']/n1:entry/n1:substanceAdministration">
	<xsl:sort select="./n1:substanceAdministration/n1:effectiveTime/@value" order="descending" />
	
	<xsl:if test="position() > 1" >
		<tr>
		<td><hr width="75%"  noshade="false" /></td>
		</tr>
	</xsl:if>
	
	<h4 align='center'><b><xsl:text>Immunization Event Entry</xsl:text></b></h4>
	
	
	<table border="0" cellpadding="0" width="100%">
	<tr>
	
	<!-- ** Refusal Flag ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Refusal Flag: </xsl:text></b>
	<xsl:value-of select="./@negationInd" />
	</td>

	<!-- ** Administrated Date ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Administrated Date: </xsl:text></b>
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

	<!-- ** Medication Series Number ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Medication Series Number: </xsl:text></b>
	<xsl:value-of select="./n1:entryRelationship[@typeCode='SUBJ']/n1:observation/n1:value/@value" />
	</td>
	</tr>
	
	<tr>
	
	<!-- ** Reaction ** -->	
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Reaction: </xsl:text></b>
	<xsl:for-each select="./n1:entryRelationship[@typeCode = 'CAUS']/n1:observation[n1:templateId/@root = '2.16.840.1.113883.10.20.1.54']">
		<xsl:variable name="reactionTextValue" select="substring(./n1:text/n1:reference/@value, 2)"/>
		<xsl:if test="$reactionTextValue = (../../../../n1:text/n1:content/@ID)">
			<xsl:value-of select="../../../../n1:text/n1:content[contains (@ID, $reactionTextValue)]"/>									 
		</xsl:if>
		<xsl:if test="position() != last()">
			<xsl:text> / </xsl:text>
		</xsl:if>								
		</xsl:for-each>
	</td>

	<!-- ** Performer ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Performer: </xsl:text></b>
	<xsl:value-of select="./n1:performer/n1:assignedEntity/n1:representedOrganization/n1:name" />
	</td>
	</tr>
	
	</table>
	
	<h4 align='center'><b><xsl:text>Medication Information</xsl:text></b></h4>
	
	
	<table border="0" cellpadding="0" width="100%">
	<tr>
	
	<!-- ** Coded Product Name ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Coded Product Name: </xsl:text></b>
	<xsl:value-of select="./n1:consumable/n1:manufacturedProduct/n1:manufacturedMaterial/n1:code/@displayName" />
	</td>	
	
	<!-- ** Free Text Product Name ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Free Text Product Name: </xsl:text></b>
	<xsl:value-of select="./n1:consumable/n1:manufacturedProduct/n1:manufacturedMaterial/n1:code/n1:originalText" />
	</td>		

	<!-- ** Drug Manufacturer  ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Drug Manufacturer: </xsl:text></b>
	<xsl:value-of select="./n1:consumable/n1:manufacturedProduct/n1:manufacturerOrganization/n1:name" />
	</td>		
	</tr>
	
	<tr>

	<!-- ** Lot Number ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Lot Number: </xsl:text></b>
	<xsl:value-of select="./n1:consumable/n1:manufacturedProduct/n1:manufacturedMaterial/n1:lotNumberText" />
	</td>	
	
	<!-- ** Refusal Reason ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Refusal Reason: </xsl:text></b>
	<xsl:value-of select="./n1:entryRelationship[@typeCode = 'RSON']//n1:observation[n1:templateId/@root ='2.16.840.1.113883.10.20.1.28']/n1:code/@displayName" />
	</td>	
	</tr>
	
	</table>
	
	</xsl:for-each>
	
	<!-- Call HTML table formatting -->
	<xsl:if test="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.6']/n1:text/n1:table" >
	<xsl:apply-templates select="n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.6']/n1:text/n1:table"/>
	</xsl:if>
	
	<!-- *********************************************************************  -->
	<!-- ********************* Comments Section ******************************  -->
	<!-- *********************************************************************  -->
	<xsl:choose>
		<xsl:when test="(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.6']//n1:code[contains (@code, '48767-8')])and(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.6']/n1:text !='')">
			<h4 align='center'><b><xsl:text>Comments</xsl:text></b></h4>
			<xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.6']//n1:code[contains (@code, '48767-8')]" >	
				<xsl:variable name="immComment" select="substring(../n1:text/n1:reference/@value, 2)" />
				<xsl:if test="$immComment = (/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.6']/n1:text/n1:content/@ID)">
					<xsl:value-of select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.6']/n1:text/n1:content[contains (@ID, $immComment)]"/>
				<br/>
				</xsl:if>
			</xsl:for-each>
		</xsl:when>
	</xsl:choose>
	
<hr />	
</xsl:if>
</xsl:template>	
</xsl:stylesheet>
	