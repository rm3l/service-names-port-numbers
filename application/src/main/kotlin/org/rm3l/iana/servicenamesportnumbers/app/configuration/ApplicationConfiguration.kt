package org.rm3l.iana.servicenamesportnumbers.app.configuration

import org.rm3l.iana.servicenamesportnumbers.IANAServiceNamesPortNumbersClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationConfiguration {

    @Bean(initMethod = "refreshCache", destroyMethod = "invalidateCache")
    fun xmlRegistryClient() = IANAServiceNamesPortNumbersClient
            .builder()
            .build()
}
