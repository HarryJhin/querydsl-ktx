package com.querydsl.ktx.integration.repository

import com.querydsl.core.Tuple
import com.querydsl.ktx.case
import com.querydsl.ktx.integration.domain.Order
import com.querydsl.ktx.integration.domain.OrderStatus
import com.querydsl.ktx.integration.domain.QOrder
import com.querydsl.ktx.support.QuerydslRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class OrderRepository : QuerydslRepository<Order>() {
    private val order = QOrder.order

    fun findOrdersAfter(date: LocalDateTime? = null): List<Order> =
        selectFrom(order).where(order.orderedAt after date).fetch()

    fun findOrdersBefore(date: LocalDateTime? = null): List<Order> =
        selectFrom(order).where(order.orderedAt before date).fetch()

    fun findOrdersBetween(from: LocalDateTime? = null, to: LocalDateTime? = null): List<Order> =
        selectFrom(order).where(order.orderedAt between (from to to)).fetch()

    fun findOrdersWithPriorityLabel(): List<Tuple> {
        val priorityLabel = case<String> {
            `when`(order.amount.goe(100000)) then "HIGH"
            `when`(order.amount.goe(50000)) then "MEDIUM"
            otherwise("LOW")
        } ?: return emptyList()
        return select(order.productName, priorityLabel)
            .from(order)
            .fetch()
    }

    fun deleteByStatus(status: OrderStatus): Long =
        modifying {
            delete(order)
                .where(order.orderStatus.eq(status))
                .execute()
        }
}
