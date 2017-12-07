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
@file:Suppress("unused")

package org.rm3l.servicenamesportnumbers.app.resolvers

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import org.rm3l.servicenamesportnumbers.ServiceNamesPortNumbersClient
import org.rm3l.servicenamesportnumbers.domain.Protocol
import org.rm3l.servicenamesportnumbers.domain.Record
import org.rm3l.servicenamesportnumbers.domain.RecordFilter

class Query(private val registryClient: ServiceNamesPortNumbersClient) : GraphQLQueryResolver {

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