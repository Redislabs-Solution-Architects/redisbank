# About this repository

Demo application #1 uses Redis core data structures, Streams, RediSearch and TimeSeries to build a
Java/Spring Boot/Spring Data Redis Reactive application that shows a searchable transaction overview with realtime updates
as well as in investments overview with realtime stock updates. UI in Bootstrap/CSS/Vue.

Demo application #2 does not use Redis, instead it uses an RDBMS. It's a text based (Lanterna) based application
that simulates an aging trading desk application. The application itself will communicate exclusively with the database.
Via RedisCDC these changes will appear in demo application #1.

# Getting Started

## Running locally

1. Checkout the project
2. `docker run -p 6379:6379 redislabs/redismod:latest`
3. `./mvnw clean package spring-boot:run`
4. Navigate to http://localhost:8080 and login with user lars and password larsje

## Running on Azure Spring Cloud

1. Follow the steps from 'Running locally'
2. Make sure you are logged into the Azure CLI
3. Add the Azure Spring Cloud extension to the Azure CLI `az extension add --name spring-cloud` If you already have the extension, make sure it's up to date using `az extension update --name spring-cloud`
2. Create an Azure Spring Cloud instance using `az spring-cloud create -n acrebank -g rdsLroACRE -l northeurope` (this may take a few minutes)
3. Create an App in the newly created Azure Spring Cloud instance using `az spring-cloud app create -n acrebankapp -s acrebank -g rdsLroACRE --assign-endpoint true --runtime-version Java_11`
4. Modify the application.properties so it points to your newly created ACRE instance

```
spring.redis.host=your ACRE hostname
spring.redis.port=your ACRE port (default: 10000)
spring.redis.password= your ACRE access key
```

5. Modify the application.properties so the websocket config will point to the Azure Spring Cloud app instance endpoint createed in step 3.

```
stomp.host=your ASC app endpoint URL (Default: <appname>-<service-name>.azuremicroservices.io)
stomp.port=443
stomp.protocol=wss
```

6. Rebuild the app using `./mvnw package`
7. Deploy the app to Azure Spring Cloud using `az spring-cloud app deploy -n acrebankapp -s acrebank -g rdsLroAcre --jar-path target/redisbank-0.0.1-SNAPSHOT.jar`

## Troubleshooting tips on Azure Spring Cloud

To get the application logs:

`az spring-cloud app logs -n acrebankapp -g rdsLroAcre -s acrebank`

Note: project is compiled with JDK11 as that's currently the max LTS version that's supported by Azure Spring Cloud. Project will run fine when running locally or on other platforms up to JDK16.