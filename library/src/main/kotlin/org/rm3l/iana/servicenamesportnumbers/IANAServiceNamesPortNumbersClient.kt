package org.rm3l.iana.servicenamesportnumbers

import com.github.benmanes.caffeine.cache.Caffeine
import org.rm3l.iana.servicenamesportnumbers.domain.Protocol
import org.rm3l.iana.servicenamesportnumbers.domain.Record
import org.rm3l.iana.servicenamesportnumbers.domain.RecordFilter
import org.rm3l.iana.servicenamesportnumbers.parsers.ServiceNamesPortNumbersMappingParser
import org.rm3l.iana.servicenamesportnumbers.parsers.impl.ServiceNamesPortNumbersXmlParser
import java.net.URL
import java.util.concurrent.TimeUnit

private const val DEFAULT_DB_URL =
        "https://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.xml"
private const val DEFAULT_CACHE_SIZE = 10L
private const val DEFAULT_CACHE_EXPIRATION_DAYS = 1L

@Suppress("unused")
class IANAServiceNamesPortNumbersClient private constructor(
        private val cacheMaximumSize: Long,
        private val cacheExpiration: Pair<Long, TimeUnit>,
        private val database: URL,
        private val parser: ServiceNamesPortNumbersMappingParser
) {

    private val databaseToParserPair = this.database to this.parser

    private val cache = Caffeine
            .newBuilder()
            .recordStats()
            .maximumSize(this.cacheMaximumSize)
            .expireAfterWrite(this.cacheExpiration.first, this.cacheExpiration.second)
            .expireAfterAccess(this.cacheExpiration.first, this.cacheExpiration.second)
            .removalListener<Pair<URL, ServiceNamesPortNumbersMappingParser>, List<Record>> { key, _, cause ->
                println("Key $key was removed from cache : $cause")
            }
            .build<Pair<URL, ServiceNamesPortNumbersMappingParser>, List<Record>> { urlParserPair ->
                println("Loading data from '${urlParserPair.first}' ...")
                urlParserPair.second.parse(urlParserPair.first.readText())
            }

    fun refreshCache() = this.cache.refresh(this.databaseToParserPair)

    fun invalidateCache() = this.cache.invalidate(this.databaseToParserPair)

    fun query(filter: RecordFilter?): List<Record> {
        val fullListOfRecords: List<Record> = this.cache.get(this.databaseToParserPair) ?:
                throw IllegalStateException("Failed to fetch content from $database")
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
        fun builder() = Builder()
    }

    @Suppress("unused")
    class Builder {

        private var cacheMaximumSize: Long? = null

        private var cacheExpiration: Pair<Long, TimeUnit>? = null

        private var database: URL? = null

        private var parser: ServiceNamesPortNumbersMappingParser? = null

        fun cacheMaximumSize(size: Long): Builder {
            this.cacheMaximumSize = size
            return this
        }

        fun cacheExpiration(duration: Long, timeUnit: TimeUnit): Builder {
            this.cacheExpiration = duration to timeUnit
            return this
        }

        fun database(database: URL): Builder {
            this.database = database
            return this
        }

        fun <T : ServiceNamesPortNumbersMappingParser> parser(parser: T): Builder {
            this.parser = parser
            return this
        }

        fun build() = IANAServiceNamesPortNumbersClient(
                cacheMaximumSize = this.cacheMaximumSize ?: DEFAULT_CACHE_SIZE,
                cacheExpiration = this.cacheExpiration ?: DEFAULT_CACHE_EXPIRATION_DAYS to TimeUnit.DAYS,
                database = this.database ?: URL(DEFAULT_DB_URL),
                parser = this.parser ?: ServiceNamesPortNumbersXmlParser())
    }


}