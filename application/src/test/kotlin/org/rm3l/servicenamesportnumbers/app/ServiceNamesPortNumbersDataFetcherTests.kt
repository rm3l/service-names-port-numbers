/*
The MIT License (MIT)

Copyright (c) 2021 Armel Soro

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
package org.rm3l.servicenamesportnumbers.app

import com.jayway.jsonpath.TypeRef
import com.netflix.graphql.dgs.DgsQueryExecutor
import com.netflix.graphql.dgs.autoconfig.DgsAutoConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rm3l.servicenamesportnumbers.ServiceNamesPortNumbersClient
import org.rm3l.servicenamesportnumbers.app.graphql.fetchers.ServiceNamesPortNumbersDataFetcher
import org.rm3l.servicenamesportnumbers.app.graphql.scalars.LongScalarRegistration
import org.rm3l.servicenamesportnumbers.domain.Protocol
import org.rm3l.servicenamesportnumbers.domain.Record
import org.rm3l.servicenamesportnumbers.parsers.ServiceNamesPortNumbersMappingParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.test.context.ActiveProfiles
import java.nio.file.Files
import kotlin.io.path.createTempFile

@SpringBootTest(
        classes = [
          DgsAutoConfiguration::class,
          LongScalarRegistration::class,
          ServiceNamesPortNumbersDataFetcher::class,
          ServiceNamesPortNumberConfigurationForTests::class,
        ],
        properties = [
          "datasources.etc_services=false" //platform-dependent
        ]
)
@ActiveProfiles("test")
class ServiceNamesPortNumbersDataFetcherTests {

    @Autowired private lateinit var dgsQueryExecutor: DgsQueryExecutor

    @Test
    fun `test records`() {
      val serviceNames: List<String> =
        dgsQueryExecutor.executeAndExtractJsonPath(
          """
        {
          records(filter: {ports: [22, 2376]}) {
            serviceName
          }
        }
      """,
          "data.records[*].serviceName")
      assertEquals(2, serviceNames.size)
      assertTrue(serviceNames.contains("ssh"))
      assertTrue(serviceNames.contains("docker-secure"))
    }

    @Test
    fun `test record`() {
      val data: Record =
        dgsQueryExecutor.executeAndExtractJsonPathAsObject(
          """
          {
            record(serviceName: "https", portNumber: 443, transportProtocol: TCP) {
              portNumber
              transportProtocol
            }
          }
        """,
          "data.record",
          object: TypeRef<Record>() {})
      assertNotNull(data)
      assertEquals(443, data.portNumber)
      assertEquals(Protocol.TCP, data.transportProtocol)
    }

    @Test
    fun `test record no data`() {
      val executionResult =
        dgsQueryExecutor.execute(
          """
          {
            record(serviceName: "some-unknown-service", portNumber: 22, transportProtocol: UDP) {
              portNumber
              transportProtocol
            }
          }
        """)
      assertTrue(executionResult.isDataPresent)
      val data = executionResult.getData<Map<String, *>>()
      assertNotNull(data)
      assertNull(data["record"])
    }
}

@Configuration
@Profile("test")
class ServiceNamesPortNumberConfigurationForTests {

  @Bean(initMethod = "refreshCache", destroyMethod = "invalidateCache")
  @Primary
  fun registryClient() = ServiceNamesPortNumbersClient
    .builder(withIANADatabase = false)
    .addDatabaseAndParser(createTempFile().toFile(),
      object : ServiceNamesPortNumbersMappingParser {
        override fun parse(content: String) = listOf(
          Record(serviceName = "ssh", portNumber = 22, transportProtocol = Protocol.TCP),
          Record(serviceName = "https", portNumber = 443, transportProtocol = Protocol.TCP),
          Record(serviceName = "docker-secure", portNumber = 2376, transportProtocol = Protocol.TCP)
        )
      })
    .build()

}
