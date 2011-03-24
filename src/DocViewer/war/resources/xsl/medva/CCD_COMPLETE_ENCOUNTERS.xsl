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

<xsl:template name="COMPLETE_ENCOUNTERS">
	<xsl:if test="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.3']">
	<!-- *******************************************************  -->
	<!-- ***************** Complete - Encounters ***************  -->
	<!-- *******************************************************  -->	
	<h4 align='center'><b><xsl:text>Encounters</xsl:text></b></h4>

	<xsl:if test="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.3']/n1:entry/n1:encounter" >
	
	<table border="0" cellpadding="0" width="100%">
	
	<!-- ** Header ** -->
	<tr>
	<td width='5%' align='left' valign="top"><b><xsl:text>#</xsl:text></b></td>
	<td width='20%' align='left' valign="top"><b><xsl:text>Date</xsl:text></b></td>
	<td width='40%' align='left' valign="top"><b><xsl:text>Type of Encounter</xsl:text></b></td>
	<td width='15%' align='left' valign="top"><b><xsl:text>Provider Name</xsl:text></b></td>
	<td width='15%' align='left' valign="top"><b><xsl:text>Facility</xsl:text></b></td>
	</tr>
	
	<xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.3']/n1:entry">
	<xsl:sort select="(./n1:encounter/n1:effectiveTime/n1:low/@value)|(./n1:encounter/n1:effectiveTime/@value)" order="descending"/>
	
	<tr>
		
	<!-- ** Count ** -->
	<td width='5%' align='left' valign="top"><xsl:value-of select="position()"/></td>

	<!-- ** Effective Date ** -->
		<td width='20%' align='left' valign="top">
			<xsl:choose>
				<xsl:when test="./n1:encounter/n1:effectiveTime/n1:low/@value">
					<xsl:call-template name="formatDate">
					<xsl:with-param name="date" select="./n1:encounter/n1:effectiveTime/n1:low/@value" />
					</xsl:call-template>
				</xsl:when>
					<xsl:when test="./n1:encounter/n1:effectiveTime/@value">
					<xsl:call-template name="formatDate">
					<xsl:with-param name="date" select="./n1:encounter/n1:effectiveTime/@value" />
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>No date available</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</td>
		
	<!-- ** Type ** -->
	<td width='40%' align='left' valign="top">
	<xsl:value-of select="./n1:encounter/n1:code/@displayName"/>
	</td>
		
	<!-- ** Provider Name ** -->
	<td width='15%' align='left' valign="top">
	<xsl:value-of select="./n1:encounter/n1:performer/n1:assignedEntity/n1:assignedPerson/n1:name/n1:prefix"/>
	<xsl:text> </xsl:text>
	<xsl:value-of select="./n1:encounter/n1:performer/n1:assignedEntity/n1:assignedPerson/n1:name/n1:given"/>
	<xsl:text> </xsl:text>
	<xsl:value-of select="./n1:encounter/n1:performer/n1:assignedEntity/n1:assignedPerson/n1:name/n1:family"/>
	</td>


	<!-- ** Facility ** -->
	<td width='15%' align='left' valign="top">
	<xsl:value-of select="./n1:encounter/n1:participant[n1:templateId/@root='2.16.840.1.113883.10.20.1.45']/n1:participantRole[@classCode = 'SDLOC']/n1:playingEntity[@classCode ='PLC']/n1:name" />
	</td>
	</tr>
	
	</xsl:for-each>
	</table>
	</xsl:if>

	

	
	<!-- *********************************************************************  -->
	<!-- ********************* Narrative Section *****************************  -->
	<!-- *********************************************************************  -->
		
	<xsl:variable name="encounterXpath" select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.3']" />	
	<xsl:choose>
	   <xsl:when test="($encounterXpath/n1:entry/n1:encounter/n1:text)and(not($encounterXpath/n1:entry/n1:encounter/n1:code/n1:originalText))">
		<h4 align='center'><b><xsl:text>Encounter Details</xsl:text></b></h4>
		<table border="0" cellpadding="0" width="100%">
			<xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.3']/n1:entry/n1:encounter/n1:text">
				<xsl:sort select="(../n1:effectiveTime/n1:low/@value)|(../n1:effectiveTime/@value)" order="descending"/>
				
				<xsl:variable name="ENCTextValueText" select="substring(./n1:reference/@value, 2)" />
				<tr><td><h4 align='center'><br/><b><xsl:text>Encounter # </xsl:text><xsl:value-of select="position()"/>
						<xsl:text> </xsl:text>
						<xsl:value-of select="../n1:code/@displayName"/>
						<xsl:if  test="../n1:participant[n1:templateId/@root='2.16.840.1.113883.10.20.1.45']/n1:participantRole[@classCode = 'SDLOC']/n1:playingEntity[@classCode ='PLC']/n1:name != ''">
							<xsl:text> at </xsl:text><xsl:value-of select="../n1:participant[n1:templateId/@root='2.16.840.1.113883.10.20.1.45']/n1:participantRole[@classCode = 'SDLOC']/n1:playingEntity[@classCode ='PLC']/n1:name" />
						</xsl:if>
						<br/><xsl:text> Date: </xsl:text>
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
					</xsl:choose></b></h4></td></tr>
				<tr><td width="100%">
				<xsl:choose>
					<xsl:when test="$ENCTextValueText = (../../../n1:text/n1:content/@ID)">
					    <xsl:choose>
					       <xsl:when test="../../../n1:text/n1:content[contains (@ID, $ENCTextValueText)] != ''">
					    		<xsl:apply-templates select="../../../n1:text/n1:content[contains (@ID, $ENCTextValueText)]"/>
					       </xsl:when>
					       <xsl:otherwise>
					       		<xsl:text>The medical source did not provide any encounter information for this date.    Please review other sections of this Continuity of Care Document for information for this date.</xsl:text>
						</xsl:otherwise>
					    </xsl:choose>
					</xsl:when>
					<xsl:otherwise>
						<xsl:text>The medical source did not provide any encounter information for this date.    Please review other sections of this Continuity of Care Document for information for this date.</xsl:text>
					</xsl:otherwise>	
				</xsl:choose>
				<br /><br /></td></tr>
			</xsl:for-each>	
		</table>
	   </xsl:when>
	</xsl:choose>			

