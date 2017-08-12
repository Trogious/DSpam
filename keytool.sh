export CLASSPATH=.:~/dspam/ssl/bcprov-jdk16-146.jar:~/dspam/ssl/bcprov-ext-jdk16-1.46.jar
keytool -list -keystore mystore.bks -provider org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath /root/dspam/ssl/bcprov-jdk16-146.jar -storetype BKS -storepass dupa.12
keytool -import -alias swmud -file client.pem -keystore mystore.bks -provider org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath /root/dspam/ssl/bcprov-jdk16-146.jar -storetype BKS -storepass dupa.12
#-storepass mysecret

openssl pkcs12 -export -in client.pem -inkey client.key -out keystore.p12 -name swmud.net -CAfile ca.pem
keytool -importkeystore -deststorepass dupa.12 -destkeypass dupa.12 -destkeystore mystore.bks -deststoretype BKS -srckeystore keystore.p12 -srcstoretype PKCS12 -alias swmud.net

keytool -import -deststorepass dupa.12 -destkeypass dupa.12 -destkeystore truststore.bks -deststoretype BKS -alias swmudCA -file ca.pem

