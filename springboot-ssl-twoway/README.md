
# 2 way ssl with Spring Boot microservices

https://medium.com/@niral22/2-way-ssl-with-spring-boot-microservices-2c97c974e83


We will create 2 Spring Boot applications. Ideally, we can call it client and server, but just because we are using spring boot and as per microservice principle, it is better to have a gateway application fronting all your underlying microservices. We will refer to the client as gateway and the server as ms — for microservice obviously.

## Create A Self Signed Client Cert

`keytool -genkeypair -alias nt-gateway -keyalg RSA -keysize 2048 -storetype JKS -keystore nt-gateway.jks -validity 3650 -ext SAN=dns:localhost,ip:127.0.0.1`

## Create A Self Signed Server Cert

`keytool -genkeypair -alias nt-ms -keyalg RSA -keysize 2048 -storetype JKS -keystore nt-ms.jks -validity 3650 -ext SAN=dns:localhost,ip:127.0.0.1`


## Create public certificate file from client cert:

`keytool -export -alias nt-gateway -file nt-gateway.crt -keystore nt-gateway.jks`

Enter keystore password:

Certificate stored in file <nt-gateway.crt>


## Create Public Certificate File From Server Cert:

`keytool -export -alias nt-ms -file nt-ms.crt -keystore nt-ms.jks`

Enter keystore password:

Certificate stored in file <nt-ms.crt>



Now, we will have to import client’s cert to server’s keystore and server’s cert to client’s keystore file.

## Import Client Cert to Server jks File:

`keytool -import -alias nt-gateway -file nt-gateway.crt -keystore nt-ms.jks`

## Import Server Cert to Client jks File:

`keytool -import -alias nt-ms -file nt-ms.crt -keystore nt-gateway.jks`


## SpringBoot: Configure Server For 2 Way SSL:

See codes:

- Copy final server jks file (in my case, nt-ms.jks) to the src/main/resources/ folder of nt-ms application.

- Add the entries shown below in application.yml (or application.properties. But I prefer .yml)

see ms server application.yml

-Create a controller class with REST endpoint to serve the incoming request


## SpringBoot: Configure Client For 2 Way SSL:

Now, this requires some more changes than the server side as https communication is going to be initiated from here. But don’t worry. We’ll go step by step.

- First, copy final client jks (in my case nt-gateway.jks) to src/main/resources/ folder

- Next, add the entries shown below in application.yml

see gateway application.yml

- We will need to add the below dependency in our pom. Don’t worry, we will know the use of these in next step.

```
<dependency>
    <groupId>org.apache.httpcomponents</groupId>
    <artifactId>httpcore</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.httpcomponents</groupId>
    <artifactId>httpclient</artifactId>
</dependency>
<dependency>
```

see gateway-pom-dependency

- Luckily, Spring Boot comes with a cool RestTemplate class for http communication. We will use this class for our https call from the client application to the server. And because we are going with 2 way SSL, we need to configure this RestTemplate to use the trust store with server certificate.

see gatewy-resttemplate-https

- Once we’re done with that, let’s add a property in application.yml to tell the url for the server application endpoint to call.

```
endpoint:
  ms-service: https://localhost:9002/nt-ms/data
```

- After that, we will create a controller class with 2 methods for testing


## Configure from browser to Gateway!!!

But, there is still one problem. How do you test this? If you try to access the application with https://localhost url, your browser will complain about the client certificate being needed!!! Why? We’ve accessed all these https applications in the world so far without any problem. So, what’s so special about our application?  

Because it’s 2 way SSL. When we access gateway url in browser, our browser becomes the client to our gateway application and so, our gateway web app will ask the browser to present a cert for authentication.  

To overcome this, we will have to import a cert to our browser. But our browser can’t understand a .jks file. Instead, it understands PKCS12 format. So, how do we convert .jks file to PKCS12 format? Again, keytool command to the rescue!!

```
keytool -importkeystore -srckeystore nt-ms.jks -destkeystore nt-ms.p12 -srcstoretype JKS -deststoretype PKCS12 -srcstorepass nt-service -deststorepass nt-service -srcalias nt-ms -destalias nt-ms -srckeypass nt-service -destkeypass nt-service -noprompt
```


