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
package org.rm3l.iana.servicenamesportnumbers

import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.rm3l.iana.servicenamesportnumbers.domain.Protocol
import org.rm3l.iana.servicenamesportnumbers.domain.RecordFilter

/**
 * Test class
 */
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
    fun testQueryProtocols() {
        val records = defaultClient!!.query(Protocol.TCP)
        Assert.assertFalse(records.isEmpty())
        Assert.assertTrue(records.size > 100)
    }

    @Test
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