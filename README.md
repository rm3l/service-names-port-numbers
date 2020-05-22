# Service Names And Port Numbers Lookup

[![Bintray](https://img.shields.io/bintray/v/rm3l/maven/org.rm3l:service-names-port-numbers.svg)](https://bintray.com/rm3l/maven/org.rm3l%3Aservice-names-port-numbers) 
[![License](https://img.shields.io/badge/license-MIT-green.svg?style=flat)](https://github.com/rm3l/service-names-port-numbers/blob/master/LICENSE) 

[![Build Workflow](https://github.com/rm3l/service-names-port-numbers/workflows/CI/badge.svg)](https://github.com/rm3l/service-names-port-numbers/actions?query=workflow%3A%22CI%22)

[![Heroku](https://img.shields.io/badge/heroku-deployed%20on%20free%20dyno-blue.svg)](https://service-names-port-numbers.herokuapp.com/graphiql)

[![Docker Stars](https://img.shields.io/docker/stars/rm3l/service-names-port-numbers.svg)](https://hub.docker.com/r/rm3l/service-names-port-numbers)
[![Docker Pulls](https://img.shields.io/docker/pulls/rm3l/service-names-port-numbers.svg)](https://hub.docker.com/r/rm3l/service-names-port-numbers)

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [Usage](#usage)
  - [Live Server](#live-server)
  - [Using the library](#using-the-library)
    - [Adding the dependency](#adding-the-dependency)
      - [Maven](#maven)
      - [Gradle](#gradle)
    - [Usage](#usage-1)
  - [Using the server](#using-the-server)
    - [Downloading and building](#downloading-and-building)
    - [Querying the GraphQL API](#querying-the-graphql-api)
    - [Docker](#docker)
- [In use in the following apps/services](#in-use-in-the-following-appsservices)
- [Developed by](#developed-by)
- [License](#license)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

Library and microservice for looking up inside the IANA Service Names And Port Numbers Registry records. 
Written in [Kotlin](https://kotlinlang.org).
It supports registering various datasources (IANA, Nmap Services, ...) 
and allows to lookup service names from port numbers, or vice-versa.

This library is an in-memory database that allows to lookup IANA records based upon certain filters (e.g., 
service names, ports, transport protocols, ...).
It is very lightweight, and allows for million of lookups per second.

By default, the database is automatically fetched from 
[here](https://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.xml). 
But you can optionally provide your own database and parser by specifying a file or a URL where the data can be downloaded from. 
The server supports non-interrupting updates and can update the database while it is running.

Service names and port numbers are used to distinguish between different services that run over transport protocols 
such as TCP, UDP, DCCP, and SCTP.

And [GraphQL](http://graphql.org) is a data query language allowing clients to define the structure of the data required,
and exactly the same structure of the data is returned from the server. It is a strongly typed runtime which allows 
clients to dictate what data is needed.

* Live Server on Heroku PaaS: https://service-names-port-numbers.herokuapp.com/graphiql
* Download IANA Database: https://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.xhtml

## Usage

### Live Server

A live running version of the GraphQL API is available on Heroku: https://service-names-port-numbers.herokuapp.com/graphiql

Note that this API operates entirely from memory, and auto-updates itself every 12 hours.

Also, this runs on a free dyno, and as such it may go to sleep ater 30 minutes of inactivity.


### Using the library

The library is published on Bintray JCenter. So importing it should be straightforward.

#### Adding the dependency

##### Maven

```xml
<dependency>
  <groupId>org.rm3l</groupId>
  <artifactId>service-names-port-numbers-library</artifactId>
  <version>0.7.0</version>
</dependency>
```

##### Gradle

```groovy
compile 'org.rm3l:service-names-port-numbers-library:0.7.0'
```

#### Usage

This shows the basic usage of the library. Opening a database and querying.

Example with Kotlin:
```kotlin
import org.rm3l.servicenamesportnumbers.ServiceNamesPortNumbersClient
import org.rm3l.servicenamesportnumbers.domain.Record
import org.rm3l.servicenamesportnumbers.domain.RecordFilter
import org.rm3l.servicenamesportnumbers.domain.Protocol

fun main(args: Array<String>) {

    val serviceNamesPortNumbersClient = ServiceNamesPortNumbersClient
        .builder()
        //You may customize other parts here
        .build()
        
    var records = serviceNamesPortNumbersClient.query(443L) //records is a List<Record>
    //Do something with the records
    
    //To benefit from caching, it is recommended you reuse the client instance
    records = serviceNamesPortNumbersClient.query("http")
    
    //You may pass in complex filters
    records = serviceNamesPortNumbersClient.query(
                RecordFilter(
                    ports=listOf(80L, 443L, 2375L),
                    protocols=listOf(Protocol.TCP)))
    
    //Hot-update the database
    serviceNamesPortNumbersClient.updateDatabase(
                oldDatabase = File("/path/to/my/old/iana-database.xml"),
                newDatabase = File("/path/to/my/local/iana-database.xml"))
}
```

Example with Java:
```java
import org.rm3l.servicenamesportnumbers.ServiceNamesPortNumbersClient;
import org.rm3l.servicenamesportnumbers.domain.*;
import java.util.*;

public class MyService {
    
    public static void main(String... args) {

        final ServiceNamesPortNumbersClient serviceNamesPortNumbersClient = ServiceNamesPortNumbersClient
            .builder()
            //You may customize other parts here
            .build();
        
        List<Record> records = serviceNamesPortNumbersClient.query(443L);
        //Do something with the records
        
        //To benefit from caching, it is recommended you reuse the client instance
        records = serviceNamesPortNumbersClient.query("http");
        
        //You may pass in complex filters
        records = serviceNamesPortNumbersClient.query(
                    new RecordFilter(
                        null,
                        Collections.singletonList(Protocol.TCP),
                        Arrays.asList(80L, 443L, 2375L)));
        
        //Hot-update the database
        serviceNamesPortNumbersClient.updateDatabase(
                (new File("/path/to/my/old/iana-database.xml"),
                new File("/path/to/my/local/iana-database.xml"));
    }
}
```

### Using the server

#### Downloading and building
You can build the project with the `Gradle Wrapper`:

```bash
./gradlew build
```

You will then find the artifacts in the sub-projects `build` directories:

* `library/build/libs` : the library code
* `application/build/libs` : Spring-Boot sample application, representing the server app

Running the app is as simple as issuing the following command:

```bash
java -jar ./application/build/libs/service-names-port-numbers-app-0.7.0.jar
```

Then navigate to http://localhost:8080/graphiql to start exploring the GraphQL API.

Visit the `application/src/main/resources/application.properties` file 
to see which JVM options you can pass to the application. 
For example, to make the service listen on port `8888` instead, run:
```bash
java \
 -Dserver.port=8888 \
 -jar ./application/build/libs/service-names-port-numbers-app-0.7.0.jar
```

#### Querying the GraphQL API
Visit the http://localhost:8080/graphiql to start exploring the GraphQL schema and get completion hints with your queries.

Or send a `POST` request to the `/graphql` API endpoint to perform requests.
Example with `curl`:

```bash
curl -k -i -X POST http://localhost:8080/graphql \
  -H'Content-Type: application/json' \
  -d '{"query":"{records(filter: {ports: [80, 443, 2375], protocols: [TCP]}) {serviceName portNumber description assignmentNotes}}"}'

HTTP/1.1 200 
X-Application-Context: application
Content-Type: application/json;charset=UTF-8
Content-Length: 481
Date: Sun, 03 Dec 2017 22:22:11 GMT

{
 "data": {
   "records": [
     {
       "serviceName":"http",
       "portNumber":80,
       "description":"World Wide Web HTTP",
       "assignmentNotes":"Defined TXT keys: u=<username> p=<password> path=<path to document>"
     },
     {
       "serviceName":"https",
       "portNumber":443,
       "description":"http protocol over TLS/SSL",
       "assignmentNotes":null
     },
     {
       "serviceName":"docker",
       "portNumber":2375,
       "description":"Docker REST API (plain text)",
       "assignmentNotes":null
     }
   ]
 }
}

```

#### Docker

A Docker repository with the microservice can be found here: https://hub.docker.com/r/rm3l/service-names-port-numbers/

To fetch the docker image, run: 
```bash
docker pull rm3l/service-names-port-numbers
```

To run the server with the default options and expose it on ports 8080 (and port 8081, for the management endpoints), run:
```bash
docker run -p 8080:8080 -p 8081:8081 --rm rm3l/service-names-port-numbers
```

Then open http://localhost:8080/graphiql on your favorite browser, to start exploring the GraphQL API.


## In use in the following apps/services

(If you use this library and/or the microservice, please drop me a line at &lt;armel@rm3l.org&gt; 
(or again, fork, modify this file and submit a pull request), so I can list your app(s) here)

* [DD-WRT Companion](https://ddwrt-companion.app), to provide comprehensive insights about IP Connections
* [Androcker](https://play.google.com/store/apps/details?id=org.rm3l.container_companion), a companion app for Docker


## Developed by

* Armel Soro
  * [keybase.io/rm3l](https://keybase.io/rm3l)
  * [rm3l.org](https://rm3l.org) - &lt;armel@rm3l.org&gt; - [@rm3l](https://twitter.com/rm3l)
  * [paypal.me/rm3l](https://paypal.me/rm3l)
  * [coinbase.com/rm3l](https://www.coinbase.com/rm3l)


## License

    The MIT License (MIT)
    
    Copyright (c) 2017 Armel Soro
    
    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    
    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.

