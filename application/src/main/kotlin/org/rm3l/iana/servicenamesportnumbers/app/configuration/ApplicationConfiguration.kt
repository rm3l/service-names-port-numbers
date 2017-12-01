package org.rm3l.iana.servicenamesportnumbers.app.configuration

import org.rm3l.iana.servicenamesportnumbers.IANAServiceNamesPortNumbersClient
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

    @Bean(initMethod = "refreshCache", destroyMethod = "invalidateCache")
    fun xmlRegistryClient() = IANAServiceNamesPortNumbersClient
            .builder()
            .cacheMaximumSize(this.cacheMaximumSize.toLong())
            .cacheExpiration(this.cacheExpirationDays.toLong(), TimeUnit.DAYS)
            .build()
}