<!-- BLOCK START
	 The following block is in place as a backwards compatibility check in case 
	 MedVA has not updated their code to be in compliant with the above block.
	 Once they are compliant revert to the previous version in MKS  -->
	 
	<xsl:choose>
	   <xsl:when test="($encounterXpath/n1:entry/n1:encounter/n1:code/n1:originalText)">
		<h4 align='center'><b><xsl:text>Encounter Details</xsl:text></b></h4>
		<table border="0" cellpadding="0" width="100%">
			<xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.3']/n1:entry/n1:encounter/n1:code">
				<xsl:sort select="(../n1:effectiveTime/n1:low/@value)|(../n1:effectiveTime/@value)" order="descending"/>
				
				<xsl:variable name="ENCTextValueOriginalText" select="substring(./n1:originalText/n1:reference/@value, 2)" />
				<tr><td><h4 align='center'><br/><b><xsl:text>Encounter # </xsl:text><xsl:value-of select="position()"/>
						<xsl:text> </xsl:text>
						<xsl:value-of select="./@displayName"/>
						<xsl:if  test="../n1:participant[n1:templateId/@root='2.16.840.1.113883.10.20.1.45']/n1:participantRole[@classCode = 'SDLOC']/n1:playingEntity[@classCode ='PLC']/n1:name != ''">
							<xsl:text> at </xsl:text><xsl:value-of select="../n1:participant[n1:templateId/@root='2.16.840.1.113883.10.20.1.45']/n1:participantRole[@classCode = 'SDLOC']/n1:playingEntity[@classCode ='PLC']/n1:name" />
						</xsl:if>
						<br/><xsl:text> Date: </xsl:text>
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
					</xsl:choose></b></h4></td></tr>
				<tr><td width="100%">
				<xsl:choose>
					<xsl:when test="$ENCTextValueOriginalText = (../../../n1:text/n1:content/@ID)">
					    <xsl:choose>
					       <xsl:when test="../../../n1:text/n1:content[contains (@ID, $ENCTextValueOriginalText)] != ''">
					    		<xsl:apply-templates select="../../../n1:text/n1:content[contains (@ID, $ENCTextValueOriginalText)]"/>
					       </xsl:when>
					       <xsl:otherwise>
					       		<xsl:text>The medical source did not provide any encounter information for this date. Please review other sections of this Continuity of Care Document for information for this date.</xsl:text>
						</xsl:otherwise>
					    </xsl:choose>
					</xsl:when>
					<xsl:otherwise>
						<xsl:text>The medical source did not provide any encounter information for this date.    Please review other sections of this Continuity of Care Document for information for this date.</xsl:text>
					</xsl:otherwise>	
				</xsl:choose>
				<br /><br /></td></tr>
			</xsl:for-each>	
		</table>
	   </xsl:when>
	</xsl:choose>	
<!-- BLOCK END  -->		

	<!-- Call HTML table formatting -->
	<xsl:if test="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.3']/n1:text/n1:table" >
	<xsl:apply-templates select="n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.3']/n1:text/n1:table"/>
	</xsl:if>	

	
	
	
	<!-- *********************************************************************  -->
	<!-- ********************* Comments Section ******************************  -->
	<!-- *********************************************************************  -->
	<xsl:choose>
		<xsl:when test="(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.3']//n1:code[contains (@code, '48767-8')])and(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.3']/n1:text !='')">
			<h4 align='center'><b><xsl:text>Comments</xsl:text></b></h4>
			<xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.3']//n1:code[contains (@code, '48767-8')]" >	
				<xsl:variable name="encComment" select="substring(../n1:text/n1:reference/@value, 2)" />
				<xsl:if test="$encComment = (/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.3']/n1:text/n1:content/@ID)">
					<xsl:value-of select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.3']/n1:text/n1:content[contains (@ID, $encComment)]"/>
				<br/>
				</xsl:if>
			</xsl:for-each>
		</xsl:when>
	</xsl:choose>

<hr />	

</xsl:if>
 </xsl:template>	
 </xsl:stylesheet>
