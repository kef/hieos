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

<xsl:template name="COMPLETE_RESULTS">

	<!-- *******************************************************  -->
	<!-- ****************** Complete - Result ******************  -->
	<!-- *******************************************************  -->
<xsl:if test="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.14']" >
	<h4 align='center'><b><xsl:text>Results</xsl:text></b></h4>
	
		

	
	<!-- **** Results in Organizer **** -->
	<xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.14']/n1:entry">
	<xsl:sort select="(./n1:organizer/n1:component/n1:observation/n1:effectiveTime/n1:low/@value)|(./n1:organizer/n1:component/n1:observation/n1:effectiveTime/@value)|(./n1:observation/n1:effectiveTime/n1:low/@value)|(./n1:observation/n1:effectiveTime/@value)" order="descending" />
	
	<xsl:if test="./n1:organizer !=''">
	<hr width="85%" />
	<table border="0" cellpadding="0" width="100%">
	<tr>	<td width="45%" align="right"><b><xsl:text>Battery Test: </xsl:text></b><xsl:value-of select="./n1:organizer/n1:code/@displayName" /></td>
		<td width="10%"></td>
		<td width="45%" align="left"><b><xsl:text>Result Date: </xsl:text></b>	 	
		<xsl:choose>
	 		<xsl:when test="./n1:organizer/n1:effectiveTime/@value">
	 			<xsl:call-template name="formatDate">
	 			<xsl:with-param name="date" select="./n1:organizer/n1:effectiveTime/@value" />
	 			</xsl:call-template>
	 		</xsl:when>
	 			<xsl:when test="./n1:organizer/n1:effectiveTime/n1:low/@value">
	 			<xsl:call-template name="formatDate">
	 			<xsl:with-param name="date" select="./n1:organizer/n1:effectiveTime/n1:low/@value" />
	 			</xsl:call-template>
	 		</xsl:when>
	 		<xsl:otherwise>
	 			<xsl:text>No date available</xsl:text>
	 		</xsl:otherwise>
	 	</xsl:choose>
		</td>
	</tr>
	</table>
	<br />
	<xsl:for-each select="./n1:organizer/n1:component" >
	
	<table border="0" cellpadding="0" width="100%">
	<tr>
	
	<!-- ** Result ID ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Result ID: </xsl:text></b>
	<xsl:for-each select="./n1:observation/n1:id">
		<xsl:value-of select="./@root" />
	</xsl:for-each>	
	</td>
	

	<!-- ** Result Type ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Result Type: </xsl:text></b>
	<xsl:value-of select="./n1:observation/n1:code/@displayName" />
	</td>
	
	
	
	
	<!-- ** Result Status ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Result Status: </xsl:text></b>
	<xsl:value-of select="./n1:observation/n1:statusCode/@code" />
	</td>	
	</tr>
	
	<tr>
	<!-- ** Result Value ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Result Value: </xsl:text></b>
		<xsl:choose>
			<xsl:when test="./n1:observation/n1:value/@value" >
				<xsl:value-of select="./n1:observation/n1:value/@value"/>
				<xsl:text> </xsl:text>
				<xsl:value-of select="./n1:observation/n1:value/@unit" />
			</xsl:when>
			<xsl:when test="./n1:observation/n1:value/n1:numerator">
				<xsl:value-of select="./n1:observation/n1:value/n1:numerator/@value"/>
				<xsl:text> </xsl:text>
				<xsl:value-of select="./n1:observation/n1:value/n1:numerator/@unit" />
				<xsl:text> / </xsl:text>
				<xsl:value-of select="./n1:observation/n1:value/n1:denominator/@value"/>
				<xsl:text> </xsl:text>
				<xsl:value-of select="./n1:observation/n1:value/n1:denominator/@unit" />
			</xsl:when>
		</xsl:choose>
	</td>
	<!-- ** Result Interpretation ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Result Interpretation: </xsl:text></b>
	<xsl:value-of select="./n1:observation/n1:interpretationCode/@displayName" />
	</td>
	
	
	
	
	<!-- ** Result Reference Range ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Result Reference Range: </xsl:text></b>	
	<xsl:for-each select="./n1:observation/n1:referenceRange/n1:observationRange" >
	    	<xsl:value-of select="./n1:text"/><xsl:text> </xsl:text>
		<xsl:choose>
			 <xsl:when test="./n1:value">
			 	<xsl:value-of select="./n1:value/n1:low/@value" />
			 	<xsl:text> </xsl:text>
			 	<xsl:value-of select="./n1:value/n1:low/@unit" />
			 	<xsl:text> - </xsl:text>
			 	<xsl:value-of select="./n1:value/n1:high/@value" />
			 	<xsl:text> </xsl:text>
			 	<xsl:value-of select="./n1:value/n1:high/@unit" />
			</xsl:when>	
		</xsl:choose>
		<xsl:if test="position() != last()">
			<xsl:text> / </xsl:text>
		</xsl:if>								
	</xsl:for-each>			
	</td>
	</tr>
	
	<tr>
	<!-- ** Result Text ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Result Text: </xsl:text></b>
	<xsl:choose>
		<xsl:when test="(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.14']/n1:text !='')">			
			<xsl:variable name="ResTextValue" select="substring(./n1:observation/n1:text/n1:reference/@value, 2)" />
			<xsl:if test="$ResTextValue = (../../../n1:text/n1:content/@ID)">
				<xsl:value-of select="../../../n1:text/n1:content[contains (@ID, $ResTextValue)]" />
			</xsl:if>
		</xsl:when>
	</xsl:choose>
	</td>
	</tr>

	</table>

	<xsl:if test="position() > 0" >
		<tr>
		<td><hr width="50%"  noshade="false" /></td>
		</tr>
	</xsl:if>
	
	</xsl:for-each>
	<hr width="85%" />
	</xsl:if>
	
	
	
	
	<!-- **** Vitals in Entry **** -->
	<xsl:if test="./n1:observation" >
	
	<table border="0" cellpadding="0" width="100%">
	<tr>
	
	<!-- ** Result ID ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Result ID: </xsl:text></b>
	<xsl:for-each select="./n1:observation/n1:id">
		<xsl:value-of select="./@root" />
	</xsl:for-each>	
	</td>
	
	<!-- ** Result Date/Time ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Result Date/Time: </xsl:text></b>
	<xsl:choose>
		<xsl:when test="./n1:observation/n1:effectiveTime/@value">
			
			<xsl:call-template name="formatDate">
				<xsl:with-param name="date" select="./n1:observation/n1:effectiveTime/@value" />
			</xsl:call-template>
			<xsl:text> </xsl:text>
			<xsl:call-template name="formatTime">
				<xsl:with-param name="time" select="./n1:observation/n1:effectiveTime/@value" />
			</xsl:call-template>						
		</xsl:when>
		<xsl:when test="./n1:observation/n1:effectiveTime/n1:low/@value">
	
			<xsl:call-template name="formatDate">
			<xsl:with-param name="date" select="./n1:observation/n1:effectiveTime/n1:low/@value" />
			</xsl:call-template>
			<xsl:text> </xsl:text>
			<xsl:call-template name="formatTime">
				<xsl:with-param name="time" select="./n1:observation/n1:effectiveTime/n1:low/@value" />
			</xsl:call-template>	
		</xsl:when>
		<xsl:otherwise>
			<xsl:text>No date available</xsl:text>
		</xsl:otherwise>
	</xsl:choose>
	</td>

	<!-- ** Result Type ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Result Type: </xsl:text></b>
	<xsl:value-of select="./n1:observation/n1:code/@displayName" />
	</td>
	
	</tr>
	<tr>
	
	<!-- ** Result Status ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Result Status: </xsl:text></b>
	<xsl:value-of select="./n1:observation/n1:statusCode/@code" />
	</td>	
	
	
	<!-- ** Result Value ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Result Value: </xsl:text></b>
		<xsl:choose>
			<xsl:when test="./n1:observation/n1:value/@value" >
				<xsl:value-of select="./n1:observation/n1:value/@value"/>
				<xsl:text> </xsl:text>
				<xsl:value-of select="./n1:observation/n1:value/@unit" />
			</xsl:when>
			<xsl:when test="./n1:observation/n1:value/n1:numerator">
				<xsl:value-of select="./n1:observation/n1:value/n1:numerator/@value"/>
				<xsl:text> </xsl:text>
				<xsl:value-of select="./n1:observation/n1:value/n1:numerator/@unit" />
				<xsl:text> / </xsl:text>
				<xsl:value-of select="./n1:observation/n1:value/n1:denominator/@value"/>
				<xsl:text> </xsl:text>
				<xsl:value-of select="./n1:observation/n1:value/n1:denominator/@unit" />
			</xsl:when>
		</xsl:choose>
	</td>
	<!-- ** Result Interpretation ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Result Interpretation: </xsl:text></b>
	<xsl:value-of select="./n1:observation/n1:interpretationCode/@displayName" />
	</td>
	
	</tr>
	<tr>
	
	<!-- ** Result Reference Range ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Result Reference Range: </xsl:text></b>	
	<xsl:for-each select="./n1:observation/n1:referenceRange/n1:observationRange" >
	    	<xsl:value-of select="./n1:text"/><xsl:text> </xsl:text>
		<xsl:choose>
			 <xsl:when test="./n1:value">
			 	<xsl:value-of select="./n1:value/n1:low/@value" />
			 	<xsl:text> </xsl:text>
			 	<xsl:value-of select="./n1:value/n1:low/@unit" />
			 	<xsl:text> - </xsl:text>
			 	<xsl:value-of select="./n1:value/n1:high/@value" />
			 	<xsl:text> </xsl:text>
			 	<xsl:value-of select="./n1:value/n1:high/@unit" />
			</xsl:when>	
		</xsl:choose>
		<xsl:if test="position() != last()">
			<xsl:text> / </xsl:text>
		</xsl:if>								
	</xsl:for-each>			
	</td>
	
	<!-- ** Result Text: ** -->
	<td width='33%' align='left' valign="top">
	<b><xsl:text>Result Text: </xsl:text></b>	
	<xsl:choose>
		<xsl:when test="(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.14']/n1:text !='')">			
			<xsl:variable name="ResTextValue" select="substring(./n1:observation/n1:text/n1:reference/@value, 2)" />
			<xsl:if test="$ResTextValue = (../n1:text/n1:content/@ID)">
				<xsl:value-of select="../n1:text/n1:content[contains (@ID, $ResTextValue)]" />
			</xsl:if>
		</xsl:when>
	</xsl:choose>		
	
	
	</td>
	</tr>	
	</table>

	<xsl:if test="position() > 0" >
		<tr>
		<td><hr width="75%"  noshade="false" /></td>
		</tr>
	</xsl:if>	
	</xsl:if>


