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

<xsl:template name="COMPLETE_ALLERGY">
	<xsl:if test="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.2']">
	<!-- **************************************************************  -->
	<!-- ****************** Complete - Allergies **********************  -->
	<!-- **************************************************************  -->
	
	<h4 align='center'><b><xsl:text>Allergy and Sensitivity</xsl:text></b></h4>
		

	<xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.2']/n1:entry/n1:act">
	<xsl:sort select="./n1:entryRelationship/n1:observation/n1:effectiveTime/n1:low/@value" order="descending"/>
	
	<table border="0" cellpadding="0" width="100%">
	
	<tr>
	<!-- ** Event Date ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Event Date: </xsl:text></b>
	<xsl:choose>
		<xsl:when test="./n1:entryRelationship/n1:observation/n1:effectiveTime/@value">
			<xsl:call-template name="formatDate">
				<xsl:with-param name="date" select="./n1:entryRelationship/n1:observation/n1:effectiveTime/@value" />
			</xsl:call-template>
		</xsl:when>
		<xsl:when test="./n1:entryRelationship/n1:observation/n1:effectiveTime/n1:low/@value">
			<xsl:call-template name="formatDate">
				<xsl:with-param name="date" select="./n1:entryRelationship/n1:observation/n1:effectiveTime/n1:low/@value" />
			</xsl:call-template>
		</xsl:when>
		<xsl:otherwise>
			<xsl:text>No date available</xsl:text>
		</xsl:otherwise>
	</xsl:choose>
	</td>
	
	<!-- ** Allergy Type ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Allergy Type: </xsl:text></b>
	<xsl:value-of select="./n1:entryRelationship/n1:observation[n1:templateId/@root = '2.16.840.1.113883.10.20.1.18']/n1:code/@displayName"/></td>	
	
	
	<!-- ** Product Free Text ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Product Free Text: </xsl:text></b>	
	<xsl:value-of select="./n1:entryRelationship/n1:observation/n1:participant/n1:participantRole/n1:playingEntity/n1:name" /></td>
	
	</tr>
	
	<tr>
	<!-- ** Product Coded ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Product Coded: </xsl:text></b>
	<xsl:for-each select="./n1:entryRelationship/n1:observation/n1:participant">
		<xsl:choose>
			<xsl:when test="not(./n1:participantRole/n1:playingEntity/n1:code/@displayName) =''">
				<xsl:value-of select="./n1:participantRole/n1:playingEntity/n1:code/@displayName"/>
			</xsl:when>
			<xsl:when test="not(./n1:participantRole/n1:playingEntity/n1:code/@displayName)">
				<xsl:value-of select="./n1:participantRole/n1:playingEntity/n1:code/@code"/>
				<xsl:text> / </xsl:text>
				<xsl:value-of select="./n1:participant/n1:participantRole/n1:playingEntity/n1:code/@codeSystem"/>  
			</xsl:when>
		</xsl:choose> <br />
	</xsl:for-each>
	</td>
	
	<!-- ** Reaction Free Text ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Reaction Free Text: </xsl:text></b>
	<xsl:variable name="alertReactionText" select="substring(.//n1:observation[n1:templateId/@root = '2.16.840.1.113883.10.20.1.54']/n1:text/n1:reference/@value, 2)"/>
	<xsl:if test="$alertReactionText = (../../n1:text/n1:content/@ID)">
		<xsl:value-of select="../../n1:text/n1:content[contains (@ID, $alertReactionText)]"/>				
	</xsl:if></td>

	<!-- ** Reaction Coded ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Reaction Coded: </xsl:text></b>	
	<xsl:for-each select="./n1:entryRelationship/n1:observation/n1:entryRelationship/n1:observation[contains(./n1:templateId/@root,'2.16.840.1.113883.10.20.1.54')]">
		<xsl:value-of select="./n1:value/@displayName"/><br />
	</xsl:for-each>		
	</td>
	
	</tr>
	<tr>
	<!-- ** Severity Free Text ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Severity Free Text: </xsl:text></b>	
	<xsl:variable name="alertSeverityText" select="substring(.//n1:observation[n1:templateId/@root = '2.16.840.1.113883.10.20.1.55']/n1:text/n1:reference/@value, 2)"/>
	<xsl:if test="$alertSeverityText = (../../n1:text/n1:content/@ID)">
		<xsl:value-of select="../../n1:text/n1:content[contains (@ID, $alertSeverityText)]"/>				
	</xsl:if>
	</td>
	
	<!-- ** Severity Coded ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Severity Coded: </xsl:text></b>	
	<xsl:variable name="status" select=".//n1:observation[contains(n1:templateId/@root,'2.16.840.1.113883.10.20.1.55')]/n1:value/@code"/>
	<xsl:choose>
		<xsl:when test="$status='255604002'"><xsl:text>Mild</xsl:text></xsl:when>
		<xsl:when test="$status='371923003'"><xsl:text>Mild to Moderate</xsl:text></xsl:when>
		<xsl:when test="$status='6736007'"><xsl:text>Moderate</xsl:text></xsl:when>
		<xsl:when test="$status='371924009'"><xsl:text>Moderate to Severe</xsl:text></xsl:when>
		<xsl:when test="$status='24484000'"><xsl:text>Severe</xsl:text></xsl:when>
		<xsl:when test="$status='399166001'"><xsl:text>Fatal</xsl:text></xsl:when>
	</xsl:choose>	
	</td>
	
	</tr>
	
	</table>
	
	<xsl:if test="position() != last()" >
		<tr>
		<td><hr width="75%"  noshade="false" /></td>

		</tr>
	</xsl:if>
	
	</xsl:for-each>
	
	
	<!-- *********************************************************************  -->
	<!-- ********************* Narrative Section *****************************  -->
	<!-- *********************************************************************  -->
	
	<!-- Call HTML table formatting -->
	<xsl:if test="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.2']/n1:text/n1:table" >
	<xsl:apply-templates select="n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.2']/n1:text/n1:table"/>
	</xsl:if>

	<!-- *********************************************************************  -->
	<!-- ********************* Comments Section ******************************  -->
	<!-- *********************************************************************  
	<xsl:choose>
		<xsl:when test="(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.2']//n1:code[contains (@code, '48767-8')])and(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.2']/n1:text !='')">
			<h4 align='center'><b><xsl:text>Comments</xsl:text></b></h4>
			<xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.2']//n1:code[contains (@code, '48767-8')]" >	
				<xsl:variable name="alertComment" select="substring(../n1:text/n1:reference/@value, 2)" />
				<xsl:if test="$alertComment = (/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.2']/n1:text/n1:content/@ID)">
					<xsl:value-of select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.2']/n1:text/n1:content[contains (@ID, $alertComment)]"/>
				<br/>
				</xsl:if>
			</xsl:for-each>
		</xsl:when>
	</xsl:choose>-->

<hr />
 </xsl:if>		
 </xsl:template>	
 </xsl:stylesheet>