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

<xsl:template name="COMPLETE_PROBLEM_LIST">
	<xsl:if test="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.11']">
	<!-- *******************************************************  -->
	<!-- ****************** Complete - Problem List ************  -->
	<!-- *******************************************************  -->

	<h4 align='center'><b><xsl:text>Problem List</xsl:text></b></h4>
	
	<!-- ** Summary - Problem List ** -->
	<xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.11']/n1:entry/n1:act/n1:entryRelationship/n1:observation">
	<xsl:sort select="./n1:effectiveTime/n1:low/@value" order="descending"/>

	<table border="0" cellpadding="0" width="100%">

	<!-- ** Effective Date ** -->
	<tr><td width='33%' align='left' valign="top">
		<b><xsl:text>Event Date: </xsl:text></b>
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
	

	
	<!-- ** Problem type ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Problem Type: </xsl:text></b><xsl:value-of select="./n1:code/@displayName" />
	</td>

	<!-- ** Problem Name ** -->
	<td width='33%' align='left' valign="top">
		<b><xsl:text>Problem Name: </xsl:text></b>
		<xsl:variable name="probNameText" select="substring(./n1:text/n1:reference/@value, 2)"/>
		<xsl:if test="$probNameText = (../../../../n1:text/n1:content/@ID)">
			<xsl:value-of select="../../../../n1:text/n1:content[contains (@ID, $probNameText)]"/>				
		</xsl:if>	
	</td>
	</tr>
	
	<!-- ** Problem Code # ** -->
	<tr><td width='33%' align='left' valign="top">
		<b><xsl:text>Problem Code: </xsl:text></b>
		<xsl:text>(</xsl:text><xsl:value-of select="normalize-space(./n1:value/@code)"/><xsl:text>)</xsl:text><xsl:text> </xsl:text> <xsl:value-of select="./n1:value/@displayName" />
	</td>
	

	<!-- ** Problem Status ** -->
	<td width='33%' align='left' valign="top">
		<b><xsl:text>Problem Status: </xsl:text></b>
		<xsl:value-of select="./n1:entryRelationship/n1:observation/n1:value/@displayName"/>
	</td>

	<!-- ** Treating Provider ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Treating Provider: </xsl:text></b>
	<xsl:for-each select="./../../n1:performer">
		<xsl:choose>
			<xsl:when test="position()=last()">
				<xsl:value-of select="./n1:assignedEntity/n1:assignedPerson/n1:name/n1:prefix"/>
				<xsl:text> </xsl:text>
				<xsl:value-of select="./n1:assignedEntity/n1:assignedPerson/n1:name/n1:given"/>
				<xsl:text> </xsl:text>
				<xsl:value-of select="./n1:assignedEntity/n1:assignedPerson/n1:name/n1:family"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="./n1:assignedEntity/n1:assignedPerson/n1:name/n1:prefix"/>
				<xsl:text> </xsl:text>
				<xsl:value-of select="./n1:assignedEntity/n1:assignedPerson/n1:name/n1:given"/>
				<xsl:text> </xsl:text>
				<xsl:value-of select="./n1:assignedEntity/n1:assignedPerson/n1:name/n1:family"/>
				<xsl:text> / </xsl:text>
			</xsl:otherwise>
		</xsl:choose>	
	</xsl:for-each>
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
	<xsl:if test="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.11']/n1:text/n1:table" >
	<xsl:apply-templates select="n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.11']/n1:text/n1:table"/>
	</xsl:if>	
	
	<!-- *********************************************************************  -->
	<!-- ********************* Comments Section ******************************  -->
	<!-- *********************************************************************  -->
	
	<xsl:choose>
		<xsl:when test="(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.11']//n1:code[contains (@code, '48767-8')])and(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.11']/n1:text !='')">
			<h4 align='center'><b><xsl:text>Comments</xsl:text></b></h4>
			<xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.11']//n1:code[contains (@code, '48767-8')]" >	
				<xsl:variable name="probComment" select="substring(../n1:text/n1:reference/@value, 2)" />
				<xsl:if test="$probComment = (/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.11']/n1:text/n1:content/@ID)">
					<br/>
					<xsl:value-of select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.11']/n1:text/n1:content[contains (@ID, $probComment)]"/>
					<br/>
				</xsl:if>
			</xsl:for-each>
			<hr />
		</xsl:when>
		<xsl:otherwise>
		<hr/>
		</xsl:otherwise>
	</xsl:choose>
		
	</xsl:if>
 </xsl:template>	
 </xsl:stylesheet>