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
package org.rm3l.servicenamesportnumbers

import com.github.benmanes.caffeine.cache.Caffeine
import org.rm3l.servicenamesportnumbers.domain.Protocol
import org.rm3l.servicenamesportnumbers.domain.Record
import org.rm3l.servicenamesportnumbers.domain.RecordFilter
import org.rm3l.servicenamesportnumbers.parsers.ServiceNamesPortNumbersMappingParser
import org.rm3l.servicenamesportnumbers.parsers.impl.IANAXmlServiceNamesPortNumbersParser
import org.rm3l.servicenamesportnumbers.parsers.impl.IANA_XML_DB_URL
import org.rm3l.servicenamesportnumbers.parsers.impl.NMAP_SERVICES_DB_URL
import org.rm3l.servicenamesportnumbers.parsers.impl.NmapServicesParser
import java.io.File
import java.net.URI
import java.net.URL
import java.util.concurrent.TimeUnit

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
class ServiceNamesPortNumbersClient private constructor(
        private val cacheMaximumSize: Long,
        private val cacheExpiration: Pair<Long, TimeUnit>,
        private val databaseAndParserMap: MutableMap<URL, ServiceNamesPortNumbersMappingParser> = mutableMapOf()) {

    private val cache = Caffeine
            .newBuilder()
            .recordStats()
            .maximumSize(this.cacheMaximumSize)
            .expireAfterWrite(this.cacheExpiration.first, this.cacheExpiration.second)
            .expireAfterAccess(this.cacheExpiration.first, this.cacheExpiration.second)
            .removalListener<Pair<URL, ServiceNamesPortNumbersMappingParser>, Set<Record>> { key, _, cause ->
                println("Key $key was removed from cache : $cause")
            }
            .build<Pair<URL, ServiceNamesPortNumbersMappingParser>, Set<Record>> { urlParserPair ->
                println("Loading data from '${urlParserPair.first}' ...")
                urlParserPair.second.parse(urlParserPair.first.readText()).toSet()
            }

    private val recordsCache = Caffeine
            .newBuilder()
            .recordStats()
            .maximumSize(Math.max(this.cacheMaximumSize/2, 10L))
            .build<RecordFilter, Collection<Record>> { filter ->
                val fullRecords = this.databaseAndParserMap
                        .flatMap { this.cache.get(it.toPair())?: emptySet() }
                        .toSet()
                if (filter.isEmpty()) {
                    fullRecords
                } else {
                    fullRecords
                            .filter { filter.ports == null || filter.ports.isEmpty() || filter.ports.contains(it.portNumber) }
                            .filter { filter.protocols == null || filter.protocols.isEmpty() || filter.protocols.contains(it.transportProtocol) }
                            .filter { filter.services == null || filter.services.isEmpty() || filter.services.contains(it.serviceName) }
                }
            }

    /**
     * Force a refresh of the internal cache
     */
    fun refreshCache() = this.databaseAndParserMap.forEach { this.cache.refresh(it.toPair()) }

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
     * @param oldDatabase the old database URL
     * @param newDatabase the new database URL
     * @param parser the new parser to use. If null is specified, the existing parser will be used.
     * In this case, you need to make sure the new database is compatible with the existing parser
     */
    @Suppress("MemberVisibilityCanPrivate")
    fun updateDatabase(oldDatabase: URL, newDatabase: URL, parser: ServiceNamesPortNumbersMappingParser? = null) {
        val existingParser = this.databaseAndParserMap.remove(oldDatabase)
        this.databaseAndParserMap[newDatabase] = parser?:existingParser?: IANA_XML_DB_PARSER
        this.recordsCache.invalidateAll()
        this.refreshCache()
    }

    /**
     * Update the internal database
     *
     * @param oldDatabase the old database URI
     * @param newDatabase the new database URI
     * @param parser the new parser to use. If null is specified, the existing parser will be used.
     * In this case, you need to make sure the new database is compatible with the existing parser
     */
    @Suppress("MemberVisibilityCanPrivate")
    fun updateDatabase(oldDatabase: URI, newDatabase: URI, parser: ServiceNamesPortNumbersMappingParser? = null) =
            this.updateDatabase(oldDatabase.toURL(), newDatabase.toURL(), parser)

    /**
     * Update the internal database
     *
     * @param oldDatabase the old database File
     * @param newDatabase the new database File
     * @param parser the new parser to use. If null is specified, the existing parser will be used.
     * In this case, you need to make sure the new database is compatible with the existing parser
     */
    fun updateDatabase(oldDatabase: File, newDatabase: File, parser: ServiceNamesPortNumbersMappingParser? = null) =
            this.updateDatabase(oldDatabase.toURI(), newDatabase.toURI(), parser)

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

        private val IANA_XML_DB_PARSER = IANAXmlServiceNamesPortNumbersParser()

        /**
         * Entry point for constructing a new instance of the [ServiceNamesPortNumbersClient].
         * Contains by default the IANA XML Database, but you are free to add other parsers as needed.
         */
        @JvmStatic
        fun builder() = Builder().withIANADatabase()
    }

    /**
     * Builder class for [ServiceNamesPortNumbersClient].
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

        private val databaseAndParserMap: MutableMap<URL?, ServiceNamesPortNumbersMappingParser?> = mutableMapOf()

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
         * Use the IANA Database
         */
        fun withIANADatabase() = this.addDatabaseAndParser(
                URL(IANA_XML_DB_URL),
                IANA_XML_DB_PARSER)

        /**
         * Use the IANA Database
         */
        fun withNmapServicesDatabase() = this.addDatabaseAndParser(
                URL(NMAP_SERVICES_DB_URL),
                NmapServicesParser())

        /**
         * Add a database URL to fetch, along with its parser
         * @param T the dedicated type of parser
         * @param database the database to set
         * @param parser the parser to use
         */
        fun <T : ServiceNamesPortNumbersMappingParser> addDatabaseAndParser(database: URL, parser: T? = null): Builder {
            this.databaseAndParserMap.put(database, parser)
            return this
        }

        /**
         * Add a database URI to fetch, along with its parser
         * @param T the dedicated type of parser
         * @param database the database to set
         * @param parser the parser to use
         */
        fun <T : ServiceNamesPortNumbersMappingParser> addDatabaseAndParser(database: URI, parser: T? = null) =
                this.addDatabaseAndParser(database.toURL(), parser)

        /**
         * Add a database File to fetch, along with its parser
         * @param T the dedicated type of parser
         * @param database the database to set
         * @param parser the parser to use
         */
        fun <T : ServiceNamesPortNumbersMappingParser> addDatabaseAndParser(database: File, parser: T? = null) =
                this.addDatabaseAndParser(database.toURI(), parser)

        /**
         * Set the database URL for lookup
         * @param database the database to set
         */
        fun database(database: URL): Builder {
            this.databaseAndParserMap.clear()
            this.addDatabaseAndParser(database, null)
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
         * Set the database parser for lookup. This will be applied to all databases set.
         * @param T the dedicated type of parser
         * @param parser the parser to set
         */
        fun <T : ServiceNamesPortNumbersMappingParser> parser(parser: T): Builder {
            this.databaseAndParserMap.replaceAll { _, _ -> parser }
            return this
        }

        /**
         * Construct a new instance of [ServiceNamesPortNumbersClient],
         * with the parameters set beforehand, or with the default settings
         */
        fun build(): ServiceNamesPortNumbersClient {
            return ServiceNamesPortNumbersClient(
                    cacheMaximumSize = this.cacheMaximumSize ?: DEFAULT_CACHE_SIZE,
                    cacheExpiration = this.cacheExpiration ?: DEFAULT_CACHE_EXPIRATION_DAYS to TimeUnit.DAYS,
                    databaseAndParserMap = this.databaseAndParserMap
                            .mapKeys { it.key ?: URL(IANA_XML_DB_URL) }
                            .mapValues { it.value ?: IANA_XML_DB_PARSER }
                            .toMutableMap()
            )
        }
    }


}