<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:n3="http://www.w3.org/1999/xhtml" xmlns:n1="urn:hl7-org:v3" xmlns:n2="urn:hl7-org:v3/meta/voc" xmlns:voc="urn:hl7-org:v3/voc" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<xsl:output method="html" indent="yes" version="4.01" encoding="ISO-8859-1" doctype-public="-//W3C//DTD HTML 4.01//EN"/>

<!-- CDA document -->
<xsl:variable name="title">
    <xsl:choose>
         <xsl:when test="/n1:ClinicalDocument/n1:title">
             <xsl:value-of select="/n1:ClinicalDocument/n1:title"/>
         </xsl:when>
             <xsl:otherwise>Clinical Document</xsl:otherwise>
    </xsl:choose>
</xsl:variable>

<xsl:template match="/">
<xsl:apply-templates select="n1:ClinicalDocument"/>
</xsl:template>

<xsl:template match="n1:ClinicalDocument">
<xsl:variable name="patientRole" select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole"/>
         <xsl:variable name="sex" select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole/n1:patient/n1:administrativeGenderCode/@code"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>
	 <xsl:text>CSMC CCD: </xsl:text>

	 <xsl:call-template name="getName">
	 <xsl:with-param name="name" select="$patientRole/n1:patient/n1:name"/>   		 		        	
	 </xsl:call-template>      -->          
</title>

<!-- BHT (disabled)
<style type="text/css">
/*margin and padding on body element
  can introduce errors in determining
  element position and are not recommended;
  we turn them off as a foundation for YUI
  CSS treatments. */
body {
	margin:0;
	padding:0;
}
#toggle {
    text-align: center;
    padding: 1em;
}
#toggle a {
    padding: 0 5px;
    border-left: 1px solid black;
}
#tRight {
    border-left: none !important;
}
</style> -->

<!-- BHT (disabled)
<script type="text/javascript">
$(document).ready(function(){
		var currentTime = new Date();
		var month = currentTime.getMonth() + 1;
		var day = currentTime.getDate();
		var year = currentTime.getFullYear();
		var currentDate = year + "" +  month + "" + day; 

		function callDate(){
		return currentDate; 
    }
}); 
</script> -->



<link rel="stylesheet" type="text/css" href="./resources/xsl/raa/css/reset-fonts-grids.css" />
<link rel="stylesheet" type="text/css" href="./resources/xsl/raa/css/resize.css" />
<link rel="stylesheet" type="text/css" href="./resources/xsl/raa/css/layout.css" />
<link rel="stylesheet" type="text/css" href="./resources/xsl/raa/css/button.css" />

<script type="text/javascript" src="./resources/xsl/raa/js/yahoo-min.js"></script>
<script type="text/javascript" src="./resources/xsl/raa/js/event-min.js"></script>
<script type="text/javascript" src="./resources/xsl/raa/js/dom-min.js"></script>
<script type="text/javascript" src="./resources/xsl/raa/js/element-min.js"></script>
<script type="text/javascript" src="./resources/xsl/raa/js/dragdrop-min.js"></script>
<script type="text/javascript" src="./resources/xsl/raa/js/resize-min.js"></script>
<script type="text/javascript" src="./resources/xsl/raa/js/animation-min.js"></script>
<script type="text/javascript" src="./resources/xsl/raa/js/layout-min.js"></script>
<script type="text/javascript" src="./resources/xsl/raa/js/bleh.js"></script>
<script type="text/javascript" src="./resources/xsl/raa/js/search.js"></script>


</head>

<body class=" yui-skin-sam">