</xsl:for-each>

	<!-- *********************************************************************  -->
	<!-- ********************* Narrative Section *****************************  -->
	<!-- *********************************************************************  -->
		
	<!-- Call HTML table formatting -->
	<xsl:if test="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.14']/n1:text/n1:table" >
	<xsl:apply-templates select="n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.14']/n1:text/n1:table"/>
	</xsl:if>
		

	
	<!-- *********************************************************************  -->
	<!-- ********************* Comments Section ******************************  -->
	<!-- *********************************************************************  -->
	<xsl:choose>
		<xsl:when test="(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.14']//n1:code[contains (@code, '48767-8')])and(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.14']/n1:text !='')">
			<h4 align='center'><b><xsl:text>Comments</xsl:text></b></h4>
			<xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.14']//n1:code[contains (@code, '48767-8')]" >	
				<xsl:variable name="resComment" select="substring(../n1:text/n1:reference/@value, 2)" />
				<xsl:if test="$resComment = (/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.14']/n1:text/n1:content/@ID)">
					<xsl:value-of select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.14']/n1:text/n1:content[contains (@ID, $resComment)]"/>
				<br/>
				</xsl:if>
			</xsl:for-each>
		</xsl:when>
	</xsl:choose>

<hr />		
</xsl:if>
 </xsl:template>	
 </xsl:stylesheet>