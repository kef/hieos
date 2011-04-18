@echo off
setlocal ENABLEDELAYEDEXPANSION
for %%I IN (..\..\..\..\lib\axis2-1.5\log4j-1.2.15.jar) DO SET CP=!CP!;%%I
for %%I IN (..\..\..\..\lib\ojdbc6.jar) DO SET CP=!CP!;%%I
for %%I IN (..\..\..\xutil\dist\*.jar) DO SET CP=!CP!;%%I
for %%I IN (..\..\dist\*.jar) DO SET CP=!CP!;%%I
java -cp %CP% com.vangent.hieos.services.atna.arr.serviceimpl.ATNAServer -p atnaserver.properties
