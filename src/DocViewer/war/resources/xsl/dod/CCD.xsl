<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:n3="http://www.w3.org/1999/xhtml" xmlns:n1="urn:hl7-org:v3" xmlns:n2="urn:hl7-org:v3/meta/voc" xmlns:voc="urn:hl7-org:v3/voc" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:section="urn:gov.va.med">

  <xsl:output method="html" indent="yes" version="4.01" encoding="ISO-8859-1" doctype-public="-//W3C//DTD HTML 4.01//EN"/>

  <!-- CDA document -->

  <xsl:variable name="tableWidth">50%</xsl:variable>
  <xsl:variable name="snomedCode">2.16.840.1.113883.6.96</xsl:variable>
  <xsl:variable name="snomedProblemCode">55607006</xsl:variable>
  <xsl:variable name="snomedProblemCode2">404684003</xsl:variable>
  <xsl:variable name="snomedProblemCode3">418799008</xsl:variable>
  <xsl:variable name="snomedAllergyCode">416098002</xsl:variable>

  <xsl:variable name="loincCode">2.16.840.1.113883.6.1</xsl:variable>
  <xsl:variable name="loincProblemCode">11450-4</xsl:variable>
  <xsl:variable name="loincAllergyCode">48765-2</xsl:variable>
  <xsl:variable name="loincMedCode">10160-0</xsl:variable>
  <xsl:variable name="loincVitalsCode">8716-3</xsl:variable>
  <xsl:variable name="loincLabsCode">30954-2</xsl:variable>
  <xsl:variable name="loincImmunizationsCode">11369-6</xsl:variable>
  <xsl:variable name="loincEncounterCode">46240-8</xsl:variable>
  <xsl:variable name="loincProceduresCode">47519-4</xsl:variable>
  <xsl:variable name="vitalsTemplateCode">2.16.840.1.113883.10.20.1.32</xsl:variable>
  <xsl:variable name="labsTemplateCode">2.16.840.1.113883.10.20.1.32</xsl:variable>
  <xsl:variable name="immunizationsTemplateCode">2.16.840.1.113883.10.20.1.32</xsl:variable>
  <xsl:variable name="allergyTemplateCode">2.16.840.1.113883.10.20.1.18</xsl:variable>
  <xsl:variable name="problemTemplateCode">2.16.840.1.113883.10.20.1.28</xsl:variable>

  <xsl:variable name="title">
    <xsl:choose>
      <xsl:when test="string-length(/n1:ClinicalDocument/n1:title)=0">
        <xsl:text>Clinical Document</xsl:text>
      </xsl:when>
      <xsl:when test="/n1:ClinicalDocument/n1:title">
        <xsl:value-of select="/n1:ClinicalDocument/n1:title"/>
      </xsl:when>
    </xsl:choose>
  </xsl:variable>

  <xsl:template match="/">
    <xsl:apply-templates select="n1:ClinicalDocument"/>
  </xsl:template>

  <xsl:template match="n1:ClinicalDocument">
    <html>
      <head>
        <!-- <meta name='Generator' content='&CDA-Stylesheet;'/> -->
        <xsl:comment>
          Do NOT edit this HTML directly, it was generated via an XSLt
          transformation from the original release 2 CDA Document.
        </xsl:comment>
        <title>
          <xsl:value-of select="$title"/>
        </title>

        <style type="text/css">

          body {
          border-right-width: 0px;
          border-top-width: 0px;
          border-left-width: 0px;
          border-bottom-width: 0px;
          padding-top: 0px;
          padding-bottom: 0px;
          padding-left: 0px;
          padding-right: 0px;
          margin-top: 0px;
          margin-bottom: 0px;
          margin-left: 0px;
          margin-right: 0px;
          border-collapse: collapse
          }

          table.first {
          text-align: left;
          vertical-align: top;
          background-color: #CCCCff;
          border-right: 3px solid #002452;
          border-top: 3px solid #002452;
          border-left: 3px solid #002452;
          border-bottom: 3px solid #002452;
          padding-top: 0px;
          padding-bottom: 0px;
          padding-left: 0px;
          padding-right: 0px;
          margin-top: 0px;
          margin-bottom: 0px;
          margin-left: 0px;
          margin-right: 0px;
          font: 95% "Times New Roman";
          border-collapse: collapse
          }

          table.second {
          text-align: left;
          vertical-align: top;
          background-color: #CCCCff;
          border-right: 3px solid #002452;
          border-top: 0px solid #002452;
          border-left: 3px solid #002452;
          border-bottom: 3px solid #002452;
          padding-top: 0px;
          padding-bottom: 0px;
          padding-left: 0px;
          padding-right: 0px;
          margin-top: 0px;
          margin-bottom: 0px;
          margin-left: 0px;
          margin-right: 0px;
          font: 95% "Times New Roman";
          border-collapse: collapse
          }

          th.first {
          text-align: left;
          vertical-align: top;
          color: white;
          background-color: #002452;
          font: bold 95% "Times New Roman";
          padding-left: 3px;
          padding-right: 3px;
          border-collapse: collapse
          }


          tr.first {
          text-align: left;
          vertical-align: top;
          color: black;
          <!--background-color: #E2E0E0;-->
          background-color: #E8F0F0;
          padding-top: 3px;
          padding-bottom: 3px;
          padding-left: 9px;
          padding-right: 3px;
          border-collapse: collapse
          }

          td.first  {
          padding-left: 3px;
          padding-right: 3px;
          padding-top: 2px;
          padding-bottom: 3px;
          color: white;
          background-color: #002452;
          }

          tr.second {
          text-align: left;
          vertical-align: top;
          color: black;
          <!--background-color: #F9F4EF;  F0F5F5-->
          background-color: #CCCCff;
          padding-top: 3px;
          padding-bottom: 3px;
          padding-left: 9px;
          padding-right: 3px;
          border-collapse: collapse
          }


          #smenu {
          z-index: 1;
          position: absolute;
          top: 45px;
          left: 685px;
          width: 100%;
          float: left;
          text-align: right;
          color: #000;
          }
        </style>

        <style type="text/css">
          #menu {
          position: absolute;
          top: 45px;
          left: 0px;
          z-index: 1;
          float: left;
          text-align: right;
          color: #000;
          list-style: none;
          line-height: 1;
          }
        </style>

        <style type="text/css">

          #menu ul {
          list-style: none;
          margin: 0;
          padding: 0;
          width: 12em;
          float: right;
          text-align: right;
          color: #000;
          }

          #menu a, #menu h2 {
          font: bold 11px/16px arial, helvetica, sans-serif;
          text-align: right;
          display: block;
          border-width: 0px;
          border-style: solid;
          border-color: #ccc #888 #555 #bbb;
          margin: 0;
          padding: 2px 3px;
          color: #000;
          }

          #menu h2 {
          color: #fff;
          text-transform: uppercase;
          text-align: right;
          }

          #menu a {
          text-decoration: none;
          text-align: right;
          border-width: 1px;
          border-style: solid;
          border-color: #fff #777 #777 #777;
          }

          #menu a:hover {
          color: #000;
          background: #fff;
          text-align: right;
          }

          #menu li {
          position: relative;
          }

          #menu ul ul {
          position: relative;
          z-index: 500;
          text-align: left;
          color: #000;
          background-color: #E0E5E5;
          float: right;
          }

          #menu ul ul ul {
          position: absolute;
          top: 0;
          left: 100%;
          text-align: right;
          float: right;
          }

          div#menu ul ul,
          div#menu ul li:hover ul ul,
          div#menu ul ul li:hover ul ul
          {display: none;}

          div#menu ul li:hover ul,
          div#menu ul ul li:hover ul,
          div#menu ul ul ul li:hover ul
          {display: block;}

        </style>

      </head>
      <xsl:comment>

      </xsl:comment>
      <body>

        <script type = "text/javascript">
          var TipBoxID = "TipBox";
          var tip_box_id;
          function findPosX(obj)
          {
          var curleft = 0;
          if(obj.offsetParent)
          while(1)
          {
          curleft += obj.offsetLeft;
          if(!obj.offsetParent)
          break;
          obj = obj.offsetParent;
          }
          else if(obj.x)
          curleft += obj.x;
          return curleft;
          }

          function findPosY(obj)
          {
          var curtop = 0;
          if(obj.offsetParent)
          while(1)
          {
          curtop += obj.offsetTop;
          if(!obj.offsetParent)
          break;
          obj = obj.offsetParent;
          }
          else if(obj.y)
          curtop += obj.y;
          return curtop;
          }

          function HideTip() {
          tip_box_id.style.display = "none";
          }

          function DisplayTip(me,offX,offY) {
          var content = me.innerHTML;
          var tdLength = me.parentNode.offsetWidth;
          var textLength = me.innerHTML.length;
          if(((textLength-1)*10) > tdLength) {
          var tipO = me;
          tip_box_id = document.getElementById(TipBoxID);
          var x = findPosX(me);
          var y = findPosY(me);
          var left = x + offX - 100;

          if( left &lt; 0) {
          left = 0;
          }
          var top = y + offY - 10;

          tip_box_id.style.left = String(parseInt(left) + 'px');
          tip_box_id.style.top = String(parseInt(top) + 'px');
          tip_box_id.innerHTML = content;
          tip_box_id.style.display = "block";
          tipO.onmouseout = HideTip;
          }

          }


        </script>

        <!-- source -->
        <h2 align="center">
          <xsl:call-template name="documentTitle">
            <xsl:with-param name="root" select="."/>
          </xsl:call-template>
        </h2>

        <!-- title -->
        <div style="text-align:center;">
          <span style="font-size:larger;font-weight:bold;">
            <xsl:value-of select="n1:code/@displayName"/>
          </span>
        </div>

        <!-- Report ID#'s -->
        <p align='center'>
          <b>
            <xsl:text>Created On: </xsl:text>
          </b>
          <xsl:choose>
            <xsl:when test="string-length(/n1:ClinicalDocument/n1:effectiveTime/@value)=0">
              <xsl:text>Not Available</xsl:text>
            </xsl:when>
            <xsl:when test="starts-with(/n1:ClinicalDocument/n1:effectiveTime/@value,' ')">
              <xsl:text>Not Available</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="formatDateFull">
                <xsl:with-param name="date" select="/n1:ClinicalDocument/n1:effectiveTime/@value"/>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:if test="/n1:ClinicalDocument/n1:documentationOf/n1:serviceEvent/n1:performer/n1:effectiveTime/n1:low/@value">
            <xsl:text disable-output-escaping="yes">&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;</xsl:text>
            <b>
              <xsl:text>Date Range: </xsl:text>
            </b>
            <xsl:choose>
              <xsl:when test="string-length(/n1:ClinicalDocument/n1:documentationOf/n1:serviceEvent/n1:performer/n1:effectiveTime/n1:low/@value)=0">
                <xsl:text>Not Available</xsl:text>
              </xsl:when>
              <xsl:when test="starts-with(/n1:ClinicalDocument/n1:documentationOf/n1:serviceEvent/n1:performer/n1:effectiveTime/n1:low/@value,' ')">
                <xsl:text>Not Available</xsl:text>
              </xsl:when>
              <xsl:otherwise>
                <xsl:call-template name="formatDateFull">
                  <xsl:with-param name="date" select="/n1:ClinicalDocument/n1:documentationOf/n1:serviceEvent/n1:performer/n1:effectiveTime/n1:low/@value"/>
                </xsl:call-template>
              </xsl:otherwise>
            </xsl:choose>
            <b>
              <xsl:text disable-output-escaping="yes"> - </xsl:text>
            </b>
            <xsl:choose>
              <xsl:when test="string-length(/n1:ClinicalDocument/n1:documentationOf/n1:serviceEvent/n1:performer/n1:effectiveTime/n1:high/@value)=0">
                <xsl:text>Not Available</xsl:text>
              </xsl:when>
              <xsl:when test="starts-with(/n1:ClinicalDocument/n1:documentationOf/n1:serviceEvent/n1:performer/n1:effectiveTime/n1:high/@value,' ')">
                <xsl:text>Not Available</xsl:text>
              </xsl:when>
              <xsl:otherwise>
                <xsl:call-template name="formatDateFull">
                  <xsl:with-param name="date" select="/n1:ClinicalDocument/n1:documentationOf/n1:serviceEvent/n1:performer/n1:effectiveTime/n1:high/@value"/>
                </xsl:call-template>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:if>
        </p>

        <table width='100%' class="first">
          <xsl:variable name="patientRole" select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole"/>
          <tr>
            <td width='10%' valign="top">
              <b>
                <xsl:text>Patient: </xsl:text>
              </b>
            </td>
            <td width='35%' valign="top">
              <xsl:call-template name="getName">
                <xsl:with-param name="name" select="$patientRole/n1:patient/n1:name"/>
              </xsl:call-template>
              <xsl:if test="$patientRole/n1:addr">
                <xsl:call-template name="getAddress">
                  <xsl:with-param name="addr" select="$patientRole/n1:addr"/>
                </xsl:call-template>
              </xsl:if>
              <xsl:if test="$patientRole/n1:telecom">
                <xsl:call-template name="getTelecom">
                  <xsl:with-param name="telecom" select="$patientRole/n1:telecom"/>
                </xsl:call-template>
              </xsl:if>
            </td>
            <td width='15%' valign="top">
              <b>
                <xsl:text>Patient ID: </xsl:text>
              </b>
            </td>
            <td width='15%' valign="top">
              <!--  Replaced to support HRNs - AMS
              <xsl:if test="string-length($patientRole/n1:id/@extension)>0">
                <xsl:value-of select="$patientRole/n1:id/@extension"/>
              </xsl:if>-->
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

            </td>
            <td width="5%" valign="top">
            </td>
            <td width='15%' valign="top">
            </td>
          </tr>

          <tr>
            <td width="10%" valign="top">
              <b>
                <xsl:text>Birthdate: </xsl:text>
              </b>
            </td>
            <td width='35%' valign="top">
              <xsl:call-template name="formatDateFull">
                <xsl:with-param name="date" select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole/n1:patient/n1:birthTime/@value"/>
              </xsl:call-template>
            </td>
            <td width="15%" valign="top">
              <b>
                <xsl:text>Gender: </xsl:text>
              </b>
            </td>
            <td width='15%' valign="top">
              <xsl:choose>
                <xsl:when test="string-length(/n1:ClinicalDocument/n1:recordTarget/n1:patientRole/n1:patient/n1:administrativeGenderCode/@code)!=0">
                  <xsl:value-of select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole/n1:patient/n1:administrativeGenderCode/@code"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole/n1:patient/n1:administrativeGenderCode/n1:originalText"/>
                </xsl:otherwise>
              </xsl:choose>
            </td>
            <td width="5%" valign="top">
            </td>
            <td width='15%' valign="top">
              <b>
                <xsl:text>Marital Status: </xsl:text>
              </b>
              <xsl:choose>
                <xsl:when test="string-length(/n1:ClinicalDocument/n1:recordTarget/n1:patientRole/n1:patient/n1:maritalStatusCode/@displayName)!=0">
                  <xsl:value-of select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole/n1:patient/n1:maritalStatusCode/@displayName"/>    
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="/n1:ClinicalDocument/n1:recordTarget/n1:patientRole/n1:patient/n1:maritalStatusCode/n1:originalText"/>
                </xsl:otherwise>
              </xsl:choose>
              
              
            </td>
          </tr>

          <tr>
            <td width="10%" valign="top">
              <b>
                <xsl:text>Language(s):</xsl:text>
              </b>
            </td>
            <td width="35%" valign="top">
              <xsl:choose>
                <xsl:when test="starts-with($patientRole/n1:patient/n1:languageCommunication/n1:languageCode/@nullFlavor,'UNK') != 'true'">
                  <xsl:value-of select="$patientRole/n1:patient/n1:languageCommunication"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:text>UNK</xsl:text>
                </xsl:otherwise>
              </xsl:choose>
            </td>
            <td width="15%" valign="top">
              <b>
                <xsl:text>Religious Affiliation: </xsl:text>
              </b>
            </td>
            <td width="15%" valign="top">
              <xsl:choose>
                <xsl:when test="$patientRole/n1:patient/n1:religiousAffiliationCode/n1:originalText">
                  <xsl:value-of select="$patientRole/n1:patient/n1:religiousAffiliationCode/n1:originalText/text()"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$patientRole/n1:patient/n1:religiousAffiliationCode/@nullFlavor"/>
                </xsl:otherwise>
              </xsl:choose>
            </td>
            <td width="5%" valign="top">
            </td>
            <td width='15%' valign="top">
            </td>
          </tr>
        </table>

        <xsl:if test="n1:author">
          <table width="100%" class="second">
            <tr>
              <td width="10%">
                <b>Source:</b>
              </td>
              <td width="35%">
                <xsl:value-of select="n1:author/n1:assignedAuthor/n1:representedOrganization/n1:name/text()"/>
              </td>
              <td width='15%' valign="top">
              </td>
              <td width='15%' valign="top">
              </td>
              <td width="5%" valign="top">
              </td>
              <td width='15%' valign="top">
              </td>              
            </tr>
          </table>
        </xsl:if>

        <div>
          <h3>
            <a name="toc">Table of Contents</a>
          </h3>
          <ul>
            <xsl:for-each select="n1:component/n1:structuredBody/n1:component/n1:section/n1:title">
              <li>
                <a href="#{generate-id(.)}">
                  <xsl:value-of select="."/>
                </a>
              </li>
            </xsl:for-each>
          </ul>
        </div>
        <xsl:apply-templates select="n1:component/n1:structuredBody"/>
        <br></br>
        <br></br>
        <xsl:if test="string-length(/n1:ClinicalDocument/n1:participant[@typeCode='IND']/n1:associatedEntity[@classCode='NOK'])>0 or string-length(/n1:ClinicalDocument/n1:participant[@typeCode='IND']/n1:associatedEntity[@classCode='ECON'])>0">
          <table>
            <tr valign="top">
              <xsl:if test="string-length(/n1:ClinicalDocument/n1:participant[@typeCode='IND']/n1:associatedEntity[@classCode='NOK'])>0">
                <td>
                  <table class="first">
                    <tr>
                      <td width="100px" valign="top" align='left'>
                        <b>Next of Kin: </b>
                      </td>
                      <td valign="top">
                        <xsl:call-template name="getParticipant">
                          <xsl:with-param name="participant" select="/n1:ClinicalDocument/n1:participant[@typeCode='IND']/n1:associatedEntity[@classCode='NOK']"/>
                        </xsl:call-template>
                      </td>
                      <td width="50px"> </td>
                    </tr>
                  </table>
                </td>
              </xsl:if>
              <xsl:if test="string-length(/n1:ClinicalDocument/n1:participant[@typeCode='IND']/n1:associatedEntity[@classCode='ECON'])>0">
                <td>
                  <table class="first">
                    <tr>
                      <td width="150px" valign="top" align='left'>
                        <b>Emergency Contact: </b>
                      </td>
                      <td valign="top">
                        <xsl:call-template name="getParticipant">
                          <xsl:with-param name="participant" select="/n1:ClinicalDocument/n1:participant[@typeCode='IND']/n1:associatedEntity[@classCode='ECON']"/>
                        </xsl:call-template>
                      </td>
                      <td width="50px"> </td>
                    </tr>
                  </table>
                </td>
              </xsl:if>
            </tr>
          </table>
        </xsl:if>
        <xsl:call-template name="bottomline"/>

        <div
   id="TipBox"
   style="
      display:none;
      position:absolute; 
      font-size:12px;
      font-weight:bold;
      font-family:verdana;
      border:#72B0E6 solid 1px;
      padding:15px;
      color:black;
      background-color:#FFFFFF;">
        </div>



      </body>
    </html>
  </xsl:template>

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
      <xsl:if test="$participant/n1:code/n1:originalText">
        <br/>
        Relationship:
        <xsl:value-of select="$participant/n1:code/n1:originalText"/>
      </xsl:if>
    </p>
  </xsl:template>

  <xsl:template name="getSingleAddress">
    <xsl:param name="addr"/>
    <xsl:if test="$addr/n1:streetAddressLine != ' '">
      <br/>
      <xsl:if test="string-length($addr/n1:streetAddressLine)>0">
        <xsl:value-of select="$addr/n1:streetAddressLine"/>
      </xsl:if>

      <br/>
      <xsl:value-of select="$addr/n1:city"/>,
      <xsl:value-of select="$addr/n1:state"/>,
      <xsl:value-of select="$addr/n1:postalCode"/>
    </xsl:if>
  </xsl:template>

  <xsl:template name="getAddress">
    <xsl:param name="addr"/>
    <xsl:if test="$addr/n1:streetAddressLine != ' '">
      <xsl:for-each select="$addr/n1:streetAddressLine">
        <br/>
        <xsl:if test="string-length($addr/n1:streetAddressLine)>0">
          <xsl:value-of select="."/>
        </xsl:if>
      </xsl:for-each>
      <br/>
      <xsl:value-of select="$addr/n1:city"/>,
      <xsl:value-of select="$addr/n1:state"/>,
      <xsl:value-of select="$addr/n1:postalCode"/>
    </xsl:if>
  </xsl:template>

  <xsl:template name="getTelecom">
    <xsl:param name="telecom"/>
    <br/>
    <xsl:if test="string-length($telecom/@value)>0">
      <xsl:value-of select="$telecom/@value"/>
    </xsl:if>
  </xsl:template>

  <!-- Get a Name  -->
  <xsl:template name="getName">
    <xsl:param name="name"/>
    <xsl:choose>
      <xsl:when test="string-length($name/n1:family)=0">
      </xsl:when>
      <xsl:when test="$name/n1:family">
        <xsl:if test="string-length($name/n1:given)>0">
          <xsl:value-of select="$name/n1:given"/>
        </xsl:if>
        <xsl:text> </xsl:text>
        <xsl:if test="string-length($name/n1:family)>0">
          <xsl:value-of select="$name/n1:family"/>
        </xsl:if>
        <xsl:text> </xsl:text>
        <xsl:if test="string-length($name/n1:suffix)>0">
          <xsl:if test="$name/n1:suffix != ' '">
            <xsl:text>, </xsl:text>
            <xsl:value-of select="$name/n1:suffix"/>
          </xsl:if>
        </xsl:if>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$name"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--  Format Date 
      outputs a date in Month Day, Year form
      e.g., 19991207  ==> Dec 07 Dec 99 -->
  <xsl:template name="formatDate">
    <xsl:param name="date"/>
    <xsl:if test="string-length($date)>0">
      <xsl:value-of select="substring ($date, 7, 2)"/>
      <xsl:text> </xsl:text>
      <xsl:variable name="month" select="substring ($date, 5, 2)"/>
      <xsl:choose>
        <xsl:when test="$month='01'">
          <xsl:text>Jan </xsl:text>
        </xsl:when>
        <xsl:when test="$month='02'">
          <xsl:text>Feb </xsl:text>
        </xsl:when>
        <xsl:when test="$month='03'">
          <xsl:text>Mar </xsl:text>
        </xsl:when>
        <xsl:when test="$month='04'">
          <xsl:text>Apr </xsl:text>
        </xsl:when>
        <xsl:when test="$month='05'">
          <xsl:text>May </xsl:text>
        </xsl:when>
        <xsl:when test="$month='06'">
          <xsl:text>Jun </xsl:text>
        </xsl:when>
        <xsl:when test="$month='07'">
          <xsl:text>Jul </xsl:text>
        </xsl:when>
        <xsl:when test="$month='08'">
          <xsl:text>Aug </xsl:text>
        </xsl:when>
        <xsl:when test="$month='09'">
          <xsl:text>Sep </xsl:text>
        </xsl:when>
        <xsl:when test="$month='10'">
          <xsl:text>Oct </xsl:text>
        </xsl:when>
        <xsl:when test="$month='11'">
          <xsl:text>Nov </xsl:text>
        </xsl:when>
        <xsl:when test="$month='12'">
          <xsl:text>Dec </xsl:text>
        </xsl:when>
      </xsl:choose>
      <xsl:value-of select="substring ($date, 1, 4)"/>
    </xsl:if>
  </xsl:template>

  <!--  Format Date 
      outputs a date in Month Day, Year form
      e.g., 19991207  ==> December 07, 1999 -->
  <xsl:template name="formatDateFull">
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

  <!-- StructuredBody -->

  <!-- Component/Section -->
  <xsl:template match="n1:component/n1:section" name="detailSection">
    <xsl:apply-templates select="n1:title"/>
    <xsl:choose>
      <xsl:when test="n1:code[@code=$loincProblemCode]">
        <xsl:call-template name="problemDetails">
          <xsl:with-param select="." name="section"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="n1:code[@code=$loincAllergyCode]">
        <xsl:call-template name="allergyDetails">
          <xsl:with-param select="." name="section"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="n1:code[@code=$loincMedCode]">
        <xsl:call-template name="medDetails">
          <xsl:with-param select="." name="section"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="n1:code[@code=$loincVitalsCode]">
        <xsl:call-template name="vitalsDetails">
          <xsl:with-param select="." name="section"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="n1:code[@code=$loincLabsCode]">
        <xsl:call-template name="labsDetails">
          <xsl:with-param select="." name="section"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="n1:code[@code=$loincImmunizationsCode]">
        <xsl:call-template name="immunizationsDetails">
          <xsl:with-param select="." name="section"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="n1:code[@code=$loincEncounterCode]">
        <xsl:call-template name="encounterDetails">
          <xsl:with-param select="." name="section" />
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="n1:code[@code=$loincProceduresCode]">
        <xsl:call-template name="proceduresDetails">
          <xsl:with-param select="." name="section" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="n1:text"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="n1:component/n1:section"/>
  </xsl:template>

  <!-- Vitals Detail Section -->
  <xsl:template name="vitalsDetails">
    <xsl:param name="section"/>
    <table border="1" style="font-size:14px">
      <thead>
        <tr>
          <th class="first">Date</th>
          <th class="first">TEMP</th>
          <th class="first">PULSE</th>
          <th class="first">RESP</th>
          <th class="first">BP</th>
          <th class="first">Ht</th>
          <th class="first">Wt</th>
          <th class="first">POx</th>
          <th class="first">Source</th>
        </tr>
      </thead>
      <tbody>
        <xsl:apply-templates select="$section/n1:entry">
        </xsl:apply-templates>
      </tbody>
    </table>
    <xsl:call-template name="displayComments">
      <xsl:with-param name="section" select="$section"/>
    </xsl:call-template>

  </xsl:template>

  <!-- Labs Detail Section -->
  <xsl:template name="labsDetails">
    <xsl:param name="section"/>
    <table border="1" style="font-size:14px">
      <thead>
        <tr>
          <th class="first">Date</th>
          <th class="first">Test</th>
          <th class="first">Result - Unit</th>
          <th class="first">Interpretation</th>
          <th class="first">Ref Range</th>
          <th class="first">Status</th>
        </tr>
      </thead>
      <tbody>
        <xsl:apply-templates select="$section/n1:entry">
        </xsl:apply-templates>
      </tbody>
    </table>
    <xsl:call-template name="displayComments">
      <xsl:with-param name="section" select="$section"/>
    </xsl:call-template>

  </xsl:template>

  <!-- Immunizations Detail Section -->
  <xsl:template name="immunizationsDetails">
    <xsl:param name="section"/>
    <table border="1" style="font-size:14px">
      <thead>
        <tr>
          <th class="first">Immunizations</th>
          <th class="first">Series</th>
          <th class="first">Date Issued</th>
          <th class="first">Reaction</th>
          <th class="first">Comments</th>
        </tr>
      </thead>
      <tbody>
        <xsl:apply-templates select="$section/n1:entry">
        </xsl:apply-templates>
      </tbody>
    </table>
    <xsl:call-template name="displayComments">
      <xsl:with-param name="section" select="$section"/>
    </xsl:call-template>
  </xsl:template>

  <!-- Meds Detail Section -->
  <xsl:template name="medDetails">
    <xsl:param name="section"/>
    <table border="1" style="font-size:14px">
      <thead>
        <tr>
          <th class="first">Medications</th>
          <th class="first">Status</th>
          <th class="first">Sig</th>
          <th class="first">Ordered Quantity</th>
          <th class="first">No of Refills</th>
          <th class="first">Refills Left</th>
          <th class="first">Order Date</th>
          <th class="first">Prescriber</th>
          <th class="first">Dispense Date</th>
          <th class="first">Prescription NBR</th>
          <th class="first">Source</th>
        </tr>
      </thead>
      <tbody>
        <xsl:apply-templates select="$section/n1:entry">
        </xsl:apply-templates>
      </tbody>
    </table>
    <xsl:call-template name="displayComments">
      <xsl:with-param name="section" select="$section"/>
    </xsl:call-template>
  </xsl:template>

  <!-- Problem Detail Section -->
  <xsl:template name="problemDetails">
    <xsl:param name="section"/>
    <table border="1" style="font-size:14px">
      <thead>
        <tr>
          <th class="first">Problems</th>
          <th class="first">Status</th>
          <th class="first">Code</th>
          <th class="first">Date of Onset</th>
          <th class="first">Provider</th>
        </tr>

      </thead>
      <tbody>
        <xsl:apply-templates select="$section/n1:entry">
        </xsl:apply-templates>
      </tbody>
    </table>
    <xsl:call-template name="displayComments">
      <xsl:with-param name="section" select="$section"/>
    </xsl:call-template>
  </xsl:template>

  <!-- Allergy Detail Section -->
  <xsl:template name="allergyDetails">
    <xsl:param name="section"/>
    <table border="1" style="font-size:14px">
      <thead>
        <tr>
          <th class="first">Allergens</th>
          <th class="first">Reaction</th>
          <th class="first">Severity</th>
          <th class="first">Verification Date</th>
          <th class="first">Source</th>
        </tr>
      </thead>
      <tbody>
        <xsl:apply-templates select="$section/n1:entry">
        </xsl:apply-templates>
      </tbody>
    </table>
    <xsl:call-template name="displayComments">
      <xsl:with-param name="section" select="$section"/>
    </xsl:call-template>
  </xsl:template>

  <!-- Encounter Detail Section -->
  <xsl:template name="encounterDetails">
    <xsl:param name="section" />
    <table border="1" style="font-size:14px;">
      <thead>
        <tr>
          <th class="first">Date/Time</th>
          <th class="first">Encounter Type</th>
          <th class="first">Encounter Type Comments</th>
          <th class="first">Provider</th>
        </tr>
      </thead>
      <tbody>
        <xsl:apply-templates select="$section/n1:entry">
          <xsl:sort select="n1:encounter/n1:effectiveTime/n1:low/@value" order="descending" />
        </xsl:apply-templates>
      </tbody>
    </table>
    <br />
    <br />
  </xsl:template>

  <!-- Procedure Detail Section -->
  <xsl:template name="proceduresDetails">
    <xsl:param name="section" />
    <table border="1" style="font-size:14px">
      <thead>
        <tr>
          <th class="first">Date/Time</th>
          <th class="first">Procedure Type</th>
          <th class="first">Procedure Comment</th>
          <th class="first">Provider</th>
        </tr>
      </thead>
      <tbody>
        <xsl:apply-templates select="$section/n1:entry">
          <xsl:sort select="n1:procedure/n1:effectiveTime/n1:low/@value" order="descending" />
        </xsl:apply-templates>
      </tbody>
    </table>
    <br />
    <br />
  </xsl:template>

  <!-- entry processing -->
  <xsl:template match="n1:entry">
    <xsl:variable name="allergy-prob-Root" select="n1:act/n1:entryRelationship/n1:observation/n1:templateId/@root"/>
    <xsl:variable name="med-imm-Root" select="n1:substanceAdministration/n1:templateId/@root"/>
    <xsl:variable name="labs-Root" select="../n1:templateId/@root"/>
    <xsl:variable name="vitals-Root" select="../n1:templateId/@root"/>
    
    <xsl:choose>
      <xsl:when test="$allergy-prob-Root='2.16.840.1.113883.10.20.1.18'">
        <xsl:call-template name="allergyRow">
          <xsl:with-param name="row" select="."/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$allergy-prob-Root!='2.16.840.1.113883.10.20.1.18'">
        <xsl:call-template name="problemRow">
          <xsl:with-param name="row" select="."/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$med-imm-Root='2.16.840.1.113883.3.88.11.83.13'">
        <xsl:call-template name="immunizationsRow">
          <xsl:with-param name="row" select="."/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$med-imm-Root!='2.16.840.1.113883.3.88.11.83.13'">
        <xsl:call-template name="medRow">
          <xsl:with-param name="row" select="."/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$vitals-Root='2.16.840.1.113883.10.20.1.16'">
        <xsl:call-template name="vitalsRow">
          <xsl:with-param name="row" select="."/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$labs-Root='2.16.840.1.113883.10.20.1.14'">
        <xsl:call-template name="labsRow">
          <xsl:with-param name="row" select="."/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="n1:encounter">
        <xsl:call-template name="encRow">
          <xsl:with-param name="row" select="." />
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="n1:procedure">
        <xsl:call-template name="procedureRow">
          <xsl:with-param name="row" select="." />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise/>
    </xsl:choose>
  </xsl:template>

  <!-- Medication Entry row -->
  <xsl:template name="medRow">
    <xsl:param name="row"/>

    <tr class="second">
      <!-- Medications -->
      <td>
        <div style="overflow:hidden; ">
          <!--KP & MedVA-->
          <xsl:value-of select="$row/n1:substanceAdministration/n1:consumable/n1:manufacturedProduct/n1:manufacturedMaterial/n1:code/n1:originalText"/>

          <!--VA-->
          <xsl:variable name="medReference" select="$row/n1:substanceAdministration/n1:consumable/n1:manufacturedProduct/n1:manufacturedMaterial/n1:code/n1:originalText/n1:reference/@value"/>
          <xsl:value-of select="../n1:text/n1:list/n1:item/n1:content[@ID=$medReference]"/>
        </div>
      </td>
      <!-- Status -->
      <td>
        <div style="overflow:hidden; white-space:nowrap;">
          <!--VA & DoD-->
          <xsl:value-of select="$row/n1:substanceAdministration/n1:entryRelationship/n1:observation/n1:value[@xsi:type='CE']/n1:originalText"/>
          <!--KP & MedVA-->
          <xsl:value-of select="$row/n1:substanceAdministration/n1:entryRelationship/n1:observation/n1:value/@displayName"/>
        </div>
      </td>
      <!-- Sig -->
      <td>
        <div style="overflow:hidden;">
          <xsl:variable name="sigReference" select="$row/n1:substanceAdministration/n1:text/n1:reference/@value"/>
          <xsl:value-of select="../n1:text/n1:list/n1:item/n1:content[@ID=$sigReference]"/>
          <xsl:value-of select="../n1:text/n1:table/n1:tbody/n1:tr/n1:td/n1:content[@ID=$sigReference]"/>

          <xsl:variable name="sigReference2" select="substring-after($row/n1:substanceAdministration/n1:text/n1:reference/@value,'#')"/>
          <xsl:value-of select="../n1:text/n1:list/n1:item/n1:content[@ID=$sigReference2]"/>
          <xsl:value-of select="../n1:text/n1:content[@ID=$sigReference2]"/>
        </div>
      </td>
      <!-- Ordered Quantity -->
      <td>
        <div style="overflow:hidden; white-space:nowrap;">
          <xsl:choose>
            <xsl:when test="string-length($row/n1:substanceAdministration/n1:entryRelationship/n1:supply[@classCode='SPLY' and @moodCode='INT']/n1:quantity/@value)=0">
              <xsl:text>--</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$row/n1:substanceAdministration/n1:entryRelationship/n1:supply[@classCode='SPLY' and @moodCode='INT']/n1:quantity/@value"/>
            </xsl:otherwise>
          </xsl:choose>
        </div>
      </td>
      <!-- No of Refills -->
      <td>
        <div style="overflow:hidden; white-space:nowrap;">
          <xsl:value-of select="$row/n1:substanceAdministration/n1:entryRelationship/n1:supply/n1:repeatNumber/@value"/>
          <xsl:if test="string-length($row/n1:substanceAdministration/n1:entryRelationship/n1:supply/n1:repeatNumber/@value)=0">
            <xsl:text>--</xsl:text>
          </xsl:if>
        </div>
      </td>
      <!-- Refills Left-->
      <td>
        <div style="overflow:hidden; white-space:nowrap;">
          <xsl:variable name="refillReference" select="$row/n1:substanceAdministration/n1:entryRelationship/n1:supply/n1:repeatNumber/@value"/>
          <xsl:variable name="lastFillReference" select="$row/n1:substanceAdministration/n1:entryRelationship[@typeCode='COMP']/n1:supply/n1:sequenceNum/@value"/>

          <xsl:choose>
            <xsl:when test="string-length(lastFillReference)=0">
              <xsl:choose>
                <xsl:when test="string-length($refillReference)!=0">
                  <xsl:value-of select="$refillReference"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:text>--</xsl:text>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$refillReference - $lastFillReference"/>
            </xsl:otherwise>
          </xsl:choose>
        </div>
      </td>
      <!-- Order Date-->
      <td>
        <div style="overflow:hidden; white-space:nowrap;">
          <xsl:choose>
            <xsl:when test="string-length($row/n1:substanceAdministration/n1:entryRelationship/n1:supply/n1:author/n1:time/@value)=0">
              <xsl:text>--</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="formatDate">
                <xsl:with-param name="date" select="$row/n1:substanceAdministration/n1:entryRelationship/n1:supply/n1:author/n1:time/@value"/>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </div>
      </td>
      <!-- Prescriber -->
      <td>
        <div style="overflow:hidden; white-space:nowrap;">

          <xsl:variable name="familyReference" select="$row/n1:substanceAdministration/n1:entryRelationship/n1:supply/n1:author/n1:assignedAuthor/n1:assignedPerson/n1:name/n1:family"/>
          <xsl:variable name="givenReference" select="$row/n1:substanceAdministration/n1:entryRelationship/n1:supply/n1:author/n1:assignedAuthor/n1:assignedPerson/n1:name/n1:given"/>

          <xsl:choose>
            <xsl:when test="string-length($familyReference)!=0 and string-length($givenReference)!=0">
              <xsl:value-of select="$givenReference" />
              <xsl:text> </xsl:text>
              <xsl:value-of select="$familyReference" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$row/n1:substanceAdministration/n1:entryRelationship/n1:supply/n1:author/n1:assignedAuthor/n1:assignedPerson/n1:name" />
              <xsl:if test="string-length($row/n1:substanceAdministration/n1:entryRelationship/n1:supply/n1:author/n1:assignedAuthor/n1:assignedPerson/n1:name)=0">
                <xsl:text>--</xsl:text>
              </xsl:if>
            </xsl:otherwise>
          </xsl:choose>
        </div>
      </td>
      <!-- Dispense Date -->
      <td>
        <div style="overflow:hidden; white-space:nowrap;">
          <xsl:choose>
            <xsl:when test="string-length($row/n1:substanceAdministration/n1:entryRelationship/n1:supply[@classCode='SPLY' and @moodCode='EVN']/n1:effectiveTime/@value)=0">
              <xsl:text>--</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="formatDate">
                <xsl:with-param name="date" select="$row/n1:substanceAdministration/n1:entryRelationship/n1:supply[@classCode='SPLY' and @moodCode='EVN']/n1:effectiveTime/@value"/>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </div>
      </td>
      <!-- Prescription ID (Nbr) -->
      <td>
        <div style="overflow:hidden; white-space:nowrap; width:80px;">
          <xsl:choose>
            <xsl:when test="string-length($row/n1:substanceAdministration/n1:entryRelationship/n1:supply[@classCode='SPLY' and @moodCode='EVN']/n1:id/@extension)=0">
              <xsl:text>--</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$row/n1:substanceAdministration/n1:entryRelationship/n1:supply[@classCode='SPLY' and @moodCode='EVN']/n1:id/@extension"/>
            </xsl:otherwise>
          </xsl:choose>
        </div>
      </td>
      <!-- Source -->
      <td>
        <div style="overflow:hidden; white-space:nowrap;">
          <xsl:value-of select="$row/n1:substanceAdministration/n1:author/n1:assignedAuthor/n1:representedOrganization/n1:name"/>
          <xsl:if test="string-length($row/n1:substanceAdministration/n1:author/n1:assignedAuthor/n1:representedOrganization/n1:name)=0">
            <xsl:text>--</xsl:text>
          </xsl:if>
        </div>
      </td>
    </tr>
  </xsl:template>

  <!-- problem entry row -->
  <xsl:template name="problemRow">
    <xsl:param name="row"/>
    <xsl:variable name="rowData" select="$row/n1:act/n1:entryRelationship/n1:observation"/>
    <tr class="second">
      
      <!-- Problems -->
      <td>
        <div style="overflow:hidden; white-space:nowrap;">

          <xsl:variable name="probReference" select="substring-after($row/n1:act/n1:entryRelationship/n1:observation/n1:text/n1:reference/@value,'#')"/>
          <xsl:value-of select="../n1:text/n1:paragraph[@ID=$probReference]" />
          <xsl:value-of select="../n1:text/n1:content[@ID=$probReference]" />

          <xsl:variable name="probReference2" select="$row/n1:act/n1:entryRelationship/n1:observation/n1:text/n1:reference/@value"/>
          <xsl:value-of select="../n1:text/n1:paragraph[@ID=$probReference2]" />

          <xsl:value-of select="$row/n1:act/n1:entryRelationship[@typeCode='SUBJ']/n1:observation/n1:value/n1:originalText"/>


        </div>
      </td>

      <!-- Status -->
      <td>
        <div style="overflow:hidden; white-space:nowrap;">
          <xsl:value-of select="$row/n1:act/n1:entryRelationship[@typeCode='SUBJ']/n1:observation/n1:entryRelationship[@typeCode='REFR']/n1:observation/n1:value/@displayName"/>
          <xsl:value-of select="$row/n1:act/n1:entryRelationship[@typeCode='REFR']/n1:observation/n1:value/@displayName"/>
        </div>
      </td>
      
      <!-- Code -->
      <td>
        <div style="overflow:hidden; white-space:nowrap;">
          <xsl:value-of select="$row/n1:act/n1:entryRelationship[@typeCode='SUBJ']/n1:observation/n1:value/@code"/>
          <xsl:value-of select="$row/n1:act/n1:entryRelationship/n1:observation/n1:value/n1:translation/@code"/>
          <xsl:if test="string-length($row/n1:act/n1:entryRelationship[@typeCode='SUBJ']/n1:observation/n1:value/@code)=0">
            <xsl:if test="string-length($row/n1:act/n1:entryRelationship/n1:observation/n1:value/n1:translation/@code)=0">
              <xsl:text>--</xsl:text>
            </xsl:if>
          </xsl:if>
        </div>
      </td>
      
      <!-- Date of Onset -->
      <td>
        <div style="overflow:hidden; white-space:nowrap; width:140px;">

          <xsl:choose>
            <xsl:when test="string-length($row/n1:act/n1:effectiveTime/n1:low/@value)!=0">
              <xsl:call-template name="formatDate">
                <xsl:with-param name="date" select="$row/n1:act/n1:effectiveTime/n1:low/@value"/>
              </xsl:call-template>              
            </xsl:when>
            <xsl:otherwise>
              <xsl:choose>
                <xsl:when test="string-length($row/n1:act/n1:entryRelationship/n1:observation/n1:effectiveTime/n1:low/@value)!=0">
                  <xsl:call-template name="formatDate">
                    <xsl:with-param name="date" select="$row/n1:act/n1:entryRelationship/n1:observation/n1:effectiveTime/n1:low/@value"/>
                  </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:text>--</xsl:text>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:otherwise>
          </xsl:choose>
        </div>
      </td>
      
      <!-- Provider -->
      <td>
        <div style="overflow:hidden; white-space:nowrap; width:180px;">
          <xsl:variable name="providerReference" select="$row/n1:act/n1:performer/n1:assignedEntity/n1:id/@extension"/>
          <xsl:variable name="provider" select="/n1:ClinicalDocument/n1:documentationOf/n1:serviceEvent/n1:performer/n1:assignedEntity/n1:id[@extension=$providerReference]/../n1:assignedPerson/n1:name"/>
          <xsl:choose>
            <xsl:when test="string-length($provider)!=0">
              <xsl:value-of select="$provider" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:text>--</xsl:text>
            </xsl:otherwise>
          </xsl:choose>
        </div>
      </td>
    </tr>
  </xsl:template>

  <!-- vitals entry row -->
  <xsl:template name="vitalsRow">
    <xsl:param name="row"/>
    <xsl:variable name="rowData" select="$row/n1:organizer/n1:component/n1:observation"/>
    <xsl:variable name="height" select="$row/n1:organizer/n1:component/n1:observation/n1:code[@code='8302-2']/.."/>
    <xsl:variable name="weight" select="$row/n1:organizer/n1:component/n1:observation/n1:code[@code='29463-7']/.."/>
    <xsl:variable name="systolic" select="$row/n1:organizer/n1:component/n1:observation/n1:code[@code='8480-6']/.."/>
    <xsl:variable name="diastolic" select="$row/n1:organizer/n1:component/n1:observation/n1:code[@code='8462-4']/.."/>
    <xsl:variable name="temp" select="$row/n1:organizer/n1:component/n1:observation/n1:code[@code='8310-5']/.."/>
    <xsl:variable name="pulse" select="$row/n1:organizer/n1:component/n1:observation/n1:code[@code='8867-4']/.."/>
    <xsl:variable name="resp" select="$row/n1:organizer/n1:component/n1:observation/n1:code[@code='9279-1']/.."/>
    <xsl:variable name="pox" select="$row/n1:organizer/n1:component/n1:observation/n1:code[@code='2710-2']/.."/>
    <tr class="second">
      <!-- observation text -->
      <!-- problem effective date -->
      <td>
        <div style="overflow:hidden; white-space:nowrap; width:80px;">
          <xsl:choose>
            <xsl:when test="string-length($rowData/n1:effectiveTime/@value)=0">
              <xsl:text>-- Not Available --</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="formatDate">
                <xsl:with-param name="date" select="$rowData/n1:effectiveTime/@value"/>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </div>
      </td>
      <!-- temp -->
      <td>
        <div style="overflow:hidden; white-space:nowrap; width:40px;">
          <xsl:value-of select="$temp/n1:value/@value"/>
        </div>
      </td>
      <!-- pulse -->
      <td>
        <div style="overflow:hidden; white-space:nowrap; width:40px;">
          <xsl:value-of select="$pulse/n1:value/@value"/>
        </div>
      </td>
      <!-- resp -->
      <td>
        <div style="overflow:hidden; white-space:nowrap; width:40px;">
          <xsl:value-of select="$resp/n1:value/@value"/>
        </div>
      </td>
      <!-- BP  systolic / diastolic -->
      <td>
        <div style="overflow:hidden; white-space:nowrap; width:60px;">
          <xsl:value-of select="$systolic/n1:value/@value"/>
          /
          <xsl:value-of select="$diastolic/n1:value/@value"/>
        </div>
      </td>
      <!-- height -->
      <td>
        <div style="overflow:hidden; white-space:nowrap; width:40px;">
          <xsl:value-of select="$height/n1:value/@value"/>
          <xsl:if test="string-length($height/n1:value/@value)=0">
            <xsl:text>--</xsl:text>
          </xsl:if>
        </div>
      </td>
      <!-- weight -->
      <td>
        <div style="overflow:hidden; white-space:nowrap; width:40px;">
          <xsl:value-of select="$weight/n1:value/@value"/>
          <xsl:if test="string-length($weight/n1:value/@value)=0">
            <xsl:text>--</xsl:text>
          </xsl:if>
        </div>
      </td>
      <!-- pox -->
      <td>
        <div style="overflow:hidden; white-space:nowrap; width:40px;">
          <xsl:value-of select="$pox/n1:value/@value"/>
          <xsl:if test="string-length($pox/n1:value/@value)=0">
            <xsl:text>--</xsl:text>
          </xsl:if>
        </div>
      </td>
      <!-- source -->
      <td>
        <div style="overflow:hidden; white-space:nowrap; width:120px;">
          <xsl:value-of select="$row/n1:organizer/n1:author/n1:assignedAuthor/n1:representedOrganization/n1:name"/>
        </div>
      </td>
    </tr>
  </xsl:template>

  <!-- labs entry row -->
  <xsl:template name="labsRow">
    <xsl:param name="row"/>
    
    <xsl:choose>
      <xsl:when  test="string-length($row/n1:organizer)!=0">
        <xsl:for-each select="$row/n1:organizer/n1:component">
          <tr class="second">
            <!-- Date -->
            <td>
              <div style="overflow:hidden; white-space:nowrap;">
                <xsl:choose>
                  <xsl:when test="string-length(n1:observation/n1:effectiveTime/@value)=0">
                    <xsl:text> Not Available </xsl:text>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:call-template name="formatDate">
                      <xsl:with-param name="date" select="n1:observation/n1:effectiveTime/@value"/>
                    </xsl:call-template>
                  </xsl:otherwise>
                </xsl:choose>
              </div>
            </td>
            <!-- Test -->
            <td>
              <div style="overflow:hidden; white-space:nowrap;">
                <xsl:choose>
                  <xsl:when test="string-length(n1:observation/n1:code/@displayName)!=0">
                    <xsl:value-of select="n1:observation/n1:code/@displayName"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:variable name="labReference" select="n1:observation/n1:text/n1:reference/@value"/>
                    <xsl:value-of select="../n1:text/n1:table/n1:tbody/n1:tr/n1:td/n1:content[@ID=$labReference]"/>
                  </xsl:otherwise>
                </xsl:choose>
              </div>
            </td>
            <!-- Result - Unit -->
            <td>
              <div style="overflow:hidden; white-space:nowrap;">
                <xsl:choose>
                  <xsl:when test="string-length(n1:observation/n1:value/@value)!=0">
                    <xsl:value-of select="n1:observation/n1:value/@value" />
                    <xsl:if test="n1:observation/n1:value/@unit">
                      <xsl:text> </xsl:text>
                      <xsl:value-of select="n1:observation/n1:value/@unit" />
                    </xsl:if>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:choose>
                      <xsl:when test="string-length(n1:observation/n1:value/n1:high/@value)!=0">
                        <xsl:value-of select="n1:observation/n1:value/n1:high/@value" />
                        <xsl:if test="n1:observation/n1:value/n1:high/@unit">
                          <xsl:text> </xsl:text>
                          <xsl:value-of select="n1:observation/n1:value/n1:high/@unit" />
                        </xsl:if>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:choose>
                          <xsl:when test="string-length(n1:observation/n1:value/n1:low/@value)!=0">
                            <xsl:value-of select="n1:observation/n1:value/n1:low/@value" />
                            <xsl:if test="n1:observation/n1:value/n1:low/@unit">
                              <xsl:text> </xsl:text>
                              <xsl:value-of select="n1:observation/n1:value/n1:low/@unit" />
                            </xsl:if>
                          </xsl:when>
                          <xsl:otherwise>
                            <xsl:choose>
                              <xsl:when test="string-length(n1:observation/n1:value)!=0">
                                <xsl:value-of select="n1:observation/n1:value"/>
                              </xsl:when>
                              <xsl:otherwise>
                                <xsl:text>--</xsl:text>
                              </xsl:otherwise>
                            </xsl:choose>
                          </xsl:otherwise>
                        </xsl:choose>
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:otherwise>
                </xsl:choose>
              </div>
            </td>
            <!-- Interpretation -->
            <td>
              <div style="overflow:hidden; white-space:nowrap;">
                <xsl:value-of select="n1:observation/n1:interpretationCode/n1:originalText"/>
                <xsl:if test="string-length(n1:observation/n1:interpretationCode/n1:originalText)=0">
                  <xsl:text>--</xsl:text>
                </xsl:if>

              </div>
            </td>
            <!-- Ref Range -->
            <td>
              <div style="overflow:hidden; white-space:nowrap;">
                <xsl:call-template name="flyoverSpan">
                  <xsl:with-param name="data" select="n1:observation/n1:referenceRange/n1:observationRange/n1:text"/>
                </xsl:call-template>
                <xsl:if test="string-length(n1:observation/n1:referenceRange/n1:observationRange/n1:text)=0">
                  <xsl:text>--</xsl:text>
                </xsl:if>
              </div>
            </td>
            <!-- Status -->
            <td>
              <div style="overflow:hidden; white-space:nowrap;">
                <xsl:value-of select="n1:observation/n1:statusCode/@code"/>
              </div>
            </td>
          </tr>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="string-length($row/n1:observation/n1:code/@displayName)!=0 or $row/n1:observation/n1:text/n1:reference/@value">
            <tr class="second">
              <!-- Date -->
              <td>
                <div style="overflow:hidden; white-space:nowrap;">
                  <xsl:choose>
                    <xsl:when test="string-length($row/n1:observation/n1:effectiveTime/@value)=0">
                      <xsl:text> Not Available </xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:call-template name="formatDate">
                        <xsl:with-param name="date" select="$row/n1:observation/n1:effectiveTime/@value"/>
                      </xsl:call-template>
                    </xsl:otherwise>
                  </xsl:choose>
                </div>
              </td>
              <!-- Test -->
              <td>
                <div style="overflow:hidden; white-space:nowrap;">
                  <xsl:choose>
                    <xsl:when test="string-length($row/n1:observation/n1:code/@displayName)!=0">
                      <xsl:value-of select="$row/n1:observation/n1:code/@displayName"/>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:variable name="labReference" select="$row/n1:observation/n1:text/n1:reference/@value"/>
                      <xsl:value-of select="../n1:text/n1:table/n1:tbody/n1:tr/n1:td/n1:content[@ID=$labReference]"/>
                    </xsl:otherwise>
                  </xsl:choose>
                </div>
              </td>
              <!-- Result - Unit -->
              <td>
                <div style="overflow:hidden; white-space:nowrap;">
                  <xsl:choose>
                    <xsl:when test="string-length($row/n1:observation/n1:value/@value)!=0">
                      <xsl:value-of select="$row/n1:observation/n1:value/@value" />
                      <xsl:if test="$row/n1:observation/n1:value/@unit">
                        <xsl:text> </xsl:text>
                        <xsl:value-of select="$row/n1:observation/n1:value/@unit" />
                      </xsl:if>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:choose>
                        <xsl:when test="string-length($row/n1:observation/n1:value/n1:high/@value)!=0">
                          <xsl:value-of select="$row/n1:observation/n1:value/n1:high/@value" />
                          <xsl:if test="$row/n1:observation/n1:value/n1:high/@unit">
                            <xsl:text> </xsl:text>
                            <xsl:value-of select="$row/n1:observation/n1:value/n1:high/@unit" />
                          </xsl:if>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:choose>
                            <xsl:when test="string-length($row/n1:observation/n1:value/n1:low/@value)!=0">
                              <xsl:value-of select="$row/n1:observation/n1:value/n1:low/@value" />
                              <xsl:if test="$row/n1:observation/n1:value/n1:low/@unit">
                                <xsl:text> </xsl:text>
                                <xsl:value-of select="$row/n1:observation/n1:value/n1:low/@unit" />
                              </xsl:if>
                            </xsl:when>
                            <xsl:otherwise>
                              <xsl:choose>
                                <xsl:when test="string-length($row/n1:observation/n1:value)!=0">
                                  <xsl:value-of select="$row/n1:observation/n1:value"/>
                                </xsl:when>
                                <xsl:otherwise>
                                  <xsl:text>--</xsl:text>
                                </xsl:otherwise>
                              </xsl:choose>
                            </xsl:otherwise>
                          </xsl:choose>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:otherwise>
                  </xsl:choose>
                </div>
              </td>
              <!-- Interpretation -->
              <td>
                <div style="overflow:hidden; white-space:nowrap;">
                  <xsl:value-of select="$row/n1:observation/n1:interpretationCode/n1:originalText"/>
                  <xsl:if test="string-length($row/n1:observation/n1:interpretationCode/n1:originalText)=0">
                    <xsl:text>--</xsl:text>
                  </xsl:if>

                </div>
              </td>
              <!-- Ref Range -->
              <td>
                <div style="overflow:hidden; white-space:nowrap;">
                  <xsl:call-template name="flyoverSpan">
                    <xsl:with-param name="data" select="$row/n1:observation/n1:referenceRange/n1:observationRange/n1:text"/>
                  </xsl:call-template>
                  <xsl:if test="string-length($row/n1:observation/n1:referenceRange/n1:observationRange/n1:text)=0">
                    <xsl:text>--</xsl:text>
                  </xsl:if>
                </div>
              </td>
              <!-- Status -->
              <td>
                <div style="overflow:hidden; white-space:nowrap;">
                  <xsl:value-of select="$row/n1:observation/n1:statusCode/@code"/>
                </div>
              </td>
            </tr>
          </xsl:when>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- immunization entry row -->
  <xsl:template name="immunizationsRow">
    <xsl:param name="row"/>
    <xsl:variable name="rowData" select="$row/n1:substanceAdministration/n1:consumable/n1:manufacturedProduct/n1:manufacturedMaterial"/>
    <xsl:variable name="rowSubj" select="$row/n1:substanceAdministration/n1:entryRelationship[@typeCode='SUBJ']/n1:observation"/>
    <xsl:variable name="rowCause" select="$row/n1:substanceAdministration/n1:entryRelationship[@typeCode='CAUS']/n1:observation"/>

    <tr class="second">
      <!-- name -->
      <td>
        <div style="overflow:hidden; white-space:nowrap; width:360px;">
          <xsl:variable name="immReference" select="$rowData/n1:code/n1:originalText/n1:reference/@value"/>
          <xsl:variable name="imm" select="../n1:text/n1:content[@ID=$immReference]"/>
          <xsl:call-template name="flyoverSpan">
            <xsl:with-param name="data" select="$imm"/>
          </xsl:call-template>
        </div>
      </td>
      <!-- series -->
      <td>
        <div style="overflow:hidden; white-space:nowrap; width:60px;">
          <xsl:call-template name="flyoverSpan">
            <xsl:with-param name="data" select="$rowSubj/n1:value"/>
          </xsl:call-template>
        </div>
      </td>
      <!--  effective date -->
      <td>
        <div style="overflow:hidden; white-space:nowrap; width:100;">
          <xsl:choose>
            <xsl:when test="string-length($row/n1:substanceAdministration/n1:effectiveTime/@value)=0">
              <xsl:text>-- Not Available --</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="formatDate">
                <xsl:with-param name="date" select="$row/n1:substanceAdministration/n1:effectiveTime/@value"/>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </div>
      </td>
      <!-- reaction -->
      <td>
        <xsl:value-of select="rowCause/n1:id//@extension"/>
        <div style="overflow:hidden; white-space:nowrap; width:260px;">
          <xsl:variable name="reactionReference" select="$rowCause/n1:id/@extension"/>
          <xsl:variable name="reaction" select="../n1:text/n1:content[@ID=$reactionReference]"/>
          <xsl:call-template name="flyoverSpan">
            <xsl:with-param name="data" select="$reaction"/>
          </xsl:call-template>
        </div>
      </td>
      <!-- comments -->
      <td>
        <xsl:variable name="commentReference" select="$row/n1:substanceAdministration/n1:text/n1:reference/@value"/>
        <div style="overflow:hidden; white-space:nowrap; width:240px;">
          <xsl:call-template name="flyoverSpan">
            <xsl:with-param name="data" select="../n1:text/n1:content[@ID=$commentReference]"/>
          </xsl:call-template>
        </div>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="getReactionValue">
    <xsl:param name="reaction"/>
    <xsl:variable name="reactionReference" select="$reaction"/>
    <xsl:variable name="reactionValue" select="/n1:ClinicalDocument/n1:component/n1:structuredBody/n1:component/n1:section/n1:text/n1:content[@ID=$reactionReference]"/>
    <xsl:value-of select="$reactionValue"/>
    <br/>

  </xsl:template>

  <!-- allergy entry row -->
  <xsl:template name="allergyRow">
    <xsl:param name="row"/>
    <xsl:variable name="observation" select="$row/n1:act/n1:entryRelationship/n1:observation"/>
    <tr class="second">
      
      <!-- Allergens -->
      <td style="overflow:hidden; white-space:nowrap;">
        <xsl:value-of select="$observation/n1:participant/n1:participantRole/n1:playingEntity/n1:name"/>
      </td>
      
      <!-- Reaction-->
      <td style="overflow:hidden; white-space:nowrap;">
        <!--KP & MedVA-->
        <xsl:for-each select="$observation/n1:entryRelationship[@typeCode='MFST']">
          <xsl:variable name="reactionReference" select="substring-after(n1:observation/n1:text/n1:reference/@value,'#')"/>
          <xsl:text> </xsl:text>
          <xsl:value-of select="../../../../../n1:text/n1:content[@ID=$reactionReference]"/>
        </xsl:for-each>
        <!--VA-->
        <xsl:variable name="reactionReference2" select="$observation/n1:entryRelationship/n1:observation/n1:text/n1:reference/@value"/>
        <xsl:value-of select="../n1:text/n1:content[@ID=$reactionReference2]"/>

        <!--DoD-->
        <xsl:variable name="reactionReference3" select="$observation/n1:entryRelationship/n1:observation/n1:text/n1:reference/@value"/>
        <xsl:value-of select="../n1:text/n1:table/n1:tbody/n1:tr/n1:td/n1:content[@ID=$reactionReference3]"/>

        <xsl:if test="string-length($observation/n1:entryRelationship/n1:observation/n1:text/n1:reference/@value)=0">
          <xsl:text>--</xsl:text>
        </xsl:if>
      </td>

      <!-- Severity -->
      <td style="overflow:hidden; white-space:nowrap;">
        <xsl:choose>
          <xsl:when test="string-length($observation/n1:entryRelationship[@typeCode='SUBJ']/n1:observation/n1:text/n1:reference/@value)=0">
            <xsl:text>--</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:variable name="severityReference" select="$observation/n1:entryRelationship[@typeCode='SUBJ']/n1:observation/n1:text/n1:reference/@value"/>
            <xsl:value-of select="../n1:text/n1:content[@ID=$severityReference]"/>
          </xsl:otherwise>
        </xsl:choose>
      </td>
      
      <!-- Verification Date-->
      <td style="overflow:hidden; white-space:nowrap;">
        <xsl:choose>
          <xsl:when test="string-length($observation/n1:effectiveTime/n1:low/@value)=0">
            <xsl:text>Not Available</xsl:text>
          </xsl:when>
          <xsl:when test="$observation/n1:effectiveTime/n1:low/@value">
            <xsl:call-template name="formatDate">
              <xsl:with-param name="date" select="$observation/n1:effectiveTime/n1:low/@value"/>
            </xsl:call-template>
          </xsl:when>
        </xsl:choose>
      </td>

      <!-- Source -->
      <td style="overflow:hidden; white-space:nowrap;">
        <xsl:choose>
          <xsl:when test="string-length($row/n1:act/n1:author/n1:assignedAuthor/n1:representedOrganization/n1:name)=0">
            <xsl:text>--</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$row/n1:act/n1:author/n1:assignedAuthor/n1:representedOrganization/n1:name"/>
          </xsl:otherwise>
        </xsl:choose>
      </td>
    </tr>

  </xsl:template>

  <!--  Encounter row entry  -->
  <xsl:template name="encRow">
    <xsl:param name="row" />
    <tr class="second">
      
      <!--  Encounter Date/Time -->
      <td>
        <div style="overflow:hidden; white-space:nowrap; width:auto;">
          <xsl:choose>
            <xsl:when test="$row/n1:encounter/n1:effectiveTime/n1:low/@value">
              <xsl:call-template name="formatDate">
                <xsl:with-param name="date" select="$row/n1:encounter/n1:effectiveTime/n1:low/@value" />
              </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
              <xsl:text>--</xsl:text>
            </xsl:otherwise>
          </xsl:choose>
        </div>
      </td>
      
      <!--  Encounter Type   -->
      <td>
        <div style="overflow:hidden; white-space:nowrap; width:auto;">
          <xsl:value-of select="$row/n1:encounter/n1:code/@code"/>
          <xsl:if test="string-length($row/n1:encounter/n1:code/@code)=0">
            <xsl:text>--</xsl:text>
          </xsl:if>
        </div>
      </td>
      
      <!--  Encounter Free Text Type -->
      <td>
        <div style="overflow:hidden; white-space:nowrap; width:360px;">
          <xsl:variable name="encRef" select="substring-after($row/n1:encounter/n1:code/n1:originalText/n1:reference/@value,'#')" />
          <xsl:variable name="encFreeText" select="../n1:text/n1:content[@ID=$encRef]" />
          <xsl:call-template name="flyoverSpan">
            <xsl:with-param name="data" select="$encFreeText"/>
          </xsl:call-template>
          <xsl:if test="string-length($encFreeText)=0">
            <xsl:text>--</xsl:text>
          </xsl:if>
        </div>
      </td>
      
      <!--  Encounter Provider -->
      <td>
        <div style="overflow:hidden; white-space:nowrap;">
          
          <xsl:variable name="first" select="$row/n1:encounter/n1:performer/n1:assignedEntity/n1:assignedPerson/n1:name/n1:given" />
          <xsl:variable name="last" select="$row/n1:encounter/n1:performer/n1:assignedEntity/n1:assignedPerson/n1:name/n1:family" />

          <xsl:choose>
            <xsl:when test="string-length($first)!=0 and string-length($last)!=0">
              <xsl:value-of select="$first" />
              <xsl:text> </xsl:text>
              <xsl:value-of select="$last" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$row/n1:encounter/n1:performer/n1:assignedEntity/n1:assignedPerson" />
              <xsl:if test="string-length($row/n1:encounter/n1:performer/n1:assignedEntity/n1:assignedPerson)=0">
                <xsl:text>--</xsl:text>
              </xsl:if>
            </xsl:otherwise>
          </xsl:choose>
        </div>
      </td>
    </tr>
  </xsl:template>

  <!--  Procedures row entry -->
  <xsl:template name="procedureRow">
    <xsl:param name="row" />
    <tr class="second">
      <!--  Procedure Date/Time -->
      <td>
        <div style="overflow:hidden; white-space:nowrap;">
          <xsl:choose>
            <xsl:when test="$row/n1:procedure/n1:effectiveTime/n1:low/@value">
              <xsl:call-template name="formatDate">
                <xsl:with-param name="date" select="$row/n1:procedure/n1:effectiveTime/n1:low/@value" />
              </xsl:call-template>
            </xsl:when>
            <xsl:otherwise />
          </xsl:choose>
        </div>
      </td>
      <!--  Procedure Type  -->
      <td>
        <div style="overflow:hidden; white-space:nowrap; width:260px;">
            <xsl:variable name="displayName" select="$row/n1:procedure/n1:code/@displayName" />
            <xsl:variable name="code" select="$row/n1:procedure/n1:code/@code" />
             <xsl:choose>
               <xsl:when test="contains($displayName,$code)">
                <xsl:value-of select="substring-after($displayName,$code)" />
              </xsl:when>
               <xsl:otherwise>
                <xsl:value-of select="$row/n1:procedure/n1:code/@displayName" />
              </xsl:otherwise>
            </xsl:choose>            
        </div>
      </td>
      <!--  Procedure Free Text Type -->
      <td>
        <div style="overflow:hidden; white-space:nowrap; width:260px;">
          <xsl:variable name="procRef" select="substring-after($row/n1:procedure/n1:code/n1:originalText/n1:reference/@value,'#')" />
          <xsl:call-template name="flyoverSpan">
            <xsl:with-param name="data" select="../n1:text/n1:content[@ID=$procRef]" />
          </xsl:call-template>
          
          <xsl:variable name="procRef2" select="substring-after($row/n1:procedure/n1:text/n1:reference/@value,'#')" />
          <xsl:call-template name="flyoverSpan">
            <xsl:with-param name="data" select="../n1:text/n1:content[@ID=$procRef2]" />
          </xsl:call-template>

        </div>
      </td>
      <!--  Procedure Provider -->
      <td>
        <div style="overflow:hidden; white-space:nowrap;">
          <xsl:choose>
            <xsl:when test="$row/n1:procedure/n1:performer/n1:assignedEntity/n1:assignedPerson/n1:name/n1:family and $row/n1:procedure/n1:performer/n1:assignedEntity/n1:assignedPerson/n1:name/n1:given">
              <xsl:value-of select="$row/n1:procedure/n1:performer/n1:assignedEntity/n1:assignedPerson/n1:name/n1:given" />
              <xsl:text> </xsl:text>
              <xsl:value-of select="$row/n1:procedure/n1:performer/n1:assignedEntity/n1:assignedPerson/n1:name/n1:family" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$row/n1:procedure/n1:performer/n1:assignedEntity/n1:assignedPerson" />
              <xsl:if test="string-length($row/n1:procedure/n1:performer/n1:assignedEntity/n1:assignedPerson)=0">
                <xsl:text>--</xsl:text>
              </xsl:if>
            </xsl:otherwise>
          </xsl:choose>          
          
        </div>
      </td>
    </tr>
  </xsl:template>

  <!--   flyover -->
  <xsl:template name="flyoverSpan">
    <xsl:param name="data"/>
    <span onmouseover='DisplayTip(this,25,-50)'>
      <xsl:value-of select="$data"/>
    </span>
  </xsl:template>

  <!--   Title  -->
  <xsl:template match="n1:title">

    <h3>
      <span style="font-weight:bold;">
        <a name="{generate-id(.)}" href="#toc">
          <xsl:value-of select="."/>
        </a>
      </span>
    </h3>

  </xsl:template>

  <!--   Text   -->
  <xsl:template match="n1:text">
    <xsl:apply-templates />
  </xsl:template>

  <!--   paragraph  -->
  <xsl:template match="n1:paragraph">
    <p>
      <xsl:apply-templates/>
    </p>
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

  <xsl:template match="n1:table">
    <table>

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
      <b>
        <xsl:text>Electronically generated by: </xsl:text>
      </b>
      <xsl:call-template name="getName">
        <xsl:with-param name="name"
        select="/n1:ClinicalDocument/n1:legalAuthenticator/n1:assignedEntity/n1:representedOrganization/n1:name"/>
      </xsl:call-template>
      <xsl:text> on </xsl:text>
      <xsl:choose>
        <xsl:when test="string-length(/n1:ClinicalDocument/n1:effectiveTime/@value)=0">
          <xsl:text>Not Available</xsl:text>
        </xsl:when>
        <xsl:when test="starts-with(/n1:ClinicalDocument/n1:effectiveTime/@value,' ')">
          <xsl:text>Not Available</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="formatDateFull">
            <xsl:with-param name="date" select="/n1:ClinicalDocument/n1:effectiveTime/@value"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>

    </p>
  </xsl:template>

  <xsl:template match="n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.11']/n1:text/n1:table/n1:tbody">
    <xsl:apply-templates>
      <xsl:sort select="n1:td[3]" order="descending"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.8']/n1:text/n1:table/n1:tbody">
    <xsl:apply-templates>
      <xsl:sort select="n1:td[5]" order="descending"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.16' or n1:templateId/@root='2.16.840.1.113883.10.20.1.14' or n1:templateId/@root='2.16.840.1.113883.10.20.1.6' or n1:templateId/@root='2.16.840.1.113883.10.20.1.3']/n1:text/n1:table/n1:tbody">
    <xsl:apply-templates>
      <xsl:sort select="n1:td[2]" order="descending"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.11']/n1:text/n1:table/n1:tbody/n1:tr/n1:td[3]">
    <td>
      <xsl:call-template name="formatDate">
        <xsl:with-param name="date"
 select="text()"/>
      </xsl:call-template>
    </td>
  </xsl:template>

  <xsl:template match="n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.8']/n1:text/n1:table/n1:tbody/n1:tr/n1:td[5]">
    <td>
      <xsl:call-template name="formatDate">
        <xsl:with-param name="date"
   select="text()"/>
      </xsl:call-template>
    </td>
  </xsl:template>

  <xsl:template match="n1:component/n1:section[n1:templateId/@root='2.16.840.1.113883.10.20.1.16' or n1:templateId/@root='2.16.840.1.113883.10.20.1.14' or n1:templateId/@root='2.16.840.1.113883.10.20.1.6' or n1:templateId/@root='2.16.840.1.113883.10.20.1.3']/n1:text/n1:table/n1:tbody/n1:tr/n1:td[2]">
    <td>
      <xsl:call-template name="formatDate">
        <xsl:with-param name="date"
   select="concat(substring(text(),1,4),substring(text(),6,2),substring(text(),9,2))"/>
      </xsl:call-template>
    </td>
  </xsl:template>

  <xsl:template match="n1:languageCommunication">
    <xsl:variable name="langCode" select="substring(n1:languageCode/@code,1,2)"/>
    <xsl:choose>
      <xsl:when test="string-length($langCode)=0">
      </xsl:when>
      <xsl:when test="$langCode='en'">
        <li>
          <xsl:text>English</xsl:text>
        </li>
      </xsl:when>
      <xsl:when test="$langCode='es'">
        <li>
          <xsl:text>Spanish</xsl:text>
        </li>
      </xsl:when>
      <xsl:otherwise>
        <li>
          <xsl:value-of select="n1:languageCode/@code"/>
        </li>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="documentTitle">
    <xsl:param name="root"/>

    <xsl:choose>
      <xsl:when test="$root/n1:custodian/n1:assignedCustodian/n1:representedCustodianOrganization/n1:name and string-length($root/n1:custodian/n1:assignedCustodian/n1:representedCustodianOrganization/n1:name)>0">
        <xsl:value-of select="$root/n1:custodian/n1:assignedCustodian/n1:representedCustodianOrganization/n1:name"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$root/n1:author[1]/n1:assignedAuthor/n1:representedOrganization/n1:name"/>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <xsl:template name="displayComments">
    <xsl:param name="section" />
    <xsl:variable name="commentReference" select="$section/n1:entry/n1:act/n1:text/n1:reference/@value"/>
    <div style="font-style: italic;">
      <xsl:value-of select="$section/n1:text/n1:content[@ID=$commentReference]" />
    </div>
  </xsl:template>


</xsl:stylesheet>