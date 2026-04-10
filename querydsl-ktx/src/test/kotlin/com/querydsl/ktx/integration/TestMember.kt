package com.querydsl.ktx.integration

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "test_member")
class TestMember(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String = "",
    val age: Int = 0,
    @Enumerated(EnumType.STRING)
    val status: TestStatus = TestStatus.NORMAL,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

enum class TestStatus {
    VIP, NORMAL, INACTIVE
}
