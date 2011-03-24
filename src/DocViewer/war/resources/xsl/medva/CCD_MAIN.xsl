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
 xmlns:fo="http://www.w3.org/1999/XSL/Format"
 extension-element-prefixes="java">
 

  <!-- *********************** -->
  <!-- **INCLUDED XSL FILES ** -->  
  <!-- *********************** -->
  <xsl:import href="./CCD_FUNCTIONS.xsl"/>
  <xsl:import href="./CCD_COVERPAGE.xsl"/>


	
  <xsl:import href="./CCD_COMPLETE_DEMOGRAPHICS.xsl" />
  <xsl:import href="./CCD_COMPLETE_PHYSICIAN_LIST.xsl" />
  <xsl:import href="./CCD_COMPLETE_PAYERS.xsl" />
  <xsl:import href="./CCD_COMPLETE_ADVANCE_DIRECTIVES.xsl" />
  <xsl:import href="./CCD_COMPLETE_PROBLEM_LIST.xsl" />
  <xsl:import href="./CCD_COMPLETE_ALLERGY.xsl" />
  <xsl:import href="./CCD_COMPLETE_SUPPORT.xsl" />
  <xsl:import href="./CCD_COMPLETE_MEDICATIONS.xsl" />
  <xsl:import href="./CCD_COMPLETE_MEDICAL_EQUIPMENT.xsl" />
  <xsl:import href="./CCD_COMPLETE_IMMUNIZATION.xsl" />
  <xsl:import href="./CCD_COMPLETE_FUNCTIONAL_STATUS.xsl" />
  <xsl:import href="./CCD_COMPLETE_VITAL_SIGNS.xsl" />
  <xsl:import href="./CCD_COMPLETE_RESULTS.xsl" />
  <xsl:import href="./CCD_COMPLETE_PROCEDURES.xsl" />
  <xsl:import href="./CCD_COMPLETE_FAMILY_HISTORY.xsl" />
  <xsl:import href="./CCD_COMPLETE_ENCOUNTERS.xsl" />
  <xsl:import href="./CCD_COMPLETE_PLAN_OF_CARE.xsl" />
  <xsl:import href="./CCD_COMPLETE_SOCIAL_HISTORY.xsl" />
  
 
<xsl:output method="html" indent="yes" version="4.01" encoding="ISO-8859-1" doctype-public="-//W3C//DTD HTML 4.01//EN"/> 

	
	<!-- **************************************  FORM HEADER ********************************************  -->
	<!-- ************************************************************************************************  -->
	<!-- ******************** Claimant Health Summary - Social Security Administration ******************  -->
	<!-- ************************************************************************************************  -->

	<xsl:param name = "terminologyService"/>

	<!-- CDA document -->

	<xsl:variable name="tableWidth">100%</xsl:variable>

	<!-- ** Title variable ** -->
	<xsl:variable name="title">
	<xsl:choose>
        <xsl:when test="(/n1:ClinicalDocument/n1:title)!=''">
        <xsl:value-of select="/n1:ClinicalDocument/n1:title"/>
        </xsl:when>
        <xsl:otherwise>Health Summary Document</xsl:otherwise>
    	</xsl:choose>
	</xsl:variable>
	
	<!-- ** Template Match ** -->
	<xsl:template match="/">
	<xsl:apply-templates select="n1:ClinicalDocument"/>
	</xsl:template>

	<!-- ** Break ** -->
	<xsl:template match="n1:br">
    <br/>
    </xsl:template> 
	

	<xsl:template match="n1:ClinicalDocument">
	
	<!-- ** Patient Name variable ** -->
	<xsl:variable name="patientRole" select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole"/>
	
	<html>
	<head>

	
	<!-- ** Title ** -->
	<title>
	<xsl:value-of select="$title"/>
	</title>
	</head>


	<body> 

	<!-- ** Call Coverpage ** -->
	<xsl:call-template name="COVERPAGE" />
	
	
        <!-- ****************************************************************  -->
	<!-- *************** Health Summary - Table of Contents *************  -->
	<!-- ****************************************************************  --> 
 	<DIV style="page-break-after:always">
        <h3><a name="xoc">Table of Contents</a></h3>
        <ul>
    	    <li><xsl:text>Complete Report [COMPLETE]</xsl:text></li>
			<!---->
        </ul>
        </DIV>
  	

<hr/>
	<!-- ****************************************************************  -->
	<!-- *************** XSL Template Calls *****************************  -->
	<!-- ****************************************************************  --> 
	


	<xsl:call-template name="COMPLETE_DEMOGRAPHICS" />
	<xsl:call-template name="COMPLETE_PHYSICIAN_LIST" />
	<xsl:call-template name="COMPLETE_PAYERS" />
	<xsl:call-template name="COMPLETE_ADVANCE_DIRECTIVES" />
	<xsl:call-template name="COMPLETE_PROBLEM_LIST" />
	<xsl:call-template name="COMPLETE_ALLERGY" />
	<xsl:call-template name="COMPLETE_SUPPORT" />
	<xsl:call-template name="COMPLETE_MEDICATIONS" />
	<xsl:call-template name="COMPLETE_MEDICAL_EQUIPMENT" />
	<xsl:call-template name="COMPLETE_IMMUNIZATION" />
	<xsl:call-template name="COMPLETE_FUNCTIONAL_STATUS" />
	<xsl:call-template name="COMPLETE_VITAL_SIGNS" />
	<xsl:call-template name="COMPLETE_RESULTS" />
	<xsl:call-template name="COMPLETE_PROCEDURES" />
	<xsl:call-template name="COMPLETE_FAMILY_HISTORY" />
	<xsl:call-template name="COMPLETE_ENCOUNTERS" />
	<xsl:call-template name="COMPLETE_PLAN_OF_CARE" />
	<xsl:call-template name="COMPLETE_SOCIAL_HISTORY" />


	</body>
	</html>
				
	
	</xsl:template>
	

</xsl:stylesheet>