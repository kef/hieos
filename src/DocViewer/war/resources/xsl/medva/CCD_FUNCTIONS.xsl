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
 
<!-- declare the global variable -->
<xsl:variable name="terminologyService" /> 
 
<xsl:output method="html" indent="yes" version="4.01" encoding="ISO-8859-1" doctype-public="-//W3C//DTD HTML 4.01//EN"/> 


	<!-- *******************************************  -->
	<!-- ************* Get Participant *************  -->
	<!-- *******************************************  -->

	<xsl:template name="getParticipant">
	<xsl:param name="participant"/>

	 <p>
	 <xsl:call-template name="getName">
	 <xsl:with-param name="name" select="$participant/n1:associatedPerson/n1:name"/>
	 </xsl:call-template>
 		 		 
         <xsl:if test="$participant/n1:addr">
         <xsl:call-template name="getAddress"> 
	 <xsl:with-param name="addr" select="$participant/n1:addr"/>
	 </xsl:call-template>
	 </xsl:if>
 		 		 
         <xsl:if test="$participant/n1:telecom">
         <xsl:call-template name="getTelecom"> 
	 <xsl:with-param name="telecom" select="$participant/n1:telecom"/>
	 </xsl:call-template>
 	 </xsl:if>
                
	 </p>
	 </xsl:template>


	<!-- *******************************************  -->
	<!-- ****************** Get Address ************  -->
	<!-- *******************************************  -->
	<xsl:template name="getAddress">
	<xsl:param name="addr"/>
	<xsl:if test="$addr != ''">
	<xsl:value-of select="$addr/n1:streetAddressLine"/>
	<br/><xsl:value-of select="$addr/n1:city"/>, <xsl:value-of select="$addr/n1:state"/><xsl:text> </xsl:text><xsl:value-of select="$addr/n1:postalCode"/>
	</xsl:if>
	</xsl:template>



	<!-- *******************************************  -->
	<!-- ************* Get Telecom ************  -->
	<!-- *******************************************  -->
	<xsl:template name="getTelecom">
	<xsl:param name="telecom"/>
	<xsl:value-of select="$telecom/@value"/>
	</xsl:template>
    	
	<!-- *******************************************  -->
	<!-- ************* Get Patient Name ************  -->
	<!-- *******************************************  -->
	<xsl:template name="getName">
	<xsl:param name="name"/>
	<xsl:choose>
        	<xsl:when test="$name/n1:family">
        		<xsl:value-of select="$name/n1:given"/>
        		<xsl:text> </xsl:text>
        		<xsl:value-of select="$name/n1:family"/>
        		<xsl:text> </xsl:text>
        		<xsl:if test="($name/n1:suffix)and($name/n1:suffix != '')">
       				 <xsl:text>, </xsl:text>
        			<xsl:value-of select="$name/n1:suffix"/>
       			 </xsl:if>
       		 </xsl:when>
      		 <xsl:otherwise>
       			 <xsl:value-of select="$name"/>
		</xsl:otherwise>
	</xsl:choose>
	</xsl:template>



	<!-- *******************************************  -->
	<!-- *********** Get Physician Name ************  -->
	<!-- *******************************************  -->
	<xsl:template name="getPhysician">
	<xsl:param name="phyname"/>
	<xsl:choose>
        <xsl:when test="$phyname/n1:family">
        <xsl:value-of select="$phyname/n1:prefix"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="$phyname/n1:given"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="$phyname/n1:family"/>
        <xsl:text> </xsl:text>
        <xsl:if test="$phyname/n1:prefix">
        </xsl:if>
        </xsl:when>
        <xsl:otherwise>
        <xsl:value-of select="$phyname"/>
        </xsl:otherwise>
	</xsl:choose>
	</xsl:template>


	<!-- ********************************************************  -->
	<!-- *********************** Format Time ********************  -->
    	<!-- ********************* outputs Time *********************  -->
	<!-- ********************************************************  -->
		<xsl:template name="formatTime">
        <xsl:param name="time"/>
        <xsl:variable name="Time" select="substring ($time, 9, 4)"/>
        <xsl:choose>
        <xsl:when test='substring ($time, 9, 4)=""'>
        <xsl:value-of select="substring ($time, 9, 2)"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="substring ($time, 11, 2)"/>
        </xsl:when>
        <xsl:otherwise>
        <xsl:value-of select="substring ($time, 9, 2)"/>
        <xsl:text>:</xsl:text>
        <xsl:value-of select="substring ($time, 11, 2)"/>
        </xsl:otherwise>
	</xsl:choose>
	</xsl:template>
	
	<!-- **************************************************  -->
	<!-- ********** Get Preferred Name (Java) *************  -->
	<!-- **************************************************  -->

  	<xsl:template  name="getPreferredName"> 
      	<xsl:param  name="codeSystem"/> 
      	<xsl:param  name="code"/>
      	<xsl:param  name="displayName"/>

      	<xsl:if test="string($terminologyService)">
      	<xsl:if test="string-length($code)>0">      	
      	<xsl:variable name="preferredName" select="java:getPreferredTerm($terminologyService,$codeSystem, $code)"/>
  	<xsl:choose>
	<xsl:when test="$preferredName">
	<xsl:value-of select="$preferredName"/>
	</xsl:when>
	<xsl:when test="$displayName">
	<xsl:value-of select="$displayName"/>
	</xsl:when>
	<xsl:otherwise>
	<xsl:value-of select="$code"/>
	</xsl:otherwise>
        </xsl:choose>
        </xsl:if>
        
        <xsl:if test="not(string($terminologyService))">
  	<xsl:choose>
	<xsl:when test="$displayName">
	<xsl:value-of select="$displayName"/>
	</xsl:when>
	<xsl:when test="$code">
	<xsl:value-of select="$code"/>
	</xsl:when>		
	<xsl:otherwise>
	<xsl:value-of select="''"/>
	</xsl:otherwise>
        </xsl:choose>        
	</xsl:if>
	</xsl:if>
	
  	</xsl:template> 
	
	

	<!-- *******************************************  -->
	<!-- ******** Get Component/Section ************  -->
	<!-- *******************************************  -->
	
	<xsl:template match="n1:component/n1:section">
	<xsl:apply-templates select="n1:title"/>
	<xsl:apply-templates select="n1:text"/>
	<xsl:apply-templates select="n1:component/n1:section"/>
	</xsl:template>

	<!-- *******************************************  -->
	<!-- ******** Get Table of Contents ************  -->
	<!-- *******************************************  -->
	
	<xsl:template match="n1:title">
	<h3><span style="font-weight:bold;">		 
	<a name="{generate-id(.)}" href="#toc"><xsl:value-of select="."/></a>
	</span></h3>
	</xsl:template>


	<!-- ********************************************************  -->
	<!-- *********************** Format Date ********************  -->
    	<!-- ********** outputs a date in Month Day, Year form ******  -->
        <!-- ********** e.g., 19991207  ==> December 07, 1999 *******  -->
	<!-- ********************************************************  -->

	<xsl:template name="formatDate">
        <xsl:param name="date"/>
        <xsl:variable name="month" select="substring ($date, 5, 2)"/>
        <xsl:choose>
                <xsl:when test="$month='01'">
                        <xsl:text>January </xsl:text>
                </xsl:when>
                <xsl:when test="$month='02'">
                        <xsl:text>February </xsl:text>
                </xsl:when>
                <xsl:when test="$month='03'">
                        <xsl:text>March </xsl:text>
                </xsl:when>
                <xsl:when test="$month='04'">
                        <xsl:text>April </xsl:text>
                </xsl:when>
                <xsl:when test="$month='05'">
                        <xsl:text>May </xsl:text>
                </xsl:when>
                <xsl:when test="$month='06'">
                        <xsl:text>June </xsl:text>
                </xsl:when>
                <xsl:when test="$month='07'">
                        <xsl:text>July </xsl:text>
                </xsl:when>
                <xsl:when test="$month='08'">
                        <xsl:text>August </xsl:text>
                </xsl:when>
                <xsl:when test="$month='09'">
                        <xsl:text>September </xsl:text>
                </xsl:when>
                <xsl:when test="$month='10'">
                        <xsl:text>October </xsl:text>
                </xsl:when>
                <xsl:when test="$month='11'">
                        <xsl:text>November </xsl:text>
                </xsl:when>
                <xsl:when test="$month='12'">
                        <xsl:text>December </xsl:text>
                </xsl:when>
        </xsl:choose>
        <xsl:choose>
        	<xsl:when test="substring ($date, 5, 2)=''">
        		<xsl:text></xsl:text>
        	</xsl:when>
                <xsl:when test='substring ($date, 7, 1)="0"'>
                        <xsl:value-of select="substring ($date, 8, 1)"/>
                        <xsl:text>, </xsl:text>
                </xsl:when>
                <xsl:otherwise>
                        <xsl:value-of select="substring ($date, 7, 2)"/>
                        <xsl:text>, </xsl:text>
                </xsl:otherwise>
        </xsl:choose>
        <xsl:value-of select="substring ($date, 1, 4)"/>
	</xsl:template>
	
	<!-- ** Bottomline ** -->
	<xsl:template name="bottomline">
	<p>
     	<b><xsl:text>Electronically generated by: </xsl:text></b>
	<xsl:call-template name="getName">
        <xsl:with-param name="name" select="/n1:ClinicalDocument/n1:legalAuthenticator/n1:assignedEntity/n1:representedOrganization/n1:name"/>
        </xsl:call-template>
        <xsl:text> on </xsl:text>
	<xsl:call-template name="formatDate">
   	<xsl:with-param name="date" select="//n1:ClinicalDocument/n1:legalAuthenticator/n1:time/@value"/>
        </xsl:call-template>
       	</p>
       	</xsl:template>
       	
       	<!-- ** StructuredBody ** -->
	<xsl:template match="n1:component/n1:structuredBody">
	<xsl:apply-templates select="n1:component/n1:section"/>
	</xsl:template>
	
	<!-- ** Component/Section ** -->    
	<xsl:template match="n1:component/n1:section">
	<xsl:apply-templates select="n1:title"/>		 
	<xsl:apply-templates select="n1:text/n1:table"/>	 		 		 		 		 
	<xsl:apply-templates select="n1:component/n1:section"/>
	</xsl:template>
	
	<!-- ** Title ** -->
	<xsl:template match="n1:title">
	<h3><span style="font-weight:bold;">		 
	<a name="{generate-id(.)}" href="#toc"><xsl:value-of select="."/></a>
	</span></h3>
	</xsl:template>
	
	<!-- ** Text ** -->
	<xsl:template match="n1:text">		 
	<xsl:apply-templates />		 
	</xsl:template>
	
	<!-- **  paragraph ** -->
	<xsl:template match="n1:paragraph">
	<p><xsl:apply-templates/></p>
	</xsl:template>
	
	<!-- ** Content w/ deleted text is hidden ** -->
	<xsl:template match="n1:content[@revised='delete']"/>
	
	<!-- ** content ** -->
	<xsl:template match="n1:content">
	<xsl:apply-templates/>
	</xsl:template>
	
	
	<!-- ** list ** -->
	<xsl:template match="n1:list">
	<xsl:if test="n1:caption">
	<span style="font-weight:bold; ">
	<xsl:apply-templates select="n1:caption"/>
	</span>
	</xsl:if>
	<ul>
	<xsl:for-each select="n1:item">
	<li>
	<xsl:apply-templates />
	</li>
	</xsl:for-each>
	</ul>		 
	</xsl:template>
	
	<!-- ** listType ** -->
	<xsl:template match="n1:list[@listType='ordered']">
	<xsl:if test="n1:caption">
	<span style="font-weight:bold; ">
	<xsl:apply-templates select="n1:caption"/>
	</span>
	</xsl:if>
	<ol>
	<xsl:for-each select="n1:item">
	<li>
	<xsl:apply-templates />
	</li>
	</xsl:for-each>
	</ol>		 
	</xsl:template>
			 
	
	<!-- ** caption ** -->
	<xsl:template match="n1:caption">  
	<xsl:apply-templates/>
	<xsl:text>: </xsl:text>
	</xsl:template>
			 
	<!-- ** Tables ** -->
	<xsl:template match="n1:table/@*|n1:thead/@*|n1:tfoot/@*|n1:tbody/@*|n1:colgroup/@*|n1:col/@*|n1:tr/@*|n1:th/@*|n1:td/@*">
	<xsl:copy>
	<xsl:copy-of select="@*"/>
	<xsl:apply-templates/>
	</xsl:copy>
	</xsl:template>
			 
	<!-- ** n1:table ** -->
	<xsl:template match="n1:table">
	<table>		 
	<xsl:copy-of select="@*"/>
	<xsl:apply-templates/>
	</table>		 
	</xsl:template>
	
	<!-- ** n1:thead ** -->
	<xsl:template match="n1:thead">
	<thead>
	<br/>
	<xsl:copy-of select="@*"/>
	<xsl:apply-templates/>
	</thead>		 
	</xsl:template>
	
	<!-- ** n1:tfoot ** -->
	<xsl:template match="n1:tfoot">
	<tfoot>		 
	<xsl:copy-of select="@*"/>
	<xsl:apply-templates/>
	</tfoot>		 
	</xsl:template>
	
	<!-- ** n1:tbody ** -->
	<xsl:template match="n1:tbody">
	<tbody>		 
	<xsl:copy-of select="@*"/>
	<xsl:apply-templates/>
	</tbody>		 
	</xsl:template>
	
	<!-- ** n1:colgroup ** -->
	<xsl:template match="n1:colgroup">
	<colgroup>		 
	<xsl:copy-of select="@*"/>
	<xsl:apply-templates/>
	</colgroup>		 
	</xsl:template>
	
	<!-- ** n1:col ** -->
	<xsl:template match="n1:col">
	<col>		 
	<xsl:copy-of select="@*"/>
	<xsl:apply-templates/>
	</col>		 
	</xsl:template>
			 
	<!-- ** n1:tr ** -->
	<xsl:template match="n1:tr">
	<tr>		 		 
	<xsl:copy-of select="@*"/>
	<xsl:apply-templates/>
	</tr>		 
	</xsl:template>
			 
	<!-- ** n1:th ** -->
	<xsl:template match="n1:th">
	<th>		 
	<xsl:copy-of select="@*"/>
	<xsl:apply-templates/>
	</th>		 
	</xsl:template>
			 
	<!-- ** n1:td ** -->
	<xsl:template match="n1:td">
	<td>		 
	<xsl:copy-of select="@*"/>
	<xsl:apply-templates/>
	</td>		 
	</xsl:template>
			 
	<!-- ** Caption ** -->
	<xsl:template match="n1:table/n1:caption">
	<span style="font-weight:bold; ">		 
	<xsl:apply-templates/>
	</span>		 
	</xsl:template>

	<!-- *********************************************************************  -->
	<!-- ***************************** RenderMultiMedia **********************  -->
	<!-- *********************************************************************  -->

	<xsl:template match="n1:renderMultiMedia">
	<xsl:variable name="imageRef" select="@referencedObject"/>
	<xsl:choose>
	<xsl:when test="//n1:regionOfInterest[@ID=$imageRef]">
	<!-- Here is where the Region of Interest image referencing goes -->
	<xsl:if test='//n1:regionOfInterest[@ID=$imageRef]//n1:observationMedia/n1:value[@mediaType="image/gif" or @mediaType="image/jpeg"]'>
	<br clear='all'/>
	<xsl:element name='img'>
	<xsl:attribute name='src'>
	<xsl:value-of select='//n1:regionOfInterest[@ID=$imageRef]//n1:observationMedia/n1:value/n1:reference/@value'/>
	</xsl:attribute>
	</xsl:element>
	</xsl:if>
	</xsl:when>
	<xsl:otherwise>
	<!-- Here is where the direct MultiMedia image referencing goes -->
	<xsl:if test='//n1:observationMedia[@ID=$imageRef]/n1:value[@mediaType="image/gif" or @mediaType="image/jpeg"]'>
	<br clear='all'/>
	<xsl:element name='img'>
	<xsl:attribute name='src'>
	<xsl:value-of select='//n1:observationMedia[@ID=$imageRef]/n1:value/n1:reference/@value'/>
	</xsl:attribute>
	</xsl:element>
	</xsl:if>              
	</xsl:otherwise>
	</xsl:choose>		 
	</xsl:template>
	     
	<!-- *********************************************************************  -->
	<!-- ************************* Stylecode processing **********************  -->
	<!-- ********** Supports Bold, Underline and Italics display *************  -->
	<!-- *********************************************************************  -->
	
	<xsl:template match="//n1:*[@styleCode]">
	
	<!-- ** Bold ** -->
	<xsl:if test="@styleCode='Bold'">
	<xsl:element name='b'>		 		 		 		 
	<xsl:apply-templates/>
	</xsl:element>		 
	</xsl:if> 
	
	<!-- ** Italics ** -->
	<xsl:if test="@styleCode='Italics'">
	<xsl:element name='i'>		 		 		 		 
	<xsl:apply-templates/>
	</xsl:element>		 
	</xsl:if>
	<!-- ** Underline ** -->
	<xsl:if test="@styleCode='Underline'">
	<xsl:element name='u'>		 		 		 		 
	<xsl:apply-templates/>
	</xsl:element>		 
	</xsl:if>
	
	<xsl:if test="contains(@styleCode,'Bold') and contains(@styleCode,'Italics') and not (contains(@styleCode, 'Underline'))">
	<xsl:element name='b'>
	<xsl:element name='i'>		 		 		 		 
	<xsl:apply-templates/>
	</xsl:element>
	</xsl:element>		 
	</xsl:if>
	
	<xsl:if test="contains(@styleCode,'Bold') and contains(@styleCode,'Underline') and not (contains(@styleCode, 'Italics'))">
	<xsl:element name='b'>
	<xsl:element name='u'>		 		 		 		 
	<xsl:apply-templates/>
	</xsl:element>
	</xsl:element>		 
	</xsl:if>
	
	<xsl:if test="contains(@styleCode,'Italics') and contains(@styleCode,'Underline') and not (contains(@styleCode, 'Bold'))">
	<xsl:element name='i'>
	<xsl:element name='u'>		 		 		 		 
	<xsl:apply-templates/>
	</xsl:element>
	</xsl:element>		 
	</xsl:if>
	
	<xsl:if test="contains(@styleCode,'Italics') and contains(@styleCode,'Underline') and contains(@styleCode, 'Bold')">
	<xsl:element name='b'>
	<xsl:element name='i'>
	<xsl:element name='u'>		 		 		 		 
	<xsl:apply-templates/>
	</xsl:element>
	</xsl:element>
	</xsl:element>		 
	</xsl:if>
	</xsl:template>
	
	<!-- ** Superscript or Subscript ** -->
	<xsl:template match="n1:sup">
	<xsl:element name='sup'>		 		 		 		 
	<xsl:apply-templates/>
	</xsl:element>		 
	</xsl:template>
	<xsl:template match="n1:sub">
	<xsl:element name='sub'>		 		 		 		 
	<xsl:apply-templates/>
	</xsl:element>		 
	</xsl:template>
	
	<!-- *********************************************************************  -->
	<!-- ************************* Family Lookup Table  **********************  -->
	<!-- *********************************************************************  -->
	<xsl:template name="famLookup">
	<xsl:param name="relationship" />
	<xsl:choose>
		<xsl:when test="$relationship='FAMMEMB'">Family Member</xsl:when>
		<xsl:when test="$relationship='CHILD'">Child</xsl:when>
		<xsl:when test="$relationship='CHLDADOPT'">Adopted Child</xsl:when>
		<xsl:when test="$relationship='DAUADOPT'">Adopted Daughter</xsl:when>
		<xsl:when test="$relationship='SONADOPT'">Adopted Son</xsl:when>
		<xsl:when test="$relationship='CHILDINLAW'">Child in-law</xsl:when>
		<xsl:when test="$relationship='DAUINLAW'">Daughter in-law</xsl:when>
		<xsl:when test="$relationship='SONINLAW'">Son in-law</xsl:when>
		<xsl:when test="$relationship='CHLDFOST'">Foster Child</xsl:when>
		<xsl:when test="$relationship='DAUFOST'">Foster Daughter</xsl:when>
		<xsl:when test="$relationship='SONFAUST'">Foster Son</xsl:when>
		<xsl:when test="$relationship='NCHILD'">Natural Child</xsl:when>
		<xsl:when test="$relationship='DAU'">Daughter</xsl:when>
		<xsl:when test="$relationship='SON'">Son</xsl:when>
		<xsl:when test="$relationship='STPCHLD'">Step Child</xsl:when>
		<xsl:when test="$relationship='STPDAU'">Step Daughter</xsl:when>
		<xsl:when test="$relationship='STPSON'">Step Son</xsl:when>
		<xsl:when test="$relationship='SPS'">Spouse</xsl:when>
		<xsl:when test="$relationship='HUSB'">Husband</xsl:when>
		<xsl:when test="$relationship='WIFE'">Wife</xsl:when>
		<xsl:when test="$relationship='GRNDCHILD'">Grandchild</xsl:when>
		<xsl:when test="$relationship='GRNDDAU'">Granddaughter</xsl:when>
		<xsl:when test="$relationship='GRNDSON'">Grandson</xsl:when>
		<xsl:when test="$relationship='GRPRN'">Grandparent</xsl:when>
		<xsl:when test="$relationship='GRFTH'">Grandfather</xsl:when>
		<xsl:when test="$relationship='GRMTH'">Grandmother</xsl:when>
		<xsl:when test="$relationship='GGRPRN'">Great Grandparent</xsl:when>
		<xsl:when test="$relationship='GGRFTH'">Great Grandfather</xsl:when>
		<xsl:when test="$relationship='GGRMTH'">Great Grandmother</xsl:when>
		<xsl:when test="$relationship='NIENEPH'">Niece/Nephew</xsl:when>
		<xsl:when test="$relationship='NEPHEW'">Nephew</xsl:when>
		<xsl:when test="$relationship='NIECE'">Niece</xsl:when>
		<xsl:when test="$relationship='PRN'">Parent</xsl:when>
		<xsl:when test="$relationship='FTH'">Father</xsl:when>	
		<xsl:when test="$relationship='MTH'">Mother</xsl:when>
		<xsl:when test="$relationship='NPRN'">Natural Parent</xsl:when>
		<xsl:when test="$relationship='NFTH'">Natural Father</xsl:when>
		<xsl:when test="$relationship='NFTHF'">Natural Father of the Fetus</xsl:when>
		<xsl:when test="$relationship='NMTH'">Natural Mother</xsl:when>
		<xsl:when test="$relationship='PRNINLAW'">Parent in-law</xsl:when>
		<xsl:when test="$relationship='FTHINLAW'">Father in-law</xsl:when>
		<xsl:when test="$relationship='MTHINLAW'">Mother in-law</xsl:when>
		<xsl:when test="$relationship='STPPRN'">Step Parent</xsl:when>	
		<xsl:when test="$relationship='STPFTH'">Stepfather</xsl:when>	
		<xsl:when test="$relationship='STPMTH'">Stepmother</xsl:when>
		<xsl:when test="$relationship='ROOM'">Roomate</xsl:when>	
		<xsl:when test="$relationship='SIB'">Sibling</xsl:when>
		<xsl:when test="$relationship='BRO'">Brother</xsl:when>
		<xsl:when test="$relationship='HSIB'">Half-sibling</xsl:when>	
		<xsl:when test="$relationship='HBRO'">Half-brother</xsl:when>
		<xsl:when test="$relationship='HSIS'">Half-sister</xsl:when>
		<xsl:when test="$relationship='NSIB'">Natural Sibling</xsl:when>
		<xsl:when test="$relationship='NRBO'">Natural Brother</xsl:when>
		<xsl:when test="$relationship='NSIS'">Natural Sister</xsl:when>
		<xsl:when test="$relationship='SIBINLAW'">Sibling in-law</xsl:when>
		<xsl:when test="$relationship='BROINLAW'">Brother in-law</xsl:when>
		<xsl:when test="$relationship='SISINLAW'">Sister in-law</xsl:when>
		<xsl:when test="$relationship='NSIS'">Natural Sister</xsl:when>
		<xsl:when test="$relationship='SIS'">Sister</xsl:when>
		<xsl:when test="$relationship='STPSIB'">Step Sibling</xsl:when>
		<xsl:when test="$relationship='STPBRO'">Stepbrother</xsl:when>
		<xsl:when test="$relationship='STPSIS'">Stepsister</xsl:when>
		<xsl:when test="$relationship='SIGOTHR'">Significant Other</xsl:when>
		<xsl:when test="$relationship='AUNT'">Aunt</xsl:when>
		<xsl:when test="$relationship='COUSIN'">Cousin</xsl:when>
		<xsl:when test="$relationship='DOMPART'">Domestic Partner</xsl:when>
		<xsl:when test="$relationship='UNCLE'">Uncle</xsl:when>
		<xsl:when test="$relationship='NBOR'">Neighbor</xsl:when>
		<xsl:when test="$relationship='FRND'">Unrelated Friend</xsl:when>
	</xsl:choose>
	</xsl:template>
	
	
	<!-- *********************************************************************  -->
	<!-- ********************** Contact Type Lookup Table  *******************  -->
	<!-- *********************************************************************  -->	
	<xsl:template name="contactTypeLookup">
		<xsl:param name="contactType" />
		<xsl:choose>
		<xsl:when test="$contactType='AGNT'">Agent</xsl:when>
		<xsl:when test="$contactType='CAREGIVER'">Caregiver</xsl:when>
		<xsl:when test="$contactType='ECON'">Emergency Contact</xsl:when>
		<xsl:when test="$contactType='GUARD'">Guardian</xsl:when>
		<xsl:when test="$contactType='NOK'">Next of Kin</xsl:when>
		<xsl:when test="$contactType='PRS'">Personal</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<!-- *****************************************************  -->
	<!-- ********** This matches the string in the first *****  --> 
	<!-- ********** argument with a regular expression and ***  -->
	<!-- ********** returns email address or URL (Java) ******  -->
	<!-- *****************************************************  -->	
	
	<xsl:template  name="matchPattern"> 
	        <xsl:param  name="input"/> 
	        <xsl:param  name="expression"/>
	         
		<!--  commented by WCJ 05/13/2010 
		      this functionality could be implemented in .NET, but do not have 
			  the time at this point.
		<xsl:if test="java:matchPattern($terminologyService,$input, $expression)">
		   <br />
		   <xsl:value-of select="$input" />
	  	</xsl:if>--> 
      	</xsl:template> 
	
 </xsl:stylesheet>