<div id="top1">


		<xsl:text>&#160;&#160;&#160;&#160;&#160;Patient:  </xsl:text>
		<b>
		<xsl:call-template name="getName">
		<xsl:with-param name="name" select="$patientRole/n1:patient/n1:name"/>
		</xsl:call-template>
		</b>
		
		<xsl:text>&#160;&#160;&#160;&#160;|&#160;&#160;&#160;Birthdate: </xsl:text>
		<b>
		<xsl:call-template name="formatDate">
		<xsl:with-param name="date" select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole/n1:patient/n1:birthTime/@value"/>
		</xsl:call-template>
		<xsl:variable name = "birthYear" select = "substring(/n1:ClinicalDocument/n1:recordTarget/n1:patientRole/n1:patient/n1:birthTime/@value, 1, 4)"></xsl:variable>
		<xsl:variable name = "birthMonth" select = "substring(/n1:ClinicalDocument/n1:recordTarget/n1:patientRole/n1:patient/n1:birthTime/@value, 5, 2)"></xsl:variable>
		<xsl:variable name = "birthDay" select = "substring(/n1:ClinicalDocument/n1:recordTarget/n1:patientRole/n1:patient/n1:birthTime/@value, 7, 2)"></xsl:variable>
		
		<!-- (Age 
		<xsl:choose>
			<xsl:when test = "5 > $birthMonth"><xsl:value-of select = "2010 - $birthYear"/></xsl:when>
			<xsl:otherwise><xsl:value-of select = "2010 - ($birthYear + 1)"/></xsl:otherwise>
		</xsl:choose>) 
		-->
		
		</b>	
		
		<xsl:text>&#160;&#160;&#160;&#160;|&#160;&#160;&#160;Gender: </xsl:text>
		<b>

		<xsl:choose>
		<xsl:when test="$sex='M'">Male</xsl:when>
		<xsl:when test="$sex='F'">Female</xsl:when>
		</xsl:choose>
		</b>
		<xsl:text>&#160;&#160;&#160;&#160;|&#160;&#160;&#160;MRN: </xsl:text>
		
		<b>
		<!--  Replaced to support HRNs
		   <xsl:value-of select="$patientRole/n1:id/@extension"/>
		-->
	        <xsl:choose>
	           <!-- if atleast one "id" element is present with attribute "root" = "HRN"-->
	           <xsl:when test="count($patientRole/n1:id[@root='HRN'])!=0">
		      <xsl:for-each select="$patientRole/n1:id[@root='HRN']">
		         <xsl:if test="position() &gt; 1">
			    <xsl:text>, </xsl:text>
			 </xsl:if>
			 <xsl:value-of select="./@extension"/>
		      </xsl:for-each> 		               		                
		   </xsl:when>
	           <!-- if atleast one "id" element is present with attribute "root" != "HRN"-->
	           <xsl:otherwise>
	              <xsl:if test="count($patientRole/n1:id[@root='HRN'])=0 and count($patientRole/n1:id) !=0">
		         <xsl:value-of select="$patientRole/n1:id[1]/@extension"/>
		      </xsl:if>
	           </xsl:otherwise>
                </xsl:choose>
		</b>         
		

</div>


<div id="left1">

	<div id="sidenav">
    <div id = "tableContentsLabel">Contents</div>

<!-- Profile -->

    <div id="geninfo">

           <b><xsl:call-template name="getName">		     
           <xsl:with-param name="name" select="$patientRole/n1:patient/n1:name"/></xsl:call-template></b>		     <br/>
           <xsl:choose>
           <xsl:when test="$patientRole/n1:telecom != ''">
           	<br/>
           		<xsl:call-template name="getTelecom">
           			<xsl:with-param name="telecom" select="$patientRole/n1:telecom"/>
		   		</xsl:call-template>

           </xsl:when>
			</xsl:choose>
                   <xsl:if test="$patientRole/n1:addr">
                   		     <br/>
                   		   Address/City:
                   <b><xsl:call-template name="getAddress">
                           <xsl:with-param name="addr" select="$patientRole/n1:addr"/>
                   </xsl:call-template></b>
           </xsl:if>
           <br/>

           <xsl:text> Gender: </xsl:text>
           <b>
  
			<xsl:choose>
			<xsl:when test="$sex='M'">Male</xsl:when>
			<xsl:when test="$sex='F'">Female</xsl:when>
			</xsl:choose>
			</b>

			<br/>

           <xsl:text> Birthdate: </xsl:text>
           <b>

	       <xsl:call-template name="formatDate">
           <xsl:with-param name="date" select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole/n1:patient/n1:birthTime/@value"/>
           </xsl:call-template>
           </b>

           <br/>

           <xsl:text> MRN: </xsl:text>
           <b><xsl:value-of select="$patientRole/n1:id/@extension"/></b>

           <br/>
        	
		<xsl:variable name="guardian" select="/n1:ClinicalDocument/n1:participant[@typeCode='IND']/n1:associatedEntity[@classCode='GUAR']"/>
		<xsl:choose>
       		<xsl:when test="$guardian/n1:associatedPerson/n1:name != ''">
    			Guardian: 
		 		<b>
		 		<xsl:call-template name="getParticipant">
	     			<xsl:with-param name="participant" select="/n1:ClinicalDocument/n1:participant[@typeCode='IND']/n1:associatedEntity[@classCode='GUAR']"/>
	     		</xsl:call-template>
	     		</b>
			</xsl:when>
        	<xsl:otherwise></xsl:otherwise>
        </xsl:choose>

        	
		<xsl:variable name="nok" select="/n1:ClinicalDocument/n1:participant[@typeCode='IND']/n1:associatedEntity[@classCode='NOK']"/>
		<xsl:choose>
       		<xsl:when test="$nok/n1:associatedPerson/n1:name != ''">
	       		Next of Kin: 
	       		<b>
	       		<xsl:call-template name="getParticipant">
	       			<xsl:with-param name="participant" select="/n1:ClinicalDocument/n1:participant[@typeCode='IND']/n1:associatedEntity[@classCode='NOK']"/>
	       		</xsl:call-template>
	       		</b>
	       		</xsl:when>
	       		<xsl:otherwise></xsl:otherwise>
	    </xsl:choose>
	    
	    
        </div>
            
    <div id = "tableContentsPanel">
    	<div id="leftScroll">			 		 			
		<ul>
			<xsl:apply-templates select="n1:component/n1:structuredBody/n1:component/n1:section/n1:title" mode="rowcount"/>
		</ul>
	</div>	
						
			<div id = "provenance">
					<ul>
			  		 	<li style="list-style-type:none;">
					    	 <a href = "#{generate-id(.)}"> 
					    	 	Provenance (3<!--<xsl:value-of select="count(../n1:entry)" />-->)</a>
			 		 	</li>
					</ul>
			</div>	
		</div>
					
    </div>
    
