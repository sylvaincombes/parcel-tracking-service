package com.shoplane.parceltracking.infra.persistence

import com.shoplane.parceltracking.domain.parcel.ParcelStatistics
import com.shoplane.parceltracking.domain.parcel.ParcelStatus
import com.shoplane.parceltracking.domain.parcel.SellerId
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository

@Repository
class JdbcParcelStatistics(private val jdbc: JdbcClient) : ParcelStatistics {
    override fun countWithCarrier(sellerId: SellerId): Long =
        jdbc.sql(COUNT_WITH_CARRIER)
            .param("sellerId", sellerId.value)
            .query(Long::class.java)
            .single()

    private companion object {
        val WITH_CARRIER = ParcelStatus.entries.filterNot { it.isTerminal }
        val COUNT_WITH_CARRIER =
            "select count(*) from parcel where seller_id = :sellerId " +
                "and status in (${WITH_CARRIER.joinToString { "'${it.name}'" }})"
    }
}
