package org.rm3l.servicenamesportnumbers.app.graphql.fetchers

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import org.rm3l.servicenamesportnumbers.ServiceNamesPortNumbersClient
import org.rm3l.servicenamesportnumbers.domain.Protocol
import org.rm3l.servicenamesportnumbers.domain.Record
import org.rm3l.servicenamesportnumbers.domain.RecordFilter

@DgsComponent
class ServiceNamesPortNumbersDataFetcher(private val registryClient: ServiceNamesPortNumbersClient) {

  @DgsData(parentType = "Query", field = "record")
  fun record(serviceName: String?, transportProtocol: Protocol, portNumber: Long): Record? {
    val filter = RecordFilter(
      services = if (serviceName != null) listOf(serviceName) else emptyList(),
      protocols = listOf(transportProtocol),
      ports = listOf(portNumber))
    val fullListOfRecords = registryClient.query(filter)
    return when {
      fullListOfRecords.isEmpty() -> null
      else -> fullListOfRecords.first()
    }
  }

  @DgsData(parentType = "Query", field = "records")
  fun records(filter: RecordFilter?): List<Record> {
    val fullListOfRecords = registryClient.query(filter).toList()
    return if (filter == null) {
      fullListOfRecords
    } else {
      fullListOfRecords
        .filter {
          filter.ports == null
            || filter.ports!!.isEmpty()
            || filter.ports!!.contains(it.portNumber)
        }
        .filter {
          filter.protocols == null
            || filter.protocols!!.isEmpty()
            || filter.protocols!!.contains(it.transportProtocol)
        }
        .filter {
          filter.services == null
            || filter.services!!.isEmpty()
            || filter.services!!.contains(it.serviceName)
        }
    }
  }

}
