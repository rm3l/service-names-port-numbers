package org.rm3l.iana.servicenamesportnumbers

import org.junit.Assert
import org.junit.Test
import org.rm3l.iana.servicenamesportnumbers.domain.Protocol
import org.rm3l.iana.servicenamesportnumbers.domain.RecordFilter

class RecordFilterTest {

    @Test
    fun testEquality() {
        val filter1 = RecordFilter(listOf("service1", "service2"), listOf(Protocol.TCP), listOf(10L))
        val filter2 = RecordFilter(listOf("service1", "service2"), listOf(Protocol.TCP), listOf(10L))
        val filter3 = RecordFilter(listOf("service1", "service2"), listOf(Protocol.SCTP), listOf(10L))
        val filter4 = RecordFilter(listOf("service1"), listOf(Protocol.TCP), listOf(10L))
        Assert.assertEquals(filter1, filter2)
        Assert.assertNotEquals(filter1, filter3)
        Assert.assertNotEquals(filter1, filter4)
        Assert.assertNotEquals(filter3, filter4)
    }

}