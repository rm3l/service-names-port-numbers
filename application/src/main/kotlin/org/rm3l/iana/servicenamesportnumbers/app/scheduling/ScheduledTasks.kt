package org.rm3l.iana.servicenamesportnumbers.app.scheduling

import org.rm3l.iana.servicenamesportnumbers.IANAServiceNamesPortNumbersClient
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ScheduledTasks(val registryClient: IANAServiceNamesPortNumbersClient) {

    private val logger = LoggerFactory.getLogger(ScheduledTasks::class.java)

    @Scheduled(cron = "\${cacheRefresh.cron.expression}")
    fun refreshCache() {
        try {
            logger.info("Updating DB ... ")
            registryClient.refreshCache()
            logger.info("... Task scheduled. Will be refreshed soon.")
        } catch (e: Exception) {
            if (logger.isDebugEnabled) {
                logger.debug(e.message, e)
            }
        }
    }
}