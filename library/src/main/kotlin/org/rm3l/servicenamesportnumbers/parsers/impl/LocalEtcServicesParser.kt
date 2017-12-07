package org.rm3l.servicenamesportnumbers.parsers.impl

import org.rm3l.servicenamesportnumbers.domain.Protocol
import org.rm3l.servicenamesportnumbers.domain.Record
import org.rm3l.servicenamesportnumbers.parsers.ServiceNamesPortNumbersMappingParser
import java.io.File

/**
 * Parser for the Nmap Service Database,
 * located at /etc/services
 */
class LocalEtcServicesParser: ServiceNamesPortNumbersMappingParser {

    companion object {
        val ETC_SERVICES_FILE = File("/etc/services")
    }

    override fun parse(content: String): List<Record> {
        //Format: name port/protocol aliases comments
        val records = mutableListOf<Record>()

        content.lines()
                .filterNot { it.startsWith("#") }
                .filterNot { it.isBlank() }
                .forEach { line ->
                    val lineComponents = line
                            .split("\t")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                    var serviceName = lineComponents[0]
                    val portNumberAndProtocol: List<String>
                    if (serviceName.contains(" ")) { //Not supposed to contain whitespaces
                        val list = serviceName.split(" ").map { it.trim() }.filter { it.isNotBlank() }
                        serviceName = list[0]
                        portNumberAndProtocol = list[1].split("/")
                    } else {
                        portNumberAndProtocol = lineComponents[1].split("/")
                    }
                    val portNumber = portNumberAndProtocol[0].trim().toLong()
                    val protocol = Protocol.valueOf(portNumberAndProtocol[1].trim().toUpperCase())

                    var serviceAlias: String? = null
                    var description: String? = null
                    if (lineComponents.size >= 4) {
                        serviceAlias = lineComponents[2]
                        description = lineComponents[3].substring("# ".length)
                    } else if (lineComponents.size == 3) {
                        val lineItem = lineComponents[2]
                        if (lineItem.startsWith("#")) {
                            description = lineItem.substring("# ".length)
                        } else {
                            serviceAlias = lineItem
                        }
                    }

                    val record = Record(
                            serviceName = serviceName,
                            serviceAlias = serviceAlias,
                            portNumber = portNumber,
                            transportProtocol = protocol,
                            description = description
                    )
                    records.add(record)
                }

        return records.toList()


    }
}