package org.rm3l.iana.servicenamesportnumbers.internal

import com.github.benmanes.caffeine.cache.Caffeine
import khttp.get
import org.rm3l.iana.servicenamesportnumbers.domain.Record
import org.rm3l.iana.servicenamesportnumbers.parsers.Format
import java.util.concurrent.TimeUnit

internal object ServiceNamePortNumberRegistryCache {

    private val DL_URL_FORMAT =
            "https://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.%s"

    val INSTANCE = Caffeine
            .newBuilder()
            .recordStats()
            .maximumSize(Format.values().size.toLong())
            .expireAfterWrite(1L, TimeUnit.DAYS)
            .expireAfterAccess(1L, TimeUnit.DAYS)
            .removalListener<Format, List<Record>> { key, _, cause ->
                println("Key $key was removed from cache : $cause")
            }
            .build<Format, List<Record>> { format ->
                //Blocking network call
                val url = DL_URL_FORMAT.format(format.name.toLowerCase())
                println("Loading data from '$url' ...")
                format.parser.parse(get(url).text)
            }

}