</div>

<div id="center1">
    <div id="content">
		<br/>
		<xsl:apply-templates select="n1:component/n1:structuredBody"/> 			
 		<xsl:call-template name="Provenance"/> 		
 	</div>			
</div>


<script type="text/javascript" src="./resources/xsl/raa/js/yui.js"></script>

</body>
</html>
<!-- p6.ydn.sp1.yahoo.com compressed/chunked Sun May 16 18:27:04 PDT 2010 -->
</xsl:template>


<!--> other functions ...............................................................-->

<xsl:template name="getParticipant">
	<xsl:param name="participant"/>
		<xsl:choose>
			<xsl:when test="$participant != ''">
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
			</xsl:when>
			<xsl:otherwise>None listed</xsl:otherwise>
		</xsl:choose><br/>		 
</xsl:template>

<xsl:template name="getAddress">
	<xsl:param name="addr"/>
		<xsl:choose>
			<xsl:when test="$addr/n1:streetAddressLine != ''"><br/>
				&#160;&#160;&#160;&#160;&#160;<xsl:value-of select="$addr/n1:streetAddressLine"/><br/>
				&#160;&#160;&#160;&#160;&#160;<xsl:value-of select="$addr/n1:city"/>, <xsl:value-of select="$addr/n1:state"/>, <xsl:value-of select="$addr/n1:postalCode"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$addr/n1:city"/>, <xsl:value-of select="$addr/n1:state"/>
			</xsl:otherwise>
		</xsl:choose>
</xsl:template>

<xsl:template name="getTelecom">
		 <xsl:param name="telecom"/>
		 <br/>&#160;&#160;&#160;&#160;&#160;<xsl:value-of select="$telecom/@value"/>
</xsl:template>
    
<!-- Get a Name  -->
<xsl:template name="getName">
    <xsl:param name="name"/>
    <xsl:choose>
         <xsl:when test="$name/n1:family">
              <xsl:value-of select="$name/n1:family"/>
              <xsl:text>, </xsl:text>
              <xsl:value-of select="$name/n1:given"/>

              <xsl:if test="$name/n1:suffix">
                  <xsl:text>, </xsl:text>
                  <xsl:value-of select="$name/n1:suffix"/>
              </xsl:if>
          </xsl:when>
          <xsl:otherwise>
               <xsl:value-of select="$name"/>
          </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<!--  Format Date 
    
      outputs a date in Month Day, Year form
      e.g., 19991207  ==> December 07, 1999
