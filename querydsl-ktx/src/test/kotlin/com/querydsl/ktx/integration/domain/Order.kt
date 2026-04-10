package com.querydsl.ktx.integration.domain

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
class Order(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    val member: Member? = null,
    val productName: String = "",
    val amount: Int = 0,
    @Enumerated(EnumType.STRING)
    val orderStatus: OrderStatus = OrderStatus.PENDING,
    val orderedAt: LocalDateTime = LocalDateTime.now(),
)

enum class OrderStatus { PENDING, CONFIRMED, CANCELLED }
