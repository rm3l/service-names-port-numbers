package org.rm3l.iana.servicenamesportnumbers.parsers

import org.rm3l.iana.servicenamesportnumbers.domain.Record

interface ServiceNamesPortNumbersMappingParser {

    fun parse(content: String): List<Record>
}