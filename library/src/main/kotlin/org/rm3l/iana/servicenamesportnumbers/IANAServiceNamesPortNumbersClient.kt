package org.rm3l.iana.servicenamesportnumbers

import org.rm3l.iana.servicenamesportnumbers.domain.Protocol
import org.rm3l.iana.servicenamesportnumbers.domain.Record
import org.rm3l.iana.servicenamesportnumbers.domain.RecordFilter
import org.rm3l.iana.servicenamesportnumbers.internal.ServiceNamePortNumberRegistryCache
import org.rm3l.iana.servicenamesportnumbers.parsers.Format

class IANAServiceNamesPortNumbersClient private constructor(
        private val format: Format
) {

    fun refreshCache() = ServiceNamePortNumberRegistryCache.INSTANCE.refresh(this.format)

    fun invalidateCache() = ServiceNamePortNumberRegistryCache.INSTANCE.invalidate(this.format)

    fun query(filter: RecordFilter?): List<Record> {
        val fullListOfRecords: List<Record> = ServiceNamePortNumberRegistryCache
                .INSTANCE.get(this.format) ?: throw IllegalStateException("Failed to fetch XML content")
        return if (filter == null) {
            fullListOfRecords
        } else {
            fullListOfRecords
                    .filter { filter.ports == null || filter.ports.isEmpty() || filter.ports.contains(it.portNumber) }
                    .filter { filter.protocols == null || filter.protocols.isEmpty() || filter.protocols.contains(it.transportProtocol) }
                    .filter { filter.services == null || filter.services.isEmpty() || filter.services.contains(it.serviceName) }
        }
    }

    fun query(vararg ports: Long): List<Record> =
            this.query(RecordFilter(ports = ports.toList()))

    fun query(vararg serviceNames: String): List<Record> =
            this.query(RecordFilter(services = serviceNames.toList()))

    fun query(vararg protocols: Protocol): List<Record> =
            this.query(RecordFilter(protocols = protocols.toList()))

    companion object {

        @JvmStatic
        fun builder() = IANAServiceNamesPortNumbersClientBuilder()
    }

    class IANAServiceNamesPortNumbersClientBuilder {

        //TODO Add any other relevant fields here
        private var format: Format? = null

        @Deprecated("Format has an internal meaning, and should not be exposed to the user")
        private fun withFormat(format: Format): IANAServiceNamesPortNumbersClientBuilder {
            this.format = format
            return this
        }

        fun build() = IANAServiceNamesPortNumbersClient(this.format ?: Format.XML)
    }
}