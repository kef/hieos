@echo off
setlocal ENABLEDELAYEDEXPANSION
for %%I IN (..\..\..\xutil\dist\*.jar) DO SET CP=!CP!;%%I
for %%I IN (..\..\dist\*.jar) DO SET CP=!CP!;%%I
java -cp %CP% com.vangent.hieos.services.atna.arr.serviceimpl.ATNAClient -host localhost -port 3110 -protocol UDP -secure false
