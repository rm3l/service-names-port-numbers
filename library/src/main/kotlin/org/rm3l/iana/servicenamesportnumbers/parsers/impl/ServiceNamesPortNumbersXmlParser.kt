@file:JvmName("ServiceNamesPortNumbersXmlParser")
package org.rm3l.iana.servicenamesportnumbers.parsers.impl

import org.rm3l.iana.servicenamesportnumbers.domain.Protocol
import org.rm3l.iana.servicenamesportnumbers.domain.Record
import org.rm3l.iana.servicenamesportnumbers.parsers.ServiceNamesPortNumbersMappingParser
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Parser for the XML Database of the IANA Registry,
 * located at www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.xml
 */
class ServiceNamesPortNumbersXmlParser : ServiceNamesPortNumbersMappingParser {

    override fun parse(content: String): List<Record> {
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val documentParsed = dBuilder.parse(content.byteInputStream())
        documentParsed.documentElement.normalize()

        val records = mutableListOf<Record>()

        val recordNodeList = documentParsed.getElementsByTagName("record")
        for (i in 0 until recordNodeList.length) {
            val recordNode = recordNodeList.item(i)
            if (recordNode.nodeType == Node.ELEMENT_NODE) {
                val element = recordNode as Element
                val serviceName = element.getElementsByTagName("name")?.item(0)?.textContent
                val protocolLowerCase = element.getElementsByTagName("protocol")?.item(0)?.textContent
                val description = element.getElementsByTagName("description")?.item(0)?.textContent
                val note = element.getElementsByTagName("note")?.item(0)?.textContent
                val portNumber = element.getElementsByTagName("number")?.item(0)?.textContent?.toLongOrNull()
                val record = Record(serviceName = serviceName,
                        portNumber = portNumber,
                        transportProtocol = if (protocolLowerCase != null) Protocol.valueOf(protocolLowerCase.toUpperCase()) else null,
                        description = description,
                        assignmentNotes = note)
                if (note != null && note.contains("should not be used for discovery purposes", ignoreCase = true)) {
                    continue
                }
                records.add(record)
            }
        }

        return records.toList()
    }
}