-->
<xsl:template name="formatDate">
        <xsl:param name="date"/>
        <xsl:variable name="month" select="substring ($date, 5, 2)"/>
        <xsl:choose>
                <xsl:when test="$month='01'">
                        <xsl:text>01/</xsl:text>
                </xsl:when>
                <xsl:when test="$month='02'">
                        <xsl:text>02/</xsl:text>
                </xsl:when>
                <xsl:when test="$month='03'">
                        <xsl:text>03/</xsl:text>
                </xsl:when>
                <xsl:when test="$month='04'">
                        <xsl:text>04/</xsl:text>
                </xsl:when>
                <xsl:when test="$month='05'">
                        <xsl:text>05/</xsl:text>
                </xsl:when>
                <xsl:when test="$month='06'">
                        <xsl:text>06/</xsl:text>
                </xsl:when>
                <xsl:when test="$month='07'">
                        <xsl:text>07/</xsl:text>
                </xsl:when>
                <xsl:when test="$month='08'">
                        <xsl:text>08/</xsl:text>
                </xsl:when>
                <xsl:when test="$month='09'">
                        <xsl:text>09/</xsl:text>
                </xsl:when>
                <xsl:when test="$month='10'">
                        <xsl:text>10/</xsl:text>
                </xsl:when>
                <xsl:when test="$month='11'">
                        <xsl:text>11/</xsl:text>
                </xsl:when>
                <xsl:when test="$month='12'">
                        <xsl:text>12/</xsl:text>
                </xsl:when>
        </xsl:choose>
        <xsl:choose>
                <xsl:when test='substring ($date, 7, 1)="0"'>
                        <xsl:value-of select="substring ($date, 7, 2)"/>
                        <xsl:text>/</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                        <xsl:value-of select="substring ($date, 7, 2)"/>
                        <xsl:text>/</xsl:text>
                </xsl:otherwise>
        </xsl:choose>
        <xsl:value-of select="substring ($date, 1, 4)"/>
</xsl:template>

<!-- StructuredBody -->
<xsl:template match="n1:component/n1:structuredBody">
	<xsl:apply-templates select="n1:component/n1:section"/>
</xsl:template>

<!-- Component/Section --> 
   
<xsl:template match="n1:component/n1:section">
	<xsl:apply-templates select="n1:title"/>
	<xsl:apply-templates select="n1:text"/>
    <xsl:apply-templates select="n1:component/n1:section"/>
</xsl:template>

<!--   Title  -->
<xsl:template match="n1:title">
	<div id = "dataTitleStyle">		 
		<a name="{generate-id(.)}"><xsl:value-of select="."/></a>
	</div>
</xsl:template>

<!-- Calculates counts for the left tab totals - modified to count the table rows so counts are correct for all sections -->
<xsl:template match="n1:title" mode = "rowcount">
<xsl:variable name ="count" select ="count(../n1:text/n1:table/n1:tbody/n1:tr)"/>	 
	<div id = "rowcount">
		<a href="#{generate-id(.)}"><xsl:value-of select="."/> (<xsl:value-of select="$count"/>)</a>
	</div>
</xsl:template>

<!--   Text   -->
<xsl:template match="n1:text">	
	<xsl:choose>
		<xsl:when test = "n1:table != ''">
			<xsl:apply-templates />
		</xsl:when>
		<xsl:otherwise>
		<br/>
			<xsl:apply-templates />
		<br/>
		<br/>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<!--   paragraph  -->
<xsl:template match="n1:paragraph">
	<xsl:apply-templates/>
</xsl:template>

<!--     Content w/ deleted text is hidden -->
<xsl:template match="n1:content[@revised='delete']"/>

<!--   content  -->
<xsl:template match="n1:content">
	<xsl:apply-templates/>
</xsl:template>


<!--   list  -->
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
		 

<!--   caption  -->
<xsl:template match="n1:caption">  
		 <xsl:apply-templates/>
		 <xsl:text>: </xsl:text>
</xsl:template>
		 
		 <!--      Tables   -->
		 <xsl:template match="n1:table/@*|n1:thead/@*|n1:tfoot/@*|n1:tbody/@*|n1:colgroup/@*|n1:col/@*|n1:tr/@*|n1:th/@*|n1:td/@*">
		 		 <xsl:copy>
		 		 		 <xsl:copy-of select="@*"/>
		 		 		 <xsl:apply-templates/>
		 		 </xsl:copy>
		 </xsl:template>

		<!--       IS THIS THE TABLE -->
		 <xsl:template match="n1:table">		 
		 		 <table class="stripeMe">
		 		 		 <xsl:copy-of select="@*"/>
		 		 		 <xsl:apply-templates/>
		 		 </table>		 
		 </xsl:template>
		 
		 <xsl:template match="n1:thead">
		 		 <thead>		 
		 		 		 <xsl:copy-of select="@*"/>
		 		 		 <xsl:apply-templates/>
		 		 </thead>		 
		 </xsl:template>

		 <xsl:template match="n1:tfoot">
		 		 <tfoot>		 
		 		 
		 		 		 <xsl:copy-of select="@*"/>
		 		 		 <xsl:apply-templates/>
		 		 </tfoot>		 
		 </xsl:template>

		 <xsl:template match="n1:tbody">
		 		 <tbody>		 
		 		 
		 		 		 <xsl:copy-of select="@*"/>
		 		 		 <xsl:apply-templates/>
		 		 </tbody>		 
		 </xsl:template>

		 <xsl:template match="n1:colgroup">
		 		 <colgroup>		 
		 		 
		 		 		 <xsl:copy-of select="@*"/>
		 		 		 <xsl:apply-templates/>
		 		 </colgroup>		 
		 </xsl:template>

		 <xsl:template match="n1:col">
		 		 <col>		 
		 		 
		 		 		 <xsl:copy-of select="@*"/>
		 		 		 <xsl:apply-templates/>
		 		 </col>		 
		 </xsl:template>

		 <xsl:template match="n1:tr">
		 		 <tr>
		 		 		 <xsl:copy-of select="@*"/>
		 		 		 <xsl:apply-templates/>
		 		 </tr>		 
		 </xsl:template>

		 <xsl:template match="n1:th">
		 		 <th>		 
		 		 		 <xsl:copy-of select="@*"/>
		 		 		 <xsl:apply-templates/>
		 		 </th>		 
		 </xsl:template>

		 <xsl:template match="n1:td">
		 		 <td>		 
		 		 		 <xsl:copy-of select="@*"/>
		 		 		 <xsl:apply-templates/>
		 		 </td>		 
		 </xsl:template>

		 <xsl:template match="n1:table/n1:caption">
		 		 <span style="font-weight:bold; ">		 
		 		 		 <xsl:apply-templates/>
		 		 </span>		 
		 </xsl:template>

