package org.rm3l.servicenamesportnumbers.parsers.impl

import org.rm3l.servicenamesportnumbers.domain.Protocol
import org.rm3l.servicenamesportnumbers.domain.Record
import org.rm3l.servicenamesportnumbers.parsers.ServiceNamesPortNumbersMappingParser

const val NMAP_SERVICES_DB_URL = "https://svn.nmap.org/nmap/nmap-services"

/**
 * Parser for the Nmap Service Database,
 * located at https://svn.nmap.org/nmap/nmap-services
 */
class NmapServicesParser: ServiceNamesPortNumbersMappingParser {

    override fun parse(content: String): List<Record> {

        val records = mutableListOf<Record>()

        content.lines()
                .filterNot { it.startsWith("#") }
                .filterNot { it.isBlank() }
                .forEach { line ->
                    val lineComponents = line
                            .split("\t")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                    val serviceName = lineComponents[0]
                    val portNumberAndProtocol = lineComponents[1].split("/")
                    val portNumber = portNumberAndProtocol[0].trim().toLong()
                    val protocol = Protocol.valueOf(portNumberAndProtocol[1].trim().toUpperCase())
                    val description = if (lineComponents.size >= 4) lineComponents[3].substring("# ".length) else null
                    val record = Record(
                            serviceName = serviceName,
                            portNumber = portNumber,
                            transportProtocol = protocol,
                            description = description
                    )
                    records.add(record)
                }

        return records.toList()
    }
}