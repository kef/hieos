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

<xsl:template name="COMPLETE_MEDICATIONS">
	<xsl:if test="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.8']">
	<!-- *******************************************************  -->
	<!-- **************** Complete - Medications ***************  -->
	<!-- *******************************************************  -->
	

	<h4 align='center'><b><xsl:text>Medications</xsl:text></b></h4><br />
	
	
	<xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.8']/n1:entry/n1:substanceAdministration">
	<xsl:sort select="./n1:entryRelationship/n1:supply/n1:author/n1:time/@value" order="descending"/>

	
	<h4 align='center'><b><xsl:text>Medication Information</xsl:text></b></h4><br />
	<table border="0" cellpadding="0" width="100%">
	
	<tr>
	<!-- ** Coded Product Name ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Coded Product Name: </xsl:text></b>
	<xsl:value-of select="./n1:consumable/n1:manufacturedProduct/n1:manufacturedMaterial/n1:code/@displayName"/>
	</td>
	
	<!-- ** Coded Brand Name ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Coded Brand Name: </xsl:text></b>
	<xsl:value-of select="./n1:consumable/n1:manufacturedProduct/n1:manufacturedMaterial/n1:code/n1:translation/@displayName"/>
	</td>	
	
	<!-- ** Free Text Product Name ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Free Text Product Name: </xsl:text></b>
	<xsl:value-of select="./n1:consumable/n1:manufacturedProduct/n1:manufacturedMaterial/n1:code/n1:originalText"/>
	</td>		
	
	</tr>
	
	<tr>
	<!-- ** Free Text Brand Name ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Free Text Brand Name: </xsl:text></b>
	<xsl:value-of select="./n1:consumable/n1:manufacturedProduct/n1:manufacturedMaterial/n1:name"/>
	</td>	

	<!-- ** Drug Manufacturer ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Drug Manufacturer: </xsl:text></b>
	<xsl:value-of select="./n1:consumable/n1:manufacturedProduct/n1:manufacturerOrganization/n1:name"/>
	</td>		
	
	<!-- ** Type of Medication ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Type of Medication: </xsl:text></b>
	<xsl:value-of select="(./n1:entryRelationship[@typeCode='SUBJ']/n1:observation[n1:templateId/@root ='2.16.840.1.113883.3.88.11.32.10']/n1:value/@displayName)|(./n1:entryRelationship[@typeCode='SUBJ']/n1:observation[n1:templateId/@root ='2.16.840.1.113883.3.88.11.32.10']/n1:code/@displayName)" />
	</td>		
	
	</tr>
	
	<tr>
	<!-- ** Status of Medication ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Status of Medication: </xsl:text></b>
	<xsl:value-of select="./n1:entryRelationship[@typeCode='REFR']/n1:observation[n1:templateId/@root ='2.16.840.1.113883.10.20.1.47']/n1:value/@displayName"/>
	</td>	

	<!-- ** Indication ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Indication: </xsl:text></b>
	<xsl:for-each select="./n1:entryRelationship/n1:observation/n1:templateId[contains(@root,'2.16.840.1.113883.10.20.1.28')]/../n1:code">
		<xsl:value-of select="./@displayName" />
		<xsl:if test="../n1:text/n1:reference/@value">
			<xsl:text> - </xsl:text>
			<xsl:variable name="indicationTextValue" select="substring(../n1:text/n1:reference/@value, 2)"/>
			<xsl:if test="$indicationTextValue = (../../../../../n1:text/n1:content/@ID)">
				<xsl:value-of select="../../../../../n1:text/n1:content[contains (@ID, $indicationTextValue)]"/>				
			</xsl:if>
		</xsl:if>
		<xsl:if test="position() != last()">
			<xsl:text> / </xsl:text>
		</xsl:if>
	</xsl:for-each>	
	</td>		
	
	<!-- ** Patient Instructions ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Patient Instructions: </xsl:text></b>
	<xsl:variable name="PITextValue" select="substring(./n1:entryRelationship/n1:act[n1:templateId/@root = '2.16.840.1.113883.10.20.1.49']/n1:text/n1:reference/@value, 2)"/>
	<xsl:if test="$PITextValue = (../../n1:text/n1:content/@ID)">
		<xsl:value-of select="../../n1:text/n1:content[contains (@ID, $PITextValue)]"/>	
	</xsl:if>	
	</td>		
	
	</tr>	
	
	<tr>
	<!-- ** Reaction ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Reaction: </xsl:text></b>
	<xsl:variable name="reactionTextValue" select="substring(./n1:entryRelationship[@typeCode = 'CAUS']/n1:observation[n1:templateId/@root = '2.16.840.1.113883.10.20.1.54']/n1:text/n1:reference/@value, 2)"/>
	<xsl:if test="$reactionTextValue = (../../n1:text/n1:content/@ID)">
		<xsl:value-of select="../../n1:text/n1:content[contains (@ID, $reactionTextValue)]"/>				
	</xsl:if>
	</td>
	
	<!-- ** Vehicle ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Vehicle: </xsl:text></b>
	<xsl:for-each select="./n1:participant/n1:participantRole[n1:code/@code = '412307009' and n1:code/@codeSystem = '2.16.840.1.113883.6.96']/n1:playingEntity">
		<xsl:value-of select="./n1:name" />
		<xsl:if test="position() != last()">
			<xsl:text> / </xsl:text>
		</xsl:if>				   	 				
	</xsl:for-each>
	</td>	
	
	<!-- ** Dose Indicator ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Dose Indicator: </xsl:text></b>
	<xsl:for-each select="./n1:precondition">
		<xsl:value-of select="./n1:criterion/n1:text" />
		<xsl:if test="position() != last()">
			<xsl:text> / </xsl:text>
		</xsl:if>	
	</xsl:for-each>
	</td>		
	
	</tr>	
	
	</table>
	
	<h4 align='center'><b><xsl:text>Administration Information Event Entry</xsl:text></b></h4><br />
	<table border="0" cellpadding="0" width="100%">

	<tr>
	<!-- ** Start Date ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Start Date: </xsl:text></b>
	<xsl:choose>
		<xsl:when test="./n1:effectiveTime[1]/n1:low/@value">
			<xsl:call-template name="formatDate">
				<xsl:with-param name="date" select="./n1:effectiveTime[1]/n1:low/@value"/>
			</xsl:call-template>
		</xsl:when>
		<xsl:when test="./n1:effectiveTime[1]/@value">
			<xsl:call-template name="formatDate">
				<xsl:with-param name="date" select="./n1:effectiveTime[1]/@value"/>
			</xsl:call-template>
		</xsl:when>
		<xsl:otherwise>
			<xsl:text>No date available</xsl:text>
		</xsl:otherwise>
	</xsl:choose>
	</td>	

	<!-- ** Stop Date ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Stop Date: </xsl:text></b>
		<xsl:if test="./n1:effectiveTime[1]/n1:high/@value">
					<xsl:call-template name="formatDate">
						<xsl:with-param name="date" select="./n1:effectiveTime[1]/n1:high/@value"/>
					</xsl:call-template>
		</xsl:if>
		<xsl:if test="not(./n1:effectiveTime[1]/n1:high/@value)">
			<xsl:text>No date available</xsl:text>
		</xsl:if>
	</td>		
	
	<!-- ** Frequency ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Frequency: </xsl:text></b>
	<xsl:choose>
		<xsl:when test="(./n1:effectiveTime/@xsi:type = 'PIVL_TS')and((./n1:effectiveTime/@institutionSpecified = 'false')or(not(./n1:effectiveTime/@institutionSpecified)))" >
			<xsl:text>Every </xsl:text><xsl:value-of select="./n1:effectiveTime/n1:period/@value" /><xsl:value-of select="./n1:effectiveTime/n1:period/@unit" />
		</xsl:when>
		<xsl:when test="(./n1:effectiveTime/@xsi:type = 'PIVL_TS')and(./n1:effectiveTime/@institutionSpecified = 'true')" >
			<xsl:variable name="freq" select="./n1:effectiveTime/n1:period/@value"/>
			<xsl:choose>
				<xsl:when test="$freq='24'">One per Day</xsl:when>
				<xsl:when test="$freq='12'">Two per Day</xsl:when>
				<xsl:when test="$freq='8'">Three per Day</xsl:when>
				<xsl:when test="$freq='6'">Four per Day</xsl:when>
				<xsl:when test="$freq='4'">Six per Day</xsl:when>
				<xsl:when test="$freq='3'">Eight per Day</xsl:when>
				<xsl:when test="$freq='2'">Twelve per Day</xsl:when>
			</xsl:choose>
		</xsl:when>
		<xsl:when test="(./n1:effectiveTime/@xsi:type = 'EIVL')" >
			<xsl:variable name="event" select="./n1:effectiveTime/n1:event/@code"/>
			<xsl:choose>
				<xsl:when test="$event='AC'">Before meal</xsl:when>
				<xsl:when test="$event='ACM'">Before breakfast</xsl:when>
				<xsl:when test="$event='ACD'">Before lunch</xsl:when>
				<xsl:when test="$event='ACV'">Before dinner</xsl:when>
				<xsl:when test="$event='HC'">The hour of sleep</xsl:when>
				<xsl:when test="$event='IC'">Between meals</xsl:when>
				<xsl:when test="$event='ICD'">Between lunch and dinner</xsl:when>
				<xsl:when test="$event='ICM'">Between breakfast and lunch</xsl:when>
				<xsl:when test="$event='ICV'">Between dinner and the hour of sleep</xsl:when>
				<xsl:when test="$event='PC'">After meal</xsl:when>
				<xsl:when test="$event='PCD'">After lunch</xsl:when>
				<xsl:when test="$event='PCM'">After breakfast</xsl:when>
				<xsl:when test="$event='PCV'">After dinner</xsl:when>
			</xsl:choose>
		</xsl:when>	
	</xsl:choose>
	</td>		
	
	</tr>

	<tr>
	<!-- ** Route ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Route: </xsl:text></b>
	 <xsl:for-each select="./n1:routeCode">
		<xsl:value-of select="./@displayName" />
		<xsl:if test="position() != last()">
			<xsl:text> / </xsl:text>
		</xsl:if>
	 </xsl:for-each>
	</td>
	
	<!-- ** Dose ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Dose: </xsl:text></b>
	<xsl:for-each select="./n1:doseQuantity">
		<xsl:value-of select="./@value"/><xsl:text> </xsl:text><xsl:value-of select="./@unit" />
		<xsl:if test="position() != last()">
			<xsl:text> / </xsl:text>
		</xsl:if>	
	</xsl:for-each>
	</td>	
	
	<!-- ** Site ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Site: </xsl:text></b>
  	<xsl:for-each select="./n1:approachSiteCode">
	  	<xsl:value-of select="./@displayName" />
		<xsl:if test="position() != last()">
			<xsl:text> / </xsl:text>
		</xsl:if>								  
	</xsl:for-each>	
	</td>		
	
	</tr>

	<tr>
	<!-- ** Max Dose Quantity ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Max Dose Quantity: </xsl:text></b>
        <xsl:for-each select="./n1:maxDoseQuantity">
	 	<xsl:value-of select="./n1:numerator/@value" />
		<xsl:if test="position() != last()">
			<xsl:text> / </xsl:text>
		</xsl:if>
	</xsl:for-each>
	</td>
	
	<!-- ** Product Form ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Product Form: </xsl:text></b>
	<xsl:value-of select="./n1:administrationUnitCode/@displayName" />				   	 				
	</td>	
	
	<!-- ** Delivery Method ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Delivery Method: </xsl:text></b>
	<xsl:for-each select="./n1:code">
	 	<xsl:value-of select="./n1:originalText" />
		<xsl:if test="position() != last()">
			<xsl:text> / </xsl:text>
		</xsl:if>
	</xsl:for-each>	
	</td>		
	
	</tr>		

	<tr>
	<!-- ** Free Text Sig ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Free Text Sig: </xsl:text></b>
	<xsl:variable name="freeTextSigValue" select="substring(./n1:text/n1:reference/@value, 2)"/>
	<xsl:if test="$freeTextSigValue = (../../n1:text/n1:content/@ID)">
		<xsl:value-of select="../../n1:text/n1:content[contains (@ID, $freeTextSigValue)]"/>				
	</xsl:if>
	</td>

	</tr>
	
	
	
	</table>

   <xsl:if test=".//n1:entryRelationship[@typeCode ='REFR']/n1:supply[@moodCode = 'INT']">
	<h4 align='center'><b><xsl:text>Order Information</xsl:text></b></h4><br />
	<xsl:variable name="medOrdInfoSec" select=".//n1:entryRelationship[@typeCode ='REFR']/n1:supply[@moodCode = 'INT']"/>
	
	<xsl:for-each select="$medOrdInfoSec">
	<table border="0" cellpadding="0" width="100%">

	<tr>
	<!-- ** Order Number ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Order Number: </xsl:text></b>
	<xsl:value-of select="$medOrdInfoSec/n1:id/@root" />
	<xsl:if test="./n1:id/@extension">
		<xsl:text> / </xsl:text>
	<xsl:value-of select="./n1:id/@extension" />
	</xsl:if> 
	</td>
	
	<!-- ** Fills ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Fills: </xsl:text></b>
	<xsl:value-of select="./n1:repeatNumber/@value" />				   	 				
	</td>	
	
	<!-- ** Quantity Ordered ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Quantity Ordered: </xsl:text></b>
	<xsl:value-of select="./n1:quantity/@value" />
	<xsl:text> </xsl:text>
	<xsl:value-of select="./n1:quantity/@unit" />	
	</td>		
	
	</tr>

	<tr>
	<!-- ** Order Expiration Date/Time ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Order Expiration Date/Time: </xsl:text></b>
	<xsl:choose>
		<xsl:when test="./n1:effectiveTime/n1:high/@value">
			<xsl:call-template name="formatDate">
	 			<xsl:with-param name="date" select="./n1:effectiveTime/n1:high/@value"/>
			</xsl:call-template>
			<xsl:text> </xsl:text>
	 		<xsl:call-template name="formatTime">
	 			<xsl:with-param name="time" select="./n1:effectiveTime/n1:high/@value"/>
			</xsl:call-template>										
		</xsl:when>
		<xsl:when  test="$medOrdInfoSec/n1:effectiveTime/@value">
	 		<xsl:call-template name="formatDate">
	 			<xsl:with-param name="date" select="./n1:effectiveTime/@value"/>
			</xsl:call-template>
			<xsl:text>  </xsl:text>
	 		<xsl:call-template name="formatTime">
	 			<xsl:with-param name="time" select="./n1:effectiveTime/@value"/>
			</xsl:call-template>											
	 	</xsl:when>
	</xsl:choose>
	</td>
	
	<!-- ** Order Date/Time ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Order Date/Time: </xsl:text></b>
	<xsl:if test="./n1:author/n1:time/@value" >
		<xsl:call-template name="formatDate">
	 		<xsl:with-param name="date" select="./n1:author/n1:time/@value"/>
		</xsl:call-template>		
	 	<xsl:text>  </xsl:text>
	 	<xsl:call-template name="formatTime">
	 		<xsl:with-param name="time" select="./n1:author/n1:time/@value"/>
		</xsl:call-template>
	</xsl:if>				   	 				
	</td>	
	
	<!-- ** Ordering Provider ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Ordering Provider: </xsl:text></b>
	<xsl:value-of select="./n1:author/n1:assignedAuthor/n1:assignedPerson/n1:name/n1:prefix"/>
	<xsl:text> </xsl:text>
	<xsl:value-of select="./n1:author/n1:assignedAuthor/n1:assignedPerson/n1:name/n1:given"/>
	<xsl:text> </xsl:text>
	<xsl:value-of select="./n1:author/n1:assignedAuthor/n1:assignedPerson/n1:name/n1:family"/>
	</td>		
	
	</tr>	
	</table>
	</xsl:for-each>
	
	<table>
	<tr>
	<!-- ** Fulfillment Instructions ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Fulfillment Instructions: </xsl:text></b>
	<xsl:variable name="fulfillmentText" select="substring(./n1:entryRelationship/n1:act[n1:templateId/@root = '2.16.840.1.113883.10.20.1.43']/n1:text/n1:reference/@value, 2)"/>
	<xsl:if test="$fulfillmentText = (../../n1:text/n1:content/@ID)">
		<xsl:value-of select="../../n1:text/n1:content[contains (@ID, $fulfillmentText)]"/>				
	</xsl:if>
	</td>	
	
	</tr>
	</table>
   </xsl:if>		
	
   <xsl:if test="./n1:entryRelationship[@typeCode ='REFR']/n1:supply[@moodCode = 'EVN']">
	<h4 align='center'><b><xsl:text>Order Fulfillment History</xsl:text></b></h4><br />
	<xsl:variable name="medOrdFulfillSec" select="./n1:entryRelationship[@typeCode ='REFR']/n1:supply[@moodCode = 'EVN']"/>
	
	<xsl:for-each select="$medOrdFulfillSec">
	<table border="0" cellpadding="0" width="100%">
	
	<tr>
	<!-- ** Prescription Number ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Prescription Number: </xsl:text></b>
 	<xsl:value-of select="./n1:id/@root" />
 	<xsl:if test="./n1:id/@extension">
 		<xsl:text> / </xsl:text>
		<xsl:value-of select="./n1:id/@extension" />
	</xsl:if> 
	</td>
	
	<!-- ** Provider ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Provider: </xsl:text></b>
	<xsl:call-template name="getName">
		<xsl:with-param name="name" select="./n1:performer/n1:assignedEntity/n1:assignedPerson/n1:name"/>
	</xsl:call-template><br />
	<xsl:value-of select="./n1:performer/n1:assignedEntity/n1:representedOrganization/n1:name" />
	</td>	
	
	<!-- ** Location ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Location: </xsl:text></b>
	<xsl:if test="./n1:performer/n1:assignedEntity/n1:addr">
	<xsl:call-template name="getAddress"> 
	<xsl:with-param name="addr" select="./n1:performer/n1:assignedEntity/n1:addr"/>
	</xsl:call-template>
	</xsl:if>	
	</td>		
	
	</tr>	

	<tr>
	<!-- ** Dispense Date ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Dispense Date: </xsl:text></b>
	<xsl:choose>
		<xsl:when test="./n1:effectiveTime/n1:high/@value">
	 		<xsl:call-template name="formatDate">
				<xsl:with-param name="date" select="./n1:effectiveTime/n1:high/@value"/>
			</xsl:call-template>
		</xsl:when>
		<xsl:when  test="./n1:effectiveTime/@value">
			<xsl:call-template name="formatDate">
	 			<xsl:with-param name="date" select="./n1:effectiveTime/@value"/>
			</xsl:call-template>
	 	</xsl:when>
	</xsl:choose>
	</td>
	
	<!-- ** Quantity Dispensed ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Quantity Dispensed: </xsl:text></b>
	<xsl:value-of select="./n1:quantity/@value" />
	<xsl:text> </xsl:text>
	<xsl:value-of select="./n1:quantity/@unit" />			   	 				
	</td>	
	
	<!-- ** Fill Number ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Fill Number: </xsl:text></b>
	<xsl:value-of select="./n1:entryRelationship[contains (@typeCode, 'COMP')]/n1:sequenceNumber/@value" />
	</td>		
	
	</tr>		

	<tr>
	<!-- ** Fill Status ** -->
	<td width='33%' align='left' valign="top">	
	<b><xsl:text>Fill Status: </xsl:text></b>
	<xsl:value-of select="./n1:statusCode/@code" />
	</td>	
	
	</tr>
	
	</table>
	</xsl:for-each>
   </xsl:if>	
   	
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
	<xsl:if test="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.8']/n1:text/n1:table" >
	<xsl:apply-templates select="n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.8']/n1:text/n1:table"/>
	</xsl:if>
	

	<!-- *********************************************************************  -->
	<!-- ********************* Comments Section ******************************  -->
	<!-- *********************************************************************  -->
	<xsl:choose>
		<xsl:when test="(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.8']//n1:code[contains (@code, '48767-8')])and(/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.8']/n1:text !='')">
			<h4 align='center'><b><xsl:text>Comments</xsl:text></b></h4>
			<xsl:for-each select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.8']//n1:code[contains (@code, '48767-8')]" >	
				<xsl:variable name="MedComment" select="substring(../n1:text/n1:reference/@value, 2)" />
				<xsl:if test="$MedComment = (/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.8']/n1:text/n1:content/@ID)">
					<xsl:value-of select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.8']/n1:text/n1:content[contains (@ID, $MedComment)]"/>
				<br/>
				</xsl:if>
			</xsl:for-each>
		</xsl:when>
	</xsl:choose>


<hr />
</xsl:if>
 </xsl:template>	
 </xsl:stylesheet>