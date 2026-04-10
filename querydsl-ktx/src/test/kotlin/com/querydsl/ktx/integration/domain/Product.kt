package com.querydsl.ktx.integration.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "product")
class Product(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String = "",
    val price: Int = 0,
    val category: String = "",
    val saleStartAt: LocalDateTime? = null,
    val saleEndAt: LocalDateTime? = null,
)
