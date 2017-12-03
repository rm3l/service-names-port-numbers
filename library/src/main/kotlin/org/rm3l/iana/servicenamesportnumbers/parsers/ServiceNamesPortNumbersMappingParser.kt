@file:JvmName("ServiceNamesPortNumbersMappingParser")
package org.rm3l.iana.servicenamesportnumbers.parsers

import org.rm3l.iana.servicenamesportnumbers.domain.Record

/**
 * IANA Database resource parser
 */
interface ServiceNamesPortNumbersMappingParser {

    /**
     * Parse the database [content]
     * @return the complete list of records
     */
    fun parse(content: String): List<Record>
}
