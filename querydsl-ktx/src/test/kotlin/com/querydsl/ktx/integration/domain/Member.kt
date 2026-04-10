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
@Table(name = "member")
class Member(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String = "",
    val age: Int = 0,
    @Enumerated(EnumType.STRING)
    val status: MemberStatus = MemberStatus.NORMAL,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    val department: Department? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

enum class MemberStatus { VIP, NORMAL, INACTIVE }
