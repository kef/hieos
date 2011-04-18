@echo off
setlocal ENABLEDELAYEDEXPANSION
for %%I IN (..\..\..\xutil\dist\*.jar) DO SET CP=!CP!;%%I
for %%I IN (..\..\dist\*.jar) DO SET CP=!CP!;%%I
 
set keystore=C:/sun/sdk/domains/domain1/config/keystore.jks
set truststore=C:/sun/sdk/domains/domain1/config/cacerts.jks
set keyrefs=-Djavax.net.ssl.keyStore=%keystore% -Djavax.net.ssl.keyStorePassword=changeit -Djavax.net.ssl.trustStore=%truststore% -Djavax.net.ssl.trustStorePassword=changeit
set ciphers=-Dhttps.cipherSuites=SSL_RSA_WITH_3DES_EDE_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA

java -cp %CP% %keyrefs% %ciphers% com.vangent.hieos.services.atna.arr.serviceimpl.ATNAClient -host localhost -port 3100 -protocol TCP -secure true
