@echo off
setlocal ENABLEDELAYEDEXPANSION
for %%I IN (..\..\..\..\..\lib\axis2-1.5\*.jar) DO SET CP=!CP!;%%I
for %%I IN (..\..\..\..\..\lib\axis2-1.5\modules\addressing-1.5.*) DO SET CP=!CP!;%%I
for %%I IN (..\..\..\..\..\lib\hapi-0.6\*.jar) DO SET CP=!CP!;%%I
for %%I IN (..\..\..\..\..\lib\httpcore-nio\*.jar) DO SET CP=!CP!;%%I
for %%I IN (..\..\..\..\..\lib\axis2niossl-1.0.jar) DO SET CP=!CP!;%%I
for %%I IN (..\..\..\..\xutil\dist\*.jar) DO SET CP=!CP!;%%I
for %%I IN (..\..\..\dist\*.jar) DO SET CP=!CP!;%%I

set keystore=C:/glassfish/domains/hieos/config/keystore.jks
set truststore=C:/glassfish/domains/hieos/config/cacerts.jks
set keyrefs=-Djavax.net.ssl.keyStore=%keystore% -Djavax.net.ssl.keyStorePassword=changeit -Djavax.net.ssl.trustStore=%truststore% -Djavax.net.ssl.trustStorePassword=changeit
set ciphers=-Dhttps.cipherSuites=SSL_RSA_WITH_3DES_EDE_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA

java -cp %CP% %keyrefs% %ciphers% com.vangent.hieos.services.xds.registry.transactions.hl7v2.HL7ServerDaemon -p hl7server_secure.properties

rem java -cp %CP% com.vangent.hieos.services.xds.registry.transactions.hl7v2.HL7ServerDaemon -p hl7server_secure.properties
