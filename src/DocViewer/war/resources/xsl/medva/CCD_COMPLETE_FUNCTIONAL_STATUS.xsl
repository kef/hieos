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

<xsl:template name="COMPLETE_FUNCTIONAL_STATUS">

<xsl:if test="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.5']">

<!-- *******************************************************  -->
<!-- ********** Complete - Functional Status ***************  -->
<!-- *******************************************************  -->
<xsl:variable name="functionXpath" select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.5']" />

<h4 align='center'><b><xsl:text>Functional Status</xsl:text></b></h4><br />

<!-- **********************************************************************  -->
<!-- **************** Functional Status PROBLEM XSLT **********************  -->
<!-- **********************************************************************  --> 

 <xsl:if test="($functionXpath/n1:entry/n1:act/n1:entryRelationship/n1:observation) != ''">
 
 	<b><xsl:text>Functional Problem</xsl:text></b><br />
 	
 	<!-- ** Create Header for Functional Status Table ** -->	
	<table border="0" cellpadding="0" width="100%">
	<tr>
	<td width='20%' align='left' valign="top"><b><xsl:text>Date</xsl:text></b></td>
	<td width='20%' align='left' valign="top"><b><xsl:text>Type</xsl:text></b></td>
	<td width='20%' align='left' valign="top"><b><xsl:text>Funct. Problem</xsl:text></b></td>
	<td width='20%' align='left' valign="top"><b><xsl:text>Status</xsl:text></b></td>
	<td width='20%' align='left' valign="top"><b><xsl:text>Provider</xsl:text></b></td>
	</tr>		

 <xsl:for-each select="$functionXpath/n1:entry/n1:act/n1:entryRelationship/n1:observation">
	
	<!-- ** Effective Date ** -->
	<tr>
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

	<!-- ** Functional Status Type Code (Java Call)** -->
	<td width='20%' align='left' valign="top">
	<xsl:variable name="code_fs1" select="./n1:code/@code"/>
	<xsl:variable name="codeSystem_fs1" select="./n1:code/@codeSystem"/>
	<xsl:variable name="displayName_fs1" select="./n1:code/@displayName"/>
	<xsl:call-template name="getPreferredName">
	<xsl:with-param name="codeSystem" select="$codeSystem_fs1"/>
	<xsl:with-param name="code" select="$code_fs1"/>
	<xsl:with-param name="displayName" select="$displayName_fs1"/>
    	</xsl:call-template>
	</td>
	
	
	<!-- ** Functional Problem (Java Call) ** -->
	<td width='20%' align='left' valign="top">
	<xsl:variable name="code_fp1" select="./n1:value/@code"/>
	<xsl:variable name="codeSystem_fp1" select="./n1:value/@codeSystem"/>
	<xsl:variable name="displayName_fp1" select="./n1:value/@displayName"/>
	<xsl:call-template name="getPreferredName">
	<xsl:with-param name="codeSystem" select="$codeSystem_fp1"/>
	<xsl:with-param name="code" select="$code_fp1"/>
	<xsl:with-param name="displayName" select="$displayName_fp1"/>
    	</xsl:call-template>
	</td>
	
	<!-- ** Problem Status ** -->
	<td width='20%' align='left' valign="top">
	<xsl:variable name="code_ps1" select="./n1:entryRelationship/n1:observation/n1:value/@code"/>
    	<xsl:variable name="codeSystem_ps1" select="./n1:entryRelationship/n1:observation/n1:value/@codeSystem"/>
    	<xsl:variable name="displayName_ps1" select="./n1:entryRelationship/n1:observation/n1:value/@displayName"/>
	<xsl:call-template name="getPreferredName">
    	<xsl:with-param name="codeSystem" select="$codeSystem_ps1"/>
    	<xsl:with-param name="code" select="$code_ps1"/>
    	<xsl:with-param name="displayName" select="$displayName_ps1"/>
    	</xsl:call-template>
	</td>
	
	<!-- ** Provider ** -->
	<td width='20%' align='left' valign="top">
	<xsl:for-each select="./../../n1:performer">
	<xsl:value-of select="./n1:assignedEntity/n1:assignedPerson/n1:name/n1:prefix"/>
	<xsl:text> </xsl:text>
	<xsl:value-of select="./n1:assignedEntity/n1:assignedPerson/n1:name/n1:given"/>
	<xsl:text> </xsl:text>
	<xsl:value-of select="./n1:assignedEntity/n1:assignedPerson/n1:name/n1:family"/>
	<br />
	</xsl:for-each>	
	</td>
	</tr>
	
 	</xsl:for-each>
 	</table>
 	<br />
 </xsl:if>		

<!-- ********************************************************************************  -->
<!-- ***************** Functional Status RESULT ORGANIZER XSLT **********************  -->
<!-- ********************************************************************************  --> 

