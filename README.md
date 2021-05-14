# About this repository

Demo application #1 uses Redis core data structures, Streams, RediSearch and TimeSeries to build a
Java/Spring Boot/Spring Data Redis Reactive application that shows a searchable transaction overview with realtime updates
as well as in investments overview with realtime stock updates. UI in Bootstrap/CSS/Vue.

Demo application #2 does not use Redis, instead it uses an RDBMS. It's a text based (Lanterna) based application
that simulates an aging trading desk application. The application itself will communicate exclusively with the database.
Via RedisCDC these changes will appear in demo application #1.

# Getting Started

1. Checkout the project
2. `docker run -p 6379:6379 redislabs/redismod:latest`
3. `./mvnw clean package spring-boot:run`
4. Navigate to http://localhost:8080 and login with user lars and password larsje
5. Watch in awe (or horror ;))

WIP

# Demo

Demo shows 

