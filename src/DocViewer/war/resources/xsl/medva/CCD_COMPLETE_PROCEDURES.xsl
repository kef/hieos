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

<xsl:template name="COMPLETE_PROCEDURES">
	<xsl:if test="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.12']">
	<!-- *******************************************************  -->
	<!-- ***************** Complete - Procedures ***************  -->
	<!-- *******************************************************  -->
	
	<h4 align='center'><b><xsl:text>Procedures</xsl:text></b></h4>
	
	<table border="0" cellpadding="0" width="100%">
	<tr>
	<td width='20%' align='left' valign="top"><b><xsl:text>Date</xsl:text></b></td>
	<td width='40%' align='left' valign="top"><b><xsl:text>Procedure</xsl:text></b></td>
	<!-- <td width='30%' align='left' valign="top"><b><xsl:text>Facility</xsl:text></b></td> -->
	<td width='20%' align='left' valign="top"><b><xsl:text>Provider</xsl:text></b></td>
	<td width='20%' align='left' valign="top"><b><xsl:text>Related Results</xsl:text></b></td>
	</tr>
	<xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.12']/n1:entry/n1:procedure">
  	<xsl:sort select="(./n1:effectiveTime/n1:low/@value)|(./n1:effectiveTime/@value)" order="descending"/>

	<tr>
	
	<!-- ** Effective Date ** -->
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
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>No date available</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</td>
	
	
	<!-- ** Procedure ** -->
	<td width='40%' align='left' valign="top"><xsl:value-of select="./n1:code/@displayName"/><xsl:text> </xsl:text><xsl:value-of select="./n1:participant/n1:participantRole/n1:playingDevice/n1:code/@displayName"/></td>
	
	<!-- ** Facility ** 
	<td width='30%' align='left' valign="top"><xsl:value-of select="./n1:performer/n1:assignedEntity/n1:code/@displayName"/></td>-->
	
	<!-- ** Provider ** -->
	<td width='20%' align='left' valign="top">
	<xsl:call-template name="getPhysician">
	<xsl:with-param name="phyname" select="./n1:performer/n1:assignedEntity/n1:assignedPerson/n1:name"/>
	</xsl:call-template>
	</td>	
	
	<!-- ** Related Results ** -->
	<td width='20%' align='left' valign="top"></td>
	</tr>
	</xsl:for-each>
	</table>
	
	
	<xsl:variable name="proceduresXpath" select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.12']" />
	
	
	<!-- ** Procedure Text ** -->	
	<table border="0" cellpadding="0" width="100%">
	<xsl:if test="($proceduresXpath/n1:text != '')">
		<br /><b><font size="3">Procedure Text: </font></b><br />
	</xsl:if>
	</table>

	<!-- *********************************************************************  -->
	<!-- ********************* Narrative Section *****************************  -->
	<!-- *********************************************************************  -->
	
	<!-- Call HTML table formatting -->
	<xsl:if test="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.12']/n1:text/n1:table" >
	<xsl:apply-templates select="n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.12']/n1:text/n1:table"/>
	</xsl:if>

	<xsl:choose>
		<xsl:when test="(not(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.12']/n1:entry/n1:procedure/n1:text/n1:reference/@value)='')
			and(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.12']/n1:text !='')">
		<table border="0" cellpadding="0" width="100%">
			<xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.12']/n1:entry/n1:procedure/n1:text/n1:reference/@value">
				<xsl:sort select="(../../../../n1:procedure/n1:effectiveTime/n1:low/@value)|(../../../../n1:procedure/n1:effectiveTime/@value)" order="descending"/>
				
				<xsl:variable name="ProcTextValue" select="substring(current(), 2)" />
				<xsl:if test="$ProcTextValue = (../../../../../n1:text/n1:content/@ID)">
				<tr><td><h4 align='center'><br/><b>
						
						<xsl:value-of select="../../../../n1:procedure/n1:code/@displayName"/>
						<br/><xsl:text> Date: </xsl:text>
					<xsl:choose>
						<xsl:when test="../../../../n1:procedure/n1:effectiveTime/@value">
							<xsl:call-template name="formatDate">
							<xsl:with-param name="date" select="../../../../n1:procedure/n1:effectiveTime/@value" />
							</xsl:call-template>
						</xsl:when>
						<xsl:when test="../../../../n1:procedure/n1:effectiveTime/n1:low/@value">
							<xsl:call-template name="formatDate">
							<xsl:with-param name="date" select="../../../../n1:procedure/n1:effectiveTime/n1:low/@value" />
							</xsl:call-template>
						</xsl:when>
						<xsl:otherwise>
							<xsl:text>No date available</xsl:text>
						</xsl:otherwise>
					</xsl:choose></b></h4></td></tr>
				<tr><td width="100%">
				
					<xsl:value-of select="../../../../../n1:text/n1:content[contains (@ID, $ProcTextValue)]" disable-output-escaping="yes"/><br /><br /></td></tr>
				</xsl:if>
				
			</xsl:for-each>	
		</table>
			
		</xsl:when>
	
		<xsl:when test="(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.12']/n1:entry/n1:procedure/n1:code/n1:originalText/n1:reference/@value)
			and(not($proceduresXpath/n1:entry/n1:procedure/n1:text))">
		<table border="0" cellpadding="0" width="100%">
			
			<xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.12']/n1:entry/n1:procedure/n1:code/n1:originalText/n1:reference/@value">
				<xsl:sort select="(../../../../../n1:procedure/n1:effectiveTime/n1:low/@value)|(../../../../../n1:procedure/n1:effectiveTime/@value)" order="descending"/>
				
				<xsl:variable name="ProcTextValue" select="substring(current(), 2)" />
				<xsl:if test="$ProcTextValue = (../../../../../../n1:text/n1:content/@ID)">
				<tr><td><h4 align='center'><br/><b>
						
							<xsl:variable name="code" select="../../../../../n1:procedure/n1:code/@code"/>
							<xsl:variable name="codeSystem" select="../../../../../n1:procedure/n1:code/@codeSystem"/>
							<xsl:variable name="displayName" select="../../../../../n1:procedure/n1:code/@displayName"/>
							<xsl:call-template name="getPreferredName">
								<xsl:with-param name="codeSystem" select="$codeSystem"/>
								<xsl:with-param name="code" select="$code"/>
								<xsl:with-param name="displayName" select="$displayName"/>
    							</xsl:call-template>
						<br/><xsl:text> Date: </xsl:text>
					<xsl:choose>
						<xsl:when test="../../../../../n1:procedure/n1:effectiveTime/@value">
							<xsl:call-template name="formatDate">
							<xsl:with-param name="date" select="../../../../../n1:procedure/n1:effectiveTime/@value" />
							</xsl:call-template>
						</xsl:when>
						<xsl:when test="../../../../../n1:procedure/n1:effectiveTime/n1:low/@value">
							<xsl:call-template name="formatDate">
							<xsl:with-param name="date" select="../../../../../n1:procedure/n1:effectiveTime/n1:low/@value" />
							</xsl:call-template>
						</xsl:when>
						<xsl:otherwise>
							<xsl:text>No date available</xsl:text>
						</xsl:otherwise>
					</xsl:choose></b></h4></td></tr>
				<tr><td width="100%">
				
					<xsl:value-of select="../../../../../../n1:text/n1:content[contains (@ID, $ProcTextValue)]" disable-output-escaping="yes"/><br /><br /></td></tr>
				</xsl:if>
				
			</xsl:for-each>	
		</table>
			
		</xsl:when>		
	
	
	
	</xsl:choose>	


	<!-- *********************************************************************  -->
	<!-- ********************* Comments Section ******************************  -->
	<!-- *********************************************************************  -->
	<xsl:choose>
		<xsl:when test="(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.12']//n1:code[contains (@code, '48767-8')])and(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.12']/n1:text !='')">
			<h4 align='center'><b><xsl:text>Comments</xsl:text></b></h4>
			<xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.12']//n1:code[contains (@code, '48767-8')]" >	
				<xsl:variable name="procComment" select="substring(../n1:text/n1:reference/@value, 2)" />
				<xsl:if test="$procComment = (/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.12']/n1:text/n1:content/@ID)">
					<xsl:value-of select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.12']/n1:text/n1:content[contains (@ID, $procComment)]"/>
				<br/>
				</xsl:if>
			</xsl:for-each>
		</xsl:when>
	</xsl:choose>	
	

<hr/>
</xsl:if>
 </xsl:template>	
 </xsl:stylesheet>
 
 
 
 
 
 
 <!--  COMMENTED OUT	<xsl:if test="(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.12']/n1:entry/n1:procedure/n1:text/n1:reference/@value)or(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.12']/n1:entry/n1:procedure/n1:text) !=''">
 		
 		<h4 align='center'><b><xsl:text>Procedure Narrative Section</xsl:text></b></h4>
 		<b><xsl:text>Procedure Name</xsl:text></b><br />
 		<table border="0" cellpadding="0" width="100%">
 		<xsl:if test="not(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.12']/n1:text ='')">
 		
 			<xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.12']/n1:entry/n1:procedure/n1:text/n1:reference/@value">
 				<xsl:sort select="(../../../n1:effectiveTime/n1:low/@value)|(../../../n1:effectiveTime/@value)" order="descending"/>
 				<tr>
 				<xsl:variable name="procTextValue" select="substring(../../../n1:text/n1:reference/@value, 2)" />
 				<xsl:if test="$procTextValue = (../../../../../n1:text/n1:content/@ID)">
 				<td width="15%"><b><xsl:value-of select="../../../n1:code/@displayName" /></b></td>
 				<td width="5%" />
 				<td width="80%">
 					<xsl:value-of select="../../../../../n1:text/n1:content[contains (@ID, $procTextValue)]"/></td>
 				</xsl:if>
 				</tr>
 			</xsl:for-each>	
 		</xsl:if>
 		
 			<xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.12']/n1:entry/n1:procedure/n1:text">
 				<xsl:if test="(not(./n1:reference/@value))and(current() != '')">
 				<xsl:sort select="(../../n1:effectiveTime/n1:low/@value)|(../../n1:effectiveTime/@value)" order="descending"/>
 				<tr>
 				<td width="15%"><b><xsl:value-of select="../n1:code/@displayName" /></b></td>
 				<td width="5%" />
 				<td width="80%">
 				<xsl:value-of select="current()" /></td>
 				</tr>
 				</xsl:if>
 			</xsl:for-each>	
 		
 		</table>
 			
 	</xsl:if>-->

 
 
 
 
 
 
 
 
 
 	<!-- ** Operative Report ** 
 	<xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.12']/n1:entry/n1:procedure/n1:reference">
 	<xsl:choose>
 	<xsl:when test="(//n1:code/@code)='11504-8'">
 	<p align='left'><b>-->
 	
 	<!-- ** OP Report Date ** 
 	<xsl:choose>
 	<xsl:when test="../n1:effectiveTime/@value">
 	<xsl:call-template name="formatDate">
 	<xsl:with-param name="date" select="../n1:effectiveTime/@value" />
 	</xsl:call-template>
 	</xsl:when>
 	<xsl:when test="../n1:effectiveTime/n1:low/@value">
 	<xsl:call-template name="formatDate">
 	<xsl:with-param name="date" select="../n1:effectiveTime/n1:low/@value" />
 	</xsl:call-template>
 	</xsl:when>
 	<xsl:otherwise>
 	<xsl:text>No date available</xsl:text>
 	</xsl:otherwise>
 	</xsl:choose>
 
 	
 	<br />
 	<xsl:text>Operative Report For Procedure:</xsl:text><br />
 	<xsl:variable name="code" select="./../n1:code/@code"/>
 	<xsl:variable name="codeSystem" select="./../n1:code/@codeSystem"/>
 	<xsl:variable name="displayName" select="./../n1:code/@displayName"/>
 	<xsl:call-template name="getPreferredName">
 	<xsl:with-param name="codeSystem" select="$codeSystem"/>
 	<xsl:with-param name="code" select="$code"/>
 	<xsl:with-param name="displayName" select="$displayName"/>
 	</xsl:call-template></b> <br />
 	</p>
 	<xsl:value-of select="./n1:externalDocument/n1:text" />
 	</xsl:when>
 	<xsl:otherwise>
 	<xsl:text> </xsl:text>
 	</xsl:otherwise>
 	</xsl:choose>
	</xsl:for-each>-->
