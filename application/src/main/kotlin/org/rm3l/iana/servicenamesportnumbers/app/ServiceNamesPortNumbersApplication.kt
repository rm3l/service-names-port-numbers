package org.rm3l.iana.servicenamesportnumbers.app

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class ServiceNamesPortNumbersApplication

fun main(args: Array<String>) {
    SpringApplication.run(ServiceNamesPortNumbersApplication::class.java, *args)
}
