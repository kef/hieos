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

<xsl:template name="COMPLETE_PAYERS">
	<xsl:if test="(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.9'])and(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.9']/n1:entry !='')">
	<!-- *******************************************************  -->
	<!-- ****************** Complete - Payers ******************  -->
	<!-- *******************************************************  -->

	<h4 align='center'><b><xsl:text>Payers</xsl:text></b></h4>

	<table border="0" cellpadding="0" width="100%">
	<xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.9']/n1:entry">
	
	<tr>
	<td width='50%' align='left' valign="top"><b><xsl:text>Organization Name</xsl:text></b></td>
	<td width='50%' align='left' valign="top"><b><xsl:text>Group Number</xsl:text></b></td>
	</tr>
	<tr>
	<td width='50%' align='left' valign="top"><xsl:value-of select="./n1:act/n1:entryRelationship/n1:act/n1:performer/n1:assignedEntity/n1:representedOrganization/n1:name"/></td>
	<td width='50%' align='left' valign="top"><xsl:value-of select="./n1:act/n1:entryRelationship/n1:act/n1:id/@extension"/></td>
	</tr>
	<tr>


	<td width='50%' align='left' valign="top">
	<xsl:call-template name="getAddress"> 
	<xsl:with-param name="addr" select="./n1:act/n1:entryRelationship/n1:act/n1:performer/n1:assignedEntity/n1:addr"/>
	</xsl:call-template>

	<br />
	<xsl:call-template name="getTelecom"> 
	<xsl:with-param name="telecom" select="/n1:ClinicalDocument/n1:documentationOf/n1:serviceEvent/n1:performer/n1:assignedEntity/n1:telecom"/>
	</xsl:call-template>
	</td>
	</tr>
	</xsl:for-each>
	</table>
	<xsl:text>  </xsl:text>
	
	<!-- ** Coverage ** -->
	<table border="0" cellpadding="0" width="100%">
	<tr>
	<td width='50%' align='left' valign="top"><b><xsl:text>Coverage Dates</xsl:text></b></td>
	<td width='50%' align='left' valign="top"><b><xsl:text>Health Insurance Type</xsl:text></b></td>
	</tr>
	<tr>
	
	<!-- ** Date ** -->
	
	<xsl:choose>
		<xsl:when test="(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section/n1:entry/n1:act/n1:entryRelationship/n1:act/n1:participant/n1:time/n1:low/@value)and(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section/n1:entry/n1:act/n1:entryRelationship/n1:act/n1:participant/n1:time/n1:high/@value)">
			<td width='50%' align='left' valign="top">
			<xsl:call-template name="formatDate">
			<xsl:with-param name="date" select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section/n1:entry/n1:act/n1:entryRelationship/n1:act/n1:participant/n1:time/n1:low/@value"/>
			</xsl:call-template>
			<xsl:text> - </xsl:text>
			<xsl:call-template name="formatDate">
			<xsl:with-param name="date" select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section/n1:entry/n1:act/n1:entryRelationship/n1:act/n1:participant/n1:time/n1:high/@value"/>
			</xsl:call-template>
			</td>
		</xsl:when>
		<xsl:when test="(./n1:time/n1:low/@value)and not(./n1:time/n1:high/@value)">
			<td width='50%' align='left' valign="top">
			<xsl:call-template name="formatDate">
			<xsl:with-param name="date" select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section/n1:entry/n1:act/n1:entryRelationship/n1:act/n1:participant/n1:time/n1:low/@value"/>
			</xsl:call-template>
			</td>
		</xsl:when>
		<xsl:otherwise>
			<td width='50%' align='left' valign="top"><xsl:text>No date available</xsl:text></td>
		</xsl:otherwise>
	</xsl:choose>

	<td width='50%' align='left' valign="top"><xsl:value-of select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section/n1:entry/n1:act/n1:entryRelationship/n1:act/n1:code/@displayName"/>
	</td>
	</tr>
	<xsl:text>  </xsl:text>
	<xsl:text>  </xsl:text>
	<tr>
	<td width='50%' align='left' valign="top"><b><xsl:text>Patient</xsl:text></b></td>

	<td width='50%' align='left' valign="top"><b><xsl:text>DOB: </xsl:text></b>

	<xsl:call-template name="formatDate">
	<xsl:with-param name="date" select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section/n1:entry/n1:act/n1:entryRelationship/n1:act/n1:participant/n1:participantRole/n1:playingEntity/sdtc:birthTime/@value"/>
	</xsl:call-template>
	</td>
	</tr>
	<tr>
	<td width='50%' align='left' valign="top"><xsl:value-of select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section/n1:entry/n1:act/n1:entryRelationship/n1:act/n1:participant/n1:participantRole/n1:playingEntity/n1:name"/><br />
	
	<xsl:call-template name="getAddress"> 
	<xsl:with-param name="addr" select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section/n1:entry/n1:act/n1:entryRelationship/n1:act/n1:participant/n1:participantRole/n1:addr"/>
	</xsl:call-template>
	</td>
	</tr>
	</table>

	<!-- *********************************************************************  -->
	<!-- ******************** Subscriber (if any) ****************************  -->
	<xsl:if test="/n1:ClinicalDocument//n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.9']//n1:act/n1:participant[contains (@typeCode, 'HLD')]" >
	<br /><h4 align='center'><b><xsl:text>Subscriber Information</xsl:text></b></h4>
	<table border="0" cellpadding="0" width="100%">
	
	<tr>
	<td width='50%' align='left' valign="top"><b><xsl:text>Subscriber Name</xsl:text></b><br />
	<xsl:call-template name="getName">
	<xsl:with-param name="name" select="/n1:ClinicalDocument//n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.9']//n1:act/n1:participant[contains (@typeCode, 'HLD')]/n1:participantRole/n1:playingEntity/n1:name" />
	</xsl:call-template></td>
	
	<td width='50%' align='left' valign="top"><b><xsl:text>Subscriber ID</xsl:text></b><br />
	<xsl:value-of select="/n1:ClinicalDocument//n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.9']//n1:act/n1:participant[contains (@typeCode, 'HLD')]/n1:participantRole/n1:id/@extension" />
	</td>
	</tr>
	
	<tr>
	<br /><td width='50%' align='left' valign="top"><b><xsl:text>Subscriber Subscriber Address</xsl:text></b><br />
	<xsl:call-template name="getAddress"> 
	<xsl:with-param name="addr" select="/n1:ClinicalDocument//n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.9']//n1:act/n1:participant[contains (@typeCode, 'HLD')]/n1:participantRole/n1:addr" />
	</xsl:call-template>
	</td>
	
	<td width='50%' align='left' valign="top"><b><xsl:text>Subscriber DOB</xsl:text></b><br />
	<xsl:call-template name="formatDate">
		<xsl:with-param name="date" select="/n1:ClinicalDocument//n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.9']//n1:act/n1:participant[contains (@typeCode, 'HLD')]/n1:participantRole/n1:playingEntity/sdtc:birthTime/@value" />
	</xsl:call-template>
	</td>
	</tr>
	</table>
	</xsl:if>
	
	<!-- *********************************************************************  -->
	<!-- ********************* Comments Section ******************************  -->
	<!-- *********************************************************************  -->
	<xsl:choose>
		<xsl:when test="(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.9']//n1:code[contains (@code, '48767-8')])and(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.9']/n1:text !='')">
			<h4 align='center'><b><xsl:text>Comments</xsl:text></b></h4>
			<xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.9']//n1:code[contains (@code, '48767-8')]" >	
				<xsl:variable name="payerComment" select="substring(../n1:text/n1:reference/@value, 2)" />
				<xsl:if test="$payerComment = (/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.9']/n1:text/n1:content/@ID)">
					<xsl:value-of select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.9']/n1:text/n1:content[contains (@ID, $payerComment)]"/>
					<br />
				</xsl:if>
			</xsl:for-each>
		</xsl:when>
	</xsl:choose>
<hr/>
</xsl:if>

 </xsl:template>	
 </xsl:stylesheet>