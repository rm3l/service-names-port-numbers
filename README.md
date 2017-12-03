# IANA Service Names And Port Numbers Lookup

Library and microservice for looking up inside the IANA Service Names And Port Numbers Registry records. 
Written in [Kotlin](https://kotlinlang.org).
It allows to lookup service names from port numbers, or vice-versa.

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

* Download IANA Database: https://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.xhtml

## Usage

### Using the library

The library is published on Bintray JCenter. So importing it should be straightforward.

#### Adding the dependency

##### Maven

```xml
<dependency>
  <groupId>org.rm3l.iana</groupId>
  <artifactId>iana-service-names-port-numbers-client</artifactId>
  <version>0.1.3</version>
</dependency>
```

##### Gradle

```groovy
compile 'org.rm3l.iana:iana-service-names-port-numbers-client:0.1.3'
```

#### Usage

This shows the basic usage of the library. Opening a database and querying.

Example with Kotlin:
```kotlin
import org.rm3l.iana.servicenamesportnumbers.IANAServiceNamesPortNumbersClient
import org.rm3l.iana.servicenamesportnumbers.domain.Record
import org.rm3l.iana.servicenamesportnumbers.domain.RecordFilter
import org.rm3l.iana.servicenamesportnumbers.domain.Protocol

fun main(args: Array<String>) {

    val ianaClient = IANAServiceNamesPortNumbersClient
        .builder()
        //You may customize other parts here
        .build()
        
    var records = ianaClient.query(443L) //records is a List<Record>
    //Do something with the records
    
    //To benefit from caching, it is recommended you reuse the client instance
    records = ianaClient.query("http")
    
    //You may pass in complex filters
    records = ianaClient.query(
                RecordFilter(
                    ports=listOf(80L, 443L, 2375L),
                    protocols=listOf(Protocol.TCP)))
}
```

Example with Java:
```java
import org.rm3l.iana.servicenamesportnumbers.IANAServiceNamesPortNumbersClient;
import org.rm3l.iana.servicenamesportnumbers.domain.*;
import java.util.*;

public class MyService {
    
    public static void main(String... args) {

        final IANAServiceNamesPortNumbersClient ianaClient = IANAServiceNamesPortNumbersClient
            .builder()
            //You may customize other parts here
            .build();
        
        List<Record> records = ianaClient.query(443L);
        //Do something with the records
        
        //To benefit from caching, it is recommended you reuse the client instance
        records = ianaClient.query("http");
        
        //You may pass in complex filters
        records = ianaClient.query(
                    new RecordFilter(
                        null,
                        Collections.singletonList(Protocol.TCP),
                        Arrays.asList(80L, 443L, 2375L)));
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
java -jar ./application/build/libs/iana-service-names-port-numbers-app-0.1.3.jar
```

Then navigate to http://localhost:8080/graphiql to start exploring the GraphQL API.

Visit the `application/src/main/resources/application.properties` file 
to see which JVM options you can pass to the application. 
For example, to make the service listen on port `8888` instead, run:
```bash
java \
 -Dserver.port=8888 \
 -jar application/build/libs/application/build/libs/iana-service-names-port-numbers-app-0.1.3.jar
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

A Docker repository with the microservice can be found here: https://hub.docker.com/r/rm3l/iana-service-names-port-numbers/

To fetch the docker image, run: 
```bash
docker pull rm3l/iana-service-names-port-numbers
```

To run the server with the default options and expose it on ports 8080 (and port 8081, for the management endpoints), run:
```bash
docker run -p 8080:8080 -p 8081:8081 --rm rm3l/iana-service-names-port-numbers
```

Then open http://localhost:8080/graphiql on your favorite browser, to start exploring the GraphQL API.


## In use in the following apps/services

(If you use this library and/or the microservice, please drop me a line at &lt;armel@rm3l.org&gt; 
(or again, fork, modify this file and submit a pull request), so I can list your app(s) here)

* [DD-WRT Companion](http://ddwrt-companion.rm3l.org), to provide comprehensive insights about IP Connections
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

