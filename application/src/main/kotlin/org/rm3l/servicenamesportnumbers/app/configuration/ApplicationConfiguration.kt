/*
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
 */
package org.rm3l.servicenamesportnumbers.app.configuration

import org.rm3l.servicenamesportnumbers.ServiceNamesPortNumbersClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class ApplicationConfiguration {

    @Value("\${cache.maximum-size}")
    lateinit var cacheMaximumSize: String

    @Value("\${cache.expirationDays}")
    lateinit var cacheExpirationDays: String

    @Value("\${datasources.etc_services}")
    lateinit var localEtcServicesDatabase: String

    @Bean(initMethod = "refreshCache", destroyMethod = "invalidateCache")
    fun registryClient() = ServiceNamesPortNumbersClient
            .builder()
            .withIANADatabase()
            .withNmapServicesDatabase()
            .also { if (localEtcServicesDatabase.toBoolean()) it.withLocalEtcServicesDatabase() }
            .cacheMaximumSize(this.cacheMaximumSize.toLong())
            .cacheExpiration(this.cacheExpirationDays.toLong(), TimeUnit.DAYS)
            .build()
}
