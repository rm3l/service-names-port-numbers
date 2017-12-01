package org.rm3l.iana.servicenamesportnumbers.app.resolvers

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import org.rm3l.iana.servicenamesportnumbers.IANAServiceNamesPortNumbersClient
import org.rm3l.iana.servicenamesportnumbers.domain.Record
import org.rm3l.iana.servicenamesportnumbers.domain.RecordFilter

class Query(private val registryClient: IANAServiceNamesPortNumbersClient) : GraphQLQueryResolver {

    fun records(filter: RecordFilter?): List<Record> {
        val fullListOfRecords: List<Record> = registryClient.query(filter)
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