package org.rm3l.iana.servicenamesportnumbers

import com.github.benmanes.caffeine.cache.Caffeine
import org.rm3l.iana.servicenamesportnumbers.domain.Protocol
import org.rm3l.iana.servicenamesportnumbers.domain.Record
import org.rm3l.iana.servicenamesportnumbers.domain.RecordFilter
import org.rm3l.iana.servicenamesportnumbers.parsers.ServiceNamesPortNumbersMappingParser
import org.rm3l.iana.servicenamesportnumbers.parsers.impl.ServiceNamesPortNumbersXmlParser
import java.io.File
import java.net.URI
import java.net.URL
import java.util.concurrent.TimeUnit

private const val DEFAULT_DB_URL =
        "https://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.xml"
private const val DEFAULT_CACHE_SIZE = 10L
private const val DEFAULT_CACHE_EXPIRATION_DAYS = 1L

/**
 * Client against the IANA Service Names and Port Numbers registry.
 *
 * Use the [Builder] to create a given instance of the client.
 *
 * To benefit from records caching, we recommend you reuse the same client instance for querying.
 *
 * @constructor Creates the given client
 */
@Suppress("unused")
class IANAServiceNamesPortNumbersClient private constructor(
        private val cacheMaximumSize: Long,
        private val cacheExpiration: Pair<Long, TimeUnit>,
        private var database: URL,
        private var parser: ServiceNamesPortNumbersMappingParser
) {

    private var databaseToParserPair = this.database to this.parser

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

    private val recordsCache = Caffeine
            .newBuilder()
            .recordStats()
            .maximumSize(Math.max(this.cacheMaximumSize/2, 10L))
            .build<RecordFilter, List<Record>> { filter ->
                val fullRecords = this.cache.get(this.databaseToParserPair)
                if (filter.isEmpty()) {
                    fullRecords
                } else {
                    fullRecords
                            ?.
                                    filter { filter.ports == null || filter.ports.isEmpty() || filter.ports.contains(it.portNumber) }
                            ?.
                                    filter { filter.protocols == null || filter.protocols.isEmpty() || filter.protocols.contains(it.transportProtocol) }
                            ?.
                                    filter { filter.services == null || filter.services.isEmpty() || filter.services.contains(it.serviceName) }
                }
            }

    /**
     * Force a refresh of the internal cache
     */
    fun refreshCache() = this.cache.refresh(this.databaseToParserPair)

    /**
     * Force-invalidate the internal cache
     */
    fun invalidateCache() {
        this.cache.invalidateAll()
        this.recordsCache.invalidateAll()
    }

    /**
     * Update the internal database
     *
     * @param database the new database URL
     * @param parser the new parser to use. If null is specified, the existing parser will be used.
     * In this case, you need to make sure the new database is compatible with the existing parser
     */
    fun updateDatabase(database: URL, parser: ServiceNamesPortNumbersMappingParser? = null) {
        this.databaseToParserPair = database to (parser?:this.parser)
        this.recordsCache.invalidateAll()
        this.refreshCache()
    }

    /**
     * Update the internal database
     *
     * @param database the new database URL
     * @param parser the new parser to use. If null is specified, the existing parser will be used.
     * In this case, you need to make sure the new database is compatible with the existing parser
     */
    fun updateDatabase(database: URI, parser: ServiceNamesPortNumbersMappingParser? = null) =
            this.updateDatabase(database.toURL(), parser)

    /**
     * Update the internal database
     *
     * @param database the new database URL
     * @param parser the new parser to use. If null is specified, the existing parser will be used.
     * In this case, you need to make sure the new database is compatible with the existing parser
     */
    fun updateDatabase(database: File, parser: ServiceNamesPortNumbersMappingParser? = null) =
            this.updateDatabase(database.toURI(), parser)

    /**
     * Returns a current snapshot of this cache's cumulative statistics. All statistics are
     * initialized to zero, and are monotonically increasing over the lifetime of the cache.
     * <p>
     * Due to the performance penalty of maintaining statistics, usage history may not be recorded
     * immediately or at all.
     *
     * @return the current snapshot of the statistics of this cache
     */
    fun getCacheStats() = this.cache.stats()

    /**
     * Perform a lookup query against the IANA Service Names and Port Numbers registry, with the filter specified.
     *
     * @param filter the filter for querying. If the filter is null, then all records are returned.
     * Please note that this assumes the parser set to the client builder returns an appropriate set of [Record]s.
     */
    fun query(filter: RecordFilter?) = this.recordsCache.get(filter?:RecordFilter.EMPTY)!!

    /**
     * Perform a lookup query against the IANA Service Names and Port Numbers registry, with the ports specified.
     *
     * @param ports the ports to lookup
     * Please note that this assumes the parser set to the client builder returns an appropriate set of [Record]s.
     */
    fun query(vararg ports: Long) = this.query(RecordFilter(ports = ports.toList()))

    /**
     * Perform a lookup query against the IANA Service Names and Port Numbers registry, with the service names specified.
     *
     * @param serviceNames the services to lookup
     * Please note that this assumes the parser set to the client builder returns an appropriate set of [Record]s.
     */
    fun query(vararg serviceNames: String) = this.query(RecordFilter(services = serviceNames.toList()))

    /**
     * Perform a lookup query against the IANA Service Names and Port Numbers registry, with the transport protocols specified.
     *
     * @param protocols the Transport Protocols to lookup
     * Please note that this assumes the parser set to the client builder returns an appropriate set of [Record]s.
     */
    fun query(vararg protocols: Protocol) = this.query(RecordFilter(protocols = protocols.toList()))

    companion object {

        /**
         * Entry point for constructing a new instance of the [IANAServiceNamesPortNumbersClient]
         */
        @JvmStatic
        fun builder() = Builder()
    }

    /**
     * Builder class for [IANAServiceNamesPortNumbersClient].
     * Do not forget to call [build] when done initializing the Builder
     *
     * To benefit from records caching, we recommend you reuse the same client instance for querying.
     *
     * @constructor Creates a new [Builder] instance
     */
    @Suppress("unused")
    class Builder internal constructor() {

        private var cacheMaximumSize: Long? = null

        private var cacheExpiration: Pair<Long, TimeUnit>? = null

        private var database: URL? = null

        private var parser: ServiceNamesPortNumbersMappingParser? = null

        /**
         * Set the max size of the cache
         * @param size the new size
         */
        fun cacheMaximumSize(size: Long): Builder {
            this.cacheMaximumSize = size
            return this
        }

        /**
         * Set the cache expiration settings
         * @param duration the duration
         * @param timeUnit the time unit
         */
        fun cacheExpiration(duration: Long, timeUnit: TimeUnit): Builder {
            this.cacheExpiration = duration to timeUnit
            return this
        }

        /**
         * Set the database URL for lookup
         * @param database the database to set
         */
        fun database(database: URL): Builder {
            this.database = database
            return this
        }

        /**
         * Set the database URI for lookup
         * @param database the database to set
         */
        fun database(database: URI) = this.database(database.toURL())

        /**
         * Set the local database file for lookup
         * @param database the database to set
         */
        fun database(database: File) = this.database(database.toURI())

        /**
         * Set the database parser for lookup
         * @param T the dedicated type of parser
         * @param parser the parser to set
         */
        fun <T : ServiceNamesPortNumbersMappingParser> parser(parser: T): Builder {
            this.parser = parser
            return this
        }

        /**
         * Construct a new instance of [IANAServiceNamesPortNumbersClient],
         * with the parameters set beforehand, or with the default settings
         */
        fun build() = IANAServiceNamesPortNumbersClient(
                cacheMaximumSize = this.cacheMaximumSize ?: DEFAULT_CACHE_SIZE,
                cacheExpiration = this.cacheExpiration ?: DEFAULT_CACHE_EXPIRATION_DAYS to TimeUnit.DAYS,
                database = this.database ?: URL(DEFAULT_DB_URL),
                parser = this.parser ?: ServiceNamesPortNumbersXmlParser())
    }


}