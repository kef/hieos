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

<xsl:template name="COMPLETE_SOCIAL_HISTORY">
	
<xsl:if test="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.15']" >
	<!-- *******************************************************  -->
	<!-- **************** Complete - Social History ***************  -->
	<!-- *******************************************************  -->

	<h4 align='center'><b><xsl:text>Social History</xsl:text></b></h4>
	<table border="0" cellpadding="0" width="100%">
		
	<!-- ** Header ** -->
	<tr>

	<td width='20%' align='left' valign="top"><b><xsl:text>Date</xsl:text></b></td>
	<td width='40%' align='left' valign="top"><b><xsl:text>Social History Event</xsl:text></b></td>
	<td width='40%' align='left' valign="top"><b><xsl:text>Social History Event Value</xsl:text></b></td>

	</tr>
	<xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.15']/n1:entry/n1:observation">
	<tr>
	<!-- ** Event Date ** -->
	<td width='20%' align='left' valign="top">
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
			<xsl:if test="./n1:effectiveTime/n1:high/@value">
				<xsl:text> - </xsl:text>
				<xsl:call-template name="formatDate">
					<xsl:with-param name="date" select="./n1:effectiveTime/n1:high/@value" />
				</xsl:call-template>
			</xsl:if>	
		</xsl:when>
		<xsl:otherwise>
			<xsl:text>No date available</xsl:text>
		</xsl:otherwise>
	</xsl:choose>
	</td>
	
	<!-- ** Social History Event Type (Java Call) ** -->
	<td width='40%' align='left' valign="top">
	<xsl:variable name="code_sh" select="./n1:code/@code"/>
	<xsl:variable name="codeSystem_sh" select="./n1:code/@codeSystem"/>
	<xsl:variable name="displayName_sh" select="./n1:code/@displayName"/>
	<xsl:call-template name="getPreferredName">
		<xsl:with-param name="codeSystem" select="$codeSystem_sh"/>
		<xsl:with-param name="code" select="$code_sh"/>
		<xsl:with-param name="displayName" select="$displayName_sh"/>
    	</xsl:call-template>
	</td>
	
	<!-- ** Social History Event Value ** -->
	<td width='40%' align='left' valign="top">
	<xsl:choose>
		<xsl:when test="./n1:value/@xsi:type = 'ST'">
			<xsl:value-of select="./n1:value" />
		</xsl:when>
		<xsl:otherwise>
			<xsl:variable name="code" select="./n1:value/@code"/>
			<xsl:variable name="codeSystem" select="./n1:value/@codeSystem"/>
			<xsl:variable name="displayName" select="./n1:value/@displayName"/>
			<xsl:call-template name="getPreferredName">
			<xsl:with-param name="codeSystem" select="$codeSystem"/>
			<xsl:with-param name="code" select="$code"/>
			<xsl:with-param name="displayName" select="$displayName"/>
    			</xsl:call-template>
    			
    			<xsl:if test="./n1:value/@unit">
    				<xsl:text> </xsl:text>
    				<xsl:value-of select="./n1:value/@unit" />
    			</xsl:if>
		</xsl:otherwise>
	</xsl:choose>
	</td>
	
	</tr>
	</xsl:for-each>
	</table>

	<!-- *********************************************************************  -->
	<!-- ********************* Narrative Section *****************************  -->
	<!-- *********************************************************************  -->

	<!-- Call HTML table formatting -->
	<xsl:if test="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.6']/n1:text/n1:table" >
	<xsl:apply-templates select="n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.6']/n1:text/n1:table"/>
	</xsl:if>

</xsl:if>
	
 </xsl:template>	
  </xsl:stylesheet>