@echo off
setlocal ENABLEDELAYEDEXPANSION
for %%I IN (..\..\..\..\..\lib\axis2-1.5\*.jar) DO SET CP=!CP!;%%I
for %%I IN (..\..\..\..\..\lib\axis2-1.5\modules\addressing-1.5.*) DO SET CP=!CP!;%%I
for %%I IN (..\..\..\..\..\lib\hapi-0.6\*.jar) DO SET CP=!CP!;%%I
for %%I IN (..\..\..\..\..\lib\httpcore-nio\*.jar) DO SET CP=!CP!;%%I
for %%I IN (..\..\..\..\xutil\dist\*.jar) DO SET CP=!CP!;%%I
for %%I IN (..\..\..\dist\*.jar) DO SET CP=!CP!;%%I
java -cp %CP% com.vangent.hieos.services.xds.registry.transactions.hl7v2.HL7ServerDaemon -p hl7server_secure.properties
