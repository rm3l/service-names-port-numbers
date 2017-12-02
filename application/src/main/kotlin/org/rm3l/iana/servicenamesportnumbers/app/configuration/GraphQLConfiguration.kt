package org.rm3l.iana.servicenamesportnumbers.app.configuration

import com.coxautodev.graphql.tools.SchemaParser
import graphql.schema.GraphQLSchema
import org.rm3l.iana.servicenamesportnumbers.IANAServiceNamesPortNumbersClient
import org.rm3l.iana.servicenamesportnumbers.app.resolvers.Query
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import java.io.File

@Configuration
class GraphQLConfiguration(val registryClient: IANAServiceNamesPortNumbersClient) {

    @Bean
    fun graphQLSchema(): GraphQLSchema {
        val allSchemas = PathMatchingResourcePatternResolver()
                .getResources("/schema/**/*.graphql")
                .map { "schema${File.separator}${it.filename}" }
                .toList()
        return SchemaParser.newParser()
                .files(*allSchemas.toTypedArray())
                .resolvers(Query(registryClient))
                .build()
                .makeExecutableSchema()
    }

}