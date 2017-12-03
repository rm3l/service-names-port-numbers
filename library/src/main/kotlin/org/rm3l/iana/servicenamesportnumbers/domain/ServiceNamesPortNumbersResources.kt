@file:JvmName("Domain")
package org.rm3l.iana.servicenamesportnumbers.domain

/**
 * IANA Record
 *
 * @param serviceName the service name
 * @param portNumber the port number
 * @param transportProtocol the transport protocol. See [Protocol] for further details
 * @param description a description for the record
 * @param assignee the assignee person
 * @param contact the contact person
 * @param registrationDate the registration date
 * @param modificationDate the last modification date of the record
 * @param reference a reference for the record
 * @param serviceCode the service code
 * @param knownUnauthorizedUses the known authorized uses of this record
 * @param assignmentNotes the assignment notes of the record
 */
@Suppress("MemberVisibilityCanPrivate")
data class Record(
        /**
         * the service name
         */
        val serviceName: String? = null,

        /**
         * the port number
         */
        val portNumber: Long? = null,

        /**
         * the transport protocol. See [Protocol] for further details
         */
        val transportProtocol: Protocol? = null,

        /**
         * a description for the record
         */
        val description: String? = null,

        /**
         * the assignee person
         */
        val assignee: Person? = null,

        /**
         * the contact person
         */
        val contact: Person? = null,

        /**
         * the registration date
         */
        val registrationDate: String? = null,

        /**
         * the last modification date of the record
         */
        val modificationDate: String? = null,

        /**
         * a reference for the record
         */
        val reference: String? = null,

        /**
         * the service code
         */
        val serviceCode: String? = null,

        /**
         * the known authorized uses of this record
         */
        val knownUnauthorizedUses: String? = null,

        /**
         * the assignment notes of the record
         */
        val assignmentNotes: String? = null)

/**
 * Person
 *
 * @param id a unique identifier
 * @param name the person name
 * @param org the organization the person belongs to (at the time of the registration)
 * @param uri an URI for this person, if any
 * @param updated the last update of this information
 */
@Suppress("MemberVisibilityCanPrivate")
data class Person(
        /**
         * a unique identifier
         */
        val id: String,

        /**
         * the person name
         */
        val name: String,

        /**
         * the organization the person belongs to (at the time of the registration)
         */
        val org: String? = null,

        /**
         * an URI for this person, if any
         */
        val uri: String? = null,

        /**
         * the last update of this information
         */
        val updated: String? = null)

/**
 * Protocol
 */
enum class Protocol {
    /**
     * Transmission Control Protocol
     */
    TCP,

    /**
     * User Datagram Protocol
     */
    UDP,

    /**
     * Datagram Congestion Control Protocol
     */
    DCCP,

    /**
     * Stream Control Transmission Protocol
     */
    SCTP
}

/**
 * Filter used for data lookup
 *
 * @param services the list of service names to lookup
 * @param protocols the list of [Protocol]s to lookup
 * @param ports the list of ports to lookup
 */
data class RecordFilter(
        /**
         * the list of service names to lookup
         */
        val services: List<String>? = null,

        /**
         * the list of [Protocol]s to lookup
         */
        val protocols: List<Protocol>? = null,

        /**
         * the list of ports to lookup
         */
        val ports: List<Long>? = null) {

    /**
     * Determines whether the filter is null or empty
     * @return whether the filter is null or empty
     */
    fun isEmpty(): Boolean {
        return services?.isEmpty()?:true
                && protocols?.isEmpty()?:true
                && ports?.isEmpty()?:true
    }

    companion object {
        /**
         * An empty filter
         */
        val EMPTY = RecordFilter()
    }
}