<!--   RenderMultiMedia 

         this currently only handles GIF's and JPEG's.  It could, however,
	 be extended by including other image MIME types in the predicate
	 and/or by generating <object> or <applet> tag with the correct
	 params depending on the media type  @ID  =$imageRef     referencedObject
 -->
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

<!-- 	Stylecode processing   
	  Supports Bold, Underline and Italics display

-->
		 <xsl:template match="//n1:*[@styleCode]">

		 <xsl:if test="@styleCode='Bold'">
		      <xsl:element name='b'>		 		 		 		 
		           <xsl:apply-templates/>
		      </xsl:element>		 
		 </xsl:if> 

		 <xsl:if test="@styleCode='Italics'">
		      <xsl:element name='i'>		 		 		 		 
		           <xsl:apply-templates/>
		      </xsl:element>		 
		 </xsl:if>

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

<!-- 	Superscript or Subscript   -->
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

<!--  Bottomline  -->	 
     <xsl:template name="bottomline">
     <p>
     <b><xsl:text>Electronically generated by: </xsl:text></b>
		 <xsl:call-template name="getName">
           <xsl:with-param name="name" 
                select="/n1:ClinicalDocument/n1:legalAuthenticator/n1:assignedEntity/n1:representedOrganization/n1:name"/>
        </xsl:call-template>
        <xsl:text> on </xsl:text>
		        <xsl:call-template name="formatDate">
   		            <xsl:with-param name="date" 
 		                select="//n1:ClinicalDocument/n1:legalAuthenticator/n1:time/@value"/>
        </xsl:call-template>
       </p>
     </xsl:template>
     
  <!-- Provenance -->
<xsl:template name="Provenance">

<div id = "dataTitleStyle"><a name="{generate-id(.)}"><xsl:value-of select="Provenance"/>Provenance</a></div>

<text>
	<table class = "stripeMe" border="1" width="100%">
		<thead>
			<tr><th>Name</th><th>Value</th></tr>
		</thead>
		<tbody>
			
			<tr><td>Created On</td><td>
			<xsl:call-template name="formatDate">
	 			<xsl:with-param name="date" select="/n1:ClinicalDocument/n1:effectiveTime/@value"/>
         	</xsl:call-template> </td></tr>
			<tr><td>Electronically Generated By</td><td>
			<xsl:call-template name="getName">
      			<xsl:with-param name="name" select="/n1:ClinicalDocument/n1:legalAuthenticator/																	n1:assignedEntity/n1:representedOrganization/n1:name"/>
    		</xsl:call-template>
    		<xsl:text> on </xsl:text>
	       	<xsl:call-template name="formatDate">
		        	<xsl:with-param name="date" select="//n1:ClinicalDocument/n1:legalAuthenticator/n1:time/@value"/>
   			</xsl:call-template></td></tr>
			<tr><td>Originating Organization</td><td>
			<xsl:call-template name="getName">
      			<xsl:with-param name="name" select="/n1:ClinicalDocument/n1:legalAuthenticator/n1:assignedEntity/n1:representedOrganization/n1:name"/>
    		</xsl:call-template></td></tr>
		</tbody>
	</table>
</text>	
</xsl:template>


</xsl:stylesheet>