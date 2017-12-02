# IANA Service Names And Port Numbers Lookup

Library and microservice for looking up inside the IANA Service Names And Port Numbers, written in Kotlin.
It allows to lookup service names from port numbers, or vice-versa.

This library is a in-memory database that allows to look up IANA records based upon certain filters (e.g., 
service names, ports, transport protocols, ...).
The library is very lightweight, and allows for million of lookups per second. 
By default, the database is automatically fetched from 
[here](https://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.xml). 
But you can add the database by specifying a file or a URL where the data can be downloaded from. 
The server supports non-interrupting updates and can update the database while it is running.

Service names and port numbers are used to distinguish between different services that run over transport protocols 
such as TCP, UDP, DCCP, and SCTP.

## Usage

### Using the library
TODO

### Using the server

### Online Microservice
TODO

#### Downloading and building
TODO

#### Querying the GraphQL API
TODO

#### Docker

A Docker repositry with the microservice can be found here: https://hub.docker.com/r/rm3l/iana-service-names-port-numbers/

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

