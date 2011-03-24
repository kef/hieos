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

<xsl:template name="COMPLETE_PHYSICIAN_LIST">

<xsl:choose>
	<xsl:when test="not(/n1:ClinicalDocument/n1:documentationOf/n1:serviceEvent/n1:performer) =''">
	<!-- *******************************************************  -->
	<!-- *********** Complete - Healthcare Providers ***********  -->
	<!-- *******************************************************  -->


	<h4 align='center'><b><xsl:text>Healthcare Providers</xsl:text></b></h4>

	<!-- ** Physician ** -->
	<xsl:for-each select="/n1:ClinicalDocument/n1:documentationOf/n1:serviceEvent/n1:performer">
	<xsl:sort select="(./n1:time/n1:low/@value)|(./n1:time/@value)" order="descending"/>

	<table border="0" cellpadding="0" width="100%">
	
	<tr>
	<td width='50%' align='left' valign="top"><b><xsl:text>Name: </xsl:text></b></td>


	<!-- ** Speciality/Type ** -->
	<td width='50%' align='left' valign="top"><b><xsl:text>Type: </xsl:text></b>
	<xsl:variable name="class" select="./n1:functionCode/@code"/>
	<xsl:choose>
	<xsl:when test="$class='PP'">Primary Care Physician</xsl:when>
	<xsl:when test="$class='ADMPHYS'">Admitting Physician</xsl:when>
	<xsl:when test="$class='ATTPHYS'">Attending Physician</xsl:when>
	<xsl:when test="$class='DISPHYS'">Discharging Physician</xsl:when>
	<xsl:when test="$class='MDWF'">Midwife</xsl:when>
	<xsl:when test="$class='PRISURG'">Primary Surgeon</xsl:when>
	<xsl:when test="$class='CP'">Consulting Provider</xsl:when>
	</xsl:choose></td>
	</tr>

	<tr>
	<td width='30%' align='left' valign="top"><xsl:call-template name="getPhysician">
	<xsl:with-param name="phyname" select="./n1:assignedEntity/n1:assignedPerson/n1:name"/>
	</xsl:call-template>
	</td>
	</tr>

	<tr>
	<td width='50%' align='left' valign="top"><b><xsl:text>Address 1 </xsl:text></b></td>
	<td width='50%' align='left' valign="top"><b><xsl:text>Address 2 </xsl:text></b></td>
	</tr>

	<!-- ** Physician's Address ** -->
	<tr>
	<td width='50%' align='left' valign="top">
	<xsl:if test="./n1:assignedEntity/n1:addr">
	<xsl:call-template name="getAddress"> 
	<xsl:with-param name="addr" select="./n1:assignedEntity/n1:addr"/>
	</xsl:call-template>
	</xsl:if>

	<!-- ** Physician's Telephone ** -->		 		 		 		 
	<xsl:if test="./n1:performer/n1:assignedEntity/n1:telecom">
	<br />
	<xsl:call-template name="getTelecom"> 
	<xsl:with-param name="telecom" select="./n1:assignedEntity/n1:telecom"/>
	</xsl:call-template>
	</xsl:if>
	
	<!-- ** Physician's Email Address ** -->
			
	<xsl:for-each select="./n1:assignedEntity/n1:telecom">
		<xsl:call-template name="matchPattern">
			<xsl:with-param name="input" select="./@value" />
			<xsl:with-param name="expression" select="'mailto:[\w\-\.]+@([\w-]+\.)+[\w-]+'"/>
		</xsl:call-template>
	</xsl:for-each>
	
	<!-- ** URL ** -->
				
	<xsl:for-each select="./n1:associatedEntity/n1:telecom">
		<xsl:call-template name="matchPattern">
			<xsl:with-param name="input" select="./@value" />
			<xsl:with-param name="expression" select="'(http|https|ftp)://([\w-]+\.)+[\w-]+(/[\w- ./?%=]*)?'" />
		</xsl:call-template>
	</xsl:for-each>
	</td>

	<!-- ** Physician's Second Address ** -->
	<xsl:if test="(./n1:assignedEntity[n1:addr/@use='WP'])and(./n1:assignedEntity/n1:addr[2] !='')">
	<td width='50%' align='left' valign="top">
	<xsl:call-template name="getAddress"> 
	<xsl:with-param name="addr" select="./n1:assignedEntity/n1:addr[2]"/>
	</xsl:call-template>
	<br /></td>
	</xsl:if>
	</tr>


	<!-- ** Organization ** -->
	<tr><td width='50%' align='left' valign="top"><b><xsl:text>Organization Name/ Facility</xsl:text></b>
	</td></tr>
	<tr><td>
	<xsl:value-of select="./n1:assignedEntity/n1:representedOrganization/n1:name"/>
	</td></tr>


	<!-- ** Get Date Range ** --> 
	<tr><td width='50%' align='left' valign="top"><b><xsl:text>Date Range</xsl:text></b></td>
	<!-- **** MRN **** -->
	<td width='50%' align='left' valign="top"><b><xsl:text>Patient's Medical Record Number (MRN)</xsl:text></b></td>
	</tr>

	<tr>

	<xsl:choose>
		<xsl:when test="(./n1:time/n1:low/@value)and(./n1:time/n1:high/@value)">
			<td>
			<xsl:call-template name="formatDate">
			<xsl:with-param name="date" select="./n1:time/n1:low/@value"/>
			</xsl:call-template>
			<xsl:text> - </xsl:text>
			<xsl:call-template name="formatDate">
			<xsl:with-param name="date" select="./n1:time/n1:high/@value"/>
			</xsl:call-template>
			</td>
		</xsl:when>
		<xsl:when test="(./n1:time/n1:low/@value)and not(./n1:time/n1:high/@value)">
			<td>
			<xsl:call-template name="formatDate">
			<xsl:with-param name="date" select="./n1:time/n1:low/@value"/>
			</xsl:call-template>
			</td>
		</xsl:when>
		<xsl:otherwise>
			<td><xsl:text>No date available</xsl:text></td>
		</xsl:otherwise>
	</xsl:choose>
	<td>
	
	<xsl:value-of select="./n1:assignedEntity/sdtc:patient/sdtc:id/@extension"/>
	</td>
	</tr>
	
	</table>

	<xsl:if test="position() != last()" >
		<tr>
		<td><hr width="75%"  noshade="false" /></td>

		</tr>
	</xsl:if>
	
	</xsl:for-each>
	<hr/>
</xsl:when>
</xsl:choose>


 </xsl:template>	
 </xsl:stylesheet>