To import this .p12 file on mac, you will need to import this on login keychain.

Open keychain access
Click on login under “keychains” and “Certificates” under Category
Drag and drop the .p12 file here. It will prompt for the .p12 file password. Enter it and add.

Double click the cert you just uploaded and under “Trust” and select the “Always Trust” option. This will ask you for your login keychain password. Enter it and proceed.
Please close your browser windows, open and clear your cookies/cache and then hit https://localhost:9001/nt-gw/ms-data and you will be given a warning for “Connection not private” error.

Click “Show Details”

Click on “visit this website” and you will get below screen.

One final step. You will need to enter your mac login password.

And that’s it. You will be able to load the method. And if you check logs of your gateway and ms application, you will see appropriate debug entries.

-------

# 20190724: Successfully configured

## References:

- https://dzone.com/articles/hakky54mutual-tls-1
- https://www.baeldung.com/java-ssl-handshake-failures



##  self-signed: nt-gateway

keytool -genkeypair -alias nt-gateway -keyalg RSA -keysize 2048 -deststoretype pkcs12 -keystore C:\Temp\Transfer\nt-gateway.jks -validity 3650 -dname "CN=nt gateway,OU=Dev,O=oopsmails,ST=OO,C=CA" -ext "SAN=DNS:localhost,IP:127.0.0.1" -storepass nt-gateway -keypass nt-gateway

##  self-signed: nt-ms

keytool -genkeypair -alias nt-ms -keyalg RSA -keysize 2048 -deststoretype pkcs12 -keystore C:\Temp\Transfer\nt-ms.jks -validity 3650 -dname "CN=nt ms,OU=Dev,O=oopsmails,ST=TT,C=CA" -ext "SAN=DNS:localhost,IP:127.0.0.1" -storepass nt-service -keypass nt-service


##  export nt-gateway certificate

keytool -exportcert -alias nt-gateway -file C:\Temp\Transfer\nt-gateway.cer -keystore C:\Temp\Transfer\nt-gateway.jks -storepass nt-gateway


##  export nt-ms certificate

keytool -exportcert -alias nt-ms -file C:\Temp\Transfer\nt-ms.cer -keystore C:\Temp\Transfer\nt-ms.jks -storepass nt-service


##  import nt-gateway.cer into trust store of nt-ms, could use different jks file, e.g, nt-ms-truststore.jks

keytool -importcert -alias nt-gateway -file C:\Temp\Transfer\nt-gateway.cer -keystore C:\Temp\Transfer\nt-ms.jks -storepass nt-service


##  import nt-ms.cer into trust store of nt-gateway, could use different jks file, e.g, nt-gateway-truststore.jks

keytool -importcert -alias nt-ms -file C:\Temp\Transfer\nt-ms.cer -keystore C:\Temp\Transfer\nt-gateway.jks -storepass nt-gateway


##  Generate p12 for browser and import into Chrome (how toimport p12 certificate into chrome)

keytool -importkeystore -srckeystore C:\Temp\Transfer\nt-ms.jks -destkeystore C:\Temp\Transfer\nt-ms.p12 -srcstoretype JKS -srcstorepass nt-service -deststoretype PKCS12 -deststorepass nt-service -destkeypass nt-service


keytool -importkeystore -srckeystore C:\Temp\Transfer\nt-gateway.jks -destkeystore C:\Temp\Transfer\nt-ms.p12 -srcstoretype JKS -srcstorepass nt-gateway -deststoretype PKCS12 -deststorepass nt-service -destkeypass nt-service


https://localhost:9001/nt-gw/data
https://localhost:9001/nt-gw/ms-data
https://localhost:9002/nt-ms/data

## Debugging

- JVM args
```
-Djavax.net.ssl.keyStore=serverkeystore.jks \
    -Djavax.net.ssl.keyStorePassword=password \
    -Djavax.net.ssl.trustStore=servertruststore.jks \
    -Djavax.net.ssl.trustStorePassword=password
```

- list keystore
keytool -v -list -keystore serverkeystore.jks





-------


http://localhost:9001/

Bad Request
This combination of host and port requires TLS.


https://localhost:9001/



https://localhost:9001/nt-gw/ms-data  

-------



