@echo off
setlocal ENABLEDELAYEDEXPANSION
for %%I IN (..\..\..\..\lib\postgresql-8.3-604.jdbc4.jar) DO SET CP=!CP!;%%I
for %%I IN (..\..\..\..\lib\ojdbc6.jar) DO SET CP=!CP!;%%I
for %%I IN (..\..\..\..\lib\mysql-connector-java-5.1.10-bin.jar) DO SET CP=!CP!;%%I
for %%I IN (..\..\..\..\lib\javaee.jar) DO SET CP=!CP!;%%I
for %%I IN (..\..\..\..\lib\axis2-1.5\log4j-1.2.15.jar) DO SET CP=!CP!;%%I
for %%I IN (..\..\..\..\lib\axis2-1.5\*.jar) DO SET CP=!CP!;%%I
for %%I IN (C:\glassfish\lib\appserv-rt.jar) DO SET CP=!CP!;%%I
for %%I IN (C:\glassfish\lib\appserv-admin.jar) DO SET CP=!CP!;%%I
for %%I IN (C:\glassfish\lib\install\applications\jmsra\imqjmsra.jar) DO SET CP=!CP!;%%I
for %%I IN (..\..\..\xutil\dist\*.jar) DO SET CP=!CP!;%%I
for %%I IN (..\..\dist\*.jar) DO SET CP=!CP!;%%I
 
set keystore=C:/glassfish/domains/hieos/config/keystore.jks
set truststore=C:/glassfish/domains/hieos/config/cacerts.jks
set keyrefs=-Djavax.net.ssl.keyStore=%keystore% -Djavax.net.ssl.keyStorePassword=changeit -Djavax.net.ssl.trustStore=%truststore% -Djavax.net.ssl.trustStorePassword=changeit
set ciphers=-Dhttps.cipherSuites=SSL_RSA_WITH_3DES_EDE_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA

java -cp %CP% %keyrefs% %ciphers% com.vangent.hieos.services.atna.arr.serviceimpl.ATNAServer -p atnaserver.properties
