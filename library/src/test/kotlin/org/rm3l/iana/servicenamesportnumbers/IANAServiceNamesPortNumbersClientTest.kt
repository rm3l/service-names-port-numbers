package org.rm3l.iana.servicenamesportnumbers

import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.rm3l.iana.servicenamesportnumbers.domain.Protocol
import org.rm3l.iana.servicenamesportnumbers.domain.RecordFilter

class IANAServiceNamesPortNumbersClientTest {

    companion object {

        private var defaultClient: IANAServiceNamesPortNumbersClient? = null

        @JvmStatic
        @BeforeClass
        fun beforeClass() {
            this.defaultClient = IANAServiceNamesPortNumbersClient.builder().build()
        }
    }

    @Test
    @Throws(Exception::class)
    fun testQueryPorts() {
        val records = defaultClient!!.query(80)
        Assert.assertFalse(records.isEmpty())
        Assert.assertEquals(3, records.size) //TCP, UDP, SCTP
        val ports = records.map { it.portNumber }.toSet()
        Assert.assertEquals(1, ports.size)
        Assert.assertEquals(80L, ports.first())

        val serviceNames = records.map { it.serviceName }.toSet()
        Assert.assertEquals(1, serviceNames.size)
        Assert.assertEquals("http", serviceNames.first())

        val protocols = records.map { it.transportProtocol }.toSet()
        Assert.assertEquals(3, protocols.size)
        Assert.assertTrue(protocols.contains(Protocol.TCP))
        Assert.assertTrue(protocols.contains(Protocol.UDP))
        Assert.assertTrue(protocols.contains(Protocol.SCTP))
    }

    @Test
    @Throws(Exception::class)
    fun testQueryServiceNames() {
        val records = defaultClient!!.query("http")
        Assert.assertFalse(records.isEmpty())
        Assert.assertEquals(3, records.size) //TCP, UDP, SCTP
        val ports = records.map { it.portNumber }.toSet()
        Assert.assertEquals(1, ports.size)
        Assert.assertEquals(80L, ports.first())

        val serviceNames = records.map { it.serviceName }.toSet()
        Assert.assertEquals(1, serviceNames.size)
        Assert.assertEquals("http", serviceNames.first())

        val protocols = records.map { it.transportProtocol }.toSet()
        Assert.assertEquals(3, protocols.size)
        Assert.assertTrue(protocols.contains(Protocol.TCP))
        Assert.assertTrue(protocols.contains(Protocol.UDP))
        Assert.assertTrue(protocols.contains(Protocol.SCTP))
    }

    @Test
    @Throws(Exception::class)
    fun testQueryProtocols() {
        val records = defaultClient!!.query(Protocol.TCP)
        Assert.assertFalse(records.isEmpty())
        Assert.assertTrue(records.size > 100)
    }

    @Test
    @Throws(Exception::class)
    fun testQueryWithFilter() {
        val records = defaultClient!!.query(RecordFilter(
                services = listOf("http"),
                ports = listOf(80L),
                protocols = listOf(Protocol.TCP)))
        Assert.assertFalse(records.isEmpty())
        Assert.assertEquals(1, records.size)
        val record = records.first()
        Assert.assertEquals(80L, record.portNumber)
        Assert.assertEquals("http", record.serviceName)
        Assert.assertEquals(Protocol.TCP, record.transportProtocol)
    }
}