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

<xsl:template name="COMPLETE_ADVANCE_DIRECTIVES">
	<xsl:if test="(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.1'])and(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.1']/n1:entry != '')">
	<!-- *******************************************************  -->
	<!-- ************ Complete - Advance Directives ************  -->
	<!-- *******************************************************  -->

	<h4 align='center'><b><xsl:text>Advance Directives</xsl:text></b></h4>
	
	<table border="0" cellpadding="0" width="100%">
	<tr>
	<td width='15%' align='left' valign="top"><b><xsl:text>Date</xsl:text></b></td>
	<td width='40%' align='left' valign="top"><b><xsl:text>Advance Directives</xsl:text></b></td>
	<td width='45%' align='left' valign="top"><b><xsl:text>Participant</xsl:text></b></td>
	</tr>
	
	<xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.1']">
	<xsl:sort select="./n1:entry/n1:observation/n1:effectiveTime/@value"/>
	
	<tr>
	<!-- ** Effective Date ** -->
	<td width='10%' align='left' valign="top">
		<xsl:choose>
			<xsl:when test="./n1:entry/n1:observation/n1:effectiveTime/@value">
				<xsl:call-template name="formatDate">
				<xsl:with-param name="date" select="./n1:entry/n1:observation/n1:effectiveTime/@value" />
				</xsl:call-template>
			</xsl:when>
				<xsl:when test="./n1:entry/n1:observation/n1:effectiveTime/n1:low/@value">
				<xsl:call-template name="formatDate">
				<xsl:with-param name="date" select="./n1:entry/n1:observation/n1:effectiveTime/n1:low/@value" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>No date available</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</td>
	<!-- ** Advance Directives ** -->
	<td width='40%' align='left' valign="top"><xsl:value-of select="./n1:entry/n1:observation/n1:code/@displayName"/></td>
	
	<!-- ** Participant ** -->
	<td width='45%' align='left' valign="top"><xsl:value-of select="./n1:entry/n1:observation/n1:participant/n1:participantRole/n1:playingEntity/n1:name"/></td>
	
	</tr>
	
	
	</xsl:for-each>
	</table>
	
	<!-- *********************************************************************  -->
	<!-- ********************* Narrative Section *****************************  -->
	<!-- *********************************************************************  -->
	
	<xsl:if test="(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.1']/n1:entry/n1:observation/n1:code/n1:originalText/n1:reference/@value)and(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.1']/n1:text) !=''" >
	<h4 align='center'><b><xsl:text>Advance Directive Narrative Section</xsl:text></b></h4>
	
	
	<!-- ** Advance Directive Free Text -->
	<b><xsl:text>Advance Directive Free Text</xsl:text></b>
	<table border="0" cellpadding="0" width="100%">
		<xsl:for-each select="(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.1']/n1:entry/n1:observation/n1:code/n1:originalText/n1:reference/@value)" >
			<xsl:sort select="(../../../../n1:effectiveTime/@value)|(../../../../n1:effectiveTime/@value)" order="descending"/>
			<tr>
			<xsl:variable name="ADTextValue" select="substring(current(), 2)" />
			<xsl:if test="$ADTextValue = (../../../../../../n1:text/n1:content/@ID)">
				<td width="15%"><b><xsl:value-of select="../../../../n1:code/@displayName" /></b></td>
				<td width="75%">
				<xsl:value-of select="../../../../../../n1:text/n1:content[contains (@ID, $ADTextValue)]"/></td>
			</xsl:if>
			</tr>
		</xsl:for-each>	
	</table>
	<br />
	</xsl:if>

	<!-- Call HTML table formatting -->
	<xsl:if test="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.1']/n1:text/n1:table" >
	<xsl:apply-templates select="n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.1']/n1:text/n1:table"/>
	</xsl:if>
	
	<!-- *********************************************************************  -->
	<!-- ********************* Comments Section ******************************  -->
	<!-- *********************************************************************  -->
	<xsl:choose>
		<xsl:when test="(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.1']//n1:code[contains (@code, '48767-8')])and(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.1']/n1:text !='')">
			<h4 align='center'><b><xsl:text>Comments</xsl:text></b></h4>	
			<xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.1']//n1:code[contains (@code, '48767-8')]" >	
				<xsl:variable name="ADComment" select="substring(../n1:text/n1:reference/@value, 2)" />
				<xsl:if test="$ADComment = (/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.1']/n1:text/n1:content/@ID)">
					<xsl:value-of select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.1']/n1:text/n1:content[contains (@ID, $ADComment)]"/>
					<br/>
				</xsl:if>
			</xsl:for-each>
		</xsl:when>
	</xsl:choose>	
	
<hr/>
</xsl:if>

 </xsl:template>	
 </xsl:stylesheet>