<xsl:if test="($functionXpath/n1:entry/n1:organizer) != ''">
	<b><xsl:text>Functional Status Test</xsl:text></b><br />

 <xsl:for-each select="$functionXpath/n1:entry/n1:organizer">
 
 	<!-- ** Standardized Functional Status Assessement Instrument ** -->
 	<b><xsl:text>Standardized Test Name: </xsl:text></b>
 	<xsl:variable name="code_cfs" select="./n1:code/@code"/>
     	<xsl:variable name="codeSystem_cfs" select="./n1:code/@codeSystem"/>
     	<xsl:variable name="displayName_cfs" select="./n1:code/@displayName"/>
 	<xsl:call-template name="getPreferredName">
     	<xsl:with-param name="codeSystem" select="$codeSystem_cfs"/>
     	<xsl:with-param name="code" select="$code_cfs"/>
     	<xsl:with-param name="displayName" select="$displayName_cfs"/>
     	</xsl:call-template>

 	<!-- ** Create Header for Functional Status Table ** -->	
	<table border="0" cellpadding="0" width="100%">
	<tr>
	<td width='20%' align='left' valign="top"><b><xsl:text>Date</xsl:text></b></td>
	<td width='20%' align='left' valign="top"><b><xsl:text>Test/Question</xsl:text></b></td>
	<td width='20%' align='left' valign="top"><b><xsl:text>Ref Range</xsl:text></b></td>
	<td width='20%' align='left' valign="top"><b><xsl:text>Result Value</xsl:text></b></td>
	<td width='20%' align='left' valign="top"><b><xsl:text>Interpretation</xsl:text></b></td>
	</tr>
			
	<xsl:for-each select="./n1:component">
		<tr>
		<!-- ** Organizer - Date ** -->
		<td width='20%' align='left' valign="top">
		<xsl:choose>
			<xsl:when test="./n1:observation/n1:effectiveTime/@value">
				<xsl:call-template name="formatDate">
				<xsl:with-param name="date" select="./n1:observation/n1:effectiveTime/@value" />
				</xsl:call-template>
			</xsl:when>
				<xsl:when test="./n1:observation/n1:effectiveTime/n1:low/@value">
				<xsl:call-template name="formatDate">
				<xsl:with-param name="date" select="./n1:observation/n1:effectiveTime/n1:low/@value" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>No date available</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
		</td>
	
	<!-- ** Organizer - Test/Question (Java Call) ** -->
	<td width='20%' align='left' valign="top">
	<xsl:variable name="code1" select="./n1:observation/n1:code/@code"/>
	<xsl:variable name="codeSystem1" select="./n1:observation/n1:code/@codeSystem"/>
	<xsl:variable name="displayName1" select="./n1:observation/n1:code/@displayName"/>
	<xsl:call-template name="getPreferredName">
	<xsl:with-param name="codeSystem" select="$codeSystem1"/>
	<xsl:with-param name="code" select="$code1"/>
	<xsl:with-param name="displayName" select="$displayName1"/>
    	</xsl:call-template>
	</td>
	
	<!-- ** Organizer - Ref Range ** -->
	<td width='20%' align='left' valign="top">
	<xsl:value-of select="./n1:observation/n1:referenceRange/n1:observationRange/n1:text"/>
	<xsl:choose>
	<xsl:when test="./n1:observation/n1:referenceRange/n1:observationRange/n1:value">
	<xsl:value-of select="./n1:observation/n1:referenceRange/n1:observationRange/n1:value/n1:low/@value" />
	<xsl:text> </xsl:text>
	<xsl:value-of select="./n1:observation/n1:referenceRange/n1:observationRange/n1:value/n1:low/@unit" />
	<xsl:text> - </xsl:text>
	<xsl:value-of select="./n1:observation/n1:referenceRange/n1:observationRange/n1:value/n1:high/@value" />
	<xsl:text> </xsl:text>
	<xsl:value-of select="./n1:observation/n1:referenceRange/n1:observationRange/n1:value/n1:high/@unit" />
	</xsl:when>	
	</xsl:choose>
	</td>
	
	<!-- ** Organizer - Result Value ** -->
	<td width='20%' align='left' valign="top">
	<xsl:choose>
		<xsl:when test="./n1:observation/n1:value/@xsi:type = 'CE'or'CD'">
			<xsl:value-of select="./n1:observation/n1:value/@code"/>
			<xsl:text> </xsl:text>
			<xsl:value-of select="./n1:observation/n1:value/@displayName"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select="./n1:observation/n1:value/@value"/>
			<xsl:text> </xsl:text>
			<xsl:value-of select="./n1:observation/n1:value/@unit"/>
		</xsl:otherwise>
	</xsl:choose>	
	</td>
	
	<!-- ** Organizer - Interpretation ** -->
	<td width='20%' align='left' valign="top">
	<xsl:value-of select="./n1:observation/n1:interpretationCode/@displayName"/>
	</td>
 
 	</tr>
 	

 	
  </xsl:for-each>
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
	<xsl:if test="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.5']/n1:text/n1:table" >
	<xsl:apply-templates select="n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.5']/n1:text/n1:table"/>
	</xsl:if>

	<!-- *********************************************************************  -->
	<!-- ********************* Comments Section ******************************  -->
	<!-- *********************************************************************  -->
	<xsl:choose>
		<xsl:when test="(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.5']//n1:code[contains (@code, '48767-8')])and(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.5']/n1:text !='')">
			<h4 align='center'><b><xsl:text>Comments</xsl:text></b></h4>
			<xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.5']//n1:code[contains (@code, '48767-8')]" >	
				<xsl:variable name="fSComment" select="substring(../n1:text/n1:reference/@value, 2)" />
				<xsl:if test="$fSComment = (/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.5']/n1:text/n1:content/@ID)">
					<xsl:value-of select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.5']/n1:text/n1:content[contains (@ID, $fSComment)]"/>
				</xsl:if>
			</xsl:for-each>
		</xsl:when>
	</xsl:choose>


</xsl:if>
 
<hr/>
</xsl:if> 



</xsl:template>	
</xsl:stylesheet>